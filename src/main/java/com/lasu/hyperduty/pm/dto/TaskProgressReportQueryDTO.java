package com.lasu.hyperduty.pm.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * 任务进展报告查询条件 DTO
 */
@Data
public class TaskProgressReportQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID列表
     */
    private List<Long> projectIds;

    /**
     * 任务开始时间范围 - 开始
     */
    private LocalDate taskStartDateFrom;

    /**
     * 任务开始时间范围 - 结束
     */
    private LocalDate taskStartDateTo;

    /**
     * 任务结束时间范围 - 开始
     */
    private LocalDate taskEndDateFrom;

    /**
     * 任务结束时间范围 - 结束
     */
    private LocalDate taskEndDateTo;

    /**
     * 进展更新时间范围 - 开始
     */
    private LocalDateTime progressUpdateTimeFrom;

    /**
     * 进展更新时间范围 - 结束
     */
    private LocalDateTime progressUpdateTimeTo;
}
