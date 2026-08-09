from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from app.config import settings
from elasticsearch import Elasticsearch
import logging

logger = logging.getLogger(__name__)

# 创建 SQLAlchemy 引擎
engine = create_engine(
    settings.DATABASE_URL,
    pool_size=10,
    max_overflow=20,
    pool_pre_ping=True,
    echo=False
)

# 创建 Session 工厂
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 声明式基类
Base = declarative_base()


def init_db() -> None:
    """初始化数据库表结构"""
    logger.info("初始化数据库连接...")
    Base.metadata.create_all(bind=engine)
    logger.info("数据库初始化完成")


def init_es() -> None:
    """初始化 Elasticsearch 向量索引"""
    logger.info(f"正在连接 Elasticsearch: {settings.ES_HOST}:{settings.ES_PORT}...")
    
    # 初始化客户端
    es = Elasticsearch(
        f"http://{settings.ES_HOST}:{settings.ES_PORT}",
        basic_auth=(settings.ES_USER, settings.ES_PASSWORD) if settings.ES_USER else None
    )

    indices_to_create = [
        settings.ES_INDEX_NAME,
        getattr(settings, "ES_MEMORY_INDEX_NAME", "aiforge_memory")
    ]
    
    # 定义向量搜索的 Mapping (维度 1024)
    mapping = {
        "mappings": {
            "properties": {
                "text": {"type": "text"},
                "vector": {
                    "type": "dense_vector",
                    "dims": 1024,
                    "index": True,
                    "similarity": "cosine"
                },
                "metadata": {"type": "object"}
            }
        }
    }

    for idx_name in indices_to_create:
        if es.indices.exists(index=idx_name):
            logger.info(f"Elasticsearch 索引 '{idx_name}' 已存在，跳过创建")
            continue

        try:
            es.indices.create(index=idx_name, body=mapping)
            logger.info(f"Elasticsearch 索引 '{idx_name}' 创建成功 (dims: 1024)")
        except Exception as e:
            logger.error(f"创建 Elasticsearch 索引 '{idx_name}' 失败: {e}")
            raise e


def get_db():
    """获取数据库会话（用于 FastAPI 依赖注入）"""
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
