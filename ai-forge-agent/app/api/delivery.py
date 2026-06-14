from fastapi import APIRouter, Depends, Header
from typing import Optional
from app.schemas import Result
from app.schemas.delivery import DeliveryRequest, DeliveryResponse
from app.dependencies import get_delivery_agent
from agents.delivery_agent import DeliveryAndHealAgent

router = APIRouter(tags=["AI Agent"])

@router.post("/delivery", response_model=Result[DeliveryResponse], summary="最终交付与异常自愈")
async def final_delivery(
    request: DeliveryRequest, 
    token: Optional[str] = Header(None, alias="token"),
    authorization: Optional[str] = Header(None, alias="Authorization"),
    satoken: Optional[str] = Header(None, alias="satoken"),
    agent: DeliveryAndHealAgent = Depends(get_delivery_agent)
):
    """
    最终交付与异常自愈
    将起草好的长文调用 Java 服务入库。若发生业务异常（如超出字数限制），将自动触发大模型修复并重试。
    """
    actual_token = token or authorization or satoken or ""
    if actual_token.startswith("Bearer "):
        actual_token = actual_token[7:]
        
    response_dict = await agent.run(request, user_token=actual_token)
    # 无论最终业务上是否 self-heal 成功，该接口调用本身是成功的，所以返回 Result.success
    # 前端可通过 data.success 判断入库状态
    return Result.success(data=response_dict, message="交付流程结束")
