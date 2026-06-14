from pydantic import BaseModel, Field
from typing import List

class CleanRequest(BaseModel):
    """提纯请求"""
    topic: str = Field(description="文章总主题")
    section_title: str = Field(description="当前段落标题")
    content_points: List[str] = Field(description="段落核心要点提示")

class CleanResponse(BaseModel):
    """提纯响应"""
    cleaned_context: str = Field(description="清洗提纯后的参考信息")
    source_query: str = Field(description="使用的搜索词")
