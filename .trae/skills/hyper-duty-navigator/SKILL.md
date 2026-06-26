---
name: "hyper-duty-navigator"
description: "Hyper Duty 项目导航专家，负责分析用户需求并推荐最合适的技能。当用户提到做功能、修bug、解决问题、更新文档、开发新功能、修复问题、排查问题、导航、通用规范、分页、缓存、移动端适配等任何开发任务时，首先调用此技能。这是项目的入口技能，所有开发任务都应该先经过这里。"
---

# Hyper Duty 项目导航专家

这个技能是 Hyper Duty 项目的"入口技能"，负责：
1. **分析用户需求**：判断该调用哪个具体技能
2. **提供通用规范指引**：告诉开发者去查什么文档
3. **指导问题排查**：提供标准的问题排查流程

## 🎯 技能自动调用规范

### 核心原则
当收到用户请求时，必须首先分析请求的语境，选择最适合的技能进行调用。

### 技能映射表

| 需求类型 | 关键词 | 推荐技能 |
|---------|-------|---------|
| **值班管理模块任务** | 排班、请假、调班、值班统计、节假日 | `duty-module-developer` |
| **项目管理模块任务** | 项目、任务、批量新建任务、批量更新进度、甘特图、任务统计 | `project-module-developer` |
| **积分管理模块任务** | 积分、积分事件、积分记录、月度汇总、评选排名、评价 | `score-module-developer` |
| **工作流模块任务** | 流程、审批、会签、表单、任务转办、流程跟踪、委托 | `workflow-module-developer`（如果可用） |
| **工作流踩坑**（ResponseResult 重载、XML 解析、processDefinitionId 转义） | `docs/工作流模块实现文档.md` → "踩坑经验"小节 |
| **全栈开发任务** | 同时修改前端和后端、没有明确模块指定 | `hyper-duty-fullstack` |
| **代码重构任务** | 重构、优化代码结构、拆分组件、DTO/VO、API版本控制 | `hyper-duty-refactorer` |
| **前端设计任务** | 设计新界面、优化UI/UX | `frontend-design` 或 `ui-ux-pro-max` |
| **代码审查任务** | 审查代码 | `code-review` 或 `frontend-code-review` |
| **创意/设计任务** | 开始创新性工作前 | `brainstorming` |
| **SRE项目报告任务** | 生成SRE项目日报或周报 | `SRE_Project_Dimension_Report` 或 `SRE_Project_Weekly_Report` |
| **技能创建/查找任务** | 创建新技能或查找现有技能 | `skill-creator` 或 `find-skills` |
| **交互设计任务** | 优化微交互、动效设计 | `interaction-design` |

### 技能调用优先级
1. **模块专属技能**：duty-module-developer、project-module-developer、workflow-module-developer
2. **重构技能**：hyper-duty-refactorer
3. **全栈开发技能**：hyper-duty-fullstack
4. **通用技能**：frontend-design、code-review、brainstorming等

### 调用前验证
在调用技能前，确认技能确实与用户需求匹配。

### 多技能组合
对于复杂任务，可以组合调用多个技能，但需确保调用顺序合理。

### 技能使用反馈
每次使用技能后，评估其效果并在必要时调整调用策略。

## 📋 通用开发规范（指引）

### 文档查阅指南
| 规范类型 | 查阅文档 | 文档位置 |
|---------|---------|---------|
| **项目架构与技术栈** | 项目规则 | `.trae/rules/project_rules.md` |
| **业务字典使用规范** | 业务字典使用手册 | `docs/业务字典使用手册.md` |
| **分页实现规范** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **缓存管理规范** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **DTO模式和关联数据填充** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **移动端适配规范** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **SQL脚本管理** | 平台开发手册 | `docs/平台开发手册.md` |
| **API调用规范** | 平台开发手册 | `docs/平台开发手册.md` |
| **工作流环节处理人** | 工作流模块实现文档 | `docs/工作流模块实现文档.md` |
| **BpmnDesigner 集成** | 工作流模块实现文档 | `docs/工作流模块实现文档.md` |

