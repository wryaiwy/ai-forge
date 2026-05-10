package com.aiforge.ai.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description: AI 翻译结果 VO
 */
@Data
public class AiTranslateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String originalContent;

    private String translatedContent;

    private String targetLangDesc;
}
