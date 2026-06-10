-- ===============================================================
-- Hyper Duty 字典数据（终极修复版 — 不依赖 ON CONFLICT）
-- 适用：客户端工具 / 代理剥离 ON CONFLICT 关键字的场景
-- ===============================================================

-- 第 1 步：清理 id=10~32 范围的全部字典（保留 id=1,2,5 等业务字典）
DELETE FROM public.sys_dict_data WHERE dict_type_id BETWEEN 10 AND 32;
DELETE FROM public.sys_dict_type WHERE id BETWEEN 10 AND 32;

-- 第 2 步：直接插入字典类型与字典数据

-- 10. 通用状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (10, '通用状态', 'common_status', '通用启用/禁用', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (100, 10, '启用', '1', 1, '', 'primary', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (101, 10, '禁用', '0', 2, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 11. 任务状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (11, '任务状态', 'task_status', '项目管理-任务状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (110, 11, '未开始', '1', 1, '', 'info', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (111, 11, '进行中', '2', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (112, 11, '已完成', '3', 3, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (113, 11, '已暂停', '4', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (114, 11, '已取消', '5', 5, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 12. 任务优先级
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (12, '任务优先级', 'task_priority', '项目管理-任务优先级（1=高 2=中 3=低）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (120, 12, '高', '1', 1, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (121, 12, '中', '2', 2, '', 'warning', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (122, 12, '低', '3', 3, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 13. 审批状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (13, '审批状态', 'approval_status', '通用审批流-审批状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (130, 13, '待审批', '0', 1, '', 'info', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (131, 13, '已通过', '1', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (132, 13, '已驳回', '2', 3, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (133, 13, '已撤回', '3', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 14. 班次类型
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (14, '班次类型', 'shift_type', '值班管理-班次类型（0=白班 1=早班 2=中班 3=晚班 4=全天 5=夜班）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (140, 14, '白班', '0', 0, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (141, 14, '早班', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (142, 14, '中班', '2', 2, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (143, 14, '晚班', '3', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (144, 14, '全天', '4', 4, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (145, 14, '夜班', '5', 5, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 16. 性别（与后端 sys_employee.gender 对齐：0=未知 1=男 2=女）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (16, '性别', 'gender', '员工性别（0=未知 1=男 2=女）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (160, 16, '未知', '0', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (161, 16, '男', '1', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (162, 16, '女', '2', 3, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 17. 表单类型
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (17, '表单类型', 'form_type', '工作流-表单类型', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (170, 17, '系统内置', 'system', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (171, 17, '自定义表单', 'custom', 2, '', 'primary', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (172, 17, 'URL 表单', 'url', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 18. 流程部署状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (18, '流程部署状态', 'deploy_status', '工作流-流程部署状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (180, 18, '已部署', '1', 1, '', 'success', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (181, 18, '已停用', '0', 2, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 19. 项目状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (19, '项目状态', 'project_status', '项目管理-项目状态（1=未开始 2=进行中 3=已完成 4=已暂停 5=已归档）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (190, 19, '未开始', '1', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (191, 19, '进行中', '2', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (192, 19, '已完成', '3', 3, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (193, 19, '已暂停', '4', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (194, 19, '已归档', '5', 5, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 22. 积分事件类型
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (22, '积分事件类型', 'score_event_type', '积分管理-事件类型（1=加分 2=扣分）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (220, 22, '加分', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (221, 22, '扣分', '2', 2, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 23. 操作日志状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (23, '操作日志状态', 'operation_log_status', '系统管理-操作日志状态（1=成功 0=失败）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (230, 23, '成功', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (231, 23, '失败', '0', 2, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 24. 定时任务状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (24, '定时任务状态', 'job_status', '系统管理-定时任务状态（1=运行 0=暂停）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (240, 24, '运行', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (241, 24, '暂停', '0', 2, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 25. 评定季度
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (25, '评定季度', 'evaluation_quarter', '积分管理-评定季度（1..4）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (250, 25, '第一季度', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (251, 25, '第二季度', '2', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (252, 25, '第三季度', '3', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (253, 25, '第四季度', '4', 4, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 26. 是否（与 15 等价，保留兼容）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (26, '是否', 'yes_no', '通用是否（1=是 0=否）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (260, 26, '是', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (261, 26, '否', '0', 2, '', 'info', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 27. 请假类型
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (27, '请假类型', 'leave_type', '值班管理-请假类型（1=事假 2=病假 3=年假 4=调休 5=其他）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (270, 27, '事假', '1', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (271, 27, '病假', '2', 2, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (272, 27, '年假', '3', 3, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (273, 27, '调休', '4', 4, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (274, 27, '其他', '5', 5, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 28. 排班模式
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (28, '排班模式', 'schedule_mode', '值班管理-排班模式（1=轮换 2=固定 3=自定义）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (280, 28, '轮换排班', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (281, 28, '固定排班', '2', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (282, 28, '自定义', '3', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 29. 菜单类型
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (29, '菜单类型', 'menu_type', '系统管理-菜单类型（0=顶级 1=目录 2=菜单 3=按钮）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (290, 29, '顶级菜单', '0', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (291, 29, '目录', '1', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (292, 29, '菜单', '2', 3, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (293, 29, '按钮', '3', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 31. 报告类型
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (31, '报告类型', 'report_type', 'AI模块-报告类型（daily=日报 weekly=周报）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (310, 31, '日报', 'daily', 1, '', 'primary', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (311, 31, '周报', 'weekly', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 32. 任务重点
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) VALUES (32, '任务重点', 'task_focus', '项目管理-任务是否重点（1=重点 0=普通）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (320, 32, '重点', '1', 1, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) VALUES (321, 32, '普通', '0', 2, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
