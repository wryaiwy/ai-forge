from fastapi import APIRouter
from fastapi.responses import StreamingResponse
from app.schemas import SummaryRequest
from agents.summary_agent import SummaryAgent

router = APIRouter(prefix="/summary", tags=["文章摘要"])

@router.post("/stream")
async def generate_summary_stream(request: SummaryRequest):
    agent = SummaryAgent()
    return StreamingResponse(agent.run_stream(request), media_type="text/event-stream")
