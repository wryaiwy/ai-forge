package com.aiforge.system.service.impl;

import com.aiforge.system.dto.LoginDTO;
import com.aiforge.system.dto.RegisterDTO;

import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.utils.RedisUtils;
import com.aiforge.system.entity.SysUser;
import com.aiforge.system.mapper.SysUserMapper;
import com.aiforge.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

/**
 * @Author: wengrunyang
 * @Description: sys_user服务实现类
 * @DateTime: 2026/4/11 9:47
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper userMapper;

    private final RedisUtils redisUtils;

    private final ObjectMapper objectMapper; // SpringBoot 默认自带 Jackson

    /**
     * 用户登录
     * 
     * @param loginDTO 登录参数
     * @return token
     * @throws Exception
     */
    @Override
    public String login(LoginDTO loginDTO) throws Exception {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, loginDTO.getUserName()));

        // 2. 校验是否存在
        if (user == null) {
            throw new AiForgeException(400, "该账号未创建");
        }

        // 3. 校验密码
        if (!new BCryptPasswordEncoder().matches(loginDTO.getPassword(), user.getPassword())) {
            throw new AiForgeException(400, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new AiForgeException(400, "账号已被禁用");
        }

        // 3. 生成 Token
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        // 4. 存入 Redis，设置 30 分钟过期 (1800秒)
        // Key 建议带前缀，方便管理，如 "login:token:xxxx"
        String userJson = objectMapper.writeValueAsString(user);
        redisUtils.set("login:token:" + token, userJson, 1800);

        return token;
    }

    /**
     * 用户退出登录
     * 
     * @param token
     * @return
     */
    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            redisUtils.delete("login:token:" + token);
        }
    }

    /**
     * 用户注册
     * 
     * @param registerDTO 注册参数
     * @return
     * @throws Exception
     */
    @Override
    public void register(RegisterDTO registerDTO) throws Exception {
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            throw new AiForgeException(400, "两次输入的密码不一致");
        }
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, registerDTO.getUserName()));
        if (user != null) {
            throw new AiForgeException(400, "用户名已存在");
        }

        SysUser newUser = new SysUser();
        newUser.setUserName(registerDTO.getUserName());
        newUser.setPassword(new BCryptPasswordEncoder().encode(registerDTO.getPassword()));
        newUser.setStatus(1);

        userMapper.insert(newUser);
    }
}
