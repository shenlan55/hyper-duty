package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.EmployeeAvailableTime;
import com.lasu.hyperduty.mapper.EmployeeAvailableTimeMapper;
import com.lasu.hyperduty.service.EmployeeAvailableTimeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeAvailableTimeServiceImpl extends ServiceImpl<EmployeeAvailableTimeMapper, EmployeeAvailableTime> implements EmployeeAvailableTimeService {
}
