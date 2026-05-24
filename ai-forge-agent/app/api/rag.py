from fastapi import APIRouter, Depends
from app.schemas import Result, RagQueryRequest, RagQueryResponse
from app.dependencies import get_rag_agent
from agents.rag_agent import RagAgent

router = APIRouter(tags=["AI Agent"])


@router.post("/rag/query", response_model=Result[RagQueryResponse], summary="知识库查询")
async def rag_query(request: RagQueryRequest, agent: RagAgent = Depends(get_rag_agent)):
    """
    RAG 知识库查询接口
    基于向量数据库检索 + LLM 生成回答
    """
    response = await agent.run(request)
    return Result.success(data=response, message="查询成功")


@router.post("/rag/stream", summary="知识库流式查询")
async def rag_stream(request: RagQueryRequest, agent: RagAgent = Depends(get_rag_agent)):
    """
    RAG 知识库流式查询接口
    """
    from fastapi.responses import StreamingResponse
    return StreamingResponse(agent.run_stream(request), media_type="text/event-stream")