### 文档同步更新指引
- 平台底座变更：同步更新 `docs/平台开发手册.md` 和 `docs/平台需求说明书.md`
- 值班管理模块变更：同步修订 `docs/值班管理中心实现文档.md`
- 项目管理模块变更：同步修订 `docs/项目管理中心实现文档.md`
- 积分管理模块变更：同步修订 `docs/2026-06-04-员工积分管理模块设计.md`

## 🔍 问题排查规范

### 主动使用 MCP 工具
在排查问题时，必须主动使用以下 MCP 工具获取信息：
- **PostgreSQL MCP**：查询数据库结构和数据，验证数据一致性
- **Chrome DevTools MCP**：查看浏览器控制台错误、网络请求和页面状态

### 系统访问信息
- **前端访问地址**：http://localhost:5173
- **登录账号**：wangguohao
- **登录密码**：123456

### 排查流程
1. **前端问题**：先使用 Chrome DevTools 查看控制台错误和网络请求
2. **后端问题**：先使用 PostgreSQL 查询相关数据和表结构
3. **数据一致性问题**：同时使用两种 MCP 工具验证前后端数据

### 信息收集
在开始修复前，必须收集以下信息：
- 浏览器控制台错误信息
- 相关 API 的网络请求和响应
- 数据库相关表结构和数据
- 相关代码文件的最新版本

### 问题查阅指南
| 问题类型 | 查阅文档 | 文档位置 |
|---------|---------|---------|
| **分页相关问题** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **BaseTable 翻页事件被吞** | 业务字典使用手册 → 踩坑经验 #7 | `docs/业务字典使用手册.md` |
| **业务枚举硬编码** | 业务字典使用手册 → 设计原则 | `docs/业务字典使用手册.md` |
| **字典 SQL 重复 key / 外键约束** | 业务字典使用手册 → 踩坑经验 #1、#2 | `docs/业务字典使用手册.md` |
| **DBeaver ON CONFLICT 语法错** | 业务字典使用手册 → 踩坑经验 #6 | `docs/业务字典使用手册.md` |
| **员工姓名显示问题** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **班次名称显示问题** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **缓存相关问题** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **邮件配置问题** | 值班管理中心实现文档 | `docs/值班管理中心实现文档.md` |
| **工作流 ResponseResult.success(String) 误识别** | 工作流模块实现文档 → 踩坑经验 #1 | `docs/工作流模块实现文档.md` |
| **流程设计器打开空白 / XML 解析失败** | 工作流模块实现文档 → 踩坑经验 #2、#3 | `docs/工作流模块实现文档.md` |
| **processDefinitionId 含冒号导致 400** | 工作流模块实现文档 → 踩坑经验 #4 | `docs/工作流模块实现文档.md` |
| **环节处理人属性面板打不开 / selector 弹窗空白** | 工作流模块实现文档 → 踩坑经验 #5 | `docs/工作流模块实现文档.md` |
| **人员查询规范（必读）** | 通用避坑 → 避坑 #17 | 本技能 → 避坑 #17 |
| **部门查询规范（必读）** | 通用避坑 → 避坑 #18 | 本技能 → 避坑 #18 |

## ⚠️ 通用避坑指南（高频踩坑）

### 1. ResponseResult.success() 重载陷阱
`ResponseResult.success(String)` 会把 String 当 message，`data` 变 null。返回 String 类型数据时**必须**用 `success("success", data)` 双参重载。详见 `docs/工作流模块实现文档.md` 踩坑经验 #1。

### 2. 后端 @RequestMapping 不要写 /api 前缀
- 后端 `server.servlet.context-path: /api` 已经在最终路径前加了 `/api`
- Controller 写 `@RequestMapping("/workflow/...")` 即可
- 前端 axios 的 `baseURL: '/api'` + API URL `/workflow/...` 拼成 `/api/workflow/...`，Vite 代理到后端
- **最终链路**：浏览器 `/api/workflow/...` → Vite 代理 → `http://localhost:8080/api/workflow/...` → 后端 context-path 移除 `/api` → Controller 处理 `/workflow/...`

