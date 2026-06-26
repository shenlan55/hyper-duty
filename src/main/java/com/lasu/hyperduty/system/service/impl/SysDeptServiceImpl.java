package com.lasu.hyperduty.system.service.impl;

import com.lasu.hyperduty.common.service.impl.CacheableServiceImpl;
import com.lasu.hyperduty.system.entity.SysDept;
import com.lasu.hyperduty.system.mapper.SysDeptMapper;
import com.lasu.hyperduty.system.service.SysDeptService;
import com.lasu.hyperduty.common.utils.CacheUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;








@Service
public class SysDeptServiceImpl extends CacheableServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    @Cacheable(value = "dept", key = "'allDepts'")
    public List<SysDept> getAllDepts() {
        // 通用部门查询接口（默认全量，包括禁用）
        // 调用方：系统管理（Dept.vue / Employee.vue / SystemStatisticsController）
        // 业务模块选人/选部门 → 用 getActiveDepts() 替代
        // 2026-06-27 重构：双接口方案，方法名即契约，避免"白名单"维护成本
        return list();
    }

    @Override
    // 暂时禁用缓存，确保返回最新数据
    // @Cacheable(value = "dept", key = "'deptTree'")
    public List<SysDept> getDeptTree() {
        // 部门树查询（默认全量，包括禁用）
        // 调用方：系统管理（Dept.vue / Employee.vue）
        // 业务模块选人 → 用 getActiveDeptTree() 替代
        // 2026-06-27 重构：双接口方案
        List<SysDept> allDepts = list();
        System.out.println("所有部门数据: " + allDepts);

        // 构建部门树
        List<SysDept> deptTree = buildDeptTree(allDepts, 0L);
        System.out.println("构建后的部门树: " + deptTree);
        return deptTree;
    }

    @Override
    @Cacheable(value = "dept", key = "'activeDepts'")
    public List<SysDept> getActiveDepts() {
        // 启用部门查询接口（只返回 status=1）
        // 调用方：业务模块（PersonSelector / EmployeeSelector / DutyAssignment / DutyStatisticsServiceImpl）
        // 2026-06-27 新增：双接口方案，业务模块专用
        return lambdaQuery().eq(SysDept::getStatus, 1).list();
    }

    @Override
    // 暂时禁用缓存，确保返回最新数据
    // @Cacheable(value = "dept", key = "'activeDeptTree'")
    public List<SysDept> getActiveDeptTree() {
        // 启用部门树查询（只返回 status=1）
        // 调用方：业务模块选人（PersonSelector.vue）
        // 2026-06-27 新增：双接口方案，业务模块专用
        List<SysDept> activeDepts = lambdaQuery().eq(SysDept::getStatus, 1).list();
        System.out.println("启用部门数据: " + activeDepts);

        // 构建部门树
        List<SysDept> deptTree = buildDeptTree(activeDepts, 0L);
        System.out.println("启用部门树: " + deptTree);
        return deptTree;
    }

    private List<SysDept> buildDeptTree(List<SysDept> deptList, Long parentId) {
        List<SysDept> children = new ArrayList<>();
        
        for (SysDept dept : deptList) {
            if (parentId.equals(dept.getParentId())) {
                // 递归查找子部门
                List<SysDept> subDepts = buildDeptTree(deptList, dept.getId());
                dept.setChildren(subDepts);
                children.add(dept);
            }
        }
        
        // 按排序字段排序
        return children.stream()
                .sorted((d1, d2) -> d1.getSort().compareTo(d2.getSort()))
                .collect(Collectors.toList());
    }

    /**
     * 清除所有部门缓存（含启用/全量两组）
     */
    private void clearAllDeptCache() {
        // 清除所有部门缓存（双接口都要清）
        CacheUtil.delete("dept::allDepts");
        CacheUtil.delete("dept::deptTree");
        CacheUtil.delete("dept::activeDepts");
        CacheUtil.delete("dept::activeDeptTree");
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