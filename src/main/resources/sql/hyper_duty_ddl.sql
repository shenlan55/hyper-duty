-- ========================================
-- Hyper Duty 值班管理系统 DDL 脚本
-- 包含所有建表语句和表结构修改语句
-- ========================================

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET CHARACTER_SET_DATABASE=utf8mb4;
SET CHARACTER_SET_SERVER=utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hyper_duty DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hyper_duty;

-- ========================================
-- 第一部分：系统基础表
-- ========================================

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
    dict_type_id BIGINT COMMENT '字典类型ID',
    dict_data_id BIGINT COMMENT '字典数据ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_employee_code (employee_code),
    INDEX idx_employee_code (employee_code),
    INDEX idx_dict_type_id (dict_type_id),
    INDEX idx_dict_data_id (dict_data_id),
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

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    path VARCHAR(100) COMMENT '路由路径',
    component VARCHAR(200) COMMENT '组件路径',
    perm VARCHAR(100) COMMENT '权限标识',
    type TINYINT NOT NULL COMMENT '菜单类型：1目录，2菜单，3按钮',
    icon VARCHAR(50) COMMENT '菜单图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(200) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 字典类型表
CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典类型ID',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_code VARCHAR(100) NOT NULL UNIQUE COMMENT '字典编码',
    description VARCHAR(200) COMMENT '字典描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dict_code (dict_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据表
CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '字典数据ID',
    dict_type_id BIGINT NOT NULL COMMENT '字典类型ID',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典键值',
    dict_sort INT DEFAULT 0 COMMENT '字典排序',
    css_class VARCHAR(100) COMMENT '样式属性（其他样式扩展）',
    list_class VARCHAR(100) COMMENT '表格回显样式',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：0否，1是',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dict_type_id (dict_type_id),
    INDEX idx_dict_value (dict_value),
    INDEX idx_status (status),
    FOREIGN KEY (dict_type_id) REFERENCES sys_dict_type(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 排班方式表
CREATE TABLE IF NOT EXISTS duty_schedule_mode (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '排班方式ID',
    mode_name VARCHAR(100) NOT NULL COMMENT '排班方式名称',
    mode_code VARCHAR(50) NOT NULL UNIQUE COMMENT '排班方式编码',
    mode_type TINYINT NOT NULL COMMENT '排班方式类型:1-轮班制,2-综合制,3-弹性制,4-自定义',
    algorithm_class VARCHAR(200) COMMENT '算法实现类全路径',
    config_json JSON COMMENT '配置参数JSON',
    description VARCHAR(500) COMMENT '排班方式描述',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_mode_code (mode_code),
    INDEX idx_mode_type (mode_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班方式表';

-- 值班表
CREATE TABLE IF NOT EXISTS duty_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '值班表ID',
    schedule_name VARCHAR(100) NOT NULL COMMENT '值班表名称',
    description VARCHAR(200) COMMENT '描述',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    schedule_mode_id BIGINT COMMENT '排班方式ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (schedule_mode_id) REFERENCES duty_schedule_mode(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班表';

-- 值班表人员关联表
CREATE TABLE IF NOT EXISTS duty_schedule_employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    schedule_id BIGINT NOT NULL COMMENT '值班表ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    is_leader TINYINT DEFAULT 0 COMMENT '是否值班长：0否，1是',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_schedule_employee (schedule_id, employee_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_is_leader (is_leader),
    FOREIGN KEY (schedule_id) REFERENCES duty_schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班表人员关联表';

-- 值班安排表
CREATE TABLE IF NOT EXISTS duty_assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '值班安排ID',
    schedule_id BIGINT NOT NULL COMMENT '关联值班表ID',
    duty_date DATE NOT NULL COMMENT '值班日期',
    duty_shift INT DEFAULT 1 COMMENT '值班班次：1白班，2晚班，3夜班',
    employee_id BIGINT NOT NULL COMMENT '值班人员ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0取消，1正常',
    remark VARCHAR(200) COMMENT '备注',
    shift_config_id BIGINT COMMENT '班次配置ID',
    version_id BIGINT COMMENT '排班版本ID',
    is_overtime TINYINT DEFAULT 0 COMMENT '是否加班:0-否,1-是',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_shift_config_id (shift_config_id),
    INDEX idx_version_id (version_id),
    FOREIGN KEY (schedule_id) REFERENCES duty_schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE,
    FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config(id) ON DELETE SET NULL,
    FOREIGN KEY (version_id) REFERENCES schedule_version(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班安排表';

-- 值班记录表
CREATE TABLE IF NOT EXISTS duty_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '值班记录ID',
    assignment_id BIGINT NOT NULL COMMENT '关联值班安排ID',
    employee_id BIGINT NOT NULL COMMENT '值班人员ID',
    check_in_time DATETIME COMMENT '签到时间',
    check_out_time DATETIME COMMENT '签退时间',
    duty_status TINYINT DEFAULT 1 COMMENT '值班状态：1正常，2迟到，3早退，4缺勤，5请假',
    check_in_remark VARCHAR(200) COMMENT '签到备注',
    check_out_remark VARCHAR(200) COMMENT '签退备注',
    overtime_hours DECIMAL(5,2) DEFAULT 0 COMMENT '加班时长',
    approval_status VARCHAR(20) DEFAULT 'pending' COMMENT '审批状态：pending待审批，approved已审批，rejected已拒绝',
    manager_remark VARCHAR(200) COMMENT '经理备注',
    substitute_employee_id BIGINT COMMENT '替补人员ID',
    substitute_type TINYINT DEFAULT 1 COMMENT '替补类型:1-自动匹配,2-手动选择',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_substitute_employee_id (substitute_employee_id),
    FOREIGN KEY (assignment_id) REFERENCES duty_assignment(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE,
    FOREIGN KEY (substitute_employee_id) REFERENCES sys_employee(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班记录表';

-- ========================================
-- 第二部分：值班管理扩展表
-- ========================================

-- 班次配置表
CREATE TABLE IF NOT EXISTS duty_shift_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班次配置ID',
    shift_name VARCHAR(50) NOT NULL COMMENT '班次名称',
    shift_code VARCHAR(20) NOT NULL UNIQUE COMMENT '班次编码',
    shift_type TINYINT NOT NULL DEFAULT 1 COMMENT '班次类型:0-白班,1-早班,2-中班,3-晚班,4-全天,5-夜班',
    start_time TIME NOT NULL COMMENT '上班时间',
    end_time TIME NOT NULL COMMENT '下班时间',
    is_cross_day TINYINT DEFAULT 0 COMMENT '是否跨天:0-否,1-是',
    duration_hours DECIMAL(4,2) NOT NULL COMMENT '时长(小时)',
    break_hours DECIMAL(4,2) DEFAULT 0 COMMENT '休息时长(小时)',
    rest_day_rule VARCHAR(100) COMMENT '休息日规则',
    is_overtime_shift TINYINT DEFAULT 0 COMMENT '是否加班班次:0-否,1-是',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    sort INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_shift_code (shift_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次配置表';

-- 排班规则配置表
CREATE TABLE IF NOT EXISTS duty_schedule_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_code VARCHAR(50) NOT NULL UNIQUE COMMENT '规则编码',
    schedule_cycle TINYINT NOT NULL DEFAULT 1 COMMENT '排班周期:1-按周,2-按月,3-按季度',
    max_daily_shifts INT DEFAULT 3 COMMENT '每日最大班次数',
    max_weekly_hours DECIMAL(6,2) DEFAULT 48 COMMENT '每周最大工作时长(小时)',
    max_monthly_hours DECIMAL(6,2) DEFAULT 200 COMMENT '每月最大工作时长(小时)',
    min_rest_days INT DEFAULT 4 COMMENT '每月最少休息天数',
    substitute_priority_rule VARCHAR(200) COMMENT '替补优先级规则',
    conflict_detection_rule VARCHAR(200) COMMENT '冲突检测规则',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_code (rule_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班规则配置表';

-- 人员可排班时段表
CREATE TABLE IF NOT EXISTS employee_available_time (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    day_of_week TINYINT NOT NULL COMMENT '星期:1-周一,2-周二,3-周三,4-周四,5-周五,6-周六,7-周日',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    is_available TINYINT DEFAULT 1 COMMENT '是否可用:0-否,1-是',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_employee_day (employee_id, day_of_week),
    INDEX idx_employee_id (employee_id),
    FOREIGN KEY (employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员可排班时段表';

-- 节假日表
CREATE TABLE IF NOT EXISTS duty_holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节假日ID',
    holiday_name VARCHAR(50) NOT NULL COMMENT '节假日名称',
    holiday_date DATE NOT NULL COMMENT '节假日日期',
    is_workday TINYINT DEFAULT 0 COMMENT '是否调休上班:0-否,1-是',
    holiday_type TINYINT DEFAULT 1 COMMENT '节假日类型:1-法定假日,2-调休,3-公司假日',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_holiday_date (holiday_date),
    INDEX idx_holiday_date (holiday_date),
    INDEX idx_is_workday (is_workday)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日表';

-- 请假申请表
CREATE TABLE IF NOT EXISTS leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '请假申请ID',
    request_no VARCHAR(50) NOT NULL UNIQUE COMMENT '申请编号',
    employee_id BIGINT NOT NULL COMMENT '申请人ID',
    schedule_id BIGINT COMMENT '值班表ID',
    leave_type TINYINT NOT NULL COMMENT '请假类型:1-事假,2-病假,3-年假,4-调休,5-其他',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    start_time TIME COMMENT '开始时间',
    end_time TIME COMMENT '结束时间',
    shift_config_id BIGINT COMMENT '班次配置ID',
    shift_config_ids VARCHAR(500) COMMENT '班次配置ID列表，多个用逗号分隔',
    total_hours DECIMAL(6,2) COMMENT '请假总时长(小时)',
    reason VARCHAR(500) NOT NULL COMMENT '请假原因',
    attachment_url VARCHAR(500) COMMENT '附件URL',
    approval_status VARCHAR(20) DEFAULT 'pending' COMMENT '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    approval_level INT DEFAULT 1 COMMENT '当前审批层级',
    total_approval_levels INT DEFAULT 1 COMMENT '总审批层级',
    substitute_employee_id BIGINT COMMENT '替补人员ID',
    substitute_type TINYINT DEFAULT 1 COMMENT '替补类型:1-自动匹配,2-手动选择',
    substitute_status VARCHAR(20) DEFAULT 'pending' COMMENT '替补状态:pending-待确认,confirmed-已确认,rejected-已拒绝',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    approval_opinion VARCHAR(500) COMMENT '审批意见',
    schedule_completed TINYINT DEFAULT 0 COMMENT '排班是否完成:0-未完成,1-已完成',
    schedule_completed_time DATETIME COMMENT '排班完成时间',
    schedule_completed_by BIGINT COMMENT '排班完成人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_request_no (request_no),
    INDEX idx_employee_id (employee_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_approval_status (approval_status),
    INDEX idx_date_range (start_date, end_date),
    FOREIGN KEY (employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES duty_schedule(id) ON DELETE SET NULL,
    FOREIGN KEY (current_approver_id) REFERENCES sys_employee(id) ON DELETE SET NULL,
    FOREIGN KEY (substitute_employee_id) REFERENCES sys_employee(id) ON DELETE SET NULL,
    FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- 审批记录表
CREATE TABLE IF NOT EXISTS approval_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '审批记录ID',
    request_id BIGINT NOT NULL COMMENT '申请ID',
    request_type VARCHAR(20) NOT NULL COMMENT '申请类型:leave-请假,swap-调班',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approval_level INT NOT NULL COMMENT '审批层级',
    approval_status VARCHAR(20) NOT NULL COMMENT '审批状态:approved-通过,rejected-拒绝',
    approval_opinion VARCHAR(500) COMMENT '审批意见',
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_request_id (request_id),
    INDEX idx_approver_id (approver_id),
    FOREIGN KEY (request_id) REFERENCES leave_request(id) ON DELETE CASCADE,
    FOREIGN KEY (approver_id) REFERENCES sys_employee(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- 调班记录表
CREATE TABLE IF NOT EXISTS swap_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '调班申请ID',
    request_no VARCHAR(50) NOT NULL UNIQUE COMMENT '申请编号',
    original_employee_id BIGINT NOT NULL COMMENT '原值班人员ID',
    target_employee_id BIGINT NOT NULL COMMENT '目标值班人员ID',
    original_assignment_id BIGINT NOT NULL COMMENT '原值班安排ID',
    target_assignment_id BIGINT COMMENT '目标值班安排ID',
    schedule_id BIGINT NOT NULL COMMENT '值班表ID',
    swap_date DATE NOT NULL COMMENT '调班日期',
    swap_shift INT NOT NULL COMMENT '调班班次',
    reason VARCHAR(500) COMMENT '调班原因',
    approval_status VARCHAR(20) DEFAULT 'pending' COMMENT '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消',
    current_approver_id BIGINT COMMENT '当前审批人ID',
    approval_level INT DEFAULT 1 COMMENT '当前审批层级',
    total_approval_levels INT DEFAULT 1 COMMENT '总审批层级',
    original_confirm_status VARCHAR(20) DEFAULT 'pending' COMMENT '原人员确认状态',
    target_confirm_status VARCHAR(20) DEFAULT 'pending' COMMENT '目标人员确认状态',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_request_no (request_no),
    INDEX idx_original_employee (original_employee_id),
    INDEX idx_target_employee (target_employee_id),
    INDEX idx_approval_status (approval_status),
    INDEX idx_swap_date (swap_date),
    INDEX idx_schedule_id (schedule_id),
    FOREIGN KEY (original_employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE,
    FOREIGN KEY (target_employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE,
    FOREIGN KEY (original_assignment_id) REFERENCES duty_assignment(id) ON DELETE CASCADE,
    FOREIGN KEY (target_assignment_id) REFERENCES duty_assignment(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES duty_schedule(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调班记录表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
    operation_desc VARCHAR(500) COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    execution_time INT COMMENT '执行时间(毫秒)',
    status TINYINT DEFAULT 1 COMMENT '状态:0-失败,1-成功',
    error_msg VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_operator_id (operator_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_operation_module (operation_module),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (operator_id) REFERENCES sys_employee(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 排班版本表
CREATE TABLE IF NOT EXISTS schedule_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '版本ID',
    schedule_id BIGINT NOT NULL COMMENT '值班表ID',
    version_name VARCHAR(100) NOT NULL COMMENT '版本名称',
    version_code VARCHAR(50) NOT NULL COMMENT '版本编码',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态:draft-草稿,published-已发布,archived-已归档',
    is_current TINYINT DEFAULT 0 COMMENT '是否当前版本:0-否,1-是',
    create_by BIGINT COMMENT '创建人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    publish_time DATETIME COMMENT '发布时间',
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_version_code (version_code),
    INDEX idx_status (status),
    FOREIGN KEY (schedule_id) REFERENCES duty_schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (create_by) REFERENCES sys_employee(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排班版本表';

-- ========================================
-- 第三部分：表结构扩展（已集成到建表语句中）
-- ========================================

-- 所有表结构扩展已在创建表时集成，无需额外的 ALTER TABLE 语句
-- ========================================
-- 定时任务表
-- ========================================

-- 定时任务表
CREATE TABLE IF NOT EXISTS sys_schedule_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(50) NOT NULL COMMENT '任务分组',
    job_code VARCHAR(100) NOT NULL UNIQUE COMMENT '任务编码',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    bean_name VARCHAR(200) COMMENT 'Bean名称',
    method_name VARCHAR(100) COMMENT '方法名称',
    params TEXT COMMENT '参数',
    status TINYINT DEFAULT 1 COMMENT '状态:0-暂停,1-启用',
    concurrent TINYINT DEFAULT 0 COMMENT '是否允许并发:0-不允许,1-允许',
    description VARCHAR(500) COMMENT '任务描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_job_code (job_code),
    INDEX idx_job_group (job_group),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 定时任务日志表
CREATE TABLE IF NOT EXISTS sys_schedule_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    job_id BIGINT NOT NULL COMMENT '任务ID',
    job_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    job_group VARCHAR(50) NOT NULL COMMENT '任务分组',
    job_code VARCHAR(100) NOT NULL COMMENT '任务编码',
    params TEXT COMMENT '参数',
    status TINYINT DEFAULT 0 COMMENT '执行状态:0-失败,1-成功',
    error_msg TEXT COMMENT '错误信息',
    execute_time BIGINT COMMENT '执行时间(毫秒)',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_job_id (job_id),
    INDEX idx_job_code (job_code),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务日志表';

-- ========================================
-- 班次互斥关系表
-- ========================================

-- 班次互斥关系表
CREATE TABLE IF NOT EXISTS duty_shift_mutex (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    shift_config_id BIGINT NOT NULL COMMENT '班次配置ID',
    mutex_shift_config_id BIGINT NOT NULL COMMENT '互斥班次配置ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_shift_mutex (shift_config_id, mutex_shift_config_id),
    INDEX idx_shift_config_id (shift_config_id),
    INDEX idx_mutex_shift_config_id (mutex_shift_config_id),
    FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config(id) ON DELETE CASCADE,
    FOREIGN KEY (mutex_shift_config_id) REFERENCES duty_shift_config(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次互斥关系表';

-- ========================================
-- 请假顶岗信息表
-- ========================================

-- 请假顶岗信息表
CREATE TABLE IF NOT EXISTS leave_substitute (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    leave_request_id BIGINT NOT NULL COMMENT '请假申请ID',
    original_employee_id BIGINT NOT NULL COMMENT '原值班人员ID',
    substitute_employee_id BIGINT NOT NULL COMMENT '顶岗人员ID',
    duty_date DATE NOT NULL COMMENT '值班日期',
    shift_config_id BIGINT NOT NULL COMMENT '班次配置ID',
    status INT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_leave_request_id (leave_request_id),
    INDEX idx_duty_date (duty_date),
    INDEX idx_shift_config_id (shift_config_id),
    FOREIGN KEY (leave_request_id) REFERENCES leave_request(id) ON DELETE CASCADE,
    FOREIGN KEY (original_employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE,
    FOREIGN KEY (substitute_employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE,
    FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假顶岗信息表';

-- ========================================
-- 值班表班次关联表
-- ========================================

-- 值班表班次关联表
CREATE TABLE IF NOT EXISTS duty_schedule_shift (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    schedule_id BIGINT NOT NULL COMMENT '值班表ID',
    shift_config_id BIGINT NOT NULL COMMENT '班次配置ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_schedule_shift (schedule_id, shift_config_id),
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_shift_config_id (shift_config_id),
    FOREIGN KEY (schedule_id) REFERENCES duty_schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班表班次关联表';

-- ========================================
-- 表结构修改语句
-- ========================================

-- 修改 operation_log 表的 error_msg 列类型为 TEXT，解决数据截断问题
ALTER TABLE operation_log MODIFY COLUMN error_msg TEXT COMMENT '错误信息';

-- ========================================
-- DDL 脚本执行完成
-- ========================================
SELECT 'Hyper Duty DDL 脚本执行完成！共创建25个表' AS message;