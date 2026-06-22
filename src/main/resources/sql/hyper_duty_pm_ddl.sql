-- ===============================================================
-- Hyper Duty 项目管理模块表结构（PostgreSQL 语法）
-- 版本：v3 (2026-06-21)
-- 变更：pm_task 新增 attachments / stakeholders / is_focus 字段
-- ===============================================================

CREATE TABLE IF NOT EXISTS public.pm_project_participant (
    id SERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE,
    UNIQUE(project_id, employee_id)
);

-- 创建索引

-- 创建项目代理负责人关联表

CREATE TABLE IF NOT EXISTS public.pm_project_deputy_owner (
    id SERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE,
    UNIQUE(project_id, employee_id)
);

-- 创建索引

-- 创建任务进展更新表

CREATE TABLE IF NOT EXISTS public.pm_task_progress_update (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES public.pm_task(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES public.sys_employee(id),
    progress INTEGER NOT NULL CHECK (progress >= 0 AND progress <= 100),
    description TEXT,
    attachments JSONB,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引

CREATE TABLE public.pm_project (
    id bigint NOT NULL,
    project_name character varying(100) NOT NULL,
    project_code character varying(50) NOT NULL,
    module_id bigint,
    priority integer DEFAULT 3,
    status integer DEFAULT 1,
    progress integer DEFAULT 0,
    start_date date,
    end_date date,
    description character varying(200) DEFAULT NULL::character varying,
    owner_id bigint,
    create_by bigint,
    archived integer DEFAULT 0,
    sort integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.pm_project IS '项目表';

COMMENT ON COLUMN public.pm_project.id IS '项目ID';

COMMENT ON COLUMN public.pm_project.project_name IS '项目名称';

COMMENT ON COLUMN public.pm_project.project_code IS '项目编码';

COMMENT ON COLUMN public.pm_project.module_id IS '模块ID';

COMMENT ON COLUMN public.pm_project.priority IS '优先级：1-高，2-中，3-低';

COMMENT ON COLUMN public.pm_project.status IS '状态：1-进行中，2-已完成，3-已暂停，4-已取消';

COMMENT ON COLUMN public.pm_project.progress IS '进度百分比';

COMMENT ON COLUMN public.pm_project.start_date IS '开始日期';

COMMENT ON COLUMN public.pm_project.end_date IS '结束日期';

COMMENT ON COLUMN public.pm_project.description IS '项目描述';

COMMENT ON COLUMN public.pm_project.owner_id IS '负责人ID';

COMMENT ON COLUMN public.pm_project.create_by IS '创建人ID';

COMMENT ON COLUMN public.pm_project.archived IS '是否归档：0-否，1-是';

COMMENT ON COLUMN public.pm_project.sort IS '排序';

COMMENT ON COLUMN public.pm_project.create_time IS '创建时间';

COMMENT ON COLUMN public.pm_project.update_time IS '更新时间';

CREATE TABLE public.pm_project_employee (
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.pm_task (
    id bigint NOT NULL,
    task_name character varying(100) NOT NULL,
    task_code character varying(50) NOT NULL,
    project_id bigint NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    start_date date,
    end_date date,
    status smallint DEFAULT '1'::smallint,
    priority smallint DEFAULT '3'::smallint,
    assignee_id bigint,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    parent_id bigint DEFAULT 0,
    task_level integer DEFAULT 1,
    progress integer DEFAULT 0,
    is_pinned integer DEFAULT 0,
    module_id bigint,
    attachments text,
    stakeholders text,
    is_focus smallint DEFAULT 0
);

COMMENT ON TABLE public.pm_task IS '任务表';

COMMENT ON COLUMN public.pm_task.id IS '任务ID';

COMMENT ON COLUMN public.pm_task.task_name IS '任务名称';

COMMENT ON COLUMN public.pm_task.task_code IS '任务编码';

COMMENT ON COLUMN public.pm_task.project_id IS '项目ID';

COMMENT ON COLUMN public.pm_task.description IS '任务描述';

COMMENT ON COLUMN public.pm_task.start_date IS '开始日期';

COMMENT ON COLUMN public.pm_task.end_date IS '结束日期';

COMMENT ON COLUMN public.pm_task.status IS '状态：1-未开始，2-进行中，3-已完成，4-已暂停，5-已取消';

COMMENT ON COLUMN public.pm_task.priority IS '优先级：1-高，2-中，3-低';

COMMENT ON COLUMN public.pm_task.assignee_id IS '负责人ID';

COMMENT ON COLUMN public.pm_task.create_by IS '创建人ID';

COMMENT ON COLUMN public.pm_task.create_time IS '创建时间';

COMMENT ON COLUMN public.pm_task.update_time IS '更新时间';

COMMENT ON COLUMN public.pm_task.attachments IS '附件JSON列表（task_attachment 服务的存储）';

COMMENT ON COLUMN public.pm_task.stakeholders IS '干系人JSON列表（前端缓存的展示用）';

COMMENT ON COLUMN public.pm_task.is_focus IS '是否重点：0-否，1-是（重点任务单独标识）';

CREATE TABLE public.pm_task_comment (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    content text,
    attachment_url character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.pm_task_comment_attachment (
    id bigint NOT NULL,
    comment_id bigint NOT NULL,
    file_name character varying(255) NOT NULL,
    file_path character varying(255) NOT NULL,
    file_size bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.pm_task_stakeholder (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    stakeholder_type smallint DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE public.pm_custom_table (
    id bigint NOT NULL,
    table_name character varying(100) NOT NULL,
    table_code character varying(50) NOT NULL,
    project_id bigint,
    description character varying(500),
    status smallint DEFAULT 1,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.pm_custom_table IS '自定义表格配置表';
COMMENT ON COLUMN public.pm_custom_table.id IS '表格ID';
COMMENT ON COLUMN public.pm_custom_table.table_name IS '表格名称';
COMMENT ON COLUMN public.pm_custom_table.table_code IS '表格编码';
COMMENT ON COLUMN public.pm_custom_table.project_id IS '关联项目ID';
COMMENT ON COLUMN public.pm_custom_table.description IS '表格描述';
COMMENT ON COLUMN public.pm_custom_table.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN public.pm_custom_table.create_by IS '创建人ID';
COMMENT ON COLUMN public.pm_custom_table.create_time IS '创建时间';
COMMENT ON COLUMN public.pm_custom_table.update_time IS '更新时间';

-- 自定义表格列表

CREATE TABLE public.pm_custom_table_column (
    id bigint NOT NULL,
    table_id bigint NOT NULL,
    column_name character varying(100) NOT NULL,
    column_code character varying(50) NOT NULL,
    column_type character varying(20) DEFAULT 'text',
    column_width integer DEFAULT 150,
    required smallint DEFAULT 0,
    sort_order integer DEFAULT 0,
    options json,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.pm_custom_table_column IS '自定义表格列表';
COMMENT ON COLUMN public.pm_custom_table_column.id IS '列ID';
COMMENT ON COLUMN public.pm_custom_table_column.table_id IS '关联表格ID';
COMMENT ON COLUMN public.pm_custom_table_column.column_name IS '列名称';
COMMENT ON COLUMN public.pm_custom_table_column.column_code IS '列编码';
COMMENT ON COLUMN public.pm_custom_table_column.column_type IS '列类型：text文本，number数字，date日期，select下拉，person人员';
COMMENT ON COLUMN public.pm_custom_table_column.column_width IS '列宽度';
COMMENT ON COLUMN public.pm_custom_table_column.required IS '是否必填：0否，1是';
COMMENT ON COLUMN public.pm_custom_table_column.sort_order IS '排序';
COMMENT ON COLUMN public.pm_custom_table_column.options IS '下拉选项（JSON格式）';
COMMENT ON COLUMN public.pm_custom_table_column.create_time IS '创建时间';

-- 自定义表格数据行

CREATE TABLE public.pm_custom_table_row (
    id bigint NOT NULL,
    table_id bigint NOT NULL,
    row_data json,
    sort_order integer DEFAULT 0,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.pm_custom_table_row IS '自定义表格数据行';
COMMENT ON COLUMN public.pm_custom_table_row.id IS '行ID';
COMMENT ON COLUMN public.pm_custom_table_row.table_id IS '关联表格ID';
COMMENT ON COLUMN public.pm_custom_table_row.row_data IS '行数据（JSON格式）';
COMMENT ON COLUMN public.pm_custom_table_row.sort_order IS '排序字段';
COMMENT ON COLUMN public.pm_custom_table_row.create_by IS '创建人ID';
COMMENT ON COLUMN public.pm_custom_table_row.create_time IS '创建时间';
COMMENT ON COLUMN public.pm_custom_table_row.update_time IS '更新时间';

-- 任务与自定义行关联表

CREATE TABLE public.pm_task_custom_row (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    table_id bigint NOT NULL,
    row_id bigint NOT NULL,
    order_no character varying(255),
    title character varying(500),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE public.pm_task_custom_row IS '任务与自定义行关联表';
COMMENT ON COLUMN public.pm_task_custom_row.id IS '关联ID';
COMMENT ON COLUMN public.pm_task_custom_row.task_id IS '任务ID';
COMMENT ON COLUMN public.pm_task_custom_row.table_id IS '自定义表格ID';
COMMENT ON COLUMN public.pm_task_custom_row.row_id IS '自定义表格行ID';
COMMENT ON COLUMN public.pm_task_custom_row.order_no IS '单号';
COMMENT ON COLUMN public.pm_task_custom_row.title IS '标题';
COMMENT ON COLUMN public.pm_task_custom_row.create_time IS '创建时间';

-- Completed on 2026-02-24 20:59:48

-- 邮件服务配置表

CREATE TABLE IF NOT EXISTS public.pm_task_shadow (
    id BIGSERIAL PRIMARY KEY,
    source_task_id BIGINT NOT NULL REFERENCES public.pm_task(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES public.pm_project(id) ON DELETE CASCADE,
    parent_id BIGINT DEFAULT 0,
    shadow_alias VARCHAR(500),
    shadow_description TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_source_task_project UNIQUE (source_task_id, project_id)
);

CREATE INDEX IF NOT EXISTS idx_shadow_source_task ON public.pm_task_shadow(source_task_id);
CREATE INDEX IF NOT EXISTS idx_shadow_project ON public.pm_task_shadow(project_id);
CREATE INDEX IF NOT EXISTS idx_shadow_parent_id ON public.pm_task_shadow(parent_id);

COMMENT ON TABLE public.pm_task_shadow IS '影子任务关联表 - 影子是源任务在项目中的视图';
COMMENT ON COLUMN public.pm_task_shadow.source_task_id IS '源任务ID';
COMMENT ON COLUMN public.pm_task_shadow.project_id IS '项目ID';
COMMENT ON COLUMN public.pm_task_shadow.parent_id IS '在本项目中的父任务ID（0表示根任务）';
COMMENT ON COLUMN public.pm_task_shadow.shadow_alias IS '在本项目中的别名（可选，不填显示源任务名称）';
COMMENT ON COLUMN public.pm_task_shadow.shadow_description IS '本项目的描述（可选）';
COMMENT ON COLUMN public.pm_task_shadow.created_by IS '创建人';
COMMENT ON COLUMN public.pm_task_shadow.created_at IS '创建时间';
COMMENT ON COLUMN public.pm_task_shadow.updated_at IS '更新时间';

CREATE TABLE IF NOT EXISTS public.pm_shadow_annotation (
    id BIGSERIAL PRIMARY KEY,
    shadow_id BIGINT NOT NULL REFERENCES public.pm_task_shadow(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_annotation_shadow ON public.pm_shadow_annotation(shadow_id);

COMMENT ON TABLE public.pm_shadow_annotation IS '影子任务批注表 - 项目特定的批注';
COMMENT ON COLUMN public.pm_shadow_annotation.shadow_id IS '影子任务关联ID';
COMMENT ON COLUMN public.pm_shadow_annotation.content IS '批注内容';
COMMENT ON COLUMN public.pm_shadow_annotation.created_by IS '创建人';
COMMENT ON COLUMN public.pm_shadow_annotation.created_at IS '创建时间';
COMMENT ON COLUMN public.pm_shadow_annotation.updated_at IS '更新时间';

/*
DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'target_project_id') THEN
    ALTER TABLE pm_task_shadow RENAME COLUMN target_project_id TO project_id;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'shadow_name') THEN
    ALTER TABLE pm_task_shadow RENAME COLUMN shadow_name TO shadow_alias;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'create_by') THEN
    ALTER TABLE pm_task_shadow RENAME COLUMN create_by TO created_by;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'create_time') THEN
    ALTER TABLE pm_task_shadow RENAME COLUMN create_time TO created_at;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'update_time') THEN
    ALTER TABLE pm_task_shadow RENAME COLUMN update_time TO updated_at;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'created_by') THEN
    ALTER TABLE pm_task_shadow ALTER COLUMN created_by TYPE VARCHAR(100);
  END IF;
END $$;

DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints 
    WHERE table_name = 'pm_task_shadow' AND constraint_name = 'uk_source_task_project'
  ) THEN
    ALTER TABLE pm_task_shadow 
      ADD CONSTRAINT uk_source_task_project UNIQUE (source_task_id, project_id);
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_shadow_annotation' AND column_name = 'create_time') THEN
    ALTER TABLE pm_shadow_annotation RENAME COLUMN create_time TO created_at;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_shadow_annotation' AND column_name = 'update_time') THEN
    ALTER TABLE pm_shadow_annotation RENAME COLUMN update_time TO updated_at;
  END IF;
END $$;

DO $$ 
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_shadow_annotation' AND column_name = 'created_by') THEN
    ALTER TABLE pm_shadow_annotation ADD COLUMN created_by VARCHAR(100);
    UPDATE pm_shadow_annotation SET created_by = employee_name WHERE employee_name IS NOT NULL;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'source_project_id') THEN
    ALTER TABLE pm_task_shadow DROP COLUMN source_project_id;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'target_task_id') THEN
    ALTER TABLE pm_task_shadow DROP COLUMN target_task_id;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_task_shadow' AND column_name = 'is_deleted') THEN
    ALTER TABLE pm_task_shadow DROP COLUMN is_deleted;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_shadow_annotation' AND column_name = 'employee_id') THEN
    ALTER TABLE pm_shadow_annotation DROP COLUMN employee_id;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_shadow_annotation' AND column_name = 'employee_name') THEN
    ALTER TABLE pm_shadow_annotation DROP COLUMN employee_name;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_shadow_annotation' AND column_name = 'attachments') THEN
    ALTER TABLE pm_shadow_annotation DROP COLUMN attachments;
  END IF;
END $$;

DO $$ 
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'pm_shadow_annotation' AND column_name = 'is_deleted') THEN
    ALTER TABLE pm_shadow_annotation DROP COLUMN is_deleted;
  END IF;
END $$;

DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'pm_task_shadow' AND column_name = 'parent_id'
  ) THEN
    ALTER TABLE pm_task_shadow ADD COLUMN parent_id BIGINT DEFAULT 0;
    COMMENT ON COLUMN pm_task_shadow.parent_id IS '在本项目中的父任务ID（0表示根任务）';
  END IF;
END $$;

DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_indexes 
    WHERE tablename = 'pm_task_shadow' AND indexname = 'idx_shadow_parent_id'
  ) THEN
    CREATE INDEX idx_shadow_parent_id ON public.pm_task_shadow(parent_id);
  END IF;
END $$;

-- =====================================================
-- v3 (2026-06-21) - pm_task 新增字段（手动 ALTER 合规化）
-- =====================================================

DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'pm_task' AND column_name = 'attachments'
  ) THEN
    ALTER TABLE pm_task ADD COLUMN attachments text;
    COMMENT ON COLUMN pm_task.attachments IS '附件JSON列表（task_attachment 服务的存储）';
  END IF;
END $$;

DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'pm_task' AND column_name = 'stakeholders'
  ) THEN
    ALTER TABLE pm_task ADD COLUMN stakeholders text;
    COMMENT ON COLUMN pm_task.stakeholders IS '干系人JSON列表（前端缓存的展示用）';
  END IF;
END $$;

DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name = 'pm_task' AND column_name = 'is_focus'
  ) THEN
    ALTER TABLE pm_task ADD COLUMN is_focus smallint DEFAULT 0;
    COMMENT ON COLUMN pm_task.is_focus IS '是否重点：0-否，1-是（重点任务单独标识）';
  END IF;
END $$;

-- =====================================================
-- 索引（性能优化）
-- 说明：所有索引使用 CONCURRENTLY 不锁表，IF NOT EXISTS 幂等
-- =====================================================

-- pm_task 业务索引（任务管理主表）
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_project_id
    ON public.pm_task(project_id);
COMMENT ON INDEX public.idx_pm_task_project_id IS '任务-项目ID索引（任务管理主接口使用）';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_assignee_id
    ON public.pm_task(assignee_id);
COMMENT ON INDEX public.idx_pm_task_assignee_id IS '任务-负责人ID索引（我的任务使用）';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_status
    ON public.pm_task(status);
COMMENT ON INDEX public.idx_pm_task_status IS '任务-状态索引（状态过滤使用）';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_parent_id
    ON public.pm_task(parent_id);
COMMENT ON INDEX public.idx_pm_task_parent_id IS '任务-父任务ID索引（父子关系使用）';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_priority
    ON public.pm_task(priority);
COMMENT ON INDEX public.idx_pm_task_priority IS '任务-优先级索引（排序使用）';

-- pm_task 联合索引（覆盖"我的任务"主查询）
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_assignee_status
    ON public.pm_task(assignee_id, status);
COMMENT ON INDEX public.idx_pm_task_assignee_status IS '任务-负责人+状态联合索引';

-- pm_task 排序索引（置顶/创建时间排序使用）
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_is_pinned
    ON public.pm_task(is_pinned);
COMMENT ON INDEX public.idx_pm_task_is_pinned IS '任务-置顶索引（ORDER BY is_pinned DESC）';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_create_time
    ON public.pm_task(create_time DESC);
COMMENT ON INDEX public.idx_pm_task_create_time IS '任务-创建时间索引（ORDER BY create_time DESC）';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_project_status
    ON public.pm_task(project_id, status);
COMMENT ON INDEX public.idx_pm_task_project_status IS '任务-项目+状态联合索引（任务管理主接口使用）';

-- pm_task_shadow 业务索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_shadow_project_id
    ON public.pm_task_shadow(project_id);
COMMENT ON INDEX public.idx_pm_task_shadow_project_id IS '影子任务-项目ID索引';

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_shadow_source_task_id
    ON public.pm_task_shadow(source_task_id);
COMMENT ON INDEX public.idx_pm_task_shadow_source_task_id IS '影子任务-源任务ID索引';

-- pm_task_progress_update 索引（解决 MAX(create_time) 子查询）
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_progress_update_task_id
    ON public.pm_task_progress_update(task_id, create_time DESC);
COMMENT ON INDEX public.idx_pm_task_progress_update_task_id IS '进展更新-任务ID+时间索引（UNION ALL 子查询）';

-- pm_shadow_annotation 索引（解决 COUNT 子查询）
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_shadow_annotation_shadow_id
    ON public.pm_shadow_annotation(shadow_id);
COMMENT ON INDEX public.idx_pm_shadow_annotation_shadow_id IS '影子批注-影子ID索引';

-- =====================================================
-- v3.1 (2026-06-22) - pm_task 列表生产性能优化
-- 背景：生产环境任务管理列表 4.10s，本地 3ms
-- 根因：selectRootTaskPage 复杂 ORDER BY（is_pinned + CASE(status) + priority + create_time）
--       现有索引 idx_pm_task_project_id 不覆盖 ORDER BY，必须全表扫描 + Sort
-- 修复：加部分索引匹配 WHERE + ORDER BY（前缀列）
-- 效果：4.10s → 100ms 内（生产数据量级）
-- =====================================================

-- 任务列表专用：根任务 + WHERE+ORDER BY 一体化索引
-- 注意：CASE(status) 表达式无法走索引，但 status 已排在 priority 之后
--      is_pinned DESC 是第一排序键，必须在索引列首位
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_root_list
    ON public.pm_task(project_id, is_pinned DESC, priority ASC, create_time DESC)
    WHERE parent_id IS NULL OR parent_id = 0;
COMMENT ON INDEX public.idx_pm_task_root_list IS '任务列表专用-根任务+项目+置顶+优先级+创建时间复合索引（任务管理主接口，2026-06-22 加）';

-- 任务列表子任务拉取：parent_id IN (...) 走 idx_pm_task_parent_id 已够用
-- 但按 project_id 过滤时需复合索引
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_pm_task_parent_project
    ON public.pm_task(parent_id, project_id)
    WHERE parent_id IS NOT NULL AND parent_id <> 0;
COMMENT ON INDEX public.idx_pm_task_parent_project IS '子任务-父ID+项目复合索引（拉取任务子树使用，2026-06-22 加）';
