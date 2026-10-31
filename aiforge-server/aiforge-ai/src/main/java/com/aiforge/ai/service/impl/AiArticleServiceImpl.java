package com.aiforge.ai.service.impl;

import com.aiforge.ai.dto.AiArticlePolishDTO;
import com.aiforge.ai.dto.AiArticleTranslateDTO;
import com.aiforge.ai.service.AiArticleService;
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
 * @Description: AI 服务实现类 (Python Agent 代理调用)
 */
@Service
@Slf4j
@RequiredArgsConstructor
@AiMonitored("Python Agent 服务")
public class AiArticleServiceImpl implements AiArticleService {

    private final WebClient agentWebClient;

    /**
     * 文章润色
     */
    @Override
    public Flux<String> polish(AiArticlePolishDTO polishDTO) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("content", polishDTO.getContent());
            requestBody.put("polish_mode", polishDTO.getPolishMode());
            requestBody.put("target_style", polishDTO.getTargetStyle());

            return agentWebClient.post()
                    .uri("/agent/article/polish")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnError(e -> log.error("调用 Agent 文章润色流式接口失败: {}", e.getMessage(), e));
        } catch (Exception e) {
            log.error("AI 润色服务调用失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI 润色服务调用失败，请稍后重试");
        }
    }

    /**
     * 文章翻译
     */
    @Override
    public Flux<String> translate(AiArticleTranslateDTO translateDTO) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("content", translateDTO.getContent());
            requestBody.put("target_lang", translateDTO.getTargetLang());

            return agentWebClient.post()
                    .uri("/agent/article/translate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .doOnError(e -> log.error("调用 Agent 文章翻译流式接口失败: {}", e.getMessage(), e));
        } catch (Exception e) {
            log.error("AI 翻译服务调用失败: {}", e.getMessage(), e);
            throw new AiForgeException(ResultCodeEnum.FAIL.getCode(), "AI 翻译服务调用失败，请稍后重试");
        }
    }
}
