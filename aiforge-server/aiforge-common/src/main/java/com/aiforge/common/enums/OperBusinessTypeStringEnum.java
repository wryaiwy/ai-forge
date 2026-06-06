package com.aiforge.common.enums;

import lombok.Getter;

/**
 * @Author: wengrunyang
 * @Description: 操作日志业务类型--String类型
 * @DateTime: 2026/6/6 16:27
 **/
@Getter
public enum OperBusinessTypeStringEnum {

    OTHER("0", "其它"),
    INSERT("1", "新增"),
    UPDATE("2", "修改"),
    DELETE("3", "删除");

    private final String code;
    private final String desc;

    OperBusinessTypeStringEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(String code) {
        if (code == null) {
            return null;
        }
        for (OperBusinessTypeStringEnum value : OperBusinessTypeStringEnum.values()) {
            if (value.code.equals(code)) {
                return value.desc;
            }
        }
        return null;
    }
}
