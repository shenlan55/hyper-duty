-- 为值班表添加排序字段
ALTER TABLE duty_schedule ADD COLUMN sort_order INT DEFAULT 0;

-- 为排序字段添加注释
COMMENT ON COLUMN duty_schedule.sort_order IS '排序';

-- 更新现有数据的排序字段为0
UPDATE duty_schedule SET sort_order = 0 WHERE sort_order IS NULL;