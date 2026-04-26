package com.lasu.hyperduty.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.system.entity.SysMailConfig;
import com.lasu.hyperduty.system.mapper.SysMailConfigMapper;
import com.lasu.hyperduty.system.service.SysMailConfigService;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.Properties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;








/**
 * 邮件服务配置Service实现类
 */
@Service
public class SysMailConfigServiceImpl extends ServiceImpl<SysMailConfigMapper, SysMailConfig> implements SysMailConfigService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String VERIFICATION_CODE_PREFIX = "email:verification:";
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SysMailConfigServiceImpl.class);

    @Override
    public SysMailConfig getActiveConfig() {
        // 获取最新的配置
        return lambdaQuery()
                .orderByDesc(SysMailConfig::getCreateTime)
                .last("LIMIT 1")
                .one();
    }

    @Override
    public boolean saveOrUpdateConfig(SysMailConfig config) {
        if (config.getId() == null) {
            config.setCreateTime(LocalDateTime.now());
        }
        config.setUpdateTime(LocalDateTime.now());
        return saveOrUpdate(config);
    }

    @Override
    public boolean sendVerificationCode(String email, String code) {
        SysMailConfig config = getActiveConfig();
        if (config == null) {
            return false;
        }

        try {
            JavaMailSender mailSender = createMailSender(config);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(config.getFromEmail(), config.getFromName() != null ? config.getFromName() : "Hyper Duty System");
            helper.setTo(email);
            helper.setSubject("登录验证码");

            String content = config.getLoginCodeTemplate();
            if (content == null || content.isEmpty()) {
                content = "您的登录验证码是：{code}，{expire}分钟内有效。";
            }
            content = content.replace("{code}", code)
                    .replace("{expire}", String.valueOf(config.getCodeExpireMinutes() != null ? config.getCodeExpireMinutes() : 5));

            helper.setText(content, true);

            mailSender.send(message);

            // 保存验证码到Redis
            int expireMinutes = config.getCodeExpireMinutes() != null ? config.getCodeExpireMinutes() : 5;
            redisTemplate.opsForValue().set(VERIFICATION_CODE_PREFIX + email, code, expireMinutes, TimeUnit.MINUTES);

            return true;
        } catch (Exception e) {
            log.error("发送验证码邮件失败", e);
            return false;
        }
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get(VERIFICATION_CODE_PREFIX + email);
        if (storedCode != null && storedCode.equals(code)) {
            // 验证成功后删除验证码
            redisTemplate.delete(VERIFICATION_CODE_PREFIX + email);
            return true;
        }
        return false;
    }

    @Override
    public boolean testConnection(SysMailConfig config) {
        try {
            JavaMailSender mailSender = createMailSender(config);
            // 简单测试：尝试连接服务器，不需要真正发送邮件
            // JavaMailSenderImpl没有直接的连接测试方法，我们可以通过发送一封测试邮件给自己
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(config.getFromEmail(), config.getFromName() != null ? config.getFromName() : "Hyper Duty System");
            helper.setTo(config.getFromEmail());
            helper.setSubject("邮件连接测试");
            helper.setText("这是一封测试邮件，用于验证邮件服务器配置是否正确。", true);

            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("邮件连接测试失败", e);
            return false;
        }
    }

    private JavaMailSender createMailSender(SysMailConfig config) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getSmtpHost());
        mailSender.setPort(config.getSmtpPort());
        mailSender.setUsername(config.getFromEmail());
        mailSender.setPassword(config.getAuthPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        if (config.getEnableSsl() != null && config.getEnableSsl() == 1) {
            props.put("mail.smtp.ssl.enable", "true");
        }
        if (config.getEnableTls() != null && config.getEnableTls() == 1) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        props.put("mail.debug", "false");

        return mailSender;
    }
}
