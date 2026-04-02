package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pm_project_deputy_owner")
public class PmProjectDeputyOwner {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long employeeId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
