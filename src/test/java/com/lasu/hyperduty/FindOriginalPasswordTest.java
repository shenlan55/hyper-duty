package com.lasu.hyperduty;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class FindOriginalPasswordTest {

    @Test
    public void findOriginalPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String originalHash = "$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2";
        
        // 常见的默认密码列表
        String[] commonPasswords = {
            "admin",
            "password",
            "123456",
            "admin123",
            "password123",
            "12345678",
            "123123",
            "111111",
            "qwerty",
            "abc123"
        };
        
        System.out.println("Testing common passwords against hash: " + originalHash);
        System.out.println("===========================================");
        
        for (String password : commonPasswords) {
            boolean matches = encoder.matches(password, originalHash);
            System.out.printf("Password '%s' %s%n", password, matches ? "✓ MATCHES" : "✗ doesn't match");
            if (matches) {
                System.out.println("===========================================");
                System.out.println("Found matching password: " + password);
                return;
            }
        }
        
        System.out.println("===========================================");
        System.out.println("No matching common password found.");
    }
}
