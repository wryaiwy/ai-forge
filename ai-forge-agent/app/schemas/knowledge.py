from typing import Optional
from pydantic import BaseModel, Field

class KnowledgeDocRequest(BaseModel):
    """知识库文档入库请求"""
    title: str = Field(min_length=1, description="文档标题")
    content: str = Field(min_length=1, description="文档正文内容")
    bizId: Optional[str] = Field(default=None, description="关联业务ID")
    bizType: Optional[str] = Field(default=None, description="业务类型")
    kbId: Optional[str] = Field(default=None, description="关联的知识库 ID")
    metadata: Optional[dict] = Field(default=None, description="自定义元数据")


class KnowledgeDocResponse(BaseModel):
    """知识库文档入库响应"""
    doc_id: str = Field(description="文档唯一标识")
    status: str = Field(description="入库状态: success/failed")


class KnowledgeDeleteRequest(BaseModel):
    """知识库文档删除请求"""
    bizId: Optional[str] = Field(default=None, description="关联业务ID")
    bizType: Optional[str] = Field(default=None, description="业务类型")
