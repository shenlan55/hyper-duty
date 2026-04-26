package com.lasu.hyperduty.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.system.entity.SysDictType;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;








@Data
@TableName("sys_dict_type")
public class SysDictType implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String dictName;

    private String dictCode;

    private String description;

    private Integer status;

    private Date createTime;

    private Date updateTime;
}