package com.aiforge.biz.enums;

import lombok.Getter;

@Getter
public enum SeckillLuaResultEnum {
    SUCCESS(0L, "抢购成功"),
    STOCK_NOT_ENOUGH(1L, "库存不足"),
    ALREADY_PURCHASED(2L, "已购买过"),
    ;

    private final Long code;
    private final String desc;

    SeckillLuaResultEnum(Long code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SeckillLuaResultEnum getByCode(Long code) {
        if (code == null) {
            return null;
        }
        for (SeckillLuaResultEnum value : SeckillLuaResultEnum.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
