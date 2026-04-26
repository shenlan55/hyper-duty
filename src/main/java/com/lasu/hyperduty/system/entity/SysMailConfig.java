package com.lasu.hyperduty.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lasu.hyperduty.system.entity.SysMailConfig;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;








/**
 * 邮件服务配置实体类
 */
@Data
@TableName("sys_mail_config")
public class SysMailConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * SMTP服务器地址
     */
    private String smtpHost;

    /**
     * SMTP端口
     */
    private Integer smtpPort;

    /**
     * 是否启用SSL：0否，1是
     */
    private Integer enableSsl;

    /**
     * 是否启用TLS：0否，1是
     */
    private Integer enableTls;

    /**
     * 发件人邮箱
     */
    private String fromEmail;

    /**
     * 发件人名称
     */
    private String fromName;

    /**
     * 授权码/密码
     */
    private String authPassword;

    /**
     * 登录验证码模板
     */
    private String loginCodeTemplate;

    /**
     * 密码找回模板
     */
    private String passwordResetTemplate;

    /**
     * 异地登录提醒模板
     */
    private String remoteLoginTemplate;

    /**
     * 是否启用邮件验证码登录：0否，1是
     */
    private Integer enableEmailLogin;

    /**
     * 验证码有效期（分钟）
     */
    private Integer codeExpireMinutes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
