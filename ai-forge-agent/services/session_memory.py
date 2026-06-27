import json
import tiktoken
import redis
from typing import List, Dict, Optional
from app.config import settings

class SessionMemory:
    """
    基于 Redis 的 Token 滑动窗口记忆管理
    """
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

    def add_message(self, session_id: str, role: str, content: str):
        key = self._get_key(session_id)
        msg = json.dumps({"role": role, "content": content}, ensure_ascii=False)
        
        # 使用 Pipeline 减少网络往返开销 (主库写入)
        pipeline = self.redis_master.pipeline()
        pipeline.rpush(key, msg)
        pipeline.expire(key, self.ttl)
        pipeline.execute()

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
