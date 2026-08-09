import logging
from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage
from app.config import settings

logger = logging.getLogger(__name__)


async def analyze_image(image_url: str) -> str:
    """
    使用多模态模型对图片进行详细描述和文字提取
    """
    if not image_url:
        return ""

    try:
        vision_model = ChatOpenAI(
            openai_api_key=settings.OPENAI_API_KEY,
            openai_api_base=settings.OPENAI_BASE_URL,
            model_name=settings.OPENAI_VISION_MODEL,
            temperature=0,
        )

        vision_msg = HumanMessage(
            content=[
                {"type": "text", "text": "请详细描述这张图片的内容，越详细越好。如果图片包含文字，请精确提取出来。"},
                {"type": "image_url", "image_url": {"url": image_url}},
            ]
        )

        response = await vision_model.ainvoke([vision_msg])
        return response.content
    except Exception as e:
        logger.error(f"图片解析服务异常: {e}")
        raise e