### 3. 前端 API URL 不要写 /api 前缀
- `baseURL: '/api'` 已经处理，URL 写 `/workflow/...` 即可
- 否则会形成 `/api/api/...` 重复

### 4. 含特殊字符的 ID 用 query 参数
- Flowable 的 `processDefinitionId` 含冒号（如 `leave:1:50004`）
- **必须**用 `@RequestParam` query 参数，不要用 `@PathVariable`
- 原因：冒号在 path 中可能被错误解析

### 5. 排查 bug 时**优先用 MCP**
- **不要靠猜！** 用 Chrome DevTools MCP 看实际网络请求和响应
- 用 PostgreSQL MCP 验证数据库真实数据
- 看后端日志确认接口是否被调用、入参是什么、返回什么

### 6. 调试代码及时清理
排查问题时临时加的 `/debug/...` 接口、临时日志、临时 console.log，**修完 bug 立即删除**
- 提交前用 `git diff` 复盘本次修改，删除无关注释和调试残留

### 7. BaseTable 翻页事件被吞
`BaseTable.vue` 中 `el-pagination` 用 `v-model:current-page` 双向绑定本地 ref 后，`@current-change` 触发时本地 ref 已被 el-pagination 提前更新，导致判断 `localCurrentPage.value !== current` 恒为 false，**事件被吞，父组件收不到 `current-change`**。
- 修复：把判断条件从 `localCurrentPage.value !== current` 改为 `props.pagination.currentPage !== current`
- 详见 `docs/业务字典使用手册.md` 踩坑经验 #7（同一 bug 影响所有使用 BaseTable 的页面：任务、人员、字典、定时任务、值班）

### 8. 业务枚举禁止前端硬编码
**禁止**在前端 Vue 组件内写 `const STATUS_MAP = { 1: '未开始', 2: '进行中' }` 或 `<el-tag>{{ STATUS_MAP[row.status] }}</el-tag>`。
- **必须**走 `sys_dict_type + sys_dict_data` 字典表 + 前端 `useDict('task_status')` 动态加载
- 新增枚举的标准流程见 `docs/业务字典使用手册.md` 第 5 节
- 任何业务枚举（任务状态/优先级/重点、班次、请假、排班、审批、是否、性别、菜单类型、报告类型、定时任务状态、操作日志状态、评定季度、积分事件类型等）都必须走字典

### 9. BaseTable 必须传 :backend-pagination="true"（否则小数据被切没）
`BaseTable.vue` 中 `processedData` 计算属性：当 `props.backendPagination=false`（默认）时，会对 `props.data` 做客户端分页 `slice((page-1)*size, page*size)`。
- **当后端已经分页**（接口返回 `records + total + current + size`），前端必须传 `:backend-pagination="true"`
- 否则后端返回 1 条数据时，`tableData.slice(10, 20)` = `[]`，页面显示"暂无数据"（实际后端有数据）
- 适用页面：`MyTask.vue`、`TaskList.vue` 等所有调用分页接口的页面
- 参考：`docs/项目管理中心实现文档.md` 8.5 节"根不可切断"分页修复

### 10. 后端分页 total 必须是"实际行数"，不能是"totalPages × pageSize"
"根不可切断"分页时，后端实际返回的 `total` 应该 = **所有根+子的总行数**（= 顶部 stats 卡片"任务总数"）。
- 错误做法：`page.setTotal((long) totalPages * pageSize)` → 20（页数 × 每页），与 stats 卡片 11 对不上
- 正确做法：`page.setTotal((long) totalRows)` → 11（实际行数），与 stats 卡片 11 一致
- 原因：用户对照看"共 20 条" vs "总任务数 11"会困惑
- 适用方法：`PmTaskServiceImpl.pageMyTasks`、`PmTaskShadowServiceImpl.pageTaskListWithShadows`
- 参考：`docs/项目管理中心实现文档.md` 8.5.4 节"真实数据演算"
- **历史教训**（2026-06-24）：`PmTaskShadowServiceImpl.pageTaskListWithShadows` 5月22日用了 `adjustedTotal = pagesRootTasks.size() * pageSize`，导致"项目规范"11 根 0 子项目 total=20（错）。修复：`page.setTotal(currentIndex)`（即 `realTotal`）—— **接受最后一页空（el-pagination 显示"暂无数据"是正常行为）**
- **三大铁律专题**：`.trae/skills/project-module-developer/SKILL.md` → "任务分页踩坑专题"（2026-06-24 新增，必读）

