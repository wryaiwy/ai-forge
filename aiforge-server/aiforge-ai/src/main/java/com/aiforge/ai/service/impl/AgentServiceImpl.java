package com.aiforge.ai.service.impl;

import com.aiforge.ai.dto.RagQueryDTO;
import com.aiforge.ai.service.AgentService;
import com.aiforge.common.annotation.AiMonitored;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: Agent 服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
@AiMonitored("Python Agent 服务")
public class AgentServiceImpl implements AgentService {

    private final WebClient agentWebClient;

    /**
     * 生成文章流式摘要
     */
    @Override
    public Flux<String> summarizeArticleStream(String content) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("content", content);

            return agentWebClient.post()
                    .uri("/agent/summary/stream")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class);
        } catch (Exception e) {
            log.error("调用 Agent 流式摘要接口失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "生成流式摘要失败，请稍后重试");
        }
    }

    /**
     * 知识库流式查询
     */
    @Override
    public Flux<String> ragQueryStream(RagQueryDTO ragQueryDTO) {
        try {
            return agentWebClient.post()
                    .uri("/agent/rag/stream")
                    .bodyValue(ragQueryDTO)
                    .retrieve()
                    .bodyToFlux(String.class);
        } catch (Exception e) {
            log.error("调用 Agent RAG 流式查询接口失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI 知识库流式查询失败，请稍后重试");
        }
    }

}
