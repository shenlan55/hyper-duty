package com.lasu.hyperduty.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.system.entity.SysDept;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;








@Data
@TableName("sys_dept")
public class SysDept implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String deptName;

    private Long parentId;

    private String deptCode;

    private Integer sort;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // 子部门列表（非数据库字段）
    @TableField(exist = false)
    private List<SysDept> children;

}