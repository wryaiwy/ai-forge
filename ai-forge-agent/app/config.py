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
    OPENAI_BASE_URL: str = "https://dashscope.aliyuncs.com/compatible-mode"
    OPENAI_MODEL: str = "deepseek-v4-flash"

    # Ollama 配置
    OLLAMA_BASE_URL: str = "http://localhost:11434"
    OLLAMA_LLM_MODEL: str = "deepseek-r1:1.5b"
    OLLAMA_EMBEDDING_MODEL: str = "quentinz/bge-large-zh-v1.5:latest"

    # Elasticsearch 向量数据库配置
    ES_HOST: str = "localhost"
    ES_PORT: int = 9200
    ES_USER: str = ""
    ES_PASSWORD: str = ""
    ES_INDEX_NAME: str = "aiforge_docs"


# 单例模式，全局使用
@lru_cache()
def get_settings():
    return Settings()


settings = get_settings()