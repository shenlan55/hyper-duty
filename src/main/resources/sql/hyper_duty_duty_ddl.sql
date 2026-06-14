-- ===============================================================
-- Hyper Duty 值班管理模块表结构（PostgreSQL 语法）
-- ===============================================================

CREATE TABLE public.approval_record (
    id bigint NOT NULL,
    request_id bigint NOT NULL,
    request_type character varying(20) NOT NULL,
    approver_id bigint NOT NULL,
    approval_level integer NOT NULL,
    approval_status character varying(20) NOT NULL,
    approval_opinion character varying(500) DEFAULT NULL::character varying,
    approval_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.approval_record IS '审批记录表';

COMMENT ON COLUMN public.approval_record.id IS '审批记录ID';

COMMENT ON COLUMN public.approval_record.request_id IS '申请ID';

COMMENT ON COLUMN public.approval_record.request_type IS '申请类型:leave-请假,swap-调班';

COMMENT ON COLUMN public.approval_record.approver_id IS '审批人ID';

COMMENT ON COLUMN public.approval_record.approval_level IS '审批层级';

COMMENT ON COLUMN public.approval_record.approval_status IS '审批状态:approved-通过,rejected-拒绝';

COMMENT ON COLUMN public.approval_record.approval_opinion IS '审批意见';

COMMENT ON COLUMN public.approval_record.approval_time IS '审批时间';

COMMENT ON COLUMN public.approval_record.create_time IS '创建时间';

CREATE TABLE public.duty_assignment (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    duty_date date NOT NULL,
    duty_shift integer DEFAULT 1,
    employee_id bigint NOT NULL,
    status smallint DEFAULT '1'::smallint,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    shift_config_id bigint,
    version_id bigint,
    is_overtime smallint DEFAULT '0'::smallint
);

COMMENT ON TABLE public.duty_assignment IS '值班安排表';

COMMENT ON COLUMN public.duty_assignment.id IS '值班安排ID';

COMMENT ON COLUMN public.duty_assignment.schedule_id IS '关联值班表ID';

COMMENT ON COLUMN public.duty_assignment.duty_date IS '值班日期';

COMMENT ON COLUMN public.duty_assignment.duty_shift IS '值班班次：1白班，2晚班，3夜班';

COMMENT ON COLUMN public.duty_assignment.employee_id IS '值班人员ID';

COMMENT ON COLUMN public.duty_assignment.status IS '状态：0取消，1正常';

COMMENT ON COLUMN public.duty_assignment.remark IS '备注';

COMMENT ON COLUMN public.duty_assignment.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_assignment.update_time IS '更新时间';

COMMENT ON COLUMN public.duty_assignment.shift_config_id IS '班次配置ID';

COMMENT ON COLUMN public.duty_assignment.version_id IS '排班版本ID';

COMMENT ON COLUMN public.duty_assignment.is_overtime IS '是否加班:0-否,1-是';

CREATE TABLE public.duty_holiday (
    id bigint NOT NULL,
    holiday_name character varying(50) NOT NULL,
    holiday_date date NOT NULL,
    is_workday smallint DEFAULT '0'::smallint,
    holiday_type smallint DEFAULT '1'::smallint,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.duty_holiday IS '节假日表';

COMMENT ON COLUMN public.duty_holiday.id IS '节假日ID';

COMMENT ON COLUMN public.duty_holiday.holiday_name IS '节假日名称';

COMMENT ON COLUMN public.duty_holiday.holiday_date IS '节假日日期';

COMMENT ON COLUMN public.duty_holiday.is_workday IS '是否调休上班:0-否,1-是';

COMMENT ON COLUMN public.duty_holiday.holiday_type IS '节假日类型:1-法定假日,2-调休,3-公司假日';

COMMENT ON COLUMN public.duty_holiday.remark IS '备注';

COMMENT ON COLUMN public.duty_holiday.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_holiday.update_time IS '更新时间';

CREATE TABLE public.duty_record (
    id bigint NOT NULL,
    assignment_id bigint,
    schedule_id bigint,
    duty_date date,
    duty_shift integer,
    employee_id bigint NOT NULL,
    check_in_time timestamp without time zone,
    check_out_time timestamp without time zone,
    duty_status smallint DEFAULT '1'::smallint,
    check_in_remark character varying(200) DEFAULT NULL::character varying,
    check_out_remark character varying(200) DEFAULT NULL::character varying,
    overtime_hours numeric(5,2) DEFAULT 0.00,
    approval_status character varying(20) DEFAULT 'pending'::character varying,
    manager_remark character varying(200) DEFAULT NULL::character varying,
    remark text,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    substitute_employee_id bigint,
    substitute_type smallint DEFAULT '1'::smallint
);

COMMENT ON TABLE public.duty_record IS '值班记录表';

COMMENT ON COLUMN public.duty_record.id IS '值班记录ID';

COMMENT ON COLUMN public.duty_record.assignment_id IS '关联值班安排ID';

COMMENT ON COLUMN public.duty_record.schedule_id IS '值班表ID';

COMMENT ON COLUMN public.duty_record.duty_date IS '值班日期';

COMMENT ON COLUMN public.duty_record.duty_shift IS '班次';

COMMENT ON COLUMN public.duty_record.employee_id IS '值班人员ID';

COMMENT ON COLUMN public.duty_record.check_in_time IS '签到时间';

COMMENT ON COLUMN public.duty_record.check_out_time IS '签退时间';

COMMENT ON COLUMN public.duty_record.duty_status IS '值班状态：1正常，2迟到，3早退，4缺勤，5请假';

COMMENT ON COLUMN public.duty_record.check_in_remark IS '签到备注';

COMMENT ON COLUMN public.duty_record.check_out_remark IS '签退备注';

COMMENT ON COLUMN public.duty_record.overtime_hours IS '加班时长';

COMMENT ON COLUMN public.duty_record.approval_status IS '审批状态：pending待审批，approved已审批，rejected已拒绝';

COMMENT ON COLUMN public.duty_record.manager_remark IS '经理备注';

COMMENT ON COLUMN public.duty_record.remark IS '加班原因';

COMMENT ON COLUMN public.duty_record.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_record.update_time IS '更新时间';

COMMENT ON COLUMN public.duty_record.substitute_employee_id IS '替补人员ID';

COMMENT ON COLUMN public.duty_record.substitute_type IS '替补类型:1-自动匹配,2-手动选择';

CREATE TABLE public.duty_schedule (
    id bigint NOT NULL,
    schedule_name character varying(100) NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status smallint DEFAULT '1'::smallint,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    schedule_mode_id bigint,
    sort_order integer DEFAULT 0
);

COMMENT ON TABLE public.duty_schedule IS '值班表';

COMMENT ON COLUMN public.duty_schedule.id IS '值班表ID';

COMMENT ON COLUMN public.duty_schedule.schedule_name IS '值班表名称';

COMMENT ON COLUMN public.duty_schedule.description IS '描述';

COMMENT ON COLUMN public.duty_schedule.start_date IS '开始日期';

COMMENT ON COLUMN public.duty_schedule.end_date IS '结束日期';

COMMENT ON COLUMN public.duty_schedule.status IS '状态：0禁用，1启用';

COMMENT ON COLUMN public.duty_schedule.create_by IS '创建人ID';

COMMENT ON COLUMN public.duty_schedule.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_schedule.update_time IS '更新时间';

COMMENT ON COLUMN public.duty_schedule.schedule_mode_id IS '排班方式ID';

COMMENT ON COLUMN public.duty_schedule.sort_order IS '排序';

CREATE TABLE public.duty_schedule_employee (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_leader smallint DEFAULT '0'::smallint
);

COMMENT ON TABLE public.duty_schedule_employee IS '值班表人员关联表';

COMMENT ON COLUMN public.duty_schedule_employee.id IS 'ID';

COMMENT ON COLUMN public.duty_schedule_employee.schedule_id IS '值班表ID';

COMMENT ON COLUMN public.duty_schedule_employee.employee_id IS '员工ID';

COMMENT ON COLUMN public.duty_schedule_employee.sort_order IS '排序';

COMMENT ON COLUMN public.duty_schedule_employee.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_schedule_employee.is_leader IS '是否值班长：0否，1是';

CREATE TABLE public.duty_schedule_mode (
    id bigint NOT NULL,
    mode_name character varying(100) NOT NULL,
    mode_code character varying(50) NOT NULL,
    mode_type smallint NOT NULL,
    algorithm_class character varying(200) DEFAULT NULL::character varying,
    config_json json,
    description character varying(500) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    sort integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.duty_schedule_mode IS '排班方式表';

COMMENT ON COLUMN public.duty_schedule_mode.id IS '排班方式ID';

COMMENT ON COLUMN public.duty_schedule_mode.mode_name IS '排班方式名称';

COMMENT ON COLUMN public.duty_schedule_mode.mode_code IS '排班方式编码';

COMMENT ON COLUMN public.duty_schedule_mode.mode_type IS '排班方式类型:1-轮班制,2-综合制,3-弹性制,4-自定义';

COMMENT ON COLUMN public.duty_schedule_mode.algorithm_class IS '算法实现类全路径';

COMMENT ON COLUMN public.duty_schedule_mode.config_json IS '配置参数JSON';

COMMENT ON COLUMN public.duty_schedule_mode.description IS '排班方式描述';

COMMENT ON COLUMN public.duty_schedule_mode.status IS '状态:0-禁用,1-启用';

COMMENT ON COLUMN public.duty_schedule_mode.sort IS '排序';

COMMENT ON COLUMN public.duty_schedule_mode.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_schedule_mode.update_time IS '更新时间';

CREATE TABLE public.duty_schedule_rule (
    id bigint NOT NULL,
    rule_name character varying(100) NOT NULL,
    rule_code character varying(50) NOT NULL,
    schedule_cycle smallint DEFAULT '1'::smallint NOT NULL,
    max_daily_shifts integer DEFAULT 3,
    max_weekly_hours numeric(6,2) DEFAULT 48.00,
    max_monthly_hours numeric(6,2) DEFAULT 200.00,
    min_rest_days integer DEFAULT 4,
    substitute_priority_rule character varying(200) DEFAULT NULL::character varying,
    conflict_detection_rule character varying(200) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.duty_schedule_rule IS '排班规则配置表';

COMMENT ON COLUMN public.duty_schedule_rule.id IS '规则ID';

COMMENT ON COLUMN public.duty_schedule_rule.rule_name IS '规则名称';

COMMENT ON COLUMN public.duty_schedule_rule.rule_code IS '规则编码';

COMMENT ON COLUMN public.duty_schedule_rule.schedule_cycle IS '排班周期:1-按周,2-按月,3-按季度';

COMMENT ON COLUMN public.duty_schedule_rule.max_daily_shifts IS '每日最大班次数';

COMMENT ON COLUMN public.duty_schedule_rule.max_weekly_hours IS '每周最大工作时长(小时)';

COMMENT ON COLUMN public.duty_schedule_rule.max_monthly_hours IS '每月最大工作时长(小时)';

COMMENT ON COLUMN public.duty_schedule_rule.min_rest_days IS '每月最少休息天数';

COMMENT ON COLUMN public.duty_schedule_rule.substitute_priority_rule IS '替补优先级规则';

COMMENT ON COLUMN public.duty_schedule_rule.conflict_detection_rule IS '冲突检测规则';

COMMENT ON COLUMN public.duty_schedule_rule.status IS '状态:0-禁用,1-启用';

COMMENT ON COLUMN public.duty_schedule_rule.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_schedule_rule.update_time IS '更新时间';

CREATE TABLE public.duty_schedule_shift (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    shift_config_id bigint NOT NULL,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.duty_schedule_shift IS '值班表班次关联表';

COMMENT ON COLUMN public.duty_schedule_shift.id IS 'ID';

COMMENT ON COLUMN public.duty_schedule_shift.schedule_id IS '值班表ID';

COMMENT ON COLUMN public.duty_schedule_shift.shift_config_id IS '班次配置ID';

COMMENT ON COLUMN public.duty_schedule_shift.sort_order IS '排序';

COMMENT ON COLUMN public.duty_schedule_shift.create_time IS '创建时间';

CREATE TABLE public.duty_shift_config (
    id bigint NOT NULL,
    shift_name character varying(50) NOT NULL,
    shift_code character varying(20) NOT NULL,
    shift_type smallint DEFAULT '1'::smallint NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    duration_hours numeric(4,2) NOT NULL,
    break_hours numeric(4,2) DEFAULT 0.00,
    overtime_hours numeric(10,2),
    weekend_overtime_hours numeric(10,2),
    holiday_overtime_hours numeric(10,2),
    rest_day_rule character varying(100) DEFAULT NULL::character varying,
    is_overtime_shift smallint DEFAULT '0'::smallint,
    status smallint DEFAULT '1'::smallint,
    sort integer DEFAULT 0,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_cross_day smallint DEFAULT '0'::smallint
);

COMMENT ON TABLE public.duty_shift_config IS '班次配置表';

COMMENT ON COLUMN public.duty_shift_config.id IS '班次配置ID';

COMMENT ON COLUMN public.duty_shift_config.shift_name IS '班次名称';

COMMENT ON COLUMN public.duty_shift_config.shift_code IS '班次编码';

COMMENT ON COLUMN public.duty_shift_config.shift_type IS '班次类型:0-白班,1-早班,2-中班,3-晚班,4-全天,5-夜班';

COMMENT ON COLUMN public.duty_shift_config.start_time IS '上班时间';

COMMENT ON COLUMN public.duty_shift_config.end_time IS '下班时间';

COMMENT ON COLUMN public.duty_shift_config.duration_hours IS '时长(小时)';

COMMENT ON COLUMN public.duty_shift_config.break_hours IS '休息时长(小时)';

COMMENT ON COLUMN public.duty_shift_config.overtime_hours IS '加班时长(小时)';

COMMENT ON COLUMN public.duty_shift_config.weekend_overtime_hours IS '休息日加班时长(小时)';

COMMENT ON COLUMN public.duty_shift_config.holiday_overtime_hours IS '法定节假日加班时长(小时)';

COMMENT ON COLUMN public.duty_shift_config.rest_day_rule IS '休息日规则';

COMMENT ON COLUMN public.duty_shift_config.is_overtime_shift IS '是否加班班次:0-否,1-是';

COMMENT ON COLUMN public.duty_shift_config.status IS '状态:0-禁用,1-启用';

COMMENT ON COLUMN public.duty_shift_config.sort IS '排序';

COMMENT ON COLUMN public.duty_shift_config.remark IS '备注';

COMMENT ON COLUMN public.duty_shift_config.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_shift_config.update_time IS '更新时间';

COMMENT ON COLUMN public.duty_shift_config.is_cross_day IS '是否跨天:0-否,1-是';

-- 添加任务干系人字段
ALTER TABLE public.pm_task ADD COLUMN stakeholders TEXT;

-- 创建项目参与者关联表

ALTER TABLE public.duty_shift_config ADD COLUMN IF NOT EXISTS weekend_overtime_hours numeric(10,2);
ALTER TABLE public.duty_shift_config ADD COLUMN IF NOT EXISTS holiday_overtime_hours numeric(10,2);
COMMENT ON COLUMN public.duty_shift_config.weekend_overtime_hours IS '休息日加班时长(小时)';
COMMENT ON COLUMN public.duty_shift_config.holiday_overtime_hours IS '法定节假日加班时长(小时)';

CREATE TABLE public.duty_shift_mutex (
    id bigint NOT NULL,
    shift_config_id bigint NOT NULL,
    mutex_shift_config_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.duty_shift_mutex IS '班次互斥关系表';

COMMENT ON COLUMN public.duty_shift_mutex.id IS 'ID';

COMMENT ON COLUMN public.duty_shift_mutex.shift_config_id IS '班次配置ID';

COMMENT ON COLUMN public.duty_shift_mutex.mutex_shift_config_id IS '互斥班次配置ID';

COMMENT ON COLUMN public.duty_shift_mutex.create_time IS '创建时间';

COMMENT ON COLUMN public.duty_shift_mutex.update_time IS '更新时间';

CREATE TABLE public.employee_available_time (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    day_of_week smallint NOT NULL,
    start_time time without time zone,
    end_time time without time zone,
    is_available smallint DEFAULT '1'::smallint,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.employee_available_time IS '人员可排班时段表';

COMMENT ON COLUMN public.employee_available_time.id IS 'ID';

COMMENT ON COLUMN public.employee_available_time.employee_id IS '员工ID';

COMMENT ON COLUMN public.employee_available_time.day_of_week IS '星期:1-周一,2-周二,3-周三,4-周四,5-周五,6-周六,7-周日';

COMMENT ON COLUMN public.employee_available_time.start_time IS '开始时间';

COMMENT ON COLUMN public.employee_available_time.end_time IS '结束时间';

COMMENT ON COLUMN public.employee_available_time.is_available IS '是否可用:0-否,1-是';

COMMENT ON COLUMN public.employee_available_time.remark IS '备注';

COMMENT ON COLUMN public.employee_available_time.create_time IS '创建时间';

COMMENT ON COLUMN public.employee_available_time.update_time IS '更新时间';

CREATE TABLE public.leave_request (
    id bigint NOT NULL,
    request_no character varying(50) NOT NULL,
    employee_id bigint NOT NULL,
    leave_type smallint NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    start_time time without time zone,
    end_time time without time zone,
    total_hours numeric(6,2) DEFAULT NULL::numeric,
    reason character varying(500) NOT NULL,
    attachment_url character varying(500) DEFAULT NULL::character varying,
    approval_status character varying(20) DEFAULT 'pending'::character varying,
    current_approver_id bigint,
    approval_level integer DEFAULT 1,
    total_approval_levels integer DEFAULT 1,
    substitute_employee_id bigint,
    substitute_type smallint DEFAULT '1'::smallint,
    substitute_status character varying(20) DEFAULT 'pending'::character varying,
    reject_reason character varying(500) DEFAULT NULL::character varying,
    approval_opinion character varying(500) DEFAULT NULL::character varying,
    schedule_completed smallint DEFAULT '0'::smallint,
    schedule_completed_time timestamp without time zone,
    schedule_completed_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    schedule_id bigint,
    shift_config_id bigint,
    shift_config_ids character varying(500) DEFAULT NULL::character varying
);

COMMENT ON TABLE public.leave_request IS '请假申请表';

COMMENT ON COLUMN public.leave_request.id IS '请假申请ID';

COMMENT ON COLUMN public.leave_request.request_no IS '申请编号';

COMMENT ON COLUMN public.leave_request.employee_id IS '申请人ID';

COMMENT ON COLUMN public.leave_request.leave_type IS '请假类型:1-事假,2-病假,3-年假,4-调休,5-其他';

COMMENT ON COLUMN public.leave_request.start_date IS '开始日期';

COMMENT ON COLUMN public.leave_request.end_date IS '结束日期';

COMMENT ON COLUMN public.leave_request.start_time IS '开始时间';

COMMENT ON COLUMN public.leave_request.end_time IS '结束时间';

COMMENT ON COLUMN public.leave_request.total_hours IS '请假总时长(小时)';

COMMENT ON COLUMN public.leave_request.reason IS '请假原因';

COMMENT ON COLUMN public.leave_request.attachment_url IS '附件URL';

COMMENT ON COLUMN public.leave_request.approval_status IS '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消';

COMMENT ON COLUMN public.leave_request.current_approver_id IS '当前审批人ID';

COMMENT ON COLUMN public.leave_request.approval_level IS '当前审批层级';

COMMENT ON COLUMN public.leave_request.total_approval_levels IS '总审批层级';

COMMENT ON COLUMN public.leave_request.substitute_employee_id IS '替补人员ID';

COMMENT ON COLUMN public.leave_request.substitute_type IS '替补类型:1-自动匹配,2-手动选择';

COMMENT ON COLUMN public.leave_request.substitute_status IS '替补状态:pending-待确认,confirmed-已确认,rejected-已拒绝';

COMMENT ON COLUMN public.leave_request.reject_reason IS '拒绝原因';

COMMENT ON COLUMN public.leave_request.approval_opinion IS '审批意见';

COMMENT ON COLUMN public.leave_request.schedule_completed IS '排班是否完成:0-未完成,1-已完成';

COMMENT ON COLUMN public.leave_request.schedule_completed_time IS '排班完成时间';

COMMENT ON COLUMN public.leave_request.schedule_completed_by IS '排班完成人ID';

COMMENT ON COLUMN public.leave_request.create_time IS '创建时间';

COMMENT ON COLUMN public.leave_request.update_time IS '更新时间';

COMMENT ON COLUMN public.leave_request.schedule_id IS '值班表ID';

COMMENT ON COLUMN public.leave_request.shift_config_id IS '班次配置ID';

COMMENT ON COLUMN public.leave_request.shift_config_ids IS '班次配置ID列表，多个用逗号分隔';

CREATE TABLE public.leave_substitute (
    id bigint NOT NULL,
    leave_request_id bigint NOT NULL,
    original_employee_id bigint NOT NULL,
    substitute_employee_id bigint NOT NULL,
    duty_date date NOT NULL,
    shift_config_id bigint NOT NULL,
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.leave_substitute IS '请假顶岗信息表';

COMMENT ON COLUMN public.leave_substitute.id IS 'ID';

COMMENT ON COLUMN public.leave_substitute.leave_request_id IS '请假申请ID';

COMMENT ON COLUMN public.leave_substitute.original_employee_id IS '原值班人员ID';

COMMENT ON COLUMN public.leave_substitute.substitute_employee_id IS '顶岗人员ID';

COMMENT ON COLUMN public.leave_substitute.duty_date IS '值班日期';

COMMENT ON COLUMN public.leave_substitute.shift_config_id IS '班次配置ID';

COMMENT ON COLUMN public.leave_substitute.status IS '状态';

COMMENT ON COLUMN public.leave_substitute.create_time IS '创建时间';

COMMENT ON COLUMN public.leave_substitute.update_time IS '更新时间';

CREATE TABLE public.operation_log (
    id bigint NOT NULL,
    operator_id bigint,
    operator_name character varying(50) DEFAULT NULL::character varying,
    operation_type character varying(50) NOT NULL,
    operation_module character varying(50) NOT NULL,
    operation_desc character varying(500) DEFAULT NULL::character varying,
    request_method character varying(10) DEFAULT NULL::character varying,
    request_url character varying(500) DEFAULT NULL::character varying,
    request_params text,
    response_result text,
    ip_address character varying(50) DEFAULT NULL::character varying,
    user_agent character varying(500) DEFAULT NULL::character varying,
    execution_time integer,
    status smallint DEFAULT '1'::smallint,
    error_msg text,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.operation_log IS '操作日志表';

COMMENT ON COLUMN public.operation_log.id IS '日志ID';

COMMENT ON COLUMN public.operation_log.operator_id IS '操作人ID';

COMMENT ON COLUMN public.operation_log.operator_name IS '操作人姓名';

COMMENT ON COLUMN public.operation_log.operation_type IS '操作类型';

COMMENT ON COLUMN public.operation_log.operation_module IS '操作模块';

COMMENT ON COLUMN public.operation_log.operation_desc IS '操作描述';

COMMENT ON COLUMN public.operation_log.request_method IS '请求方法';

COMMENT ON COLUMN public.operation_log.request_url IS '请求URL';

COMMENT ON COLUMN public.operation_log.request_params IS '请求参数';

COMMENT ON COLUMN public.operation_log.response_result IS '响应结果';

COMMENT ON COLUMN public.operation_log.ip_address IS 'IP地址';

COMMENT ON COLUMN public.operation_log.user_agent IS '用户代理';

COMMENT ON COLUMN public.operation_log.execution_time IS '执行时间(毫秒)';

COMMENT ON COLUMN public.operation_log.status IS '状态:0-失败,1-成功';

COMMENT ON COLUMN public.operation_log.error_msg IS '错误信息';

COMMENT ON COLUMN public.operation_log.create_time IS '创建时间';

CREATE TABLE public.schedule_version (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    version_name character varying(100) NOT NULL,
    version_code character varying(50) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status character varying(20) DEFAULT 'draft'::character varying,
    is_current smallint DEFAULT '0'::smallint,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    publish_time timestamp without time zone
);

COMMENT ON TABLE public.schedule_version IS '排班版本表';

COMMENT ON COLUMN public.schedule_version.id IS '版本ID';

COMMENT ON COLUMN public.schedule_version.schedule_id IS '值班表ID';

COMMENT ON COLUMN public.schedule_version.version_name IS '版本名称';

COMMENT ON COLUMN public.schedule_version.version_code IS '版本编码';

COMMENT ON COLUMN public.schedule_version.start_date IS '开始日期';

COMMENT ON COLUMN public.schedule_version.end_date IS '结束日期';

COMMENT ON COLUMN public.schedule_version.status IS '状态:draft-草稿,published-已发布,archived-已归档';

COMMENT ON COLUMN public.schedule_version.is_current IS '是否当前版本:0-否,1-是';

COMMENT ON COLUMN public.schedule_version.create_by IS '创建人ID';

COMMENT ON COLUMN public.schedule_version.create_time IS '创建时间';

COMMENT ON COLUMN public.schedule_version.publish_time IS '发布时间';

CREATE TABLE public.swap_request (
    id bigint NOT NULL,
    request_no character varying(50) NOT NULL,
    original_employee_id bigint NOT NULL,
    target_employee_id bigint NOT NULL,
    original_assignment_id bigint NOT NULL,
    target_assignment_id bigint,
    schedule_id bigint NOT NULL,
    swap_date date NOT NULL,
    swap_shift integer NOT NULL,
    reason character varying(500) DEFAULT NULL::character varying,
    approval_status character varying(20) DEFAULT 'pending'::character varying,
    current_approver_id bigint,
    approval_level integer DEFAULT 1,
    total_approval_levels integer DEFAULT 1,
    original_confirm_status character varying(20) DEFAULT 'pending'::character varying,
    target_confirm_status character varying(20) DEFAULT 'pending'::character varying,
    reject_reason character varying(500) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    original_swap_date date,
    original_swap_shift integer,
    target_swap_date date,
    target_swap_shift integer
);

COMMENT ON TABLE public.swap_request IS '调班记录表';

COMMENT ON COLUMN public.swap_request.id IS '调班申请ID';

COMMENT ON COLUMN public.swap_request.request_no IS '申请编号';

COMMENT ON COLUMN public.swap_request.original_employee_id IS '原值班人员ID';

COMMENT ON COLUMN public.swap_request.target_employee_id IS '目标值班人员ID';

COMMENT ON COLUMN public.swap_request.original_assignment_id IS '原值班安排ID';

COMMENT ON COLUMN public.swap_request.target_assignment_id IS '目标值班安排ID';

COMMENT ON COLUMN public.swap_request.schedule_id IS '值班表ID';

COMMENT ON COLUMN public.swap_request.swap_date IS '调班日期';

COMMENT ON COLUMN public.swap_request.swap_shift IS '调班班次';

COMMENT ON COLUMN public.swap_request.reason IS '调班原因';

COMMENT ON COLUMN public.swap_request.approval_status IS '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消';

COMMENT ON COLUMN public.swap_request.current_approver_id IS '当前审批人ID';

COMMENT ON COLUMN public.swap_request.approval_level IS '当前审批层级';

COMMENT ON COLUMN public.swap_request.total_approval_levels IS '总审批层级';

COMMENT ON COLUMN public.swap_request.original_confirm_status IS '原人员确认状态';

COMMENT ON COLUMN public.swap_request.target_confirm_status IS '目标人员确认状态';

COMMENT ON COLUMN public.swap_request.reject_reason IS '拒绝原因';

COMMENT ON COLUMN public.swap_request.create_time IS '创建时间';

COMMENT ON COLUMN public.swap_request.update_time IS '更新时间';

COMMENT ON COLUMN public.swap_request.original_swap_date IS '原值班日期';

COMMENT ON COLUMN public.swap_request.original_swap_shift IS '原值班班次';

COMMENT ON COLUMN public.swap_request.target_swap_date IS '目标值班日期';

COMMENT ON COLUMN public.swap_request.target_swap_shift IS '目标值班班次';
