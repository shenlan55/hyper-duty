-- ===============================================================
-- Hyper Duty 工作流模块表结构更新脚本（PostgreSQL 语法）
-- ===============================================================

-- 更新流程分类表
-- 重命名列并添加新列
ALTER TABLE public.wf_category 
    RENAME COLUMN category_name TO name;

ALTER TABLE public.wf_category 
    RENAME COLUMN category_code TO code;

ALTER TABLE public.wf_category 
    RENAME COLUMN sort_order TO sort;

ALTER TABLE public.wf_category 
    ADD COLUMN IF NOT EXISTS status SMALLINT DEFAULT 1;

-- 更新注释
COMMENT ON COLUMN public.wf_category.name IS '分类名称';
COMMENT ON COLUMN public.wf_category.code IS '分类编码';
COMMENT ON COLUMN public.wf_category.sort IS '排序';
COMMENT ON COLUMN public.wf_category.status IS '状态：0-禁用，1-启用';


-- 更新流程表单表
-- 重命名列并添加新列
ALTER TABLE public.wf_form 
    RENAME COLUMN form_name TO name;

ALTER TABLE public.wf_form 
    RENAME COLUMN form_code TO code;

ALTER TABLE public.wf_form 
    RENAME COLUMN create_user_id TO create_user_id; -- 保持不变

ALTER TABLE public.wf_form 
    ADD COLUMN IF NOT EXISTS form_type VARCHAR(50) DEFAULT 'dynamic';

ALTER TABLE public.wf_form 
    ADD COLUMN IF NOT EXISTS status SMALLINT DEFAULT 1;

-- 更新注释
COMMENT ON COLUMN public.wf_form.name IS '表单名称';
COMMENT ON COLUMN public.wf_form.code IS '表单编码';
COMMENT ON COLUMN public.wf_form.form_type IS '表单类型：dynamic-动态表单，custom-自定义表单';
COMMENT ON COLUMN public.wf_form.status IS '状态：0-禁用，1-启用';
