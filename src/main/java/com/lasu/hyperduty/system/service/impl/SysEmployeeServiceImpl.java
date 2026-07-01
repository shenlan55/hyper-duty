package com.lasu.hyperduty.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.dto.PageRequestDTO;
import com.lasu.hyperduty.common.dto.PageResponseDTO;
import com.lasu.hyperduty.common.service.impl.CacheableServiceImpl;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import com.lasu.hyperduty.common.utils.CacheUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;








@Service
public class SysEmployeeServiceImpl extends CacheableServiceImpl<SysEmployeeMapper, SysEmployee> implements SysEmployeeService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "employee", key = "'allEmployees'")
    public List<SysEmployee> getAllEmployees() {
        // 2026-06-27 修复：通用人员查询接口默认只返回启用员工（status=1）
        // 业务模块（积分、值班、项目、AI 等）通过此接口获取可选人员，不应出现已禁用员工
        // 系统管理-员工管理分页接口 page(...) 仍走全量，不受此影响
        return lambdaQuery().eq(SysEmployee::getStatus, 1).list();
    }

    @Override
    @Cacheable(value = "employee", key = "#deptId")
    public List<SysEmployee> getEmployeesByDeptId(Long deptId) {
        // 2026-06-27 修复：通用人员查询接口默认只返回启用员工（status=1）
        LambdaQueryWrapper<SysEmployee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysEmployee::getDeptId, deptId)
                .eq(SysEmployee::getStatus, 1);
        return list(queryWrapper);
    }

    @Override
    public List<SysEmployee> getActiveEmployeesByUsernames(java.util.Collection<String> usernames) {
        // 2026-06-28 新增：驳回目标节点的审批人姓名注入
        // 走 status=1 过滤（navigator 避坑 #17）；空集合短路返回
        if (usernames == null || usernames.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return lambdaQuery()
                .in(SysEmployee::getUsername, usernames)
                .eq(SysEmployee::getStatus, 1)
                .list();
    }

    @Override
    public boolean save(SysEmployee sysEmployee) {
        // 加密密码
        if (sysEmployee.getPassword() != null && !sysEmployee.getPassword().isEmpty()) {
            sysEmployee.setPassword(passwordEncoder.encode(sysEmployee.getPassword()));
        }
        boolean result = super.save(sysEmployee);
        if (result) {
            // 清除员工相关的缓存（新增时 oldEmployee 传 null 即可，clearEmployeeCache 已处理 null 场景）
            clearEmployeeCache(sysEmployee, null);
        }
        return result;
    }

    @Override
    public boolean updateById(SysEmployee sysEmployee) {
        // 先查旧记录：用于（1）部门变更时清旧部门 cache（2）状态变更时清掉所有相关 cache
        SysEmployee oldEmployee = getById(sysEmployee.getId());
        // 使用LambdaUpdateWrapper强制更新所有字段，包括null值
        LambdaUpdateWrapper<SysEmployee> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysEmployee::getId, sysEmployee.getId())
                .set(SysEmployee::getEmployeeName, sysEmployee.getEmployeeName())
                .set(SysEmployee::getEmployeeCode, sysEmployee.getEmployeeCode())
                .set(SysEmployee::getUsername, sysEmployee.getUsername())
                .set(SysEmployee::getDeptId, sysEmployee.getDeptId())
                .set(SysEmployee::getPhone, sysEmployee.getPhone())
                .set(SysEmployee::getEmail, sysEmployee.getEmail())
                .set(SysEmployee::getGender, sysEmployee.getGender())
                .set(SysEmployee::getDictTypeId, sysEmployee.getDictTypeId())
                .set(SysEmployee::getDictDataId, sysEmployee.getDictDataId())
                .set(SysEmployee::getStatus, sysEmployee.getStatus())
                .set(SysEmployee::getSort, sysEmployee.getSort())
                .set(SysEmployee::getUpdateTime, sysEmployee.getUpdateTime());

        // 如果密码不为空，则加密密码并更新
        if (sysEmployee.getPassword() != null && !sysEmployee.getPassword().isEmpty()) {
            updateWrapper.set(SysEmployee::getPassword, passwordEncoder.encode(sysEmployee.getPassword()));
        }

        boolean result = update(updateWrapper);
        if (result) {
            // 清除员工相关的缓存
            clearEmployeeCache(sysEmployee, oldEmployee);
        }
        return result;
    }

    /**
     * 清除员工相关的缓存
     * @param sysEmployee 新员工信息
     * @param oldEmployee 旧员工信息（可为 null）
     */
    private void clearEmployeeCache(SysEmployee sysEmployee, SysEmployee oldEmployee) {
        // 清除所有员工缓存（getAllEmployees 永远清）
        CacheUtil.delete("employee::allEmployees");
        // 清除新部门的员工缓存
        if (sysEmployee != null && sysEmployee.getDeptId() != null) {
            CacheUtil.delete("employee::" + sysEmployee.getDeptId());
        }
        // 2026-06-27 修复：员工换部门时，旧部门的 cache 也必须清掉
        // 否则旧部门下选人组件会看到该员工（但实际上已不在该部门）
        if (oldEmployee != null && oldEmployee.getDeptId() != null
                && (sysEmployee == null || !oldEmployee.getDeptId().equals(sysEmployee.getDeptId()))) {
            CacheUtil.delete("employee::" + oldEmployee.getDeptId());
        }
    }

    @Override
    public Page<SysEmployee> page(
            Page<SysEmployee> page, 
            String keyword, 
            Long deptId) {
        LambdaQueryWrapper<SysEmployee> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like(SysEmployee::getEmployeeName, keyword)
                    .or()
                    .like(SysEmployee::getEmployeeCode, keyword)
                    .or()
                    .like(SysEmployee::getUsername, keyword);
        }
        
        // 部门筛选
        if (deptId != null) {
            queryWrapper.eq(SysEmployee::getDeptId, deptId);
        }

        // 按排序字段正序排列
        queryWrapper.orderByAsc(SysEmployee::getSort)
                .orderByAsc(SysEmployee::getId);
        
        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public PageResponseDTO<SysEmployee> page(PageRequestDTO pageRequestDTO, Map<String, Object> params) {
        // 创建MyBatis Plus的Page对象
        Page<SysEmployee> page = createPage(pageRequestDTO);
        
        // 从params中获取查询参数
        String keyword = pageRequestDTO.getKeyword();
        Long deptId = params != null ? (Long) params.get("deptId") : null;
        
        // 调用现有的page方法进行分页查询
        Page<SysEmployee> resultPage = page(page, keyword, deptId);
        
        // 转换为PageResponseDTO
        return toPageResponse(resultPage);
    }

    @Override
    protected void clearCache(SysEmployee entity) {
        // 清除员工相关的缓存（CacheableServiceImpl 框架调用此方法，oldEmployee 传 null 即可，部门变更场景由 updateById 显式处理）
        clearEmployeeCache(entity, null);
    }

}