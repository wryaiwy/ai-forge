from fastapi import Depends
from sqlalchemy.orm import Session
from models.database import get_db, SessionLocal
from agents.customer_agent import CustomerAgent
from agents.rag_agent import RagAgent
from agents.planning_agent import PlanningAgent
from agents.clean_agent import RetrievalAndCleanAgent
from agents.draft_agent import DraftAndCriticAgent
from agents.delivery_agent import DeliveryAndHealAgent
import logging

logger = logging.getLogger(__name__)


def get_customer_agent(db: Session = Depends(get_db)) -> CustomerAgent:
    """FastAPI 依赖注入：获取客服 Agent 实例"""
    return CustomerAgent(db_session=db)


def get_rag_agent(db: Session = Depends(get_db)) -> RagAgent:
    """FastAPI 依赖注入：获取 RAG Agent 实例"""
    return RagAgent(db_session=db)

def get_planning_agent() -> PlanningAgent:
    """FastAPI 依赖注入：获取 Planning Agent 实例"""
    return PlanningAgent()

def get_clean_agent() -> RetrievalAndCleanAgent:
    """FastAPI 依赖注入：获取 Clean Agent 实例"""
    return RetrievalAndCleanAgent()

def get_draft_agent() -> DraftAndCriticAgent:
    """FastAPI 依赖注入：获取 Draft & Critic Agent 实例"""
    return DraftAndCriticAgent()

def get_delivery_agent() -> DeliveryAndHealAgent:
    """FastAPI 依赖注入：获取 Delivery & Heal Agent 实例"""
    return DeliveryAndHealAgent()
