import tiktoken
from typing import List, Dict, Optional

class SessionMemory:
    """
    基于 Token 阈值的会话记忆管理
    """
    # model_name: 仅用于 tiktoken 选择对应的分词器来计算 token 数，不影响实际调用的 LLM 模型
    # tiktoken 不认识国产模型(如 DeepSeek)时会 fallback 到 cl100k_base，对截断场景误差可接受
    def __init__(self, max_tokens: int = 3000, model_name: str = "gpt-3.5-turbo"):
        self.max_tokens = max_tokens
        try:
            self.encoding = tiktoken.encoding_for_model(model_name)
        except KeyError:
            self.encoding = tiktoken.get_encoding("cl100k_base")
        
        self._conversations: Dict[str, List[Dict[str, str]]] = {}

    def get_or_create_session(self, session_id: str) -> List[Dict[str, str]]:
        if session_id not in self._conversations:
            self._conversations[session_id] = []
        return self._conversations[session_id]

    def add_message(self, session_id: str, role: str, content: str):
        session = self.get_or_create_session(session_id)
        session.append({"role": role, "content": content})
        self._truncate_session(session_id)

    def get_history(self, session_id: str) -> List[Dict[str, str]]:
        return self.get_or_create_session(session_id)

    def _calculate_tokens(self, text: str) -> int:
        return len(self.encoding.encode(text))

    def _truncate_session(self, session_id: str):
        session = self._conversations[session_id]
        
        total_tokens = 0
        keep_messages = []
        
        # 从最新消息开始回溯计算
        for msg in reversed(session):
            msg_tokens = self._calculate_tokens(msg["content"])
            # 为每条消息增加一些固定 token 开销 (role 等)
            msg_tokens += 4 
            
            if total_tokens + msg_tokens > self.max_tokens:
                break
                
            total_tokens += msg_tokens
            keep_messages.insert(0, msg)
            
        self._conversations[session_id] = keep_messages
