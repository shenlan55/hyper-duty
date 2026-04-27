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
        String key = buildRateLimitKey(rateLimit, method, request);
        
        // 检查是否超过限流
        Long currentCount = getCurrentCount(key);
        if (currentCount >= rateLimit.max()) {
            return ResponseResult.error(rateLimit.message());
        }
        
        // 只有没超过限流时才增加计数器
        incrementCount(key, rateLimit.window());
        
        // 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 构建限流key
     */
    private String buildRateLimitKey(RateLimit rateLimit, Method method, HttpServletRequest request) {
        StringBuilder key = new StringBuilder(rateLimit.prefix());
        
        // 添加方法名（类名.方法名）
        key.append("_").append(method.getDeclaringClass().getSimpleName());
        key.append("_").append(method.getName());
        
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
     * 获取当前计数
     */
    private Long getCurrentCount(String key) {
        Object count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            return 0L;
        }
        return Long.parseLong(count.toString());
    }

    /**
     * 增加计数
     */
    private void incrementCount(String key, int windowSeconds) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
    }
}
