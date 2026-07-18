from agents.base_agent import BaseAgent
from app.schemas import ChatRequest
from app.exceptions import AgentExecutionError
import uuid
import logging
import asyncio
from typing import Optional
from services.session_memory import SessionMemory
from app.config import settings
from services.retrieval_service import retrieval_service

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

    def _get_history(self, session_id: str, long_term_context: str = "", retrieval_context: str = "") -> list:
        """获取历史对话并注入上下文摘要、长期记忆和检索资料"""
        history = self.memory.get_history(session_id)
        summary = self.memory.get_summary(session_id)
        
        system_content = []
        if summary:
            system_content.append(f"【过去对话摘要】\n{summary}")
        if long_term_context:
            system_content.append(f"【相关历史记忆片段】\n{long_term_context}")
        if retrieval_context:
            system_content.append(f"【参考资料】\n{retrieval_context}\n\n请参考上述资料回答用户的问题。如果资料与问题无关，请忽略。")
            
        if system_content:
            # 将上下文作为 system 角色插入到最前面
            history.insert(0, {"role": "system", "content": "\n\n".join(system_content)})
            
        return history

    def _save_message(self, session_id: str, role: str, content: str):
        """保存消息到会话历史"""
        self.memory.add_message(session_id, role, content)

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

        # 1. 委托给 memory 获取长期上下文
        long_term_context = await self.memory.get_long_term_context(session_id, request.message)

        # 2. 检索相关资料
        try:
            retrieved_docs = await retrieval_service.retrieve(raw_query=request.message)
            retrieval_context = "\n\n".join([doc.document.page_content for doc in retrieved_docs]) if retrieved_docs else ""
            if retrieval_context:
                logger.info(f"【流式对话】注入Prompt的检索上下文片段:\n{retrieval_context[:200]}...")
            else:
                logger.info("【流式对话】检索结果为空，不注入上下文。")
        except Exception as e:
            logger.error(f"检索资料失败: {e}")
            retrieval_context = ""

        # 3. 从 Redis 获取截断后的历史记录，并注入上下文摘要、长期记忆和检索资料
        history = self._get_history(session_id, long_term_context, retrieval_context)
        
        # 4. 将本次用户提问存入 Redis 记忆
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
            self._fire_and_forget(self.memory.save_long_term_memory(session_id, request.message, full_response))
