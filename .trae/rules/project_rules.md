## 🛡️ 技术栈强制约束 (Mandatory Tech Stack)
1.  **基础技术栈锁定**：必须严格遵循以下技术栈开展开发：
    `Spring Web + Spring Security + SpringBoot3 + MyBatis + Druid + Knife4j + Lombok + Vue3 + Vue Router + Pinia + Axios + Element Plus + PostgreSQL`
2.  **同类框架禁止**：禁止引入与现有技术栈功能重叠的同类框架，例如：
    - 已使用 MyBatis → 禁止引入 JPA/Hibernate 等其他持久层框架
    - 已使用 Element Plus → 禁止引入 Ant Design Vue 等其他 UI 框架
    - 已使用 PostgreSQL → 禁止使用 MySQL/MariaDB 等其他数据库
3.  **工具类库允许**：允许引入不与现有技术栈功能重叠的工具类库，例如：
    - 日期处理：`dayjs`
    - 数据可视化：`echarts`
    - 身份令牌：`JWT`（用于生成和验证登录令牌）
    - 文档处理：`Apache POI`（用于生成 Excel/Word 等文件）
    - 这类工具属于功能补充，不视为违规依赖

## 🏗️ 项目模块架构规范 (Module Architecture)
### 后端模块结构
```
com.lasu.hyperduty/
├── common/              # 公共模块（全局通用的配置、工具、异常等）
│   ├── config/         # 配置类
│   ├── annotation/     # 自定义注解
│   ├── aspect/         # AOP切面
│   ├── security/       # 安全相关
│   ├── utils/          # 工具类
│   ├── dto/            # 公共DTO
│   ├── exception/      # 异常处理
│   └── service/        # 公共服务
├── duty/               # 值班管理模块
├── pm/                 # 项目管理模块
├── workflow/           # 工作流模块
├── ai/                 # AI功能模块
├── system/             # 系统管理模块
└── HyperDutyApplication.java
```

### 单个业务模块的内部结构
```
{模块名}/
├── controller/         # 控制器层
├── service/            # 服务层
│   └── impl/           # 服务实现
├── mapper/             # MyBatis Mapper
├── entity/             # 实体类
├── dto/                # 数据传输对象
└── [可选子目录]/      # 特定业务需要的其他目录
    ├── algorithm/      # 算法相关
    ├── task/           # 定时任务
    └── enums/          # 枚举类
```

### 前端模块结构
```
src/
├── views/              # 页面
│   ├── duty/           # 值班管理
│   ├── pm/             # 项目管理
│   ├── workflow/       # 工作流
│   └── [其他模块]/
├── components/         # 公共组件
├── api/                # API接口
│   ├── duty/
│   ├── pm/
│   ├── workflow/
│   └── [其他模块]/
├── stores/             # Pinia状态管理
├── router/             # 路由配置
└── utils/              # 工具函数
```

### 新增模块规范
1. **创建独立模块目录**：在 `com.lasu.hyperduty` 下创建新的模块目录，与 `duty`、`pm` 同级
2. **遵循分层架构**：严格按照 `controller` → `service` → `mapper` → `entity` 分层
3. **禁止与 common 混合**：业务模块的代码必须放在自己的目录下，禁止零散放在 `common` 目录
4. **前端对应**：在前端相应目录创建对应的视图、API和组件
5. **SQL文件**：新增模块的表结构单独创建 SQL 文件，如 `hyper_duty_{模块名}_ddl.sql`

### 模块命名规范
- 后端模块：使用小写英文单词，如 `workflow`、`duty`、`pm`
- 前端目录：与后端模块名保持一致
- 表名前缀：使用模块名作为表前缀，如 `wf_`（workflow）、`duty_`、`pm_`

## 🐘 PostgreSQL 数据库规范
### SQL 语法要求
- 所有 SQL 脚本必须使用 **PostgreSQL 语法**，禁止使用 MySQL 语法
- Schema 使用 `public`（默认）
- 自增主键使用 `BIGSERIAL`
- 日期时间使用 `TIMESTAMP`
- 短整型使用 `SMALLINT`
- 文本大字段使用 `TEXT`
- 索引使用 `CREATE INDEX IF NOT EXISTS`
- 注释使用 `COMMENT ON TABLE` 和 `COMMENT ON COLUMN`

