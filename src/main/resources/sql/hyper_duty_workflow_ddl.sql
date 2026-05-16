-- ===============================================================
-- Hyper Duty 工作流模块表结构（PostgreSQL 语法）
-- ===============================================================

-- 流程分类表
CREATE TABLE IF NOT EXISTS public.wf_category (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  name VARCHAR(100) NOT NULL,
                                                  code VARCHAR(100) NOT NULL,
                                                  sort INT DEFAULT 0,
                                                  status SMALLINT DEFAULT 1,
                                                  remark VARCHAR(500),
                                                  create_time TIMESTAMP,
                                                  update_time TIMESTAMP,
                                                  CONSTRAINT uk_category_code UNIQUE (code)
);
COMMENT ON TABLE public.wf_category IS '流程分类表';
COMMENT ON COLUMN public.wf_category.id IS '主键ID';
COMMENT ON COLUMN public.wf_category.name IS '分类名称';
COMMENT ON COLUMN public.wf_category.code IS '分类编码';
COMMENT ON COLUMN public.wf_category.sort IS '排序';
COMMENT ON COLUMN public.wf_category.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN public.wf_category.remark IS '备注';
COMMENT ON COLUMN public.wf_category.create_time IS '创建时间';
COMMENT ON COLUMN public.wf_category.update_time IS '更新时间';


