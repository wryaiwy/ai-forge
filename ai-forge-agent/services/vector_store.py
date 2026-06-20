from langchain_elasticsearch import ElasticsearchStore
from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter
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
    _text_splitter: RecursiveCharacterTextSplitter = None

    def __new__(cls) -> 'VectorStoreService':
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance._embedding_service = EmbeddingService()
            # 中文一个字符可能对应多个 Token，如果模型（如 m3e/bge 等）最大上下文是 512 Token
            # 则 1000 字符会超出限制。这里稳妥起见，把字符切片大小改小。
            cls._instance._text_splitter = RecursiveCharacterTextSplitter(
                chunk_size=300,
                chunk_overlap=50,
                length_function=len
            )
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
        
        docs = []
        for i, text in enumerate(texts):
            meta = metadatas[i] if metadatas else {}
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

    async def delete_documents(self, ids: List[str]) -> bool:
        """从向量库删除文档"""
        store = self._get_store()
        try:
            store.delete(ids=ids)
            return True
        except Exception as e:
            logger.error(f"删除向量文档失败: {e}")
            return False

    async def delete_by_biz(self, biz_id: str, biz_type: str) -> bool:
        """根据业务ID和业务类型删除向量库文档"""
        store = self._get_store()
        try:
            store.client.delete_by_query(
                index=settings.ES_INDEX_NAME,
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
