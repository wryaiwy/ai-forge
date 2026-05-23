from pydantic import BaseModel, Field

class SummaryRequest(BaseModel):
    content: str = Field(..., description="需要生成摘要的文章内容")

class SummaryResponse(BaseModel):
    summary: str = Field(..., description="生成的摘要内容")
