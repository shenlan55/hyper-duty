-- 添加父任务和子任务相关字段
ALTER TABLE pm_task ADD COLUMN parent_id BIGINT DEFAULT 0;
ALTER TABLE pm_task ADD COLUMN task_level INTEGER DEFAULT 1;
ALTER TABLE pm_task ADD COLUMN progress INTEGER DEFAULT 0;
ALTER TABLE pm_task ADD COLUMN is_pinned INTEGER DEFAULT 0;
ALTER TABLE pm_task ADD COLUMN module_id BIGINT DEFAULT NULL;

-- 添加索引
CREATE INDEX idx_parent_id ON pm_task (parent_id);
CREATE INDEX idx_task_level ON pm_task (task_level);
CREATE INDEX idx_is_pinned ON pm_task (is_pinned);

-- 更新现有数据的默认值
UPDATE pm_task SET parent_id = 0 WHERE parent_id IS NULL;
UPDATE pm_task SET task_level = 1 WHERE task_level IS NULL;
UPDATE pm_task SET progress = 0 WHERE progress IS NULL;
UPDATE pm_task SET is_pinned = 0 WHERE is_pinned IS NULL;