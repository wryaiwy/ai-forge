package com.aiforge.common.enums;

import lombok.Getter;

/**
 * 操作日志业务类型
 */
@Getter
public enum OperBusinessTypeEnum {

    OTHER(0, "其它"),
    INSERT(1, "新增"),
    UPDATE(2, "修改"),
    DELETE(3, "删除");

    private final Integer code;
    private final String desc;

    OperBusinessTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperBusinessTypeEnum value : OperBusinessTypeEnum.values()) {
            if (value.code.equals(code)) {
                return value.desc;
            }
        }
        return null;
    }
}
