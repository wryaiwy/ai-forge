package com.aiforge.ai.service;

import com.aiforge.ai.dto.AiArticlePolishDTO;
import com.aiforge.ai.dto.AiArticleTranslateDTO;
import reactor.core.publisher.Flux;

/**
 * @Description: AI 服务接口
 */
public interface AiArticleService {

    /**
     * 文章润色 (流式)
     *
     * @param polishDTO 润色请求参数
     * @return 润色结果 (含原始内容和润色后内容)
     */
    Flux<String> polish(AiArticlePolishDTO polishDTO);

    /**
     * 文章翻译 (流式)
     *
     * @param translateDTO 翻译请求参数
     * @return 翻译结果 (含原始内容和翻译后内容)
     */
    Flux<String> translate(AiArticleTranslateDTO translateDTO);
}
