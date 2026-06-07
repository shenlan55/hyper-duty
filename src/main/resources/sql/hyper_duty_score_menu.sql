-- 积分管理模块菜单数据
-- 使用更大的ID值避免与现有数据冲突

-- 首先添加积分管理的主菜单（目录）
INSERT INTO public.sys_menu (id, menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES (200, '积分管理', 0, '', '', '', 1, 'Trophy', 5, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 添加积分事件菜单
INSERT INTO public.sys_menu (id, menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES (201, '积分事件', 200, '/score/event', 'views/score/ScoreEvent.vue', 'score:event:list', 2, 'Setting', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 添加积分记录菜单
INSERT INTO public.sys_menu (id, menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES (202, '积分记录', 200, '/score/record', 'views/score/ScoreRecord.vue', 'score:record:list', 2, 'Edit', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 添加月度汇总菜单
INSERT INTO public.sys_menu (id, menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES (203, '月度汇总', 200, '/score/summary', 'views/score/ScoreSummary.vue', 'score:summary:list', 2, 'DataAnalysis', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 添加评选排名菜单
INSERT INTO public.sys_menu (id, menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES (204, '评选排名', 200, '/score/evaluation', 'views/score/ScoreEvaluation.vue', 'score:evaluation:list', 2, 'Trophy', 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 为超级管理员角色添加积分管理菜单权限
INSERT INTO public.sys_role_menu (id, role_id, menu_id, create_time)
VALUES (800, 1, 200, CURRENT_TIMESTAMP),
       (801, 1, 201, CURRENT_TIMESTAMP),
       (802, 1, 202, CURRENT_TIMESTAMP),
       (803, 1, 203, CURRENT_TIMESTAMP),
       (804, 1, 204, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 为普通用户角色添加积分管理菜单权限
INSERT INTO public.sys_role_menu (id, role_id, menu_id, create_time)
VALUES (805, 2, 200, CURRENT_TIMESTAMP),
       (806, 2, 201, CURRENT_TIMESTAMP),
       (807, 2, 202, CURRENT_TIMESTAMP),
       (808, 2, 203, CURRENT_TIMESTAMP),
       (809, 2, 204, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
