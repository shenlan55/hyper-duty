package com.lasu.hyperduty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kkfileview")
public class KKFileViewConfig {
    
    private String endpoint;
    
    private String previewPath;
}