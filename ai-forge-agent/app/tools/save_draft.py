from typing import Literal
from pydantic import Field
from app.services.tools_registry import ToolsRegistry, BaseAgentTool

class SaveDraftToolSchema(BaseAgentTool):
    execution_mode: Literal["HTTP", "MQ"] = "HTTP"
    title: str = Field(..., description="草稿标题")
    content: str = Field(..., description="草稿正文内容")
    user_id: int = Field(..., description="用户ID")

# 注册 Tool
ToolsRegistry.register("save_draft_tool", "保存文章草稿到数据库(同步)", SaveDraftToolSchema)
