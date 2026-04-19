-- =============================================
-- AI报告功能菜单和配置数据SQL脚本
-- 请手动执行此SQL脚本
-- =============================================

-- 1. 检查并添加AI报告配置菜单
-- 先检查菜单ID是否已存在
DO $$
DECLARE
    v_menu_id INTEGER;
BEGIN
    -- 检查ID为39的菜单是否存在
    SELECT id INTO v_menu_id FROM sys_menu WHERE id = 39;
    IF v_menu_id IS NULL THEN
        -- 添加 AI报告生成 菜单（如果不存在）
        INSERT INTO sys_menu (id, menu_name, parent_id, path, component, perms, menu_type, icon, order_num, status, create_time, update_time)
        VALUES (39, 'AI报告配置', 26, '/project/ai-report-config', 'views/project/AiReportConfig.vue', 'project:aiReportConfig:list', 2, 'Setting', 11, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
        RAISE NOTICE 'AI报告配置菜单已添加';
    ELSE
        RAISE NOTICE 'AI报告配置菜单已存在，跳过添加';
    END IF;
END $$;

-- 2. 为管理员角色添加菜单权限
DO $$
DECLARE
    v_role_menu_id INTEGER;
BEGIN
    -- 检查是否已经存在该权限
    SELECT id INTO v_role_menu_id FROM sys_role_menu WHERE role_id = 1 AND menu_id = 39;
    IF v_role_menu_id IS NULL THEN
        INSERT INTO sys_role_menu (role_id, menu_id, create_time)
        VALUES (1, 39, CURRENT_TIMESTAMP);
        RAISE NOTICE 'AI报告配置菜单权限已添加到管理员角色';
    ELSE
        RAISE NOTICE 'AI报告配置菜单权限已存在，跳过添加';
    END IF;
END $$;

-- 3. 初始化AI报告配置数据（如果不存在）
DO $$
DECLARE
    v_config_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_config_count FROM ai_report_config;
    IF v_config_count = 0 THEN
        INSERT INTO ai_report_config (id, config_name, config_code, report_type, prompt_template, model_name, status, remark, create_by, create_time)
        VALUES 
        (1, 'SRE项目日报', 'SRE_DAILY', 'daily', '请基于以下项目任务数据，按项目分组，生成一份结构化的日报：

【项目信息】
{projectInfo}

【今日任务更新】
{taskUpdates}

重要要求：
1. 按项目分组，每个项目独立成一个大章节
2. 每个项目下分"今日已完成"、"今日进行中"、"明日计划"三部分
3. 严格按照"一、二、三"、"1.2.3."、"①②③"的三级层级输出
4. 【核心要求】：不要简单罗列任务更新，而是要有总结提炼能力！
   - 从各个人的任务更新中总结出项目整体的进展
   - 提取关键点和里程碑成果
   - 把相似的更新合并，突出整体趋势
   - 语言要精炼、专业，突出价值
5. 【非常重要】：任务数据中明确标记了"★★★ 重点任务区域 ★★★"和"◆◆◆ 高优先级任务区域 ◆◆◆"，请在最终报告中严格保留这两个独立区域的划分，重点任务区域放在前面，两个区域之间用明显的分隔线或标题隔开！
6. 只展示你选择的项目的内容，不要展示其他项目的内容！
7. 对于重点任务，要详细描述进展和成果；对于高优先级任务，也要适当展开，但篇幅可以相对短一些！', 'glm-4-flash', 1, 'SRE项目日常日报生成配置', 1, CURRENT_TIMESTAMP),
        (2, 'SRE项目周报', 'SRE_WEEKLY', 'weekly', '请基于以下一周的任务数据，按项目分组，生成一份结构化的周报：

【项目信息】
{projectInfo}

【本周任务汇总】
{weeklyTaskData}

重要要求：
1. 按项目分组，每个项目独立成一个大章节
2. 包含"一、周报基本信息"、"二、本周核心成果（按项目）"、"三、本周工作进度复盘"、"四、项目风险与问题"、"五、下周工作计划（按项目）"
3. 严格按照"一、二、三"、"1.2.3."、"①②③"的三级层级输出
4. 【核心要求】：不要简单罗列每天的任务更新，而是要有总结提炼能力！
   - 把一周的工作归纳成核心成果，不要流水账
   - 突出亮点和关键里程碑
   - 分析进度差异和趋势
   - 语言要精炼、专业，有高度
   - 合并相似任务，提炼共同主题
5. 【非常重要】：任务数据中明确标记了"★★★ 重点任务区域 ★★★"和"◆◆◆ 高优先级任务区域 ◆◆◆"，请在最终报告中严格保留这两个独立区域的划分，重点任务区域放在前面，两个区域之间用明显的分隔线或标题隔开！
6. 只展示你选择的项目的内容，不要展示其他项目的内容！
7. 对于重点任务，要详细描述一周的整体进展；对于高优先级任务，也要适当展开，但篇幅可以相对短一些！', 'glm-4-flash', 1, 'SRE项目周报生成配置', 1, CURRENT_TIMESTAMP);
        RAISE NOTICE 'AI报告配置数据已初始化';
    ELSE
        RAISE NOTICE 'AI报告配置数据已存在，跳过初始化';
    END IF;
END $$;

-- 4. 重置序列
SELECT pg_catalog.setval('public.ai_report_config_id_seq', 2, true);

-- =============================================
-- SQL脚本执行完成！
-- =============================================
