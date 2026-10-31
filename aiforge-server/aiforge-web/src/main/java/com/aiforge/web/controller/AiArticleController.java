package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiArticlePolishDTO;
import com.aiforge.ai.dto.AiArticleTranslateDTO;
import com.aiforge.ai.service.AiArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @Description: AI 功能管理
 */
@Tag(name = "AI 功能")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class AiArticleController {

    private final AiArticleService aiArticleService;

    /**
     * 文章润色 (流式输出)
     *
     * @param polishDTO 润色参数
     * @return 润色结果 (原始内容 + 润色后内容)
     */
    @Operation(summary = "文章润色")
    @PostMapping(value = "/ai/polish", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> polish(@RequestBody @Valid AiArticlePolishDTO polishDTO) {
        return aiArticleService.polish(polishDTO);
    }

    /**
     * 文章翻译 (流式输出)
     *
     * @param translateDTO 翻译参数
     * @return 翻译结果 (原始内容 + 翻译后内容)
     */
    @Operation(summary = "文章翻译")
    @PostMapping(value = "/ai/translate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> translate(@RequestBody @Valid AiArticleTranslateDTO translateDTO) {
        return aiArticleService.translate(translateDTO);
    }
}
