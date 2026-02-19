package com.lasu.hyperduty.annotation;

import java.lang.annotation.*;

/**
 * API限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * 限流时间窗口（秒）
     */
    int window() default 60;

    /**
     * 时间窗口内允许的最大请求数
     */
    int max() default 100;

    /**
     * 限流key前缀
     */
    String prefix() default "rate_limit";

    /**
     * 限流提示消息
     */
    String message() default "请求过于频繁，请稍后再试";

    /**
     * 是否基于IP限流
     */
    boolean byIp() default true;

    /**
     * 是否基于用户限流
     */
    boolean byUser() default false;
}