### SQL 脚本管理
- 表结构脚本：`hyper_duty_{模块名}_ddl.sql`
- 数据脚本：`hyper_duty_{模块名}_dml.sql`
- 禁止零散创建 SQL 文件

### 示例 PostgreSQL 表结构
```sql
CREATE TABLE IF NOT EXISTS public.wf_category (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    create_time TIMESTAMP,
    CONSTRAINT uk_category_code UNIQUE (category_code)
);
COMMENT ON TABLE public.wf_category IS '流程分类表';
COMMENT ON COLUMN public.wf_category.id IS '主键ID';

CREATE INDEX IF NOT EXISTS idx_category_code ON public.wf_category(category_code);
```

# 🧰 通用礼节 (General Etiquette)
- SQL 脚本统一归集：所有 SQL 语句合并到对应模块的 `hyper_duty_{模块名}_ddl.sql`（结构类）和 `hyper_duty_{模块名}_dml.sql`（数据类），禁止零散创建独立 SQL 文件。
- SQL执行：所有涉及要执行的 SQL 语句，MCP 没有执行权限时，输出给人工去执行
- 文档同步更新：
  - 平台底座变更：同步更新 `平台开发手册.md` 和 `平台需求说明书.md`
  - 值班管理模块变更：同步修订 `值班管理中心实现文档.md`
  - 项目管理模块变更：同步修订 `项目管理中心实现文档.md`
- **数据获取规范**：
  - 前端应从后端 API 获取业务数据，禁止在前端硬编码业务数据
  - 对于需要在多个地方使用的数据（如班次配置、用户信息等），应通过 API 统一获取并缓存
  - 用户身份信息应从 JWT 令牌中解析获取，禁止硬编码用户信息
  - 常量配置应集中管理，优先从后端配置中心获取
- **问题排查规范**：
  - **主动使用MCP工具**：在排查问题时，必须主动使用以下MCP工具获取信息：
    - **PostgreSQL MCP**：查询数据库结构和数据，验证数据一致性
    - **Chrome DevTools MCP**：查看浏览器控制台错误、网络请求和页面状态
  - **系统访问信息**：
    - **前端访问地址**：http://localhost:5173
    - **登录账号**：wangguohao
    - **登录密码**：123456
  - **排查流程**：
    - 前端问题：先使用Chrome DevTools查看控制台错误和网络请求
    - 后端问题：先使用PostgreSQL查询相关数据和表结构
    - 数据一致性问题：同时使用两种MCP工具验证前后端数据
    - **信息收集**：在开始修复前，必须收集以下信息：
        - 浏览器控制台错误信息
        - 相关API的网络请求和响应
        - 数据库相关表结构和数据
        - 相关代码文件的最新版本
    - **使用示例**：
        - 示例1：当遇到前端API调用失败时，使用Chrome DevTools查看网络请求的详细信息
        - 示例2：当遇到后端数据查询问题时，使用PostgreSQL MCP执行SQL查询验证数据
        - 示例3：当遇到分页问题时，同时使用两种MCP工具验证前后端分页逻辑
- **代码规范**：
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
  - 类型引用规范：所有类型引用必须通过 import 语句导入后使用简单类型名，禁止直接使用完整包路径。例如：
    - 正确：`import com.baomidou.mybatisplus.extension.plugins.pagination.Page;` 然后使用 `Page<SysEmployee>`
    - 错误：直接使用 `com.baomidou.mybatisplus.extension.plugins.pagination.Page<SysEmployee>`
  - **分页实现规范**：
    - **前端分页规范**：
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
    - **后端分页规范**：
      - 使用 MyBatis-Plus 的 `Page` 对象实现分页查询
      - 继承 `BasePageService` 接口实现通用分页功能
      - 统一的分页响应格式，包含 records、total、current、size、pages 字段
      - 支持多条件筛选的分页查询，使用 LambdaQueryWrapper 构建查询条件
      - 正确处理关联查询的分页逻辑，避免子查询导致的性能问题
      - 按创建时间或相关字段倒序排序，确保最新数据优先显示
    - **前后端联调规范**：
      - 确保分页参数传递正确，前端传递 pageNum 和 pageSize
      - 验证 total 字段的一致性，前端显示应与后端返回一致
      - 测试不同分页大小的场景，确保数据显示正确
      - 检查边界情况（如第一页、最后一页）的分页控件状态
      - 使用 Chrome DevTools MCP 验证网络请求和控制台日志
      - 使用 PostgreSQL MCP 验证数据库查询结果的准确性
