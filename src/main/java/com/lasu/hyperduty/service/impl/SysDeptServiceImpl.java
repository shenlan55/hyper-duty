package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SysDept;
import com.lasu.hyperduty.mapper.SysDeptMapper;
import com.lasu.hyperduty.service.SysDeptService;
import com.lasu.hyperduty.utils.CacheUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends CacheableServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    @Cacheable(value = "dept", key = "'allDepts'")
    public List<SysDept> getAllDepts() {
        return list();
    }

    @Override
    @Cacheable(value = "dept", key = "'deptTree'")
    public List<SysDept> getDeptTree() {
        // 获取所有部门
        List<SysDept> allDepts = list();
        
        // 构建部门树
        return buildDeptTree(allDepts, 0L);
    }

    private List<SysDept> buildDeptTree(List<SysDept> deptList, Long parentId) {
        List<SysDept> children = new ArrayList<>();
        
        for (SysDept dept : deptList) {
            if (parentId.equals(dept.getParentId())) {
                // 递归查找子部门
                children.add(dept);
                // 这里可以添加子部门列表字段，暂时只返回顶层部门
            }
        }
        
        // 按排序字段排序
        return children.stream()
                .sorted((d1, d2) -> d1.getSort().compareTo(d2.getSort()))
                .collect(Collectors.toList());
    }

    /**
     * 清除所有部门缓存
     */
    private void clearAllDeptCache() {
        // 清除所有部门缓存
        CacheUtil.delete("dept::allDepts");
        CacheUtil.delete("dept::deptTree");
    }

    @Override
    protected void clearCache(SysDept entity) {
        // 清除所有部门缓存
        clearAllDeptCache();
    }

    @Override
    public boolean save(SysDept entity) {
        boolean result = super.save(entity);
        if (result) {
            // 清除所有部门缓存
            clearAllDeptCache();
        }
        return result;
    }

    @Override
    public boolean updateById(SysDept entity) {
        boolean result = super.updateById(entity);
        if (result) {
            // 清除所有部门缓存
            clearAllDeptCache();
        }
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 清除所有部门缓存
            clearAllDeptCache();
        }
        return result;
    }

}