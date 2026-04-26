package com.lasu.hyperduty.ai.dto;

import com.lasu.hyperduty.ai.dto.GenerateDailyReportDTO;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;









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