### 11. 生产环境 SQL 慢，先用 EXPLAIN ANALYZE 查执行计划
- **本地 3ms ≠ 生产 3ms**，差距主要来自**数据量级 + 索引选择性**
- 排查流程：Chrome DevTools 看 Network → 定位慢 SQL → PostgreSQL MCP 跑 `EXPLAIN ANALYZE` → 看 `Seq Scan` / `Sort` 节点
- **不要靠猜！** MCP 跑出来的执行计划就是真相
- 参考：`docs/项目管理中心实现文档.md` 8.6 节"生产环境性能排查方法论"

### 12. 复杂 ORDER BY 通常无法走索引，必须建复合索引
- 多个排序键（`is_pinned DESC, status, priority, create_time DESC`）+ `CASE WHEN` 表达式 → 走 `Sort` 节点爆
- **修复方案**：建复合索引，前 N 列匹配 ORDER BY（前缀匹配越多越好）
- `CASE WHEN` 表达式不能进索引；如果必须按 CASE 排序，可拆成 `numeric` 计算列再索引
- 实战案例：任务管理列表 4.10s → 加 `idx_pm_task_root_list` 复合索引 → 100ms 内

### 13. 生产环境加索引必须用 CREATE INDEX CONCURRENTLY
- 普通 `CREATE INDEX` 会**锁表**，业务高峰期禁止
- **`CREATE INDEX CONCURRENTLY`** 不锁表、在线建索引（生产必备）
- **注意**：CONCURRENTLY **不能放在事务块中**（BEGIN/COMMIT 会报错），DBeaver 选中 SQL 直接执行即可
- 建好后用 `pg_stat_user_indexes` 验证是否被使用：
  ```sql
  SELECT indexrelname, idx_scan, idx_tup_read
  FROM pg_stat_user_indexes
  WHERE schemaname='public' AND indexrelname='idx_xxx';
  ```
- 部分索引（带 `WHERE` 限定）能减小索引体积、提升选择性，根任务/子任务/已删除等场景强烈推荐

### 14. LATERAL 子查询 = N+1 的数据库版，> 20 行必须拆
- `LEFT JOIN LATERAL (SELECT ... WHERE col = t.col) sub ON true` —— **主查询每行都会执行一次子查询**（22 个根任务 = 22 次子查询 plan + 22 次执行）
- **修复方案（标准做法）**：把 LATERAL 拆成两个独立 SQL
  - 主查询去掉 LATERAL，用 `WHERE col IN (...)` 拉所有行
  - 批量预取 SQL：`SELECT id, MAX(...) GROUP BY id WHERE id IN (...)` 走复合索引
  - Service 层用 `Map<id, value>` 注入到结果对象
- **何时不需要拆**：主查询行数 ≤ 10 + 子查询 < 1ms + 数据量稳定
- **经验值**：> 20 行主查询 + 子查询含 GROUP BY/ORDER BY/LIMIT → **强烈建议拆**
- 实战案例：任务管理列表 `selectSubTasksByRootIds` 22 行 LATERAL 拆 SQL → 消除 N+1，预热 selectSub 4.6ms
- 参考：`docs/项目管理中心实现文档.md` 8.7 节"LATERAL 子查询拆分优化案例"

### 15. 列表 SQL 严禁 SELECT *，必须白名单字段（description 大字段治理）
- **列表接口**走白名单 select：只 SELECT 列表页用到的字段（task_name/status/progress/...）
- **详情接口**保留 `SELECT *` 或显式 select description：详情页需要完整数据
- **致命场景**：`description` 单条最大 3MB + 11 条任务 = 33MB 响应体，**接口 3.6s / 4.9MB**
- **白名单模板**（任务管理列表）：
  ```sql
  SELECT t.id, t.project_id, t.parent_id, t.task_level, t.task_name,
         t.priority, t.status, t.progress, t.start_date, t.end_date,
         t.assignee_id, t.attachments,  -- ← 保留 attachments（列表显示附件图标）
         t.is_pinned, t.is_focus, t.create_by, t.create_time, t.update_time
  FROM pm_task t
  ```
