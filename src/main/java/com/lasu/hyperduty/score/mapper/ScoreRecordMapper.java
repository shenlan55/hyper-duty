package com.lasu.hyperduty.score.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.score.entity.ScoreRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 积分记录 Mapper
 */
@Mapper
public interface ScoreRecordMapper extends BaseMapper<ScoreRecord> {

    /**
     * 按员工和月份汇总积分
     */
    @Select("SELECT COALESCE(SUM(score), 0) FROM score_record WHERE employee_id = #{employeeId} AND period_year = #{year} AND period_month = #{month}")
    Integer sumScoreByEmployeeAndMonth(@Param("employeeId") Long employeeId, @Param("year") Integer year, @Param("month") Integer month);
}