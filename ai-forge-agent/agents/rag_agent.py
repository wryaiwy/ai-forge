from agents.base_agent import BaseAgent
from app.schemas import RagQueryRequest, RagQueryResponse, RagSourceItem
from app.exceptions import AgentExecutionError, ResourceNotFoundError
from services.vector_store import vector_store_service
from langchain_core.documents import Document
import logging
from typing import Optional, List

logger = logging.getLogger(__name__)


class RagAgent(BaseAgent):
    """
    知识库 RAG Agent
    处理文章/数据集查询与总结
    """

    SYSTEM_PROMPT_TEMPLATE = """你是一个基于知识库的智能问答助手。请根据以下提供的参考资料来回答用户的问题。

【参考资料】
{context}

【回答要求】
1. 严格基于参考资料回答，不要编造信息
2. 如果参考资料中没有相关信息，请明确告知用户"根据现有知识库，暂时无法回答此问题"
3. 回答要条理清晰，必要时使用列表或分点说明
4. 使用中文回答"""

    def __init__(self, db_session=None):
        super().__init__(system_prompt="", temperature=0.5)
        self.db_session = db_session

    async def _search_knowledge(self, query: str, top_k: int, dataset_id: Optional[str] = None) -> List[Document]:
        """从向量库检索相关知识"""
        filter_dict = {"dataset_id": dataset_id} if dataset_id else None
        docs = await vector_store_service.similarity_search(
            query=query,
            k=top_k,
            filter_dict=filter_dict
        )
        return docs

    def _format_context(self, docs: List[Document]) -> str:
        """格式化检索结果为上下文文本"""
        if not docs:
            return "暂无相关参考资料。"

        context_parts = []
        for i, doc in enumerate(docs, 1):
            context_parts.append(f"[资料{i}]: {doc.page_content}")
        return "\n\n".join(context_parts)

    def _build_source_items(self, docs: List[Document]) -> List[RagSourceItem]:
        """构建引用来源列表"""
        sources = []
        for doc in docs:
            sources.append(RagSourceItem(
                content=doc.page_content[:500],
                score=doc.metadata.get("score", 0.0),
                metadata={k: v for k, v in doc.metadata.items() if k != "score"}
            ))
        return sources

    async def run(self, request: RagQueryRequest) -> RagQueryResponse:
        """执行 RAG 查询"""
        docs = await self._search_knowledge(
            query=request.query,
            top_k=request.top_k,
            dataset_id=request.dataset_id
        )

        if not docs:
            return RagQueryResponse(
                answer="根据现有知识库，暂时无法找到与您的问题相关的资料。您可以尝试换一种方式提问。",
                sources=[]
            )

        context = self._format_context(docs)
        system_prompt = self.SYSTEM_PROMPT_TEMPLATE.format(context=context)

        try:
            answer = await self.execute(
                user_input=request.query,
                history=[{"role": "system", "content": system_prompt}]
            )
        except Exception as e:
            logger.error(f"RAG Agent 执行失败: {e}")
            raise AgentExecutionError(detail=f"知识库查询失败: {str(e)}")

        sources = self._build_source_items(docs)

        return RagQueryResponse(
            answer=answer,
            sources=sources
        )
