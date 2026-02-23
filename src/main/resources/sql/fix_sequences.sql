-- =============================================
-- Hyper Duty 系统序列修复脚本
-- 功能：检查并修复所有自增ID序列，确保序列值大于当前最大ID
-- 生成时间：2026-02-23
-- =============================================

DO $$
DECLARE
    v_table_name TEXT;
    v_column_name TEXT;
    v_sequence_name TEXT;
    v_max_id BIGINT;
    v_current_seq BIGINT;
    v_sql TEXT;
BEGIN
    -- 定义需要检查的表和序列映射
    FOR v_table_name, v_column_name, v_sequence_name IN (
        VALUES
        ('approval_record', 'id', 'approval_record_id_seq'),
        ('duty_assignment', 'id', 'duty_assignment_id_seq'),
        ('duty_holiday', 'id', 'duty_holiday_id_seq'),
        ('duty_record', 'id', 'duty_record_id_seq'),
        ('duty_schedule', 'id', 'duty_schedule_id_seq'),
        ('duty_schedule_employee', 'id', 'duty_schedule_employee_id_seq'),
        ('duty_schedule_mode', 'id', 'duty_schedule_mode_id_seq'),
        ('duty_schedule_rule', 'id', 'duty_schedule_rule_id_seq'),
        ('duty_schedule_shift', 'id', 'duty_schedule_shift_id_seq'),
        ('duty_shift_config', 'id', 'duty_shift_config_id_seq'),
        ('duty_shift_mutex', 'id', 'duty_shift_mutex_id_seq'),
        ('employee_available_time', 'id', 'employee_available_time_id_seq'),
        ('leave_request', 'id', 'leave_request_id_seq'),
        ('leave_substitute', 'id', 'leave_substitute_id_seq'),
        ('operation_log', 'id', 'operation_log_id_seq'),
        ('pm_project', 'id', 'pm_project_id_seq'),
        ('pm_task', 'id', 'pm_task_id_seq'),
        ('schedule_version', 'id', 'schedule_version_id_seq'),
        ('swap_request', 'id', 'swap_request_id_seq'),
        ('sys_dept', 'id', 'sys_dept_id_seq'),
        ('sys_dict_data', 'id', 'sys_dict_data_id_seq'),
        ('sys_dict_type', 'id', 'sys_dict_type_id_seq'),
        ('sys_employee', 'id', 'sys_employee_id_seq'),
        ('sys_menu', 'id', 'sys_menu_id_seq'),
        ('sys_role', 'id', 'sys_role_id_seq'),
        ('sys_role_menu', 'id', 'sys_role_menu_id_seq'),
        ('sys_schedule_job', 'id', 'sys_schedule_job_id_seq'),
        ('sys_schedule_log', 'id', 'sys_schedule_log_id_seq'),
        ('sys_user_role', 'id', 'sys_user_role_id_seq')
    ) LOOP
        -- 检查表是否存在
        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = v_table_name) THEN
            -- 获取当前最大ID
            v_sql := 'SELECT COALESCE(MAX(' || quote_ident(v_column_name) || '), 0) FROM ' || quote_ident(v_table_name);
            EXECUTE v_sql INTO v_max_id;
            
            -- 获取当前序列值
            v_sql := 'SELECT last_value FROM ' || quote_ident(v_sequence_name);
            EXECUTE v_sql INTO v_current_seq;
            
            -- 如果序列值小于等于最大ID，则重置序列
            IF v_current_seq <= v_max_id THEN
                RAISE NOTICE '修复表 %: 最大ID=%, 序列值=%', v_table_name, v_max_id, v_current_seq;
                v_sql := 'SELECT setval(''' || quote_ident(v_sequence_name) || ''', ' || (v_max_id + 1) || ', false)';
                EXECUTE v_sql;
                RAISE NOTICE '已将 % 序列重置为 %', v_sequence_name, v_max_id + 1;
            ELSE
                RAISE NOTICE '表 % 正常: 最大ID=%, 序列值=%', v_table_name, v_max_id, v_current_seq;
            END IF;
        ELSE
            RAISE NOTICE '表 % 不存在，跳过', v_table_name;
        END IF;
    END LOOP;
    
    RAISE NOTICE '序列修复完成！';
END $$;

-- =============================================
-- 单独查询每个表的序列状态（用于验证）
-- =============================================

-- sys_dept
SELECT 'sys_dept' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_dept_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_dept_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_dept
UNION ALL

-- sys_employee
SELECT 'sys_employee' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_employee_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_employee_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_employee
UNION ALL

-- sys_menu
SELECT 'sys_menu' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_menu_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_menu_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_menu
UNION ALL

-- sys_role
SELECT 'sys_role' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_role_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_role_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_role
UNION ALL

-- sys_dict_type
SELECT 'sys_dict_type' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_dict_type_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_dict_type_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_dict_type
UNION ALL

-- sys_dict_data
SELECT 'sys_dict_data' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_dict_data_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_dict_data_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_dict_data
UNION ALL

-- sys_role_menu
SELECT 'sys_role_menu' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_role_menu_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_role_menu_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_role_menu
UNION ALL

-- sys_user_role
SELECT 'sys_user_role' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_user_role_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_user_role_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_user_role
UNION ALL

-- sys_schedule_job
SELECT 'sys_schedule_job' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_schedule_job_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_schedule_job_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_schedule_job
UNION ALL

-- sys_schedule_log
SELECT 'sys_schedule_log' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM sys_schedule_log_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM sys_schedule_log_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM sys_schedule_log
UNION ALL

-- operation_log
SELECT 'operation_log' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM operation_log_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM operation_log_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM operation_log
UNION ALL

-- duty_shift_config
SELECT 'duty_shift_config' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_shift_config_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_shift_config_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_shift_config
UNION ALL

-- duty_schedule_mode
SELECT 'duty_schedule_mode' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_schedule_mode_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_schedule_mode_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_schedule_mode
UNION ALL

-- duty_schedule_shift
SELECT 'duty_schedule_shift' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_schedule_shift_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_schedule_shift_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_schedule_shift
UNION ALL

-- duty_schedule_rule
SELECT 'duty_schedule_rule' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_schedule_rule_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_schedule_rule_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_schedule_rule
UNION ALL

-- duty_schedule
SELECT 'duty_schedule' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_schedule_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_schedule_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_schedule
UNION ALL

-- duty_schedule_employee
SELECT 'duty_schedule_employee' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_schedule_employee_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_schedule_employee_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_schedule_employee
UNION ALL

-- duty_assignment
SELECT 'duty_assignment' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_assignment_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_assignment_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_assignment
UNION ALL

-- duty_record
SELECT 'duty_record' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_record_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_record_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_record
UNION ALL

-- duty_holiday
SELECT 'duty_holiday' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_holiday_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_holiday_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_holiday
UNION ALL

-- duty_shift_mutex
SELECT 'duty_shift_mutex' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM duty_shift_mutex_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM duty_shift_mutex_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM duty_shift_mutex
UNION ALL

-- employee_available_time
SELECT 'employee_available_time' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM employee_available_time_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM employee_available_time_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM employee_available_time
UNION ALL

-- leave_request
SELECT 'leave_request' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM leave_request_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM leave_request_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM leave_request
UNION ALL

-- leave_substitute
SELECT 'leave_substitute' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM leave_substitute_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM leave_substitute_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM leave_substitute
UNION ALL

-- approval_record
SELECT 'approval_record' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM approval_record_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM approval_record_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM approval_record
UNION ALL

-- swap_request
SELECT 'swap_request' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM swap_request_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM swap_request_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM swap_request
UNION ALL

-- pm_project
SELECT 'pm_project' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM pm_project_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM pm_project_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM pm_project
UNION ALL

-- pm_task
SELECT 'pm_task' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM pm_task_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM pm_task_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM pm_task
UNION ALL

-- schedule_version
SELECT 'schedule_version' as table_name, 
       COALESCE(MAX(id), 0) as max_id,
       (SELECT last_value FROM schedule_version_id_seq) as current_seq,
       CASE WHEN (SELECT last_value FROM schedule_version_id_seq) > COALESCE(MAX(id), 0) THEN 'OK' ELSE '需要修复' END as status
FROM schedule_version;
