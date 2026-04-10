package com.aiforge.common.result;

import lombok.Getter;

/**
 * @Author: wengrunyang
 * @Description: 状态码枚举类
 * @DateTime: 2026/4/10 22:57
 **/
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "操作成功"),

    FAIL(500, "系统异常，请稍后再试"),

    PARAM_ERROR(400, "参数异常"),

    UNAUTHORIZED(401, "未登录或Token失效"),

    FORBIDDEN(403, "没有权限访问");

    private final Integer code;
    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}