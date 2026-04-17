package com.aiforge.system.service;

import com.aiforge.system.dto.LoginDTO;
import com.aiforge.system.dto.RegisterDTO;

/**
 * @Author: wengrunyang
 * @Description: sys_user服务接口
 * @DateTime: 2026/4/11 9:51
 **/
public interface SysUserService {

    /**
     * 用户登录
     * 
     * @param loginDTO 登录参数
     * @return token
     * @throws Exception
     */
    String login(LoginDTO loginDTO) throws Exception;

    /**
     * 用户退出登录
     *
     * @param token
     */
    void logout(String token);

    /**
     * 用户注册
     * 
     * @param registerDTO 注册参数
     * @return
     * @throws Exception
     */
    void register(RegisterDTO registerDTO) throws Exception;
}
