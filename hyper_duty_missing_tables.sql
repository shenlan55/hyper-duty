-- ============================================
-- Hyper Duty 系统数据库优化脚本
-- 版本: 2.0 (PostgreSQL)
-- 日期: 2026-04-26
-- ============================================

-- ============================================
-- 第一部分：索引优化（必执行）
-- ============================================

-- pm_task 表索引优化
CREATE INDEX IF NOT EXISTS idx_pm_task_project_id ON pm_task(project_id);
CREATE INDEX IF NOT EXISTS idx_pm_task_assignee_id ON pm_task(assignee_id);
CREATE INDEX IF NOT EXISTS idx_pm_task_status ON pm_task(status);
CREATE INDEX IF NOT EXISTS idx_pm_task_priority ON pm_task(priority);
CREATE INDEX IF NOT EXISTS idx_pm_task_parent_id ON pm_task(parent_id);
CREATE INDEX IF NOT EXISTS idx_pm_task_create_time ON pm_task(create_time);
CREATE INDEX IF NOT EXISTS idx_pm_task_is_pinned ON pm_task(is_pinned);
CREATE INDEX IF NOT EXISTS idx_pm_task_project_status ON pm_task(project_id, status);
CREATE INDEX IF NOT EXISTS idx_pm_task_assignee_status ON pm_task(assignee_id, status);

-- pm_project 表索引
CREATE INDEX IF NOT EXISTS idx_pm_project_owner_id ON pm_project(owner_id);
CREATE INDEX IF NOT EXISTS idx_pm_project_status ON pm_project(status);
CREATE INDEX IF NOT EXISTS idx_pm_project_create_time ON pm_project(create_time);

-- pm_task_comment 表索引
CREATE INDEX IF NOT EXISTS idx_pm_task_comment_task_id ON pm_task_comment(task_id);
CREATE INDEX IF NOT EXISTS idx_pm_task_comment_employee_id ON pm_task_comment(employee_id);

-- pm_task_progress_update 表索引
CREATE INDEX IF NOT EXISTS idx_pm_task_progress_update_task_id ON pm_task_progress_update(task_id);
CREATE INDEX IF NOT EXISTS idx_pm_task_progress_update_create_time ON pm_task_progress_update(create_time DESC);

-- pm_task_custom_row 表索引
CREATE INDEX IF NOT EXISTS idx_pm_task_custom_row_task_id ON pm_task_custom_row(task_id);
CREATE INDEX IF NOT EXISTS idx_pm_task_custom_row_table_id ON pm_task_custom_row(table_id);

-- pm_custom_table 表索引
CREATE INDEX IF NOT EXISTS idx_pm_custom_table_create_by ON pm_custom_table(create_by);

-- ============================================
-- 第二部分：JSON 字段类型转换（建议执行）
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
-- 第三部分：索引使用情况检查（可选，执行用于验证）
-- ============================================

-- 检查表大小和索引情况
SELECT 
    schemaname,
    tablename,
    indexname,
    pg_size_pretty(pg_relation_size(schemaname||'.'||indexname)) as index_size
FROM pg_indexes 
WHERE tablename IN ('pm_task', 'pm_project', 'pm_task_comment', 'pm_task_progress_update')
ORDER BY tablename, indexname;

-- ============================================
-- 第四部分：可选扩展表（根据需求选择）
-- ============================================
-- 如果需要更好的数据管理，可以创建以下独立表
-- 注意：创建后需要迁移现有数据！

-- ============================================
-- 可选 4.1：任务附件表
-- ============================================
-- CREATE TABLE IF NOT EXISTS pm_task_attachment (
--     id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
--     task_id BIGINT NOT NULL,
--     file_name VARCHAR(255) NOT NULL,
--     file_url VARCHAR(500) NOT NULL,
--     preview_url VARCHAR(500),
--     file_size BIGINT,
--     file_type VARCHAR(100),
--     created_by BIGINT,
--     created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     CONSTRAINT fk_pm_task_attachment_task FOREIGN KEY (task_id) REFERENCES pm_task(id) ON DELETE CASCADE
-- );
-- 
-- CREATE INDEX IF NOT EXISTS idx_pm_task_attachment_task_id ON pm_task_attachment(task_id);
-- CREATE INDEX IF NOT EXISTS idx_pm_task_attachment_created_time ON pm_task_attachment(created_time DESC);

-- ============================================
-- 可选 4.2：任务干系人表
-- ============================================
-- CREATE TABLE IF NOT EXISTS pm_task_stakeholder (
--     id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
--     task_id BIGINT NOT NULL,
--     employee_id BIGINT NOT NULL,
--     created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     CONSTRAINT fk_pm_task_stakeholder_task FOREIGN KEY (task_id) REFERENCES pm_task(id) ON DELETE CASCADE,
--     CONSTRAINT fk_pm_task_stakeholder_employee FOREIGN KEY (employee_id) REFERENCES sys_employee(id) ON DELETE CASCADE
-- );
-- 
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_pm_task_stakeholder_task_employee ON pm_task_stakeholder(task_id, employee_id);
-- CREATE INDEX IF NOT EXISTS idx_pm_task_stakeholder_employee_id ON pm_task_stakeholder(employee_id);

-- ============================================
-- 第五部分：优化完成提示
-- ============================================

SELECT '数据库优化脚本执行完成！' as message;
SELECT '第一部分（索引优化）已执行' as part1;
SELECT '第二部分（JSON转换）已执行' as part2;
SELECT '请检查上面的索引列表确认创建成功' as tip;

-- ============================================
-- 后续建议
-- ============================================
-- 1. 执行 ANALYZE 更新统计信息：
--    ANALYZE pm_task;
--    ANALYZE pm_project;
--
-- 2. 监控慢查询日志，确认性能提升
--
-- 3. 定期维护索引：
--    REINDEX INDEX CONCURRENTLY idx_pm_task_project_id;
--    (需要时逐个执行)
