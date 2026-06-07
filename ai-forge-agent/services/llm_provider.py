import logging
import tiktoken
from typing import Optional, List, Any
from tenacity import retry, stop_after_attempt, wait_exponential
from langchain_openai import ChatOpenAI
from langchain_ollama import ChatOllama
from app.config import settings

logger = logging.getLogger(__name__)

class LLMProvider:
    """
    LLM 调用统一封装
    支持 OpenAI 兼容接口和 Ollama 本地模型
    实现重试、Token截断、Function Calling绑定
    """

    # 初始化时创建单例实例
    _instance: Optional['LLMProvider'] = None
    _chat_model = None
    
    # 单例模式实现
    def __new__(cls) -> 'LLMProvider':
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance


    def get_chat_model(self, temperature: float = 0.7):
        """获取聊天模型实例"""
        if self._chat_model is None:
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
        # 动态更新 temperature (如果是可变属性)
        if hasattr(self._chat_model, "temperature"):
            self._chat_model.temperature = temperature
        return self._chat_model

    def _count_tokens(self, text: str) -> int:
        try:
            encoding = tiktoken.encoding_for_model(settings.OPENAI_MODEL)
        except Exception:
            encoding = tiktoken.get_encoding("cl100k_base")
        return len(encoding.encode(text))

    def truncate_messages(self, messages: List[Any], max_tokens: int = 4000) -> List[Any]:
        """Token 截断：保留 system prompt，按从新到旧保留消息"""
        if not messages:
            return []
        
        system_messages = [m for m in messages if getattr(m, "type", "") == "system" or getattr(m, "role", "") == "system"]
        other_messages = [m for m in messages if m not in system_messages]
        
        def get_msg_content(m):
            if isinstance(m, dict):
                return str(m.get("content", ""))
            return getattr(m, "content", "") or str(m)

        sys_tokens = sum(self._count_tokens(get_msg_content(m)) for m in system_messages)
        
        if sys_tokens >= max_tokens:
            return system_messages
            
        remaining_tokens = max_tokens - sys_tokens
        retained_others = []
        
        for m in reversed(other_messages):
            tokens = self._count_tokens(get_msg_content(m))
            if remaining_tokens - tokens >= 0:
                retained_others.insert(0, m)
                remaining_tokens -= tokens
            else:
                break
                
        return system_messages + retained_others

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        reraise=True
    )
    async def ainvoke(self, messages: List[Any], temperature: float = 0.7, tools: Optional[List[Any]] = None, max_tokens: int = 4000) -> Any:
        """
        异步调用 LLM
        支持重试、Token截断、Function Calling绑定
        """
        truncated_messages = self.truncate_messages(messages, max_tokens=max_tokens)
        model = self.get_chat_model(temperature=temperature)
        
        if tools:
            # 只有支持 bind_tools 的模型才能绑定，例如 ChatOpenAI
            if hasattr(model, "bind_tools"):
                model = model.bind_tools(tools)
            else:
                logger.warning(f"当前模型 {type(model).__name__} 不支持 bind_tools，工具绑定将被忽略。")
            
        response = await model.ainvoke(truncated_messages)
        return response

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10),
        reraise=True
    )
    async def astream(self, messages: List[Any], temperature: float = 0.7, max_tokens: int = 4000):
        """异步流式调用 LLM 并返回纯文本片段，带重试和截断"""
        truncated_messages = self.truncate_messages(messages, max_tokens=max_tokens)
        model = self.get_chat_model(temperature=temperature)
        
        async for chunk in model.astream(truncated_messages):
            yield chunk.content
