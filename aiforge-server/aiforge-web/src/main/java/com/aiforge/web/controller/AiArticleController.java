package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiArticlePolishDTO;
import com.aiforge.ai.dto.AiArticleTranslateDTO;
import com.aiforge.ai.service.AiArticleService;
import com.aiforge.biz.service.BizArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * @Description: 文章 AI 功能管理
 */
@Tag(name = "文章 AI 功能")
@RestController
@RequestMapping("/ai/article")
@RequiredArgsConstructor
public class AiArticleController {

    private final AiArticleService aiArticleService;
    private final BizArticleService bizArticleService;

    /**
     * 文章润色 (流式输出)
     *
     * @param polishDTO 润色参数
     * @return 润色结果 (原始内容 + 润色后内容)
     */
    @Operation(summary = "文章润色")
    @PostMapping(value = "/polish", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
    @PostMapping(value = "/translate", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> translate(@RequestBody @Valid AiArticleTranslateDTO translateDTO) {
        return aiArticleService.translate(translateDTO);
    }

    /**
     * 生成整篇文章的一键摘要（流式输出）
     *
     * @param articleId 文章ID
     * @return 摘要流内容
     */
    @Operation(summary = "生成文章一键摘要（流式输出）")
    @GetMapping(value = "/summary/{articleId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateSummary(@PathVariable Long articleId) {
        return bizArticleService.generateSummaryStream(articleId);
    }

    /**
     * 生成文章知识问答（流式输出）
     *
     * @param articleId 文章ID
     * @param question 提问问题
     * @return 问答流内容
     */
    @Operation(summary = "生成文章知识问答（流式输出）")
    @GetMapping(value = "/qa/{articleId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> generateQA(@PathVariable Long articleId, @RequestParam String question) {
        return bizArticleService.generateQAStream(articleId, question);
    }
}
