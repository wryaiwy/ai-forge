package com.aiforge.common.exception;

import com.aiforge.common.result.Result;
import com.aiforge.common.result.ResultCodeEnum;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * @Author: wengrunyang
 * @Description: 全局异常拦截器
 * @DateTime: 2026/4/10 23:02
 **/
@Slf4j // Lombok 提供的日志注解
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    /**
     * 1. 处理我们自己定义的业务异常
     */
    @ExceptionHandler(AiForgeException.class)
    public Result<String> handleAiForgeException(AiForgeException e) {
        log.error("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 2. 处理所有不可预知的系统异常（兜底）
     * 比如 NullPointerException, IndexOutOfBoundsException 等
     */
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("系统内部异常: ", e); // 打印完整错误栈到控制台，方便排错
        return Result.fail(ResultCodeEnum.FAIL);
    }

    /**
     * 3. 专门处理静态资源找不到的异常 (比如 favicon.ico)
     * 避免在控制台打印恶心的长篇堆栈
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public Result<String> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException e) {
        // 只打印一行黄色的警告日志即可，甚至可以不打印
        log.warn("请求的静态资源不存在: {}", e.getResourcePath());
        return Result.fail(404, "请求的资源不存在");
    }
}
