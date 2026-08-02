package com.aiforge.ai.service;

import com.aiforge.ai.dto.RagQueryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Flux;

/**
 * @Description: Agent 服务接口
 */
public interface AgentService {

    /**
     * 生成文章流式摘要
     * @param content 文章内容
     * @return 流式部分内容
     */
    Flux<String> summarizeArticleStream(String content);

    /**
     * 知识库流式查询
     * @param ragQueryDTO 知识库查询消息DTO参数
     * @return 流式回答内容
     */
    Flux<String> ragQueryStream(RagQueryDTO ragQueryDTO);
}
