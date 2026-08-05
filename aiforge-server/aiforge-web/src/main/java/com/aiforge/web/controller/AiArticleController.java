package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiArticlePolishDTO;
import com.aiforge.ai.dto.AiArticleTranslateDTO;
import com.aiforge.ai.service.AiArticleService;
import com.aiforge.ai.vo.AiArticlePolishVO;
import com.aiforge.ai.vo.AiArticleTranslateVO;
import com.aiforge.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: AI 功能管理
 */
@Tag(name = "AI 功能")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiArticleController {

    private final AiArticleService aiArticleService;

    /**
     * 文章润色
     *
     * @param polishDTO 润色参数
     * @return 润色结果 (原始内容 + 润色后内容)
     */
    @Operation(summary = "文章润色")
    @PostMapping("/polish")
    public Result<AiArticlePolishVO> polish(@RequestBody @Valid AiArticlePolishDTO polishDTO) {
        return Result.success(aiArticleService.polish(polishDTO));
    }

    /**
     * 文章翻译
     *
     * @param translateDTO 翻译参数
     * @return 翻译结果 (原始内容 + 翻译后内容)
     */
    @Operation(summary = "文章翻译")
    @PostMapping("/translate")
    public Result<AiArticleTranslateVO> translate(@RequestBody @Valid AiArticleTranslateDTO translateDTO) {
        return Result.success(aiArticleService.translate(translateDTO));
    }
}
