package com.aiforge.biz.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SeckillPackageStatusEnum {
    /**
     * 下架
     */
    OFFLINE(0, "下架"),

    /**
     * 上架
     */
    ONLINE(1, "上架"),

    ;

    @EnumValue
    private final Integer code;
    private final String desc;

    SeckillPackageStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SeckillPackageStatusEnum value : SeckillPackageStatusEnum.values()) {
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
        for (SeckillPackageStatusEnum value : SeckillPackageStatusEnum.values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
