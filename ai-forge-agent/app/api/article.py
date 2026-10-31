from fastapi import APIRouter, Depends
from fastapi.responses import StreamingResponse
from app.schemas.article import ArticlePolishRequest, ArticleTranslateRequest
from app.dependencies import get_article_graph
from agents.graph.article_graph import ArticleGraph

router = APIRouter(prefix="/article", tags=["文章AI服务"])


@router.post("/polish", summary="文章润色(LangGraph流式)")
async def polish_article(
    request: ArticlePolishRequest,
    graph: ArticleGraph = Depends(get_article_graph)
):
    """
    文章润色流式接口 (LangGraph 统一调度)
    """
    state = {
        "task_type": "polish",
        "content": request.content,
        "polish_mode": request.polish_mode,
        "target_style": request.target_style,
        "target_lang": None
    }
    return StreamingResponse(graph.run_stream(state), media_type="text/event-stream")


@router.post("/translate", summary="文章翻译(LangGraph流式)")
async def translate_article(
    request: ArticleTranslateRequest,
    graph: ArticleGraph = Depends(get_article_graph)
):
    """
    文章翻译流式接口 (LangGraph 统一调度)
    """
    state = {
        "task_type": "translate",
        "content": request.content,
        "polish_mode": None,
        "target_style": None,
        "target_lang": request.target_lang
    }
    return StreamingResponse(graph.run_stream(state), media_type="text/event-stream")
