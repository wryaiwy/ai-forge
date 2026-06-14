import aio_pika
import httpx
import json
from typing import Any, Dict
from app.config import settings
from app.services.tools_registry import ToolsRegistry
from app.tools.web_search import WebSearchToolExecutor
import logging

logger = logging.getLogger(__name__)

class ToolExecutor:
    def __init__(self):
        self.web_search = WebSearchToolExecutor()

    async def execute_tool(self, tool_name: str, arguments: Dict[str, Any], user_token: str = "") -> Any:
        mode = ToolsRegistry.get_execution_mode(tool_name)
        logger.info(f"执行工具 {tool_name} (Mode: {mode}), args: {arguments}")
        
        if mode == "HTTP":
            # 如果是 WebSearchTool，直接在 Python 侧执行
            if tool_name == "web_search_tool":
                return self.web_search.execute(arguments)
            else:
                # 其他同步 HTTP Tool，调用 Java 接口
                # Java 服务运行在 localhost:8080
                headers = {}
                if user_token:
                    headers["token"] = user_token
                    headers["Authorization"] = user_token
                    headers["satoken"] = user_token
                    
                async with httpx.AsyncClient() as client:
                    try:
                        resp = await client.post(
                            "http://127.0.0.1:8080/api/tools/execute",
                            json={"toolName": tool_name, "arguments": arguments},
                            headers=headers
                        )
                        return resp.json()
                    except Exception as e:
                        logger.error(f"HTTP 调用工具失败: {e}")
                        return {"status": "error", "message": str(e)}
        elif mode == "MQ":
            # 异步 MQ Tool，投递到 RabbitMQ
            await self._send_to_mq("aiforge.async.tool.queue", tool_name, arguments)
            return {"status": "success", "message": "已异步投递到MQ"}
        else:
            return {"status": "error", "message": f"未知的执行模式: {mode}"}

    async def _send_to_mq(self, queue_name: str, tool_name: str, arguments: Dict[str, Any]):
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
                    "arguments": arguments
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
        except Exception as e:
            logger.error(f"MQ 消息发送失败: {e}")
