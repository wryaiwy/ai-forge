from fastapi import APIRouter
from app.api.chat import router as chat_router
from app.api.rag import router as rag_router
from app.api.knowledge import router as knowledge_router

router = APIRouter(prefix="/agent")

router.include_router(chat_router)
router.include_router(rag_router)
router.include_router(knowledge_router)
