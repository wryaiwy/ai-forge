package com.aiforge.common.utils;

import com.aiforge.common.constant.SecurityConstants;
import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: wengrunyang
 * @Description: 获取当前线程变量中的 用户id、用户名称、租户id等信息
 * @DateTime: 2026/5/5 11:01
 **/
public class SecurityContextHolder {

    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(String key, Object value) {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? "" : value);
    }

    public static String get(String key) {
        Map<String, Object> map = getLocalMap();
        Object val = map.getOrDefault(key, "");
        return val == null ? "" : String.valueOf(val);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        Map<String, Object> map = getLocalMap();
        return (T) map.getOrDefault(key, null);
    }

    public static Map<String, Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    public static void setLocalMap(Map<String, Object> threadLocalMap) {
        THREAD_LOCAL.set(threadLocalMap);
    }

    public static Long getUserId() {
        String userId = get(SecurityConstants.DETAILS_USER_ID);
        return (userId.isEmpty() || "null".equals(userId)) ? 0L : Long.parseLong(userId);
    }

    public static void setUserId(String account) {
        set(SecurityConstants.DETAILS_USER_ID, account);
    }

    public static String getUserName() {
        return get(SecurityConstants.DETAILS_USERNAME);
    }

    public static void setUserName(String username) {
        set(SecurityConstants.DETAILS_USERNAME, username);
    }

    // 预留的 SaaS 多租户隔离机制
    public static Long getTenantId() {
        String tenantId = get(SecurityConstants.DETAILS_TENANT_ID);
        return (tenantId.isEmpty() || "null".equals(tenantId)) ? 0L : Long.parseLong(tenantId);
    }

    public static void setTenantId(String tenantId) {
        set(SecurityConstants.DETAILS_TENANT_ID, tenantId);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
