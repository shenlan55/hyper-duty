package com.lasu.hyperduty.pm.dto;

import com.lasu.hyperduty.pm.dto.TaskCreateDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Data;








/**
 * 任务创建 DTO
 */
@Data
public class TaskCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "所属项目不能为空")
    private Long projectId;

    private Long parentId;

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private Integer progress;

    @NotNull(message = "负责人不能为空")
    private Long assigneeId;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private String description;

    private String attachments;

    private Integer isFocus;

    private String stakeholders;
}
