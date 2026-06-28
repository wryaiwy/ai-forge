from agents.base_agent import BaseAgent
from app.schemas import ChatRequest, ChatResponse
from app.exceptions import AgentExecutionError
import uuid
import logging
import asyncio
from typing import Optional
from services.session_memory import SessionMemory
from app.config import settings
from services.vector_store import vector_store_service

logger = logging.getLogger(__name__)


class HomeChatAgent(BaseAgent):
    """
    首页AI对话助手
    处理多轮聊天逻辑，支持会话记忆
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

    # 用于保存后台任务的强引用，防止被 Python GC 提前回收
    _bg_tasks = set()

    def _fire_and_forget(self, coro):
        """安全地触发后台任务，消除警告并防止 GC 意外清理"""
        task = asyncio.create_task(coro)
        self._bg_tasks.add(task)
        task.add_done_callback(self._bg_tasks.discard)

    def __init__(self, db_session=None):
        super().__init__(system_prompt=self.SYSTEM_PROMPT, temperature=0.7)
        self.db_session = db_session
        self.memory = SessionMemory(max_tokens=settings.CHAT_MEMORY_MAX_TOKENS)

    def _get_or_create_session_id(self, session_id: Optional[str] = None) -> str:
        """获取或创建会话 ID"""
        return session_id or str(uuid.uuid4())

    def _get_history(self, session_id: str, long_term_context: str = "") -> list:
        """获取历史对话并注入上下文摘要和长期记忆"""
        history = self.memory.get_history(session_id)
        summary = self.memory.get_summary(session_id)
        
        system_content = []
        if summary:
            system_content.append(f"【过去对话摘要】\n{summary}")
        if long_term_context:
            system_content.append(f"【相关历史记忆片段】\n{long_term_context}")
            
        if system_content:
            # 将上下文作为 system 角色插入到最前面
            history.insert(0, {"role": "system", "content": "\n\n".join(system_content)})
            
        return history

    def _save_message(self, session_id: str, role: str, content: str):
        """保存消息到会话历史"""
        self.memory.add_message(session_id, role, content)

    async def save_to_long_term_memory(self, session_id: str, user_msg: str, ai_msg: str):
        """保存对话到长期向量记忆"""
        try:
            text = f"User: {user_msg}\nAI: {ai_msg}"
            metadata = {"session_id": session_id, "type": "chat_history"}
            await vector_store_service.add_documents([text], [metadata])
            logger.info(f"会话 {session_id} 长期记忆已入库")
        except Exception as e:
            logger.error(f"会话 {session_id} 长期记忆入库失败: {e}")

    async def check_and_compress_memory(self, session_id: str):
        """检查并压缩记忆"""
        try:
            # 1. 拿到所有近期对话
            session = self.memory.get_raw_history(session_id)
            if not session:
                return
            
            # 2. 判断是否超过阈值 (max_tokens 的 80%)
            total_tokens = sum(len(self.memory.encoding.encode(str(msg.get("content", "")))) + 4 for msg in session)
            
            if total_tokens > self.memory.max_tokens * 0.8:
                # 3. 触发压缩，保留最新的一半，把老的一半和已有摘要合并。
                keep_count = len(session) // 2
                if keep_count == 0:
                    return
                old_msgs = session[:-keep_count]
                
                old_summary = self.memory.get_summary(session_id)
                
                # 构建用于总结的 Prompt
                prompt = "请总结以下对话内容，提取关键信息。如果已有摘要，请将其与新对话内容合并，形成一个精简、连贯的新摘要。\n"
                if old_summary:
                    prompt += f"\n【已有摘要】\n{old_summary}\n"
                
                prompt += "\n【新对话内容】\n"
                for m in old_msgs:
                    prompt += f"{m['role']}: {m['content']}\n"
                    
                # 调用 LLM 总结 (注意此时不带 history)
                new_summary = await self.execute(user_input=prompt, history=None)
                
                # 保存新摘要
                self.memory.set_summary(session_id, new_summary)
                
                # 清理 Redis 中的旧消息
                self.memory.trim_history(session_id, keep_count)
                logger.info(f"会话 {session_id} 记忆压缩完成，更新了上下文摘要并清理了 {len(old_msgs)} 条旧消息。")
        except Exception as e:
            logger.error(f"会话 {session_id} 记忆压缩失败: {e}")

    async def run_stream(self, request: ChatRequest):
        """执行首页AI对话助手流式对话"""
        session_id = self._get_or_create_session_id(request.session_id)

        # 1. 从 ES 向量库检索相关长期记忆
        long_term_context = ""
        try:
            docs = await vector_store_service.similarity_search(
                query=request.message,
                k=3,
                filter_dict=[{"term": {"metadata.session_id.keyword": session_id}}]
            )
            if docs:
                long_term_context = "\n".join([doc.page_content for doc in docs])
        except Exception as e:
            logger.warning(f"长期记忆检索失败 (可能 ES 未启动): {e}")

        # 2. 从 Redis 获取截断后的历史记录，并注入摘要和长期记忆
        history = self._get_history(session_id, long_term_context)
        
        # 3. 将本次用户提问存入 Redis 记忆
        self._save_message(session_id, "user", request.message)

        full_response = ""
        try:
            async for chunk in self.execute_stream(
                    user_input=request.message,
                    history=history if history else None
            ):
                chunk_str = chunk if isinstance(chunk, str) else getattr(chunk, "content", str(chunk))
                if chunk_str:
                    full_response += chunk_str
                    data_str = chunk_str.replace("\n", "\ndata: ")
                    yield f"data: {data_str}\n\n"
        except Exception as e:
            logger.error(f"首页AI对话助手流式执行失败: {e}")
            raise AgentExecutionError(detail=f"AI 回复生成失败: {str(e)}")
        finally:
            self._save_message(session_id, "assistant", full_response)
            self._fire_and_forget(self.check_and_compress_memory(session_id))
            self._fire_and_forget(self.save_to_long_term_memory(session_id, request.message, full_response))

    async def run(self, request: ChatRequest) -> ChatResponse:
        """执行首页AI对话助手对话"""
        session_id = self._get_or_create_session_id(request.session_id)
        
        # 1. 从 ES 向量库检索相关长期记忆
        long_term_context = ""
        try:
            docs = await vector_store_service.similarity_search(
                query=request.message,
                k=3,
                filter_dict=[{"term": {"metadata.session_id.keyword": session_id}}]
            )
            if docs:
                long_term_context = "\n".join([doc.page_content for doc in docs])
        except Exception as e:
            logger.warning(f"长期记忆检索失败: {e}")

        # 2. 从 Redis 获取截断后的历史记录，并注入摘要和长期记忆
        history = self._get_history(session_id, long_term_context)
        
        # 3. 将本次用户提问存入 Redis 记忆
        self._save_message(session_id, "user", request.message)

        try:
            answer = await self.execute(
                user_input=request.message,
                history=history if history else None
            )
        except Exception as e:
            logger.error(f"首页AI对话助手执行失败: {e}")
            raise AgentExecutionError(detail=f"AI 回复生成失败: {str(e)}")

        self._save_message(session_id, "assistant", answer)
        self._fire_and_forget(self.check_and_compress_memory(session_id))
        self._fire_and_forget(self.save_to_long_term_memory(session_id, request.message, answer))

        return ChatResponse(
            answer=answer,
            session_id=session_id
        )
