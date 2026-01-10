package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.lasu.hyperduty.dto.UserVO;
import java.util.List;

public interface SysUserService extends IService<SysUser>, UserDetailsService {

    SysUser getByUsername(String username);

    List<UserVO> getAllUsers();

}