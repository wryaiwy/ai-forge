from agents.base_agent import BaseAgent
from app.schemas import ChatRequest
from app.exceptions import AgentExecutionError
import uuid
import logging
import asyncio
import re
import json
from typing import Optional
from services.session_memory import SessionMemory
from app.config import settings
from services.retrieval_service import retrieval_service
from app.tools.web_search import WebSearchToolSchema

logger = logging.getLogger(__name__)


class WritingChatAgent(BaseAgent):
    """
    AI 文章写作与创作助手 (Writing Chat Agent)
    处理写作多轮对话、构思大纲、生成文章草稿并支持会话记忆、联网搜索与查询重写
    """

    SYSTEM_PROMPT = """
        你是一个顶尖的专业 AI 技术文章创作与写作助手（Writing Chat Agent）。你的核心职责是协助用户撰写、润色、构思和优化技术文章。
        你可以为用户提供以下帮助：
        1. **构思与大纲**：根据用户给出的主题，生成结构清晰、逻辑严谨的文章大纲。
        2. **正文撰写**：根据大纲或指定要点，撰写高质量、深入浅出的技术干货文章。
        3. **优化润色**：优化语句流畅度，增强技术严谨性和可读性。
        4. **技术解答与补充**：为文章补充精确的代码示例、架构解释或最佳实践。
        
        【重要输出规范（极度重要）】：
        当用户要求你起草文章、生成大纲、撰写正文或修改草稿时：
        1. **文章标题**：如果用户**明确提供了文章标题**，请使用用户的标题；如果用户**没有提供标题**，你**必须主动构思并拟定一个吸引人且专业的技术文章标题**！绝不能使用占位符或留空。
        2. **文章标签**：如果用户**没有提供标签**，你**必须根据文章主题，自动提取/生成 3~5 个高度相关的专业技术标签**（以英文逗号分隔）！绝不能留空。
        3. 为了确保前端写作工作台能够自动结构化解析并一键回填到发表文章表单，请**必须**按以下标准的 Markdown 格式组织你的回复：
        
        # [这里务必输出文章标题]
        > **文章标签**: [标签1, 标签2, 标签3]
        
        ---
        
        [这里写文章的正文内容，使用标准的 Markdown 格式、分级标题和代码块...]
        
        注意：
        - 标题务必按 `# 文章标题` 格式放在第一行。
        - 标签务必按 `> **文章标签**: 标签1, 标签2` 格式放在第一行标题之下。
        - 如果用户只是在和你进行日常问答或简单讨论，不需要完整文章结构时，自然回答即可；只要涉及文章或草稿生成，请务必严格遵守上述规范！
    """

    # 用于保存后台任务的强引用，防止被 Python GC 提前回收
    _bg_tasks = set()

    def _fire_and_forget(self, coro):
        """安全地触发后台任务，消除警告并防止 GC 意外清理"""
        task = asyncio.create_task(coro)
        self._bg_tasks.add(task)
        task.add_done_callback(self._bg_tasks.discard)

    def __init__(self, db_session=None):
        # 写作时保持0.7的温度，兼顾创造性与逻辑严谨性
        super().__init__(system_prompt=self.SYSTEM_PROMPT, temperature=0.7)
        self.db_session = db_session
        self.memory = SessionMemory(max_tokens=settings.CHAT_MEMORY_MAX_TOKENS)

    def _get_or_create_session_id(self, session_id: Optional[str] = None) -> str:
        """获取或创建会话 ID"""
        return session_id or str(uuid.uuid4())

    def _extract_metadata(self, text: str) -> tuple[str, str, str]:
        """从 AI 生成的回复中结构化提取标题、标签和正文"""
        title = ""
        tags = ""
        content = text

        # 1. 提取标题
        title_match = re.search(r"^#\s+([^\n]+)", text, re.MULTILINE) or re.search(
            r"(?:【(?:文章)?标题】|(?:文章)?标题[:：])\s*([^\n]+)", text)
        if title_match:
            title = title_match.group(1).strip("[] \t\r")

        # 2. 提取标签
        tags_match = re.search(r"(?:>\s*)?(?:\*\*)?(?:文章标签|标签|Tags)(?:\*\*)?[:：]\s*([^\n]+)", text, re.IGNORECASE)
        if tags_match:
            tags = tags_match.group(1).strip("[] \t\r")

        # 3. 提取清洁正文 (移除顶部的标题、标签行和分割线 ---)
        if title_match:
            content = content.replace(title_match.group(0), "", 1)
        if tags_match:
            content = content.replace(tags_match.group(0), "", 1)
        content = re.sub(r"^\s*---\s*", "", content, count=1, flags=re.MULTILINE).strip()

        return title, tags, content or text

    def _get_history(self, session_id: str, long_term_context: str = "", retrieval_context: str = "",
                     web_search_context: str = "") -> list:
        """获取历史对话并注入上下文摘要、长期记忆、检索资料及联网搜索结果"""
        history = self.memory.get_history(session_id)
        summary = self.memory.get_summary(session_id)

        system_content = []
        if summary:
            system_content.append(f"【过去对话摘要】\n{summary}")
        if long_term_context:
            system_content.append(f"【相关历史记忆片段】\n{long_term_context}")
        if retrieval_context:
            system_content.append(
                f"【本地参考资料】\n{retrieval_context}\n\n请参考上述本地资料辅助技术写作或回答用户问题。")
        if web_search_context:
            system_content.append(
                f"【联网搜索实时参考资料】\n{web_search_context}\n\n请参考上述最新网络搜索结果增强文章的时效性与准确性。")

        if system_content:
            history.insert(0, {"role": "system", "content": "\n\n".join(system_content)})

        return history

    def _save_message(self, session_id: str, role: str, content: str):
        """保存消息到会话历史"""
        self.memory.add_message(session_id, role, content)

    async def check_and_compress_memory(self, session_id: str):
        """检查并压缩记忆"""
        try:
            session = self.memory.get_raw_history(session_id)
            if not session:
                return

            total_tokens = sum(len(self.memory.encoding.encode(str(msg.get("content", "")))) + 4 for msg in session)

            if total_tokens > self.memory.max_tokens * 0.8:
                keep_count = len(session) // 2
                if keep_count == 0:
                    return
                old_msgs = session[:-keep_count]

                old_summary = self.memory.get_summary(session_id)

                prompt = "请总结以下对话内容，提取关于技术文章写作的主题、核心论点及用户偏好关键信息。如果已有摘要，请将其与新对话内容合并，形成一个精简、连贯的新摘要。\n"
                if old_summary:
                    prompt += f"\n【已有摘要】\n{old_summary}\n"

                prompt += "\n【新对话内容】\n"
                for m in old_msgs:
                    prompt += f"{m['role']}: {m['content']}\n"

                new_summary = await self.execute(user_input=prompt, history=None)

                self.memory.set_summary(session_id, new_summary)
                self.memory.trim_history(session_id, keep_count)
                logger.info(f"写作会话 {session_id} 记忆压缩完成，更新了上下文摘要并清理了 {len(old_msgs)} 条旧消息。")
        except Exception as e:
            logger.error(f"写作会话 {session_id} 记忆压缩失败: {e}")

    async def run_stream(self, request: ChatRequest):
        """执行写作AI对话助手流式对话"""
        session_id = self._get_or_create_session_id(request.session_id)

        # 1. 委托给 memory 获取长期向量上下文
        long_term_context = await self.memory.get_long_term_context(session_id, request.message)

        # 2. 检索相关参考资料（同时获取经过大模型重写优化的高准确率关键词）
        rewritten_query = request.message
        try:
            retrieved_docs, rewritten_query = await retrieval_service.retrieve_with_rewrite(raw_query=request.message)
            retrieval_context = "\n\n".join(
                [doc.document.page_content for doc in retrieved_docs]) if retrieved_docs else ""
            if retrieval_context:
                logger.info(f"【写作流式对话】注入Prompt的检索上下文片段:\n{retrieval_context[:200]}...")
            else:
                logger.info("【写作流式对话】检索结果为空，不注入上下文。")
        except Exception as e:
            logger.error(f"检索资料失败: {e}")
            retrieval_context = ""

        # 3. 接入联网搜索获取实时资讯与参考资料，复用重写后的核心关键词大幅提升外网命中率
        web_search_context = ""
        try:
            search_tool = WebSearchToolSchema(query=rewritten_query)
            web_results = await search_tool.execute()
            if web_results and "未找到" not in str(web_results) and "错误" not in str(
                    web_results) and "失败" not in str(web_results):
                web_search_context = str(web_results)
                logger.info(f"【写作流式对话】联网搜索获取到最新参考资料:\n{web_search_context[:200]}...")
            else:
                logger.info("【写作流式对话】联网搜索无有效参考结果。")
        except Exception as e:
            logger.error(f"联网搜索失败: {e}")

        # 4. 从 Redis 获取历史记录，并注入上下文摘要、长期记忆、本地检索和联网搜索资料
        history = self._get_history(session_id, long_term_context, retrieval_context, web_search_context)

        # 5. 将本次用户提问存入 Redis 记忆
        self._save_message(session_id, "user", request.message)

        full_response = ""
        try:
            async for chunk in self.execute_stream(
                    user_input=request.message,
                    history=history if history else None
            ):
                chunk_str = chunk if isinstance(chunk, str) else getattr(chunk, "content", str(chunk))
                if chunk_str:
                    full_response += chunk_str
                    data_str = chunk_str.replace("\n", "\ndata: ")
                    yield f"data: {data_str}\n\n"

            # 流式结束，提取结构化标题、标签和正文，通过 [META] 事件发送给前端
            title, tags, clean_content = self._extract_metadata(full_response)
            meta_data = json.dumps({
                "articleTitle": title,
                "articleTags": tags,
                "content": clean_content
            }, ensure_ascii=False)
            yield f"data: [META]{meta_data}\n\n"
        except Exception as e:
            logger.error(f"写作AI对话助手流式执行失败: {e}")
            raise AgentExecutionError(detail=f"AI 写作回复生成失败: {str(e)}")
        finally:
            self._save_message(session_id, "assistant", full_response)
            self._fire_and_forget(self.check_and_compress_memory(session_id))
            self._fire_and_forget(self.memory.save_long_term_memory(session_id, request.message, full_response))
