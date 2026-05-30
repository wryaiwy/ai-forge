from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api import router
from app.exceptions import global_exception_handler
from models.database import init_db, init_es
import logging
import asyncio
from services.rabbitmq_consumer import start_rabbitmq_consumer

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(name)s - %(levelname)s - %(message)s"
)

logger = logging.getLogger(__name__)

app = FastAPI(
    title="AiForge Agent Service",
    description="AI Agent 服务 - 基于 FastAPI + LangChain",
    version="0.1.0"
)

# 注册全局异常处理器
app.add_exception_handler(Exception, global_exception_handler)

# 配置 CORS（允许 Java 后端跨域调用）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 注册路由
app.include_router(router)


@app.on_event("startup")
async def startup_event():
    """应用启动时初始化"""
    logger.info("AiForge Agent Service Starting...")
    try:
        init_db()
        init_es()
    except Exception as e:
        logger.warning(f"Database initialization skipped (possible misconfiguration): {e}")
        
    # 启动 RabbitMQ 消费者后台任务
    asyncio.create_task(start_rabbitmq_consumer())
    
    logger.info("AiForge Agent Service Startup Completed")


@app.get("/", tags=["Health"])
async def health_check():
    """健康检查"""
    return {"status": "ok", "service": "ai-forge-agent"}
