from fastapi import APIRouter, Depends
from app.schemas import ChatRequest
from app.dependencies import get_home_chat_agent
from agents.home_chat_agent import HomeChatAgent

router = APIRouter(tags=["AI Agent"])


# 首页AI对话助手
@router.post("/home-page-chat", summary="首页AI对话助手(流式)")
async def chat_stream(request: ChatRequest, agent: HomeChatAgent = Depends(get_home_chat_agent)):
    """
    首页AI对话助手流式接口
    """
    from fastapi.responses import StreamingResponse
    # "text/event-stream" 告诉 HTTP 协议返回的不是普通文本，而是 Server-Sent Events 数据流
    return StreamingResponse(agent.run_stream(request), media_type="text/event-stream")
