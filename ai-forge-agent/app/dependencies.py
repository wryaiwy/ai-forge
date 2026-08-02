from fastapi import Depends
from sqlalchemy.orm import Session
from models.database import get_db, SessionLocal
from agents.chain.home_chat_agent import HomeChatAgent
from agents.graph.home_chat_graph import HomeChatGraph
from agents.chain.rag_agent import RagAgent
from agents.chain.planning_agent import PlanningAgent
from agents.chain.clean_agent import RetrievalAndCleanAgent
from agents.chain.writing_chat_agent import WritingChatAgent
from agents.chain.delivery_agent import DeliveryAndHealAgent
import logging

logger = logging.getLogger(__name__)


def get_home_chat_agent(db: Session = Depends(get_db)) -> HomeChatAgent:
    """FastAPI 依赖注入：获取首页AI对话助手实例 (LangChain)"""
    return HomeChatAgent(db_session=db)


def get_home_chat_graph(db: Session = Depends(get_db)) -> HomeChatGraph:
    """FastAPI 依赖注入：获取首页AI对话助手实例 (LangGraph)"""
    return HomeChatGraph(db_session=db)


def get_rag_agent(db: Session = Depends(get_db)) -> RagAgent:
    """FastAPI 依赖注入：获取 RAG Agent 实例"""
    return RagAgent(db_session=db)


def get_planning_agent() -> PlanningAgent:
    """FastAPI 依赖注入：获取 Planning Agent 实例"""
    return PlanningAgent()


def get_clean_agent() -> RetrievalAndCleanAgent:
    """FastAPI 依赖注入：获取 Clean Agent 实例"""
    return RetrievalAndCleanAgent()


def get_writing_chat_agent(db: Session = Depends(get_db)) -> WritingChatAgent:
    """FastAPI 依赖注入：获取写作AI对话助手实例"""
    return WritingChatAgent(db_session=db)


def get_delivery_agent() -> DeliveryAndHealAgent:
    """FastAPI 依赖注入：获取 Delivery & Heal Agent 实例"""
    return DeliveryAndHealAgent()
