from langchain_openai import ChatOpenAI
from langchain_ollama import ChatOllama
from app.config import settings
import logging
from typing import Optional

logger = logging.getLogger(__name__)


class LLMProvider:
    """
    LLM 调用统一封装
    支持 OpenAI 兼容接口和 Ollama 本地模型
    """
    _instance: Optional['LLMProvider'] = None
    _chat_model = None

    def __new__(cls) -> 'LLMProvider':
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def get_chat_model(self, temperature: float = 0.7):
        """获取聊天模型实例"""
        if self._chat_model is None:
            # 优先使用 OpenAI 兼容接口（如 DashScope）
            openai_api_key = settings.OPENAI_API_KEY
            if openai_api_key:
                logger.info(f"初始化 OpenAI 兼容 LLM: {settings.OPENAI_BASE_URL} / {settings.OPENAI_MODEL}")
                self._chat_model = ChatOpenAI(
                    openai_api_key=openai_api_key,
                    openai_api_base=settings.OPENAI_BASE_URL,
                    model_name=settings.OPENAI_MODEL,
                    temperature=temperature,
                )
            else:
                logger.info(f"初始化 Ollama LLM: {settings.OLLAMA_BASE_URL} / {settings.OLLAMA_LLM_MODEL}")
                self._chat_model = ChatOllama(
                    base_url=settings.OLLAMA_BASE_URL,
                    model=settings.OLLAMA_LLM_MODEL,
                    temperature=temperature,
                )
        return self._chat_model

    async def ainvoke(self, messages: list, temperature: float = 0.7) -> str:
        """异步调用 LLM 并返回纯文本"""
        model = self.get_chat_model(temperature=temperature)
        response = await model.ainvoke(messages)
        return response.content
