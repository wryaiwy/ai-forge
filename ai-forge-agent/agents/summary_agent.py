import asyncio
from typing import List, AsyncGenerator
from langchain_text_splitters import RecursiveCharacterTextSplitter
from langchain_core.prompts import ChatPromptTemplate
from langchain_core.output_parsers import StrOutputParser
from agents.base_agent import BaseAgent

class SummaryAgent(BaseAgent):
    """
    文章一键摘要 Agent (支持超长文本 Map-Reduce & 流式输出)
    """
    def __init__(self):
        super().__init__(temperature=0.3)
        self.llm = self._llm_provider.get_chat_model()

    async def run(self, request) -> dict:
        content = request.content
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=4000, 
            chunk_overlap=200,
            length_function=len
        )
        texts = text_splitter.split_text(content)
        
        # TODO: 简历亮点 - 这里实现了一个 Map-Reduce 的大模型处理架构，
        # 通过将超长文章进行切片（Chunking），并利用 asyncio 并发调用 LLM（Map 阶段），
        # 最后再交由大模型进行汇总（Reduce 阶段），完美突破了 deepseek-v4-flash 等大模型的 Context Token 上下文限制！
        
        if len(texts) == 1:
            summary = await self._stuff_summarize(texts[0])
        else:
            summary = await self._map_reduce_summarize(texts)
            
        return {"summary": summary}

    async def run_stream(self, request) -> AsyncGenerator[str, None]:
        content = request.content
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=4000, 
            chunk_overlap=200,
            length_function=len
        )
        texts = text_splitter.split_text(content)
        
        # TODO: 简历亮点 - 超长文本流式输出 Map-Reduce。在 Map 阶段进行异步并发提炼，
        # 并实时向前端推送状态事件进行用户体验优化；在 Reduce 融合阶段，使用 astream() 进行真正的 Token 级流式推送。
        
        if len(texts) == 1:
            prompt = ChatPromptTemplate.from_template(
                "你是一个专业的内容编辑。请为以下文章生成一份精简、通顺的摘要，保留核心观点与细节，字数控制在 200 字左右。\n\n【文章内容】:\n{text}\n\n【摘要】:"
            )
            chain = prompt | self.llm | StrOutputParser()
            async for chunk in chain.astream({"text": texts[0]}):
                # SSE 格式：以 data: 开头，并以 \n\n 结尾
                data_str = chunk.replace("\n", "\ndata: ")
                yield f"data: {data_str}\n\n"
        else:
            yield "data: 【系统提示】正在对文章进行分块分析（Map阶段）...\n\n"
            
            map_prompt = ChatPromptTemplate.from_template(
                "你是一个阅读助手。请提炼以下段落的核心要点，用一句话总结。\n\n【段落内容】:\n{text}\n\n【要点总结】:"
            )
            map_chain = map_prompt | self.llm | StrOutputParser()
            
            # 并发执行 Map 任务
            tasks = [map_chain.ainvoke({"text": chunk}) for chunk in texts]
            sub_summaries = await asyncio.gather(*tasks)
            
            yield "data: 【系统提示】分块提炼完成，正在融合要点生成最终流式摘要（Reduce阶段）...\n\n"
            
            combined_text = "\n".join([f"- {s}" for s in sub_summaries])
            reduce_prompt = ChatPromptTemplate.from_template(
                "你是一个总编辑。以下是一篇长文各段落的要点汇总，请将它们融合成一篇通顺、连贯、逻辑清晰的整篇“一键摘要”，字数在 300 字左右。\n\n【各段要点】:\n{combined_text}\n\n【整篇摘要】:"
            )
            reduce_chain = reduce_prompt | self.llm | StrOutputParser()
            async for chunk in reduce_chain.astream({"combined_text": combined_text}):
                data_str = chunk.replace("\n", "\ndata: ")
                yield f"data: {data_str}\n\n"

    async def _stuff_summarize(self, text: str) -> str:
        prompt = ChatPromptTemplate.from_template(
            "你是一个专业的内容编辑。请为以下文章生成一份精简、通顺的摘要，保留核心观点与细节，字数控制在 200 字左右。\n\n【文章内容】:\n{text}\n\n【摘要】:"
        )
        chain = prompt | self.llm | StrOutputParser()
        return await chain.ainvoke({"text": text})

    async def _map_reduce_summarize(self, chunks: List[str]) -> str:
        map_prompt = ChatPromptTemplate.from_template(
            "你是一个阅读助手。请提炼以下段落的核心要点，用一句话总结。\n\n【段落内容】:\n{text}\n\n【要点总结】:"
        )
        map_chain = map_prompt | self.llm | StrOutputParser()
        
        tasks = [map_chain.ainvoke({"text": chunk}) for chunk in chunks]
        sub_summaries = await asyncio.gather(*tasks)
        
        combined_text = "\n".join([f"- {s}" for s in sub_summaries])
        
        reduce_prompt = ChatPromptTemplate.from_template(
            "你是一个总编辑。以下是一篇长文各段落的要点汇总，请将它们融合成一篇通顺、连贯、逻辑清晰的整篇“一键摘要”，字数在 300 字左右。\n\n【各段要点】:\n{combined_text}\n\n【整篇摘要】:"
        )
        reduce_chain = reduce_prompt | self.llm | StrOutputParser()
        return await reduce_chain.ainvoke({"combined_text": combined_text})
