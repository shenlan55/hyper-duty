package com.lasu.hyperduty.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;







@Data
@Configuration
@ConfigurationProperties(prefix = "rustfs")
public class RustFSConfig {
    
    private String endpoint;
    
    private String accessKey;
    
    private String secretKey;
    
    private String bucketName;
    
    private String region = "us-east-1";
}
