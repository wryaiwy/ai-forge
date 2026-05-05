package com.aiforge.web.interceptor;

import com.aiforge.common.constant.SecurityConstants;
import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.aiforge.common.utils.RedisUtils;
import com.aiforge.common.utils.SecurityContextHolder;
import com.aiforge.common.utils.SecurityUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @Author: wengrunyang
 * @Description: 拦截器
 * @DateTime: 2026/4/11 17:02
 **/
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;

    // 这个方法在请求到达 Controller 之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 放行前端浏览器的跨域预检请求 (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 复用 SecurityUtils 获取并清洗 Token (自动处理 Bearer 前缀)
        String token = SecurityUtils.getToken(request);

        // 3. 如果没拿到 Token，直接抛出未登录异常
        if (!StringUtils.hasText(token)) {
            throw new AiForgeException(ResultCodeEnum.UNAUTHORIZED);
        }

        // 4. 去 Redis 里查这个 Token
        String userInfo = redisUtils.get("login:token:" + token);
        if (!StringUtils.hasText(userInfo)) {
            // Redis 里没有，说明 Token 错误或者已经过期
            throw new AiForgeException(ResultCodeEnum.UNAUTHORIZED);
        }

        // 5. Token 续期: 30 分钟 (1800秒)
        redisUtils.expire("login:token:" + token, 1800);

        // 6. 解析 JSON 并将关键信息打散存入 TTL 上下文
        JsonNode userNode = objectMapper.readTree(userInfo);

        // 根据以前 UserContext 里的逻辑，JSON 中包含 userId 和 userName
        if (userNode.has("userId")) {
            SecurityContextHolder.setUserId(userNode.get("userId").asText());
        }
        if (userNode.has("userName")) {
            SecurityContextHolder.setUserName(userNode.get("userName").asText());
        }

        // 6.2 将 JSON 字符串反序列化为 Map 或 Object，存入上下文供 getSysUser() 使用
        // 因为拦截器在 common 模块，不知道具体的 SysUser 类，所以转成通用 Object 存入 Map
        Object userObj = objectMapper.readValue(userInfo, Object.class);
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, userObj);

        return true;
    }

    // 这个方法在请求处理完毕后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束，务必清理 TTL 内存，防止线程池复用导致的内存泄漏或数据串号
        SecurityContextHolder.remove();
    }
}
