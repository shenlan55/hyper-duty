package com.lasu.hyperduty;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lasu.hyperduty.mapper")
public class HyperDutyApplication {

    public static void main(String[] args) {
        SpringApplication.run(HyperDutyApplication.class, args);
    }

}