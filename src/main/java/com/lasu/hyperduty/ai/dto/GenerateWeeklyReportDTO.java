package com.lasu.hyperduty.ai.dto;

import com.lasu.hyperduty.ai.dto.GenerateWeeklyReportDTO;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;









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
