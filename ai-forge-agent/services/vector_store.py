from langchain_elasticsearch import ElasticsearchStore
from langchain_core.documents import Document
from app.config import settings
from services.embedding import EmbeddingService
import logging
from typing import Optional, List

logger = logging.getLogger(__name__)


class VectorStoreService:
    """
    向量数据库操作统一封装
    支持 Elasticsearch 作为向量存储后端
    """
    _instance: Optional['VectorStoreService'] = None
    _vector_store: Optional[ElasticsearchStore] = None
    _embedding_service: EmbeddingService = None

    def __new__(cls) -> 'VectorStoreService':
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._embedding_service = EmbeddingService()
        return cls._instance

    def _get_store(self) -> ElasticsearchStore:
        """获取向量存储实例"""
        if self._vector_store is None:
            logger.info(f"初始化 Elasticsearch 向量数据库: {settings.ES_HOST}:{settings.ES_PORT}")
            self._vector_store = ElasticsearchStore(
                es_url=f"http://{settings.ES_HOST}:{settings.ES_PORT}",
                es_user=settings.ES_USER if settings.ES_USER else None,
                es_password=settings.ES_PASSWORD if settings.ES_PASSWORD else None,
                index_name=settings.ES_INDEX_NAME,
                embedding=self._embedding_service.get_embeddings()
            )
        return self._vector_store

    async def add_documents(self, texts: List[str], metadatas: Optional[List[dict]] = None) -> List[str]:
        """批量添加文档到向量库"""
        store = self._get_store()
        return await store.aadd_texts(texts=texts, metadatas=metadatas)

    async def delete_documents(self, ids: List[str]) -> bool:
        """从向量库删除文档"""
        store = self._get_store()
        try:
            store.delete(ids=ids)
            return True
        except Exception as e:
            logger.error(f"删除向量文档失败: {e}")
            return False

    async def similarity_search(self, query: str, k: int = 5, filter_dict: Optional[dict] = None) -> List[Document]:
        """相似度搜索"""
        store = self._get_store()
        if filter_dict:
            return await store.asimilarity_search(query=query, k=k, filter=filter_dict)
        return await store.asimilarity_search(query=query, k=k)

    async def similarity_search_with_score(self, query: str, k: int = 5, filter_dict: Optional[dict] = None) -> List[tuple]:
        """带相似度得分的搜索"""
        store = self._get_store()
        if filter_dict:
            return await store.asimilarity_search_with_score(query=query, k=k, filter=filter_dict)
        return await store.asimilarity_search_with_score(query=query, k=k)


vector_store_service = VectorStoreService()
