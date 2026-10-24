import logging
from typing import List, Optional

from langchain_core.callbacks import BaseCallbackHandler

# 必须先导入 settings：触发 load_dotenv()，让 Langfuse SDK 能从 os.environ 读到 key
from app.config import settings

logger = logging.getLogger(__name__)

# 全局唯一 handler：
# 1. Langfuse v5 基于 OTel，按 run_id 维护 span，单实例可并发复用
# 2. 同一实例同时挂在 Graph 和 ChatModel 上时，LangChain CallbackManager 按对象身份去重，不会重复上报
langfuse_handler: Optional[BaseCallbackHandler] = None

if settings.langfuse_enabled:
    try:
        from langfuse.langchain import CallbackHandler

        langfuse_handler = CallbackHandler()
        logger.info(f"Langfuse 追踪已启用: {settings.LANGFUSE_BASE_URL}")
    except Exception as e:
        logger.warning(f"Langfuse 初始化失败，追踪已禁用: {e}")
else:
    logger.info("未配置 Langfuse key，跳过追踪")


def get_langfuse_callbacks() -> List[BaseCallbackHandler]:
    """返回可直接传给 callbacks= 的列表，未启用时为空列表"""
    return [langfuse_handler] if langfuse_handler else []


def shutdown_langfuse() -> None:
    """进程退出前 flush 剩余批次，避免最后一批 trace 丢失"""
    if not langfuse_handler:
        return
    try:
        from langfuse import get_client

        get_client().shutdown()
    except Exception as e:
        logger.warning(f"Langfuse shutdown 失败: {e}")
