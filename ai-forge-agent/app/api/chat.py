from fastapi import APIRouter, Depends
from app.schemas import ChatRequest
from app.dependencies import get_home_chat_graph
from agents.graph.home_chat_graph import HomeChatGraph

router = APIRouter(tags=["AI Agent"])


@router.post("/home-page-chat-graph", summary="首页AI对话助手(流式) - LangGraph版")
async def chat_stream_graph(request: ChatRequest, agent: HomeChatGraph = Depends(get_home_chat_graph)):
    """
    首页AI对话助手流式接口 (LangGraph 新版)
    """
    from fastapi.responses import StreamingResponse
    return StreamingResponse(agent.run_stream(request), media_type="text/event-stream")
