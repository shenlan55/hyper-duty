package com.lasu.hyperduty.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "frontend")
public class FrontendConfig {
    
    private String url;
}
