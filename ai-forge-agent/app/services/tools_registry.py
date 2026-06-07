from pydantic import BaseModel, Field
from typing import Type, Dict, Any, Literal
import logging

logger = logging.getLogger(__name__)

class BaseAgentTool(BaseModel):
    """
    Python 侧 Tool 定义基类
    用于生成大模型 Function Calling 所需的 JSON Schema
    """
    # 区分执行模式：同步HTTP 或 异步MQ
    execution_mode: Literal["HTTP", "MQ"] = "HTTP"

    @classmethod
    def get_tool_schema(cls, name: str, description: str) -> dict:
        """
        生成 LangChain 兼容的 Tool Schema
        """
        return {
            "type": "function",
            "function": {
                "name": name,
                "description": description,
                "parameters": cls.model_json_schema()
            }
        }

class ToolsRegistry:
    """工具注册中心"""
    _registry: Dict[str, dict] = {}
    _mode_registry: Dict[str, str] = {} # 记录工具的执行模式
    
    @classmethod
    def register(cls, name: str, description: str, schema_class: Type[BaseAgentTool]):
        """注册一个工具 Schema"""
        cls._registry[name] = schema_class.get_tool_schema(name, description)
        
        # 提取执行模式并保存，默认为 HTTP
        mode = schema_class.model_fields['execution_mode'].default if 'execution_mode' in schema_class.model_fields else "HTTP"
        cls._mode_registry[name] = mode
        
        logger.info(f"Registered tool schema: {name} (Mode: {mode})")
        
    @classmethod
    def get_all_schemas(cls) -> list:
        """获取所有已注册的工具 Schema 列表，供 LLM 绑定"""
        return list(cls._registry.values())

    @classmethod
    def get_schema(cls, name: str) -> dict:
        return cls._registry.get(name)

    @classmethod
    def get_execution_mode(cls, name: str) -> str:
        """获取对应工具的执行模式 (HTTP / MQ)"""
        return cls._mode_registry.get(name, "HTTP")


