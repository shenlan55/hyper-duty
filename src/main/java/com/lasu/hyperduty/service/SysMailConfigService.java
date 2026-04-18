package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysMailConfig;

/**
 * 邮件服务配置Service接口
 */
public interface SysMailConfigService extends IService<SysMailConfig> {

    /**
     * 获取当前激活的邮件配置
     */
    SysMailConfig getActiveConfig();

    /**
     * 保存或更新邮件配置
     */
    boolean saveOrUpdateConfig(SysMailConfig config);

    /**
     * 发送验证码邮件
     */
    boolean sendVerificationCode(String email, String code);

    /**
     * 验证验证码
     */
    boolean verifyCode(String email, String code);

    /**
     * 测试邮件连接
     */
    boolean testConnection(SysMailConfig config);
}
