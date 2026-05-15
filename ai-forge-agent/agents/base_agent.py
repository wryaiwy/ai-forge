from abc import ABC, abstractmethod
from typing import List, Optional
from langchain_core.messages import BaseMessage, HumanMessage, AIMessage, SystemMessage
from services.llm_provider import LLMProvider
import logging

logger = logging.getLogger(__name__)


class BaseAgent(ABC):
    """
    Agent 抽象基类
    定义通用的思考、执行规范
    """

    def __init__(self, system_prompt: str = "", temperature: float = 0.7):
        self._llm_provider = LLMProvider()
        self._system_prompt = system_prompt
        self._temperature = temperature

    def _build_messages(self, user_input: str, history: Optional[List[dict]] = None) -> List[BaseMessage]:
        """构建 LLM 消息列表"""
        messages = []

        if self._system_prompt:
            messages.append(SystemMessage(content=self._system_prompt))

        if history:
            for msg in history:
                role = msg.get("role", "user")
                content = msg.get("content", "")
                if role == "user":
                    messages.append(HumanMessage(content=content))
                elif role == "assistant":
                    messages.append(AIMessage(content=content))
                elif role == "system":
                    messages.append(SystemMessage(content=content))

        messages.append(HumanMessage(content=user_input))
        return messages

    async def execute(self, user_input: str, history: Optional[List[dict]] = None) -> str:
        """执行 Agent 推理"""
        messages = self._build_messages(user_input, history)
        logger.debug(f"[{self.__class__.__name__}] 执行推理, input_length={len(user_input)}")

        try:
            response = await self._llm_provider.ainvoke(
                messages=messages,
                temperature=self._temperature
            )
            return response
        except Exception as e:
            logger.error(f"[{self.__class__.__name__}] 推理失败: {e}")
            raise

    @abstractmethod
    async def run(self, **kwargs) -> dict:
        """
        子类必须实现的业务入口方法
        返回标准化的 dict 响应
        """
        pass
