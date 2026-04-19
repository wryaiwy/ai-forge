from fastapi import HTTPException, status
from typing import Any, Optional

class AppException(HTTPException):
    """
    自定义业务异常基类
    用于在业务逻辑中主动抛出错误（如：参数错误、Agent 执行失败等）
    """
    def __init__(
        self,
        status_code: int = status.HTTP_400_BAD_REQUEST,
        detail: str = "Business Error",
        error_code: Optional[int] = None, # 自定义错误码，方便 Java 区分错误类型
        headers: Optional[dict[str, Any]] = None,
    ):
        # 构造统一的 JSON 结构
        content = {
            "code": error_code or status_code, # 优先使用自定义错误码
            "message": detail,
            "data": None # 预留字段
        }
        super().__init__(status_code=status_code, detail=content, headers=headers)

# 具体的业务异常示例
class AgentExecutionError(AppException):
    def __init__(self, detail: str = "Agent 执行出错"):
        super().__init__(status_code=500, detail=detail, error_code=1001)

class ResourceNotFoundError(AppException):
    def __init__(self, detail: str = "资源未找到"):
        super().__init__(status_code=404, detail=detail, error_code=1002)