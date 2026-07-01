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


-- ===============================================================
-- 2026-06-24 工作流增强：处理人配置 / 抄送 / 撤回
-- ===============================================================

-- 节点处理人配置表（流程设计师在 UserTask 节点上配置，发布时持久化）
-- 用于「流程设计面板」展示节点处理人配置，以及发起时校验节点可处理人
-- 真实运行时仍以 BPMN XML 的 camunda:assignee / candidateUsers / candidateGroups 为准
CREATE TABLE IF NOT EXISTS public.wf_node_handler (
    id BIGSERIAL PRIMARY KEY,
    process_definition_id VARCHAR(100) NOT NULL,
    process_definition_key VARCHAR(100) NOT NULL,
    node_id VARCHAR(100) NOT NULL,
    node_name VARCHAR(200),
    handler_type VARCHAR(50) NOT NULL,
    handler_config TEXT,
    multi_instance_type VARCHAR(20) DEFAULT 'none',
    multi_instance_config TEXT,
    cc_config TEXT,
    seq INT DEFAULT 0,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    CONSTRAINT uk_node_handler UNIQUE (process_definition_id, node_id)
);
CREATE INDEX IF NOT EXISTS idx_node_handler_key ON public.wf_node_handler(process_definition_key);
COMMENT ON TABLE public.wf_node_handler IS '流程节点处理人配置表';
COMMENT ON COLUMN public.wf_node_handler.id IS '主键ID';
COMMENT ON COLUMN public.wf_node_handler.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN public.wf_node_handler.process_definition_key IS '流程定义KEY';
COMMENT ON COLUMN public.wf_node_handler.node_id IS '节点ID（UserTask BPMN id）';
COMMENT ON COLUMN public.wf_node_handler.node_name IS '节点名称';
COMMENT ON COLUMN public.wf_node_handler.handler_type IS '处理人类型（字典 wf_handler_type）';
COMMENT ON COLUMN public.wf_node_handler.handler_config IS '处理人配置 JSON：人员ID/角色ID/变量名/表单字段等';
COMMENT ON COLUMN public.wf_node_handler.multi_instance_type IS '多实例类型：none/parallel/sequential';
COMMENT ON COLUMN public.wf_node_handler.multi_instance_config IS '多实例配置 JSON：完成条件、比例等';
COMMENT ON COLUMN public.wf_node_handler.cc_config IS '抄送配置 JSON：人员、角色、触发时机';
COMMENT ON COLUMN public.wf_node_handler.seq IS '排序';


-- 抄送记录表
CREATE TABLE IF NOT EXISTS public.wf_cc (
    id BIGSERIAL PRIMARY KEY,
    process_instance_id VARCHAR(100) NOT NULL,
    process_definition_id VARCHAR(100),
    process_name VARCHAR(200),
    node_id VARCHAR(100),
    node_name VARCHAR(200),
    cc_user_id BIGINT NOT NULL,
    cc_user_name VARCHAR(100) NOT NULL,
    title VARCHAR(200),
    content TEXT,
    from_user_id BIGINT,
    from_user_name VARCHAR(100),
    read_status SMALLINT DEFAULT 0,
    read_time TIMESTAMP,
    create_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_cc_user ON public.wf_cc(cc_user_id);
CREATE INDEX IF NOT EXISTS idx_cc_instance ON public.wf_cc(process_instance_id);
CREATE INDEX IF NOT EXISTS idx_cc_status ON public.wf_cc(read_status);
COMMENT ON TABLE public.wf_cc IS '流程抄送表';
COMMENT ON COLUMN public.wf_cc.id IS '主键ID';
COMMENT ON COLUMN public.wf_cc.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN public.wf_cc.process_definition_id IS '流程定义ID';
COMMENT ON COLUMN public.wf_cc.process_name IS '流程名称';
COMMENT ON COLUMN public.wf_cc.node_id IS '触发抄送的节点ID';
COMMENT ON COLUMN public.wf_cc.node_name IS '触发抄送的节点名称';
COMMENT ON COLUMN public.wf_cc.cc_user_id IS '抄送人ID';
COMMENT ON COLUMN public.wf_cc.cc_user_name IS '抄送人姓名';
COMMENT ON COLUMN public.wf_cc.title IS '抄送标题';
COMMENT ON COLUMN public.wf_cc.content IS '抄送内容/意见';
COMMENT ON COLUMN public.wf_cc.from_user_id IS '抄送发起人ID';
COMMENT ON COLUMN public.wf_cc.from_user_name IS '抄送发起人姓名';
COMMENT ON COLUMN public.wf_cc.read_status IS '阅读状态：0=未读 1=已读';
COMMENT ON COLUMN public.wf_cc.read_time IS '阅读时间';
COMMENT ON COLUMN public.wf_cc.create_time IS '创建时间';


-- 给 wf_instance 增加撤回相关字段（DO 块兼容已有库）
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='wf_instance' AND column_name='withdraw_user_id') THEN
        ALTER TABLE public.wf_instance ADD COLUMN withdraw_user_id BIGINT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='wf_instance' AND column_name='withdraw_user_name') THEN
        ALTER TABLE public.wf_instance ADD COLUMN withdraw_user_name VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='wf_instance' AND column_name='withdraw_time') THEN
        ALTER TABLE public.wf_instance ADD COLUMN withdraw_time TIMESTAMP;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema='public' AND table_name='wf_instance' AND column_name='withdraw_reason') THEN
        ALTER TABLE public.wf_instance ADD COLUMN withdraw_reason VARCHAR(500);
    END IF;
