-- ===============================================================
-- Hyper Duty 系统模块初始数据（PostgreSQL 语法）
-- ===============================================================

INSERT INTO public.sys_dept VALUES (4, '财务部', 1, '004', 3, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (6, '中北事业群', 1, '005', 4, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (8, '江苏移动SRE', 7, '007', 1, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (9, 'GOC调度组', 8, '008', 1, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (10, '架构入网组', 8, '009', 2, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (11, '变更管控组', 8, '010', 3, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (12, '故障治理组', 8, '011', 4, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (13, '运营治理组', 8, '012', 5, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (14, '例行工作组', 8, '013', 6, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (3, '人事部', 1, '003', 2, 1, '2026-02-22 22:36:42.24086', '2026-02-22 22:36:42.24086');
INSERT INTO public.sys_dept VALUES (7, '交付二部', 6, '006', 1, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');
INSERT INTO public.sys_dept VALUES (1, '新炬', 0, '001', 1, 1, '2026-02-22 23:21:27.685729', '2026-02-22 23:21:27.685729');

INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (2, 1, '项目经理B', 'ProjectManagerB', 1, '', '', 0, 1, '', '2026-01-18 06:59:18', '2026-01-18 06:59:18');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (4, 1, '组长B', 'TeamLeaderB', 2, '', '', 0, 1, '', '2026-01-18 07:29:07', '2026-01-18 07:29:07');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (5, 1, '组员', 'TeamMember', 3, '', '', 0, 1, '', '2026-01-18 07:30:09', '2026-01-18 07:30:09');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (1, 1, '项目经理A', 'ProjectManagerA', 1, '', '', 0, 1, '', '2026-01-18 06:58:31', '2026-01-18 06:58:31');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (3, 1, '组长A', 'TeamLeaderA', 2, '', '', 0, 1, '', '2026-01-18 07:19:37', '2026-01-18 07:19:37');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (6, 2, '例行工作', '1', 1, '', '', 0, 1, '', '2026-02-20 11:27:16', '2026-02-20 11:27:16');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (27, 2, 'GOC调度', '2', 2, '', '', 0, 1, '', '2026-02-24 20:21:48', '2026-02-24 20:21:48');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (23, 2, '架构治理', '3', 3, '', '', 0, 1, '', '2026-02-24 20:21:08', '2026-02-24 20:21:08');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (24, 2, '入网管控', '4', 4, '', '', 0, 1, '', '2026-02-24 20:21:22', '2026-02-24 20:21:22');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (25, 2, '变更管控', '5', 5, '', '', 0, 1, '', '2026-02-24 20:21:33', '2026-02-24 20:21:33');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (28, 2, '故障治理', '6', 6, '', '', 0, 1, '', '2026-02-24 20:22:42.857598', '2026-02-24 20:22:42.857598');
INSERT INTO public.sys_dict_data ON CONFLICT (id) DO NOTHING VALUES (26, 2, '运营治理', '7', 7, '', '', 0, 1, '', '2026-02-24 20:21:40', '2026-02-24 20:21:40');

INSERT INTO public.sys_dict_type ON CONFLICT (id) DO NOTHING VALUES (2, '事项类型', 'ItemType', '', 1, '2026-02-20 11:26:29', '2026-02-20 11:26:29');
INSERT INTO public.sys_dict_type ON CONFLICT (id) DO NOTHING VALUES (1, '职位--江苏移动统维', 'Position', '', 1, '2026-01-18 06:51:26', '2026-01-18 06:51:26');

INSERT INTO public.sys_employee VALUES (1, '管理员', 1, 'EMP001', '13800138000', 'admin@example.com', 1, NULL, NULL, 1, 'admin', '$2a$10$cVw7wDpG1k6uluAFjKuROelCmanmBO/gynn.uv0/Clz4RrXEC76AS', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 1);
INSERT INTO public.sys_employee VALUES (13, '杜志强', 8, 'EMP007', '18351868973', 'duzhiqiang@shsnc.com', 1, NULL, NULL, 1, 'duzhiqiang', '$2a$10$omKfvUW4m.rvOSfBeIMBgO8MQnji0we9pWiRraE5xiSlzf/kKBUuS', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 13);
INSERT INTO public.sys_employee VALUES (14, '刘成军', 8, 'EMP008', '13815444756', 'liuchengjun@shsnc.com', 1, NULL, NULL, 1, 'liuchengjun', '$2a$10$8W/LyURtu9TFnAABwoZXYO/uTr7evJLquMw1Qiy2rbcMtFi..ulru', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 14);
INSERT INTO public.sys_employee VALUES (15, '沈建明', 8, 'EMP009', '17805139774', 'shenjianming@shsnc.com', 1, NULL, NULL, 1, 'shenjianming', '$2a$10$Zs3yNLZoA.aw/MlDjCHNme8TzWCoDXmux.HY3OGM4zYHxvLCW7xKq', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 15);
INSERT INTO public.sys_employee VALUES (16, '邢文骏', 8, 'EMP010', '18851664875', 'xingwenjun@shsnc.com', 1, NULL, NULL, 1, 'xingwenjun', '$2a$10$4NMUkJUqum5VgjGKc0qy3O3S7tc2PuWhdAW5JWIF7jWgUC3nGJH4C', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 16);
INSERT INTO public.sys_employee VALUES (17, '刘润泽', 8, 'EMP011', '15251859476', 'liurunze@shsnc.com', 1, NULL, NULL, 1, 'liurunze', '$2a$10$H6P9FjqA9cV5.T6qBBH27.1IzathR67mjip2PIH3PmhrE.noNreQ6', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 17);
INSERT INTO public.sys_employee VALUES (18, '周柏霖', 8, 'EMP012', '18251816392', 'zhoubolin@shsnc.com', 1, NULL, NULL, 1, 'zhoubolin', '$2a$10$3dKjtY3/ORomNE9HlcOjvudZ/Zir8SagPDMdCSQqLDNX.f7a6qB/S', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 18);
INSERT INTO public.sys_employee VALUES (19, '张华健', 8, 'EMP013', '', '', 1, NULL, NULL, 1, 'zhanghuajian', '$2a$10$/ImJs.ho2LjKtuI698ORwu9iVJP06YNA2EuACiWCJhB7pNFDPnFlW', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 19);
INSERT INTO public.sys_employee VALUES (20, '涂宏', 8, 'EMP014', '13851450117', 'tuhong_nj@shsnc.com', 1, NULL, NULL, 1, 'tuhong', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 20);
INSERT INTO public.sys_employee VALUES (12, '王文治', 8, 'EMP006', '18812030630', 'wangwenzhi_bj@shsnc.com', 1, NULL, NULL, 1, 'wangwenzhi', '$2a$10$xm5zpBH.OWW/jkhvcowQe.P7w/e4BQv8661.6PRtETWp8U.PuppV6', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 12);
INSERT INTO public.sys_employee VALUES (10, '王国豪', 8, 'EMP005', '18551816535', 'wangguohao@shsnc.com', 1, NULL, NULL, 1, 'wangguohao', '$2a$10$1vMTy68DKbaB9XaQjvk5t.YLNslm88vbJkL8rnVo2jiDry6lmK1oW', '2026-02-22 23:21:27.70105', '2026-02-22 23:21:27.70105', 2);

INSERT INTO public.sys_menu VALUES (2, '系统管理', 0, '', '', '', 1, 'Setting', 2, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (3, '部门管理', 2, '/dept', 'views/Dept.vue', 'sys:dept:list', 2, 'OfficeBuilding', 1, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (4, '人员管理', 2, '/employee', 'views/Employee.vue', 'sys:employee:list', 2, 'User', 2, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (6, '菜单管理', 2, '/menu', 'views/Menu.vue', 'sys:menu:list', 2, 'Menu', 4, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (7, '角色管理', 2, '/role', 'views/Role.vue', 'sys:role:list', 2, 'DocumentCopy', 5, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (8, '值班管理', 0, '', '', '', 1, 'Calendar', 3, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (9, '值班表管理', 8, '/duty/schedule', 'views/duty/DutySchedule.vue', 'duty:schedule:list', 2, 'DocumentCopy', 2, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (10, '值班安排', 8, '/duty/assignment', 'views/duty/DutyAssignment.vue', 'duty:assignment:list', 2, 'Calendar', 1, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (11, '加班记录', 8, '/duty/record', 'views/duty/DutyRecord.vue', 'duty:record:list', 2, 'Document', 9, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (12, '班次配置', 8, '/duty/shift-config', 'views/duty/ShiftConfig.vue', 'duty:shift:list', 2, 'Clock', 3, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (13, '请假申请', 8, '/duty/leave-request', 'views/duty/LeaveRequest.vue', 'duty:leave:list', 2, 'Document', 5, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (14, '请假审批', 8, '/duty/leave-approval', 'views/duty/LeaveApproval.vue', 'duty:leave:approve', 2, 'CircleCheck', 6, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (15, '调班管理', 8, '/duty/swap-request', 'views/duty/SwapRequest.vue', 'duty:swap:list', 2, 'Refresh', 7, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (16, '排班统计', 8, '/duty/statistics', 'views/duty/Statistics.vue', 'duty:statistics:view', 2, 'DataAnalysis', 8, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (17, '操作日志', 2, '/system/operation-log', 'views/system/OperationLog.vue', 'sys:log:list', 2, 'Document', 7, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (20, '定时任务', 2, '/system/schedule-job', 'views/system/ScheduleJob.vue', 'sys:schedule:job:list', 2, 'Timer', 8, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (24, '排班模式管理', 8, '/duty/schedule-mode', 'views/duty/ScheduleMode.vue', 'duty:schedule:mode:list', 2, 'Operation', 4, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (25, '字典管理', 2, '/dict', 'views/Dict.vue', 'sys:dict:list', 2, 'List', 6, 1, '2026-02-22 23:21:27.996174', '2026-02-22 23:21:27.996174');
INSERT INTO public.sys_menu VALUES (5, '用户管理--已合并到人员管理', 4, '/system/user', 'views/User.vue', 'sys:user:list', 2, 'Avatar', 3, 0, '2026-02-22 23:21:27', '2026-02-22 23:21:27');
INSERT INTO public.sys_menu VALUES (26, '项目管理', 0, '', '', '', 1, 'Briefcase', 4, 1, '2026-02-23 00:26:25.673681', '2026-02-23 00:26:25.673681');
INSERT INTO public.sys_menu VALUES (27, '项目列表', 26, '/project/list', 'views/project/ProjectList.vue', 'project:list', 2, 'List', 1, 1, '2026-02-23 00:26:25.681238', '2026-02-23 00:26:25.681238');
INSERT INTO public.sys_menu VALUES (28, '项目详情', 26, '/project/detail', 'views/project/ProjectDetail.vue', 'project:detail', 2, 'View', 2, 1, '2026-02-23 00:26:25.681238', '2026-02-23 00:26:25.681238');
INSERT INTO public.sys_menu VALUES (29, '任务管理', 26, '/project/task', 'views/project/TaskList.vue', 'project:task:list', 2, 'Check', 3, 1, '2026-02-23 00:26:25.681238', '2026-02-23 00:26:25.681238');
INSERT INTO public.sys_menu VALUES (30, '项目统计', 26, '/project/statistics', 'views/project/ProjectGantt.vue', 'project:statistics', 2, 'DataAnalysis', 4, 1, '2026-02-23 00:26:25.681238', '2026-02-23 00:26:25.681238');
INSERT INTO public.sys_menu VALUES (31, '我的任务', 26, '/project/my-task', 'views/project/MyTask.vue', 'project:myTask:list', 2, 'User', 5, 1, '2026-02-24 20:30:00', '2026-02-24 20:30:00');
INSERT INTO public.sys_menu VALUES (32, '甘特图', 26, '/project/gantt', 'views/project/ProjectGantt.vue', 'project:gantt:view', 2, 'DataAnalysis', 6, 1, '2026-02-24 20:30:00', '2026-02-24 20:30:00');
INSERT INTO public.sys_menu VALUES (33, '日历视图', 26, '/project/calendar', 'views/project/TaskCalendar.vue', 'project:calendar:view', 2, 'Calendar', 7, 1, '2026-02-24 20:30:00', '2026-02-24 20:30:00');
INSERT INTO public.sys_menu VALUES (34, '团队视图', 26, '/project/team', 'views/project/TeamView.vue', 'project:team:view', 2, 'UserFilled', 8, 1, '2026-02-24 20:30:00', '2026-02-24 20:30:00');
INSERT INTO public.sys_menu VALUES (36, '自定义表格', 26, '/project/custom-table', 'views/project/CustomTable.vue', 'project:custom:table', 2, 'Document', 9, 1, '2026-04-05 00:00:00', '2026-04-05 00:00:00');
INSERT INTO public.sys_menu VALUES (37, '工作量查询', 26, '/project/workload-query', 'views/project/WorkloadQuery.vue', 'project:workload:query', 2, 'DataLine', 10, 1, '2026-04-07 00:00:00', '2026-04-07 00:00:00');
INSERT INTO public.sys_menu VALUES (38, '邮件配置', 2, '/system/mail-config', 'views/system/MailConfig.vue', 'sys:mail:config', 2, 'Message', 9, 1, '2026-04-18 00:00:00', '2026-04-18 00:00:00');
INSERT INTO public.sys_menu VALUES (1, '首页', 0, '/dashboard', 'views/Dashboard.vue', '', 1, 'HomeFilled', 1, 1, '2026-02-22 23:21:27', '2026-02-22 23:21:27');
INSERT INTO public.sys_menu (id, menu_name, parent_id, path, component, perms, menu_type, icon, order_num, status, create_time, update_time)

INSERT INTO public.sys_role VALUES (2, '普通用户', 'ROLE_USER', '拥有基础操作权限', 1, '2026-02-22 23:21:27', '2026-02-22 23:21:27', 2);
INSERT INTO public.sys_role VALUES (1, '超级管理员', 'ROLE_ADMIN', '拥有系统所有权限', 1, '2026-02-22 23:21:27', '2026-02-22 23:21:27', 1);
INSERT INTO public.sys_role VALUES (14, '值班管理员', 'ROLE_DUTYADMIN', '值班管理菜单所有权', 1, '2026-02-24 10:27:56', '2026-02-24 10:27:56', 3);
INSERT INTO public.sys_role VALUES (15, '项目管理员', 'ROLE_PMADMIN', '项目管理模块所有权限', 1, '2026-03-05 00:00:00', '2026-03-05 00:00:00', 4);

INSERT INTO public.sys_role_menu VALUES (561, 2, 1, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (562, 2, 4, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (563, 2, 5, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (564, 2, 25, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (565, 2, 10, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (566, 2, 9, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (567, 2, 13, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (568, 2, 14, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (569, 2, 15, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (570, 2, 11, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (571, 2, 26, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (572, 2, 27, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (573, 2, 28, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (574, 2, 29, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (575, 2, 30, '2026-02-24 10:28:29.928216');
INSERT INTO public.sys_role_menu VALUES (599, 2, 31, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (600, 2, 32, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (601, 2, 33, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (602, 2, 34, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (622, 2, 36, '2026-04-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (627, 2, 37, '2026-04-07 00:00:00');
INSERT INTO public.sys_role_menu VALUES (576, 14, 1, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (577, 14, 4, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (578, 14, 5, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (579, 14, 25, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (580, 14, 8, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (581, 14, 10, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (582, 14, 9, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (583, 14, 12, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (584, 14, 24, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (585, 14, 13, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (603, 15, 1, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (604, 15, 4, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (605, 15, 5, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (606, 15, 25, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (607, 15, 10, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (608, 15, 9, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (609, 15, 13, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (610, 15, 14, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (611, 15, 15, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (612, 15, 11, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (613, 15, 26, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (614, 15, 27, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (615, 15, 28, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (616, 15, 29, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (617, 15, 30, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (618, 15, 31, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (619, 15, 32, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (620, 15, 33, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (621, 15, 34, '2026-03-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (624, 15, 36, '2026-04-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (626, 15, 37, '2026-04-07 00:00:00');
INSERT INTO public.sys_role_menu VALUES (486, 1, 1, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (487, 1, 2, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (488, 1, 3, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (489, 1, 4, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (490, 1, 5, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (491, 1, 6, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (492, 1, 7, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (493, 1, 25, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (494, 1, 17, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (495, 1, 20, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (496, 1, 8, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (497, 1, 10, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (498, 1, 9, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (499, 1, 12, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (500, 1, 24, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (501, 1, 13, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (502, 1, 14, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (503, 1, 15, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (504, 1, 16, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (505, 1, 11, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (506, 1, 26, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (507, 1, 27, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (508, 1, 28, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (509, 1, 29, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (510, 1, 30, '2026-02-23 00:26:58.775378');
INSERT INTO public.sys_role_menu VALUES (595, 1, 31, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (596, 1, 32, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (597, 1, 33, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (598, 1, 34, '2026-02-24 20:30:00');
INSERT INTO public.sys_role_menu VALUES (623, 1, 36, '2026-04-05 00:00:00');
INSERT INTO public.sys_role_menu VALUES (625, 1, 37, '2026-04-07 00:00:00');
INSERT INTO public.sys_role_menu VALUES (630, 1, 38, '2026-04-18 00:00:00');
INSERT INTO public.sys_role_menu VALUES (586, 14, 14, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (587, 14, 15, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (588, 14, 16, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (589, 14, 11, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (590, 14, 26, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (591, 14, 27, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (592, 14, 28, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (593, 14, 29, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu VALUES (594, 14, 30, '2026-02-24 10:28:32.524782');
INSERT INTO public.sys_role_menu (role_id, menu_id, create_time)

INSERT INTO public.sys_schedule_job VALUES (1, '节假日信息同步', 'system', 'holidaySync', '10 0 0 1 1 ?', 'holidayTaskExecutor', 'execute', '', 1, 0, '每年1月1日同步节假日信息', '2026-01-23 21:32:33', '2026-02-23 16:13:00.152481');

INSERT INTO public.sys_schedule_log VALUES (1, '节假日信息同步', 'system', 1, NULL, '2026-02-23 16:12:31.013229', '2026-02-23 16:12:32.214335', '2026-02-23 16:12:32.214335', 1, 'holidaySync', '', 1201);

INSERT INTO public.sys_user_role VALUES (56, 12, 2, '2026-02-22 23:25:45.053289');

-- ===============================================================
-- 业务字典（sys_dict_type / sys_dict_data）
-- 业务枚举统一从字典取，禁止前端硬编码中文标签
-- ===============================================================

-- 通用启用/禁用
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (10, '通用状态', 'common_status', '通用启用/禁用', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (100, 10, '启用', '1', 1, '', 'primary', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (101, 10, '禁用', '0', 2, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 操作日志状态（dict_value 与后端 sys_operation_log.status 字段对齐：1=成功 0=失败）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (23, '操作日志状态', 'operation_log_status', '系统管理-操作日志状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (230, 23, '成功', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (231, 23, '失败', '0', 2, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 定时任务状态（dict_value 与后端 sys_schedule_job.status 字段对齐：1=运行 0=暂停）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (24, '定时任务状态', 'job_status', '系统管理-定时任务状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (240, 24, '运行', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (241, 24, '暂停', '0', 2, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 评定季度（dict_value 与后端 score_evaluation.quarter 字段对齐：1..4）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (25, '评定季度', 'evaluation_quarter', '积分管理-评定季度（1=第一季度 2=第二季度 3=第三季度 4=第四季度）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (250, 25, '第一季度', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (251, 25, '第二季度', '2', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (252, 25, '第三季度', '3', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (253, 25, '第四季度', '4', 4, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 是否（dict_value 通用：0=否 1=是；用于"允许并发"等通用是否场景）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (26, '是否', 'yes_no', '通用是否（1=是 0=否）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (260, 26, '是', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (261, 26, '否', '0', 2, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 请假类型（dict_value 与后端 duty_leave_request.leaveType 字段对齐：1=事假 2=病假 3=年假 4=调休 5=其他）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (27, '请假类型', 'leave_type', '值班管理-请假类型', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (270, 27, '事假', '1', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (271, 27, '病假', '2', 2, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (272, 27, '年假', '3', 3, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (273, 27, '调休', '4', 4, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (274, 27, '其他', '5', 5, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 排班模式（dict_value 与后端 duty_schedule.mode / scheduleMode 字段对齐：1=轮换 2=固定 3=自定义）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (28, '排班模式', 'schedule_mode', '值班管理-排班模式（1=轮换排班 2=固定排班 3=排班模式）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (280, 28, '轮换排班', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (281, 28, '固定排班', '2', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (282, 28, '排班模式', '3', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 菜单类型（dict_value 与后端 sys_menu.menuType 字段对齐：0=顶级菜单 1=目录 2=菜单 3=按钮）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (29, '菜单类型', 'menu_type', '系统管理-菜单类型（0=顶级菜单 1=目录 2=菜单 3=按钮）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (290, 29, '顶级菜单', '0', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (291, 29, '目录', '1', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (292, 29, '菜单', '2', 3, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (293, 29, '按钮', '3', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 性别（dict_value 与后端 sys_employee.gender 字段对齐：0=未知 1=男 2=女）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (30, '性别', 'gender', '系统管理-性别（0=未知 1=男 2=女）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (300, 30, '未知', '0', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (301, 30, '男', '1', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (302, 30, '女', '2', 3, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 任务状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (11, '任务状态', 'task_status', '项目管理-任务状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (110, 11, '未开始', '1', 1, '', 'info', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (111, 11, '进行中', '2', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (112, 11, '已完成', '3', 3, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (113, 11, '已暂停', '4', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 任务优先级
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (12, '任务优先级', 'task_priority', '项目管理-任务优先级', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (120, 12, '高', '1', 1, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (121, 12, '中', '2', 2, '', 'warning', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (122, 12, '低', '3', 3, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 审批状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (13, '审批状态', 'approval_status', '通用审批流-审批状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (130, 13, '待审批', '0', 1, '', 'info', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (131, 13, '已通过', '1', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (132, 13, '已驳回', '2', 3, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (133, 13, '已撤回', '3', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 班次类型（dict_value 与后端 sys_shift_config.shiftType 字段对齐：0=白班 1=早班 2=中班 3=晚班 4=全天 5=夜班）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (14, '班次类型', 'shift_type', '值班管理-班次类型', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (140, 14, '白班', '0', 0, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (141, 14, '早班', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (142, 14, '中班', '2', 2, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (143, 14, '晚班', '3', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (144, 14, '全天', '4', 4, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (145, 14, '夜班', '5', 5, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 是否（通用）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (15, '是否', 'is_yes_no', '通用是否判断（1=是，0=否）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (150, 15, '是', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (151, 15, '否', '0', 2, '', 'info', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 性别
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (16, '性别', 'gender', '员工性别', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (160, 16, '男', '1', 1, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (161, 16, '女', '0', 2, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 表单类型
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (17, '表单类型', 'form_type', '工作流-表单类型', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (170, 17, '系统内置', 'system', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (171, 17, '自定义表单', 'custom', 2, '', 'primary', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (172, 17, 'URL 表单', 'url', 3, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 流程部署状态
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (18, '流程部署状态', 'deploy_status', '工作流-流程部署状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (180, 18, '已部署', '1', 1, '', 'success', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (181, 18, '已停用', '0', 2, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 项目状态（dict_value 与后端 pm_project.status 字段对齐：1=未开始 2=进行中 3=已完成 4=已暂停 5=已归档）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (19, '项目状态', 'project_status', '项目管理-项目状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (190, 19, '未开始', '1', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (191, 19, '进行中', '2', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (192, 19, '已完成', '3', 3, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (193, 19, '已暂停', '4', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (194, 19, '已归档', '5', 5, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 任务优先级（dict_value 与后端 pm_task.priority 字段对齐：1=高 2=中 3=低）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (20, '任务优先级', 'task_priority', '项目管理-任务优先级', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (200, 20, '高', '1', 1, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (201, 20, '中', '2', 2, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (202, 20, '低', '3', 3, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 任务状态（dict_value 与后端 pm_task.status 字段对齐：1=未开始 2=进行中 3=已完成 4=已暂停 5=已取消）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (21, '任务状态', 'task_status', '项目管理-任务状态', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (210, 21, '未开始', '1', 1, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (211, 21, '进行中', '2', 2, '', 'primary', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (212, 21, '已完成', '3', 3, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (213, 21, '已暂停', '4', 4, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (214, 21, '已取消', '5', 5, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 积分事件类型（dict_value 与后端 score_event.eventType 字段对齐：1=加分 2=扣分）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (22, '积分事件类型', 'score_event_type', '积分管理-事件类型（1=加分 2=扣分）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (220, 22, '加分', '1', 1, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (221, 22, '扣分', '2', 2, '', 'danger', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 报告类型（dict_value 与后端 ai_report_config.reportType 字段对齐：daily=日报 weekly=周报）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (31, '报告类型', 'report_type', 'AI模块-报告类型（daily=日报 weekly=周报）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (310, 31, '日报', 'daily', 1, '', 'primary', 1, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (311, 31, '周报', 'weekly', 2, '', 'success', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 任务重点标记（dict_value 与后端 pm_task.is_focus 字段对齐：1=重点 0=普通）
INSERT INTO public.sys_dict_type (id, dict_name, dict_code, description, status, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (32, '任务重点', 'task_focus', '项目管理-任务是否重点（1=重点 0=普通）', 1, '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (320, 32, '重点', '1', 1, '', 'warning', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');
INSERT INTO public.sys_dict_data (id, dict_type_id, dict_label, dict_value, dict_sort, css_class, list_class, is_default, status, remark, create_time, update_time) ON CONFLICT (id) DO NOTHING VALUES (321, 32, '普通', '0', 2, '', 'info', 0, 1, '', '2026-06-10 00:00:00', '2026-06-10 00:00:00');

-- 字典类型 5 IPMAC 备注
COMMENT ON COLUMN public.sys_dict_data.dict_sort IS '字典排序';
COMMENT ON COLUMN public.sys_dict_data.dict_label IS '字典标签（中文名）';
COMMENT ON COLUMN public.sys_dict_data.dict_value IS '字典值（业务编码）';
COMMENT ON COLUMN public.sys_dict_data.css_class IS 'CSS 类名';
COMMENT ON public.sys_dict_type.dict_code IS '字典编码（业务唯一标识）';

INSERT INTO public.sys_user_role VALUES (57, 13, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (58, 14, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (59, 15, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (60, 16, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (61, 17, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (62, 18, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (63, 19, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (64, 20, 2, '2026-02-22 23:25:45.053289');
INSERT INTO public.sys_user_role VALUES (72, 1, 1, '2026-02-23 00:26:56.357641');
INSERT INTO public.sys_user_role VALUES (73, 10, 1, '2026-02-23 00:26:56.357641');

