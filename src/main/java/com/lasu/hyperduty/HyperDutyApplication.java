package com.lasu.hyperduty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;







@SpringBootApplication
@MapperScan({"com.lasu.hyperduty.system.mapper", "com.lasu.hyperduty.duty.mapper", "com.lasu.hyperduty.pm.mapper", "com.lasu.hyperduty.ai.mapper", "com.lasu.hyperduty.common.mapper"})
public class HyperDutyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HyperDutyApplication.class, args);
    }

}