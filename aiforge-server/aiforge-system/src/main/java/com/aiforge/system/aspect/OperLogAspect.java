package com.aiforge.system.aspect;

import com.aiforge.common.annotation.OperationLog;
import com.aiforge.common.enums.OperLogStatusEnum;
import com.aiforge.common.utils.SecurityUtils;
import com.aiforge.system.entity.SysOperLog;
import com.aiforge.system.service.SysOperLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 操作审计切面：将标注了 {@link OperationLog} 的方法调用写入 sys_oper_log。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperLogAspect {

    private static final int MAX_TEXT_LENGTH = 2000;

    private final SysOperLogService operLogService;
    private final ObjectMapper objectMapper;

    /**
     * 环绕通知：将标注了 {@link OperationLog} 的方法调用写入 sys_oper_log。
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        SysOperLog operLog = buildOperLog(joinPoint, operationLog);

        try {
            Object result = joinPoint.proceed();
            operLog.setStatus(OperLogStatusEnum.SUCCESS.getCode());
            return result;
        } catch (Throwable ex) {
            operLog.setStatus(OperLogStatusEnum.FAIL.getCode());
            operLog.setErrorMsg(truncate(ex.getMessage()));
            throw ex;
        } finally {
            operLog.setCostTime(System.currentTimeMillis() - start);
            try {
                operLogService.save(operLog);
            } catch (Exception e) {
                log.error("操作日志写入失败: module={}, method={}", operLog.getModule(), operLog.getMethod(), e);
            }
        }
    }

    /**
     * 构建操作日志
     */
    private SysOperLog buildOperLog(ProceedingJoinPoint joinPoint, OperationLog operationLog) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        SysOperLog operLog = new SysOperLog();
        operLog.setUserId(SecurityUtils.getUserId());
        operLog.setModule(operationLog.module());
        operLog.setBusinessType(operationLog.businessType().getCode());
        operLog.setMethod(signature.getDeclaringType().getSimpleName() + "." + signature.getName());
        operLog.setOperTime(LocalDateTime.now());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            operLog.setRequestMethod(request.getMethod());
            operLog.setRequestUrl(request.getRequestURI());
        }

        if (operationLog.saveParam()) {
            operLog.setOperParam(truncate(serializeArgs(joinPoint.getArgs())));
        }

        return operLog;
    }

    /**
     * 序列化请求参数
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(args.length == 1 ? args[0] : args);
        } catch (Exception e) {
            return "[参数序列化失败]";
        }
    }

    /**
     * 截断文本
     */
    private String truncate(String text) {
        if (text == null) {
            return null;
        }
        return text.length() <= MAX_TEXT_LENGTH ? text : text.substring(0, MAX_TEXT_LENGTH);
    }
}
