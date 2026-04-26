package com.lasu.hyperduty.pm.dto;

import com.lasu.hyperduty.pm.dto.TaskQueryDTO;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import lombok.Data;








/**
 * 任务查询条件 DTO
 */
@Data
public class TaskQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    @Min(value = 1, message = "每页条数必须大于0")
    private Integer pageSize = 10;

    private Long projectId;

    private Long assigneeId;

    private Integer status;

    private Integer priority;

    private String taskName;

    private String assigneeName;
}
