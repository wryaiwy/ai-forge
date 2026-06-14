from agents.base_agent import BaseAgent
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser, JsonOutputParser
from app.schemas.draft import CriticFeedback
import logging

logger = logging.getLogger(__name__)

class DraftAndCriticAgent(BaseAgent):
    """
    长文本起草与 Critic 反思 Agent
    分段生成长文，并引入 Critic Agent 自核防跑题。如果跑题，则自动打回重写。
    """
    def __init__(self):
        # 起草时适当提高 temperature，增加一点文本多样性
        super().__init__(temperature=0.6)
        self.llm = self._llm_provider.get_chat_model()
        # Critic 必须严谨，并输出结构化的反馈
        self.critic_parser = JsonOutputParser(pydantic_object=CriticFeedback)

    async def run(self, request) -> dict:
        max_retries = 2
        draft_content = ""
        critic_result = None
        
        draft_prompt = ChatPromptTemplate.from_template(
            "你是一个顶尖的商业撰稿人。\n"
            "请根据以下设定，起草文章的一个段落。\n\n"
            "【总主题】：{topic}\n"
            "【本段标题】：{section_title}\n"
            "【目标字数】：{word_count_estimate} 字左右\n"
            "【本段必须涵盖的要点】：\n{content_points}\n\n"
            "【可用的纯净背景资料】：\n{cleaned_context}\n\n"
            "【之前的打回修改建议】：\n{previous_feedback}\n\n"
            "【要求】：\n"
            "1. 严格遵守字数和段落要点，不要瞎编背景资料外的内容。\n"
            "2. 语言流畅，专业且富有吸引力。\n"
            "3. 如果有【修改建议】，请务必针对建议进行改正。\n"
            "请直接输出正文内容："
        )
        
        critic_prompt = ChatPromptTemplate.from_template(
            "你是一个极其严格的主编 (Critic)。\n"
            "你需要审查撰稿人写的段落是否跑题，或者是否遗漏了核心要点。\n\n"
            "【总主题】：{topic}\n"
            "【段落标题】：{section_title}\n"
            "【必须涵盖的要点】：\n{content_points}\n\n"
            "【撰稿人提交的草稿】：\n{draft_content}\n\n"
            "【审查标准】：\n"
            "1. 草稿是否严重跑题？\n"
            "2. 是否完全覆盖了要求涵盖的要点？\n"
            "3. 内容是否空洞无物？\n"
            "如果草稿合格，passed 设为 true，并给出简短评价；\n"
            "如果草稿不合格，passed 设为 false，并给出犀利的修改意见！\n"
            "{format_instructions}"
        )
        
        draft_chain = draft_prompt | self.llm | StrOutputParser()
        critic_chain = critic_prompt | self.llm | self.critic_parser
        
        content_points_str = "\n".join([f"- {p}" for p in request.content_points])
        
        for attempt in range(max_retries):
            logger.info(f"Drafting attempt {attempt + 1}/{max_retries} for section: {request.section_title}")
            
            # 1. Drafting Phase
            previous_feedback = critic_result.feedback if critic_result and not critic_result.passed else "无"
            draft_content = await draft_chain.ainvoke({
                "topic": request.topic,
                "section_title": request.section_title,
                "content_points": content_points_str,
                "cleaned_context": request.cleaned_context,
                "word_count_estimate": request.word_count_estimate,
                "previous_feedback": previous_feedback
            })
            
            # 2. Critic Phase
            critic_data = await critic_chain.ainvoke({
                "topic": request.topic,
                "section_title": request.section_title,
                "content_points": content_points_str,
                "draft_content": draft_content,
                "format_instructions": self.critic_parser.get_format_instructions()
            })
            
            if isinstance(critic_data, dict):
                # 自动解包
                if len(critic_data) == 1 and isinstance(list(critic_data.values())[0], dict):
                    critic_data = list(critic_data.values())[0]
                critic_result = CriticFeedback(**critic_data)
            else:
                critic_result = critic_data
            
            logger.info(f"Critic Result -> passed: {critic_result.passed}, feedback: {critic_result.feedback}")
            
            if critic_result.passed:
                break
                
        return {
            "draft_content": draft_content,
            "critic_feedback": critic_result.feedback if critic_result else "未审查",
            "passed": critic_result.passed if critic_result else False,
            "attempts": attempt + 1
        }
