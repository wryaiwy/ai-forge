from fastapi import APIRouter, Depends
from app.schemas import Result, ChatRequest, ChatResponse
from app.dependencies import get_customer_agent
from agents.customer_agent import CustomerAgent

router = APIRouter(tags=["AI Agent"])


@router.post("/chat", response_model=Result[ChatResponse], summary="客服聊天")
async def chat(request: ChatRequest, agent: CustomerAgent = Depends(get_customer_agent)):
    """
    客服聊天接口
    支持多轮对话，传入 conversation_id 可延续历史对话
    """
    response = await agent.run(request)
    return Result.success(data=response, message="回复成功")
