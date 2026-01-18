package com.lasu.hyperduty.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long operatorId;

    private String operatorName;

    private String operationType;

    private String operationModule;

    private String operationDesc;

    private String requestMethod;

    private String requestUrl;

    private String requestParams;

    private String responseResult;

    private String ipAddress;

    private String userAgent;

    private Integer executionTime;

    private Integer status;

    private String errorMsg;

    private LocalDateTime createTime;

}
