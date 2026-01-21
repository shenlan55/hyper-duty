package com.lasu.hyperduty.service.algorithm;

import com.lasu.hyperduty.entity.DutyAssignment;
import com.lasu.hyperduty.entity.DutySchedule;
import com.lasu.hyperduty.entity.SysEmployee;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 排班算法接口
 * 所有排班算法都需要实现此接口
 */
public interface ScheduleAlgorithm {
    
    /**
     * 生成排班安排
     * @param schedule 值班表信息
     * @param employees 参与排班的员工列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param configParams 配置参数
     * @return 生成的排班安排列表
     */
    List<DutyAssignment> generateSchedule(DutySchedule schedule, List<SysEmployee> employees, 
                                         LocalDate startDate, LocalDate endDate, Map<String, Object> configParams);
    
    /**
     * 获取算法名称
     * @return 算法名称
     */
    String getAlgorithmName();
    
    /**
     * 获取算法描述
     * @return 算法描述
     */
    String getAlgorithmDescription();
    
    /**
     * 获取算法支持的配置参数
     * @return 配置参数说明
     */
    List<AlgorithmParam> getSupportedParams();
}