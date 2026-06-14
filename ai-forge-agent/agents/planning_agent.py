from agents.base_agent import BaseAgent
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from app.schemas.plan import TaskPlanResponse

class PlanningAgent(BaseAgent):
    """
    需求解析与大纲生成 Agent
    将用户模糊需求转化为结构化的 JSON Task Plan。
    """
    def __init__(self):
        super().__init__(temperature=0.3)
        self.llm = self._llm_provider.get_chat_model()
        # 改用 JsonOutputParser 避免底层 function calling 兼容性导致的嵌套 key 问题
        self.parser = JsonOutputParser(pydantic_object=TaskPlanResponse)

    async def run(self, request) -> dict:
        prompt = ChatPromptTemplate.from_template(
            "你是一个资深的总编和内容架构师。\n"
            "用户提出了一个写作需求，请你深度分析其真实意图，并为其生成一篇结构清晰的写作大纲（Task Plan）。\n\n"
            "用户的需求是：\n{requirement}\n\n"
            "要求：\n"
            "1. 明确文章的主题、受众和基调。\n"
            "2. 将文章划分为合理的多个段落。\n"
            "3. 每个段落给出具体的要点提示和字数预估。\n"
            "{format_instructions}"
        )
        chain = prompt | self.llm | self.parser
        
        response_data = await chain.ainvoke({
            "requirement": request.requirement,
            "format_instructions": self.parser.get_format_instructions()
        })
        
        # 将解析好的 dict 通过 Pydantic 过滤一次确保合规后返回
        if isinstance(response_data, dict):
            # 自动解包：如果模型自己给 JSON 包了一层根节点 (如 {"article_plan": {...}})
            if len(response_data) == 1 and isinstance(list(response_data.values())[0], dict):
                response_data = list(response_data.values())[0]
            return TaskPlanResponse(**response_data).model_dump()
        return response_data.model_dump()
