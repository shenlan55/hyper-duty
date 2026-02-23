-- 修复sys_schedule_log表结构，使其与MySQL dump文件和实体类一致

-- 1. 删除多余的字段
ALTER TABLE sys_schedule_log DROP COLUMN IF EXISTS invoke_target;
ALTER TABLE sys_schedule_log DROP COLUMN IF EXISTS job_message;

-- 2. 添加缺少的字段
ALTER TABLE sys_schedule_log ADD COLUMN job_id bigint NOT NULL;
ALTER TABLE sys_schedule_log ADD COLUMN job_code character varying(100) NOT NULL;
ALTER TABLE sys_schedule_log ADD COLUMN params text;
ALTER TABLE sys_schedule_log ADD COLUMN execute_time bigint;

-- 3. 创建索引
CREATE INDEX IF NOT EXISTS idx_job_id ON sys_schedule_log(job_id);
CREATE INDEX IF NOT EXISTS idx_job_code ON sys_schedule_log(job_code);
CREATE INDEX IF NOT EXISTS idx_status ON sys_schedule_log(status);
CREATE INDEX IF NOT EXISTS idx_create_time ON sys_schedule_log(create_time);

-- 4. 修改字段类型（如果需要）
-- PostgreSQL的SMALLINT对应MySQL的tinyint，这里不需要修改

-- 5. 添加注释
COMMENT ON TABLE sys_schedule_log IS '定时任务日志表';
COMMENT ON COLUMN sys_schedule_log.id IS '日志ID';
COMMENT ON COLUMN sys_schedule_log.job_id IS '任务ID';
COMMENT ON COLUMN sys_schedule_log.job_name IS '任务名称';
COMMENT ON COLUMN sys_schedule_log.job_group IS '任务分组';
COMMENT ON COLUMN sys_schedule_log.job_code IS '任务编码';
COMMENT ON COLUMN sys_schedule_log.params IS '参数';
COMMENT ON COLUMN sys_schedule_log.status IS '执行状态:0-失败,1-成功';
COMMENT ON COLUMN sys_schedule_log.error_msg IS '错误信息';
COMMENT ON COLUMN sys_schedule_log.execute_time IS '执行时间(毫秒)';
COMMENT ON COLUMN sys_schedule_log.start_time IS '开始时间';
COMMENT ON COLUMN sys_schedule_log.end_time IS '结束时间';
COMMENT ON COLUMN sys_schedule_log.create_time IS '创建时间';
