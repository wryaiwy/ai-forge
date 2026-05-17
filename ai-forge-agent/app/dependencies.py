from fastapi import Depends
from sqlalchemy.orm import Session
from models.database import get_db, SessionLocal
from agents.customer_agent import CustomerAgent
from agents.rag_agent import RagAgent
import logging

logger = logging.getLogger(__name__)


def get_customer_agent(db: Session = Depends(get_db)) -> CustomerAgent:
    """FastAPI 依赖注入：获取客服 Agent 实例"""
    return CustomerAgent(db_session=db)


def get_rag_agent(db: Session = Depends(get_db)) -> RagAgent:
    """FastAPI 依赖注入：获取 RAG Agent 实例"""
    return RagAgent(db_session=db)