- **UNION ALL 列对齐**：删除字段时用 `NULL::TEXT AS xxx` 占位（外层不返，内层也用 NULL）
- **保留 description 的接口（详情场景，不动）**：`selectTaskById` / `selectShadowById` / 报表类
- **效果**：11 行 × 366 字节 ≈ 4KB（原来 4.9MB）→ **1200× 提升**
- 实战案例：任务管理列表 3.6s → 100-200ms
- 参考：`docs/项目管理中心实现文档.md` 8.8 节"description 大字段治理"

### 16. Druid WallFilter 禁止 SQL 文本中的 `--` 和 `/* */` 注释
- **Druid 1.2.20+ 默认开启 WallFilter**，会拦截带 SQL 注释的语句，报 `sql injection violation ... comment not allow`
- **XML 注释** `<!-- xxx -->` 会被 MyBatis 解析时剥离，**不影响** SQL（放心用）
- **SQL 注释** `-- xxx` 或 `/* xxx */` 在 SQL 文本中 → **Druid 拦截**（禁止用）
- **Java 注释** `// xxx` 在 `@Select("..." + // xxx + ...)` 里 → Java 编译时剥离，**不会**进 SQL 字符串
- **实战踩坑**：在 XML mapper 里加 `-- 2026-06-22 优化：xxx` → 启动后所有查询报"comment not allow"
- **最佳实践**：
  - ✅ XML mapper 注释用 `<!-- xxx -->`（MyBatis 友好）
  - ❌ 不要在 `<select>` 标签内的 SQL 文本里加 `--` 或 `/* */` 注释
  - ⚠️ 不要在 Java `@Select("...")` 注解内塞 `// xxx`（虽然不报错，但易误导）
- **彻底解决方案**（如果必须留 SQL 注释）：在 Druid 配置中关闭 `wall.commentAllow=false`（**不推荐**）
- 参考：`docs/项目管理中心实现文档.md` 8.9 节"Druid SQL 注释踩坑"

### 17. 人员查询必须过滤 status=1（启用人员），禁止查到禁用员工（2026-06-27 新增）
- **全系统铁律**：除 `system`（系统管理）模块外，**所有模块查询 sys_employee 都必须只返回启用员工**（status=1）
- **背景**：2026-06-27 修复 — 积分管理月度汇总查到"蒋成帮"id=37 status=0，排查发现 5 处不规范的查询（积分/统计/顶岗/通用接口）
- **强制规范**：

  | 场景 | 规范 | 原因 |
  |------|------|------|
  | 通用人员查询（PersonSelector / BPMN 选人 / 自动排班 / AI 报告） | **必须**只查启用员工 | 选人/派单场景不该出现已离职/禁用的人 |
  | 顶岗候选（`getAvailableSubstitutes`） | **必须**只查启用员工 | 禁用员工不能实际顶岗 |
  | 统计/排名/汇总（`getMonthlySummary` / `getEvaluationRanking` / `getEmployeeStatistics` / `getDeptStatistics`） | **必须**过滤禁用员工 | 历史 score_summary/duty_assignment 即使绑定了禁用员工，统计时也要排除 |
  | 系统管理-员工分页（`SysEmployeeServiceImpl.page(...)`） | ❌ **故意不过滤** | 管理员需要看到所有员工（含禁用）来管理 |
  | 登录认证（`EmployeeUserDetailsService`） | ❌ **故意不过滤** | 禁用员工仍需能登录查状态 |
  | 历史记录 LEFT JOIN 展示当时姓名 | ❌ **故意不过滤** | 不能"穿越"过滤，否则历史任务的负责人姓名会变 null |

