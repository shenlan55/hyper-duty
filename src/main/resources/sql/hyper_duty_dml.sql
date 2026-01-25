-- ========================================
-- Hyper Duty 值班管理系统 DML 脚本
-- 包含所有初始数据插入语句
-- ========================================

USE hyper_duty;

-- ========================================
-- 第一部分：系统基础数据
-- ========================================

-- 插入默认部门
INSERT IGNORE INTO sys_dept (dept_name, parent_id, dept_code, sort, status) VALUES
('总公司', 0, '001', 1, 1),
('技术部', 1, '002', 2, 1),
('人事部', 1, '003', 3, 1),
('财务部', 1, '004', 4, 1);

-- 插入默认人员
INSERT IGNORE INTO sys_employee (employee_name, dept_id, employee_code, phone, email, gender, status) VALUES
('管理员', 1, 'EMP001', '13800138000', 'admin@example.com', 1, 1),
('张三', 2, 'EMP002', '13800138001', 'zhangsan@example.com', 1, 1),
('李四', 2, 'EMP003', '13800138002', 'lisi@example.com', 1, 1),
('王五', 3, 'EMP004', '13800138003', 'wangwu@example.com', 2, 1);

-- 插入默认用户（密码为123456的BCrypt加密）
INSERT IGNORE INTO sys_user (username, password, employee_id, status) VALUES
('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 1, 1),
('zhangsan', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 2, 1),
('lisi', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 3, 1),
('wangwu', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 4, 1);

-- 插入默认角色
INSERT IGNORE INTO sys_role (role_name, role_code, description, status) VALUES
('超级管理员', 'ROLE_ADMIN', '拥有系统所有权限', 1),
('普通用户', 'ROLE_USER', '拥有基础操作权限', 1);

-- 插入系统基础菜单
INSERT IGNORE INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status) VALUES
('首页', 0, '/dashboard', 'views/Dashboard.vue', '', 1, 'HomeFilled', 1, 1),
('系统管理', 0, '', '', '', 1, 'Setting', 2, 1),
('部门管理', 2, '/dept', 'views/Dept.vue', 'sys:dept:list', 2, 'OfficeBuilding', 1, 1),
('人员管理', 2, '/employee', 'views/Employee.vue', 'sys:employee:list', 2, 'User', 2, 1),
('用户管理', 2, '/user', 'views/User.vue', 'sys:user:list', 2, 'Avatar', 3, 1),
('菜单管理', 2, '/menu', 'views/Menu.vue', 'sys:menu:list', 2, 'Menu', 4, 1),
('角色管理', 2, '/role', 'views/Role.vue', 'sys:role:list', 2, 'DocumentCopy', 5, 1),
('字典管理', 2, '/dict', 'views/Dict.vue', 'sys:dict:list', 2, 'List', 6, 1),
('值班管理', 0, '', '', '', 1, 'Calendar', 3, 1),
('值班表管理', 8, '/duty/schedule', 'views/duty/DutySchedule.vue', 'duty:schedule:list', 2, 'DocumentCopy', 1, 1),
('值班安排', 8, '/duty/assignment', 'views/duty/DutyAssignment.vue', 'duty:assignment:list', 2, 'Calendar', 2, 1),
('值班记录', 8, '/duty/record', 'views/duty/DutyRecord.vue', 'duty:record:list', 2, 'Document', 3, 1);

-- 插入用户角色关联
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES
(1, 1), -- admin 关联 超级管理员
(2, 2), -- zhangsan 关联 普通用户
(3, 2), -- lisi 关联 普通用户
(4, 2); -- wangwu 关联 普通用户

-- 超级管理员拥有所有菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;

-- 普通用户拥有基础菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 1), -- 首页
(2, 2), -- 系统管理
(2, 3), -- 部门管理
(2, 4), -- 人员管理
(2, 5), -- 用户管理
(2, 8), -- 值班管理
(2, 9), -- 值班表管理
(2, 10), -- 值班安排
(2, 11), -- 值班记录
(2, 12); -- 字典管理

-- ========================================
-- 第二部分：值班管理扩展数据
-- ========================================

-- 插入值班管理扩展菜单
INSERT IGNORE INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status) VALUES
('班次配置', 8, '/duty/shift-config', 'views/duty/ShiftConfig.vue', 'duty:shift:list', 2, 'Clock', 4, 1),
('排班模式管理', 8, '/duty/schedule-mode', 'views/duty/ScheduleMode.vue', 'duty:schedule:mode:list', 2, 'Operation', 5, 1),
('请假申请', 8, '/duty/leave-request', 'views/duty/LeaveRequest.vue', 'duty:leave:list', 2, 'Document', 6, 1),
('请假审批', 8, '/duty/leave-approval', 'views/duty/LeaveApproval.vue', 'duty:leave:approve', 2, 'CircleCheck', 7, 1),
('调班管理', 8, '/duty/swap-request', 'views/duty/SwapRequest.vue', 'duty:swap:list', 2, 'Refresh', 8, 1),
('排班统计', 8, '/duty/statistics', 'views/duty/Statistics.vue', 'duty:statistics:view', 2, 'DataAnalysis', 9, 1),
('操作日志', 8, '/duty/operation-log', 'views/duty/OperationLog.vue', 'duty:log:list', 2, 'Document', 10, 1);

