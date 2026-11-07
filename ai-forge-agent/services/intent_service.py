import logging
from langchain_core.language_models.chat_models import BaseChatModel

logger = logging.getLogger(__name__)


async def analyze_rag_intent(model: BaseChatModel, user_message: str) -> bool:
    """
    意图识别：判断用户输入是否需要进行 RAG（查询平台内部资料）
    
    Args:
        model: 用于执行意图识别的 LLM 模型实例
        user_message: 用户输入的文本
        
    Returns:
        bool: 如果需要 RAG 返回 True，否则返回 False
    """
    prompt = (
        "请判断以下用户输入是否需要查询平台内部的通用知识库、技术文档或使用指南来回答。\n"
        "注意：如果用户询问的是个人专属数据（例如：'我是谁'、'我的个人信息'、'我发布了哪些文章'、'我的文章列表'）或普通闲聊，请回复 'NO'；"
        "只有需要检索平台公共文档知识库时，才仅回复 'YES'。\n"
        f"用户输入: {user_message}"
    )

    try:
        response = await model.ainvoke([{"role": "user", "content": prompt}])
        return "YES" in response.content.upper()
    except Exception as e:
        logger.warning(f"RAG意图识别调用异常，默认回退为 False: {e}")
        return False
