from fastapi import APIRouter, Depends
from app.schemas import ChatRequest
from app.dependencies import get_home_chat_agent, get_home_chat_graph
from agents.chain.home_chat_agent import HomeChatAgent
from agents.graph.home_chat_graph import HomeChatGraph

router = APIRouter(tags=["AI Agent"])


# 首页AI对话助手
@router.post("/home-page-chat", summary="首页AI对话助手(流式) - LangChain版")
async def chat_stream(request: ChatRequest, agent: HomeChatAgent = Depends(get_home_chat_agent)):
    """
    首页AI对话助手流式接口 (Chain 原版)
    """
    from fastapi.responses import StreamingResponse
    return StreamingResponse(agent.run_stream(request), media_type="text/event-stream")


@router.post("/home-page-chat-graph", summary="首页AI对话助手(流式) - LangGraph版")
async def chat_stream_graph(request: ChatRequest, agent: HomeChatGraph = Depends(get_home_chat_graph)):
    """
    首页AI对话助手流式接口 (LangGraph 新版)
    """
    from fastapi.responses import StreamingResponse
    return StreamingResponse(agent.run_stream(request), media_type="text/event-stream")
