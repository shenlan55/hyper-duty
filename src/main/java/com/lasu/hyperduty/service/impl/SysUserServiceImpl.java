package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.dto.UserVO;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.entity.SysUser;
import com.lasu.hyperduty.mapper.SysUserMapper;
import com.lasu.hyperduty.service.SysEmployeeService;
import com.lasu.hyperduty.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectByUsername(username);
    }

    @Override
    public List<UserVO> getAllUsers() {
        // 获取所有用户
        List<SysUser> sysUsers = list();
        // 获取所有人员，用于关联查询
        List<SysEmployee> employees = sysEmployeeService.getAllEmployees();
        
        // 转换为UserVO列表
        return sysUsers.stream().map(user -> {
            UserVO userVO = new UserVO();
            // 复制用户基本信息
            userVO.setId(user.getId());
            userVO.setUsername(user.getUsername());
            userVO.setPassword(user.getPassword());
            userVO.setEmployeeId(user.getEmployeeId());
            userVO.setStatus(user.getStatus());
            userVO.setCreateTime(user.getCreateTime());
            userVO.setUpdateTime(user.getUpdateTime());
            
            // 关联查询人员姓名
            if (user.getEmployeeId() != null) {
                employees.stream()
                        .filter(emp -> emp.getId().equals(user.getEmployeeId()))
                        .findFirst()
                        .ifPresent(emp -> userVO.setEmployeeName(emp.getEmployeeName()));
            }
            
            return userVO;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean save(SysUser sysUser) {
        // 加密密码
        if (sysUser.getPassword() != null && !sysUser.getPassword().startsWith("$2a$")) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        }
        return super.save(sysUser);
    }

    @Override
    public boolean updateById(SysUser sysUser) {
        // 如果密码不为空且不是加密格式，则加密密码
        if (sysUser.getPassword() != null && !sysUser.getPassword().startsWith("$2a$")) {
            sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        } else if (sysUser.getPassword() != null && sysUser.getPassword().startsWith("$2a$")) {
            // 如果是加密格式，不做处理
        } else {
            // 如果密码为空，不更新密码
            sysUser.setPassword(null);
        }
        return super.updateById(sysUser);
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

        // 这里可以添加角色和权限设置，暂时只返回用户基本信息
        return new User(sysUser.getUsername(), sysUser.getPassword(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

}