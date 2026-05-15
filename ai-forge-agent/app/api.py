from fastapi import APIRouter, Depends
from app.schemas import (
    Result, ChatRequest, ChatResponse,
    RagQueryRequest, RagQueryResponse,
    KnowledgeDocRequest, KnowledgeDocResponse, KnowledgeDeleteRequest
)
from app.dependencies import get_customer_agent, get_rag_agent
from agents.customer_agent import CustomerAgent
from agents.rag_agent import RagAgent
from services.vector_store import vector_store_service
import logging
import uuid

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/agent", tags=["AI Agent"])


@router.post("/chat", response_model=Result[ChatResponse], summary="客服聊天")
async def chat(request: ChatRequest, agent: CustomerAgent = Depends(get_customer_agent)):
    """
    客服聊天接口
    支持多轮对话，传入 conversation_id 可延续历史对话
    """
    response = await agent.run(request)
    return Result.success(data=response, message="回复成功")


@router.post("/rag/query", response_model=Result[RagQueryResponse], summary="知识库查询")
async def rag_query(request: RagQueryRequest, agent: RagAgent = Depends(get_rag_agent)):
    """
    RAG 知识库查询接口
    基于向量数据库检索 + LLM 生成回答
    """
    response = await agent.run(request)
    return Result.success(data=response, message="查询成功")


@router.post("/knowledge/add", response_model=Result[KnowledgeDocResponse], summary="添加知识文档")
async def add_knowledge(request: KnowledgeDocRequest):
    """
    添加文档到知识库
    自动进行向量化并存储
    """
    doc_id = str(uuid.uuid4())
    metadata = {
        "doc_id": doc_id,
        "title": request.title,
    }
    if request.dataset_id:
        metadata["dataset_id"] = request.dataset_id
    if request.metadata:
        metadata.update(request.metadata)

    try:
        ids = await vector_store_service.add_documents(
            texts=[request.content],
            metadatas=[metadata]
        )
        return Result.success(
            data=KnowledgeDocResponse(doc_id=doc_id, status="success"),
            message="文档入库成功"
        )
    except Exception as e:
        logger.error(f"知识文档入库失败: {e}")
        return Result.fail(code=1003, message=f"文档入库失败: {str(e)}")


@router.post("/knowledge/delete", response_model=Result[dict], summary="删除知识文档")
async def delete_knowledge(request: KnowledgeDeleteRequest):
    """
    从知识库删除指定文档
    """
    success = await vector_store_service.delete_documents(ids=[request.doc_id])
    if success:
        return Result.success(data={"doc_id": request.doc_id}, message="删除成功")
    return Result.fail(code=1004, message="删除失败")
