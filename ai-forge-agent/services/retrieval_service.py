import logging
import json
import hashlib
import redis
from typing import List, Optional

from langchain_core.messages import SystemMessage, HumanMessage
from app.schemas.rag import RetrievalQuery, RetrievedDocument
from services.llm_provider import LLMProvider
from services.embedding import EmbeddingService
from services.vector_store import vector_store_service, VectorStoreService
from app.config import settings

logger = logging.getLogger(__name__)


class RetrievalService:
    """
    RAG 检索编排服务
    负责串联查询重写、意图识别、向量召回与重排整个链路。
    """
    _instance: Optional['RetrievalService'] = None

    # 依赖组件
    _llm_provider: LLMProvider = None
    _embedding_service: EmbeddingService = None
    _vector_store: VectorStoreService = None
    _redis_client: redis.Redis = None

    def __new__(cls) -> 'RetrievalService':
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            # 依赖注入
            cls._instance._llm_provider = LLMProvider()
            cls._instance._embedding_service = EmbeddingService()
            cls._instance._vector_store = vector_store_service  # 直接复用已有的单例
            
            # 初始化 Redis 客户端用于缓存
            cls._instance._redis_client = redis.Redis(
                host=settings.REDIS_HOST,
                port=settings.REDIS_PORT,
                db=settings.REDIS_DB,
                password=settings.REDIS_PASSWORD
            )
        return cls._instance

    async def _rewrite_query(self, raw_query: str) -> RetrievalQuery:
        """
        查询重写与意图识别
        利用 LLM 解析用户意图并重写查询以利于向量检索。
        """
        system_prompt = """你是一个专业的搜索意图分析专家。你的任务是分析用户的输入，识别其意图，进行查询重写，并提取可能的过滤条件。
        
            请以 JSON 格式输出，包含以下字段：
            1. "intent": 用户意图，如 "闲聊" (chat), "问答" (qa), "任务指令" (command) 等。
            2. "rewritten_query": 重写后的查询。如果是错别字或指代不清，请结合常识修正。如果是闲聊，可以直接保留原意。重写后的查询应更利于向量检索。
            3. "filters": 提取出的元数据过滤条件（字典），如无则为空字典 {}。可能包含 "biz_type", "time_range" 等。
            
            输出示例：
            ```json
            {
              "intent": "qa",
              "rewritten_query": "AIForge系统的部署教程是什么？",
              "filters": {}
            }
            ```
            请确保只输出合法的 JSON 字符串，不要包含其他解释性文字。"""

        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=f"用户输入: {raw_query}")
        ]
        
        try:
            response = await self._llm_provider.ainvoke(messages=messages, temperature=0.1)
            content = response.content
            
            # 清理可能的 markdown code block
            content = content.strip()
            if content.startswith("```json"):
                content = content[7:]
            elif content.startswith("```"):
                content = content[3:]
            if content.endswith("```"):
                content = content[:-3]
                
            parsed_data = json.loads(content.strip())
            
            return RetrievalQuery(
                original_query=raw_query,
                rewritten_query=parsed_data.get("rewritten_query", raw_query),
                intent=parsed_data.get("intent", "qa"),
                filters=parsed_data.get("filters", {})
            )
        except Exception as e:
            logger.error(f"查询重写失败: {e}")
            # 降级处理：直接使用原查询
            return RetrievalQuery(
                original_query=raw_query,
                rewritten_query=raw_query,
                intent="qa",
                filters={}
            )

    async def _vectorize_query(self, query: str) -> List[float]:
        """
        查询向量化（带 Redis 缓存）
        对于极高频的固化 Query 缓存其向量表示，降低延迟。
        """
        cache_key = f"aiforge:retrieval:query_vector:{hashlib.md5(query.encode('utf-8')).hexdigest()}"
        try:
            cached_data = self._redis_client.get(cache_key)
            if cached_data:
                logger.info(f"命中查询向量缓存: {query}")
                return json.loads(cached_data)
        except Exception as e:
            logger.warning(f"读取查询向量缓存失败: {e}")

        # 调用 Embedding 服务
        vector = await self._embedding_service.embed_query(query)
        
        try:
            # 缓存 7 天
            self._redis_client.setex(cache_key, 86400 * 7, json.dumps(vector))
        except Exception as e:
            logger.warning(f"写入查询向量缓存失败: {e}")
            
        return vector

    async def _rerank(self, query: str, docs: List[RetrievedDocument], top_n: int = 5, threshold: float = 0.3) -> List[RetrievedDocument]:
        """
        重排与精排
        利用 LLM 对召回的 Top-K 文档与重写后的 Query 进行交叉注意力计算，给出精确相关性打分。
        """
        if not docs:
            return []

        system_prompt = """你是一个专业的文档相关性排序专家。你的任务是评估一系列召回文档与用户查询的相关性。
        请对提供的每篇文档进行打分（0.0 到 1.0 之间的浮点数，1.0 表示完全相关，0.0 表示完全不相关）。
        
        请以 JSON 格式输出，包含一个 "scores" 字段，它是一个列表，每个元素包含文档的 "id" 和 "score"。
        
        输出示例：
        ```json
        {
          "scores": [
            {"id": 0, "score": 0.95},
            {"id": 1, "score": 0.30},
            {"id": 2, "score": 0.85}
          ]
        }
        ```
        请确保只输出合法的 JSON 字符串，不要包含其他解释性文字。"""

        # 截取过长的文档内容，防止 token 溢出
        docs_text = ""
        for idx, rdoc in enumerate(docs):
            content = rdoc.document.page_content[:800] # 限制单篇文档长度
            docs_text += f"文档 ID: {idx}\n内容: {content}\n\n"

        user_prompt = f"用户查询: {query}\n\n待评估文档列表:\n{docs_text}"

        messages = [
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt)
        ]

        try:
            response = await self._llm_provider.ainvoke(messages=messages, temperature=0.1)
            content = response.content.strip()
            
            if content.startswith("```json"):
                content = content[7:]
            elif content.startswith("```"):
                content = content[3:]
            if content.endswith("```"):
                content = content[:-3]
                
            parsed_data = json.loads(content.strip())
            scores_list = parsed_data.get("scores", [])
            
            score_map = {int(item["id"]): float(item["score"]) for item in scores_list if "id" in item and "score" in item}
            
            for idx, rdoc in enumerate(docs):
                rdoc.relevance_score = score_map.get(idx, 0.0)
                
        except Exception as e:
            logger.error(f"LLM重排失败，采用降级策略 (保留原顺序): {e}")
            for idx, rdoc in enumerate(docs):
                rdoc.relevance_score = max(0.0, 1.0 - idx * 0.05)

        # 阈值截断与排序
        filtered_docs = [doc for doc in docs if doc.relevance_score >= threshold]
        filtered_docs.sort(key=lambda x: x.relevance_score, reverse=True)
        
        return filtered_docs[:top_n]

    async def retrieve(self, raw_query: str, filters: dict = None) -> List[RetrievedDocument]:
        """
        检索主干流程入口
        """
        # 1. 查询重写与意图识别
        retrieval_query = await self._rewrite_query(raw_query)
        logger.info(f"查询重写与意图识别完成: {retrieval_query.model_dump()}")
        
        # 2. 查询向量化
        query_vector = await self._vectorize_query(retrieval_query.rewritten_query)
        retrieval_query.query_vector = query_vector
        logger.info(f"查询向量化完成，向量维度: {len(query_vector)}")

        # 3. 向量检索(召回) --- 合并用户传入的 filters 和大模型提取的 filters
        final_filters = retrieval_query.filters or {}
        if filters:
            final_filters.update(filters)
            
        logger.info(f"执行向量召回，过滤条件: {final_filters}")
        
        # 由于 ElasticsearchStore 的 similarity_search_by_vector 未完整实现，这里降级使用文本召回
        # LangChain 底层会自动处理该 query 的 Embedding
        retrieved_docs_raw = await self._vector_store.similarity_search(
            query=retrieval_query.rewritten_query,
            k=20,  # 返回最相似的 Top-20 个文档片段
            filter_dict=final_filters if final_filters else None  # 如果有过滤条件，就传给ES做前置过滤，缩小搜索范围。
        )
        
        # 4. 将召回的文档转为统一的数据结构 RetrievedDocument
        retrieved_docs = [
            RetrievedDocument(document=doc, relevance_score=0.0) 
            for doc in retrieved_docs_raw
        ]
        
        logger.info(f"向量召回完成，共召回 {len(retrieved_docs)} 条文档")

        # 5. 重排与精排(Reranking)
        retrieved_docs = await self._rerank(retrieval_query.rewritten_query, retrieved_docs, top_n=5, threshold=0.3)
        
        logger.info(f"重排完成，最终返回 {len(retrieved_docs)} 条文档")

        return retrieved_docs


# 导出单例供全局调用
retrieval_service = RetrievalService()
