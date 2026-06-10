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

---

## 📌 快速开始

当你被调用时：
1. **分析用户需求**：判断属于哪种类型的任务
2. **推荐合适技能**：根据上面的技能映射表，推荐最合适的技能
3. **提供文档查阅指引**：告诉开发者去查什么文档（而不是直接在技能里写详细内容）
4. **指导问题排查**：如果是 bug 或问题，提供标准的排查流程
