package com.lasu.hyperduty.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 生成周报请求DTO
 */
@Data
public class GenerateWeeklyReportDTO {

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private List<Long> projectIds;

    private Long configId;

    private Long employeeId;
}
