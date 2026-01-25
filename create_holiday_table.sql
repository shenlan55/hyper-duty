-- 创建节假日表
CREATE TABLE IF NOT EXISTS duty_holiday (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '节假日ID',
    holiday_name VARCHAR(50) NOT NULL COMMENT '节假日名称',
    holiday_date DATE NOT NULL COMMENT '节假日日期',
    is_workday TINYINT DEFAULT 0 COMMENT '是否调休上班:0-否,1-是',
    holiday_type TINYINT DEFAULT 1 COMMENT '节假日类型:1-法定假日,2-调休,3-公司假日',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_holiday_date (holiday_date),
    INDEX idx_holiday_date (holiday_date),
    INDEX idx_is_workday (is_workday)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='节假日表';

-- 插入一些测试数据
INSERT INTO duty_holiday (holiday_name, holiday_date, is_workday, holiday_type, remark)
VALUES 
('元旦', '2024-01-01', 0, 1, '2024年元旦'),
('春节', '2024-02-10', 0, 1, '2024年春节'),
('春节', '2024-02-11', 0, 1, '2024年春节'),
('春节', '2024-02-12', 0, 1, '2024年春节'),
('春节', '2024-02-13', 0, 1, '2024年春节'),
('春节', '2024-02-14', 0, 1, '2024年春节'),
('清明节', '2024-04-04', 0, 1, '2024年清明节'),
('劳动节', '2024-05-01', 0, 1, '2024年劳动节'),
('端午节', '2024-06-10', 0, 1, '2024年端午节'),
('中秋节', '2024-09-17', 0, 1, '2024年中秋节'),
('国庆节', '2024-10-01', 0, 1, '2024年国庆节'),
('国庆节', '2024-10-02', 0, 1, '2024年国庆节'),
('国庆节', '2024-10-03', 0, 1, '2024年国庆节'),
('国庆节', '2024-10-04', 0, 1, '2024年国庆节'),
('国庆节', '2024-10-05', 0, 1, '2024年国庆节'),
('国庆节', '2024-10-06', 0, 1, '2024年国庆节'),
('国庆节', '2024-10-07', 0, 1, '2024年国庆节');
