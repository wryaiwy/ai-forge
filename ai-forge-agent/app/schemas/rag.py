from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field
from langchain_core.documents import Document

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


class RetrievalQuery(BaseModel):
    """检索查询数据结构"""
    original_query: str = Field(..., description="用户原始输入的查询")
    rewritten_query: Optional[str] = Field(None, description="经过 LLM 重写后用于实际检索的查询")
    intent: Optional[str] = Field(None, description="识别出的用户意图（如：问答、闲聊等）")
    filters: Optional[Dict[str, Any]] = Field(default_factory=dict, description="提取出的元数据过滤条件")
    query_vector: Optional[List[float]] = Field(default=None, description="向量化后的查询数组")


class RetrievedDocument(BaseModel):
    """检索召回文档的数据结构"""
    document: Document = Field(..., description="原始召回的 LangChain Document 对象")
    relevance_score: float = Field(default=0.0, description="重排打分或相关性得分")
