import asyncio
import copy
import logging
import time
from typing import List, Optional
from langchain_core.messages import AIMessage, ToolMessage
from langchain_mcp_adapters.client import MultiServerMCPClient
from app.config import settings

logger = logging.getLogger(__name__)

# Java 端 MCP Server 连接配置（携带内网零信任鉴权 Header）
JAVA_MCP_CONNECTIONS = {
    "aiforge_java": {
        "url": settings.JAVA_MCP_SSE_URL,
        "transport": "sse",
        "headers": {
            "X-Internal-Token": settings.AIFORGE_INTERNAL_TOKEN,
        },
    }
}

# 需要对大模型隐藏并由可信上下文强制注入的系统级参数名
INJECTED_CONTEXT_ARG = "userId"

# 需要强制覆写 userId 的敏感个人工具集合
USER_SCOPED_MCP_TOOLS = {"getWhoAmI", "getMyPublishedArticles"}


class JavaMCPService:
    """
    Java MCP 工具管理服务：负责连接配置、Schema 脱敏、TTL 本地缓存及安全调用（防越权注入 userId）
    """

    def __init__(self):
        self._tools_cache: List = []
        self._cache_expire_at: float = 0.0
        self._cache_lock = asyncio.Lock()

    @staticmethod
    def _strip_injected_args_from_schema(tools: list) -> list:
        """
        从发往 LLM 的 Tool JSON Schema 中彻底剥离 userId 等系统注入字段：
        1. 避免大模型产生幻觉瞎猜 userId；
        2. 节省每次对话的 Prompt Token；
        3. 底层 StructuredTool.coroutine 依然接收 **arguments，在执行期注入真实 userId。
        """
        sanitized_tools = []
        for tool in tools:
            schema = getattr(tool, "args_schema", None)
            if isinstance(schema, dict) and INJECTED_CONTEXT_ARG in schema.get("properties", {}):
                new_schema = copy.deepcopy(schema)
                new_schema["properties"].pop(INJECTED_CONTEXT_ARG, None)
                if "required" in new_schema and isinstance(new_schema["required"], list):
                    new_schema["required"] = [
                        r for r in new_schema["required"] if r != INJECTED_CONTEXT_ARG
                    ]
                tool.args_schema = new_schema
            sanitized_tools.append(tool)
        return sanitized_tools

    async def get_tools(self, force_refresh: bool = False) -> list:
        """
        从 Java MCP Server 获取工具列表（带 TTL 本地内存缓存 + 双重检查锁）：
        平时普通对话直接命中内存缓存（0 网络 IO 开销），仅在缓存过期或强制刷新时与 Java 通信。
        """
        now = time.monotonic()
        if not force_refresh and self._tools_cache and now < self._cache_expire_at:
            return self._tools_cache

        async with self._cache_lock:
            now = time.monotonic()
            if not force_refresh and self._tools_cache and now < self._cache_expire_at:
                return self._tools_cache
            try:
                mcp_client = MultiServerMCPClient(JAVA_MCP_CONNECTIONS)
                raw_tools = await mcp_client.get_tools()
                self._tools_cache = self._strip_injected_args_from_schema(raw_tools)
                self._cache_expire_at = now + settings.MCP_TOOLS_CACHE_TTL
                logger.info(
                    f"【MCP缓存刷新】成功加载并脱敏 {len(self._tools_cache)} 个 Java MCP 工具: "
                    f"{[t.name for t in self._tools_cache]}"
                )
                return self._tools_cache
            except Exception as e:
                logger.warning(f"连接 Java MCP Server 获取工具失败，将降级为普通对话: {e}")
                return self._tools_cache or []

    async def execute_tool_calls(
        self, ai_message: AIMessage, raw_user_id: Optional[str] = None
    ) -> List[ToolMessage]:
        """
        执行 AIMessage 中的所有 MCP 工具调用，并强制覆写 userId 防止大模型越界或被注入
        """
        trusted_user_id = (
            int(raw_user_id) if (raw_user_id and str(raw_user_id).isdigit()) else None
        )
        mcp_tools = await self.get_tools()
        tools_map = {t.name: t for t in mcp_tools}
        tool_messages: List[ToolMessage] = []

        for tool_call in getattr(ai_message, "tool_calls", []):
            tool_name = tool_call["name"]
            tool_args = dict(tool_call.get("args") or {})

            # 【防越权核心】强制使用可信上下文中的 user_id 覆盖模型传入的 userId
            if INJECTED_CONTEXT_ARG in tool_args or tool_name in USER_SCOPED_MCP_TOOLS:
                tool_args[INJECTED_CONTEXT_ARG] = trusted_user_id

            logger.info(f"【图节点-MCP调用】执行工具: {tool_name}, 参数: {tool_args}")
            target_tool = tools_map.get(tool_name)
            if target_tool is None:
                result_str = f"未找到对应的 MCP 工具: {tool_name}"
            else:
                try:
                    result = await target_tool.ainvoke(tool_args)
                    result_str = str(result)
                except Exception as e:
                    logger.error(f"MCP 工具 {tool_name} 调用失败: {e}")
                    result_str = f"工具调用异常: {e}"

            tool_messages.append(
                ToolMessage(
                    content=result_str,
                    name=tool_name,
                    tool_call_id=tool_call["id"],
                )
            )

        return tool_messages


mcp_service = JavaMCPService()

