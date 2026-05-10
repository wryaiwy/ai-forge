package com.aiforge.web.controller;

import com.aiforge.ai.dto.AiPolishDTO;
import com.aiforge.ai.dto.AiTranslateDTO;
import com.aiforge.ai.service.AiService;
import com.aiforge.ai.vo.AiPolishVO;
import com.aiforge.ai.vo.AiTranslateVO;
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
public class AiController {

    private final AiService aiService;

    /**
     * 文章润色
     *
     * @param polishDTO 润色参数
     * @return 润色结果 (原始内容 + 润色后内容)
     */
    @Operation(summary = "文章润色")
    @PostMapping("/polish")
    public Result<AiPolishVO> polish(@RequestBody @Valid AiPolishDTO polishDTO) {
        return Result.success(aiService.polish(polishDTO));
    }

    /**
     * 文章翻译
     *
     * @param translateDTO 翻译参数
     * @return 翻译结果 (原始内容 + 翻译后内容)
     */
    @Operation(summary = "文章翻译")
    @PostMapping("/translate")
    public Result<AiTranslateVO> translate(@RequestBody @Valid AiTranslateDTO translateDTO) {
        return Result.success(aiService.translate(translateDTO));
    }
}
