package com.aiforge.biz.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum SeckillOrderStatusEnum {
    /**
     * 未支付
     */
    UNPAID(0, "未支付"),

    /**
     * 已支付
     */
    PAID(1, "已支付"),

    /**
     * 已取消
     */
    CANCELLED(2, "已取消"),

    ;

    @EnumValue
    private final Integer code;
    private final String desc;

    SeckillOrderStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SeckillOrderStatusEnum value : SeckillOrderStatusEnum.values()) {
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
        for (SeckillOrderStatusEnum value : SeckillOrderStatusEnum.values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
