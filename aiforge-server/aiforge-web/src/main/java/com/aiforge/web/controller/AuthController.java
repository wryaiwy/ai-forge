package com.aiforge.web.controller;

import com.aiforge.common.result.Result;
import com.aiforge.system.dto.LoginDTO;
import com.aiforge.system.dto.RegisterDTO;
import com.aiforge.system.service.SysUserService;
import com.aiforge.system.service.impl.SysUserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: wengrunyang
 * @Description: 认证管理
 * @DateTime: 2026/4/11 9:49
 **/
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService userService;

    /**
     * 用户登录
     * 
     * @param loginDTO 登录数据
     * @return token
     * @throws Exception
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginDTO loginDTO) throws Exception {
        String token = userService.login(loginDTO);

        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        return Result.success(result);
    }

    /**
     * 用户退出登录
     * 
     * @param request
     * @return
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        // 1. 从请求头获取 Token
        String token = request.getHeader("Authorization");

        // 2. 执行退出逻辑
        userService.logout(token);

        // 3. 返回成功
        return Result.success();
    }

    /**
     * 用户注册
     * 
     * @param registerDTO 注册数据
     * @return
     * @throws Exception
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterDTO registerDTO) throws Exception {
        userService.register(registerDTO);
        return Result.success();
    }
}
