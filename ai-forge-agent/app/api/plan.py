from fastapi import APIRouter, Depends
from app.schemas import Result
from app.schemas.plan import PlanRequest, TaskPlanResponse
from app.dependencies import get_planning_agent
from agents.planning_agent import PlanningAgent

router = APIRouter(tags=["AI Agent"])

@router.post("/plan", response_model=Result[TaskPlanResponse], summary="需求解析与大纲生成")
async def generate_plan(request: PlanRequest, agent: PlanningAgent = Depends(get_planning_agent)):
    """
    需求解析与大纲生成
    解析用户输入的模糊需求，输出结构化的 JSON Task Plan。
    该接口可用于后续 Java 端的 Human-in-the-loop 人工审批。
    """
    response_dict = await agent.run(request)
    return Result.success(data=response_dict, message="大纲生成成功")
