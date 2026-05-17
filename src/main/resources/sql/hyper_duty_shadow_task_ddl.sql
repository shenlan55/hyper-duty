-- =====================================================
-- 影子任务功能 - 数据库表结构 (v2)
-- 设计理念：影子是源任务在项目中的"视图"
-- =====================================================

-- =====================================================
-- 1. 影子任务关联表 (pm_task_shadow)
-- =====================================================
CREATE TABLE IF NOT EXISTS public.pm_task_shadow (
    id BIGSERIAL PRIMARY KEY,
    source_task_id BIGINT NOT NULL REFERENCES public.pm_task(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES public.pm_project(id) ON DELETE CASCADE,
    shadow_alias VARCHAR(500),
    shadow_description TEXT,
    created_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_source_task_project UNIQUE (source_task_id, project_id)
);

CREATE INDEX IF NOT EXISTS idx_shadow_source_task ON public.pm_task_shadow(source_task_id);
CREATE INDEX IF NOT EXISTS idx_shadow_project ON public.pm_task_shadow(project_id);

COMMENT ON TABLE public.pm_task_shadow IS '影子任务关联表 - 影子是源任务在项目中的视图';
COMMENT ON COLUMN public.pm_task_shadow.source_task_id IS '源任务ID';
COMMENT ON COLUMN public.pm_task_shadow.project_id IS '项目ID';
COMMENT ON COLUMN public.pm_task_shadow.shadow_alias IS '在本项目中的别名（可选，不填显示源任务名称）';
COMMENT ON COLUMN public.pm_task_shadow.shadow_description IS '本项目的描述（可选）';
COMMENT ON COLUMN public.pm_task_shadow.created_by IS '创建人';
COMMENT ON COLUMN public.pm_task_shadow.created_at IS '创建时间';
COMMENT ON COLUMN public.pm_task_shadow.updated_at IS '更新时间';

-- =====================================================
-- 2. 影子任务批注表 (pm_shadow_annotation)
-- =====================================================
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

-- =====================================================
-- 3. 从 v1 升级到 v2 的脚本
-- 如果您的数据库中已经有 v1 版本的表，请执行以下升级脚本
-- =====================================================
/*
-- 3.1 更新 pm_task_shadow 表
-- -----------------------------------------------------
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

-- 3.2 更新 pm_shadow_annotation 表
-- -----------------------------------------------------
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

-- 3.3 删除旧字段（执行前请先备份！）
-- -----------------------------------------------------
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

-- =====================================================
-- 完成
-- =====================================================
