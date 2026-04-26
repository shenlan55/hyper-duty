package com.lasu.hyperduty.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.system.entity.SysDictData;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;








@Data
@TableName("sys_dict_data")
public class SysDictData implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long dictTypeId;

    private String dictLabel;

    private String dictValue;

    private Integer dictSort;

    private String cssClass;

    private String listClass;

    private Integer isDefault;

    private Integer status;

    private String remark;

    private Date createTime;

    private Date updateTime;
}