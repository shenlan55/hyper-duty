-- ===============================================================
-- Hyper Duty 工作流模块菜单管理数据
-- ===============================================================

-- 工作流模块父菜单
INSERT INTO public.sys_menu VALUES (100, '工作流管理', 0, '', '', '', 1, 'Operation', 5, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程定义管理
INSERT INTO public.sys_menu VALUES (101, '流程定义', 100, '/workflow/process-list', 'views/workflow/ProcessList.vue', 'workflow:process:list', 2, 'DocumentCopy', 1, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程设计器
INSERT INTO public.sys_menu VALUES (102, '流程设计', 100, '/workflow/designer', 'views/workflow/ProcessDesigner.vue', 'workflow:process:design', 2, 'Edit', 2, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程实例管理（含跟踪）
INSERT INTO public.sys_menu VALUES (103, '流程实例', 100, '/workflow/instance-list', 'views/workflow/ProcessInstanceList.vue', 'workflow:instance:list', 2, 'View', 3, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 待办任务
INSERT INTO public.sys_menu VALUES (104, '待办任务', 100, '/workflow/todo-task', 'views/workflow/TodoTaskList.vue', 'workflow:task:todo', 2, 'List', 4, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 已办任务
INSERT INTO public.sys_menu VALUES (105, '已办任务', 100, '/workflow/done-task', 'views/workflow/DoneTaskList.vue', 'workflow:task:done', 2, 'DocumentChecked', 5, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 表单管理
INSERT INTO public.sys_menu VALUES (106, '表单管理', 100, '/workflow/form-list', 'views/workflow/FormList.vue', 'workflow:form:list', 2, 'Document', 6, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 流程分类管理
INSERT INTO public.sys_menu VALUES (107, '流程分类', 100, '/workflow/category-list', 'views/workflow/CategoryList.vue', 'workflow:category:list', 2, 'Files', 7, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 委托配置管理
INSERT INTO public.sys_menu VALUES (108, '委托配置', 100, '/workflow/delegate-list', 'views/workflow/DelegateList.vue', 'workflow:delegate:list', 2, 'SwitchButton', 8, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');

-- 发起流程
INSERT INTO public.sys_menu VALUES (109, '发起流程', 100, '/workflow/start', 'views/workflow/ProcessStart.vue', 'workflow:process:start', 2, 'PlayCircle', 9, 1, '2026-05-10 00:00:00', '2026-05-10 00:00:00');


-- ===============================================================
-- 角色菜单权限配置
-- ===============================================================

-- 超级管理员拥有工作流模块所有权限
INSERT INTO public.sys_role_menu VALUES (700, 1, 100, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (701, 1, 101, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (702, 1, 102, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (703, 1, 103, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (704, 1, 104, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (705, 1, 105, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (706, 1, 106, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (707, 1, 107, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (708, 1, 108, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (709, 1, 109, '2026-05-10 00:00:00');

-- 普通用户拥有工作流模块基础权限
INSERT INTO public.sys_role_menu VALUES (750, 2, 100, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (751, 2, 103, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (752, 2, 104, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (753, 2, 105, '2026-05-10 00:00:00');
INSERT INTO public.sys_role_menu VALUES (754, 2, 109, '2026-05-10 00:00:00');
