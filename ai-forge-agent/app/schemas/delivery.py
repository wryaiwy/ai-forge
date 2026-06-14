from pydantic import BaseModel, Field

class DeliveryRequest(BaseModel):
    """交付与入库请求"""
    title: str = Field(description="文章总标题")
    draft_content: str = Field(description="已生成的完整或分段正文")
    user_id: int = Field(description="用户ID")

class DeliveryResponse(BaseModel):
    """交付响应"""
    success: bool = Field(description="是否最终入库成功")
    final_content: str = Field(description="最终成功入库的正文内容（如果触发了异常自愈，可能与入参不同）")
    attempts: int = Field(description="尝试入库的总次数")
    message: str = Field(description="详细状态信息")
