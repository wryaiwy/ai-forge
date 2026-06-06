package com.aiforge.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * @Author: wengrunyang
 * @Description: 业务类型枚举
 * @DateTime: 2026/6/6 15:43
 **/
@Getter
public enum BizTypeEnum {

    /**
     * 博客文章
     */
    ARTICLE("article", "博客文章"),


    ;

    @EnumValue
    private final String code;
    private final String desc;

    BizTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static BizTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (BizTypeEnum value : BizTypeEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getDescByCode(String code) {
        BizTypeEnum bizType = getByCode(code);
        return bizType != null ? bizType.desc : null;
    }

    public static boolean existsByCode(String code) {
        return getByCode(code) != null;
    }
}
