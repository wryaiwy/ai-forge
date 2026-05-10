package com.aiforge.ai.enums;

import lombok.Getter;

/**
 * AI 翻译目标语言枚举
 */
@Getter
public enum AiTranslateLangEnum {

    EN("en", "英语"),
    ZH("zh", "中文"),
    JA("ja", "日语"),
    KO("ko", "韩语"),
    FR("fr", "法语"),
    DE("de", "德语"),
    ES("es", "西班牙语"),
    ;

    private final String code;
    private final String desc;

    AiTranslateLangEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AiTranslateLangEnum getByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (AiTranslateLangEnum lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return null;
    }
}
