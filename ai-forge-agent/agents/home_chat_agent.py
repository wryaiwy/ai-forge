from agents.base_agent import BaseAgent
from app.schemas import ChatRequest, ChatResponse
from app.exceptions import AgentExecutionError
import uuid
import logging
from typing import Optional

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

    def __init__(self, db_session=None):
        super().__init__(system_prompt=self.SYSTEM_PROMPT, temperature=0.7)
        self.db_session = db_session
        from services.session_memory import SessionMemory
        from app.config import settings
        self.memory = SessionMemory(max_tokens=settings.CHAT_MEMORY_MAX_TOKENS)

    def _get_or_create_session_id(self, session_id: Optional[str] = None) -> str:
        """获取或创建会话 ID"""
        return session_id or str(uuid.uuid4())

    def _get_history(self, session_id: str) -> list:
        """获取历史对话"""
        return self.memory.get_history(session_id)

    def _save_message(self, session_id: str, role: str, content: str):
        """保存消息到会话历史"""
        self.memory.add_message(session_id, role, content)

    async def run_stream(self, request: ChatRequest):
        """执行首页AI对话助手流式对话"""
        session_id = self._get_or_create_session_id(request.session_id)

        # 1. 从 Redis 获取截断后的历史记录（不包含本次提问）
        history = self._get_history(session_id)
        
        # 2. 将本次用户提问存入 Redis 记忆
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

    async def run(self, request: ChatRequest) -> ChatResponse:
        """执行首页AI对话助手对话"""
        session_id = self._get_or_create_session_id(request.session_id)
        
        # 1. 从 Redis 获取截断后的历史记录（不包含本次提问）
        history = self._get_history(session_id)
        
        # 2. 将本次用户提问存入 Redis 记忆
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

        return ChatResponse(
            answer=answer,
            session_id=session_id
        )
