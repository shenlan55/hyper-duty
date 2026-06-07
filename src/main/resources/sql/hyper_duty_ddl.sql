-- ===============================================================
-- Hyper Duty 系统模块表结构（PostgreSQL 语法）
-- ===============================================================

CREATE TABLE public.sys_dept (
    id bigint NOT NULL,
    dept_name character varying(50) NOT NULL,
    parent_id bigint DEFAULT '0'::bigint,
    dept_code character varying(20) NOT NULL,
    sort integer DEFAULT 0,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.sys_dept IS '部门表';

COMMENT ON COLUMN public.sys_dept.id IS '部门ID';

COMMENT ON COLUMN public.sys_dept.dept_name IS '部门名称';

COMMENT ON COLUMN public.sys_dept.parent_id IS '上级部门ID';

COMMENT ON COLUMN public.sys_dept.dept_code IS '部门编码';

COMMENT ON COLUMN public.sys_dept.sort IS '排序';

COMMENT ON COLUMN public.sys_dept.status IS '状态：0禁用，1启用';

COMMENT ON COLUMN public.sys_dept.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_dept.update_time IS '更新时间';

CREATE TABLE public.sys_dict_data (
    id bigint NOT NULL,
    dict_type_id bigint NOT NULL,
    dict_label character varying(100) NOT NULL,
    dict_value character varying(100) NOT NULL,
    dict_sort integer DEFAULT 0,
    css_class character varying(100) DEFAULT NULL::character varying,
    list_class character varying(100) DEFAULT NULL::character varying,
    is_default smallint DEFAULT '0'::smallint,
    status smallint DEFAULT '1'::smallint,
    remark character varying(500) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.sys_dict_data IS '字典数据表';

COMMENT ON COLUMN public.sys_dict_data.id IS '字典数据ID';

COMMENT ON COLUMN public.sys_dict_data.dict_type_id IS '字典类型ID';

COMMENT ON COLUMN public.sys_dict_data.dict_label IS '字典标签';

COMMENT ON COLUMN public.sys_dict_data.dict_value IS '字典键值';

COMMENT ON COLUMN public.sys_dict_data.dict_sort IS '字典排序';

COMMENT ON COLUMN public.sys_dict_data.css_class IS '样式属性（其他样式扩展）';

COMMENT ON COLUMN public.sys_dict_data.list_class IS '表格回显样式';

COMMENT ON COLUMN public.sys_dict_data.is_default IS '是否默认：0否，1是';

COMMENT ON COLUMN public.sys_dict_data.status IS '状态：0禁用，1启用';

COMMENT ON COLUMN public.sys_dict_data.remark IS '备注';

COMMENT ON COLUMN public.sys_dict_data.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_dict_data.update_time IS '更新时间';

CREATE TABLE public.sys_dict_type (
    id bigint NOT NULL,
    dict_name character varying(100) NOT NULL,
    dict_code character varying(100) NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.sys_dict_type IS '字典类型表';

COMMENT ON COLUMN public.sys_dict_type.id IS '字典类型ID';

COMMENT ON COLUMN public.sys_dict_type.dict_name IS '字典名称';

COMMENT ON COLUMN public.sys_dict_type.dict_code IS '字典编码';

COMMENT ON COLUMN public.sys_dict_type.description IS '字典描述';

COMMENT ON COLUMN public.sys_dict_type.status IS '状态：0禁用，1启用';

COMMENT ON COLUMN public.sys_dict_type.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_dict_type.update_time IS '更新时间';

CREATE TABLE public.sys_employee (
    id bigint NOT NULL,
    employee_name character varying(50) NOT NULL,
    dept_id bigint NOT NULL,
    employee_code character varying(20) DEFAULT NULL::character varying,
    phone character varying(11) DEFAULT NULL::character varying,
    email character varying(50) DEFAULT NULL::character varying,
    gender smallint DEFAULT '0'::smallint,
    dict_type_id bigint,
    dict_data_id bigint,
    status smallint DEFAULT '1'::smallint,
    username character varying(50) DEFAULT NULL::character varying,
    password character varying(100) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    sort integer DEFAULT 0
);

COMMENT ON TABLE public.sys_employee IS '人员表';

COMMENT ON COLUMN public.sys_employee.id IS '人员ID';

COMMENT ON COLUMN public.sys_employee.employee_name IS '人员姓名';

COMMENT ON COLUMN public.sys_employee.dept_id IS '所属部门ID';

COMMENT ON COLUMN public.sys_employee.employee_code IS '人员编码';

COMMENT ON COLUMN public.sys_employee.phone IS '手机号码';

COMMENT ON COLUMN public.sys_employee.email IS '邮箱';

COMMENT ON COLUMN public.sys_employee.gender IS '性别：0未知，1男，2女';

COMMENT ON COLUMN public.sys_employee.dict_type_id IS '字典类型ID';

COMMENT ON COLUMN public.sys_employee.dict_data_id IS '字典数据ID';

COMMENT ON COLUMN public.sys_employee.status IS '状态：0禁用，1启用';

COMMENT ON COLUMN public.sys_employee.username IS '用户名';

COMMENT ON COLUMN public.sys_employee.password IS '密码';

COMMENT ON COLUMN public.sys_employee.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_employee.update_time IS '更新时间';

CREATE TABLE public.sys_menu (
    id bigint NOT NULL,
    menu_name character varying(50) NOT NULL,
    parent_id bigint DEFAULT '0'::bigint,
    path character varying(200) DEFAULT NULL::character varying,
    component character varying(200) DEFAULT NULL::character varying,
    perm character varying(100) DEFAULT NULL::character varying,
    type smallint NOT NULL,
    icon character varying(50) DEFAULT NULL::character varying,
    sort integer DEFAULT 0,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.sys_menu IS '菜单表';

COMMENT ON COLUMN public.sys_menu.id IS '菜单ID';

COMMENT ON COLUMN public.sys_menu.menu_name IS '菜单名称';

COMMENT ON COLUMN public.sys_menu.parent_id IS '父菜单ID';

COMMENT ON COLUMN public.sys_menu.path IS '路由路径';

COMMENT ON COLUMN public.sys_menu.component IS '组件路径';

COMMENT ON COLUMN public.sys_menu.perm IS '权限标识';

COMMENT ON COLUMN public.sys_menu.type IS '菜单类型：1目录，2菜单，3按钮';

COMMENT ON COLUMN public.sys_menu.icon IS '菜单图标';

COMMENT ON COLUMN public.sys_menu.sort IS '排序';

COMMENT ON COLUMN public.sys_menu.status IS '状态：0禁用，1启用';

COMMENT ON COLUMN public.sys_menu.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_menu.update_time IS '更新时间';

CREATE TABLE public.sys_role (
    id bigint NOT NULL,
    role_name character varying(50) NOT NULL,
    role_code character varying(50) NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    sort integer DEFAULT 0
);

COMMENT ON TABLE public.sys_role IS '角色表';

COMMENT ON COLUMN public.sys_role.id IS '角色ID';

COMMENT ON COLUMN public.sys_role.role_name IS '角色名称';

COMMENT ON COLUMN public.sys_role.role_code IS '角色编码';

COMMENT ON COLUMN public.sys_role.description IS '角色描述';

COMMENT ON COLUMN public.sys_role.status IS '状态：0禁用，1启用';

COMMENT ON COLUMN public.sys_role.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_role.update_time IS '更新时间';

CREATE TABLE public.sys_role_menu (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    menu_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.sys_role_menu IS '角色菜单关联表';

COMMENT ON COLUMN public.sys_role_menu.id IS 'ID';

COMMENT ON COLUMN public.sys_role_menu.role_id IS '角色ID';

COMMENT ON COLUMN public.sys_role_menu.menu_id IS '菜单ID';

COMMENT ON COLUMN public.sys_role_menu.create_time IS '创建时间';

CREATE TABLE public.sys_schedule_job (
    id bigint NOT NULL,
    job_name character varying(100) NOT NULL,
    job_group character varying(50) NOT NULL,
    job_code character varying(100) NOT NULL,
    cron_expression character varying(100) NOT NULL,
    bean_name character varying(200) DEFAULT NULL::character varying,
    method_name character varying(100) DEFAULT NULL::character varying,
    params text,
    status smallint DEFAULT '1'::smallint,
    concurrent smallint DEFAULT '0'::smallint,
    description character varying(500) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.sys_schedule_job IS '定时任务表';

COMMENT ON COLUMN public.sys_schedule_job.id IS '任务ID';

COMMENT ON COLUMN public.sys_schedule_job.job_name IS '任务名称';

COMMENT ON COLUMN public.sys_schedule_job.job_group IS '任务分组';

COMMENT ON COLUMN public.sys_schedule_job.job_code IS '任务编码';

COMMENT ON COLUMN public.sys_schedule_job.cron_expression IS 'Cron表达式';

COMMENT ON COLUMN public.sys_schedule_job.bean_name IS 'Bean名称';

COMMENT ON COLUMN public.sys_schedule_job.method_name IS '方法名称';

COMMENT ON COLUMN public.sys_schedule_job.params IS '参数';

COMMENT ON COLUMN public.sys_schedule_job.status IS '状态:0-暂停,1-启用';

COMMENT ON COLUMN public.sys_schedule_job.concurrent IS '是否允许并发:0-不允许,1-允许';

COMMENT ON COLUMN public.sys_schedule_job.description IS '任务描述';

COMMENT ON COLUMN public.sys_schedule_job.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_schedule_job.update_time IS '更新时间';

CREATE TABLE public.sys_schedule_log (
    id bigint NOT NULL,
    job_name character varying(100) NOT NULL,
    job_group character varying(50) NOT NULL,
    status smallint NOT NULL,
    error_msg text,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    job_id bigint NOT NULL,
    job_code character varying(100) NOT NULL,
    params text,
    execute_time bigint
);

COMMENT ON TABLE public.sys_schedule_log IS '定时任务日志表';

COMMENT ON COLUMN public.sys_schedule_log.id IS '日志ID';

COMMENT ON COLUMN public.sys_schedule_log.job_name IS '任务名称';

COMMENT ON COLUMN public.sys_schedule_log.job_group IS '任务分组';

COMMENT ON COLUMN public.sys_schedule_log.status IS '执行状态:0-失败,1-成功';

COMMENT ON COLUMN public.sys_schedule_log.error_msg IS '错误信息';

COMMENT ON COLUMN public.sys_schedule_log.start_time IS '开始时间';

COMMENT ON COLUMN public.sys_schedule_log.end_time IS '结束时间';

COMMENT ON COLUMN public.sys_schedule_log.create_time IS '创建时间';

COMMENT ON COLUMN public.sys_schedule_log.job_id IS '任务ID';

COMMENT ON COLUMN public.sys_schedule_log.job_code IS '任务编码';

COMMENT ON COLUMN public.sys_schedule_log.params IS '参数';

COMMENT ON COLUMN public.sys_schedule_log.execute_time IS '执行时间(毫秒)';

CREATE TABLE public.sys_user_role (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    role_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.sys_user_role IS '用户角色关联表';

COMMENT ON COLUMN public.sys_user_role.id IS 'ID';

COMMENT ON COLUMN public.sys_user_role.employee_id IS '人员ID';

COMMENT ON COLUMN public.sys_user_role.role_id IS '角色ID';

COMMENT ON COLUMN public.sys_user_role.create_time IS '创建时间';

ALTER TABLE ONLY public.approval_record
    ADD CONSTRAINT approval_record_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT duty_assignment_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_holiday
    ADD CONSTRAINT duty_holiday_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT duty_record_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_schedule_employee
    ADD CONSTRAINT duty_schedule_employee_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_schedule_mode
    ADD CONSTRAINT duty_schedule_mode_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_schedule
    ADD CONSTRAINT duty_schedule_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_schedule_rule
    ADD CONSTRAINT duty_schedule_rule_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_schedule_shift
    ADD CONSTRAINT duty_schedule_shift_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_shift_config
    ADD CONSTRAINT duty_shift_config_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.duty_shift_mutex
    ADD CONSTRAINT duty_shift_mutex_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.employee_available_time
    ADD CONSTRAINT employee_available_time_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.operation_log
    ADD CONSTRAINT operation_log_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_project_id_employee_id_key UNIQUE (project_id, employee_id);

ALTER TABLE ONLY public.pm_project
    ADD CONSTRAINT pm_project_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pm_task_comment_attachment
    ADD CONSTRAINT pm_task_comment_attachment_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pm_task_comment
    ADD CONSTRAINT pm_task_comment_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pm_task
    ADD CONSTRAINT pm_task_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_task_id_employee_id_key UNIQUE (task_id, employee_id);

ALTER TABLE ONLY public.schedule_version
    ADD CONSTRAINT schedule_version_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_dept
    ADD CONSTRAINT sys_dept_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_dict_data
    ADD CONSTRAINT sys_dict_data_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_dict_type
    ADD CONSTRAINT sys_dict_type_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_employee
    ADD CONSTRAINT sys_employee_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_menu
    ADD CONSTRAINT sys_menu_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_role_menu
    ADD CONSTRAINT sys_role_menu_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT sys_role_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_schedule_job
    ADD CONSTRAINT sys_schedule_job_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_schedule_log
    ADD CONSTRAINT sys_schedule_log_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_pkey PRIMARY KEY (id);

ALTER TABLE ONLY public.approval_record
    ADD CONSTRAINT approval_record_ibfk_1 FOREIGN KEY (request_id) REFERENCES public.leave_request(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.approval_record
    ADD CONSTRAINT approval_record_ibfk_2 FOREIGN KEY (approver_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT duty_assignment_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT duty_assignment_ibfk_2 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT duty_record_ibfk_1 FOREIGN KEY (assignment_id) REFERENCES public.duty_assignment(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT duty_record_ibfk_2 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_schedule_employee
    ADD CONSTRAINT duty_schedule_employee_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_schedule_employee
    ADD CONSTRAINT duty_schedule_employee_ibfk_2 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_schedule_shift
    ADD CONSTRAINT duty_schedule_shift_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_schedule_shift
    ADD CONSTRAINT duty_schedule_shift_ibfk_2 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_shift_mutex
    ADD CONSTRAINT duty_shift_mutex_ibfk_1 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.duty_shift_mutex
    ADD CONSTRAINT duty_shift_mutex_ibfk_2 FOREIGN KEY (mutex_shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.employee_available_time
    ADD CONSTRAINT employee_available_time_ibfk_1 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT fk_schedule FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.duty_schedule
    ADD CONSTRAINT fk_schedule_mode FOREIGN KEY (schedule_mode_id) REFERENCES public.duty_schedule_mode(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT fk_shift_config FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT fk_substitute_employee FOREIGN KEY (substitute_employee_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT fk_version FOREIGN KEY (version_id) REFERENCES public.schedule_version(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_1 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_2 FOREIGN KEY (current_approver_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_3 FOREIGN KEY (substitute_employee_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_4 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_1 FOREIGN KEY (leave_request_id) REFERENCES public.leave_request(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_2 FOREIGN KEY (original_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_3 FOREIGN KEY (substitute_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_4 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.operation_log
    ADD CONSTRAINT operation_log_ibfk_1 FOREIGN KEY (operator_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.pm_task_comment_attachment
    ADD CONSTRAINT pm_task_comment_attachment_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES public.pm_task_comment(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.pm_task_comment
    ADD CONSTRAINT pm_task_comment_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.pm_task_comment
    ADD CONSTRAINT pm_task_comment_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.pm_task(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.pm_task
    ADD CONSTRAINT pm_task_ibfk_1 FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.pm_task
    ADD CONSTRAINT pm_task_ibfk_2 FOREIGN KEY (assignee_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.pm_task(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.schedule_version
    ADD CONSTRAINT schedule_version_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_1 FOREIGN KEY (original_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_2 FOREIGN KEY (target_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_3 FOREIGN KEY (original_assignment_id) REFERENCES public.duty_assignment(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_4 FOREIGN KEY (target_assignment_id) REFERENCES public.duty_assignment(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_5 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.sys_dict_data
    ADD CONSTRAINT sys_dict_data_ibfk_1 FOREIGN KEY (dict_type_id) REFERENCES public.sys_dict_type(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.sys_employee
    ADD CONSTRAINT sys_employee_ibfk_1 FOREIGN KEY (dept_id) REFERENCES public.sys_dept(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.sys_role_menu
    ADD CONSTRAINT sys_role_menu_ibfk_1 FOREIGN KEY (role_id) REFERENCES public.sys_role(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.sys_role_menu
    ADD CONSTRAINT sys_role_menu_ibfk_2 FOREIGN KEY (menu_id) REFERENCES public.sys_menu(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_ibfk_1 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_ibfk_2 FOREIGN KEY (role_id) REFERENCES public.sys_role(id) ON DELETE CASCADE;

-- 任务表结构调整
-- 添加attachments字段
ALTER TABLE pm_task ADD COLUMN attachments text;

-- 增加description字段长度以支持富文本内容
ALTER TABLE pm_task ALTER COLUMN description TYPE text;

-- 项目表结构调整
-- 删除旧的单个 deputy_owner_id 字段
ALTER TABLE pm_project DROP COLUMN IF EXISTS deputy_owner_id;

-- 创建代理负责人关联表

CREATE TABLE public.sys_mail_config (
    id bigint NOT NULL,
    smtp_host character varying(255) NOT NULL,
    smtp_port integer NOT NULL,
    enable_ssl smallint DEFAULT 0,
    enable_tls smallint DEFAULT 0,
    from_email character varying(255) NOT NULL,
    from_name character varying(255),
    auth_password character varying(255) NOT NULL,
    login_code_template text,
    password_reset_template text,
    remote_login_template text,
    enable_email_login smallint DEFAULT 0,
    code_expire_minutes integer DEFAULT 5,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    remark text
);

COMMENT ON TABLE public.sys_mail_config IS '邮件服务配置表';
COMMENT ON COLUMN public.sys_mail_config.id IS '主键ID';
COMMENT ON COLUMN public.sys_mail_config.smtp_host IS 'SMTP服务器地址';
COMMENT ON COLUMN public.sys_mail_config.smtp_port IS 'SMTP端口';
COMMENT ON COLUMN public.sys_mail_config.enable_ssl IS '是否启用SSL：0否，1是';
COMMENT ON COLUMN public.sys_mail_config.enable_tls IS '是否启用TLS：0否，1是';
COMMENT ON COLUMN public.sys_mail_config.from_email IS '发件人邮箱';
COMMENT ON COLUMN public.sys_mail_config.from_name IS '发件人名称';
COMMENT ON COLUMN public.sys_mail_config.auth_password IS '授权码/密码';
COMMENT ON COLUMN public.sys_mail_config.login_code_template IS '登录验证码模板';
COMMENT ON COLUMN public.sys_mail_config.password_reset_template IS '密码找回模板';
COMMENT ON COLUMN public.sys_mail_config.remote_login_template IS '异地登录提醒模板';
COMMENT ON COLUMN public.sys_mail_config.enable_email_login IS '是否启用邮件验证码登录：0否，1是';
COMMENT ON COLUMN public.sys_mail_config.code_expire_minutes IS '验证码有效期（分钟）';
COMMENT ON COLUMN public.sys_mail_config.create_time IS '创建时间';
COMMENT ON COLUMN public.sys_mail_config.update_time IS '更新时间';
COMMENT ON COLUMN public.sys_mail_config.remark IS '备注';

-- AI报告功能扩展
-- 给任务表添加是否重点字段
ALTER TABLE public.pm_task ADD COLUMN IF NOT EXISTS is_focus smallint DEFAULT 0;
COMMENT ON COLUMN public.pm_task.is_focus IS '是否重点任务：0否，1是';

-- AI报告配置表

