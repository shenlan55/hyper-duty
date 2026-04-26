package com.lasu.hyperduty.duty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.duty.entity.DutyScheduleEmployee;
import com.lasu.hyperduty.duty.mapper.DutyScheduleEmployeeMapper;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;








@Mapper
public interface DutyScheduleEmployeeMapper extends BaseMapper<DutyScheduleEmployee> {

    List<Long> getEmployeeIdsByScheduleId(Long scheduleId);

    List<DutyScheduleEmployee> getEmployeesByScheduleId(Long scheduleId);

    List<Map<String, Object>> getScheduleEmployeesWithDetails(Long scheduleId);

    int deleteByScheduleId(Long scheduleId);
}
