package com.aiforge.common.aspect;

import com.aiforge.common.annotation.AiMonitored;
import com.aiforge.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AI 调用监控切面：记录调用用户、方法、耗时与成功/失败状态。
 */
@Slf4j
@Aspect
@Component
public class AiInvokeLogAspect {

    /**
     * 环绕通知：记录 AI 调用用户、方法、耗时与成功/失败状态。
     */
    @Around("@within(com.aiforge.common.annotation.AiMonitored) || @annotation(com.aiforge.common.annotation.AiMonitored)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        String bizDesc = resolveBizDesc(joinPoint, signature);

        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();

        log.info("[AI模块调用] 开始 | userId={} | username={} | method={} | desc={}",
                userId, username, method, bizDesc);

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            log.info("[AI模块调用] 成功 | userId={} | username={} | method={} | desc={} | cost={}ms",
                    userId, username, method, bizDesc, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("[AI模块调用] 失败 | userId={} | username={} | method={} | desc={} | cost={}ms | error={}",
                    userId, username, method, bizDesc, System.currentTimeMillis() - start, ex.getMessage());
            throw ex;
        }
    }

    /**
     * 解析业务描述：从方法或类上的 @AiMonitored 注解中获取。
     */
    private String resolveBizDesc(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        AiMonitored methodAnno = AnnotationUtils.findAnnotation(signature.getMethod(), AiMonitored.class);
        if (methodAnno != null && StringUtils.hasText(methodAnno.value())) {
            return methodAnno.value();
        }

        AiMonitored classAnno = AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), AiMonitored.class);
        if (classAnno != null && StringUtils.hasText(classAnno.value())) {
            return classAnno.value();
        }

        return "-";
    }
}
