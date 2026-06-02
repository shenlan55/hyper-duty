# ⚠️ 绝对优先！强制！先看这里！⚠️

## � 顶层强制指令（必须先看！）

### 1️⃣ 技能调用规范（第一优先级！）

**超级超级重要，强制执行！** 遇到**任何**开发任务或问题，**必须首先调用** `hyper-duty-navigator` 技能！

**不要跳过这个步骤！不要直接开始写代码！**

`hyper-duty-navigator` 会告诉你：
- 该调用哪个具体技能
- 相关的开发规范
- 问题排查流程

**不要在项目规则中查找开发规范**，所有具体规范都已分散到各个技能中。

#### 为什么必须先调用技能？
1. **有现成的规范**：每个模块都有专属技能，里面有完整的开发规范
2. **避免踩坑**：技能中有很多已经踩过的坑的解决方案
3. **保证一致性**：按照技能规范开发，代码风格和架构保持一致
4. **节约时间**：技能中有现成的最佳实践，不用从零开始

#### 什么时候必须调用技能？
- 新建功能时 → 先调用对应模块的技能
- 修改现有代码时 → 先调用对应模块的技能
- 排查问题时 → 先调用 hyper-duty-navigator
- 更新文档时 → 先调用 hyper-duty-documentation
- 重构代码时 → 先调用 hyper-duty-refactorer

#### 技能调用流程
```
用户需求 → hyper-duty-navigator（必须！）→ 对应模块技能 → 开始开发
         ↑
       必须先到这里！
```

---

## ��️ 技术栈强制约束 (Mandatory Tech Stack)
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
