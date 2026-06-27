from typing import Optional, List
from pydantic import BaseModel, Field


class ChatMessageRequest(BaseModel):
    """单条聊天消息"""
    role: str = Field(description="消息角色: user/assistant/system")
    content: str = Field(description="消息内容")


class ChatRequest(BaseModel):
    """首页AI对话助手请求"""
    user_id: str = Field(description="用户 ID")
    message: str = Field(min_length=1, description="用户输入的消息")
    session_id: Optional[str] = Field(default=None, description="会话 ID，首次对话可为空")
    history: Optional[List[ChatMessageRequest]] = Field(default=None, description="历史对话上下文")


class ChatResponse(BaseModel):
    """首页AI对话助手响应"""
    answer: str = Field(description="AI 回复内容")
    session_id: str = Field(description="当前会话 ID")
    usage: Optional[dict] = Field(default=None, description="Token 使用统计")
