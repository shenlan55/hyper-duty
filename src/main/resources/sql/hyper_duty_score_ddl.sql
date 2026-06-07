-- ===============================================================
-- Hyper Duty 员工积分管理模块表结构（PostgreSQL 语法）
-- ===============================================================

-- 积分事件定义表
CREATE TABLE IF NOT EXISTS public.score_event (
    id BIGSERIAL PRIMARY KEY,
    event_name VARCHAR(100) NOT NULL,
    event_type SMALLINT NOT NULL DEFAULT 1,
    default_score INTEGER NOT NULL DEFAULT 0,
    category VARCHAR(50),
    status SMALLINT DEFAULT 1,
    sort INTEGER DEFAULT 0,
    remark VARCHAR(500),
    create_time TIMESTAMP,
    update_time TIMESTAMP
);
COMMENT ON TABLE public.score_event IS '积分事件定义表';
COMMENT ON COLUMN public.score_event.id IS '主键ID';
COMMENT ON COLUMN public.score_event.event_name IS '事件名称';
COMMENT ON COLUMN public.score_event.event_type IS '事件类型：1=加分，2=扣分';
COMMENT ON COLUMN public.score_event.default_score IS '默认分值（加分正数，扣分存绝对值）';
COMMENT ON COLUMN public.score_event.category IS '分类：赋能提效/培训分享/抽检等';
COMMENT ON COLUMN public.score_event.status IS '状态：0=禁用，1=启用';
COMMENT ON COLUMN public.score_event.sort IS '排序';
COMMENT ON COLUMN public.score_event.remark IS '备注';
COMMENT ON COLUMN public.score_event.create_time IS '创建时间';
COMMENT ON COLUMN public.score_event.update_time IS '更新时间';

-- 积分记录表
CREATE TABLE IF NOT EXISTS public.score_record (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    score INTEGER NOT NULL DEFAULT 0,
    record_date DATE NOT NULL,
    description TEXT,
    create_by BIGINT,
    period_year INTEGER NOT NULL,
    period_month INTEGER NOT NULL,
    create_time TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_score_record_employee ON public.score_record(employee_id);
CREATE INDEX IF NOT EXISTS idx_score_record_period ON public.score_record(period_year, period_month);
COMMENT ON TABLE public.score_record IS '积分记录表';
COMMENT ON COLUMN public.score_record.id IS '主键ID';
COMMENT ON COLUMN public.score_record.employee_id IS '员工ID，关联 sys_employee';
COMMENT ON COLUMN public.score_record.event_id IS '积分事件ID，关联 score_event';
COMMENT ON COLUMN public.score_record.score IS '实际分值（正数=加分，负数=扣分）';
COMMENT ON COLUMN public.score_record.record_date IS '记录日期';
COMMENT ON COLUMN public.score_record.description IS '详细描述';
COMMENT ON COLUMN public.score_record.create_by IS '录入人ID';
COMMENT ON COLUMN public.score_record.period_year IS '所属年份';
COMMENT ON COLUMN public.score_record.period_month IS '所属月份';
COMMENT ON COLUMN public.score_record.create_time IS '创建时间';

-- 周期配置表
CREATE TABLE IF NOT EXISTS public.score_period_config (
    id BIGSERIAL PRIMARY KEY,
    period_type VARCHAR(20) NOT NULL,
    period_year INTEGER NOT NULL,
    period_index INTEGER NOT NULL DEFAULT 1,
    point_weight DECIMAL(5,2) DEFAULT 0.60,
    hour_weight DECIMAL(5,2) DEFAULT 0.40,
    status SMALLINT DEFAULT 0,
    create_time TIMESTAMP,
    update_time TIMESTAMP
);
COMMENT ON TABLE public.score_period_config IS '评选周期配置表';
COMMENT ON COLUMN public.score_period_config.id IS '主键ID';
COMMENT ON COLUMN public.score_period_config.period_type IS '周期类型：QUARTERLY=季度，YEARLY=年度';
COMMENT ON COLUMN public.score_period_config.period_year IS '年份';
COMMENT ON COLUMN public.score_period_config.period_index IS '周期序号：季度(1-4)，年度固定1';
COMMENT ON COLUMN public.score_period_config.point_weight IS '积分权重（默认0.6）';
COMMENT ON COLUMN public.score_period_config.hour_weight IS '工时权重（默认0.4）';
COMMENT ON COLUMN public.score_period_config.status IS '状态：0=未开始，1=进行中，2=已结束';
COMMENT ON COLUMN public.score_period_config.create_time IS '创建时间';
COMMENT ON COLUMN public.score_period_config.update_time IS '更新时间';

-- 月度汇总表
CREATE TABLE IF NOT EXISTS public.score_summary (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    period_year INTEGER NOT NULL,
    period_month INTEGER NOT NULL,
    total_score INTEGER DEFAULT 0,
    work_hours DECIMAL(10,2) DEFAULT 0.00,
    comprehensive_score DECIMAL(10,2) DEFAULT 0.00,
    create_time TIMESTAMP,
    CONSTRAINT uk_employee_month UNIQUE (employee_id, period_year, period_month)
);
CREATE INDEX IF NOT EXISTS idx_score_summary_period ON public.score_summary(period_year, period_month);
COMMENT ON TABLE public.score_summary IS '积分月度汇总表';
COMMENT ON COLUMN public.score_summary.id IS '主键ID';
COMMENT ON COLUMN public.score_summary.employee_id IS '员工ID';
COMMENT ON COLUMN public.score_summary.period_year IS '年份';
COMMENT ON COLUMN public.score_summary.period_month IS '月份';
COMMENT ON COLUMN public.score_summary.total_score IS '当月积分合计';
COMMENT ON COLUMN public.score_summary.work_hours IS '当月加班工时（从 duty_record 汇总）';
COMMENT ON COLUMN public.score_summary.comprehensive_score IS '综合评分';
COMMENT ON COLUMN public.score_summary.create_time IS '创建时间';