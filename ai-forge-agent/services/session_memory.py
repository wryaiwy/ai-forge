import json
import tiktoken
import redis
import logging
from typing import List, Dict, Optional
from app.config import settings
from services.vector_store import vector_store_service


class SessionMemory:
    """
    基于 Redis 的 Token 滑动窗口记忆管理
    """
    logger = logging.getLogger(__name__)

    # model_name: 仅用于 tiktoken 选择对应的分词器来计算 token 数，不影响实际调用的 LLM 模型
    def __init__(self, max_tokens: int = 3000, ttl_seconds: int = 86400, model_name: str = "gpt-3.5-turbo"):
        self.redis_master = redis.Redis(
            host=settings.REDIS_HOST,
            port=settings.REDIS_PORT,
            db=settings.REDIS_DB,
            password=settings.REDIS_PASSWORD
        )
        self.redis_slave = redis.Redis(
            host=settings.REDIS_SLAVE_HOST,
            port=settings.REDIS_SLAVE_PORT,
            db=settings.REDIS_DB,
            password=settings.REDIS_SLAVE_PASSWORD
        )
        self.max_tokens = max_tokens
        self.ttl = ttl_seconds

        try:
            self.encoding = tiktoken.encoding_for_model(model_name)
        except KeyError:
            self.encoding = tiktoken.get_encoding("cl100k_base")

    def _get_key(self, session_id: str) -> str:
        return f"aiforge:memory:short:{session_id}"

    # 摘要Key
    def _get_summary_key(self, session_id: str) -> str:
        return f"aiforge:memory:mid:{session_id}"

    def add_message(self, session_id: str, role: str, content: str):
        key = self._get_key(session_id)
        msg = json.dumps({"role": role, "content": content}, ensure_ascii=False)

        # 使用 Pipeline 减少网络往返开销 (主库写入)
        pipeline = self.redis_master.pipeline()
        pipeline.rpush(key, msg)
        pipeline.expire(key, self.ttl)
        pipeline.execute()

    # 从 Key 中获取摘要信息
    def get_summary(self, session_id: str) -> Optional[str]:
        key = self._get_summary_key(session_id)
        val = self.redis_slave.get(key)
        return val.decode('utf-8') if val else None

    # 设置摘要信息
    def set_summary(self, session_id: str, summary: str):
        key = self._get_summary_key(session_id)
        pipeline = self.redis_master.pipeline()
        pipeline.set(key, summary)
        pipeline.expire(key, self.ttl)
        pipeline.execute()

    # 从 Key 中获取原始对话历史
    def get_raw_history(self, session_id: str) -> List[Dict[str, str]]:
        key = self._get_key(session_id)
        raw_messages = self.redis_slave.lrange(key, 0, -1)
        if not raw_messages:
            return []
        return [json.loads(msg.decode('utf-8')) for msg in raw_messages]

    # 截断对话历史(保留最新消息)
    def trim_history(self, session_id: str, keep_count: int):
        key = self._get_key(session_id)
        self.redis_master.ltrim(key, -keep_count, -1)

    # 获取历史对话(结合摘要)
    def get_history(self, session_id: str) -> List[Dict[str, str]]:
        key = self._get_key(session_id)

        # 获取近期对话 (限制最大拉取条数) 从库读取
        raw_messages = self.redis_slave.lrange(key, -50, -1)
        if not raw_messages:
            return []

        session = [json.loads(msg.decode('utf-8')) for msg in raw_messages]

        total_tokens = 0
        keep_messages = []

        # 从最新消息回溯做滑动截断
        for msg in reversed(session):
            # 为了容错，避免 content 为 None 或非字符串类型报错
            content_str = str(msg.get("content", ""))
            msg_tokens = len(self.encoding.encode(content_str)) + 4
            if total_tokens + msg_tokens > self.max_tokens:
                break

            total_tokens += msg_tokens
            # 保证正序
            keep_messages.insert(0, msg)

        return keep_messages

    async def get_long_term_context(self, session_id: str, query: str, k: int = 3) -> str:
        """从 ES 获取长期向量记忆"""
        try:
            docs = await vector_store_service.similarity_search(
                query=query,
                k=k,
                filter_dict=[{"term": {"metadata.session_id.keyword": session_id}}],
                index_name=settings.ES_MEMORY_INDEX_NAME
            )
            if docs:
                return "\n".join([doc.page_content for doc in docs])
        except Exception as e:
            self.logger.warning(f"长期记忆检索失败: {e}")
        return ""

    async def save_long_term_memory(self, session_id: str, user_msg: str, ai_msg: str):
        """保存对话到长期向量记忆"""
        try:
            text = f"User: {user_msg}\nAI: {ai_msg}"
            metadata = {"session_id": session_id, "type": "chat_history"}
            await vector_store_service.add_documents(
                [text],
                [metadata],
                index_name=settings.ES_MEMORY_INDEX_NAME
            )
            self.logger.info(f"会话 {session_id} 长期记忆已入库")
        except Exception as e:
            self.logger.error(f"会话 {session_id} 长期记忆入库失败: {e}")
