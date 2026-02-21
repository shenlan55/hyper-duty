package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.dto.PageRequestDTO;
import com.lasu.hyperduty.dto.PageResponseDTO;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.mapper.SysEmployeeMapper;
import com.lasu.hyperduty.service.SysEmployeeService;
import com.lasu.hyperduty.utils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SysEmployeeServiceImpl extends CacheableServiceImpl<SysEmployeeMapper, SysEmployee> implements SysEmployeeService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Cacheable(value = "employee", key = "'allEmployees'")
    public List<SysEmployee> getAllEmployees() {
        return list();
    }

    @Override
    @Cacheable(value = "employee", key = "#deptId")
    public List<SysEmployee> getEmployeesByDeptId(Long deptId) {
        LambdaQueryWrapper<SysEmployee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysEmployee::getDeptId, deptId);
        return list(queryWrapper);
    }

    @Override
    public boolean save(SysEmployee sysEmployee) {
        // 加密密码
        if (sysEmployee.getPassword() != null && !sysEmployee.getPassword().isEmpty()) {
            sysEmployee.setPassword(passwordEncoder.encode(sysEmployee.getPassword()));
        }
        boolean result = super.save(sysEmployee);
        if (result) {
            // 清除员工相关的缓存
            clearEmployeeCache(sysEmployee);
        }
        return result;
    }

    @Override
    public boolean updateById(SysEmployee sysEmployee) {
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
                .set(SysEmployee::getUpdateTime, sysEmployee.getUpdateTime());
        
        // 如果密码不为空，则加密密码并更新
        if (sysEmployee.getPassword() != null && !sysEmployee.getPassword().isEmpty()) {
            updateWrapper.set(SysEmployee::getPassword, passwordEncoder.encode(sysEmployee.getPassword()));
        }
        
        boolean result = update(updateWrapper);
        if (result) {
            // 清除员工相关的缓存
            clearEmployeeCache(sysEmployee);
        }
        return result;
    }

    /**
     * 清除员工相关的缓存
     * @param sysEmployee 员工信息
     */
    private void clearEmployeeCache(SysEmployee sysEmployee) {
        // 清除所有员工缓存
        CacheUtil.delete("employee::allEmployees");
        // 如果员工有部门ID，清除对应部门的员工缓存
        if (sysEmployee != null && sysEmployee.getDeptId() != null) {
            CacheUtil.delete("employee::" + sysEmployee.getDeptId());
        }
    }

    @Override
    public Page<SysEmployee> page(Page<SysEmployee> page, String keyword, Long deptId) {
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
        // 清除员工相关的缓存
        clearEmployeeCache(entity);
    }

}