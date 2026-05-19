package com.aiforge.common.annotation;

import com.aiforge.common.enums.OperBusinessTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作审计注解
 * 配合 {@link com.aiforge.system.aspect.OperLogAspect} 写入 sys_oper_log。
 * 用于 审计记录 的 AOP 切面注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块，如：文章管理
     */
    String module();

    /**
     * 业务类型
     */
    OperBusinessTypeEnum businessType() default OperBusinessTypeEnum.OTHER;

    /**
     * 是否保存请求参数
     */
    boolean saveParam() default true;
}
