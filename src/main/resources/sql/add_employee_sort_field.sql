-- =============================================
-- 为 sys_employee 表添加排序字段
-- 生成时间：2026-02-23
-- =============================================

-- 添加 sort 字段
ALTER TABLE sys_employee ADD COLUMN IF NOT EXISTS sort INTEGER DEFAULT 0;

-- 为现有数据设置默认排序值
UPDATE sys_employee SET sort = id WHERE sort IS NULL OR sort = 0;

-- 创建索引以提高排序查询性能
CREATE INDEX IF NOT EXISTS idx_employee_sort ON sys_employee(sort);

-- 验证字段添加成功
SELECT column_name, data_type, column_default
FROM information_schema.columns
WHERE table_name = 'sys_employee' AND column_name = 'sort';

-- 查看当前数据
SELECT id, employee_name, sort FROM sys_employee ORDER BY sort;
