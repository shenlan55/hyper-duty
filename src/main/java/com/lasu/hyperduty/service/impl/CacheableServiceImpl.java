package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.utils.CacheUtil;

import java.io.Serializable;

/**
 * 可缓存的Service基类
 * 提供增删改操作时的缓存清除功能
 * @param <M> Mapper类型，必须是BaseMapper的子类
 * @param <T> 实体类型
 */
public class CacheableServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    /**
     * 保存实体并清除缓存
     * @param entity 实体
     * @return 是否成功
     */
    @Override
    public boolean save(T entity) {
        boolean result = super.save(entity);
        if (result) {
            clearCache(entity);
        }
        return result;
    }

    /**
     * 更新实体并清除缓存
     * @param entity 实体
     * @return 是否成功
     */
    @Override
    public boolean updateById(T entity) {
        boolean result = super.updateById(entity);
        if (result) {
            clearCache(entity);
        }
        return result;
    }

    /**
     * 删除实体并清除缓存
     * @param id 实体ID
     * @return 是否成功
     */
    @Override
    public boolean removeById(Serializable id) {
        T entity = getById(id);
        boolean result = super.removeById(id);
        if (result && entity != null) {
            clearCache(entity);
        }
        return result;
    }

    /**
     * 清除缓存
     * 子类需要重写此方法，实现具体的缓存清除逻辑
     * @param entity 实体
     */
    protected void clearCache(T entity) {
        // 默认实现，子类需要重写
    }

    /**
     * 清除指定键的缓存
     * @param key 缓存键
     */
    protected void clearCache(String key) {
        CacheUtil.delete(key);
    }

    /**
     * 清除匹配模式的缓存
     * @param pattern 匹配模式，例如 "dict::data_*"
     */
    protected void clearCacheByPattern(String pattern) {
        CacheUtil.deleteByPattern(pattern);
    }
}
