-- PostgreSQL dump for Hyper Duty system
-- Generated from MySQL dump on 2026-02-22

-- Create database if not exists
-- CREATE DATABASE IF NOT EXISTS hyper_duty;

-- Connect to database
-- \c hyper_duty;

-- Create extension for uuid-ossp if not exists
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET TIMEZONE='Asia/Shanghai';

-- =============================================
-- 基础表（无依赖）
-- =============================================

--
-- Table structure for table `sys_dept`
--

DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
  id BIGSERIAL PRIMARY KEY,
  dept_name VARCHAR(50) NOT NULL,
  parent_id BIGINT DEFAULT '0',
  dept_code VARCHAR(20) NOT NULL,
  sort INT DEFAULT '0',
  status SMALLINT DEFAULT '1',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE sys_dept IS '部门表';
COMMENT ON COLUMN sys_dept.id IS '部门ID';
COMMENT ON COLUMN sys_dept.dept_name IS '部门名称';
COMMENT ON COLUMN sys_dept.parent_id IS '上级部门ID';
COMMENT ON COLUMN sys_dept.dept_code IS '部门编码';
COMMENT ON COLUMN sys_dept.sort IS '排序';
COMMENT ON COLUMN sys_dept.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN sys_dept.create_time IS '创建时间';
COMMENT ON COLUMN sys_dept.update_time IS '更新时间';

-- Create indexes for sys_dept
CREATE UNIQUE INDEX uk_dept_code ON sys_dept (dept_code);

--
-- Table structure for table `sys_dict_type`
--

DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
  id BIGSERIAL PRIMARY KEY,
  dict_name VARCHAR(100) NOT NULL,
  dict_code VARCHAR(100) NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE sys_dict_type IS '字典类型表';
COMMENT ON COLUMN sys_dict_type.id IS '字典类型ID';
COMMENT ON COLUMN sys_dict_type.dict_name IS '字典名称';
COMMENT ON COLUMN sys_dict_type.dict_code IS '字典编码';
COMMENT ON COLUMN sys_dict_type.description IS '字典描述';
COMMENT ON COLUMN sys_dict_type.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN sys_dict_type.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_type.update_time IS '更新时间';

-- Create indexes for sys_dict_type
CREATE UNIQUE INDEX uk_dict_code ON sys_dict_type (dict_code);
CREATE INDEX idx_dict_code ON sys_dict_type (dict_code);
CREATE INDEX idx_status ON sys_dict_type (status);

-- Insert data for sys_dict_type
INSERT INTO sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time)
VALUES (1, '职位--江苏移动统维', 'Position', '', 1, '2026-01-18 06:51:26', '2026-01-18 06:51:26'),
       (2, '事项类型', 'ItemType', '', 1, '2026-02-20 11:26:29', '2026-02-20 11:26:29');

-- Reset sequence value for sys_dict_type
SELECT setval('sys_dict_type_id_seq', 3);

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
  id BIGSERIAL PRIMARY KEY,
  menu_name VARCHAR(50) NOT NULL,
  parent_id BIGINT DEFAULT '0',
  path VARCHAR(200) DEFAULT NULL,
  component VARCHAR(200) DEFAULT NULL,
  perm VARCHAR(100) DEFAULT NULL,
  type SMALLINT NOT NULL,
  icon VARCHAR(50) DEFAULT NULL,
  sort INT DEFAULT '0',
  status SMALLINT DEFAULT '1',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.id IS '菜单ID';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.perm IS '权限标识';
COMMENT ON COLUMN sys_menu.type IS '菜单类型：1目录，2菜单，3按钮';
COMMENT ON COLUMN sys_menu.icon IS '菜单图标';
COMMENT ON COLUMN sys_menu.sort IS '排序';
COMMENT ON COLUMN sys_menu.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN sys_menu.create_time IS '创建时间';
COMMENT ON COLUMN sys_menu.update_time IS '更新时间';

-- Create indexes for sys_menu
CREATE INDEX idx_parent_id ON sys_menu (parent_id);
CREATE INDEX idx_type ON sys_menu (type);
CREATE INDEX idx_status ON sys_menu (status);

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
  id BIGSERIAL PRIMARY KEY,
  role_name VARCHAR(50) NOT NULL,
  role_code VARCHAR(50) NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.id IS '角色ID';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_code IS '角色编码';
COMMENT ON COLUMN sys_role.description IS '角色描述';
COMMENT ON COLUMN sys_role.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN sys_role.create_time IS '创建时间';
COMMENT ON COLUMN sys_role.update_time IS '更新时间';

-- Create indexes for sys_role
CREATE UNIQUE INDEX uk_role_code ON sys_role (role_code);

--
-- Table structure for table `duty_schedule_mode`
--

