package com.lasu.hyperduty.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseCharsetTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/hyper_duty?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8";
        String username = "root";
        String password = "123456";

        try {
            // 加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            try (Connection conn = DriverManager.getConnection(url, username, password);
                 Statement stmt = conn.createStatement()) {

                // 测试角色表
                System.out.println("=== 角色表数据 ===");
                ResultSet rs1 = stmt.executeQuery("SELECT * FROM sys_role");
                while (rs1.next()) {
                    System.out.println("角色ID: " + rs1.getLong("id") + ", 角色名称: " + rs1.getString("role_name") + ", 角色描述: " + rs1.getString("description"));
                }

                // 测试菜单表
                System.out.println("\n=== 菜单表数据 ===");
                ResultSet rs2 = stmt.executeQuery("SELECT * FROM sys_menu");
                while (rs2.next()) {
                    System.out.println("菜单ID: " + rs2.getLong("id") + ", 菜单名称: " + rs2.getString("menu_name") + ", 路由路径: " + rs2.getString("path"));
                }

                // 测试部门表
                System.out.println("\n=== 部门表数据 ===");
                ResultSet rs3 = stmt.executeQuery("SELECT * FROM sys_dept");
                while (rs3.next()) {
                    System.out.println("部门ID: " + rs3.getLong("id") + ", 部门名称: " + rs3.getString("dept_name") + ", 部门编码: " + rs3.getString("dept_code"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}