- **缓存管理规范**：
  - 缓存键设计：使用 `{模块}::{键名}_{参数}` 的格式，确保缓存键的唯一性和可读性
  - 缓存清除机制：
    - 简单场景：使用 `@CacheEvict` 注解自动清除缓存
    - 复杂场景：继承 `CacheableServiceImpl` 基类，实现 `clearCache` 方法
    - 特殊场景：使用 `CacheUtil` 工具类手动清除缓存
  - 序列化配置：确保 Redis 序列化配置正确，支持 Java 8 日期时间类型的序列化和反序列化
  - 缓存监控：定期检查 Redis 缓存使用情况，避免缓存膨胀
  - 缓存一致性：在添加、更新或删除数据时，必须确保对应的缓存能够被及时清除，避免数据不一致
- **API 调用规范**：
  - 前端调用 API 时，直接使用返回的数据，不再检查 `response.code`
  - 示例：`const data = await apiFunction(); data.value = data || [];`
  - 确保所有组件遵循统一的 API 调用模式，提高代码一致性和可维护性
- **开发流程规范**：
  - 修改代码前应查阅相关文档，了解业务逻辑和数据结构
  - 前后端联调时应确保数据格式一致，避免类型转换错误
  - 测试时应覆盖正常场景、异常场景和边界场景
  - 提交代码前应进行代码审查，确保符合规范
- **数据库操作规范**：
  - 数据库操作应通过MCP（PostgreSQL Connection Pool）实现，确保连接管理和事务处理的一致性
  - 后端逻辑应基于实际数据库结构和数据进行分析，避免想当然的代码编写
  - 开发前应通过SQL查询验证数据结构和关系，确保代码与数据库实际情况匹配
  - 涉及复杂数据库操作时，应先编写和测试SQL语句，再集成到代码中

- **技能自动调用规范**：
  - **核心原则**：当收到用户请求时，必须首先分析请求的语境，选择最适合的技能进行调用
  - **值班管理模块任务**：当用户提及"排班"、"请假"、"调班"、"值班统计"、"节假日"等关键词时，优先调用 `duty-module-developer` 技能
  - **项目管理模块任务**：当用户提及"项目"、"任务"、"批量新建任务"、"批量更新进度"、"甘特图"、"任务统计"等关键词时，优先调用 `project-module-developer` 技能
  - **工作流模块任务**：当用户提及"流程"、"审批"、"会签"、"表单"、"任务转办"、"流程跟踪"、"委托"等关键词时，优先调用 `workflow-module-developer` 技能（如果可用）
  - **全栈开发任务**：当用户需要同时修改前端和后端、或没有明确模块指定时，优先调用 `hyper-duty-fullstack` 技能
  - **代码重构任务**：当用户提及"重构"、"优化代码结构"、"拆分组件"、"DTO/VO"、"API版本控制"等关键词时，优先调用 `hyper-duty-refactorer` 技能
  - **前端设计任务**：当用户需要设计新界面、优化UI/UX时，调用 `frontend-design` 或 `ui-ux-pro-max` 技能
  - **代码审查任务**：当用户需要审查代码时，调用 `code-review` 或 `frontend-code-review` 技能
  - **创意/设计任务**：在开始任何创新性工作前，先调用 `brainstorming` 技能
  - **SRE项目报告任务**：当用户需要生成SRE项目日报或周报时，分别调用 `SRE_Project_Dimension_Report` 或 `SRE_Project_Weekly_Report` 技能
  - **技能创建/查找任务**：当用户需要创建新技能或查找现有技能时，调用 `skill-creator` 或 `find-skills` 技能
  - **交互设计任务**：当用户需要优化微交互、动效设计时，调用 `interaction-design` 技能
  - **技能调用优先级**：
    1. 模块专属技能（duty-module-developer、project-module-developer、workflow-module-developer）
    2. 重构技能（hyper-duty-refactorer）
    3. 全栈开发技能（hyper-duty-fullstack）
    4. 通用技能（frontend-design、code-review、brainstorming等）
  - **调用前验证**：在调用技能前，确认技能确实与用户需求匹配
  - **多技能组合**：对于复杂任务，可以组合调用多个技能，但需确保调用顺序合理
  - **技能使用反馈**：每次使用技能后，评估其效果并在必要时调整调用策略