package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.lasu.hyperduty.dto.UserVO;
import com.lasu.hyperduty.dto.PageRequestDTO;
import com.lasu.hyperduty.dto.PageResponseDTO;
import java.util.List;
import java.util.Map;

public interface SysUserService extends BasePageService<SysUser>, UserDetailsService {

    SysUser getByUsername(String username);

    List<UserVO> getAllUsers();

    @Override
    PageResponseDTO<SysUser> page(PageRequestDTO pageRequestDTO, Map<String, Object> params);

    PageResponseDTO<UserVO> pageUserVO(PageRequestDTO pageRequestDTO, Map<String, Object> params);

}