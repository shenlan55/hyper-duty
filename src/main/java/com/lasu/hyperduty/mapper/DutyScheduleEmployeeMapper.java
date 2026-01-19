package com.lasu.hyperduty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.entity.DutyScheduleEmployee;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DutyScheduleEmployeeMapper extends BaseMapper<DutyScheduleEmployee> {

    List<Long> getEmployeeIdsByScheduleId(Long scheduleId);

    List<DutyScheduleEmployee> getEmployeesByScheduleId(Long scheduleId);

    int deleteByScheduleId(Long scheduleId);
}
