package com.aiforge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要 AI 调用监控的方法或类。
 * 配合 {@link com.aiforge.common.aspect.AiInvokeLogAspect} 记录调用用户、耗时与结果。
 * 用于 日志记录 的 AOP 切面注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AiMonitored {

    /**
     * 可选的业务描述，便于日志检索
     */
    String value() default "";
}
