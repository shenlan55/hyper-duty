package com.lasu.hyperduty.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.mapper.SysEmployeeMapper;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;








/**
 * 用户详情服务实现类
 * 使用SysEmployeeService来获取用户信息
 */
@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    private final SysEmployeeService sysEmployeeService;
    private final SysEmployeeMapper sysEmployeeMapper;

    @Autowired
    public EmployeeUserDetailsService(SysEmployeeService sysEmployeeService, SysEmployeeMapper sysEmployeeMapper) {
        this.sysEmployeeService = sysEmployeeService;
        this.sysEmployeeMapper = sysEmployeeMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从SysEmployee表中查询用户信息
        LambdaQueryWrapper<SysEmployee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysEmployee::getUsername, username);
        SysEmployee employee = sysEmployeeService.getOne(queryWrapper);

        if (employee == null) {
            throw new UsernameNotFoundException("用户名不存在");
        }

        // 检查用户状态
        if (employee.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已禁用");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        try {
            // 获取用户角色编码
            List<String> roleCodes = sysEmployeeMapper.selectRoleCodesByUserId(employee.getId());
            // 获取用户权限标识
            List<String> perms = sysEmployeeMapper.selectPermsByUserId(employee.getId());

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

        return new User(employee.getUsername(), employee.getPassword(), authorities);
    }
}