DROP TABLE IF EXISTS duty_schedule_mode;
CREATE TABLE duty_schedule_mode (
  id BIGSERIAL PRIMARY KEY,
  mode_name VARCHAR(100) NOT NULL,
  mode_code VARCHAR(50) NOT NULL,
  mode_type SMALLINT NOT NULL,
  algorithm_class VARCHAR(200) DEFAULT NULL,
  config_json JSON DEFAULT NULL,
  description VARCHAR(500) DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  sort INT DEFAULT '0',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE duty_schedule_mode IS '排班方式表';
COMMENT ON COLUMN duty_schedule_mode.id IS '排班方式ID';
COMMENT ON COLUMN duty_schedule_mode.mode_name IS '排班方式名称';
COMMENT ON COLUMN duty_schedule_mode.mode_code IS '排班方式编码';
COMMENT ON COLUMN duty_schedule_mode.mode_type IS '排班方式类型:1-轮班制,2-综合制,3-弹性制,4-自定义';
COMMENT ON COLUMN duty_schedule_mode.algorithm_class IS '算法实现类全路径';
COMMENT ON COLUMN duty_schedule_mode.config_json IS '配置参数JSON';
COMMENT ON COLUMN duty_schedule_mode.description IS '排班方式描述';
COMMENT ON COLUMN duty_schedule_mode.status IS '状态:0-禁用,1-启用';
COMMENT ON COLUMN duty_schedule_mode.sort IS '排序';
COMMENT ON COLUMN duty_schedule_mode.create_time IS '创建时间';
COMMENT ON COLUMN duty_schedule_mode.update_time IS '更新时间';

-- Create indexes for duty_schedule_mode
CREATE UNIQUE INDEX mode_code ON duty_schedule_mode (mode_code);
CREATE INDEX idx_mode_code ON duty_schedule_mode (mode_code);
CREATE INDEX idx_mode_type ON duty_schedule_mode (mode_type);
CREATE INDEX idx_status ON duty_schedule_mode (status);



--
-- Table structure for table `duty_schedule_rule`
--

DROP TABLE IF EXISTS duty_schedule_rule;
CREATE TABLE duty_schedule_rule (
  id BIGSERIAL PRIMARY KEY,
  rule_name VARCHAR(100) NOT NULL,
  rule_code VARCHAR(50) NOT NULL,
  schedule_cycle SMALLINT NOT NULL DEFAULT '1',
  max_daily_shifts INT DEFAULT '3',
  max_weekly_hours DECIMAL(6,2) DEFAULT '48.00',
  max_monthly_hours DECIMAL(6,2) DEFAULT '200.00',
  min_rest_days INT DEFAULT '4',
  substitute_priority_rule VARCHAR(200) DEFAULT NULL,
  conflict_detection_rule VARCHAR(200) DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE duty_schedule_rule IS '排班规则配置表';
COMMENT ON COLUMN duty_schedule_rule.id IS '规则ID';
COMMENT ON COLUMN duty_schedule_rule.rule_name IS '规则名称';
COMMENT ON COLUMN duty_schedule_rule.rule_code IS '规则编码';
COMMENT ON COLUMN duty_schedule_rule.schedule_cycle IS '排班周期:1-按周,2-按月,3-按季度';
COMMENT ON COLUMN duty_schedule_rule.max_daily_shifts IS '每日最大班次数';
COMMENT ON COLUMN duty_schedule_rule.max_weekly_hours IS '每周最大工作时长(小时)';
COMMENT ON COLUMN duty_schedule_rule.max_monthly_hours IS '每月最大工作时长(小时)';
COMMENT ON COLUMN duty_schedule_rule.min_rest_days IS '每月最少休息天数';
COMMENT ON COLUMN duty_schedule_rule.substitute_priority_rule IS '替补优先级规则';
COMMENT ON COLUMN duty_schedule_rule.conflict_detection_rule IS '冲突检测规则';
COMMENT ON COLUMN duty_schedule_rule.status IS '状态:0-禁用,1-启用';
COMMENT ON COLUMN duty_schedule_rule.create_time IS '创建时间';
COMMENT ON COLUMN duty_schedule_rule.update_time IS '更新时间';

-- Create indexes for duty_schedule_rule
CREATE UNIQUE INDEX rule_code ON duty_schedule_rule (rule_code);
CREATE INDEX idx_rule_code ON duty_schedule_rule (rule_code);
CREATE INDEX idx_status ON duty_schedule_rule (status);

--
-- Table structure for table `duty_shift_config`
--

DROP TABLE IF EXISTS duty_shift_config;
CREATE TABLE duty_shift_config (
  id BIGSERIAL PRIMARY KEY,
  shift_name VARCHAR(50) NOT NULL,
  shift_code VARCHAR(20) NOT NULL,
  shift_type SMALLINT NOT NULL DEFAULT '1',
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  duration_hours DECIMAL(4,2) NOT NULL,
  break_hours DECIMAL(4,2) DEFAULT '0.00',
  rest_day_rule VARCHAR(100) DEFAULT NULL,
  is_overtime_shift SMALLINT DEFAULT '0',
  status SMALLINT DEFAULT '1',
  sort INT DEFAULT '0',
  remark VARCHAR(200) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_cross_day SMALLINT DEFAULT '0'
);

-- Add comments for table and columns
COMMENT ON TABLE duty_shift_config IS '班次配置表';
COMMENT ON COLUMN duty_shift_config.id IS '班次配置ID';
COMMENT ON COLUMN duty_shift_config.shift_name IS '班次名称';
COMMENT ON COLUMN duty_shift_config.shift_code IS '班次编码';
COMMENT ON COLUMN duty_shift_config.shift_type IS '班次类型:0-白班,1-早班,2-中班,3-晚班,4-全天,5-夜班';
COMMENT ON COLUMN duty_shift_config.start_time IS '上班时间';
COMMENT ON COLUMN duty_shift_config.end_time IS '下班时间';
COMMENT ON COLUMN duty_shift_config.duration_hours IS '时长(小时)';
COMMENT ON COLUMN duty_shift_config.break_hours IS '休息时长(小时)';
COMMENT ON COLUMN duty_shift_config.rest_day_rule IS '休息日规则';
COMMENT ON COLUMN duty_shift_config.is_overtime_shift IS '是否加班班次:0-否,1-是';
COMMENT ON COLUMN duty_shift_config.status IS '状态:0-禁用,1-启用';
COMMENT ON COLUMN duty_shift_config.sort IS '排序';
COMMENT ON COLUMN duty_shift_config.remark IS '备注';
COMMENT ON COLUMN duty_shift_config.create_time IS '创建时间';
COMMENT ON COLUMN duty_shift_config.update_time IS '更新时间';
COMMENT ON COLUMN duty_shift_config.is_cross_day IS '是否跨天:0-否,1-是';

-- Create indexes for duty_shift_config
CREATE UNIQUE INDEX shift_code ON duty_shift_config (shift_code);
CREATE INDEX idx_shift_code ON duty_shift_config (shift_code);
CREATE INDEX idx_status ON duty_shift_config (status);

--
-- Table structure for table `duty_holiday`
--

DROP TABLE IF EXISTS duty_holiday;
CREATE TABLE duty_holiday (
  id BIGSERIAL PRIMARY KEY,
  holiday_name VARCHAR(50) NOT NULL,
  holiday_date DATE NOT NULL,
  is_workday SMALLINT DEFAULT '0',
  holiday_type SMALLINT DEFAULT '1',
  remark VARCHAR(200) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE duty_holiday IS '节假日表';
COMMENT ON COLUMN duty_holiday.id IS '节假日ID';
COMMENT ON COLUMN duty_holiday.holiday_name IS '节假日名称';
COMMENT ON COLUMN duty_holiday.holiday_date IS '节假日日期';
COMMENT ON COLUMN duty_holiday.is_workday IS '是否调休上班:0-否,1-是';
COMMENT ON COLUMN duty_holiday.holiday_type IS '节假日类型:1-法定假日,2-调休,3-公司假日';
COMMENT ON COLUMN duty_holiday.remark IS '备注';
COMMENT ON COLUMN duty_holiday.create_time IS '创建时间';
COMMENT ON COLUMN duty_holiday.update_time IS '更新时间';

-- Create indexes for duty_holiday
CREATE UNIQUE INDEX uk_holiday_date ON duty_holiday (holiday_date);
CREATE INDEX idx_holiday_date ON duty_holiday (holiday_date);
CREATE INDEX idx_is_workday ON duty_holiday (is_workday);

--
-- Table structure for table `pm_project`
--

DROP TABLE IF EXISTS pm_project;
CREATE TABLE pm_project (
  id BIGSERIAL PRIMARY KEY,
  project_name VARCHAR(100) NOT NULL,
  project_code VARCHAR(50) NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  create_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE pm_project IS '项目表';
COMMENT ON COLUMN pm_project.id IS '项目ID';
COMMENT ON COLUMN pm_project.project_name IS '项目名称';
COMMENT ON COLUMN pm_project.project_code IS '项目编码';
COMMENT ON COLUMN pm_project.description IS '项目描述';
COMMENT ON COLUMN pm_project.start_date IS '开始日期';
COMMENT ON COLUMN pm_project.end_date IS '结束日期';
COMMENT ON COLUMN pm_project.status IS '状态：0-暂停，1-进行中，2-已完成';
COMMENT ON COLUMN pm_project.create_by IS '创建人ID';
COMMENT ON COLUMN pm_project.create_time IS '创建时间';
COMMENT ON COLUMN pm_project.update_time IS '更新时间';

-- Create indexes for pm_project
CREATE UNIQUE INDEX uk_project_code ON pm_project (project_code);
CREATE INDEX idx_status ON pm_project (status);

--
-- Table structure for table `sys_schedule_job`
--

DROP TABLE IF EXISTS sys_schedule_job;
CREATE TABLE sys_schedule_job (
  id BIGSERIAL PRIMARY KEY,
  job_name VARCHAR(100) NOT NULL,
  job_group VARCHAR(50) NOT NULL,
  job_code VARCHAR(100) NOT NULL,
  cron_expression VARCHAR(100) NOT NULL,
  bean_name VARCHAR(200) DEFAULT NULL,
  method_name VARCHAR(100) DEFAULT NULL,
  params TEXT DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  concurrent SMALLINT DEFAULT '0',
  description VARCHAR(500) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE sys_schedule_job IS '定时任务表';
COMMENT ON COLUMN sys_schedule_job.id IS '任务ID';
COMMENT ON COLUMN sys_schedule_job.job_name IS '任务名称';
COMMENT ON COLUMN sys_schedule_job.job_group IS '任务分组';
COMMENT ON COLUMN sys_schedule_job.job_code IS '任务编码';
COMMENT ON COLUMN sys_schedule_job.cron_expression IS 'Cron表达式';
COMMENT ON COLUMN sys_schedule_job.bean_name IS 'Bean名称';
COMMENT ON COLUMN sys_schedule_job.method_name IS '方法名称';
COMMENT ON COLUMN sys_schedule_job.params IS '参数';
COMMENT ON COLUMN sys_schedule_job.status IS '状态:0-暂停,1-启用';
COMMENT ON COLUMN sys_schedule_job.concurrent IS '是否允许并发:0-不允许,1-允许';
COMMENT ON COLUMN sys_schedule_job.description IS '任务描述';
COMMENT ON COLUMN sys_schedule_job.create_time IS '创建时间';
COMMENT ON COLUMN sys_schedule_job.update_time IS '更新时间';

-- Create indexes for sys_schedule_job
CREATE UNIQUE INDEX job_code ON sys_schedule_job (job_code);
CREATE INDEX idx_job_code ON sys_schedule_job (job_code);
CREATE INDEX idx_job_group ON sys_schedule_job (job_group);
CREATE INDEX idx_status ON sys_schedule_job (status);

-- Insert initial data
INSERT INTO sys_schedule_job (id, job_name, job_group, job_code, cron_expression, bean_name, method_name, params, status, concurrent, description, create_time, update_time)
VALUES (1, '节假日信息同步', 'system', 'holidaySync', '10 0 0 1 1 ?', 'holidayTaskExecutor', 'execute', '', 1, 0, '每年1月1日同步节假日信息', '2026-01-23 21:32:33', '2026-01-24 20:36:58');

-- 重置序列值，确保后续插入的ID从2开始
SELECT setval('sys_schedule_job_id_seq', 2);

-- =============================================
-- 依赖基础表的表
-- =============================================

--
-- Table structure for table `sys_employee`
--

DROP TABLE IF EXISTS sys_employee;
CREATE TABLE sys_employee (
  id BIGSERIAL PRIMARY KEY,
  employee_name VARCHAR(50) NOT NULL,
  dept_id BIGINT NOT NULL,
  employee_code VARCHAR(20) DEFAULT NULL,
  phone VARCHAR(11) DEFAULT NULL,
  email VARCHAR(50) DEFAULT NULL,
  gender SMALLINT DEFAULT '0',
  dict_type_id BIGINT DEFAULT NULL,
  dict_data_id BIGINT DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  username VARCHAR(50) DEFAULT NULL,
  password VARCHAR(100) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT sys_employee_ibfk_1 FOREIGN KEY (dept_id) REFERENCES sys_dept (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE sys_employee IS '人员表';
COMMENT ON COLUMN sys_employee.id IS '人员ID';
COMMENT ON COLUMN sys_employee.employee_name IS '人员姓名';
COMMENT ON COLUMN sys_employee.dept_id IS '所属部门ID';
COMMENT ON COLUMN sys_employee.employee_code IS '人员编码';
COMMENT ON COLUMN sys_employee.phone IS '手机号码';
COMMENT ON COLUMN sys_employee.email IS '邮箱';
COMMENT ON COLUMN sys_employee.gender IS '性别：0未知，1男，2女';
COMMENT ON COLUMN sys_employee.dict_type_id IS '字典类型ID';
COMMENT ON COLUMN sys_employee.dict_data_id IS '字典数据ID';
COMMENT ON COLUMN sys_employee.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN sys_employee.username IS '用户名';
COMMENT ON COLUMN sys_employee.password IS '密码';
COMMENT ON COLUMN sys_employee.create_time IS '创建时间';
COMMENT ON COLUMN sys_employee.update_time IS '更新时间';

-- Create indexes for sys_employee
CREATE UNIQUE INDEX uk_employee_code ON sys_employee (employee_code);
CREATE INDEX idx_employee_code ON sys_employee (employee_code);
CREATE INDEX idx_dict_type_id ON sys_employee (dict_type_id);
CREATE INDEX idx_dict_data_id ON sys_employee (dict_data_id);

--
-- Table structure for table `sys_dict_data`
--

DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
  id BIGSERIAL PRIMARY KEY,
  dict_type_id BIGINT NOT NULL,
  dict_label VARCHAR(100) NOT NULL,
  dict_value VARCHAR(100) NOT NULL,
  dict_sort INT DEFAULT '0',
  css_class VARCHAR(100) DEFAULT NULL,
  list_class VARCHAR(100) DEFAULT NULL,
  is_default SMALLINT DEFAULT '0',
  status SMALLINT DEFAULT '1',
  remark VARCHAR(500) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT sys_dict_data_ibfk_1 FOREIGN KEY (dict_type_id) REFERENCES sys_dict_type (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE sys_dict_data IS '字典数据表';
COMMENT ON COLUMN sys_dict_data.id IS '字典数据ID';
COMMENT ON COLUMN sys_dict_data.dict_type_id IS '字典类型ID';
COMMENT ON COLUMN sys_dict_data.dict_label IS '字典标签';
COMMENT ON COLUMN sys_dict_data.dict_value IS '字典键值';
COMMENT ON COLUMN sys_dict_data.dict_sort IS '字典排序';
COMMENT ON COLUMN sys_dict_data.css_class IS '样式属性（其他样式扩展）';
COMMENT ON COLUMN sys_dict_data.list_class IS '表格回显样式';
COMMENT ON COLUMN sys_dict_data.is_default IS '是否默认：0否，1是';
COMMENT ON COLUMN sys_dict_data.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN sys_dict_data.remark IS '备注';
COMMENT ON COLUMN sys_dict_data.create_time IS '创建时间';
COMMENT ON COLUMN sys_dict_data.update_time IS '更新时间';

-- Create indexes for sys_dict_data
CREATE INDEX idx_dict_type_id ON sys_dict_data (dict_type_id);
CREATE INDEX idx_dict_value ON sys_dict_data (dict_value);
CREATE INDEX idx_status ON sys_dict_data (status);

-- Insert data for sys_dict_data
INSERT INTO sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time)
VALUES (1, 1, '项目经理A', 'ProjectManagerA', 1, '', '', 0, 1, '', '2026-01-18 06:58:31', '2026-01-18 06:58:31'),
       (2, 1, '项目经理B', 'ProjectManagerB', 1, '', '', 0, 1, '', '2026-01-18 06:59:18', '2026-01-18 06:59:18'),
       (3, 1, '组长A', 'TeamLeaderA', 2, '', '', 0, 1, '', '2026-01-18 07:19:37', '2026-01-18 07:19:37'),
       (4, 1, '组长B', 'TeamLeaderB', 2, '', '', 0, 1, '', '2026-01-18 07:29:07', '2026-01-18 07:29:07'),
       (5, 1, '组员', 'TeamMember', 3, '', '', 0, 1, '', '2026-01-18 07:30:09', '2026-01-18 07:30:09'),
       (6, 2, '例行工作', '0', 1, '', '', 0, 1, '', '2026-02-20 11:27:16', '2026-02-20 11:27:16'),
       (14, 2, '3', '1', 0, '', '', 0, 1, '', '2026-02-20 12:14:29', '2026-02-20 12:14:29'),
       (15, 1, '123', '1231', 0, '', '', 0, 1, '', '2026-02-21 07:26:04', '2026-02-21 07:26:04'),
       (16, 1, '2131', '41421', 0, '', '', 0, 1, '', '2026-02-21 07:26:10', '2026-02-21 07:26:10'),
       (17, 1, '342', '5231', 0, '', '', 0, 1, '', '2026-02-21 07:26:14', '2026-02-21 07:26:14'),
       (18, 1, '1231', '3434', 0, '', '', 0, 1, '', '2026-02-21 07:26:18', '2026-02-21 07:26:18'),
       (19, 1, '123123', '4324324', 0, '', '', 0, 1, '', '2026-02-21 07:26:21', '2026-02-21 07:26:21'),
       (20, 1, '13123', '432432', 0, '', '', 0, 1, '', '2026-02-21 07:26:24', '2026-02-21 07:26:24');

-- Reset sequence value for sys_dict_data
SELECT setval('sys_dict_data_id_seq', 21);

--
-- Table structure for table `duty_schedule`
--

DROP TABLE IF EXISTS duty_schedule;
CREATE TABLE duty_schedule (
  id BIGSERIAL PRIMARY KEY,
  schedule_name VARCHAR(100) NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  status SMALLINT DEFAULT '1',
  create_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  schedule_mode_id BIGINT DEFAULT NULL,
  CONSTRAINT fk_schedule_mode FOREIGN KEY (schedule_mode_id) REFERENCES duty_schedule_mode (id) ON DELETE SET NULL
);

-- Add comments for table and columns
COMMENT ON TABLE duty_schedule IS '值班表';
COMMENT ON COLUMN duty_schedule.id IS '值班表ID';
COMMENT ON COLUMN duty_schedule.schedule_name IS '值班表名称';
COMMENT ON COLUMN duty_schedule.description IS '描述';
COMMENT ON COLUMN duty_schedule.start_date IS '开始日期';
COMMENT ON COLUMN duty_schedule.end_date IS '结束日期';
COMMENT ON COLUMN duty_schedule.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN duty_schedule.create_by IS '创建人ID';
COMMENT ON COLUMN duty_schedule.create_time IS '创建时间';
COMMENT ON COLUMN duty_schedule.update_time IS '更新时间';
COMMENT ON COLUMN duty_schedule.schedule_mode_id IS '排班方式ID';

-- Create indexes for duty_schedule
CREATE INDEX fk_schedule_mode ON duty_schedule (schedule_mode_id);

--
-- Table structure for table `duty_shift_mutex`
--

DROP TABLE IF EXISTS duty_shift_mutex;
CREATE TABLE duty_shift_mutex (
  id BIGSERIAL PRIMARY KEY,
  shift_config_id BIGINT NOT NULL,
  mutex_shift_config_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT duty_shift_mutex_ibfk_1 FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config (id) ON DELETE CASCADE,
  CONSTRAINT duty_shift_mutex_ibfk_2 FOREIGN KEY (mutex_shift_config_id) REFERENCES duty_shift_config (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE duty_shift_mutex IS '班次互斥关系表';
COMMENT ON COLUMN duty_shift_mutex.id IS 'ID';
COMMENT ON COLUMN duty_shift_mutex.shift_config_id IS '班次配置ID';
COMMENT ON COLUMN duty_shift_mutex.mutex_shift_config_id IS '互斥班次配置ID';
COMMENT ON COLUMN duty_shift_mutex.create_time IS '创建时间';
COMMENT ON COLUMN duty_shift_mutex.update_time IS '更新时间';

-- Create indexes for duty_shift_mutex
CREATE UNIQUE INDEX uk_shift_mutex ON duty_shift_mutex (shift_config_id, mutex_shift_config_id);
CREATE INDEX idx_shift_config_id ON duty_shift_mutex (shift_config_id);
CREATE INDEX idx_mutex_shift_config_id ON duty_shift_mutex (mutex_shift_config_id);

--
-- Table structure for table `employee_available_time`
--

DROP TABLE IF EXISTS employee_available_time;
CREATE TABLE employee_available_time (
  id BIGSERIAL PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  day_of_week SMALLINT NOT NULL,
  start_time TIME DEFAULT NULL,
  end_time TIME DEFAULT NULL,
  is_available SMALLINT DEFAULT '1',
  remark VARCHAR(200) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT employee_available_time_ibfk_1 FOREIGN KEY (employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE employee_available_time IS '人员可排班时段表';
COMMENT ON COLUMN employee_available_time.id IS 'ID';
COMMENT ON COLUMN employee_available_time.employee_id IS '员工ID';
COMMENT ON COLUMN employee_available_time.day_of_week IS '星期:1-周一,2-周二,3-周三,4-周四,5-周五,6-周六,7-周日';
COMMENT ON COLUMN employee_available_time.start_time IS '开始时间';
COMMENT ON COLUMN employee_available_time.end_time IS '结束时间';
COMMENT ON COLUMN employee_available_time.is_available IS '是否可用:0-否,1-是';
COMMENT ON COLUMN employee_available_time.remark IS '备注';
COMMENT ON COLUMN employee_available_time.create_time IS '创建时间';
COMMENT ON COLUMN employee_available_time.update_time IS '更新时间';

-- Create indexes for employee_available_time
CREATE UNIQUE INDEX uk_employee_day ON employee_available_time (employee_id, day_of_week);
CREATE INDEX idx_employee_id ON employee_available_time (employee_id);

-- =============================================
-- 依赖上述表的表
-- =============================================

--
-- Table structure for table `duty_schedule_employee`
--

DROP TABLE IF EXISTS duty_schedule_employee;
CREATE TABLE duty_schedule_employee (
  id BIGSERIAL PRIMARY KEY,
  schedule_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  sort_order INT DEFAULT '0',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  is_leader SMALLINT DEFAULT '0',
  CONSTRAINT duty_schedule_employee_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES duty_schedule (id) ON DELETE CASCADE,
  CONSTRAINT duty_schedule_employee_ibfk_2 FOREIGN KEY (employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE duty_schedule_employee IS '值班表人员关联表';
COMMENT ON COLUMN duty_schedule_employee.id IS 'ID';
COMMENT ON COLUMN duty_schedule_employee.schedule_id IS '值班表ID';
COMMENT ON COLUMN duty_schedule_employee.employee_id IS '员工ID';
COMMENT ON COLUMN duty_schedule_employee.sort_order IS '排序';
COMMENT ON COLUMN duty_schedule_employee.create_time IS '创建时间';
COMMENT ON COLUMN duty_schedule_employee.is_leader IS '是否值班长：0否，1是';

-- Create indexes for duty_schedule_employee
CREATE UNIQUE INDEX uk_schedule_employee ON duty_schedule_employee (schedule_id, employee_id);
CREATE INDEX idx_schedule_id ON duty_schedule_employee (schedule_id);
CREATE INDEX idx_employee_id ON duty_schedule_employee (employee_id);
CREATE INDEX idx_is_leader ON duty_schedule_employee (is_leader);

--
-- Table structure for table `duty_schedule_shift`
--

DROP TABLE IF EXISTS duty_schedule_shift;
CREATE TABLE duty_schedule_shift (
  id BIGSERIAL PRIMARY KEY,
  schedule_id BIGINT NOT NULL,
  shift_config_id BIGINT NOT NULL,
  sort_order INT DEFAULT '0',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT duty_schedule_shift_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES duty_schedule (id) ON DELETE CASCADE,
  CONSTRAINT duty_schedule_shift_ibfk_2 FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE duty_schedule_shift IS '值班表班次关联表';
COMMENT ON COLUMN duty_schedule_shift.id IS 'ID';
COMMENT ON COLUMN duty_schedule_shift.schedule_id IS '值班表ID';
COMMENT ON COLUMN duty_schedule_shift.shift_config_id IS '班次配置ID';
COMMENT ON COLUMN duty_schedule_shift.sort_order IS '排序';
COMMENT ON COLUMN duty_schedule_shift.create_time IS '创建时间';

-- Create indexes for duty_schedule_shift
CREATE UNIQUE INDEX uk_schedule_shift ON duty_schedule_shift (schedule_id, shift_config_id);
CREATE INDEX idx_schedule_id ON duty_schedule_shift (schedule_id);
CREATE INDEX idx_shift_config_id ON duty_schedule_shift (shift_config_id);

--
-- Table structure for table `schedule_version`
--

DROP TABLE IF EXISTS schedule_version;
CREATE TABLE schedule_version (
  id BIGSERIAL PRIMARY KEY,
  schedule_id BIGINT NOT NULL,
  version_name VARCHAR(100) NOT NULL,
  version_code VARCHAR(50) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  status VARCHAR(20) DEFAULT 'draft',
  is_current SMALLINT DEFAULT '0',
  create_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  publish_time TIMESTAMP DEFAULT NULL,
  CONSTRAINT schedule_version_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES duty_schedule (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE schedule_version IS '排班版本表';
COMMENT ON COLUMN schedule_version.id IS '版本ID';
COMMENT ON COLUMN schedule_version.schedule_id IS '值班表ID';
COMMENT ON COLUMN schedule_version.version_name IS '版本名称';
COMMENT ON COLUMN schedule_version.version_code IS '版本编码';
COMMENT ON COLUMN schedule_version.start_date IS '开始日期';
COMMENT ON COLUMN schedule_version.end_date IS '结束日期';
COMMENT ON COLUMN schedule_version.status IS '状态:draft-草稿,published-已发布,archived-已归档';
COMMENT ON COLUMN schedule_version.is_current IS '是否当前版本:0-否,1-是';
COMMENT ON COLUMN schedule_version.create_by IS '创建人ID';
COMMENT ON COLUMN schedule_version.create_time IS '创建时间';
COMMENT ON COLUMN schedule_version.publish_time IS '发布时间';

-- Create indexes for schedule_version
CREATE INDEX idx_schedule_id ON schedule_version (schedule_id);
CREATE INDEX idx_version_code ON schedule_version (version_code);
CREATE INDEX idx_status ON schedule_version (status);

--
-- Table structure for table `duty_assignment`
--

DROP TABLE IF EXISTS duty_assignment;
CREATE TABLE duty_assignment (
  id BIGSERIAL PRIMARY KEY,
  schedule_id BIGINT NOT NULL,
  duty_date DATE NOT NULL,
  duty_shift INT DEFAULT '1',
  employee_id BIGINT NOT NULL,
  status SMALLINT DEFAULT '1',
  remark VARCHAR(200) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  shift_config_id BIGINT DEFAULT NULL,
  version_id BIGINT DEFAULT NULL,
  is_overtime SMALLINT DEFAULT '0',
  CONSTRAINT duty_assignment_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES duty_schedule (id) ON DELETE CASCADE,
  CONSTRAINT duty_assignment_ibfk_2 FOREIGN KEY (employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT fk_shift_config FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config (id) ON DELETE SET NULL,
  CONSTRAINT fk_version FOREIGN KEY (version_id) REFERENCES schedule_version (id) ON DELETE SET NULL
);

-- Add comments for table and columns
COMMENT ON TABLE duty_assignment IS '值班安排表';
COMMENT ON COLUMN duty_assignment.id IS '值班安排ID';
COMMENT ON COLUMN duty_assignment.schedule_id IS '关联值班表ID';
COMMENT ON COLUMN duty_assignment.duty_date IS '值班日期';
COMMENT ON COLUMN duty_assignment.duty_shift IS '值班班次：1白班，2晚班，3夜班';
COMMENT ON COLUMN duty_assignment.employee_id IS '值班人员ID';
COMMENT ON COLUMN duty_assignment.status IS '状态：0取消，1正常';
COMMENT ON COLUMN duty_assignment.remark IS '备注';
COMMENT ON COLUMN duty_assignment.create_time IS '创建时间';
COMMENT ON COLUMN duty_assignment.update_time IS '更新时间';
COMMENT ON COLUMN duty_assignment.shift_config_id IS '班次配置ID';
COMMENT ON COLUMN duty_assignment.version_id IS '排班版本ID';
COMMENT ON COLUMN duty_assignment.is_overtime IS '是否加班:0-否,1-是';

-- Create indexes for duty_assignment
CREATE INDEX schedule_id ON duty_assignment (schedule_id);
CREATE INDEX employee_id ON duty_assignment (employee_id);
CREATE INDEX idx_shift_config_id ON duty_assignment (shift_config_id);
CREATE INDEX idx_version_id ON duty_assignment (version_id);

--
-- Table structure for table `leave_request`
--

DROP TABLE IF EXISTS leave_request;
CREATE TABLE leave_request (
  id BIGSERIAL PRIMARY KEY,
  request_no VARCHAR(50) NOT NULL,
  employee_id BIGINT NOT NULL,
  leave_type SMALLINT NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  start_time TIME DEFAULT NULL,
  end_time TIME DEFAULT NULL,
  total_hours DECIMAL(6,2) DEFAULT NULL,
  reason VARCHAR(500) NOT NULL,
  attachment_url VARCHAR(500) DEFAULT NULL,
  approval_status VARCHAR(20) DEFAULT 'pending',
  current_approver_id BIGINT DEFAULT NULL,
  approval_level INT DEFAULT '1',
  total_approval_levels INT DEFAULT '1',
  substitute_employee_id BIGINT DEFAULT NULL,
  substitute_type SMALLINT DEFAULT '1',
  substitute_status VARCHAR(20) DEFAULT 'pending',
  reject_reason VARCHAR(500) DEFAULT NULL,
  approval_opinion VARCHAR(500) DEFAULT NULL,
  schedule_completed SMALLINT DEFAULT '0',
  schedule_completed_time TIMESTAMP DEFAULT NULL,
  schedule_completed_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  schedule_id BIGINT DEFAULT NULL,
  shift_config_id BIGINT DEFAULT NULL,
  shift_config_ids VARCHAR(500) DEFAULT NULL,
  CONSTRAINT fk_schedule FOREIGN KEY (schedule_id) REFERENCES duty_schedule (id) ON DELETE SET NULL,
  CONSTRAINT leave_request_ibfk_1 FOREIGN KEY (employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT leave_request_ibfk_2 FOREIGN KEY (current_approver_id) REFERENCES sys_employee (id) ON DELETE SET NULL,
  CONSTRAINT leave_request_ibfk_3 FOREIGN KEY (substitute_employee_id) REFERENCES sys_employee (id) ON DELETE SET NULL,
  CONSTRAINT leave_request_ibfk_4 FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config (id) ON DELETE SET NULL
);

-- Add comments for table and columns
COMMENT ON TABLE leave_request IS '请假申请表';
COMMENT ON COLUMN leave_request.id IS '请假申请ID';
COMMENT ON COLUMN leave_request.request_no IS '申请编号';
COMMENT ON COLUMN leave_request.employee_id IS '申请人ID';
COMMENT ON COLUMN leave_request.leave_type IS '请假类型:1-事假,2-病假,3-年假,4-调休,5-其他';
COMMENT ON COLUMN leave_request.start_date IS '开始日期';
COMMENT ON COLUMN leave_request.end_date IS '结束日期';
COMMENT ON COLUMN leave_request.start_time IS '开始时间';
COMMENT ON COLUMN leave_request.end_time IS '结束时间';
COMMENT ON COLUMN leave_request.total_hours IS '请假总时长(小时)';
COMMENT ON COLUMN leave_request.reason IS '请假原因';
COMMENT ON COLUMN leave_request.attachment_url IS '附件URL';
COMMENT ON COLUMN leave_request.approval_status IS '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消';
COMMENT ON COLUMN leave_request.current_approver_id IS '当前审批人ID';
COMMENT ON COLUMN leave_request.approval_level IS '当前审批层级';
COMMENT ON COLUMN leave_request.total_approval_levels IS '总审批层级';
COMMENT ON COLUMN leave_request.substitute_employee_id IS '替补人员ID';
COMMENT ON COLUMN leave_request.substitute_type IS '替补类型:1-自动匹配,2-手动选择';
COMMENT ON COLUMN leave_request.substitute_status IS '替补状态:pending-待确认,confirmed-已确认,rejected-已拒绝';
COMMENT ON COLUMN leave_request.reject_reason IS '拒绝原因';
COMMENT ON COLUMN leave_request.approval_opinion IS '审批意见';
COMMENT ON COLUMN leave_request.schedule_completed IS '排班是否完成:0-未完成,1-已完成';
COMMENT ON COLUMN leave_request.schedule_completed_time IS '排班完成时间';
COMMENT ON COLUMN leave_request.schedule_completed_by IS '排班完成人ID';
COMMENT ON COLUMN leave_request.create_time IS '创建时间';
COMMENT ON COLUMN leave_request.update_time IS '更新时间';
COMMENT ON COLUMN leave_request.schedule_id IS '值班表ID';
COMMENT ON COLUMN leave_request.shift_config_id IS '班次配置ID';
COMMENT ON COLUMN leave_request.shift_config_ids IS '班次配置ID列表，多个用逗号分隔';

-- Create indexes for leave_request
CREATE UNIQUE INDEX uk_request_no ON leave_request (request_no);
CREATE INDEX idx_employee_id ON leave_request (employee_id);
CREATE INDEX idx_approval_status ON leave_request (approval_status);
CREATE INDEX idx_date_range ON leave_request (start_date, end_date);
CREATE INDEX idx_current_approver_id ON leave_request (current_approver_id);
CREATE INDEX idx_substitute_employee_id ON leave_request (substitute_employee_id);
CREATE INDEX idx_schedule_id ON leave_request (schedule_id);
CREATE INDEX idx_shift_config_id ON leave_request (shift_config_id);

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
  id BIGSERIAL PRIMARY KEY,
  operator_id BIGINT DEFAULT NULL,
  operator_name VARCHAR(50) DEFAULT NULL,
  operation_type VARCHAR(50) NOT NULL,
  operation_module VARCHAR(50) NOT NULL,
  operation_desc VARCHAR(500) DEFAULT NULL,
  request_method VARCHAR(10) DEFAULT NULL,
  request_url VARCHAR(500) DEFAULT NULL,
  request_params TEXT DEFAULT NULL,
  response_result TEXT DEFAULT NULL,
  ip_address VARCHAR(50) DEFAULT NULL,
  user_agent VARCHAR(500) DEFAULT NULL,
  execution_time INT DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  error_msg TEXT DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT operation_log_ibfk_1 FOREIGN KEY (operator_id) REFERENCES sys_employee (id) ON DELETE SET NULL
);

-- Add comments for table and columns
COMMENT ON TABLE operation_log IS '操作日志表';
COMMENT ON COLUMN operation_log.id IS '日志ID';
COMMENT ON COLUMN operation_log.operator_id IS '操作人ID';
COMMENT ON COLUMN operation_log.operator_name IS '操作人姓名';
COMMENT ON COLUMN operation_log.operation_type IS '操作类型';
COMMENT ON COLUMN operation_log.operation_module IS '操作模块';
COMMENT ON COLUMN operation_log.operation_desc IS '操作描述';
COMMENT ON COLUMN operation_log.request_method IS '请求方法';
COMMENT ON COLUMN operation_log.request_url IS '请求URL';
COMMENT ON COLUMN operation_log.request_params IS '请求参数';
COMMENT ON COLUMN operation_log.response_result IS '响应结果';
COMMENT ON COLUMN operation_log.ip_address IS 'IP地址';
COMMENT ON COLUMN operation_log.user_agent IS '用户代理';
COMMENT ON COLUMN operation_log.execution_time IS '执行时间(毫秒)';
COMMENT ON COLUMN operation_log.status IS '状态:0-失败,1-成功';
COMMENT ON COLUMN operation_log.error_msg IS '错误信息';
COMMENT ON COLUMN operation_log.create_time IS '创建时间';

-- Create indexes for operation_log
CREATE INDEX idx_operator_id ON operation_log (operator_id);
CREATE INDEX idx_operation_type ON operation_log (operation_type);
CREATE INDEX idx_operation_module ON operation_log (operation_module);
CREATE INDEX idx_create_time ON operation_log (create_time);

--
-- Table structure for table `pm_task`
--

DROP TABLE IF EXISTS pm_task;
CREATE TABLE pm_task (
  id BIGSERIAL PRIMARY KEY,
  task_name VARCHAR(100) NOT NULL,
  task_code VARCHAR(50) NOT NULL,
  project_id BIGINT NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  priority SMALLINT DEFAULT '3',
  assignee_id BIGINT DEFAULT NULL,
  create_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pm_task_ibfk_1 FOREIGN KEY (project_id) REFERENCES pm_project (id) ON DELETE CASCADE,
  CONSTRAINT pm_task_ibfk_2 FOREIGN KEY (assignee_id) REFERENCES sys_employee (id) ON DELETE SET NULL
);

-- Add comments for table and columns
COMMENT ON TABLE pm_task IS '任务表';
COMMENT ON COLUMN pm_task.id IS '任务ID';
COMMENT ON COLUMN pm_task.task_name IS '任务名称';
COMMENT ON COLUMN pm_task.task_code IS '任务编码';
COMMENT ON COLUMN pm_task.project_id IS '项目ID';
COMMENT ON COLUMN pm_task.description IS '任务描述';
COMMENT ON COLUMN pm_task.start_date IS '开始日期';
COMMENT ON COLUMN pm_task.end_date IS '结束日期';
COMMENT ON COLUMN pm_task.status IS '状态：0-未开始，1-进行中，2-已完成';
COMMENT ON COLUMN pm_task.priority IS '优先级：1-高，2-中，3-低';
COMMENT ON COLUMN pm_task.assignee_id IS '负责人ID';
COMMENT ON COLUMN pm_task.create_by IS '创建人ID';
COMMENT ON COLUMN pm_task.create_time IS '创建时间';
COMMENT ON COLUMN pm_task.update_time IS '更新时间';

-- Create indexes for pm_task
CREATE UNIQUE INDEX uk_task_code ON pm_task (task_code);
CREATE INDEX idx_project_id ON pm_task (project_id);
CREATE INDEX idx_status ON pm_task (status);
CREATE INDEX idx_priority ON pm_task (priority);
CREATE INDEX idx_assignee_id ON pm_task (assignee_id);

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
  id BIGSERIAL PRIMARY KEY,
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT sys_role_menu_ibfk_1 FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE,
  CONSTRAINT sys_role_menu_ibfk_2 FOREIGN KEY (menu_id) REFERENCES sys_menu (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';
COMMENT ON COLUMN sys_role_menu.id IS 'ID';
COMMENT ON COLUMN sys_role_menu.role_id IS '角色ID';
COMMENT ON COLUMN sys_role_menu.menu_id IS '菜单ID';
COMMENT ON COLUMN sys_role_menu.create_time IS '创建时间';

-- Create indexes for sys_role_menu
CREATE UNIQUE INDEX uk_role_menu ON sys_role_menu (role_id, menu_id);
CREATE INDEX idx_role_id ON sys_role_menu (role_id);
CREATE INDEX idx_menu_id ON sys_role_menu (menu_id);

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
  id BIGSERIAL PRIMARY KEY,
  employee_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT sys_user_role_ibfk_1 FOREIGN KEY (employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT sys_user_role_ibfk_2 FOREIGN KEY (role_id) REFERENCES sys_role (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE sys_user_role IS '用户角色关联表';
COMMENT ON COLUMN sys_user_role.id IS 'ID';
COMMENT ON COLUMN sys_user_role.employee_id IS '人员ID';
COMMENT ON COLUMN sys_user_role.role_id IS '角色ID';
COMMENT ON COLUMN sys_user_role.create_time IS '创建时间';

-- Create indexes for sys_user_role
CREATE UNIQUE INDEX uk_employee_role ON sys_user_role (employee_id, role_id);
CREATE INDEX idx_employee_id ON sys_user_role (employee_id);
CREATE INDEX idx_role_id ON sys_user_role (role_id);

-- =============================================
-- 依赖上述表的表
-- =============================================

--
-- Table structure for table `approval_record`
--

DROP TABLE IF EXISTS approval_record;
CREATE TABLE approval_record (
  id BIGSERIAL PRIMARY KEY,
  request_id BIGINT NOT NULL,
  request_type VARCHAR(20) NOT NULL,
  approver_id BIGINT NOT NULL,
  approval_level INT NOT NULL,
  approval_status VARCHAR(20) NOT NULL,
  approval_opinion VARCHAR(500) DEFAULT NULL,
  approval_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT approval_record_ibfk_1 FOREIGN KEY (request_id) REFERENCES leave_request (id) ON DELETE CASCADE,
  CONSTRAINT approval_record_ibfk_2 FOREIGN KEY (approver_id) REFERENCES sys_employee (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE approval_record IS '审批记录表';
COMMENT ON COLUMN approval_record.id IS '审批记录ID';
COMMENT ON COLUMN approval_record.request_id IS '申请ID';
COMMENT ON COLUMN approval_record.request_type IS '申请类型:leave-请假,swap-调班';
COMMENT ON COLUMN approval_record.approver_id IS '审批人ID';
COMMENT ON COLUMN approval_record.approval_level IS '审批层级';
COMMENT ON COLUMN approval_record.approval_status IS '审批状态:approved-通过,rejected-拒绝';
COMMENT ON COLUMN approval_record.approval_opinion IS '审批意见';
COMMENT ON COLUMN approval_record.approval_time IS '审批时间';
COMMENT ON COLUMN approval_record.create_time IS '创建时间';

-- Create indexes for approval_record
CREATE INDEX idx_request_id ON approval_record (request_id);
CREATE INDEX idx_approver_id ON approval_record (approver_id);

--
-- Table structure for table `duty_record`
--

DROP TABLE IF EXISTS duty_record;
CREATE TABLE duty_record (
  id BIGSERIAL PRIMARY KEY,
  assignment_id BIGINT NOT NULL,
  employee_id BIGINT NOT NULL,
  check_in_time TIMESTAMP DEFAULT NULL,
  check_out_time TIMESTAMP DEFAULT NULL,
  duty_status SMALLINT DEFAULT '1',
  check_in_remark VARCHAR(200) DEFAULT NULL,
  check_out_remark VARCHAR(200) DEFAULT NULL,
  overtime_hours DECIMAL(5,2) DEFAULT '0.00',
  approval_status VARCHAR(20) DEFAULT 'pending',
  manager_remark VARCHAR(200) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  substitute_employee_id BIGINT DEFAULT NULL,
  substitute_type SMALLINT DEFAULT '1',
  CONSTRAINT duty_record_ibfk_1 FOREIGN KEY (assignment_id) REFERENCES duty_assignment (id) ON DELETE CASCADE,
  CONSTRAINT duty_record_ibfk_2 FOREIGN KEY (employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT fk_substitute_employee FOREIGN KEY (substitute_employee_id) REFERENCES sys_employee (id) ON DELETE SET NULL
);

-- Add comments for table and columns
COMMENT ON TABLE duty_record IS '值班记录表';
COMMENT ON COLUMN duty_record.id IS '值班记录ID';
COMMENT ON COLUMN duty_record.assignment_id IS '关联值班安排ID';
COMMENT ON COLUMN duty_record.employee_id IS '值班人员ID';
COMMENT ON COLUMN duty_record.check_in_time IS '签到时间';
COMMENT ON COLUMN duty_record.check_out_time IS '签退时间';
COMMENT ON COLUMN duty_record.duty_status IS '值班状态：1正常，2迟到，3早退，4缺勤，5请假';
COMMENT ON COLUMN duty_record.check_in_remark IS '签到备注';
COMMENT ON COLUMN duty_record.check_out_remark IS '签退备注';
COMMENT ON COLUMN duty_record.overtime_hours IS '加班时长';
COMMENT ON COLUMN duty_record.approval_status IS '审批状态：pending待审批，approved已审批，rejected已拒绝';
COMMENT ON COLUMN duty_record.manager_remark IS '经理备注';
COMMENT ON COLUMN duty_record.create_time IS '创建时间';
COMMENT ON COLUMN duty_record.update_time IS '更新时间';
COMMENT ON COLUMN duty_record.substitute_employee_id IS '替补人员ID';
COMMENT ON COLUMN duty_record.substitute_type IS '替补类型:1-自动匹配,2-手动选择';

-- Create indexes for duty_record
CREATE INDEX assignment_id ON duty_record (assignment_id);
CREATE INDEX employee_id ON duty_record (employee_id);
CREATE INDEX idx_substitute_employee_id ON duty_record (substitute_employee_id);

--
-- Table structure for table `leave_substitute`
--

DROP TABLE IF EXISTS leave_substitute;
CREATE TABLE leave_substitute (
  id BIGSERIAL PRIMARY KEY,
  leave_request_id BIGINT NOT NULL,
  original_employee_id BIGINT NOT NULL,
  substitute_employee_id BIGINT NOT NULL,
  duty_date DATE NOT NULL,
  shift_config_id BIGINT NOT NULL,
  status INT DEFAULT '1',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT leave_substitute_ibfk_1 FOREIGN KEY (leave_request_id) REFERENCES leave_request (id) ON DELETE CASCADE,
  CONSTRAINT leave_substitute_ibfk_2 FOREIGN KEY (original_employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT leave_substitute_ibfk_3 FOREIGN KEY (substitute_employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT leave_substitute_ibfk_4 FOREIGN KEY (shift_config_id) REFERENCES duty_shift_config (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE leave_substitute IS '请假顶岗信息表';
COMMENT ON COLUMN leave_substitute.id IS 'ID';
COMMENT ON COLUMN leave_substitute.leave_request_id IS '请假申请ID';
COMMENT ON COLUMN leave_substitute.original_employee_id IS '原值班人员ID';
COMMENT ON COLUMN leave_substitute.substitute_employee_id IS '顶岗人员ID';
COMMENT ON COLUMN leave_substitute.duty_date IS '值班日期';
COMMENT ON COLUMN leave_substitute.shift_config_id IS '班次配置ID';
COMMENT ON COLUMN leave_substitute.status IS '状态';
COMMENT ON COLUMN leave_substitute.create_time IS '创建时间';
COMMENT ON COLUMN leave_substitute.update_time IS '更新时间';

-- Create indexes for leave_substitute
CREATE INDEX idx_leave_request_id ON leave_substitute (leave_request_id);
CREATE INDEX idx_duty_date ON leave_substitute (duty_date);
CREATE INDEX idx_shift_config_id ON leave_substitute (shift_config_id);

--
-- Table structure for table `swap_request`
--

DROP TABLE IF EXISTS swap_request;
CREATE TABLE swap_request (
  id BIGSERIAL PRIMARY KEY,
  request_no VARCHAR(50) NOT NULL,
  original_employee_id BIGINT NOT NULL,
  target_employee_id BIGINT NOT NULL,
  original_assignment_id BIGINT NOT NULL,
  target_assignment_id BIGINT DEFAULT NULL,
  schedule_id BIGINT NOT NULL,
  swap_date DATE NOT NULL,
  swap_shift INT NOT NULL,
  reason VARCHAR(500) DEFAULT NULL,
  approval_status VARCHAR(20) DEFAULT 'pending',
  current_approver_id BIGINT DEFAULT NULL,
  approval_level INT DEFAULT '1',
  total_approval_levels INT DEFAULT '1',
  original_confirm_status VARCHAR(20) DEFAULT 'pending',
  target_confirm_status VARCHAR(20) DEFAULT 'pending',
  reject_reason VARCHAR(500) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  original_swap_date DATE DEFAULT NULL,
  original_swap_shift INT DEFAULT NULL,
  target_swap_date DATE DEFAULT NULL,
  target_swap_shift INT DEFAULT NULL,
  CONSTRAINT swap_request_ibfk_1 FOREIGN KEY (original_employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT swap_request_ibfk_2 FOREIGN KEY (target_employee_id) REFERENCES sys_employee (id) ON DELETE CASCADE,
  CONSTRAINT swap_request_ibfk_3 FOREIGN KEY (original_assignment_id) REFERENCES duty_assignment (id) ON DELETE CASCADE,
  CONSTRAINT swap_request_ibfk_4 FOREIGN KEY (target_assignment_id) REFERENCES duty_assignment (id) ON DELETE CASCADE,
  CONSTRAINT swap_request_ibfk_5 FOREIGN KEY (schedule_id) REFERENCES duty_schedule (id) ON DELETE CASCADE
);

-- Add comments for table and columns
COMMENT ON TABLE swap_request IS '调班记录表';
COMMENT ON COLUMN swap_request.id IS '调班申请ID';
COMMENT ON COLUMN swap_request.request_no IS '申请编号';
COMMENT ON COLUMN swap_request.original_employee_id IS '原值班人员ID';
COMMENT ON COLUMN swap_request.target_employee_id IS '目标值班人员ID';
COMMENT ON COLUMN swap_request.original_assignment_id IS '原值班安排ID';
COMMENT ON COLUMN swap_request.target_assignment_id IS '目标值班安排ID';
COMMENT ON COLUMN swap_request.schedule_id IS '值班表ID';
COMMENT ON COLUMN swap_request.swap_date IS '调班日期';
COMMENT ON COLUMN swap_request.swap_shift IS '调班班次';
COMMENT ON COLUMN swap_request.reason IS '调班原因';
COMMENT ON COLUMN swap_request.approval_status IS '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消';
COMMENT ON COLUMN swap_request.current_approver_id IS '当前审批人ID';
COMMENT ON COLUMN swap_request.approval_level IS '当前审批层级';
COMMENT ON COLUMN swap_request.total_approval_levels IS '总审批层级';
COMMENT ON COLUMN swap_request.original_confirm_status IS '原人员确认状态';
COMMENT ON COLUMN swap_request.target_confirm_status IS '目标人员确认状态';
COMMENT ON COLUMN swap_request.reject_reason IS '拒绝原因';
COMMENT ON COLUMN swap_request.create_time IS '创建时间';
COMMENT ON COLUMN swap_request.update_time IS '更新时间';
COMMENT ON COLUMN swap_request.original_swap_date IS '原值班日期';
COMMENT ON COLUMN swap_request.original_swap_shift IS '原值班班次';
COMMENT ON COLUMN swap_request.target_swap_date IS '目标值班日期';
COMMENT ON COLUMN swap_request.target_swap_shift IS '目标值班班次';

-- Create indexes for swap_request
CREATE UNIQUE INDEX uk_request_no ON swap_request (request_no);
CREATE INDEX idx_original_employee ON swap_request (original_employee_id);
CREATE INDEX idx_target_employee ON swap_request (target_employee_id);
CREATE INDEX idx_approval_status ON swap_request (approval_status);
CREATE INDEX idx_swap_date ON swap_request (swap_date);
CREATE INDEX idx_original_assignment_id ON swap_request (original_assignment_id);
CREATE INDEX idx_target_assignment_id ON swap_request (target_assignment_id);
CREATE INDEX idx_schedule_id ON swap_request (schedule_id);

-- =============================================
-- 其他表
-- =============================================

--
-- Table structure for table `sys_schedule_log`
--

DROP TABLE IF EXISTS sys_schedule_log;
CREATE TABLE sys_schedule_log (
  id BIGSERIAL PRIMARY KEY,
  job_name VARCHAR(100) NOT NULL,
  job_group VARCHAR(50) NOT NULL,
  invoke_target VARCHAR(200) NOT NULL,
  job_message VARCHAR(500) DEFAULT NULL,
  status SMALLINT NOT NULL,
  error_msg TEXT DEFAULT NULL,
  start_time TIMESTAMP DEFAULT NULL,
  end_time TIMESTAMP DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE sys_schedule_log IS '定时任务日志表';
COMMENT ON COLUMN sys_schedule_log.id IS '日志ID';
COMMENT ON COLUMN sys_schedule_log.job_name IS '任务名称';
COMMENT ON COLUMN sys_schedule_log.job_group IS '任务组名';
COMMENT ON COLUMN sys_schedule_log.invoke_target IS '调用目标字符串';
COMMENT ON COLUMN sys_schedule_log.job_message IS '任务信息';
COMMENT ON COLUMN sys_schedule_log.status IS '执行状态：0-失败，1-成功';
COMMENT ON COLUMN sys_schedule_log.error_msg IS '异常信息';
COMMENT ON COLUMN sys_schedule_log.start_time IS '开始时间';
COMMENT ON COLUMN sys_schedule_log.end_time IS '结束时间';
COMMENT ON COLUMN sys_schedule_log.create_time IS '创建时间';

-- Create indexes for sys_schedule_log
CREATE INDEX idx_job_group ON sys_schedule_log (job_group);
CREATE INDEX idx_status ON sys_schedule_log (status);
CREATE INDEX idx_create_time ON sys_schedule_log (create_time);

-- =============================================
-- 数据插入
-- =============================================

-- ========================================
-- 系统基础数据
-- ========================================

-- 插入默认部门（与dump文件保持一致）
INSERT INTO sys_dept (id, dept_name, parent_id, dept_code, sort, status) VALUES
(1, '新炬', 0, '001', 1, 1),
(2, '技术部', 1, '002', 1, 1),
(3, '人事部', 1, '003', 2, 1),
(4, '财务部', 1, '004', 3, 1),
(6, '中北事业群', 1, '005', 4, 1),
(7, '交付二部', 6, '006', 1, 1),
(8, '江苏移动SRE', 7, '007', 1, 1),
(9, 'GOC调度组', 8, '008', 1, 1),
(10, '架构入网组', 8, '009', 2, 1),
(11, '变更管控组', 8, '010', 3, 1),
(12, '故障治理组', 8, '011', 4, 1),
(13, '运营治理组', 8, '012', 5, 1),
(14, '例行工作组', 8, '013', 6, 1);

-- 插入默认人员
INSERT INTO sys_employee (id, employee_name, dept_id, employee_code, phone, email, gender, status, username, password) VALUES
(1, '管理员', 1, 'EMP001', '13800138000', 'admin@example.com', 1, 1, 'admin', '$2a$10$cVw7wDpG1k6uluAFjKuROelCmanmBO/gynn.uv0/Clz4RrXEC76AS'),
(10, '王国豪', 8, 'EMP005', '18551816535', 'wangguohao@shsnc.com', 1, 1, 'wangguohao', '$2a$10$MMjjnvpoL2nP5c2szF12ceNCaHzd6WCeywT3X4ML8cpgW7TtbRRtS'),
(12, '王文治', 8, 'EMP006', '18812030630', 'wangwenzhi_bj@shsnc.com', 1, 1, 'wangwenzhi', '$2a$10$L9FFQ7d/tm7UxJkGYuKJNer2u5xnggfRzjtF2TUH4JRjtopRxlmIy'),
(13, '杜志强', 8, 'EMP007', '18351868973', 'duzhiqiang@shsnc.com', 1, 1, 'duzhiqiang', '$2a$10$omKfvUW4m.rvOSfBeIMBgO8MQnji0we9pWiRraE5xiSlzf/kKBUuS'),
(14, '刘成军', 8, 'EMP008', '13815444756', 'liuchengjun@shsnc.com', 1, 1, 'liuchengjun', '$2a$10$8W/LyURtu9TFnAABwoZXYO/uTr7evJLquMw1Qiy2rbcMtFi..ulru'),
(15, '沈建明', 8, 'EMP009', '17805139774', 'shenjianming@shsnc.com', 1, 1, 'shenjianming', '$2a$10$Zs3yNLZoA.aw/MlDjCHNme8TzWCoDXmux.HY3OGM4zYHxvLCW7xKq'),
(16, '邢文骏', 8, 'EMP010', '18851664875', 'xingwenjun@shsnc.com', 1, 1, 'xingwenjun', '$2a$10$4NMUkJUqum5VgjGKc0qy3O3S7tc2PuWhdAW5JWIF7jWgUC3nGJH4C'),
(17, '刘润泽', 8, 'EMP011', '15251859476', 'liurunze@shsnc.com', 1, 1, 'liurunze', '$2a$10$H6P9FjqA9cV5.T6qBBH27.1IzathR67mjip2PIH3PmhrE.noNreQ6'),
(18, '周柏霖', 8, 'EMP012', '18251816392', 'zhoubolin@shsnc.com', 1, 1, 'zhoubolin', '$2a$10$3dKjtY3/ORomNE9HlcOjvudZ/Zir8SagPDMdCSQqLDNX.f7a6qB/S'),
(19, '张华健', 8, 'EMP013', '', '', 1, 1, 'zhanghuajian', '$2a$10$/ImJs.ho2LjKtuI698ORwu9iVJP06YNA2EuACiWCJhB7pNFDPnFlW'),
(20, '涂宏', 8, 'EMP014', '13851450117', 'tuhong_nj@shsnc.com', 1, 1, 'tuhong', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2'),
(21, '张三', 2, 'EMP002', '13800138001', 'zhangsan@example.com', 1, 1, 'zhangsan', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2'),
(22, '李四', 2, 'EMP003', '13800138002', 'lisi@example.com', 1, 1, 'lisi', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2');

-- 重置序列值，确保后续插入的ID从23开始
SELECT setval('sys_employee_id_seq', 23);

-- 插入默认角色
INSERT INTO sys_role (id, role_name, role_code, description, status) VALUES
(1, '超级管理员', 'ROLE_ADMIN', '拥有系统所有权限', 1),
(2, '普通用户', 'ROLE_USER', '拥有基础操作权限', 1),
(3, '112321', '2213', '', 1),
(4, '421321', '21321', '', 1),
(5, '21342', '343', '', 1),
(6, '5432', '1254', '', 1),
(7, '4354354', '23213', '', 1),
(8, '213215', '324324', '', 1),
(9, '324324', '123213', '', 1),
(10, '213213', '43543', '', 1),
(11, '21321', '5442432', '', 1);

-- 重置序列值，确保后续插入的ID从12开始
SELECT setval('sys_role_id_seq', 12);

-- 插入系统基础菜单
INSERT INTO sys_menu (id, menu_name, parent_id, path, component, perm, type, icon, sort, status) VALUES
(1, '首页', 0, '/dashboard', 'views/Dashboard.vue', '', 1, 'HomeFilled', 1, 1),
(2, '系统管理', 0, '', '', '', 1, 'Setting', 2, 1),
(3, '部门管理', 2, '/dept', 'views/Dept.vue', 'sys:dept:list', 2, 'OfficeBuilding', 1, 1),
(4, '人员管理', 2, '/employee', 'views/Employee.vue', 'sys:employee:list', 2, 'User', 2, 1),
(5, '用户管理--已合并到人员管理', 4, '/system/user', 'views/User.vue', 'sys:user:list', 2, 'Avatar', 3, 0),
(6, '菜单管理', 2, '/menu', 'views/Menu.vue', 'sys:menu:list', 2, 'Menu', 4, 1),
(7, '角色管理', 2, '/role', 'views/Role.vue', 'sys:role:list', 2, 'DocumentCopy', 5, 1),
(8, '值班管理', 0, '', '', '', 1, 'Calendar', 3, 1),
(9, '值班表管理', 8, '/duty/schedule', 'views/duty/DutySchedule.vue', 'duty:schedule:list', 2, 'DocumentCopy', 2, 1),
(10, '值班安排', 8, '/duty/assignment', 'views/duty/DutyAssignment.vue', 'duty:assignment:list', 2, 'Calendar', 1, 1),
(11, '加班记录', 8, '/duty/record', 'views/duty/DutyRecord.vue', 'duty:record:list', 2, 'Document', 9, 1),
(12, '班次配置', 8, '/duty/shift-config', 'views/duty/ShiftConfig.vue', 'duty:shift:list', 2, 'Clock', 3, 1),
(13, '请假申请', 8, '/duty/leave-request', 'views/duty/LeaveRequest.vue', 'duty:leave:list', 2, 'Document', 5, 1),
(14, '请假审批', 8, '/duty/leave-approval', 'views/duty/LeaveApproval.vue', 'duty:leave:approve', 2, 'CircleCheck', 6, 1),
(15, '调班管理', 8, '/duty/swap-request', 'views/duty/SwapRequest.vue', 'duty:swap:list', 2, 'Refresh', 7, 1),
(16, '排班统计', 8, '/duty/statistics', 'views/duty/Statistics.vue', 'duty:statistics:view', 2, 'DataAnalysis', 8, 1),
(17, '操作日志', 2, '/system/operation-log', 'views/system/OperationLog.vue', 'sys:log:list', 2, 'Document', 7, 1),
(20, '定时任务', 2, '/system/schedule-job', 'views/system/ScheduleJob.vue', 'sys:schedule:job:list', 2, 'Timer', 8, 1),
(24, '排班模式管理', 8, '/duty/schedule-mode', 'views/duty/ScheduleMode.vue', 'duty:schedule:mode:list', 2, 'Operation', 4, 1),
(25, '字典管理', 2, '/dict', 'views/Dict.vue', 'sys:dict:list', 2, 'List', 6, 1),
(26, '项目管理', 0, '', '', '', 1, 'Briefcase', 4, 1),
(27, '项目列表', 26, '/project/list', 'views/project/ProjectList.vue', 'project:list', 2, 'List', 1, 1),
(28, '项目详情', 26, '/project/detail', 'views/project/ProjectDetail.vue', 'project:detail', 2, 'View', 2, 1),
(29, '任务管理', 26, '/project/task', 'views/project/TaskList.vue', 'project:task:list', 2, 'Check', 3, 1),
(30, '项目统计', 26, '/project/statistics', 'views/project/ProjectGantt.vue', 'project:statistics', 2, 'DataAnalysis', 4, 1);

-- 重置序列值，确保后续插入的ID从31开始
SELECT setval('sys_menu_id_seq', 31);

-- 插入人员角色关联
INSERT INTO sys_user_role (id, employee_id, role_id) VALUES
(32, 1, 1), -- admin 关联 超级管理员
(33, 10, 1), -- wangguohao 关联 超级管理员
(44, 12, 2), -- wangwenzhi 关联 普通用户
(45, 13, 2), -- duzhiqiang 关联 普通用户
(46, 14, 2), -- liuchengjun 关联 普通用户
(47, 15, 2), -- shenjianming 关联 普通用户
(48, 16, 2), -- xingwenjun 关联 普通用户
(49, 17, 2), -- liurunze 关联 普通用户
(50, 18, 2), -- zhoubolin 关联 普通用户
(51, 19, 2), -- zhanghuajian 关联 普通用户
(52, 20, 2), -- tuhong 关联 普通用户
(53, 21, 2), -- zhangsan 关联 普通用户
(54, 22, 2); -- lisi 关联 普通用户

-- 重置序列值，确保后续插入的ID从55开始
SELECT setval('sys_user_role_id_seq', 55);

-- 插入角色菜单关联
INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(231, 2, 1),
(232, 2, 17),
(233, 2, 10),
(234, 2, 9),
(235, 2, 13),
(236, 2, 14),
(237, 2, 15),
(238, 2, 16),
(239, 2, 11),
(410, 1, 1),
(411, 1, 2),
(412, 1, 3),
(413, 1, 4),
(414, 1, 5),
(415, 1, 6),
(416, 1, 7),
(417, 1, 20),
(418, 1, 17),
(419, 1, 25),
(420, 1, 8),
(421, 1, 10),
(422, 1, 9),
(423, 1, 12),
(424, 1, 24),
(425, 1, 13),
(426, 1, 14),
(427, 1, 15),
(428, 1, 16),
(429, 1, 11),
(430, 1, 26),
(431, 1, 27),
(432, 1, 28),
(433, 1, 29),
(434, 1, 30);

-- 重置序列值，确保后续插入的ID从435开始
SELECT setval('sys_role_menu_id_seq', 435);

-- 插入默认班次配置
INSERT INTO duty_shift_config (id, shift_name, shift_code, shift_type, start_time, end_time, duration_hours, break_hours, is_overtime_shift, status, sort, is_cross_day) VALUES
(4, '全天班', 'FULL_DAY', 4, '08:00:00', '08:00:00', 16.00, 0.00, 0, 1, 4, 1),
(5, 'GOC夜班', 'GOCNIGHT', 5, '20:00:00', '08:00:00', 8.00, 0.00, 0, 1, 5, 1),
(6, 'SRE白班', 'SREDAY', 0, '08:30:00', '17:30:00', 8.00, 0.00, 0, 1, 4, 0),
(7, 'SRE值班', 'SREFULL', 5, '08:30:00', '08:30:00', 0.00, 0.00, 1, 1, 5, 1),
(8, 'GOC白班', 'GOCDAY', 0, '08:00:00', '20:00:00', 8.00, 0.00, 0, 1, 4, 0),
(9, '1', '132', 1, '16:22:57', '16:22:57', 8.00, 0.00, 0, 1, 0, 0),
(10, '2131', '13123', 1, '16:23:06', '16:23:06', 8.00, 0.00, 0, 1, 0, 0),
(11, '12312', '12321', 1, '16:23:12', '16:23:12', 8.00, 0.00, 0, 1, 0, 0),
(12, '432', '543213', 1, '16:23:18', '16:23:18', 8.00, 0.00, 0, 1, 0, 0),
(13, '324341', '1321', 1, '16:23:23', '16:23:23', 8.00, 0.00, 0, 1, 0, 0),
(14, '123', '5434', 1, '16:23:28', '16:40:28', 8.00, 0.00, 0, 1, 0, 0);

-- 重置序列值，确保后续插入的ID从15开始
SELECT setval('duty_shift_config_id_seq', 15);

-- 插入默认排班规则
INSERT INTO duty_schedule_rule (id, rule_name, rule_code, schedule_cycle, max_daily_shifts, max_weekly_hours, max_monthly_hours, min_rest_days, substitute_priority_rule, conflict_detection_rule, status, create_time, update_time) VALUES
(1, '默认排班规则', 'DEFAULT_RULE', 2, 3, 48.00, 200.00, 4, '同部门优先,其次按班次匹配', '检测重复排班、超负载排班', 1, '2026-01-17 14:59:03', '2026-01-17 14:59:03');

-- 重置序列值，确保后续插入的ID从2开始
SELECT setval('duty_schedule_rule_id_seq', 2);

-- Insert data for duty_schedule_mode
INSERT INTO duty_schedule_mode (id, mode_name, mode_code, mode_type, algorithm_class, config_json, description, status, sort, create_time, update_time) VALUES
(1, '单班制', 'SINGLE_SHIFT', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', '每天只有一个班次，适合小型团队', 0, 3, '2026-01-20 14:07:14', '2026-01-26 06:57:37'),
(2, '两班倒', 'TWO_SHIFTS', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', '早班和晚班交替，适合24小时服务', 0, 4, '2026-01-20 14:07:14', '2026-01-26 06:57:31'),
(3, '三班倒', 'THREE_SHIFTS', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', '早、中、晚三班循环，适合连续生产', 0, 5, '2026-01-20 14:07:14', '2026-01-26 06:57:24'),
(4, '四班三倒', 'FOUR_THREE_SHIFTS', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', '四个班，每班工作三天休息一天', 0, 6, '2026-01-20 14:07:14', '2026-01-26 06:57:19'),
(5, '弹性排班', 'FLEXIBLE_SHIFT', 3, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', '根据需求灵活安排班次', 0, 7, '2026-01-20 14:07:14', '2026-01-26 06:57:13'),
(6, 'GOC-白夜休休', 'GOC-BYXX', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}, {"name": "第4天", "shifts": [], "dayIndex": 4}], "teams": [{"shifts": [{"count": 2, "shiftId": "6", "shiftName": "SRE白班"}, {"count": 2, "shiftId": "5", "shiftName": "GOC夜班"}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}, {"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 2, "shiftId": "6", "shiftName": "SRE白班"}, {"count": 2, "shiftId": "5", "shiftName": "GOC夜班"}, {"count": 0, "shiftId": "", "shiftName": ""}]}, {"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 2, "shiftId": "6", "shiftName": "SRE白班"}, {"count": 2, "shiftId": "5", "shiftName": "GOC夜班"}]}, {"shifts": [{"count": 2, "shiftId": "5", "shiftName": "GOC夜班"}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 2, "shiftId": "6", "shiftName": "SRE白班"}]}]}', NULL, 0, 1, '2026-01-23 01:14:31', '2026-02-20 12:34:39'),
(7, 'GOC-24休休', 'GOC-24XX', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 2, "shiftId": "4", "shiftName": "全天班"}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}, {"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 2, "shiftId": "4", "shiftName": "全天班"}, {"count": 0, "shiftId": "", "shiftName": ""}]}, {"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 2, "shiftId": "4", "shiftName": "全天班"}]}]}', NULL, 1, 2, '2026-01-23 03:21:27', '2026-02-09 13:59:57'),
(8, '1231', '43242', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', NULL, 1, 0, '2026-02-21 08:23:56', '2026-02-21 08:23:56'),
(9, '3423', '213', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', NULL, 1, 0, '2026-02-21 08:24:03', '2026-02-21 08:24:03'),
(10, '4543', '1231', 1, NULL, '{"days": [{"name": "第一天", "shifts": [], "dayIndex": 1}, {"name": "第二天", "shifts": [], "dayIndex": 2}, {"name": "第三天", "shifts": [], "dayIndex": 3}], "teams": [{"shifts": [{"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}, {"count": 0, "shiftId": "", "shiftName": ""}]}]}', NULL, 1, 0, '2026-02-21 08:24:07', '2026-02-21 08:24:07');

-- Reset sequence for duty_schedule_mode
SELECT setval('duty_schedule_mode_id_seq', 11);

-- 插入默认排班表
INSERT INTO duty_schedule (id, schedule_name, description, start_date, end_date, status, create_by, create_time, update_time, schedule_mode_id) VALUES
(1, 'GOC组值班2026', '用排班模式-24休休', '2026-01-01', '2026-12-31', 1, NULL, '2026-01-17 15:58:57', '2026-01-17 15:58:57', NULL),
(2, 'SRE组值班2026', '上五休二，每天2人轮值', '2026-01-01', '2026-12-31', 1, NULL, '2026-01-20 13:52:25', '2026-01-20 13:52:25', NULL),
(3, '1231', '', '2026-02-02', '2026-02-12', 1, NULL, '2026-02-21 08:55:30', '2026-02-21 08:55:30', NULL),
(4, '2315', '', '2026-02-03', '2026-02-13', 1, NULL, '2026-02-21 08:55:36', '2026-02-21 08:55:36', NULL),
(5, '43543', '', '2026-01-26', '2026-02-12', 1, NULL, '2026-02-21 08:55:41', '2026-02-21 08:55:41', NULL),
(6, '45234', '', '2026-02-03', '2026-02-11', 1, NULL, '2026-02-21 08:55:46', '2026-02-21 08:55:46', NULL),
(7, '43543', '', '2026-02-09', '2026-02-19', 1, NULL, '2026-02-21 08:55:52', '2026-02-21 08:55:52', NULL),
(8, '43542', '', '2026-02-02', '2026-02-26', 1, NULL, '2026-02-21 08:55:58', '2026-02-21 08:55:58', NULL),
(9, '43123', '', '2026-02-03', '2026-02-19', 1, NULL, '2026-02-21 08:56:03', '2026-02-21 08:56:03', NULL),
(10, '35412', '', '2026-02-02', '2026-02-17', 1, NULL, '2026-02-21 08:56:08', '2026-02-21 08:56:08', NULL),
(11, '4332', '', '2026-02-03', '2026-02-25', 1, NULL, '2026-02-21 08:56:14', '2026-02-21 08:56:14', NULL);

-- 重置序列值，确保后续插入的ID从12开始
SELECT setval('duty_schedule_id_seq', 12);

-- 插入值班表人员关联
INSERT INTO duty_schedule_employee (id, schedule_id, employee_id, sort_order, create_time, is_leader) VALUES
(275, 2, 10, 0, '2026-02-21 08:22:45', 1),
(276, 2, 12, 1, '2026-02-21 08:22:45', 0),
(277, 2, 13, 2, '2026-02-21 08:22:45', 0),
(278, 2, 14, 3, '2026-02-21 08:22:45', 0),
(279, 2, 15, 4, '2026-02-21 08:22:45', 0),
(280, 2, 16, 5, '2026-02-21 08:22:45', 1),
(281, 2, 17, 6, '2026-02-21 08:22:45', 0),
(282, 2, 18, 7, '2026-02-21 08:22:45', 0),
(283, 2, 20, 8, '2026-02-21 08:22:45', 0),
(284, 2, 21, 9, '2026-02-21 08:22:45', 0),
(285, 2, 22, 10, '2026-02-21 08:22:45', 0),
(286, 1, 10, 0, '2026-02-21 08:22:48', 1),
(287, 1, 12, 1, '2026-02-21 08:22:48', 0),
(288, 1, 13, 2, '2026-02-21 08:22:48', 0),
(289, 1, 14, 3, '2026-02-21 08:22:48', 0),
(290, 1, 15, 4, '2026-02-21 08:22:48', 0),
(291, 1, 16, 5, '2026-02-21 08:22:48', 1),
(292, 1, 17, 6, '2026-02-21 08:22:48', 0),
(293, 1, 18, 7, '2026-02-21 08:22:48', 0),
(294, 1, 20, 8, '2026-02-21 08:22:48', 0),
(295, 1, 21, 9, '2026-02-21 08:22:48', 0),
(296, 1, 22, 10, '2026-02-21 08:22:48', 0);

-- 重置序列值，确保后续插入的ID从297开始
SELECT setval('duty_schedule_employee_id_seq', 297);

-- 插入值班表班次关联
INSERT INTO duty_schedule_shift (id, schedule_id, shift_config_id, sort_order, create_time) VALUES
(8, 2, 6, 0, '2026-02-21 08:22:45'),
(9, 2, 7, 1, '2026-02-21 08:22:45'),
(10, 1, 4, 0, '2026-02-21 08:22:48');

-- 重置序列值，确保后续插入的ID从11开始
SELECT setval('duty_schedule_shift_id_seq', 11);

-- ========================================
-- 值班相关数据
-- ========================================

--
-- Dumping data for table `duty_assignment`
--

INSERT INTO duty_assignment (id, schedule_id, duty_date, duty_shift, employee_id, status, remark, create_time, update_time, shift_config_id, version_id, is_overtime) VALUES
(8426, 1, '2026-01-29', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8427, 1, '2026-01-29', 4, 14, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8428, 1, '2026-01-30', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8429, 1, '2026-01-30', 4, 15, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8430, 1, '2026-01-31', 4, 13, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8431, 1, '2026-02-01', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8432, 1, '2026-02-01', 4, 14, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8433, 1, '2026-02-02', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-20 12:32:45', NULL, NULL, 0),
(8434, 1, '2026-02-02', 4, 15, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8435, 1, '2026-02-03', 4, 13, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8436, 1, '2026-02-04', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8437, 1, '2026-02-04', 4, 14, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8438, 1, '2026-02-05', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8439, 1, '2026-02-05', 4, 15, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8440, 1, '2026-02-06', 4, 13, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8441, 1, '2026-02-07', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8442, 1, '2026-02-07', 4, 14, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8443, 1, '2026-02-08', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8444, 1, '2026-02-08', 4, 15, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8445, 1, '2026-02-09', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8446, 1, '2026-02-10', 4, 21, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(8447, 1, '2026-02-10', 4, 14, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9220, 1, '2026-02-01', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9221, 1, '2026-02-01', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9228, 1, '2026-02-04', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9229, 1, '2026-02-05', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9236, 1, '2026-02-09', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9237, 1, '2026-02-09', 4, 12, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9244, 1, '2026-02-12', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9252, 1, '2026-02-16', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9260, 1, '2026-02-20', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9268, 1, '2026-02-24', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0),
(9270, 1, '2026-02-25', 4, 10, 1, '', '2026-02-19 19:25:09', '2026-02-19 19:25:09', NULL, NULL, 0);

--
-- Dumping data for table `duty_holiday`
--

INSERT INTO duty_holiday (id, holiday_name, holiday_date, is_workday, holiday_type, remark, create_time, update_time) VALUES
(183, '元旦', '2026-01-01', 0, 1, '元旦', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(184, '元旦', '2026-01-02', 0, 1, '元旦', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(185, '元旦', '2026-01-03', 0, 1, '元旦', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(186, '元旦后补班', '2026-01-04', 1, 2, '元旦后补班', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(187, '春节前补班', '2026-02-14', 1, 2, '春节前补班', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(188, '春节', '2026-02-15', 0, 1, '春节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(189, '除夕', '2026-02-16', 0, 1, '除夕', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(190, '初一', '2026-02-17', 0, 1, '初一', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(191, '初二', '2026-02-18', 0, 1, '初二', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(192, '初三', '2026-02-19', 0, 1, '初三', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(193, '初四', '2026-02-20', 0, 1, '初四', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(194, '初五', '2026-02-21', 0, 1, '初五', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(195, '初六', '2026-02-22', 0, 1, '初六', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(196, '初七', '2026-02-23', 0, 1, '初七', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(197, '春节后补班', '2026-02-28', 1, 2, '春节后补班', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(198, '清明节', '2026-04-04', 0, 1, '清明节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(199, '清明节', '2026-04-05', 0, 1, '清明节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(200, '清明节', '2026-04-06', 0, 1, '清明节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(201, '劳动节', '2026-05-01', 0, 1, '劳动节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(202, '劳动节', '2026-05-02', 0, 1, '劳动节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(203, '劳动节', '2026-05-03', 0, 1, '劳动节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(204, '劳动节', '2026-05-04', 0, 1, '劳动节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(205, '劳动节', '2026-05-05', 0, 1, '劳动节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(206, '劳动节后补班', '2026-05-09', 1, 2, '劳动节后补班', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(207, '端午节', '2026-06-19', 0, 1, '端午节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(208, '端午节', '2026-06-20', 0, 1, '端午节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(209, '端午节', '2026-06-21', 0, 1, '端午节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(210, '中秋节', '2026-09-26', 0, 1, '中秋节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(211, '中秋节', '2026-09-27', 0, 1, '中秋节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(212, '中秋节后补班', '2026-09-28', 1, 2, '中秋节后补班', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(213, '国庆节', '2026-10-01', 0, 1, '国庆节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(214, '国庆节', '2026-10-02', 0, 1, '国庆节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(215, '国庆节', '2026-10-03', 0, 1, '国庆节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(216, '国庆节', '2026-10-04', 0, 1, '国庆节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(217, '国庆节', '2026-10-05', 0, 1, '国庆节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(218, '国庆节', '2026-10-06', 0, 1, '国庆节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(219, '国庆节', '2026-10-07', 0, 1, '国庆节', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(220, '国庆节后补班', '2026-10-11', 1, 2, '国庆节后补班', '2026-01-25 19:30:30', '2026-01-25 19:30:30'),
(221, '国庆节后补班', '2026-10-12', 1, 2, '国庆节后补班', '2026-01-25 19:30:30', '2026-01-25 19:30:30');

-- 重置序列值，确保后续插入的ID从222开始
SELECT setval('duty_holiday_id_seq', 222);

--
-- Dumping data for table `duty_record`
--

INSERT INTO duty_record VALUES (36, 9228, 10, '2026-02-04 23:32:00', '2026-02-05 15:32:00', 2, '', '', 16.00, '待审批', '', '2026-02-21 08:32:20', '2026-02-21 08:32:20', NULL, 1);
INSERT INTO duty_record VALUES (37, 9236, 10, '2026-02-09 04:37:00', '2026-02-09 11:37:00', 0, '', '', 7.00, '待审批', '', '2026-02-21 11:37:59', '2026-02-21 11:37:59', NULL, 1);
INSERT INTO duty_record VALUES (38, 9220, 10, '2026-02-01 04:37:00', '2026-02-01 11:37:00', 0, '', '', 7.00, '待审批', '', '2026-02-21 11:38:08', '2026-02-21 11:38:08', NULL, 1);
INSERT INTO duty_record VALUES (39, 9244, 10, '2026-02-12 20:37:00', '2026-02-13 11:37:00', 0, '', '', 15.00, '待审批', '', '2026-02-21 11:38:36', '2026-02-21 11:38:36', NULL, 1);
INSERT INTO duty_record VALUES (40, 9252, 10, '2026-02-16 20:37:00', '2026-02-17 11:37:00', 0, '', '', 15.00, '待审批', '', '2026-02-21 11:38:50', '2026-02-21 11:38:50', NULL, 1);
INSERT INTO duty_record VALUES (41, 9260, 10, '2026-02-20 20:37:00', '2026-02-21 11:37:00', 0, '', '', 15.00, '待审批', '', '2026-02-21 11:38:59', '2026-02-21 11:38:59', NULL, 1);
INSERT INTO duty_record VALUES (42, 9268, 10, '2026-02-24 20:37:00', '2026-02-25 11:37:00', 0, '', '', 15.00, '待审批', '', '2026-02-21 11:39:06', '2026-02-21 11:39:06', NULL, 1);
INSERT INTO duty_record VALUES (43, 9270, 10, '2026-02-25 20:37:00', '2026-02-26 11:37:00', 0, '', '', 15.00, '待审批', '', '2026-02-21 11:39:15', '2026-02-21 11:39:15', NULL, 1);
INSERT INTO duty_record VALUES (44, 9221, 12, '2026-02-01 06:36:00', '2026-02-01 14:36:00', 2, '', '', 8.00, '待审批', '', '2026-02-21 14:36:28', '2026-02-21 14:36:28', NULL, 1);
INSERT INTO duty_record VALUES (45, 9229, 12, '2026-02-05 06:36:00', '2026-02-05 14:36:00', 2, '', '', 8.00, '待审批', '', '2026-02-21 14:36:36', '2026-02-21 14:36:36', NULL, 1);
INSERT INTO duty_record VALUES (46, 9237, 12, '2026-02-09 06:36:00', '2026-02-09 14:36:00', 2, '', '', 8.00, '待审批', '', '2026-02-21 14:36:43', '2026-02-21 14:36:43', NULL, 1);

-- 重置序列值，确保后续插入的ID从47开始
SELECT setval('duty_record_id_seq', 47);

-- 移除重复的数据插入语句，这些数据已经在前面的部分插入过了

--
-- Dumping data for table `duty_shift_mutex`
--

INSERT INTO duty_shift_mutex VALUES (13, 8, 4, '2026-02-09 03:13:54', '2026-02-09 03:13:54');
INSERT INTO duty_shift_mutex VALUES (14, 8, 6, '2026-02-09 03:13:54', '2026-02-09 03:13:54');
INSERT INTO duty_shift_mutex VALUES (15, 8, 7, '2026-02-09 03:13:54', '2026-02-09 03:13:54');
INSERT INTO duty_shift_mutex VALUES (17, 5, 4, '2026-02-09 03:14:18', '2026-02-09 03:14:18');
INSERT INTO duty_shift_mutex VALUES (18, 5, 6, '2026-02-09 03:14:18', '2026-02-09 03:14:18');
INSERT INTO duty_shift_mutex VALUES (19, 5, 7, '2026-02-09 03:14:18', '2026-02-09 03:14:18');
INSERT INTO duty_shift_mutex VALUES (24, 6, 4, '2026-02-09 03:14:37', '2026-02-09 03:14:37');
INSERT INTO duty_shift_mutex VALUES (25, 6, 5, '2026-02-09 03:14:37', '2026-02-09 03:14:37');
INSERT INTO duty_shift_mutex VALUES (26, 6, 8, '2026-02-09 03:14:37', '2026-02-09 03:14:37');
INSERT INTO duty_shift_mutex VALUES (30, 4, 5, '2026-02-09 14:23:22', '2026-02-09 14:23:22');
INSERT INTO duty_shift_mutex VALUES (31, 4, 6, '2026-02-09 14:23:22', '2026-02-09 14:23:22');
INSERT INTO duty_shift_mutex VALUES (32, 4, 7, '2026-02-09 14:23:22', '2026-02-09 14:23:22');
INSERT INTO duty_shift_mutex VALUES (33, 4, 8, '2026-02-09 14:23:22', '2026-02-09 14:23:22');
INSERT INTO duty_shift_mutex VALUES (49, 7, 4, '2026-02-20 12:34:24', '2026-02-20 12:34:24');
INSERT INTO duty_shift_mutex VALUES (50, 7, 5, '2026-02-20 12:34:24', '2026-02-20 12:34:24');
INSERT INTO duty_shift_mutex VALUES (51, 7, 8, '2026-02-20 12:34:24', '2026-02-20 12:34:24');

-- 重置序列值，确保后续插入的ID从52开始
SELECT setval('duty_shift_mutex_id_seq', 52);

-- =============================================
-- 项目管理相关表
-- =============================================

--
-- Table structure for table `pm_project`
--

DROP TABLE IF EXISTS pm_project CASCADE;
CREATE TABLE pm_project (
  id BIGSERIAL PRIMARY KEY,
  project_name VARCHAR(100) NOT NULL,
  project_code VARCHAR(50) NOT NULL,
  module_id BIGINT DEFAULT NULL,
  priority INTEGER DEFAULT '3',
  status INTEGER DEFAULT '1',
  progress INTEGER DEFAULT '0',
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  description VARCHAR(200) DEFAULT NULL,
  owner_id BIGINT DEFAULT NULL,
  create_by BIGINT DEFAULT NULL,
  archived INTEGER DEFAULT '0',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add comments for table and columns
COMMENT ON TABLE pm_project IS '项目表';
COMMENT ON COLUMN pm_project.id IS '项目ID';
COMMENT ON COLUMN pm_project.project_name IS '项目名称';
COMMENT ON COLUMN pm_project.project_code IS '项目编码';
COMMENT ON COLUMN pm_project.module_id IS '模块ID';
COMMENT ON COLUMN pm_project.priority IS '优先级：1-高，2-中，3-低';
COMMENT ON COLUMN pm_project.status IS '状态：1-进行中，2-已完成，3-已暂停，4-已取消';
COMMENT ON COLUMN pm_project.progress IS '进度百分比';
COMMENT ON COLUMN pm_project.start_date IS '开始日期';
COMMENT ON COLUMN pm_project.end_date IS '结束日期';
COMMENT ON COLUMN pm_project.description IS '项目描述';
COMMENT ON COLUMN pm_project.owner_id IS '负责人ID';
COMMENT ON COLUMN pm_project.create_by IS '创建人ID';
COMMENT ON COLUMN pm_project.archived IS '是否归档：0-否，1-是';
COMMENT ON COLUMN pm_project.create_time IS '创建时间';
COMMENT ON COLUMN pm_project.update_time IS '更新时间';

-- Create indexes for pm_project
CREATE UNIQUE INDEX uk_project_code ON pm_project (project_code);
CREATE INDEX idx_owner_id ON pm_project (owner_id);
CREATE INDEX idx_status ON pm_project (status);
CREATE INDEX idx_archived ON pm_project (archived);

--
-- Table structure for table `pm_task`
--

DROP TABLE IF EXISTS pm_task;
CREATE TABLE pm_task (
  id BIGSERIAL PRIMARY KEY,
  task_name VARCHAR(100) NOT NULL,
  task_code VARCHAR(50) NOT NULL,
  project_id BIGINT NOT NULL,
  description VARCHAR(200) DEFAULT NULL,
  start_date DATE DEFAULT NULL,
  end_date DATE DEFAULT NULL,
  status SMALLINT DEFAULT '1',
  priority SMALLINT DEFAULT '3',
  assignee_id BIGINT DEFAULT NULL,
  create_by BIGINT DEFAULT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pm_task_ibfk_1 FOREIGN KEY (project_id) REFERENCES pm_project (id) ON DELETE CASCADE,
  CONSTRAINT pm_task_ibfk_2 FOREIGN KEY (assignee_id) REFERENCES sys_employee (id) ON DELETE SET NULL
);

-- Add comments for table and columns
COMMENT ON TABLE pm_task IS '任务表';
COMMENT ON COLUMN pm_task.id IS '任务ID';
COMMENT ON COLUMN pm_task.task_name IS '任务名称';
COMMENT ON COLUMN pm_task.task_code IS '任务编码';
COMMENT ON COLUMN pm_task.project_id IS '项目ID';
COMMENT ON COLUMN pm_task.description IS '任务描述';
COMMENT ON COLUMN pm_task.start_date IS '开始日期';
COMMENT ON COLUMN pm_task.end_date IS '结束日期';
COMMENT ON COLUMN pm_task.status IS '状态：1-未开始，2-进行中，3-已完成，4-已暂停，5-已取消';
COMMENT ON COLUMN pm_task.priority IS '优先级：1-高，2-中，3-低';
COMMENT ON COLUMN pm_task.assignee_id IS '负责人ID';
COMMENT ON COLUMN pm_task.create_by IS '创建人ID';
COMMENT ON COLUMN pm_task.create_time IS '创建时间';
COMMENT ON COLUMN pm_task.update_time IS '更新时间';

-- Create indexes for pm_task
CREATE UNIQUE INDEX uk_task_code ON pm_task (task_code);
CREATE INDEX idx_project_id ON pm_task (project_id);
CREATE INDEX idx_status ON pm_task (status);
CREATE INDEX idx_priority ON pm_task (priority);
CREATE INDEX idx_assignee_id ON pm_task (assignee_id);

-- =============================================
-- 项目管理相关数据
-- =============================================

-- 插入默认项目
INSERT INTO pm_project (id, project_name, project_code, priority, status, progress, start_date, end_date, description, owner_id, create_by, archived, create_time, update_time) VALUES
(1, 'Hyper Duty系统开发', 'HYPER_DUTY', 1, 2, 100, '2026-01-01', '2026-02-20', 'Hyper Duty值班管理系统开发项目', 10, 10, 0, '2026-01-01 10:00:00', '2026-02-20 18:00:00'),
(2, 'Spring Boot 3升级', 'SPRING_BOOT_UPGRADE', 2, 1, 50, '2026-02-01', '2026-03-01', '将系统从Spring Boot 2升级到Spring Boot 3', 12, 12, 0, '2026-02-01 09:00:00', '2026-02-22 10:00:00'),
(3, '前端优化', 'FRONTEND_OPTIMIZATION', 3, 1, 30, '2026-02-15', '2026-03-15', '优化前端性能和用户体验', 14, 14, 0, '2026-02-15 14:00:00', '2026-02-22 10:00:00');

-- 重置序列值，确保后续插入的ID从4开始
SELECT setval('pm_project_id_seq', 4);

-- 插入默认任务
INSERT INTO pm_task (id, task_name, task_code, project_id, description, start_date, end_date, status, priority, assignee_id, create_by, create_time, update_time) VALUES
(1, '数据库设计', 'DB_DESIGN', 1, '设计系统数据库表结构', '2026-01-01', '2026-01-10', 3, 1, 10, 10, '2026-01-01 10:00:00', '2026-01-10 18:00:00'),
(2, '后端API开发', 'BACKEND_API', 1, '开发系统后端API接口', '2026-01-10', '2026-01-30', 3, 1, 12, 10, '2026-01-10 10:00:00', '2026-01-30 18:00:00'),
(3, '前端页面开发', 'FRONTEND_PAGE', 1, '开发系统前端页面', '2026-01-20', '2026-02-10', 3, 1, 14, 10, '2026-01-20 10:00:00', '2026-02-10 18:00:00'),
(4, '系统测试', 'SYSTEM_TEST', 1, '进行系统功能测试', '2026-02-10', '2026-02-20', 3, 1, 16, 10, '2026-02-10 10:00:00', '2026-02-20 18:00:00'),
(5, 'Spring Boot 3依赖更新', 'SPRING_BOOT_DEPS', 2, '更新项目依赖到Spring Boot 3', '2026-02-01', '2026-02-10', 3, 1, 12, 12, '2026-02-01 09:00:00', '2026-02-10 18:00:00'),
(6, '代码适配', 'CODE_ADAPT', 2, '适配Spring Boot 3的API变更', '2026-02-10', '2026-02-20', 2, 1, 12, 12, '2026-02-10 09:00:00', '2026-02-22 10:00:00'),
(7, '性能测试', 'PERF_TEST', 2, '进行性能测试', '2026-02-20', '2026-03-01', 1, 1, 16, 12, '2026-02-20 09:00:00', '2026-02-22 10:00:00'),
(8, '前端组件优化', 'COMPONENT_OPT', 3, '优化前端组件性能', '2026-02-15', '2026-02-25', 2, 2, 14, 14, '2026-02-15 14:00:00', '2026-02-22 10:00:00'),
(9, '用户体验改进', 'UX_IMPROVE', 3, '改进用户界面和交互体验', '2026-02-25', '2026-03-10', 1, 2, 14, 14, '2026-02-25 14:00:00', '2026-02-22 10:00:00'),
(10, '响应式设计优化', 'RESPONSIVE_OPT', 3, '优化系统的响应式设计', '2026-03-10', '2026-03-15', 1, 3, 14, 14, '2026-03-10 14:00:00', '2026-02-22 10:00:00');

-- 重置序列值，确保后续插入的ID从11开始
SELECT setval('pm_task_id_seq', 11);

--
-- PostgreSQL 转换完成
--

SELECT 'Hyper Duty PostgreSQL 脚本执行完成！共创建27个表' AS message;