-- 普通用户增加值班管理菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, 14), -- 班次配置
(2, 15), -- 排班模式管理
(2, 16), -- 请假申请
(2, 17), -- 请假审批
(2, 18), -- 调班管理
(2, 19), -- 排班统计
(2, 20); -- 操作日志

-- 插入默认班次配置
INSERT IGNORE INTO duty_shift_config (shift_name, shift_code, shift_type, start_time, end_time, duration_hours, break_hours, is_overtime_shift, status, sort) VALUES
('早班', 'MORNING', 1, '08:00:00', '12:00:00', 4.00, 0, 0, 1, 1),
('中班', 'AFTERNOON', 2, '14:00:00', '18:00:00', 4.00, 0, 0, 1, 2),
('晚班', 'EVENING', 3, '18:00:00', '22:00:00', 4.00, 0, 0, 1, 3),
('全天班', 'FULL_DAY', 4, '08:00:00', '17:00:00', 8.00, 1, 0, 1, 4),
('夜班', 'NIGHT', 5, '22:00:00', '06:00:00', 8.00, 0, 0, 1, 5);

-- 插入默认排班规则
INSERT IGNORE INTO duty_schedule_rule (rule_name, rule_code, schedule_cycle, max_daily_shifts, max_weekly_hours, max_monthly_hours, min_rest_days, substitute_priority_rule, conflict_detection_rule, status) VALUES
('默认排班规则', 'DEFAULT_RULE', 2, 3, 48.00, 200.00, 4, '同部门优先,其次按班次匹配', '检测重复排班、超负载排班', 1);

-- 插入默认排班模式
INSERT IGNORE INTO duty_schedule_mode (mode_name, mode_code, mode_type, algorithm_class, description, status, sort) VALUES
('单班制', 'SINGLE_SHIFT', 1, 'com.lasu.hyperduty.service.algorithm.impl.DaytimeAllNightShiftRotationAlgorithm', '每天只有一个班次，适合小型团队', 1, 1),
('两班倒', 'TWO_SHIFTS', 1, 'com.lasu.hyperduty.service.algorithm.impl.ThreeDayRotationAlgorithm', '早班和晚班交替，适合24小时服务', 1, 2),
('三班倒', 'THREE_SHIFTS', 1, 'com.lasu.hyperduty.service.algorithm.impl.ThreeDayRotationAlgorithm', '早、中、晚三班循环，适合连续生产', 1, 3),
('四班三倒', 'FOUR_THREE_SHIFTS', 1, 'com.lasu.hyperduty.service.algorithm.impl.FourDayRotationAlgorithm', '四个班，每班工作三天休息一天', 1, 4),
('弹性排班', 'FLEXIBLE_SHIFT', 3, 'com.lasu.hyperduty.service.algorithm.impl.DaytimeAllNightShiftRotationAlgorithm', '根据需求灵活安排班次', 1, 5);

-- ========================================
-- 第三部分：更新系统管理菜单路径
-- ========================================

-- 更新系统管理菜单路径
UPDATE sys_menu SET path = '/system' WHERE menu_name = '系统管理' AND parent_id = 0;
UPDATE sys_menu SET path = '/system/dept' WHERE menu_name = '部门管理';
UPDATE sys_menu SET path = '/system/employee' WHERE menu_name = '人员管理';
UPDATE sys_menu SET path = '/system/user' WHERE menu_name = '用户管理';
UPDATE sys_menu SET path = '/system/menu' WHERE menu_name = '菜单管理';
UPDATE sys_menu SET path = '/system/role' WHERE menu_name = '角色管理';
UPDATE sys_menu SET path = '/system/dict' WHERE menu_name = '字典管理';

-- 将操作日志菜单移动到系统管理下，并设置正确的排序
UPDATE sys_menu SET parent_id = (SELECT id FROM sys_menu WHERE menu_name = '系统管理' AND parent_id = 0), 
                   path = '/system/operation-log', 
                   perm = 'sys:log:list',
                   sort = 7 
WHERE menu_name = '操作日志';

-- 插入定时任务菜单
INSERT IGNORE INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status) VALUES
('定时任务', (SELECT id FROM sys_menu WHERE menu_name = '系统管理' AND parent_id = 0), '/system/schedule-job', 'views/system/ScheduleJob.vue', 'sys:schedule:job:list', 2, 'Timer', 8, 1);

-- 普通用户增加定时任务菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
(2, (SELECT id FROM sys_menu WHERE menu_name = '定时任务'));

-- ========================================
-- DML 脚本执行完成
-- ========================================
SELECT 'Hyper Duty DML 脚本执行完成！共插入17个菜单、5个班次配置、1个排班规则，已更新系统管理菜单路径' AS message;
