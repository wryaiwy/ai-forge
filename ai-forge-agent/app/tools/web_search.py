import httpx
from bs4 import BeautifulSoup
from typing import Dict, Any, Literal
from pydantic import Field
import logging
import urllib.parse
from app.services.tools_registry import ToolsRegistry, BaseAgentTool

logger = logging.getLogger(__name__)

class WebSearchToolSchema(BaseAgentTool):
    execution_mode: Literal["HTTP", "MQ"] = "HTTP"
    query: str = Field(..., description="搜索关键词")

class WebSearchToolExecutor:
    """
    Task 1.3: WebSearchTool 的实际执行逻辑
    使用国内网络直连必应 (cn.bing.com) 进行网页搜索解析，无需任何代理或 Token。
    """

    # 搜索 URL 模板
    SEARCH_URL_TEMPLATE = "https://cn.bing.com/search?q={}"
    # 默认请求头
    DEFAULT_HEADERS = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36",
        "Accept-Language": "zh-CN,zh;q=0.9"
    }

    def __init__(self):
        pass

    def execute(self, arguments: Dict[str, Any]) -> str:
        query = arguments.get("query", "")
        if not query:
            return "WebSearchTool 错误：未提供 query 参数"
        
        try:
            logger.info(f"正在执行国内必应搜索 (cn.bing.com): {query}")
            url = self.SEARCH_URL_TEMPLATE.format(urllib.parse.quote(query))
            
            resp = httpx.get(url, headers=self.DEFAULT_HEADERS, timeout=10.0)
            resp.raise_for_status()
            
            soup = BeautifulSoup(resp.text, 'html.parser')
            results = []
            
            for li in soup.find_all('li', class_='b_algo'):
                h2 = li.find('h2')
                if not h2: continue
                title = h2.get_text(strip=True)
                
                # 寻找摘要
                p = li.find('p')
                desc = p.get_text(strip=True) if p else ""
                
                # Bing有时候把摘要放在其他的 div 里
                if not desc:
                    div = li.find('div', class_='b_caption')
                    if div:
                        desc = div.get_text(strip=True)
                
                results.append(f"【标题】: {title}\n【摘要】: {desc}")
            
            if not results:
                return "未找到相关搜索结果。"
            
            # 返回前 3 条结果
            return "\n\n".join(results[:3])
            
        except Exception as e:
            logger.error(f"WebSearchTool 真实搜索失败: {str(e)}")
            return f"WebSearchTool 搜索失败: {str(e)}"

# 注册 Tool
ToolsRegistry.register("web_search_tool", "搜索外网信息(同步)", WebSearchToolSchema)
