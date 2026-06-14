from agents.base_agent import BaseAgent
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from app.tools.web_search import WebSearchToolExecutor

class RetrievalAndCleanAgent(BaseAgent):
    """
    信息获取与噪音清洗 Agent
    根据大纲搜索信息，并增加总结清洗节点，提纯信息。
    """
    def __init__(self):
        super().__init__(temperature=0.3)
        self.llm = self._llm_provider.get_chat_model()
        self.search_tool = WebSearchToolExecutor()

    async def run(self, request) -> dict:
        # 1. 构造搜索词
        query = f"{request.topic} {request.section_title}"
        
        # 2. 执行搜索获取参考资料
        raw_results = self.search_tool.execute({"query": query})
        
        # 3. 利用 LLM 进行噪音清洗与提纯
        prompt = ChatPromptTemplate.from_template(
            "你是一个极其专业的资料提纯专家。\n"
            "下面是我刚从搜索引擎抓取到的网页原始内容，里面夹杂着大量无用的广告、菜单栏以及毫不相关的信息（噪音）。\n\n"
            "【当前代写任务主题】：{topic}\n"
            "【当前正在起草的段落】：{section_title}\n"
            "【该段落的核心要点】：\n{content_points}\n\n"
            "【搜索引擎抓取到的原始噪音数据】：\n{raw_results}\n\n"
            "【你的任务】：\n"
            "请仔细阅读这些原始数据，只萃取出对写这个段落**真正有用的事实、论点、数据和案例**。\n"
            "直接输出清洗、总结后的核心参考资料。切忌长篇大论，要求高密度、无废话。如果抓取到的内容完全无关，请回答“未找到有效参考信息”。\n\n"
            "【纯净版参考资料】："
        )
        chain = prompt | self.llm | StrOutputParser()
        
        cleaned_context = await chain.ainvoke({
            "topic": request.topic,
            "section_title": request.section_title,
            "content_points": "\n".join([f"- {p}" for p in request.content_points]),
            "raw_results": raw_results
        })
        
        return {
            "cleaned_context": cleaned_context,
            "source_query": query
        }
