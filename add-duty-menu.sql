-- 添加值班管理菜单到数据库

-- 1. 添加值班管理目录菜单
INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES ('值班管理', 0, '/duty', 'views/duty/DutyLayout.vue', '', 1, 'Calendar', 3, 1, NOW(), NOW());

-- 获取刚插入的值班管理目录ID
SET @duty_menu_id = LAST_INSERT_ID();

-- 2. 添加值班表管理菜单
INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES ('值班表管理', @duty_menu_id, '/duty/schedule', 'views/duty/DutySchedule.vue', 'duty:schedule:list', 2, 'DocumentCopy', 1, 1, NOW(), NOW());

-- 3. 添加值班安排菜单
INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES ('值班安排', @duty_menu_id, '/duty/assignment', 'views/duty/DutyAssignment.vue', 'duty:assignment:list', 2, 'Calendar', 2, 1, NOW(), NOW());

-- 4. 添加值班记录菜单
INSERT INTO sys_menu (menu_name, parent_id, path, component, perm, type, icon, sort, status, create_time, update_time)
VALUES ('值班记录', @duty_menu_id, '/duty/record', 'views/duty/DutyRecord.vue', 'duty:record:list', 2, 'Document', 3, 1, NOW(), NOW());

-- 查询添加的菜单，验证结果
SELECT * FROM sys_menu WHERE parent_id = @duty_menu_id OR id = @duty_menu_id;
