package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.dto.UserVO;
import com.lasu.hyperduty.dto.PageRequestDTO;
import com.lasu.hyperduty.dto.PageResponseDTO;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.entity.SysUser;
import com.lasu.hyperduty.mapper.SysUserMapper;
import com.lasu.hyperduty.service.SysEmployeeService;
import com.lasu.hyperduty.service.SysUserService;
import com.lasu.hyperduty.utils.CacheUtil;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends CacheableServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysEmployeeService sysEmployeeService;
    private PasswordEncoder passwordEncoder;

    // 使用构造函数注入，只注入非循环依赖的Bean
    @Autowired
    public SysUserServiceImpl(SysUserMapper sysUserMapper, SysEmployeeService sysEmployeeService) {
        this.sysUserMapper = sysUserMapper;
        this.sysEmployeeService = sysEmployeeService;
    }

    // 使用方法注入来注入PasswordEncoder，打破循环依赖
    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SysUser getByUsername(String username) {
        // 从SysEmployee表中查询用户信息
        LambdaQueryWrapper<SysEmployee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysEmployee::getUsername, username);
        SysEmployee employee = sysEmployeeService.getOne(queryWrapper);
        
        if (employee == null) {
            return null;
        }
        
        // 转换为SysUser对象
        SysUser sysUser = new SysUser();
        sysUser.setId(employee.getId());
        sysUser.setUsername(employee.getUsername());
        sysUser.setPassword(employee.getPassword());
        sysUser.setEmployeeId(employee.getId());
        sysUser.setStatus(employee.getStatus());
        sysUser.setCreateTime(employee.getCreateTime());
        sysUser.setUpdateTime(employee.getUpdateTime());
        
        return sysUser;
    }

    @Override
    @Cacheable(value = "user", key = "'allUsers'")
    public List<UserVO> getAllUsers() {
        // 获取所有人员
        List<SysEmployee> employees = sysEmployeeService.getAllEmployees();
        
        // 转换为UserVO列表
        return employees.stream()
                .filter(emp -> emp.getUsername() != null && !emp.getUsername().isEmpty())
                .map(emp -> {
                    UserVO userVO = new UserVO();
                    // 复制用户基本信息
                    userVO.setId(emp.getId());
                    userVO.setUsername(emp.getUsername());
                    userVO.setPassword(emp.getPassword());
                    userVO.setEmployeeId(emp.getId());
                    userVO.setEmployeeName(emp.getEmployeeName());
                    userVO.setStatus(emp.getStatus());
                    userVO.setCreateTime(emp.getCreateTime());
                    userVO.setUpdateTime(emp.getUpdateTime());
                    
                    return userVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean save(SysUser sysUser) {
        // 由于我们已经将用户管理的功能合并到了人员管理中，这个方法不再使用
        // 所有的保存操作都通过SysEmployeeService的save方法进行
        return false;
    }

    @Override
    public boolean updateById(SysUser sysUser) {
        // 由于我们已经将用户管理的功能合并到了人员管理中，这个方法不再使用
        // 所有的更新操作都通过SysEmployeeService的updateById方法进行
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = getByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

        // 检查用户状态
        if (sysUser.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已禁用");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        try {
            // 获取用户角色编码
            List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(sysUser.getId());
            // 获取用户权限标识
            List<String> perms = sysUserMapper.selectPermsByUserId(sysUser.getId());
            
            // 添加角色
            for (String roleCode : roleCodes) {
                authorities.add(new SimpleGrantedAuthority(roleCode));
            }
            
            // 添加权限
            for (String perm : perms) {
                authorities.add(new SimpleGrantedAuthority(perm));
            }
        } catch (Exception e) {
            // 如果获取角色或权限失败，至少给用户一个默认角色
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(sysUser.getUsername(), sysUser.getPassword(), authorities);
    }

    @Override
    public PageResponseDTO<SysUser> page(PageRequestDTO pageRequestDTO, Map<String, Object> params) {
        // 创建MyBatis Plus的Page对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysEmployee> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageRequestDTO.getPageNum(), pageRequestDTO.getPageSize());
        
        // 从params中获取查询参数
        String keyword = pageRequestDTO.getKeyword();
        Long deptId = params != null ? (Long) params.get("deptId") : null;
        
        // 获取分页数据
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysEmployee> employeePage = sysEmployeeService.page(page, keyword, deptId);
        
        // 转换为SysUser列表
        List<SysUser> userList = employeePage.getRecords().stream()
                .filter(emp -> emp.getUsername() != null && !emp.getUsername().isEmpty())
                .map(emp -> {
                    SysUser sysUser = new SysUser();
                    sysUser.setId(emp.getId());
                    sysUser.setUsername(emp.getUsername());
                    sysUser.setPassword(emp.getPassword());
                    sysUser.setEmployeeId(emp.getId());
                    sysUser.setStatus(emp.getStatus());
                    sysUser.setCreateTime(emp.getCreateTime());
                    sysUser.setUpdateTime(emp.getUpdateTime());
                    return sysUser;
                })
                .collect(java.util.stream.Collectors.toList());
        
        // 创建新的Page对象，设置数据
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysUser> userPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        userPage.setRecords(userList);
        userPage.setTotal(employeePage.getTotal());
        userPage.setCurrent(employeePage.getCurrent());
        userPage.setSize(employeePage.getSize());
        userPage.setPages(employeePage.getPages());
        
        // 转换为PageResponseDTO
        return toPageResponse(userPage);
    }

    @Override
    public PageResponseDTO<UserVO> pageUserVO(PageRequestDTO pageRequestDTO, Map<String, Object> params) {
        // 创建MyBatis Plus的Page对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysEmployee> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageRequestDTO.getPageNum(), pageRequestDTO.getPageSize());
        
        // 从params中获取查询参数
        String keyword = pageRequestDTO.getKeyword();
        Long deptId = params != null ? (Long) params.get("deptId") : null;
        
        // 获取分页数据
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysEmployee> employeePage = sysEmployeeService.page(page, keyword, deptId);
        
        // 转换为UserVO列表
        List<UserVO> userVOList = employeePage.getRecords().stream()
                .filter(emp -> emp.getUsername() != null && !emp.getUsername().isEmpty())
                .map(emp -> {
                    UserVO userVO = new UserVO();
                    userVO.setId(emp.getId());
                    userVO.setUsername(emp.getUsername());
                    userVO.setPassword(emp.getPassword());
                    userVO.setEmployeeId(emp.getId());
                    userVO.setEmployeeName(emp.getEmployeeName());
                    userVO.setStatus(emp.getStatus());
                    userVO.setCreateTime(emp.getCreateTime());
                    userVO.setUpdateTime(emp.getUpdateTime());
                    return userVO;
                })
                .collect(java.util.stream.Collectors.toList());
        
        // 创建新的Page对象，设置数据
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserVO> userVOPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        userVOPage.setRecords(userVOList);
        userVOPage.setTotal(employeePage.getTotal());
        userVOPage.setCurrent(employeePage.getCurrent());
        userVOPage.setSize(employeePage.getSize());
        userVOPage.setPages(employeePage.getPages());
        
        // 转换为PageResponseDTO
        return PageResponseDTO.fromPage(userVOPage);
    }

    @Override
    protected void clearCache(SysUser entity) {
        // 清除用户相关的缓存
        CacheUtil.delete("user::allUsers");
    }

}
