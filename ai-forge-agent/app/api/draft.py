from fastapi import APIRouter, Depends
from app.schemas import Result
from app.schemas.draft import DraftRequest, DraftResponse
from app.dependencies import get_draft_agent
from agents.draft_agent import DraftAndCriticAgent

router = APIRouter(tags=["AI Agent"])

@router.post("/draft", response_model=Result[DraftResponse], summary="长文本起草与 Critic 反思")
async def generate_draft(request: DraftRequest, agent: DraftAndCriticAgent = Depends(get_draft_agent)):
    """
    长文本起草与 Critic 反思
    分段生成长文，并引入 Critic Agent 自核防跑题。如果跑题，则自动打回重写。
    """
    response_dict = await agent.run(request)
    return Result.success(data=response_dict, message="起草与反思完成")
