package com.aiforge.biz.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ArticleStatusEnum {
    /**
     * 已发布
     */
    PUBLISHED(1, "已发布"),

    /**
     * 已下架
     */
    OFFLINE(2, "已下架"),

    /**
     * 草稿
     */
    DRAFT(3, "草稿"),

    ;

    @EnumValue
    private final Integer code;
    private final String desc;

    ArticleStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ArticleStatusEnum value : ArticleStatusEnum.values()) {
            if (value.code.equals(code)) {
                return value.desc;
            }
        }
        return null;
    }

    public static boolean existsByCode(Integer code) {
        if (code == null) {
            return false;
        }
        for (ArticleStatusEnum value : ArticleStatusEnum.values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}