-- 流程定义扩展表
CREATE TABLE IF NOT EXISTS public.wf_definition (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    process_definition_id VARCHAR(100) NOT NULL,
                                                    process_definition_key VARCHAR(100) NOT NULL,
                                                    process_name VARCHAR(200) NOT NULL,
                                                    category_id BIGINT,
                                                    form_id BIGINT,
                                                    version INT NOT NULL DEFAULT 1,
                                                    status SMALLINT DEFAULT 1,
                                                    remark VARCHAR(500),
                                                    create_time TIMESTAMP,
                                                    update_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_process_key ON public.wf_definition(process_definition_key);
CREATE INDEX IF NOT EXISTS idx_category_id ON public.wf_definition(category_id);
COMMENT ON TABLE public.wf_definition IS '流程定义扩展表';
COMMENT ON COLUMN public.wf_definition.id IS '主键ID';
COMMENT ON COLUMN public.wf_definition.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN public.wf_definition.process_definition_key IS '流程定义KEY';
COMMENT ON COLUMN public.wf_definition.process_name IS '流程名称';
COMMENT ON COLUMN public.wf_definition.category_id IS '分类ID';
COMMENT ON COLUMN public.wf_definition.form_id IS '表单ID';
COMMENT ON COLUMN public.wf_definition.version IS '版本号';
COMMENT ON COLUMN public.wf_definition.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN public.wf_definition.remark IS '备注';
COMMENT ON COLUMN public.wf_definition.create_time IS '创建时间';
COMMENT ON COLUMN public.wf_definition.update_time IS '更新时间';


-- 流程表单表
CREATE TABLE IF NOT EXISTS public.wf_form (
                                              id BIGSERIAL PRIMARY KEY,
                                              name VARCHAR(200) NOT NULL,
                                              code VARCHAR(100) NOT NULL,
                                              form_type VARCHAR(50) DEFAULT 'dynamic',
                                              status SMALLINT DEFAULT 1,
                                              form_config TEXT,
                                              form_content TEXT,
                                              version INT NOT NULL DEFAULT 1,
                                              remark VARCHAR(500),
                                              create_user_id BIGINT,
                                              create_time TIMESTAMP,
                                              update_time TIMESTAMP,
                                              CONSTRAINT uk_form_code UNIQUE (code)
);
CREATE INDEX IF NOT EXISTS idx_create_user ON public.wf_form(create_user_id);
COMMENT ON TABLE public.wf_form IS '流程表单表';
COMMENT ON COLUMN public.wf_form.id IS '主键ID';
COMMENT ON COLUMN public.wf_form.name IS '表单名称';
COMMENT ON COLUMN public.wf_form.code IS '表单编码';
COMMENT ON COLUMN public.wf_form.form_type IS '表单类型：dynamic-动态表单，custom-自定义表单';
COMMENT ON COLUMN public.wf_form.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN public.wf_form.form_config IS '表单配置JSON';
COMMENT ON COLUMN public.wf_form.form_content IS '表单内容JSON';
COMMENT ON COLUMN public.wf_form.version IS '版本号';
COMMENT ON COLUMN public.wf_form.remark IS '备注';
COMMENT ON COLUMN public.wf_form.create_user_id IS '创建人ID';
COMMENT ON COLUMN public.wf_form.create_time IS '创建时间';
COMMENT ON COLUMN public.wf_form.update_time IS '更新时间';


-- 流程实例扩展表
CREATE TABLE IF NOT EXISTS public.wf_instance (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  process_instance_id VARCHAR(100) NOT NULL,
                                                  process_definition_id VARCHAR(100) NOT NULL,
                                                  process_name VARCHAR(200),
                                                  business_key VARCHAR(200),
                                                  business_data TEXT,
                                                  start_user_id BIGINT,
                                                  start_user_name VARCHAR(100),
                                                  status SMALLINT DEFAULT 0,
                                                  remark VARCHAR(500),
                                                  create_time TIMESTAMP,
                                                  update_time TIMESTAMP,
                                                  end_time TIMESTAMP,
                                                  CONSTRAINT uk_process_instance_id UNIQUE (process_instance_id)
);
CREATE INDEX IF NOT EXISTS idx_business_key ON public.wf_instance(business_key);
CREATE INDEX IF NOT EXISTS idx_start_user ON public.wf_instance(start_user_id);
COMMENT ON TABLE public.wf_instance IS '流程实例扩展表';
COMMENT ON COLUMN public.wf_instance.id IS '主键ID';
COMMENT ON COLUMN public.wf_instance.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN public.wf_instance.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN public.wf_instance.process_name IS '流程名称';
COMMENT ON COLUMN public.wf_instance.business_key IS '业务KEY';
COMMENT ON COLUMN public.wf_instance.business_data IS '业务数据JSON';
COMMENT ON COLUMN public.wf_instance.start_user_id IS '发起人ID';
COMMENT ON COLUMN public.wf_instance.start_user_name IS '发起人姓名';
COMMENT ON COLUMN public.wf_instance.status IS '状态：0-进行中，1-已完成，2-已作废';
COMMENT ON COLUMN public.wf_instance.remark IS '备注';
COMMENT ON COLUMN public.wf_instance.create_time IS '创建时间';
COMMENT ON COLUMN public.wf_instance.update_time IS '更新时间';
COMMENT ON COLUMN public.wf_instance.end_time IS '结束时间';


-- 委托代理表
CREATE TABLE IF NOT EXISTS public.wf_delegate (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  delegator_id BIGINT NOT NULL,
                                                  delegator_name VARCHAR(100) NOT NULL,
                                                  attorney_id BIGINT NOT NULL,
                                                  attorney_name VARCHAR(100) NOT NULL,
                                                  process_definition_key VARCHAR(100),
                                                  start_time TIMESTAMP NOT NULL,
                                                  end_time TIMESTAMP NOT NULL,
                                                  status SMALLINT DEFAULT 1,
                                                  remark VARCHAR(500),
                                                  create_time TIMESTAMP,
                                                  update_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_delegator ON public.wf_delegate(delegator_id);
CREATE INDEX IF NOT EXISTS idx_attorney ON public.wf_delegate(attorney_id);
CREATE INDEX IF NOT EXISTS idx_delegate_status ON public.wf_delegate(status);
COMMENT ON TABLE public.wf_delegate IS '委托代理表';
COMMENT ON COLUMN public.wf_delegate.id IS '主键ID';
COMMENT ON COLUMN public.wf_delegate.delegator_id IS '委托人ID';
COMMENT ON COLUMN public.wf_delegate.delegator_name IS '委托人姓名';
COMMENT ON COLUMN public.wf_delegate.attorney_id IS '代理人ID';
COMMENT ON COLUMN public.wf_delegate.attorney_name IS '代理人姓名';
COMMENT ON COLUMN public.wf_delegate.process_definition_key IS '流程定义KEY，空表示全部';
COMMENT ON COLUMN public.wf_delegate.start_time IS '开始时间';
COMMENT ON COLUMN public.wf_delegate.end_time IS '结束时间';
COMMENT ON COLUMN public.wf_delegate.status IS '状态：0-禁用，1-启用';
COMMENT ON COLUMN public.wf_delegate.remark IS '备注';
COMMENT ON COLUMN public.wf_delegate.create_time IS '创建时间';
COMMENT ON COLUMN public.wf_delegate.update_time IS '更新时间';


-- ===============================================================
-- 工作流模块菜单管理数据
-- ===============================================================

-- 工作流模块父菜单
INSERT INTO public.sys_menu VALUES (100, '工作流管理', 0, '', '', '', 1, 'Operation', 5, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程定义管理
INSERT INTO public.sys_menu VALUES (101, '流程定义', 100, '/workflow/process-list', 'views/workflow/ProcessList.vue', 'workflow:process:list', 2, 'DocumentCopy', 1, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程设计器
INSERT INTO public.sys_menu VALUES (102, '流程设计', 100, '/workflow/designer', 'views/workflow/ProcessDesigner.vue', 'workflow:process:design', 2, 'Edit', 2, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程实例管理（含跟踪）
INSERT INTO public.sys_menu VALUES (103, '流程实例', 100, '/workflow/instance-list', 'views/workflow/ProcessInstanceList.vue', 'workflow:instance:list', 2, 'View', 3, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 待办任务
INSERT INTO public.sys_menu VALUES (104, '待办任务', 100, '/workflow/todo-task', 'views/workflow/TodoTaskList.vue', 'workflow:task:todo', 2, 'List', 4, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 已办任务
INSERT INTO public.sys_menu VALUES (105, '已办任务', 100, '/workflow/done-task', 'views/workflow/DoneTaskList.vue', 'workflow:task:done', 2, 'DocumentChecked', 5, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 表单管理
INSERT INTO public.sys_menu VALUES (106, '表单管理', 100, '/workflow/form-list', 'views/workflow/FormList.vue', 'workflow:form:list', 2, 'Document', 6, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程分类管理
INSERT INTO public.sys_menu VALUES (107, '流程分类', 100, '/workflow/category-list', 'views/workflow/CategoryList.vue', 'workflow:category:list', 2, 'Files', 7, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 委托配置管理
INSERT INTO public.sys_menu VALUES (108, '委托配置', 100, '/workflow/delegate-list', 'views/workflow/DelegateList.vue', 'workflow:delegate:list', 2, 'SwitchButton', 8, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');


-- ===============================================================
-- 角色菜单权限配置
-- ===============================================================

-- 超级管理员拥有工作流模块所有权限
INSERT INTO public.sys_role_menu VALUES (700, 1, 100, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (701, 1, 101, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (702, 1, 102, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (703, 1, 103, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (704, 1, 104, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (705, 1, 105, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (706, 1, 106, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (707, 1, 107, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (708, 1, 108, '2026-05-10 00:00:00');

-- 普通用户拥有工作流模块基础权限
INSERT INTO public.sys_role_menu VALUES (750, 2, 100, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (751, 2, 103, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (752, 2, 104, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (753, 2, 105, '2026-05-10 00:00:00');
