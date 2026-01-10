-- 创建数据库
CREATE DATABASE IF NOT EXISTS hyper_duty DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hyper_duty;

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '上级部门ID',
    dept_code VARCHAR(20) NOT NULL COMMENT '部门编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_dept_code (dept_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 人员表
CREATE TABLE IF NOT EXISTS sys_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '人员ID',
    employee_name VARCHAR(50) NOT NULL COMMENT '人员姓名',
    dept_id BIGINT NOT NULL COMMENT '所属部门ID',
    employee_code VARCHAR(20) COMMENT '人员编码',
    phone VARCHAR(11) COMMENT '手机号码',
    email VARCHAR(50) COMMENT '邮箱',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知，1男，2女',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_employee_code (employee_code),
    INDEX idx_employee_code (employee_code),
    FOREIGN KEY (dept_id) REFERENCES sys_dept(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员表';

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    employee_id BIGINT NOT NULL COMMENT '关联人员ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_employee_id (employee_id),
    FOREIGN KEY (employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化数据
-- 插入默认部门
INSERT INTO sys_dept (dept_name, parent_id, dept_code, sort, status) VALUES
('总公司', 0, '001', 1, 1),
('技术部', 1, '002', 2, 1),
('人事部', 1, '003', 3, 1),
('财务部', 1, '004', 4, 1);

-- 插入默认人员
INSERT INTO sys_employee (employee_name, dept_id, employee_code, phone, email, gender, status) VALUES
('管理员', 1, 'EMP001', '13800138000', 'admin@example.com', 1, 1),
('张三', 2, 'EMP002', '13800138001', 'zhangsan@example.com', 1, 1),
('李四', 2, 'EMP003', '13800138002', 'lisi@example.com', 1, 1),
('王五', 3, 'EMP004', '13800138003', 'wangwu@example.com', 2, 1);

-- 插入默认用户，密码为123456（BCrypt加密）
INSERT INTO sys_user (username, password, employee_id, status) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, 1),
('zhangsan', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 2, 1),
('lisi', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 3, 1),
('wangwu', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 4, 1);
