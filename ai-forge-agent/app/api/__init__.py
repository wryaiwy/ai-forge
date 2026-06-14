from fastapi import APIRouter
from app.api.chat import router as chat_router
from app.api.rag import router as rag_router
from app.api.knowledge import router as knowledge_router
from app.api.summary import router as summary_router
from app.api.plan import router as plan_router
from app.api.clean import router as clean_router
from app.api.draft import router as draft_router
from app.api.delivery import router as delivery_router

router = APIRouter(prefix="/agent")

router.include_router(chat_router)
router.include_router(rag_router)
router.include_router(knowledge_router)
router.include_router(summary_router)
router.include_router(plan_router)
router.include_router(clean_router)
router.include_router(draft_router)
router.include_router(delivery_router)
