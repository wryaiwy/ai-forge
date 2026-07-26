import httpx
from bs4 import BeautifulSoup
from typing import Any, ClassVar, List
from pydantic import Field
import logging
import urllib.parse
import re
from app.tools.core import ToolsRegistry, BaseAgentTool

logger = logging.getLogger(__name__)


class WebSearchToolSchema(BaseAgentTool):
    """
    网络搜索工具
    采用多引擎容灾自动降级机制（搜狗 -> 360搜索 -> DuckDuckGo -> 必应），直连抓取外网资讯，无需代理或 Token。
    """
    query: str = Field(..., description="搜索关键词")

    # 默认请求头
    DEFAULT_HEADERS: ClassVar[dict] = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language": "zh-CN,zh;q=0.9,en;q=0.8"
    }

    def _clean_query(self, raw_query: str) -> str:
        """规范化和精简搜索关键词"""
        q = raw_query.strip(" '\"“”‘’\t\r\n")
        # 移除常见的会话指令词，提升搜索引擎命中率
        for prefix in ["写一篇", "帮我写一篇", "帮我写", "请写一篇", "请写", "关于", "文章", "的优缺点", "有哪些"]:
            if q.startswith(prefix) and len(q) > len(prefix) + 2:
                q = q[len(prefix):].strip(" '\"“”‘’:-_")
            if q.endswith(prefix) and len(q) > len(prefix) + 2:
                q = q[:-len(prefix)].strip(" '\"“”‘’:-_")
        return q or raw_query

    async def _search_sogou(self, client: httpx.AsyncClient, query: str) -> List[str]:
        url = f"https://www.sogou.com/web?query={urllib.parse.quote(query)}"
        resp = await client.get(url, headers=self.DEFAULT_HEADERS, timeout=7.0)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, 'html.parser')
        results = []
        for div in soup.find_all('div', class_='vrwrap'):
            h3 = div.find('h3')
            if not h3: continue
            title = h3.get_text(strip=True)
            p = div.find('p', class_='star-wiki') or div.find('div', class_='text-layout') or div.find('p', class_='str_info') or div
            desc = p.get_text(strip=True)
            if title and desc and title != desc:
                results.append(f"【标题】: {title}\n【摘要】: {desc[:200]}")
        return results

    async def _search_so(self, client: httpx.AsyncClient, query: str) -> List[str]:
        url = f"https://www.so.com/s?q={urllib.parse.quote(query)}"
        resp = await client.get(url, headers=self.DEFAULT_HEADERS, timeout=7.0)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, 'html.parser')
        results = []
        for li in soup.find_all('li', class_='res-list'):
            h3 = li.find('h3')
            if not h3: continue
            title = h3.get_text(strip=True)
            p = li.find('p', class_='res-desc') or li.find('div', class_='res-desc') or li
            desc = p.get_text(strip=True)
            if title and desc and title != desc:
                results.append(f"【标题】: {title}\n【摘要】: {desc[:200]}")
        return results

    async def _search_ddg(self, client: httpx.AsyncClient, query: str) -> List[str]:
        url = f"https://html.duckduckgo.com/html/?q={urllib.parse.quote(query)}"
        resp = await client.get(url, headers=self.DEFAULT_HEADERS, timeout=7.0)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, 'html.parser')
        results = []
        for div in soup.find_all('div', class_='result'):
            a = div.find('a', class_='result__url') or div.find('a', class_='result__title')
            title = a.get_text(strip=True) if a else ""
            snippet = div.find('a', class_='result__snippet') or div.find('div', class_='result__snippet')
            desc = snippet.get_text(strip=True) if snippet else ""
            if title and desc:
                results.append(f"【标题】: {title}\n【摘要】: {desc[:200]}")
        return results

    async def _search_bing(self, client: httpx.AsyncClient, query: str) -> List[str]:
        url = f"https://www.bing.com/search?q={urllib.parse.quote(query)}"
        resp = await client.get(url, headers=self.DEFAULT_HEADERS, timeout=7.0)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, 'html.parser')
        results = []
        for li in soup.find_all('li', class_='b_algo'):
            h2 = li.find('h2')
            if not h2: continue
            title = h2.get_text(strip=True)
            p = li.find('p') or li.find('div', class_='b_caption') or li
            desc = p.get_text(strip=True) if p else ""
            if title and desc:
                results.append(f"【标题】: {title}\n【摘要】: {desc[:200]}")
        return results

    async def execute(self, user_token: str = "", **kwargs) -> Any:
        if not self.query:
            return "WebSearchTool 错误：未提供 query 参数"

        clean_query = self._clean_query(self.query)
        logger.info(f"正在执行网络搜索，原始词: 【{self.query}】，精简关键词: 【{clean_query}】")

        search_engines = [
            ("搜狗搜索 (Sogou)", self._search_sogou),
            ("360搜索 (So.com)", self._search_so),
            ("DuckDuckGo", self._search_ddg),
            ("必应搜索 (Bing)", self._search_bing)
        ]

        # 禁用系统环境代理及环境变量(trust_env=False, proxy=None)，开启自动重定向，防止被本地安全软件或 VPN 拦截导致连接超时
        async with httpx.AsyncClient(proxy=None, trust_env=False, follow_redirects=True) as client:
            for engine_name, search_func in search_engines:
                try:
                    logger.info(f"正在尝试使用 [{engine_name}] 检索: {clean_query}")
                    results = await search_func(client, clean_query)
                    if results and len(results) > 0:
                        logger.info(f"[{engine_name}] 成功获取 {len(results)} 条相关网页结果。")
                        return "\n\n".join(results[:4])
                except Exception as e:
                    err_desc = f"{type(e).__name__}: {str(e)}" if str(e) else type(e).__name__
                    logger.warning(f"[{engine_name}] 搜索未达到预期或网络异常: {err_desc}，正在自动降级尝试下一引擎...")

        logger.error(f"全部联网搜索引擎均尝试完毕，未找到有效结果: {clean_query}")
        return "未找到相关搜索结果。"


# 注册 Tool
ToolsRegistry.register("web_search_tool", "搜索外网信息(同步)", WebSearchToolSchema)
