package com.aiforge.web.interceptor;

import com.aiforge.common.exception.AiForgeException;
import com.aiforge.common.result.ResultCodeEnum;
import com.aiforge.common.utils.RedisUtils;
import com.aiforge.common.utils.UserContext;
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

    // 这个方法在请求到达 Controller 之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 1. 放行前端浏览器的跨域预检请求 (OPTIONS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 从请求头中获取 Token (通常前端会放在 Authorization 请求头里)
        String token = request.getHeader("Authorization");
        // 如果前端带了 Bearer 前缀，就把它去掉 (这是规范的做法)
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 3. 如果没拿到 Token，直接抛出未登录异常 (会被全局异常处理器接管！)
        if (!StringUtils.hasText(token)) {
            throw new AiForgeException(ResultCodeEnum.UNAUTHORIZED);
        }

        // 4. 去 Redis 里查这个 Token
        String userInfo = redisUtils.get("login:token:" + token);
        if (!StringUtils.hasText(userInfo)) {
            // Redis 里没有，说明 Token 错误或者已经过期
            throw new AiForgeException(ResultCodeEnum.UNAUTHORIZED);
        }

        // 5. Token 续期: 如果用户还在操作，重置 30 分钟过期时间
        redisUtils.expire("login:token:" + token, 1800);

        // 6. 将用户信息存入 ThreadLocal，放行请求
        UserContext.set(userInfo);
        return true;
    }

    // 这个方法在请求处理完毕后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 一定要清理，因为 Tomcat 线程池里的线程是复用的，不清理会导致数据错乱和内存泄漏
        UserContext.remove();
    }
}
