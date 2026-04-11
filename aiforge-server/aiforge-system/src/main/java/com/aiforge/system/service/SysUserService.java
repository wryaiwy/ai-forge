package com.aiforge.system.service;

/**
 * @Author: wengrunyang
 * @Description: sys_user服务接口
 * @DateTime: 2026/4/11 9:51
 **/
public interface SysUserService {

    /**
     * 用户登录
     * 
     * @param userName 用户名
     * @param password 密码
     * @return token
     * @throws Exception
     */
    String login(String userName, String password) throws Exception;

    /**
     * 用户退出登录
     * 
     * @param request
     * @return
     */
    void logout(String token);

    /**
     * 用户注册
     * 
     * @param userName 用户名
     * @param password 密码
     * @return
     * @throws Exception
     */
    void register(String userName, String password) throws Exception;
}
