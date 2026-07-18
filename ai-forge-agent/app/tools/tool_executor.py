from typing import Any, Dict
from app.tools.base import ToolsRegistry
import logging

logger = logging.getLogger(__name__)


class ToolExecutor:
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
