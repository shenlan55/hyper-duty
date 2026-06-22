 -- ===============================================================
-- 生产→开发 环境数据迁移脚本（纯 SQL 生成 INSERT VALUES）
-- 作者：hyper-duty 项目管理模块
-- 日期：2026-06-22
--
-- 用法（生产库 DBeaver 跑）：
--   1. 选中每段 SELECT，F8 跑出结果
--   2. 右键结果 → "导出到文件" / "Copy Cell"
--   3. 8 个表各保存为 1_export_<table>.sql
--
-- 用法（开发库 DBeaver 跑）：
--   1. 按顺序执行 0_clean_dev.sql（清空）+ 1_import_all.sql（导入）
--   2. 最后跑 2_setval.sql（修序列）+ 3_verify.sql（验证）
-- ===============================================================


-- ===============================================================
-- 0. 生产库 - 清空 7 张表无关数据（保留字典和员工基础数据）
--    注意：只在你想"完全替换开发库任务数据"时跑
-- ===============================================================

-- DELETE FROM public.pm_shadow_annotation;
-- DELETE FROM public.pm_task_shadow;
-- DELETE FROM public.pm_task_progress_update;
-- DELETE FROM public.pm_task_comment;
-- DELETE FROM public.pm_task_stakeholder;
-- DELETE FROM public.pm_task;
-- DELETE FROM public.pm_project;
-- （员工和字典保留不删，避免破坏其他模块）


-- ===============================================================
-- 0.5 生产库 - 数据量自检（先跑这个，决定方案）
-- ===============================================================

-- A. 全部任务 + 依赖行数
SELECT
  'pm_task' AS tbl, COUNT(*) AS n,
  pg_size_pretty(SUM(pg_column_size(pm_task.*))::bigint) AS total_size
FROM public.pm_task
UNION ALL SELECT 'pm_task_progress_update', COUNT(*), pg_size_pretty(SUM(pg_column_size(pm_task_progress_update.*))::bigint) FROM public.pm_task_progress_update
UNION ALL SELECT 'pm_task_stakeholder', COUNT(*), pg_size_pretty(SUM(pg_column_size(pm_task_stakeholder.*))::bigint) FROM public.pm_task_stakeholder
UNION ALL SELECT 'pm_task_comment', COUNT(*), pg_size_pretty(SUM(pg_column_size(pm_task_comment.*))::bigint) FROM public.pm_task_comment
UNION ALL SELECT 'pm_task_shadow', COUNT(*), pg_size_pretty(SUM(pg_column_size(pm_task_shadow.*))::bigint) FROM public.pm_task_shadow
UNION ALL SELECT 'pm_shadow_annotation', COUNT(*), pg_size_pretty(SUM(pg_column_size(pm_shadow_annotation.*))::bigint) FROM public.pm_shadow_annotation
UNION ALL SELECT 'pm_project', COUNT(*), pg_size_pretty(SUM(pg_column_size(pm_project.*))::bigint) FROM public.pm_project
UNION ALL SELECT 'sys_employee', COUNT(*), pg_size_pretty(SUM(pg_column_size(sys_employee.*))::bigint) FROM public.sys_employee
ORDER BY 1;

-- B. 估算 INSERT VALUES 文本大小（粗略：每行约 200-500 字节）
-- pm_task 行数 × 300B ≈ INSERT 文本字节数
-- 1000 行 ≈ 300KB，10000 行 ≈ 3MB，50000 行 ≈ 15MB

-- C. ID 范围（决定开发库序列 setval 起点）
SELECT
  'pm_task' AS tbl, MIN(id) AS min_id, MAX(id) AS max_id FROM public.pm_task
UNION ALL SELECT 'pm_task_progress_update', MIN(id), MAX(id) FROM public.pm_task_progress_update
UNION ALL SELECT 'pm_task_stakeholder', MIN(id), MAX(id) FROM public.pm_task_stakeholder
UNION ALL SELECT 'pm_task_comment', MIN(id), MAX(id) FROM public.pm_task_comment
UNION ALL SELECT 'pm_task_shadow', MIN(id), MAX(id) FROM public.pm_task_shadow
UNION ALL SELECT 'pm_project', MIN(id), MAX(id) FROM public.pm_project
UNION ALL SELECT 'sys_employee', MIN(id), MAX(id) FROM public.sys_employee
ORDER BY 1;

-- ⚠️ 如果总 INSERT 文本 > 50MB（生产任务 > 10000 行），
--    强烈建议改用 pg_dump --inserts 命令行（见文件末尾"备选方案"）


-- ===============================================================
-- 1. 生产库 - 导出 SQL（每段一个 SELECT，结果保存为 .sql）
-- ===============================================================

