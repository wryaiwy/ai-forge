package com.aiforge.ai.enums;

import lombok.Getter;

/**
 * @Description: AI 润色模式枚举
 */
@Getter
public enum AiArticlePolishModeEnum {

    /**
     * 语法修正
     */
    GRAMMAR_FIX(1, "语法修正"),

    /**
     * 专业润色
     */
    PROFESSIONAL_POLISH(2, "专业润色"),

    /**
     * 风格转换
     */
    STYLE_TRANSFER(3, "风格转换"),

    ;

    private final Integer code;
    private final String desc;

    AiArticlePolishModeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AiArticlePolishModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AiArticlePolishModeEnum mode : values()) {
            if (mode.code.equals(code)) {
                return mode;
            }
        }
        return null;
    }

    public static boolean existsByCode(Integer code) {
        return getByCode(code) != null;
    }
}
