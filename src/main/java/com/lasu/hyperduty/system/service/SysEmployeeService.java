package com.lasu.hyperduty.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.dto.PageRequestDTO;
import com.lasu.hyperduty.common.dto.PageResponseDTO;
import com.lasu.hyperduty.common.service.BasePageService;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import java.util.List;
import java.util.Map;









public interface SysEmployeeService extends BasePageService<SysEmployee> {

    List<SysEmployee> getAllEmployees();

    List<SysEmployee> getEmployeesByDeptId(Long deptId);

    Page<SysEmployee> page(
            Page<SysEmployee> page, 
            String keyword, 
            Long deptId);

    @Override
    PageResponseDTO<SysEmployee> page(PageRequestDTO pageRequestDTO, Map<String, Object> params);

}