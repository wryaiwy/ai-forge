package com.aiforge.ai.enums;

import lombok.Getter;

/**
 * AI 润色模式枚举
 */
@Getter
public enum AiPolishModeEnum {

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

    AiPolishModeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AiPolishModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AiPolishModeEnum mode : values()) {
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
