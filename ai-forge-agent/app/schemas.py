from typing import Generic, TypeVar, Optional, List
from pydantic import BaseModel, Field

# 1. 定义一个泛型变量 T
T = TypeVar('T')

# 2. 创建 Result 基类，继承 Generic[T]
class Result(BaseModel, Generic[T]):
    """
    通用返回结果封装类 (类似 Java 的 Result<T>)
    """
    code: int       # 状态码 (200 表示成功，其他表示错误)
    message: str    # 描述信息
    data: Optional[T] = None  # 泛型数据，可以是任何类型

    # 3. 定义几个静态方法，方便在代码中快速调用

    @staticmethod
    def success(data: Optional[T] = None, message: str = "操作成功") -> 'Result[T]':
        """成功返回"""
        return Result(code=200, message=message, data=data)

    @staticmethod
    def fail(code: int = 500, message: str = "操作失败") -> 'Result[T]':
        """失败返回"""
        return Result(code=code, message=message, data=None)


# ==========================================
# 客服聊天相关 Schema
# ==========================================

class ChatMessageRequest(BaseModel):
    """单条聊天消息"""
    role: str = Field(description="消息角色: user/assistant/system")
    content: str = Field(description="消息内容")


class ChatRequest(BaseModel):
    """客服聊天请求"""
    user_id: str = Field(description="用户 ID")
    message: str = Field(min_length=1, description="用户输入的消息")
    conversation_id: Optional[str] = Field(default=None, description="会话 ID，首次对话可为空")
    history: Optional[List[ChatMessageRequest]] = Field(default=None, description="历史对话上下文")


class ChatResponse(BaseModel):
    """客服聊天响应"""
    answer: str = Field(description="AI 回复内容")
    conversation_id: str = Field(description="当前会话 ID")
    usage: Optional[dict] = Field(default=None, description="Token 使用统计")


# ==========================================
# RAG 知识库相关 Schema
# ==========================================

class RagQueryRequest(BaseModel):
    """RAG 知识库查询请求"""
    query: str = Field(min_length=1, description="查询问题")
    top_k: int = Field(default=5, ge=1, le=20, description="返回最相关的 K 条结果")
    dataset_id: Optional[str] = Field(default=None, description="指定数据集 ID，为空则全局搜索")


class RagSourceItem(BaseModel):
    """RAG 检索来源条目"""
    content: str = Field(description="匹配到的文本片段")
    score: float = Field(description="相似度得分")
    metadata: Optional[dict] = Field(default=None, description="附加元数据（如文章标题、作者等）")


class RagQueryResponse(BaseModel):
    """RAG 知识库查询响应"""
    answer: str = Field(description="基于知识库生成的回答")
    sources: List[RagSourceItem] = Field(description="引用来源列表")


# ==========================================
# 知识库管理相关 Schema
# ==========================================

class KnowledgeDocRequest(BaseModel):
    """知识库文档入库请求"""
    title: str = Field(min_length=1, description="文档标题")
    content: str = Field(min_length=1, description="文档正文内容")
    dataset_id: Optional[str] = Field(default=None, description="所属数据集 ID")
    metadata: Optional[dict] = Field(default=None, description="自定义元数据")


class KnowledgeDocResponse(BaseModel):
    """知识库文档入库响应"""
    doc_id: str = Field(description="文档唯一标识")
    status: str = Field(description="入库状态: success/failed")


class KnowledgeDeleteRequest(BaseModel):
    """知识库文档删除请求"""
    doc_id: str = Field(description="文档唯一标识")
    dataset_id: Optional[str] = Field(default=None, description="所属数据集 ID")
