package com.lasu.hyperduty.pm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.pm.entity.PmProjectEmployee;
import java.time.LocalDateTime;
import lombok.Data;








@Data
@TableName("pm_project_employee")
public class PmProjectEmployee {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long employeeId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
