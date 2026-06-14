from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # 数据库配置
    DB_HOST: str = "localhost"
    DB_PORT: int = 3306
    DB_USER: str = "root"
    DB_PASSWORD: str = "1234567"
    DB_NAME: str = "aiforge"

    # 构造 SQLAlchemy 专用的数据库 URL
    # 格式: mysql+pymysql://用户:密码@主机:端口/数据库?参数
    @property
    def DATABASE_URL(self) -> str:
        return (f"mysql+pymysql://{self.DB_USER}:{self.DB_PASSWORD}@"
                f"{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
                f"?charset=utf8mb4")

    # OpenAI 兼容配置（如 DashScope）
    OPENAI_API_KEY: str = ""
    OPENAI_BASE_URL: str = "https://dashscope.aliyuncs.com/compatible-mode/v1"
    OPENAI_MODEL: str = "qwen3.7-max-2026-06-08"

    # Ollama 配置
    OLLAMA_BASE_URL: str = "http://localhost:11434"
    OLLAMA_LLM_MODEL: str = "deepseek-r1:1.5b"
    OLLAMA_EMBEDDING_MODEL: str = "quentinz/bge-large-zh-v1.5:latest"

    # Elasticsearch 向量数据库配置
    ES_HOST: str = "localhost"
    ES_PORT: int = 9200
    ES_USER: str = "elastic"
    ES_PASSWORD: str = "1234567"
    ES_INDEX_NAME: str = "aiforge_docs"

    # 文本分块配置（需匹配 embedding 模型的上下文长度）
    CHUNK_SIZE: int = 256
    CHUNK_OVERLAP: int = 50

    # RabbitMQ 配置
    MQ_HOST: str = "192.168.88.129"
    MQ_PORT: int = 5672
    MQ_USER: str = "admin"
    MQ_PASSWORD: str = "admin123"
    MQ_QUEUE_VECTOR_SYNC: str = "aiforge_vector_sync_queue" # 向量同步队列名称

# 单例模式，全局使用
@lru_cache()
def get_settings():
    return Settings()


settings = get_settings()