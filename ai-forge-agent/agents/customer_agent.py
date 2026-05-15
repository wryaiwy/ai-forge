from agents.base_agent import BaseAgent
from app.schemas import ChatRequest, ChatResponse
from app.exceptions import AgentExecutionError
import uuid
import logging
from typing import Optional

logger = logging.getLogger(__name__)


class CustomerAgent(BaseAgent):
    """
    客服 Agent
    处理多轮聊天逻辑，支持会话记忆
    """

    SYSTEM_PROMPT = """
        你是一个专业的技术平台 AI 客服助手。你的职责是：
        1. 回答用户关于平台使用、功能、技术相关的问题
        2. 提供清晰、准确、简洁的回答
        3. 如果不确定答案，坦诚告知用户
        4. 保持友好和专业的态度
        5. 使用中文回答，除非用户明确要求其他语言
                    """

    def __init__(self, db_session=None):
        super().__init__(system_prompt=self.SYSTEM_PROMPT, temperature=0.7)
        self.db_session = db_session
        self._conversations: dict = {}

    def _get_or_create_conversation_id(self, conversation_id: Optional[str] = None) -> str:
        """获取或创建会话 ID"""
        if conversation_id and conversation_id in self._conversations:
            return conversation_id
        new_id = conversation_id or str(uuid.uuid4())
        if new_id not in self._conversations:
            self._conversations[new_id] = []
        return new_id

    def _get_history(self, conversation_id: str, max_turns: int = 10) -> list:
        """获取历史对话"""
        history = self._conversations.get(conversation_id, [])
        return history[-max_turns * 2:]

    def _save_message(self, conversation_id: str, role: str, content: str):
        """保存消息到会话历史"""
        if conversation_id not in self._conversations:
            self._conversations[conversation_id] = []
        self._conversations[conversation_id].append({"role": role, "content": content})

    async def run(self, request: ChatRequest) -> ChatResponse:
        """执行客服对话"""
        conversation_id = self._get_or_create_conversation_id(request.conversation_id)
        history = self._get_history(conversation_id)

        self._save_message(conversation_id, "user", request.message)

        try:
            answer = await self.execute(
                user_input=request.message,
                history=history if history else None
            )
        except Exception as e:
            logger.error(f"客服 Agent 执行失败: {e}")
            raise AgentExecutionError(detail=f"AI 回复生成失败: {str(e)}")

        self._save_message(conversation_id, "assistant", answer)

        return ChatResponse(
            answer=answer,
            conversation_id=conversation_id
        )
