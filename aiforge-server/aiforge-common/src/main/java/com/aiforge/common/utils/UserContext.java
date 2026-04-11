package com.aiforge.common.utils;

/**
 * @Author: wengrunyang
 * @Description: 上下文工具类
 * @DateTime: 2026/4/11 17:01
 **/
public class UserContext {

    // 使用 ThreadLocal 存储当前线程的 Token
    private static final ThreadLocal<String> USER_THREAD_LOCAL = new ThreadLocal<>();

    // 存入当前用户的 Token (或者用户 JSON 字符串)
    public static void set(String userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }

    // 获取当前用户
    public static String get() {
        return USER_THREAD_LOCAL.get();
    }

    // 防止内存泄漏
    public static void remove() {
        USER_THREAD_LOCAL.remove();
    }

}
