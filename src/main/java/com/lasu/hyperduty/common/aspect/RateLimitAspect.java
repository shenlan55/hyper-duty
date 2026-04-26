package com.lasu.hyperduty.common.aspect;

import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import io.jsonwebtoken.lang.Strings;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;








/**
 * API限流切面
 */
@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.lasu.hyperduty.common.annotation.RateLimit)")
    public void rateLimitPointcut() {
    }

    /**
     * 环绕通知
     */
    @Around("rateLimitPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取限流注解
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        
        // 构建限流key
        String key = buildRateLimitKey(rateLimit, request);
        
        // 检查是否超过限流
        if (isOverLimit(key, rateLimit)) {
            return ResponseResult.error(rateLimit.message());
        }
        
        // 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 构建限流key
     */
    private String buildRateLimitKey(RateLimit rateLimit, HttpServletRequest request) {
        StringBuilder key = new StringBuilder(rateLimit.prefix());
        
        // 添加IP
        if (rateLimit.byIp()) {
            String ip = getClientIp(request);
            key.append("_ip_").append(ip);
        }
        
        // 添加用户ID（如果需要）
        if (rateLimit.byUser()) {
            String userId = request.getHeader("X-User-Id");
            if (Strings.hasText(userId)) {
                key.append("_user_").append(userId);
            }
        }
        
        return key.toString();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 检查是否超过限流
     */
    private boolean isOverLimit(String key, RateLimit rateLimit) {
        // 自增计数器
        Long count = redisTemplate.opsForValue().increment(key);
        
        // 如果是第一次请求，设置过期时间
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.window(), TimeUnit.SECONDS);
        }
        
        // 检查是否超过最大请求数
        return count > rateLimit.max();
    }
}
