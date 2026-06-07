-- ===============================================================
-- Hyper Duty AI模块表结构（PostgreSQL 语法）
-- ===============================================================

CREATE TABLE public.ai_report_config (
    id bigint NOT NULL,
    config_name character varying(100) NOT NULL,
    config_code character varying(50) NOT NULL,
    report_type character varying(20) NOT NULL,
    prompt_template text NOT NULL,
    model_name character varying(50) DEFAULT 'glm-4-flash'::character varying,
    model_config json,
    temperature double precision DEFAULT 0.7,
    max_tokens integer DEFAULT 4000,
    top_p double precision DEFAULT 0.9,
    max_retries integer DEFAULT 3,
    system_prompt text,
    status smallint DEFAULT 1,
    remark character varying(500),
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp without time zone
);

COMMENT ON TABLE public.ai_report_config IS 'AI报告配置表';
COMMENT ON COLUMN public.ai_report_config.id IS '主键ID';
COMMENT ON COLUMN public.ai_report_config.config_name IS '配置名称';
COMMENT ON COLUMN public.ai_report_config.config_code IS '配置编码';
COMMENT ON COLUMN public.ai_report_config.report_type IS '报告类型：daily日报，weekly周报';
COMMENT ON COLUMN public.ai_report_config.prompt_template IS '提示词模板';
COMMENT ON COLUMN public.ai_report_config.model_name IS '使用的模型名称';
COMMENT ON COLUMN public.ai_report_config.model_config IS '模型配置JSON（备用）';
COMMENT ON COLUMN public.ai_report_config.temperature IS '温度参数：控制输出随机性，0-1之间，越小越确定';
COMMENT ON COLUMN public.ai_report_config.max_tokens IS '最大token数：限制输出长度';
COMMENT ON COLUMN public.ai_report_config.top_p IS 'top_p参数：核采样参数，0-1之间';
COMMENT ON COLUMN public.ai_report_config.max_retries IS '最大重试次数';
COMMENT ON COLUMN public.ai_report_config.system_prompt IS 'System Prompt：AI的角色设定';
COMMENT ON COLUMN public.ai_report_config.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN public.ai_report_config.remark IS '备注';
COMMENT ON COLUMN public.ai_report_config.create_by IS '创建人';
COMMENT ON COLUMN public.ai_report_config.create_time IS '创建时间';
COMMENT ON COLUMN public.ai_report_config.update_by IS '更新人';
COMMENT ON COLUMN public.ai_report_config.update_time IS '更新时间';

-- AI报告生成记录表

CREATE TABLE public.ai_report (
    id bigint NOT NULL,
    report_title character varying(200) NOT NULL,
    report_type character varying(20) NOT NULL,
    report_date date,
    start_date date,
    end_date date,
    project_id bigint,
    project_name character varying(200),
    report_content text NOT NULL,
    config_id bigint,
    config_name character varying(100),
    model_name character varying(50),
    prompt_used text,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_by bigint,
    update_time timestamp without time zone
);

COMMENT ON TABLE public.ai_report IS 'AI报告生成记录表';
COMMENT ON COLUMN public.ai_report.id IS '主键ID';
COMMENT ON COLUMN public.ai_report.report_title IS '报告标题';
COMMENT ON COLUMN public.ai_report.report_type IS '报告类型：daily日报，weekly周报';
COMMENT ON COLUMN public.ai_report.report_date IS '报告日期（日报）';
COMMENT ON COLUMN public.ai_report.start_date IS '开始日期（周报）';
COMMENT ON COLUMN public.ai_report.end_date IS '结束日期（周报）';
COMMENT ON COLUMN public.ai_report.project_id IS '项目ID';
COMMENT ON COLUMN public.ai_report.project_name IS '项目名称';
COMMENT ON COLUMN public.ai_report.report_content IS '报告内容';
COMMENT ON COLUMN public.ai_report.config_id IS '使用的配置ID';
COMMENT ON COLUMN public.ai_report.config_name IS '使用的配置名称';
COMMENT ON COLUMN public.ai_report.model_name IS '使用的模型名称';
COMMENT ON COLUMN public.ai_report.prompt_used IS '实际使用的提示词';
COMMENT ON COLUMN public.ai_report.create_by IS '创建人';
COMMENT ON COLUMN public.ai_report.create_time IS '创建时间';
COMMENT ON COLUMN public.ai_report.update_by IS '更新人';
COMMENT ON COLUMN public.ai_report.update_time IS '更新时间';

