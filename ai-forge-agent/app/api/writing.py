from fastapi import APIRouter, Depends
from app.schemas import ChatRequest
from app.dependencies import get_writing_chat_agent
from agents.writing_chat_agent import WritingChatAgent
from fastapi.responses import StreamingResponse

router = APIRouter(tags=["AI Agent"])


@router.post("/writing-chat", summary="写作AI对话助手(流式)")
async def chat_stream(request: ChatRequest, agent: WritingChatAgent = Depends(get_writing_chat_agent)):
    """
    写作AI对话助手流式接口
    """
    return StreamingResponse(agent.run_stream(request), media_type="text/event-stream")