- **统一入口（推荐使用，不要直接 `lambdaQuery().list()`）**：
  ```java
  // ✅ 正确：通过通用接口拿启用员工
  sysEmployeeService.getAllEmployees();                          // 全量启用
  sysEmployeeService.getEmployeesByDeptId(deptId);                // 指定部门启用
  sysEmployeeService.lambdaQuery()
      .in(SysEmployee::getId, ids)
      .eq(SysEmployee::getStatus, 1)
      .list();                                                    // 批量按 ID 取启用

  // ❌ 错误：直接 list() 拉全表
  sysEmployeeService.list();                                     // 包含禁用员工
  sysEmployeeService.listByIds(ids);                             // 包含禁用员工
  sysEmployeeMapper.selectById(id);                              // 按 ID 单条取（含历史展示场景可接受，但不要批量用）
  ```
- **缓存一致性**：员工状态/部门变更时，必须清掉 `employee::allEmployees` + 新旧部门 cache（2026-06-27 修复换部门只清新部门的 bug）
- **新功能自检清单**（提交前必过）：
  1. 这次新增的人员查询接口，调用了 `getAllEmployees` / `getEmployeesByDeptId` / `lambdaQuery().list()` / `listByIds` / `selectById` 中的哪一个？
  2. 选人/统计/顶岗场景 → 是否过滤了 `status=1`？
  3. 员工更新时是否清掉了正确的 cache（all + 新部门 + 旧部门）？
  4. 前端 PersonSelector / 自动选人组件是否只拉取了启用员工？
- **参考实现**：`SysEmployeeServiceImpl.getAllEmployees` / `ScoreSummaryServiceImpl.getMonthlySummary` / `LeaveRequestServiceImpl.getAvailableSubstitutes` / `DutyStatisticsServiceImpl.getEmployeeStatistics`

### 18. 部门查询使用"双接口"（2026-06-27 第二版，从一刀切改为按需选接口）

- **设计原则**：**方法名即契约** —— `getAll*` 默认全量（系统管理用），`getActive*` 只查启用（业务模块用）
- **背景**：2026-06-27 第一版一刀切（`getAllDepts` 加 status=1）后，**系统管理部门管理页面看不到禁用部门**（用户报"禁用的部门，系统管理员在部门管理里面都看不到了"）；重构成双接口方案，方法名自带语义
- **强制规范**：

  | 接口 | 是否过滤 status=1 | 调用方 |
  |------|-------------------|--------|
  | `sysDeptService.getAllDepts()` | ❌ 不过滤 | 系统管理（Dept.vue / Employee.vue / SystemStatisticsController）|
  | `sysDeptService.getDeptTree()` | ❌ 不过滤 | 系统管理（Dept.vue / Employee.vue）|
  | `sysDeptService.getActiveDepts()` | ✅ 过滤 | 业务（EmployeeSelector / DutyAssignment / DutyStatisticsServiceImpl）|
  | `sysDeptService.getActiveDeptTree()` | ✅ 过滤 | 业务（PersonSelector.vue 选部门）|

- **统一入口（推荐使用，不要直接 `lambdaQuery().list()`）**：
  ```java
  // ✅ 系统管理（看全量）
  sysDeptService.getAllDepts();
  sysDeptService.getDeptTree();

  // ✅ 业务模块（只查启用）
  sysDeptService.getActiveDepts();
  sysDeptService.getActiveDeptTree();

  // ❌ 错误：直接 list() 拉全表
  sysDeptService.list();  // 包含禁用部门
  ```
- **前端对应**：
  - `api/dept.js` → `getDeptList` / `getDeptTree`（全量） + `getActiveDeptList` / `getActiveDeptTree`（启用）
  - 业务组件（`PersonSelector` / `EmployeeSelector` / `DutyAssignment`）**必须**用 `getActive*`
  - 系统管理页面（`Dept.vue` / `Employee.vue`）用 `getDeptList` / `getDeptTree` 即可
