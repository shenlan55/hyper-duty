package com.lasu.hyperduty.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;

/**
 * 缓存工具类
 * 提供缓存的清除、获取等操作
 */
@Component
public class CacheUtil {

    private static RedisTemplate<String, Object> redisTemplate;

    /**
     * 注入RedisTemplate
     * 静态注入，确保静态方法能够使用
     */
    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        CacheUtil.redisTemplate = redisTemplate;
    }

    /**
     * 清除指定的缓存键
     * @param key 缓存键
     */
    public static void delete(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 清除多个缓存键
     * @param keys 缓存键集合
     */
    public static void delete(Collection<String> keys) {
        if (redisTemplate != null && keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 清除匹配模式的缓存键
     * @param pattern 匹配模式，例如 "dict::data_*"
     */
    public static void deleteByPattern(String pattern) {
        if (redisTemplate != null) {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    /**
     * 清除字典数据缓存
     * @param dictTypeId 字典类型ID
     */
    public static void deleteDictDataCache(Long dictTypeId) {
        if (dictTypeId != null) {
            delete("dict::data_" + dictTypeId);
        }
    }

    /**
     * 清除字典类型缓存
     */
    public static void deleteDictTypeCache() {
        deleteByPattern("dict::type_*");
    }

    /**
     * 清除用户缓存
     * @param userId 用户ID
     */
    public static void deleteUserCache(Long userId) {
        if (userId != null) {
            delete("user::info_" + userId);
        }
    }

    /**
     * 清除角色缓存
     * @param roleId 角色ID
     */
    public static void deleteRoleCache(Long roleId) {
        if (roleId != null) {
            delete("role::info_" + roleId);
        }
    }

    /**
     * 清除菜单缓存
     * @param userId 用户ID
     */
    public static void deleteMenuCache(Long userId) {
        if (userId != null) {
            delete("menu::user_" + userId);
        }
    }

    /**
     * 清除所有缓存
     * 注意：谨慎使用，会清除所有缓存
     */
    public static void deleteAll() {
        if (redisTemplate != null) {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }
}