-- 1.1 sys_employee（员工）
-- 说明：生产员工可能比开发多，需要先导入员工
-- ⚠️ 字段名以生产库为准，开发库是 dept_id / dict_type_id / dict_data_id
SELECT string_agg(
  format(
    'INSERT INTO public.sys_employee (id, employee_name, dept_id, employee_code, phone, email, gender, dict_type_id, dict_data_id, status, username, password, sort, create_time, update_time) VALUES (%s, %L, %s, %L, %L, %L, %s, %s, %s, %s, %L, %L, %s, %L, %L) ON CONFLICT (id) DO NOTHING;',
    id, employee_name,
    COALESCE(dept_id::text, 'NULL'),
    COALESCE(employee_code, ''),
    COALESCE(phone, ''),
    COALESCE(email, ''),
    COALESCE(gender::text, '0'),
    COALESCE(dict_type_id::text, 'NULL'),
    COALESCE(dict_data_id::text, 'NULL'),
    COALESCE(status::text, '1'),
    COALESCE(username, ''),
    COALESCE(password, ''),
    COALESCE(sort::text, '0'),
    COALESCE(create_time::text, 'NULL'),
    COALESCE(update_time::text, 'NULL')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.sys_employee;

-- 1.2 pm_project（项目）
SELECT string_agg(
  format(
    'INSERT INTO public.pm_project (id, project_name, project_code, module_id, priority, status, progress, start_date, end_date, description, owner_id, create_by, archived, sort, create_time, update_time) VALUES (%s, %L, %L, %s, %s, %s, %s, %L, %L, %L, %s, %s, %s, %s, %L, %L) ON CONFLICT (id) DO NOTHING;',
    id, project_name, project_code,
    COALESCE(module_id::text, 'NULL'),
    COALESCE(priority::text, '3'),
    COALESCE(status::text, '1'),
    COALESCE(progress::text, '0'),
    COALESCE(start_date::text, 'NULL'),
    COALESCE(end_date::text, 'NULL'),
    COALESCE(description, ''),
    COALESCE(owner_id::text, 'NULL'),
    COALESCE(create_by::text, 'NULL'),
    COALESCE(archived::text, '0'),
    COALESCE(sort::text, '0'),
    COALESCE(create_time::text, 'NULL'),
    COALESCE(update_time::text, 'NULL')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.pm_project;

-- 1.3 pm_task（任务 - 核心表）
SELECT string_agg(
  format(
    'INSERT INTO public.pm_task (id, task_name, task_code, project_id, description, start_date, end_date, status, priority, assignee_id, create_by, create_time, update_time, parent_id, task_level, progress, is_pinned, module_id, attachments, stakeholders, is_focus) VALUES (%s, %L, %L, %s, %L, %L, %L, %s, %s, %s, %s, %L, %L, %s, %s, %s, %s, %s, %L, %L, %s) ON CONFLICT (id) DO NOTHING;',
    id, task_name, task_code, project_id,
    COALESCE(description, ''),
    COALESCE(start_date::text, 'NULL'),
    COALESCE(end_date::text, 'NULL'),
    status, priority,
    COALESCE(assignee_id::text, 'NULL'),
    COALESCE(create_by::text, 'NULL'),
    COALESCE(create_time::text, 'NULL'),
    COALESCE(update_time::text, 'NULL'),
    COALESCE(parent_id::text, '0'),
    COALESCE(task_level::text, '1'),
    COALESCE(progress::text, '0'),
    COALESCE(is_pinned::text, '0'),
    COALESCE(module_id::text, 'NULL'),
    COALESCE(attachments, 'NULL'),
    COALESCE(stakeholders, 'NULL'),
    COALESCE(is_focus::text, '0')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.pm_task;

-- 1.4 pm_task_stakeholder（干系人）
SELECT string_agg(
  format(
    'INSERT INTO public.pm_task_stakeholder (id, task_id, employee_id, stakeholder_type, created_at, updated_at) VALUES (%s, %s, %s, %s, %L, %L) ON CONFLICT (id) DO NOTHING;',
    id, task_id, employee_id, stakeholder_type,
    COALESCE(created_at::text, 'NULL'),
    COALESCE(updated_at::text, 'NULL')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.pm_task_stakeholder;

-- 1.5 pm_task_progress_update（进度更新）
SELECT string_agg(
  format(
    'INSERT INTO public.pm_task_progress_update (id, task_id, employee_id, progress, description, attachments, create_time) VALUES (%s, %s, %s, %s, %L, %L, %L) ON CONFLICT (id) DO NOTHING;',
    id, task_id, employee_id, progress,
    COALESCE(description, ''),
    COALESCE(attachments::text, 'NULL'),
    COALESCE(create_time::text, 'NULL')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.pm_task_progress_update;

-- 1.6 pm_task_comment（任务评论）
SELECT string_agg(
  format(
    'INSERT INTO public.pm_task_comment (id, task_id, employee_id, content, attachment_url, created_at, updated_at) VALUES (%s, %s, %s, %L, %L, %L, %L) ON CONFLICT (id) DO NOTHING;',
    id, task_id, employee_id,
    COALESCE(content, ''),
    COALESCE(attachment_url, ''),
    COALESCE(created_at::text, 'NULL'),
    COALESCE(updated_at::text, 'NULL')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.pm_task_comment;

-- 1.7 pm_task_shadow（影子任务）
SELECT string_agg(
  format(
    'INSERT INTO public.pm_task_shadow (id, source_task_id, project_id, parent_id, shadow_alias, shadow_description, created_by, created_at, updated_at) VALUES (%s, %s, %s, %s, %L, %L, %L, %L, %L) ON CONFLICT (id) DO NOTHING;',
    id, source_task_id, project_id,
    COALESCE(parent_id::text, '0'),
    COALESCE(shadow_alias, ''),
    COALESCE(shadow_description, ''),
    COALESCE(created_by, ''),
    COALESCE(created_at::text, 'NULL'),
    COALESCE(updated_at::text, 'NULL')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.pm_task_shadow;

-- 1.8 pm_shadow_annotation（影子批注）
SELECT string_agg(
  format(
    'INSERT INTO public.pm_shadow_annotation (id, shadow_id, content, created_by, created_at) VALUES (%s, %s, %L, %L, %L) ON CONFLICT (id) DO NOTHING;',
    id, shadow_id, content,
    COALESCE(created_by, ''),
    COALESCE(created_at::text, 'NULL')
  ),
  E'\n' ORDER BY id
) AS sql_text
FROM public.pm_shadow_annotation;


-- ===============================================================
-- 2. 开发库 - 修序列（必须在所有 INSERT 跑完后执行）
-- ===============================================================

-- 2.1 修 pm_task_progress_update 序列
-- （这是 BIGSERIAL 自增的唯一一张任务相关表）
SELECT setval(
  pg_get_serial_sequence('public.pm_task_progress_update', 'id'),
  COALESCE((SELECT MAX(id) FROM public.pm_task_progress_update), 1)
);

-- 2.2 修字典序列（如果生产字典 ID 更大）
SELECT setval(
  pg_get_serial_sequence('public.sys_dict_type', 'id'),
  COALESCE((SELECT MAX(id) FROM public.sys_dict_type), 1)
);
SELECT setval(
  pg_get_serial_sequence('public.sys_dict_data', 'id'),
  COALESCE((SELECT MAX(id) FROM public.sys_dict_data), 1)
);

-- 2.3 修其它 SERIAL/BIGSERIAL 表（如果生产用 SERIAL 而开发用 BIGSERIAL）
-- SELECT setval(pg_get_serial_sequence('public.pm_project_participant', 'id'),
--   COALESCE((SELECT MAX(id) FROM public.pm_project_participant), 1));
-- SELECT setval(pg_get_serial_sequence('public.pm_project_deputy_owner', 'id'),
--   COALESCE((SELECT MAX(id) FROM public.pm_project_deputy_owner), 1));


-- ===============================================================
-- 3. 开发库 - 验证
-- ===============================================================

SELECT
  'pm_task' AS tbl, COUNT(*) AS rows_imported FROM public.pm_task
UNION ALL SELECT 'pm_task_progress_update', COUNT(*) FROM public.pm_task_progress_update
UNION ALL SELECT 'pm_task_stakeholder', COUNT(*) FROM public.pm_task_stakeholder
UNION ALL SELECT 'pm_task_comment', COUNT(*) FROM public.pm_task_comment
UNION ALL SELECT 'pm_task_shadow', COUNT(*) FROM public.pm_task_shadow
UNION ALL SELECT 'pm_project', COUNT(*) FROM public.pm_project
UNION ALL SELECT 'sys_employee', COUNT(*) FROM public.sys_employee
ORDER BY 1;

-- 检查孤儿数据（外键完整性）
SELECT 'orphan_task_project' AS check, COUNT(*) FROM public.pm_task t
  WHERE NOT EXISTS (SELECT 1 FROM public.pm_project p WHERE p.id = t.project_id);
SELECT 'orphan_task_assignee' AS check, COUNT(*) FROM public.pm_task t
  WHERE t.assignee_id IS NOT NULL
    AND NOT EXISTS (SELECT 1 FROM public.sys_employee e WHERE e.id = t.assignee_id);


-- ===============================================================
-- 备选方案：pg_dump 命令行（数据量大时推荐）
-- ===============================================================
-- 在生产服务器（或能 ssh 登录的跳板机）执行：
--
--   # 1. 导出 8 张表为纯 INSERT 脚本
--   pg_dump -h 生产IP -U postgres -d hyper_duty \
--     --data-only --inserts \
--     -t public.sys_employee \
--     -t public.pm_project \
--     -t public.pm_task \
--     -t public.pm_task_stakeholder \
--     -t public.pm_task_progress_update \
--     -t public.pm_task_comment \
--     -t public.pm_task_shadow \
--     -t public.pm_shadow_annotation \
--     > prod_data.sql
--
--   # 2. scp 传回开发机
--   scp prod_data.sql user@开发IP:/path/to/prod_data.sql
--
--   # 3. 开发库导入
--   psql -h localhost -U postgres -d hyper_duty < prod_data.sql
--
--   # 4. 开发库修序列（同上面的 2_setval 段）
--
-- 优势：自动处理大字段、引号转义、ID 范围；几 GB 数据也能 1-2 分钟搞定
-- 劣势：需要在生产服务器有 ssh 权限
