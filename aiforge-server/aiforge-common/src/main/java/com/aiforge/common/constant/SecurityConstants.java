package com.aiforge.common.constant;

/**
 * @Author: wengrunyang
 * @Description: 安全相关常量
 * @DateTime: 2026/5/5 11:01
 **/
public interface SecurityConstants {

    String DETAILS_USER_ID = "user_id";
    String DETAILS_USERNAME = "username";
    String DETAILS_TENANT_ID = "tenant_id"; // SaaS 多租户支持

    String USER_KEY = "user_key";
    String ROLE_PERMISSION = "role_permission";
    String LOGIN_USER = "login_user";

    String AUTHORIZATION_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";

}
