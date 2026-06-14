from typing import List, Optional
from pydantic import BaseModel, Field

class PlanRequest(BaseModel):
    """大纲生成请求"""
    requirement: str = Field(description="用户模糊或具体的代写需求", min_length=1)

class SectionPlan(BaseModel):
    """段落大纲"""
    title: str = Field(description="段落标题")
    content_points: List[str] = Field(description="段落核心要点提示")
    word_count_estimate: int = Field(description="预计字数")

class TaskPlanResponse(BaseModel):
    """结构化大纲响应"""
    topic: str = Field(description="文章主题")
    audience: str = Field(description="目标受众")
    tone: str = Field(description="文章基调 (如：专业、活泼等)")
    total_word_count: int = Field(description="总预计字数")
    sections: List[SectionPlan] = Field(description="文章段落划分")
