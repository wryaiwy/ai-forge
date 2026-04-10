package com.aiforge.common.exception;

import com.aiforge.common.result.ResultCodeEnum;
import lombok.Getter;

/**
 * @Author: wengrunyang
 * @Description: 自定义业务异常类
 * @DateTime: 2026/4/10 23:01
 **/
@Getter
public class AiForgeException extends RuntimeException {

    private final Integer code;

    public AiForgeException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    public AiForgeException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
