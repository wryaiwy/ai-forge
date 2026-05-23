from typing import Generic, TypeVar, Optional
from pydantic import BaseModel

T = TypeVar('T')

class Result(BaseModel, Generic[T]):
    """
    通用返回结果封装类 (类似 Java 的 Result<T>)
    """
    code: int       # 状态码 (200 表示成功，其他表示错误)
    message: str    # 描述信息
    data: Optional[T] = None  # 泛型数据，可以是任何类型

    @staticmethod
    def success(data: Optional[T] = None, message: str = "操作成功") -> 'Result[T]':
        """成功返回"""
        return Result(code=200, message=message, data=data)

    @staticmethod
    def fail(code: int = 500, message: str = "操作失败") -> 'Result[T]':
        """失败返回"""
        return Result(code=code, message=message, data=None)