-- 升级脚本：为现有数据库添加新字段
-- 给 ai_report_config 表添加新字段
ALTER TABLE public.ai_report_config ADD COLUMN IF NOT EXISTS model_config json;
ALTER TABLE public.ai_report_config ADD COLUMN IF NOT EXISTS temperature double precision DEFAULT 0.7;
ALTER TABLE public.ai_report_config ADD COLUMN IF NOT EXISTS max_tokens integer DEFAULT 4000;
ALTER TABLE public.ai_report_config ADD COLUMN IF NOT EXISTS top_p double precision DEFAULT 0.9;
ALTER TABLE public.ai_report_config ADD COLUMN IF NOT EXISTS max_retries integer DEFAULT 3;
ALTER TABLE public.ai_report_config ADD COLUMN IF NOT EXISTS system_prompt text;

-- 添加字段注释（如果字段已存在会跳过，不会报错）
COMMENT ON COLUMN public.ai_report_config.model_config IS '模型配置JSON（备用）';
COMMENT ON COLUMN public.ai_report_config.temperature IS '温度参数：控制输出随机性，0-1之间，越小越确定';
COMMENT ON COLUMN public.ai_report_config.max_tokens IS '最大token数：限制输出长度';
COMMENT ON COLUMN public.ai_report_config.top_p IS 'top_p参数：核采样参数，0-1之间';
COMMENT ON COLUMN public.ai_report_config.max_retries IS '最大重试次数';
COMMENT ON COLUMN public.ai_report_config.system_prompt IS 'System Prompt：AI的角色设定';

-- ============================================
-- 索引优化（必执行）
-- ============================================

-- pm_task 表索引优化

-- pm_project 表索引

-- pm_task_comment 表索引

-- pm_task_progress_update 表索引

-- pm_task_custom_row 表索引

-- pm_custom_table 表索引

-- ============================================
-- JSON 字段类型转换（建议执行）
-- ============================================
-- 将 VARCHAR 类型的 JSON 字符串转换为 PostgreSQL 原生 JSON 类型
-- 提高查询性能，支持 JSON 操作符

-- 注意：执行前请确认现有数据格式正确，建议先备份！

-- 转换 attachments 字段
DO $$ 
BEGIN
    -- 检查当前字段类型
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'pm_task' 
        AND column_name = 'attachments' 
        AND data_type = 'character varying'
    ) THEN
        RAISE NOTICE '正在转换 attachments 字段为 JSON 类型...';
        ALTER TABLE pm_task ALTER COLUMN attachments TYPE JSON USING attachments::json;
        RAISE NOTICE 'attachments 字段类型转换完成！';
    ELSE
        RAISE NOTICE 'attachments 字段已经是 JSON 类型或其他类型，跳过转换。';
    END IF;
END $$;

-- 转换 stakeholders 字段
DO $$ 
BEGIN
    -- 检查当前字段类型
    IF EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'pm_task' 
        AND column_name = 'stakeholders' 
        AND data_type = 'character varying'
    ) THEN
        RAISE NOTICE '正在转换 stakeholders 字段为 JSON 类型...';
        ALTER TABLE pm_task ALTER COLUMN stakeholders TYPE JSON USING stakeholders::json;
        RAISE NOTICE 'stakeholders 字段类型转换完成！';
    ELSE
        RAISE NOTICE 'stakeholders 字段已经是 JSON 类型或其他类型，跳过转换。';
    END IF;
END $$;

-- ============================================
-- 后续建议
-- ============================================
-- 1. 执行 ANALYZE 更新统计信息：
--    ANALYZE pm_task;
--    ANALYZE pm_project;
-- 2. 监控慢查询日志，确认性能提升
-- 3. 定期维护索引：
--    REINDEX INDEX CONCURRENTLY idx_pm_task_project_id;
--    (需要时逐个执行)
