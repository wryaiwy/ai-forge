from langchain_elasticsearch import ElasticsearchStore
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
from app.config import settings
from services.embedding import EmbeddingService
from services.llm_provider import LLMProvider
from langchain_core.messages import SystemMessage, HumanMessage
import json
import logging
from typing import Optional, List

logger = logging.getLogger(__name__)


class VectorStoreService:
    """
    向量数据库操作统一封装
    支持 Elasticsearch 作为向量存储后端
    """
    _instance: Optional['VectorStoreService'] = None
    _stores: dict = None
    _embedding_service: EmbeddingService = None
    _text_splitter: RecursiveCharacterTextSplitter = None

    def __new__(cls) -> 'VectorStoreService':
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._stores = {}
            cls._instance._embedding_service = EmbeddingService()
            # 中文一个字符可能对应多个 Token，如果模型（如 m3e/bge 等）最大上下文是 512 Token
            # 则 1000 字符会超出限制。这里稳妥起见，把字符切片大小改小。
            cls._instance._text_splitter = RecursiveCharacterTextSplitter(
                chunk_size=300,
                chunk_overlap=50,
                length_function=len
            )
        return cls._instance

    def _get_store(self, index_name: str = None) -> ElasticsearchStore:
        """获取向量存储实例"""
        index_name = index_name or settings.ES_INDEX_NAME
        
        if index_name not in self._stores:
            logger.info(f"初始化 Elasticsearch 向量数据库: {settings.ES_HOST}:{settings.ES_PORT} 索引: {index_name}")
            self._stores[index_name] = ElasticsearchStore(
                es_url=f"http://{settings.ES_HOST}:{settings.ES_PORT}",
                es_user=settings.ES_USER if settings.ES_USER else None,
                es_password=settings.ES_PASSWORD if settings.ES_PASSWORD else None,
                index_name=index_name,
                embedding=self._embedding_service.get_embeddings()
            )
        return self._stores[index_name]

    async def enrich_metadata(self, text: str, meta: dict) -> dict:
        """
        利用 LLM 进行元数据打标（提取关键词、生成摘要等）
        """
        llm = LLMProvider()
        sys_msg = SystemMessage(content="你是一个专业的元数据打标助手。请从给定文本中提取3-5个核心关键词(keywords)和一句话摘要(summary)。严格返回JSON格式，如: {\"keywords\": [\"a\", \"b\"], \"summary\": \"...\"}，不要任何额外说明。")
        usr_msg = HumanMessage(content=f"提取以下文本的元数据:\n{text[:2000]}") # 限制长度避免超出 token
        
        try:
            res = await llm.ainvoke([sys_msg, usr_msg], temperature=0.1)
            content = res.content.strip()
            if content.startswith("```json"):
                content = content[7:-3]
            elif content.startswith("```"):
                content = content[3:-3]
            
            extracted = json.loads(content)
            meta["llm_keywords"] = extracted.get("keywords", [])
            meta["llm_summary"] = extracted.get("summary", "")
            logger.info(f"LLM 元数据打标成功! 提取到关键词: {meta['llm_keywords']}")
        except Exception as e:
            logger.warning(f"LLM 元数据打标失败，跳过: {e}")
            
        return meta

    async def add_documents(self, texts: List[str], metadatas: Optional[List[dict]] = None, auto_tagging: bool = False, index_name: Optional[str] = None) -> List[str]:
        """批量添加文档到向量库"""
        store = self._get_store(index_name)
        
        docs = []
        for i, text in enumerate(texts):
            meta = metadatas[i] if metadatas else {}
            if auto_tagging:
                meta = await self.enrich_metadata(text, meta.copy())
                
            # 对长文本进行切片
            splits = self._text_splitter.split_text(text)
            for split in splits:
                docs.append(Document(page_content=split, metadata=meta))
                
        if docs:
            logger.info(f"即将入库 {len(docs)} 个 Chunk，最大字符长度: {max(len(d.page_content) for d in docs)}")
            # 手动分批给模型，避免一次性给 Ollama 传太多文本导致 OOM 或超出上下文总长限制
            batch_size = 5
            inserted_ids = []
            for i in range(0, len(docs), batch_size):
                batch_docs = docs[i:i + batch_size]
                ids = await store.aadd_documents(batch_docs)
                inserted_ids.extend(ids)
            return inserted_ids
        return []

    async def delete_documents(self, ids: List[str], index_name: Optional[str] = None) -> bool:
        """从向量库删除文档"""
        store = self._get_store(index_name)
        try:
            store.delete(ids=ids)
            return True
        except Exception as e:
            logger.error(f"删除向量文档失败: {e}")
            return False

    async def delete_by_biz(self, biz_id: str, biz_type: str, index_name: Optional[str] = None) -> bool:
        """根据业务ID和业务类型删除向量库文档"""
        target_index = index_name or settings.ES_INDEX_NAME
        store = self._get_store(target_index)
        try:
            store.client.delete_by_query(
                index=target_index,
                body={
                    "query": {
                        "bool": {
                            "must": [
                                {"term": {"metadata.bizId.keyword": biz_id}},
                                {"term": {"metadata.bizType.keyword": biz_type}}
                            ]
                        }
                    }
                }
            )
            return True
        except Exception as e:
            logger.error(f"按业务ID删除向量文档失败: {e}")
            return False

    async def similarity_search(self, query: str, k: int = 5, filter_dict: Optional[dict] = None, index_name: Optional[str] = None) -> List[Document]:
        """相似度搜索"""
        store = self._get_store(index_name)
        if filter_dict:
            return await store.asimilarity_search(query=query, k=k, filter=filter_dict)
        return await store.asimilarity_search(query=query, k=k)

    async def similarity_search_with_score(self, query: str, k: int = 5, filter_dict: Optional[dict] = None, index_name: Optional[str] = None) -> List[tuple]:
        """带相似度得分的搜索"""
        store = self._get_store(index_name)
        if filter_dict:
            return await store.asimilarity_search_with_score(query=query, k=k, filter=filter_dict)
        return await store.asimilarity_search_with_score(query=query, k=k)

    async def similarity_search_by_vector(self, embedding: List[float], k: int = 5, filter_dict: Optional[dict] = None, index_name: Optional[str] = None) -> List[Document]:
        """通过向量进行相似度搜索"""
        store = self._get_store(index_name)
        if filter_dict:
            return await store.asimilarity_search_by_vector(embedding=embedding, k=k, filter=filter_dict)
        return await store.asimilarity_search_by_vector(embedding=embedding, k=k)

    async def keyword_search(self, query: str, k: int = 5, filter_dict: Optional[dict] = None, index_name: Optional[str] = None) -> List[Document]:
        """BM25 文本搜索"""
        target_index = index_name or settings.ES_INDEX_NAME
        store = self._get_store(target_index)
        import asyncio

        def _do_search():
            es_query = {
                "bool": {
                    "must": [
                        {
                            "match": {
                                "text": query
                            }
                        }
                    ]
                }
            }
            if filter_dict:
                filter_clauses = [{"term": {f"metadata.{key}.keyword": val}} for key, val in filter_dict.items()]
                es_query["bool"]["filter"] = filter_clauses
                
            return store.client.search(index=target_index, query=es_query, size=k)
            
        try:
            res = await asyncio.to_thread(_do_search)
            docs = []
            for hit in res.get("hits", {}).get("hits", []):
                content = hit["_source"].get("text", "")
                metadata = hit["_source"].get("metadata", {})
                docs.append(Document(page_content=content, metadata=metadata))
            return docs
        except Exception as e:
            logger.error(f"BM25 文本搜索失败: {e}")
            return []


vector_store_service = VectorStoreService()
