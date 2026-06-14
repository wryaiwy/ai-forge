from abc import ABC, abstractmethod
from typing import List, Optional, Any
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
            return response.content if hasattr(response, "content") else str(response)
        except Exception as e:
            logger.error(f"[{self.__class__.__name__}] 推理失败: {e}")
            raise

    def _parse_fallback_tool_calls(self, content: str) -> List[dict]:
        """尝试从纯文本内容中解析兼容模式的工具调用，以支持解耦和扩展"""
        import json
        import uuid
        
        content_str = content.strip()
        
        # 兼容格式1: 阿里云 Qwen 兼容模式 (以 "call\n{" 开头)
        if content_str.startswith("call\n{"):
            try:
                json_str = content_str[5:]
                call_dict = json.loads(json_str)
                if "name" in call_dict and "arguments" in call_dict:
                    return [{
                        "name": call_dict["name"],
                        "args": call_dict["arguments"],
                        "id": f"call_{uuid.uuid4().hex[:8]}"
                    }]
            except Exception as e:
                logger.warning(f"[{self.__class__.__name__}] 尝试解析纯文本 tool_call 失败: {e}")
                
        # 如果未来有其他平台的奇怪格式，可以在这里继续扩展 elif
        
        return []

    async def execute_react(self, user_input: str, tools: List[Any], history: Optional[List[dict]] = None, max_iterations: int = 5) -> str:
        """执行带最大重试次数保护的 ReAct 循环"""
        from app.tools.tool_executor import ToolExecutor
        from langchain_core.messages import ToolMessage
        import json
        
        messages = self._build_messages(user_input, history)
        tool_executor = ToolExecutor()
        
        for iteration in range(max_iterations):
            logger.debug(f"[{self.__class__.__name__}] ReAct Loop Iteration {iteration + 1}/{max_iterations}")
            
            response = await self._llm_provider.ainvoke(
                messages=messages,
                temperature=self._temperature,
                tools=tools
            )
            
            messages.append(response)
            
            tool_calls = getattr(response, "tool_calls", [])
            
            # 兼容处理：部分模型兼容模式可能会把工具调用输出在纯文本里
            if not tool_calls and isinstance(response.content, str):
                tool_calls = self._parse_fallback_tool_calls(response.content)

            # 检查是否有 tool_calls
            if not tool_calls:
                # 没有工具调用，说明大模型给出了最终答案
                return response.content
                
            # 存在工具调用，遍历并执行
            for tool_call in tool_calls:
                tool_name = tool_call["name"]
                tool_args = tool_call["args"]
                tool_call_id = tool_call["id"]
                
                logger.info(f"[{self.__class__.__name__}] 调用工具: {tool_name}, args: {tool_args}")
                
                try:
                    result = await tool_executor.execute_tool(tool_name, tool_args)
                    result_str = json.dumps(result, ensure_ascii=False) if isinstance(result, (dict, list)) else str(result)
                except Exception as e:
                    logger.error(f"[{self.__class__.__name__}] 工具执行异常: {e}")
                    result_str = f"Error: {str(e)}"
                    
                tool_message = ToolMessage(
                    content=result_str,
                    name=tool_name,
                    tool_call_id=tool_call_id
                )
                messages.append(tool_message)
                
        logger.warning(f"[{self.__class__.__name__}] ReAct 循环达到最大重试次数 ({max_iterations})")
        return "很抱歉，我已经思考了太久，未能得出最终结论（已达到最大迭代次数保护）。"

    async def execute_stream(self, user_input: str, history: Optional[List[dict]] = None):
        """执行 Agent 推理并流式返回"""
        messages = self._build_messages(user_input, history)
        logger.debug(f"[{self.__class__.__name__}] 执行流式推理, input_length={len(user_input)}")

        try:
            async for chunk in self._llm_provider.astream(
                messages=messages,
                temperature=self._temperature
            ):
                yield chunk
        except Exception as e:
            logger.error(f"[{self.__class__.__name__}] 流式推理失败: {e}")
            raise

    @abstractmethod
    async def run(self, **kwargs) -> dict:
        """
        子类必须实现的业务入口方法
        返回标准化的 dict 响应
        """
        pass
