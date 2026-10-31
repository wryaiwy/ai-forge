from typing import TypedDict, Optional, AsyncGenerator, Literal
import logging
from langgraph.graph import StateGraph, START, END
from langchain_core.messages import SystemMessage, HumanMessage
from langchain_core.runnables import RunnableConfig
from services.llm_provider import LLMProvider

logger = logging.getLogger(__name__)


class ArticleGraphState(TypedDict, total=False):
    """
    文章处理 Graph 状态 (DTO)
    """
    task_type: Literal["polish", "translate"]
    content: str
    polish_mode: Optional[int]
    target_style: Optional[str]
    target_lang: Optional[str]
    system_prompt: str
    user_prompt: str
    output: str


# 提示词模板
POLISH_PROMPTS = {
    1: "你是一名专业的技术文章语法校对专家。请修正文章中的语法错误、拼写错误、标点符号错误，保持原文的写作风格和结构不变。只修正明显的错误，不要大幅改动原文内容。保持 Markdown 格式不变。",
    2: "你是一名资深的技术博客编辑。请对文章进行全面润色，包括：优化语句表达使其更加流畅专业、改善段落结构和逻辑衔接、增强技术描述的准确性、统一术语使用。保持 Markdown 格式和原文的核心观点不变。",
    3: "你是一名专业的写作风格转换专家。请将文章改写为以下风格：{style_desc}。保持文章的核心内容和技术准确性不变，只调整语言风格和表达方式。保持 Markdown 格式不变。"
}

LANG_MAP = {
    "en": "英语",
    "zh": "中文",
    "ja": "日语",
    "ko": "韩语",
    "fr": "法语",
    "de": "德语",
    "es": "西班牙语"
}


class ArticleGraph:
    """
    文章智能处理图 (LangGraph 版本：集成润色与翻译)
    """

    def __init__(self, temperature: float = 0.3):
        self._llm_provider = LLMProvider()
        self.temperature = temperature
        self.graph = self._build_graph()

    def _build_graph(self):
        workflow = StateGraph(ArticleGraphState)

        # 注册节点
        workflow.add_node("prepare_polish", self.node_prepare_polish)
        workflow.add_node("prepare_translate", self.node_prepare_translate)
        workflow.add_node("generate_response", self.node_generate_response)

        # 条件边路由
        def route_task(state: ArticleGraphState) -> str:
            return "prepare_polish" if state.get("task_type") == "polish" else "prepare_translate"

        workflow.add_conditional_edges(
            START,
            route_task,
            {
                "prepare_polish": "prepare_polish",
                "prepare_translate": "prepare_translate"
            }
        )

        # 统一汇入生成节点
        workflow.add_edge("prepare_polish", "generate_response")
        workflow.add_edge("prepare_translate", "generate_response")
        workflow.add_edge("generate_response", END)

        return workflow.compile()

    # 节点 1：组装润色 Prompt
    def node_prepare_polish(self, state: ArticleGraphState) -> dict:
        mode = state.get("polish_mode") or 2
        if mode == 3:
            style = state.get("target_style")
            style_desc = style.strip() if style and style.strip() else "简洁、专业、适合技术博客发布"
            system_prompt = POLISH_PROMPTS[3].format(style_desc=style_desc)
        else:
            system_prompt = POLISH_PROMPTS.get(mode, POLISH_PROMPTS[2])

        user_prompt = f"请润色以下 Markdown 文章内容，直接返回润色后的完整内容，不要添加任何额外解释：\n\n{state['content']}"
        return {"system_prompt": system_prompt, "user_prompt": user_prompt}

    # 节点 2：组装翻译 Prompt
    def node_prepare_translate(self, state: ArticleGraphState) -> dict:
        lang_code = (state.get("target_lang") or "en").lower()
        lang_desc = LANG_MAP.get(lang_code, lang_code)
        system_prompt = (
            f"你是一名专业的技术文章翻译专家。请将以下 Markdown 文章内容翻译为{lang_desc}。"
            "要求：1. 保持 Markdown 格式不变；2. 技术术语翻译准确，必要时可在括号中保留原文；"
            "3. 保持原文的段落结构和逻辑；4. 译文应自然流畅，符合目标语言的表达习惯。"
            "直接返回翻译后的完整内容，不要添加任何额外解释。"
        )
        user_prompt = f"请将以下内容翻译为{lang_desc}：\n\n{state['content']}"
        return {"system_prompt": system_prompt, "user_prompt": user_prompt}

    # 节点 3：调用大模型
    async def node_generate_response(self, state: ArticleGraphState, config: RunnableConfig) -> dict:
        model = self._llm_provider.get_chat_model(temperature=self.temperature)
        messages = [
            SystemMessage(content=state["system_prompt"]),
            HumanMessage(content=state["user_prompt"])
        ]
        response = await model.ainvoke(messages, config=config)
        return {"output": response.content}

    # 原生流式输出方法 (兼容 Langfuse 跟踪)
    async def run_stream(self, initial_state: ArticleGraphState) -> AsyncGenerator[str, None]:
        task_type = initial_state.get("task_type", "article")
        run_config = {
            "run_name": f"article_{task_type}_graph",
            "metadata": {
                "langfuse_trace_name": f"article_{task_type}",
                "langfuse_tags": ["article_graph", task_type],
            },
        }

        try:
            async for event in self.graph.astream_events(initial_state, config=run_config, version="v2"):
                # 捕获大模型在 generate_response 节点的逐 Token 输出
                if event["event"] == "on_chat_model_stream":
                    if event.get("metadata", {}).get("langgraph_node") == "generate_response":
                        chunk = event["data"]["chunk"]
                        if chunk.content:
                            data_str = chunk.content.replace("\n", "\ndata: ")
                            yield f"data: {data_str}\n\n"
        except Exception as e:
            logger.error(f"文章智能图流式执行失败: {e}", exc_info=True)
            yield f"data: [ERROR] 处理失败: {str(e)}\n\n"

    async def run(self, initial_state: ArticleGraphState) -> str:
        """非流式调用"""
        result = await self.graph.ainvoke(initial_state)
        return result.get("output", "")


# 暴露单例实例供 API 与 LangGraph Studio 使用
article_graph = ArticleGraph()
graph = article_graph.graph

