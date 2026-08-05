package com.aiforge.ai.enums;

import lombok.Getter;

/**
 * @Description: AI 翻译目标语言枚举
 */
@Getter
public enum AiArticleTranslateLangEnum {

    /**
     * 英语
     */
    EN("en", "英语"),

    /**
     * 中文
     */
    ZH("zh", "中文"),
    
    /**
     * 日语
     */
    JA("ja", "日语"),
    
    /**
     * 韩语
     */
    KO("ko", "韩语"),
    
    /**
     * 法语
     */
    FR("fr", "法语"),
    
    /**
     * 德语
     */
    DE("de", "德语"),
    
    /**
     * 西班牙语
     */
    ES("es", "西班牙语"),
    ;

    private final String code;
    private final String desc;

    AiArticleTranslateLangEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static AiArticleTranslateLangEnum getByCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (AiArticleTranslateLangEnum lang : values()) {
            if (lang.code.equalsIgnoreCase(code)) {
                return lang;
            }
        }
        return null;
    }
}
