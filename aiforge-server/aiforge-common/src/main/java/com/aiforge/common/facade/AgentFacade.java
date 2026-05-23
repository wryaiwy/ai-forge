package com.aiforge.common.facade;

import reactor.core.publisher.Flux;

/**
 * @Description: AI Agent 服务门面接口（供其他模块调用，由 ai 模块实现）
 */
public interface AgentFacade {

    /**
     * 生成文章流式摘要
     * @param content 文章内容
     * @return 流式摘要内容
     */
    Flux<String> summarizeArticleStream(String content);
}