END$$;
COMMENT ON COLUMN public.wf_instance.withdraw_user_id IS '撤回人ID';
COMMENT ON COLUMN public.wf_instance.withdraw_user_name IS '撤回人姓名';
COMMENT ON COLUMN public.wf_instance.withdraw_time IS '撤回时间';
COMMENT ON COLUMN public.wf_instance.withdraw_reason IS '撤回原因';


-- ===============================================================
-- 新增工作流业务字典
-- ===============================================================

-- 33. 处理人类型（wf_handler_type）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (33, '处理人类型', 'wf_handler_type', '工作流-节点处理人类型', 1, '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (330, 33, '指定人员', 'ASSIGNEE', 1, '', 'primary', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (331, 33, '候选人', 'CANDIDATE_USERS', 2, '', 'success', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (332, 33, '候选角色', 'CANDIDATE_GROUPS', 3, '', 'info', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (333, 33, '发起人', 'INITIATOR', 4, '', 'warning', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (334, 33, '发起人部门负责人', 'DEPT_LEADER', 5, '', 'warning', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (335, 33, '角色负责人', 'ROLE_LEADER', 6, '', 'warning', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (336, 33, '上一步处理人', 'PREV_ASSIGNEE', 7, '', 'info', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (337, 33, '表单内人员字段', 'FORM_FIELD', 8, '', 'primary', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (338, 33, '流程变量', 'VARIABLE', 9, '', 'info', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');

-- 34. 流程实例状态（wf_instance_status，含"已撤回"）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (34, '流程实例状态', 'wf_instance_status', '工作流-流程实例状态（0=进行中 1=已完成 2=已作废 3=已撤回）', 1, '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (340, 34, '进行中', '0', 1, '', 'primary', 1, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (341, 34, '已完成', '1', 2, '', 'success', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (342, 34, '已作废', '2', 3, '', 'info', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (343, 34, '已撤回', '3', 4, '', 'warning', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');

-- 35. 多实例会签类型（wf_multi_instance_type）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (35, '多实例类型', 'wf_multi_instance_type', '工作流-多实例类型（none/single/parallel/sequential/countersign）', 1, '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (350, 35, '单人审批', 'none', 1, '', 'info', 1, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (351, 35, '会签（全部同意）', 'countersign', 2, '', 'primary', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (352, 35, '或签（任一同意）', 'or_sign', 3, '', 'success', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (353, 35, '顺序会签', 'sequential', 4, '', 'warning', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (354, 35, '按比例通过', 'ratio', 5, '', 'warning', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');

