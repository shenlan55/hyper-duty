---
name: "hyper-duty-navigator"
description: "Hyper Duty 项目导航专家，分析用户需求，推荐最合适的技能，并提供通用开发规范和问题排查流程。当用户提到导航、通用规范、问题排查、分页、缓存、移动端适配、XSS防护等通用开发问题时，调用此技能。"
---

# Hyper Duty 项目导航专家

这个技能是 Hyper Duty 项目的"入口技能"，负责：
1. **分析用户需求**：判断该调用哪个具体技能
2. **提供通用规范**：提供项目通用的开发规范
3. **指导问题排查**：提供标准的问题排查流程

## 🎯 技能自动调用规范

### 核心原则
当收到用户请求时，必须首先分析请求的语境，选择最适合的技能进行调用。

### 技能映射表

| 需求类型 | 关键词 | 推荐技能 |
|---------|-------|---------|
| **值班管理模块任务** | 排班、请假、调班、值班统计、节假日 | `duty-module-developer` |
| **项目管理模块任务** | 项目、任务、批量新建任务、批量更新进度、甘特图、任务统计 | `project-module-developer` |
| **工作流模块任务** | 流程、审批、会签、表单、任务转办、流程跟踪、委托 | `workflow-module-developer`（如果可用） |
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

## 📋 通用开发规范

### SQL 脚本管理
- **SQL 脚本统一归集**：所有 SQL 语句合并到对应模块的 `hyper_duty_{模块名}_ddl.sql`（结构类）和 `hyper_duty_{模块名}_dml.sql`（数据类），禁止零散创建独立 SQL 文件。
- **SQL 执行**：所有涉及要执行的 SQL 语句，MCP 没有执行权限时，输出给人工去执行。

### 文档同步更新
- 平台底座变更：同步更新 `平台开发手册.md` 和 `平台需求说明书.md`
- 值班管理模块变更：同步修订 `值班管理中心实现文档.md`
- 项目管理模块变更：同步修订 `项目管理中心实现文档.md`

### 数据获取规范
- 前端应从后端 API 获取业务数据，禁止在前端硬编码业务数据
- 对于需要在多个地方使用的数据（如班次配置、用户信息等），应通过 API 统一获取并缓存
- 用户身份信息应从 JWT 令牌中解析获取，禁止硬编码用户信息
- 常量配置应集中管理，优先从后端配置中心获取

### 员工姓名获取规范（重要！）
- **绝对禁止**使用 `getEmployeeList(1, 1000)` 或类似方式获取所有员工
- **优先策略**：
  1. 先检查 `userStore` 或 `localStorage` 获取当前登录用户的 employee_id 和 employee_name
  2. 如果是当前用户，直接返回 userStore 中的姓名
  3. 如果不是当前用户，再从 employeeList 中查找（保持正常的分页查询）
- **代码示例**：
  ```javascript
  const getEmployeeName = (employeeId) => {
    if (!employeeId) return '未知人员';
    const targetId = parseInt(employeeId) || 0;
    // 优先检查是否是当前登录用户
    if (targetId === userStore.employeeId && userStore.employeeName) {
      return userStore.employeeName;
    }
    // 再从列表中查找
    const employee = employeeList.value.find(e => parseInt(e.id) === targetId);
    return employee ? employee.employeeName : '未知人员';
  };
  ```

### 代码规范
- 前端组件应通过 props 接收数据，通过 emit 事件传递操作
- 后端 API 返回数据应包含 code、message 和 data 字段，前端统一处理
- 前端 API 响应处理：使用响应拦截器模式，成功响应时直接返回 `data.data` 部分，简化前端代码
- 错误处理应统一，避免在每个 API 调用处重复处理
- 日志记录应完整，包括关键操作、错误信息和性能指标
- 前端表格应使用 `BaseTable` 组件，统一表格样式和功能
- 前端长列表应使用 `VirtualList` 组件，优化性能
- 前端处理用户输入时应使用 `xssUtil` 工具，防止 XSS 攻击
- 后端热点数据应使用 `@Cacheable` 注解，利用 Redis 缓存提升性能
- 后端敏感接口应使用 `@RateLimit` 注解，防止恶意请求和 DDoS 攻击
- 前端附件显示规范：附件应采用垂直方向逐行显示，提高可读性和一致性
- 类型引用规范：所有类型引用必须通过 import 语句导入后使用简单类型名，禁止直接使用完整包路径。

### 移动端适配规范
#### 按钮对齐规范
- **全局按钮对齐**：所有移动端按钮使用 `display: inline-flex` + `align-items: center` + `justify-content: center` + `vertical-align: middle` 确保完美垂直对齐
- **Element Plus按钮覆盖**：在 `mobile.scss` 中统一覆盖 `.el-button` 样式，避免按钮高低不一
- **特殊场景按钮**：
  - 表格内按钮、对话框按钮、附件列表按钮单独优化对齐
  - 日历组件按钮使用 `display: inline-flex` + 统一高度确保对齐

#### 卡片显示规范
- **隐藏数字字段**：
  - 自动排除 `id`、`sort`、`sortOrder`、`jobId`、`employeeId`、`deptId`、`roleId`、`dictId`、`menuId`、`userId` 等数字字段
  - 排除所有 `prop.toLowerCase().endsWith('id')` 的字段
  - 在 `mobile.scss` 中使用 CSS 双重保障隐藏
