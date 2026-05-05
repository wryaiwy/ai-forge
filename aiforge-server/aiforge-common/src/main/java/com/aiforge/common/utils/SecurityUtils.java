package com.aiforge.common.utils;

import com.aiforge.common.constant.SecurityConstants;
import jakarta.servlet.http.HttpServletRequest; // Spring Boot 3 专属包路径
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @Author: wengrunyang
 * @Description: 权限获取工具类
 * @DateTime: 2026/5/5 11:02
 **/
public class SecurityUtils {

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return SecurityContextHolder.getUserId();
    }

    /**
     * 获取用户名称
     */
    public static String getUsername() {
        return SecurityContextHolder.getUserName();
    }

    /**
     * 获取当前租户ID (SaaS环境隔离使用)
     */
    public static Long getTenantId() {
        return SecurityContextHolder.getTenantId();
    }

    /**
     * 根据当前 Spring MVC 请求获取 token
     */
    public static String getToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return getToken(attributes.getRequest());
        }
        return null;
    }

    /**
     * 根据 request 获取请求 token
     */
    public static String getToken(HttpServletRequest request) {
        // 从 header 获取 token 标识
        String token = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        return replaceTokenPrefix(token);
    }

    /**
     * 裁剪 token 前缀
     */
    public static String replaceTokenPrefix(String token) {
        if (StringUtils.hasText(token) && token.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return token.replaceFirst(SecurityConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 获取当前登录用户的完整对象 (泛型方法，避免 common 模块找不到上层实体类)
     *
     * @param clazz 目标实体类的 Class 对象
     * @return 完整用户对象，如果没有则返回 null
     */
    public static <T> T getSysUser(Class<T> clazz) {
        return SecurityContextHolder.get(SecurityConstants.LOGIN_USER, clazz);
    }
}
