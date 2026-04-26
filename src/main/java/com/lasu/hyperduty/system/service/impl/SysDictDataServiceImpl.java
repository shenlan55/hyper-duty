package com.lasu.hyperduty.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.service.impl.CacheableServiceImpl;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.mapper.SysDictDataMapper;
import com.lasu.hyperduty.system.service.SysDictDataService;
import com.lasu.hyperduty.common.utils.CacheUtil;
import java.io.Serializable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;








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

    @Override
    public Page<SysDictData> page(
            Page<SysDictData> page, 
            Long dictTypeId, 
            String keyword) {
        QueryWrapper<SysDictData> queryWrapper = new QueryWrapper<>();
        
        // 根据字典类型ID过滤
        if (dictTypeId != null) {
            queryWrapper.eq("dict_type_id", dictTypeId);
        }
        
        // 根据关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> 
                wrapper.like("dict_label", keyword)
                       .or().like("dict_value", keyword)
            );
        }
        
        // 按排序字段排序
        queryWrapper.orderByAsc("dict_sort");
        
        return baseMapper.selectPage(page, queryWrapper);
    }
}