package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.dto.LoginDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testLogin() {
        // 创建登录请求体
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("123456");

        // 创建HTTP头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // 创建HTTP实体
        HttpEntity<LoginDTO> entity = new HttpEntity<>(loginDTO, headers);

        // 发送登录请求
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                entity,
                String.class
        );

        // 打印响应状态码和响应体
        System.out.println("Response Status Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        // 验证响应状态码
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
