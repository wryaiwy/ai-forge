package com.aiforge.ai.facade.impl;

import com.aiforge.ai.dto.RagQueryDTO;
import com.aiforge.ai.service.AgentService;
import com.aiforge.common.facade.AgentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @Description: AgentFacade 实现类，桥接 common 的 AgentFacade 接口到 ai 模块的 AgentService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentFacadeImpl implements AgentFacade {

    private final AgentService agentService;

    /**
     * 生成文章流式摘要
     */
    @Override
    public Flux<String> summarizeArticleStream(String content) {
        return agentService.summarizeArticleStream(content);
    }

    /**
     * 知识问答流式回答
     */
    @Override
    public Flux<String> answerKnowledgeStream(String bizId, String bizType, String question) {
        RagQueryDTO queryDTO = new RagQueryDTO();
        queryDTO.setQuery(question);
        queryDTO.setBizId(bizId);
        queryDTO.setBizType(bizType);
        return agentService.ragQueryStream(queryDTO);
    }
}
