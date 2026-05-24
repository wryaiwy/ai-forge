from typing import Optional, List
from pydantic import BaseModel, Field

class RagQueryRequest(BaseModel):
    """RAG 知识库查询请求"""
    query: str = Field(min_length=1, description="查询问题")
    top_k: int = Field(default=5, ge=1, le=20, description="返回最相关的 K 条结果")
    kb_id: Optional[str] = Field(default=None, description="指定知识库 ID，为空则全局搜索")
    biz_id: Optional[str] = Field(default=None, description="指定业务 ID，用于按业务过滤")
    biz_type: Optional[str] = Field(default=None, description="指定业务类型，用于按业务过滤")


class RagSourceItem(BaseModel):
    """RAG 检索来源条目"""
    content: str = Field(description="匹配到的文本片段")
    score: float = Field(description="相似度得分")
    metadata: Optional[dict] = Field(default=None, description="附加元数据（如文章标题、作者等）")


class RagQueryResponse(BaseModel):
    """RAG 知识库查询响应"""
    answer: str = Field(description="基于知识库生成的回答")
    sources: List[RagSourceItem] = Field(description="引用来源列表")