- **卡片标题智能选择**：
  - 优先选择名称类字段：`name`、`title`、`jobName`、`employeeName`、`deptName`、`userName`、`roleName`、`dictName`、`menuName`
  - 其次选择编码类字段：`code`、`jobCode`
  - 最后选择非 ID/排序的字段
  - 实在找不到才使用 `id`

#### 触摸交互优化
- **移除蓝色闪烁**：全局添加 `-webkit-tap-highlight-color: transparent`
- **卡片点击反馈**：添加平滑缩放动画 `transform: scale(0.985)`，背景色轻微变化 `background-color: #f8f9fb`，阴影柔化，营造自然的键入感
- **过渡动画**：所有变化添加 `transition: transform 0.12s ease, background-color 0.12s ease, box-shadow 0.12s ease`

#### 日历组件优化
- **日历标题栏**：使用 `display: flex` + `justify-content: space-between` + `align-items: center` 确保按钮和标题对齐
- **日历按钮组**：
  - 使用 `display: flex` + `flex-wrap: nowrap` + `gap: 4px` + `align-items: center`
  - 按钮统一高度 `height: 28px` + `min-height: 28px`
  - 按钮内部内容居中对齐

#### 移动端样式隔离
- 所有移动端样式严格限制在 `@media (max-width: 767px)` 内
- 移动端样式文件统一放在 `styles/mobile.scss`
- 使用 `!important` 确保移动端样式优先级（仅在必要时）

### 分页实现规范

#### 前端分页规范
- 使用 `useSearchPagination` hook 管理分页状态（currentPage、pageSize、total）
- BaseTable 组件的 pagination 属性配置：
  ```vue
  :pagination="{
    currentPage: pagination.currentPage,
    pageSize: pagination.pageSize,
    pageSizes: pagination.pageSizes,
    total: pagination.total
  }"
  ```
- 模板中正确绑定分页数据，使用 `pagination.total` 而非 `total.value`
- 监听分页变化事件（size-change、current-change）并重新加载数据
- 处理后端返回的分页格式数据，确保正确更新 total 值
- 页面初始化时，在设置默认筛选条件后自动加载数据

#### 后端分页规范
- 使用 MyBatis-Plus 的 `Page` 对象实现分页查询
- 继承 `BasePageService` 接口实现通用分页功能
- 统一的分页响应格式，包含 records、total、current、size、pages 字段
- 支持多条件筛选的分页查询，使用 LambdaQueryWrapper 构建查询条件
- 正确处理关联查询的分页逻辑，避免子查询导致的性能问题
- 按创建时间或相关字段倒序排序，确保最新数据优先显示

#### 前后端联调规范
- 确保分页参数传递正确，前端传递 pageNum 和 pageSize
- 验证 total 字段的一致性，前端显示应与后端返回一致
- 测试不同分页大小的场景，确保数据显示正确
- 检查边界情况（如第一页、最后一页）的分页控件状态
- 使用 Chrome DevTools MCP 验证网络请求和控制台日志
- 使用 PostgreSQL MCP 验证数据库查询结果的准确性

### 缓存管理规范
- **缓存键设计**：使用 `{模块}::{键名}_{参数}` 的格式，确保缓存键的唯一性和可读性
- **缓存清除机制**：
  - 简单场景：使用 `@CacheEvict` 注解自动清除缓存
  - 复杂场景：继承 `CacheableServiceImpl` 基类，实现 `clearCache` 方法
  - 特殊场景：使用 `CacheUtil` 工具类手动清除缓存
- **序列化配置**：确保 Redis 序列化配置正确，支持 Java 8 日期时间类型的序列化和反序列化
- **缓存监控**：定期检查 Redis 缓存使用情况，避免缓存膨胀
- **缓存一致性**：在添加、更新或删除数据时，必须确保对应的缓存能够被及时清除，避免数据不一致

### API 调用规范
- 前端调用 API 时，直接使用返回的数据，不再检查 `response.code`
- 示例：`const data = await apiFunction(); data.value = data || [];`
- 确保所有组件遵循统一的 API 调用模式，提高代码一致性和可维护性

### 开发流程规范
- 修改代码前应查阅相关文档，了解业务逻辑和数据结构
- 前后端联调时应确保数据格式一致，避免类型转换错误
- 测试时应覆盖正常场景、异常场景和边界场景
- 提交代码前应进行代码审查，确保符合规范

### 数据库操作规范
- 数据库操作应通过 MCP（PostgreSQL Connection Pool）实现，确保连接管理和事务处理的一致性
- 后端逻辑应基于实际数据库结构和数据进行分析，避免想当然的代码编写
- 开发前应通过 SQL 查询验证数据结构和关系，确保代码与数据库实际情况匹配
- 涉及复杂数据库操作时，应先编写和测试 SQL 语句，再集成到代码中

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

### 使用示例
- **示例1**：当遇到前端 API 调用失败时，使用 Chrome DevTools 查看网络请求的详细信息
- **示例2**：当遇到后端数据查询问题时，使用 PostgreSQL MCP 执行 SQL 查询验证数据
- **示例3**：当遇到分页问题时，同时使用两种 MCP 工具验证前后端分页逻辑

## 📌 快速开始

当你被调用时：
1. **分析用户需求**：判断属于哪种类型的任务
2. **推荐合适技能**：根据上面的技能映射表，推荐最合适的技能
3. **提供相关规范**：如果是常见问题，提供相关的开发规范
4. **指导问题排查**：如果是 bug 或问题，提供标准的排查流程

让我们一起高效开发！🚀
