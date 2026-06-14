from fastapi import APIRouter, Depends
from app.schemas import Result
from app.schemas.clean import CleanRequest, CleanResponse
from app.dependencies import get_clean_agent
from agents.clean_agent import RetrievalAndCleanAgent

router = APIRouter(tags=["AI Agent"])

@router.post("/clean", response_model=Result[CleanResponse], summary="信息获取与噪音清洗")
async def retrieve_and_clean(request: CleanRequest, agent: RetrievalAndCleanAgent = Depends(get_clean_agent)):
    """
    信息获取与噪音清洗
    根据单段的大纲去检索资料，并经过 LLM 的清洗节点提纯出绝对干净可用的背景上下文。
    """
    response_dict = await agent.run(request)
    return Result.success(data=response_dict, message="资料清洗完成")
