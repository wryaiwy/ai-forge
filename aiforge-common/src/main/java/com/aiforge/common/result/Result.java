package com.aiforge.common.result;

import lombok.Data;

/**
 * @Author: wengrunyang
 * @Description: 全局统一返回类
 * @DateTime: 2026/4/10 22:55
 **/
@Data
public class Result<T> {

    // 状态码
    private Integer code;
    // 提示信息
    private String message;
    // 真正响应的数据
    private T data;

    // 构造私有化，强制使用静态方法
    private Result() {}

    // 快速构建成功响应 (带数据)
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCodeEnum.SUCCESS.getCode());
        result.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    // 快速构建成功响应 (不带数据)
    public static <T> Result<T> success() {
        return success(null);
    }

    // 快速构建失败响应 (使用默认失败信息)
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(ResultCodeEnum.FAIL.getCode());
        result.setMessage(ResultCodeEnum.FAIL.getMessage());
        return result;
    }

    // 快速构建失败响应 (自定义错误码和信息)
    public static <T> Result<T> fail(ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    // 快速构建失败响应 (自定义状态码和信息) —— 专门给全局异常处理器用的！
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    // 快速构建失败响应 (只自定义信息)
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCodeEnum.FAIL.getCode());
        result.setMessage(message);
        return result;
    }

}
