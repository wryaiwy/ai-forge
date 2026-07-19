from pydantic import BaseModel
from typing import Type, Dict, Any
import logging
import httpx
import aio_pika
import json
from app.config import settings

logger = logging.getLogger(__name__)


class BaseAgentTool(BaseModel):
    """
    Python 侧 Tool 定义基类
    用于生成大模型 Function Calling 所需的 JSON Schema
    """

    async def execute(self, user_token: str = "", **kwargs) -> Any:
        """子类必须实现此方法"""
        raise NotImplementedError()

    @classmethod
    def get_tool_schema(cls, name: str, description: str) -> dict:
        """
        生成 LangChain 兼容的 Tool Schema
        """
        schema = cls.model_json_schema()

        return {
            "type": "function",
            "function": {
                "name": name,
                "description": description,
                "parameters": schema
            }
        }


class RemoteHttpTool(BaseAgentTool):
    """透传给 Java 侧的同步工具模板"""
    async def execute(self, user_token: str = "", **kwargs) -> Any:
        tool_name = ToolsRegistry.get_name_by_class(self.__class__)
        if not tool_name:
            tool_name = self.__class__.__name__

        headers = {}
        if user_token:
            headers["token"] = user_token
            headers["Authorization"] = user_token
            headers["satoken"] = user_token

        async with httpx.AsyncClient() as client:
            try:
                resp = await client.post(
                    "http://127.0.0.1:8080/api/tools/execute",
                    json={"toolName": tool_name, "arguments": kwargs},
                    headers=headers
                )
                return resp.json()
            except Exception as e:
                logger.error(f"HTTP 调用工具失败: {e}")
                return {"status": "error", "message": str(e)}


class AsyncMQTool(BaseAgentTool):
    """投递给 MQ 的异步工具模板"""
    async def execute(self, user_token: str = "", **kwargs) -> Any:
        tool_name = ToolsRegistry.get_name_by_class(self.__class__)
        if not tool_name:
            tool_name = self.__class__.__name__
            
        queue_name = "aiforge.async.tool.queue"
        try:
            connection = await aio_pika.connect_robust(
                host=settings.MQ_HOST,
                port=settings.MQ_PORT,
                login=settings.MQ_USER,
                password=settings.MQ_PASSWORD
            )
            async with connection:
                channel = await connection.channel()
                # 声明队列以防不存在
                await channel.declare_queue(queue_name, durable=True)
                
                message_body = {
                    "toolName": tool_name,
                    "arguments": kwargs
                }
                message = aio_pika.Message(
                    body=json.dumps(message_body).encode(),
                    delivery_mode=aio_pika.DeliveryMode.PERSISTENT
                )
                await channel.default_exchange.publish(
                    message,
                    routing_key=queue_name
                )
                logger.info(f"MQ 消息发送成功: {tool_name}")
                return {"status": "success", "message": "已异步投递到MQ"}
        except Exception as e:
            logger.error(f"MQ 消息发送失败: {e}")
            return {"status": "error", "message": str(e)}


class ToolsRegistry:
    """工具注册中心"""
    _registry: Dict[str, dict] = {}
    _class_registry: Dict[str, Type[BaseAgentTool]] = {}

    @classmethod
    def register(cls, name: str, description: str, schema_class: Type[BaseAgentTool]):
        """注册一个工具 Schema"""
        cls._registry[name] = schema_class.get_tool_schema(name, description)
        cls._class_registry[name] = schema_class
        logger.info(f"Registered tool schema: {name}")

    @classmethod
    def get_all_schemas(cls) -> list:
        """获取所有已注册的工具 Schema 列表，供 LLM 绑定"""
        return list(cls._registry.values())

    @classmethod
    def get_schema(cls, name: str) -> dict:
        return cls._registry.get(name)

    @classmethod
    def get_schema_class(cls, name: str) -> Type[BaseAgentTool]:
        """获取工具对应的类"""
        return cls._class_registry.get(name)
        
    @classmethod
    def get_name_by_class(cls, schema_class: Type[BaseAgentTool]) -> str:
        """通过类获取注册的名称"""
        for name, cls_obj in cls._class_registry.items():
            if cls_obj == schema_class:
                return name
        return ""


class ToolExecutor:
    """动态工具调度器"""
    def __init__(self):
        pass

    async def execute_tool(self, tool_name: str, arguments: Dict[str, Any], user_token: str = "") -> Any:
        logger.info(f"执行工具 {tool_name}, args: {arguments}")
        
        tool_class = ToolsRegistry.get_schema_class(tool_name)
        if not tool_class:
            return {"status": "error", "message": f"未知的工具: {tool_name}"}
            
        try:
            # 1. 自动利用 Pydantic 进行参数校验
            tool_instance = tool_class(**arguments)
            # 2. 多态调用真正的逻辑
            return await tool_instance.execute(user_token=user_token, **arguments)
        except Exception as e:
            logger.error(f"工具执行异常: {e}")
            return {"status": "error", "message": str(e)}
