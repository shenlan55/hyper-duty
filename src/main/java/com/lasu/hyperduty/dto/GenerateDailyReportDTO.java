package com.lasu.hyperduty.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 生成日报请求DTO
 */
@Data
public class GenerateDailyReportDTO {

    @NotNull(message = "报告日期不能为空")
    private LocalDate reportDate;

    private List<Long> projectIds;

    private Long configId;

    private Long employeeId;
}
