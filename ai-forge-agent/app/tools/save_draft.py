from app.tools.core import RemoteHttpTool, ToolsRegistry
from pydantic import Field


class SaveDraftToolSchema(RemoteHttpTool):
    title: str = Field(...)
    content: str = Field(...)


# 注册 Tool
ToolsRegistry.register("save_draft", "保存草稿", SaveDraftToolSchema)
