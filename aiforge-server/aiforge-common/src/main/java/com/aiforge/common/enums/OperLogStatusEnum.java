package com.aiforge.common.enums;

import lombok.Getter;

/**
 * 操作日志状态
 */
@Getter
public enum OperLogStatusEnum {

    SUCCESS(0, "成功"),
    FAIL(1, "失败");

    private final Integer code;
    private final String desc;

    OperLogStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperLogStatusEnum value : OperLogStatusEnum.values()) {
            if (value.code.equals(code)) {
                return value.desc;
            }
        }
        return null;
    }
}
