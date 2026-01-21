package com.lasu.hyperduty.service.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算法参数类
 * 定义算法支持的配置参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlgorithmParam {
    
    /**
     * 参数名称
     */
    private String paramName;
    
    /**
     * 参数代码
     */
    private String paramCode;
    
    /**
     * 参数类型
     */
    private String paramType;
    
    /**
     * 是否必填
     */
    private boolean required;
    
    /**
     * 参数描述
     */
    private String description;
    
    /**
     * 默认值
     */
    private Object defaultValue;
}