- **缓存一致性**：部门状态变更时，`SysDeptServiceImpl.clearAllDeptCache()` 已清掉 4 个 cache key（`dept::allDepts` + `dept::deptTree` + `dept::activeDepts` + `dept::activeDeptTree`）✅（2026-06-27 验证）
- **不联动 employee 查询（A 方案）**：员工被禁过滤自己（status=1），员工所在部门被禁**不过滤**（避免引入 JOIN 慢查询 + 避免管理员误禁部门后无法通过 dept_id 找回员工）
- **新功能自检清单**（提交前必过）：
  1. 这次新增的部门查询调用了 4 个接口中的哪一个？
  2. 系统管理 / 业务场景选择对了吗？（系统管理 → `getAll*`；业务 → `getActive*`）
  3. 前端 `PersonSelector` / 部门下拉框是否用了 `getActive*`？
  4. 部门更新 / 删除时是否清掉了所有 4 个部门 cache？
- **参考实现**：`SysDeptServiceImpl.getAllDepts` / `getDeptTree` / `getActiveDepts` / `getActiveDeptTree` + `DutyStatisticsServiceImpl.getDeptStatistics`

---

## 🎓 任务分页算法 5 版本对照（2026-06-24 整理）

| 版本 | total 算法 | 适用 |
|------|----------|------|
| **5月17日原版** | `realTotal` 准 | ❌ 大项目有空页（48→5 页 p5 空）|
| **5月22日原版** | `pages × pageSize` 凑整 | ❌ 小项目凑整 +9（11→20）|
| **6月22日 f670c8d**（性能优化版）| `realTotal` | ❌ pageNum 边界 bug |
| **6月24日改坏版**（按需分批查）| `realTotal` | ❌ 网络 RTT 累积 + 边界 bug |
| **6月24日 v3（当前生产）**| **智能 total**（小项目 realTotal，大项目 pages×pageSize）| ✅ **所有场景都不空** |

**v3 终极方案**（2026-06-24）：
```java
int realTotal = currentIndex;
int pagesSize = pagesRootTasks.size();
int adjustedTotal = (realTotal >= pagesSize * pageSize)
    ? pagesSize * pageSize    // 大项目：凑整 -X（少算）
    : realTotal;              // 小项目：不凑整
page.setTotal(adjustedTotal);
```

**v3 验证矩阵**：

| 场景 | pages | realTotal | 凑整 | total | el-pagination 算 | p_last 行数 |
|------|-------|-----------|------|-------|------------------|------------|
| 1 根 0 子 | 1 | 1 | 不凑整 | **1** | 1 页 | p1=1 ✅ |
| 11 根 0 子 | 2 | 11 | 不凑整 | **11** | 2 页 | p2=1 ✅ |
| 11 根 37 子 | 4 | 48 | 凑整 -8 | **40** | 4 页 | p4=4 ✅ |
| 100 行 10 大根 | 10 | 100 | 不凑整 | **100** | 10 页 | p10=10 ✅ |
| 105 行 11 大根 | 11 | 105 | 不凑整 | **105** | 11 页 | p11=5 ✅ |

**关键结论**：
1. **service 分页算法 = 5月17日"根不可切断"**（不要动）
2. **total = v3 智能判断**（小项目准，大项目凑整 -X）
3. **绝对不要"按需分批查"**（网络 RTT 累积 + 边界 bug）
4. **绝对不要"凑整 +X"**（多算用户会投诉）—— 只能"凑整 -X"或"不凑整"

**专题文档**：
- 完整修复过程：`docs/项目管理中心实现文档.md` → 最近更新 2026-06-24 v1 + v2 + v3
- 三大铁律 + 检查清单：`.trae/skills/project-module-developer/SKILL.md` → "任务分页踩坑专题"
- 用户原话："5月21号之前好好的"（5月17日原版 = 用户认可的金标准）

---

## 📌 快速开始

当你被调用时：
1. **分析用户需求**：判断属于哪种类型的任务
2. **推荐合适技能**：根据上面的技能映射表，推荐最合适的技能
3. **提供文档查阅指引**：告诉开发者去查什么文档（而不是直接在技能里写详细内容）
4. **指导问题排查**：如果是 bug 或问题，提供标准的排查流程
