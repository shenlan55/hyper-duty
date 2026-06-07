package com.lasu.hyperduty.score.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.score.entity.ScoreSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 积分汇总 Mapper
 */
@Mapper
public interface ScoreSummaryMapper extends BaseMapper<ScoreSummary> {

    /**
     * 查询某员工在指定期间的工时汇总（从 duty_record 表）
     */
    @Select("SELECT COALESCE(SUM(overtime_hours), 0) FROM duty_record WHERE employee_id = #{employeeId} AND EXTRACT(YEAR FROM duty_date) = #{year} AND EXTRACT(MONTH FROM duty_date) = #{month}")
    java.math.BigDecimal sumOvertimeHoursByEmployeeAndMonth(@Param("employeeId") Long employeeId, @Param("year") Integer year, @Param("month") Integer month);
}