from agents.base_agent import BaseAgent
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from app.tools.tool_executor import ToolExecutor
import logging

logger = logging.getLogger(__name__)

class DeliveryAndHealAgent(BaseAgent):
    """
    最终交付与异常自愈 Agent
    请求 Java 执行入库。若抛出业务异常，交给大模型自行修改并重试。
    """
    def __init__(self):
        super().__init__(temperature=0.3)
        self.llm = self._llm_provider.get_chat_model()
        self.tool_executor = ToolExecutor()

    async def run(self, request, user_token: str = "") -> dict:
        max_retries = 3
        current_content = request.draft_content
        
        for attempt in range(max_retries):
            logger.info(f"Delivery attempt {attempt + 1}/{max_retries}")
            
            # 1. 尝试交付入库 (调用 HTTP 模式的 Java 接口)
            try:
                result = await self.tool_executor.execute_tool(
                    "save_draft_tool",
                    {
                        "title": request.title,
                        "content": current_content,
                        "user_id": request.user_id
                    },
                    user_token=user_token
                )
                
                # 2. 判断成功与否
                is_success = False
                if isinstance(result, dict):
                    if result.get("status") == "success" or result.get("code") in (200, 0) or result.get("success") is True:
                        is_success = True

                if is_success:
                    logger.info("入库交付成功！")
                    return {
                        "success": True,
                        "final_content": current_content,
                        "attempts": attempt + 1,
                        "message": "交付入库成功"
                    }
                
                # 若无 success 标志，提取错误信息
                error_msg = result.get("message", str(result)) if isinstance(result, dict) else str(result)
                
            except Exception as e:
                error_msg = str(e)
                
            logger.warning(f"入库失败，触发自愈流程，错误信息: {error_msg}")
            
            if attempt == max_retries - 1:
                return {
                    "success": False,
                    "final_content": current_content,
                    "attempts": attempt + 1,
                    "message": f"达到最大重试次数，最终交付失败。最后一次错误：{error_msg}"
                }
            
            # 3. 触发 LLM 异常自愈
            heal_prompt = ChatPromptTemplate.from_template(
                "你是一个极其聪明的错误修复专家。\n"
                "我们试图将一篇文章保存到数据库时，后端系统抛出了以下业务异常（例如：超出字数限制、包含违禁词等）：\n"
                "【错误信息】：{error_msg}\n\n"
                "【当前报错的文章内容】：\n{current_content}\n\n"
                "【你的任务】：\n"
                "请仔细分析上面的错误信息。在尽量不改变文章原意和核心质量的前提下，修改原文以解决上述报错问题。\n"
                "请直接输出修改后的全新文章内容，绝对不要包含任何多余的解释、问候或Markdown代码块标记！\n"
                "【修复后的纯净内容】："
            )
            
            heal_chain = heal_prompt | self.llm | StrOutputParser()
            current_content = await heal_chain.ainvoke({
                "error_msg": error_msg,
                "current_content": current_content
            })
            
        return {
            "success": False,
            "final_content": current_content,
            "attempts": max_retries,
            "message": "未知异常导致循环跳出"
        }
