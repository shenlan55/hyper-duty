package com.lasu.hyperduty.pm.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量创建任务 DTO
 */
@Data
public class BatchTaskCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "任务列表不能为空")
    @Valid
    private List<TaskCreateDTO> tasks;
}
