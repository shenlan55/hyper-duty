package com.lasu.hyperduty.service.impl;

import com.lasu.hyperduty.entity.SysDictData;
import com.lasu.hyperduty.mapper.SysDictDataMapper;
import com.lasu.hyperduty.service.SysDictDataService;
import com.lasu.hyperduty.utils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class SysDictDataServiceImpl extends CacheableServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @Override
    @Cacheable(value = "dict", key = "'data_' + #dictTypeId")
    public List<SysDictData> getByDictTypeId(Long dictTypeId) {
        return sysDictDataMapper.selectByDictTypeId(dictTypeId);
    }

    /**
     * 清除缓存
     * @param entity 字典数据实体
     */
    @Override
    protected void clearCache(SysDictData entity) {
        // 清除对应字典类型的缓存
        if (entity != null && entity.getDictTypeId() != null) {
            CacheUtil.deleteDictDataCache(entity.getDictTypeId());
        }
    }
    
    /**
     * 重载removeById方法，接收Long类型参数
     * 确保与Controller中的参数类型匹配
     */
    public boolean removeById(Long id) {
        return removeById((Serializable) id);
    }
}