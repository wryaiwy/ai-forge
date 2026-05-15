from langchain_ollama import OllamaEmbeddings
from langchain_elasticsearch import ElasticsearchStore
from app.config import settings
import logging

logger = logging.getLogger(__name__)


class EmbeddingService:
    _instance = None
    _embeddings = None
    _vector_store = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def get_embeddings(self) -> OllamaEmbeddings:
        if self._embeddings is None:
            logger.info(f"初始化 Ollama Embedding 模型: {settings.OLLAMA_EMBEDDING_MODEL}")
            self._embeddings = OllamaEmbeddings(
                base_url=settings.OLLAMA_BASE_URL,
                model=settings.OLLAMA_EMBEDDING_MODEL
            )
        return self._embeddings

    def get_vector_store(self) -> ElasticsearchStore:
        if self._vector_store is None:
            logger.info(f"初始化 Elasticsearch 向量数据库: {settings.ES_HOST}:{settings.ES_PORT}")
            self._vector_store = ElasticsearchStore(
                es_url=f"http://{settings.ES_HOST}:{settings.ES_PORT}",
                es_user=settings.ES_USER if settings.ES_USER else None,
                es_password=settings.ES_PASSWORD if settings.ES_PASSWORD else None,
                index_name=settings.ES_INDEX_NAME,
                embedding=self.get_embeddings()
            )
        return self._vector_store

    async def embed_query(self, text: str) -> list[float]:
        embeddings = self.get_embeddings()
        return await embeddings.aembed_query(text)

    async def embed_documents(self, texts: list[str]) -> list[list[float]]:
        embeddings = self.get_embeddings()
        return await embeddings.aembed_documents(texts)

    async def add_texts(self, texts: list[str], metadatas: list[dict] | None = None) -> list[str]:
        vector_store = self.get_vector_store()
        return await vector_store.aadd_texts(texts=texts, metadatas=metadatas)

    async def similarity_search(self, query: str, k: int = 4) -> list:
        vector_store = self.get_vector_store()
        return await vector_store.asimilarity_search(query=query, k=k)


embedding_service = EmbeddingService()
