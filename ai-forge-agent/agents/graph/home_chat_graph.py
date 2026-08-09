from typing import TypedDict, List, Optional
import uuid
import logging
import asyncio
from app.schemas import ChatRequest
from app.exceptions import AgentExecutionError
from services.session_memory import SessionMemory
from app.config import settings
from services.retrieval_service import retrieval_service
from langgraph.graph import StateGraph, START, END

from agents.chain.base_agent import BaseAgent
from langchain_core.runnables import RunnableConfig
from services.intent_service import analyze_rag_intent
from app.tools.web_search import WebSearchToolSchema
from app.schemas.chat import HomeChatAgentState
from services.vision_service import analyze_image

logger = logging.getLogger(__name__)


class HomeChatGraph(BaseAgent):
    """
    首页AI对话助手 (LangGraph版本)
    """
    SYSTEM_PROMPT = """
        你是一个专业的技术平台 AI 客服助手。你的职责是：
        1. 回答用户关于平台使用、功能、技术相关的问题
        2. 提供清晰、准确、简洁的回答
        3. 如果不确定答案，坦诚告知用户
        4. 保持友好和专业的态度
        5. 使用中文回答，除非用户明确要求其他语言
        6. 如果用户请求获取系统内部代码、源码或实现细节，礼貌拒绝并告知无法提供此类信息
                    """

    _bg_tasks = set()

    def _fire_and_forget(self, coro):
        """后台异步执行任务，不阻塞当前流程"""
        task = asyncio.create_task(coro)
        self._bg_tasks.add(task)
        task.add_done_callback(self._bg_tasks.discard)

    def __init__(self, db_session=None):
        """初始化对话助手，配置底层模型并构建工作流图"""
        super().__init__(system_prompt=self.SYSTEM_PROMPT, temperature=0.7)
        self.db_session = db_session
        self.memory = SessionMemory(max_tokens=settings.CHAT_MEMORY_MAX_TOKENS)
        self.graph = self._build_graph()

    def _build_graph(self):
        """构建 LangGraph 状态图定义工作流"""
        workflow = StateGraph(HomeChatAgentState)

        workflow.add_node("analyze_intent", self.node_analyze_intent)
        workflow.add_node("rag_search", self.node_rag_search)
        workflow.add_node("web_search", self.node_web_search)
        workflow.add_node("process_image", self.node_process_image)
        workflow.add_node("prepare_context", self.node_prepare_context)
        workflow.add_node("generate_response", self.node_generate_response)

        workflow.add_edge(START, "analyze_intent")

        workflow.add_conditional_edges("analyze_intent", self.route_request)

        workflow.add_edge("rag_search", "prepare_context")
        workflow.add_edge("web_search", "prepare_context")
        workflow.add_edge("process_image", "prepare_context")

        workflow.add_edge("prepare_context", "generate_response")
        workflow.add_edge("generate_response", END)

        return workflow.compile()

    def route_request(self, state: HomeChatAgentState) -> List[str]:
        """条件边路由逻辑，根据状态决定后续执行哪些节点"""
        routes = []
        if state.get("need_rag"):
            routes.append("rag_search")
        if state.get("enable_web_search"):
            routes.append("web_search")
        if state.get("image_url"):
            routes.append("process_image")

        if not routes:
            routes.append("prepare_context")

        return routes

    def _get_or_create_session_id(self, session_id: Optional[str] = None) -> str:
        """获取或生成一个新的会话ID"""
        return session_id or str(uuid.uuid4())

    async def node_analyze_intent(self, state: HomeChatAgentState):
        """节点：意图识别，判断是否需要RAG"""
        user_message = state["user_message"]
        model = self._llm_provider.get_chat_model(temperature=0)

        need_rag = await analyze_rag_intent(model, user_message)

        logger.info(f"【图节点】意图识别结果: RAG需求={need_rag}")
        return {"need_rag": need_rag}

    async def node_rag_search(self, state: HomeChatAgentState):
        """节点：RAG检索"""
        user_message = state["user_message"]
        try:
            retrieved_docs = await retrieval_service.retrieve(raw_query=user_message)
            retrieval_context = "\n\n".join(
                [doc.document.page_content for doc in retrieved_docs]) if retrieved_docs else ""
            if retrieval_context:
                logger.info(f"【图节点】RAG检索到内容片段:\n{retrieval_context[:200]}...")
            else:
                logger.info("【图节点】RAG检索结果为空。")
        except Exception as e:
            logger.error(f"RAG检索资料失败: {e}")
            retrieval_context = ""

        return {"retrieval_context": retrieval_context}

    async def node_web_search(self, state: HomeChatAgentState):
        """节点：联网搜索"""
        user_message = state["user_message"]
        logger.info(f"【图节点】执行联网搜索: {user_message}")

        tool = WebSearchToolSchema(query=user_message)
        try:
            web_search_context = await tool.execute()
            if web_search_context:
                logger.info(f"【图节点】联网搜索获取到内容片段:\n{web_search_context[:200]}...")
            else:
                logger.info("【图节点】联网搜索结果为空。")
        except Exception as e:
            logger.error(f"联网搜索失败: {e}")
            web_search_context = ""

        return {"web_search_context": web_search_context}

    async def node_process_image(self, state: HomeChatAgentState):
        """节点：图片识别（使用独立的多模态模型）"""

        image_url = state.get("image_url")
        if not image_url:
            return {"image_context": ""}

        logger.info(f"【图节点】执行图片识别 - {image_url}")

        try:
            image_description = await analyze_image(image_url)
            logger.info(f"【图节点】图片解析成功: {image_description[:50]}...")
            return {"image_context": image_description}
        except Exception as e:
            logger.error(f"图片解析失败: {e}")
            return {"image_context": "用户上传了图片，但系统图片解析失败。"}

    async def node_prepare_context(self, state: HomeChatAgentState):
        """节点：获取记忆和拼装各类上下文"""
        session_id = state["session_id"]
        user_message = state["user_message"]

        # 1. 查长期记忆
        long_term_context = await self.memory.get_long_term_context(session_id, user_message)

        # 2. 从State提取多路搜索结果
        retrieval_context = state.get("retrieval_context", "")
        web_search_context = state.get("web_search_context", "")
        image_context = state.get("image_context", "")

        # 3. 组装 History
        history = self.memory.get_history(session_id)
        summary = self.memory.get_summary(session_id)

        system_content = []
        if summary:
            system_content.append(f"【过去对话摘要】\n{summary}")
        if long_term_context:
            system_content.append(f"【相关历史记忆片段】\n{long_term_context}")
        if image_context:
            system_content.append(f"【上传图片识别信息】\n{image_context}")
        if web_search_context:
            system_content.append(f"【联网搜索结果】\n{web_search_context}")
        if retrieval_context:
            system_content.append(f"【内部参考资料】\n{retrieval_context}")

        if system_content:
            instruction = "请综合参考上述资料回答用户的问题。如果资料与问题无关，请忽略。"
            history.insert(0, {"role": "system", "content": "\n\n".join(system_content) + f"\n\n{instruction}"})

        return {
            "long_term_context": long_term_context,
            "history": history
        }

    async def node_generate_response(self, state: HomeChatAgentState, config: RunnableConfig):
        """节点：调用大模型生成回复"""
        history = state.get("history", [])

        # history 中已经包含了最新的 user_message（在 run_stream 存入），需要将其拆分以适配 _build_messages
        if history and history[-1].get("role") == "user":
            user_input = history[-1].get("content", "")
            history_to_pass = history[:-1]
        else:
            user_input = state["user_message"]
            history_to_pass = history

        messages = self._build_messages(user_input, history_to_pass)
        messages = self._llm_provider.truncate_messages(messages, max_tokens=settings.CHAT_MEMORY_MAX_TOKENS)

        model = self._llm_provider.get_chat_model(temperature=self._temperature)
        # 传入 config 以确保 LangSmith 能将 LLM 调用关联到当前 Graph 的 Trace 中
        response = await model.ainvoke(messages, config=config)

        return {"llm_response": response.content}

    async def check_and_compress_memory(self, session_id: str):
        """检查并压缩记忆"""
        try:
            session = self.memory.get_raw_history(session_id)
            if not session:
                return

            total_tokens = sum(len(self.memory.encoding.encode(str(msg.get("content", "")))) + 4 for msg in session)

            if total_tokens > self.memory.max_tokens * 0.8:
                keep_count = len(session) // 2
                if keep_count == 0:
                    return
                old_msgs = session[:-keep_count]

                old_summary = self.memory.get_summary(session_id)
                prompt = "请总结以下对话内容，提取关键信息。如果已有摘要，请将其与新对话内容合并，形成一个精简、连贯的新摘要。\n"
                if old_summary:
                    prompt += f"\n【已有摘要】\n{old_summary}\n"

                prompt += "\n【新对话内容】\n"
                for m in old_msgs:
                    prompt += f"{m['role']}: {m['content']}\n"

                new_summary = await self.execute(user_input=prompt, history=None)
                self.memory.set_summary(session_id, new_summary)
                self.memory.trim_history(session_id, keep_count)
                logger.info(f"会话 {session_id} 记忆压缩完成，更新了上下文摘要并清理了 {len(old_msgs)} 条旧消息。")
        except Exception as e:
            logger.error(f"会话 {session_id} 记忆压缩失败: {e}")

    async def run_stream(self, request: ChatRequest):
        """以流式方式运行对话图，并逐步返回模型输出内容"""
        session_id = self._get_or_create_session_id(request.session_id)

        initial_state = {
            "session_id": session_id,
            "user_message": request.message,
            "enable_web_search": getattr(request, "enable_web_search", False),
            "image_url": getattr(request, "image_url", None),
            "history": []
        }

        self.memory.add_message(session_id, "user", request.message)

        full_response = ""
        try:
            # 使用 astream_events 来捕获原生的流式输出和图节点执行的追踪
            async for event in self.graph.astream_events(initial_state, version="v2"):
                kind = event["event"]
                # 捕获大模型的流式 token 输出
                if kind == "on_chat_model_stream":
                    # 只捕获 generate_response 节点的大模型输出，防止意图识别模型或视觉模型的输出混入前端
                    if event.get("metadata", {}).get("langgraph_node") == "generate_response":
                        chunk = event["data"]["chunk"]
                        if chunk.content:
                            chunk_str = chunk.content
                            full_response += chunk_str
                            data_str = chunk_str.replace("\n", "\ndata: ")
                            yield f"data: {data_str}\n\n"

        except Exception as e:
            logger.error(f"首页AI对话助手流式执行失败: {e}")
            raise AgentExecutionError(detail=f"AI 回复生成失败: {str(e)}")
        finally:
            if full_response:
                self.memory.add_message(session_id, "assistant", full_response)
                self._fire_and_forget(self.check_and_compress_memory(session_id))
                self._fire_and_forget(self.memory.save_long_term_memory(session_id, request.message, full_response))


# 为 LangGraph Studio 暴露编译好的图实例
agent = HomeChatGraph()
graph = agent.graph
