from pydantic import BaseModel, Field
from typing import List, Optional

class DraftRequest(BaseModel):
    """起草请求"""
    topic: str = Field(description="文章总主题")
    section_title: str = Field(description="当前段落标题")
    content_points: List[str] = Field(description="段落核心要点提示")
    cleaned_context: str = Field(description="提纯后的参考资料上下文")
    word_count_estimate: int = Field(description="预计字数")

class CriticFeedback(BaseModel):
    """Critic 反思结果"""
    passed: bool = Field(description="是否通过了内容质量审查（无跑题、包含所有要点）")
    feedback: str = Field(description="审查反馈或修改建议")

class DraftResponse(BaseModel):
    """起草与反思响应"""
    draft_content: str = Field(description="最终生成的段落内容")
    critic_feedback: str = Field(description="Critic 的最终反馈建议")
    passed: bool = Field(description="是否最终通过审查")
    attempts: int = Field(description="重试次数")