-- 36. 抄送状态（wf_cc_status）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (36, '抄送状态', 'wf_cc_status', '工作流-抄送阅读状态（0=未读 1=已读）', 1, '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (360, 36, '未读', '0', 1, '', 'danger', 1, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (361, 36, '已读', '1', 2, '', 'info', 0, 1, '', '2026-06-24 00:00:00', '2026-06-24 00:00:00');


-- ===============================================================
-- 新增工作流菜单
-- ===============================================================

-- 109. 发起流程（卡片列表，替代旧版 /workflow/start）
INSERT INTO public.sys_menu VALUES (109, '发起流程', 100, '/workflow/start', 'views/workflow/ProcessStart.vue', 'workflow:start:list', 2, 'Promotion', 2, 1, '2026-06-24 00:00:00', '2026-06-24 00:00:00');

-- 110. 抄送我的
INSERT INTO public.sys_menu VALUES (110, '抄送我的', 100, '/workflow/cc-list', 'views/workflow/CcList.vue', 'workflow:cc:list', 2, 'Share', 9, 1, '2026-06-24 00:00:00', '2026-06-24 00:00:00');

-- 111. 我发起的流程（独立入口，旧版合并在 103 流程实例下）
INSERT INTO public.sys_menu VALUES (111, '我发起的', 100, '/workflow/my-started', 'views/workflow/MyStartedList.vue', 'workflow:instance:my', 2, 'EditPen', 10, 1, '2026-06-24 00:00:00', '2026-06-24 00:00:00');

-- 超级管理员拥有新增菜单权限
INSERT INTO public.sys_role_menu VALUES (709, 1, 109, '2026-06-24 00:00:00');
INSERT INTO public.sys_role_menu VALUES (710, 1, 110, '2026-06-24 00:00:00');
INSERT INTO public.sys_role_menu VALUES (711, 1, 111, '2026-06-24 00:00:00');

-- 普通用户拥有新增菜单权限
INSERT INTO public.sys_role_menu VALUES (754, 2, 109, '2026-06-24 00:00:00');
INSERT INTO public.sys_role_menu VALUES (755, 2, 110, '2026-06-24 00:00:00');
INSERT INTO public.sys_role_menu VALUES (756, 2, 111, '2026-06-24 00:00:00');

-- ===============================================================
-- P1-8 流程催办记录表
-- ===============================================================
CREATE TABLE IF NOT EXISTS public.wf_urge_record (
    id BIGSERIAL PRIMARY KEY,
    process_instance_id VARCHAR(64) NOT NULL,
    task_id VARCHAR(64) NOT NULL,
    from_user_id BIGINT NOT NULL,
    to_user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE public.wf_urge_record IS '流程催办记录';
COMMENT ON COLUMN public.wf_urge_record.id IS '主键ID';
COMMENT ON COLUMN public.wf_urge_record.process_instance_id IS '流程实例ID';
COMMENT ON COLUMN public.wf_urge_record.task_id IS '任务ID';
COMMENT ON COLUMN public.wf_urge_record.from_user_id IS '催办人 userId';
COMMENT ON COLUMN public.wf_urge_record.to_user_id IS '接收人 userId';
COMMENT ON COLUMN public.wf_urge_record.content IS '催办内容';
COMMENT ON COLUMN public.wf_urge_record.create_time IS '催办时间';

CREATE INDEX IF NOT EXISTS idx_urge_record_task ON public.wf_urge_record(task_id);
CREATE INDEX IF NOT EXISTS idx_urge_record_instance ON public.wf_urge_record(process_instance_id);
CREATE INDEX IF NOT EXISTS idx_urge_record_to_user ON public.wf_urge_record(to_user_id, create_time DESC);
CREATE INDEX IF NOT EXISTS idx_urge_record_from_user ON public.wf_urge_record(from_user_id, create_time DESC);


-- ====================== P3-1 流程模板市场 ======================
CREATE TABLE IF NOT EXISTS public.wf_template (
    id BIGSERIAL PRIMARY KEY,
    template_key VARCHAR(100) NOT NULL,
    template_name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    description TEXT,
    bpmn_xml TEXT NOT NULL,
    icon VARCHAR(255),
    use_count INT DEFAULT 0,
    sort_no INT DEFAULT 0,
    status SMALLINT DEFAULT 1, -- 1启用 0停用
    create_by VARCHAR(64),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0,
    CONSTRAINT uk_template_key UNIQUE (template_key)
);
COMMENT ON TABLE public.wf_template IS '流程模板市场：内置 4-5 个常用模板（请假/报销/出差/通用审批）供一键新建';
COMMENT ON COLUMN public.wf_template.template_key IS '模板 KEY（唯一）';
COMMENT ON COLUMN public.wf_template.template_name IS '模板名称';
COMMENT ON COLUMN public.wf_template.category IS '分类（leave/reimburse/trip/general）';
COMMENT ON COLUMN public.wf_template.bpmn_xml IS 'BPMN 2.0 XML 模板内容';
COMMENT ON COLUMN public.wf_template.use_count IS '使用次数';
COMMENT ON COLUMN public.wf_template.status IS '状态：1启用 0停用';

CREATE INDEX IF NOT EXISTS idx_template_category ON public.wf_template(category, status);
CREATE INDEX IF NOT EXISTS idx_template_sort ON public.wf_template(sort_no, use_count DESC);
