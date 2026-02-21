package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.dto.PageRequestDTO;
import com.lasu.hyperduty.dto.PageResponseDTO;
import com.lasu.hyperduty.entity.SysEmployee;

import java.util.List;
import java.util.Map;

public interface SysEmployeeService extends BasePageService<SysEmployee> {

    List<SysEmployee> getAllEmployees();

    List<SysEmployee> getEmployeesByDeptId(Long deptId);

    Page<SysEmployee> page(Page<SysEmployee> page, String keyword, Long deptId);

    @Override
    PageResponseDTO<SysEmployee> page(PageRequestDTO pageRequestDTO, Map<String, Object> params);

}