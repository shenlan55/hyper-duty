package com.lasu.hyperduty.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.service.impl.CacheableServiceImpl;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.entity.SysDictType;
import com.lasu.hyperduty.system.mapper.SysDictDataMapper;
import com.lasu.hyperduty.system.service.SysDictDataService;
import com.lasu.hyperduty.system.service.SysDictTypeService;
import com.lasu.hyperduty.common.utils.CacheUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;








@Service
public class SysDictDataServiceImpl extends CacheableServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @Override
    @Cacheable(value = "dict", key = "'data_' + #dictTypeId")
    public List<SysDictData> getByDictTypeId(Long dictTypeId) {
        return sysDictDataMapper.selectByDictTypeId(dictTypeId);
    }

    /**
     * 按 dict_code 批量查询字典数据
     * 流程：dict_code 列表 → 查 SysDictType 拿 typeId 集合 → 批量查 SysDictData → 按 dict_code 重组
     * @param dictCodes 字典类型编码集合
     * @return Map<dictCode, List<SysDictData>>
     */
    @Override
    public Map<String, List<SysDictData>> getByDictTypeCodes(List<String> dictCodes) {
        if (dictCodes == null || dictCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        // 去除空值、去重、保持顺序
        List<String> codes = dictCodes.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 查所有 dict_type（启用状态）
        List<SysDictType> typeList = sysDictTypeService.list(
                new QueryWrapper<SysDictType>()
                        .in("dict_code", codes)
                        .eq("status", 1)
        );
        if (typeList.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. code -> typeId 映射 + 收集 typeId 列表
        Map<String, Long> codeToIdMap = typeList.stream()
                .collect(Collectors.toMap(SysDictType::getDictCode, SysDictType::getId, (a, b) -> a));
        List<Long> typeIds = new ArrayList<>(codeToIdMap.values());

        // 3. 一次查全部 dict_data
        List<SysDictData> dataList = sysDictDataMapper.selectByDictTypeIds(typeIds);
        // 4. 按 dict_type_id 分组
        Map<Long, List<SysDictData>> grouped = dataList.stream()
                .collect(Collectors.groupingBy(SysDictData::getDictTypeId));

        // 5. 按入参 codes 顺序输出 Map
        Map<String, List<SysDictData>> result = new LinkedHashMap<>();
        for (String code : codes) {
            Long typeId = codeToIdMap.get(code);
            result.put(code, grouped.getOrDefault(typeId, Collections.emptyList()));
        }
        return result;
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