## 🛡️ 技术栈强制约束 (Mandatory Tech Stack)
1.  **基础技术栈锁定**：必须严格遵循以下技术栈开展开发：
    `Spring Web + Spring Security + SpringBoot3 + MyBatis + Druid + Knife4j + Lombok + Vue3 + Vue Router + Pinia + Axios + Element Plus`
2.  **同类框架禁止**：禁止引入与现有技术栈功能重叠的同类框架，例如：
    - 已使用 MyBatis → 禁止引入 JPA/Hibernate 等其他持久层框架
    - 已使用 Element Plus → 禁止引入 Ant Design Vue 等其他 UI 框架
3.  **工具类库允许**：允许引入不与现有技术栈功能重叠的工具类库，例如：
    - 日期处理：`dayjs`
    - 数据可视化：`echarts`
    - 身份令牌：`JWT`（用于生成和验证登录令牌）
    - 文档处理：`Apache POI`（用于生成 Excel/Word 等文件）
    - 这类工具属于功能补充，不视为违规依赖

## 🧰 通用礼节 (General Etiquette)
- SQL 脚本统一归集：所有 SQL 语句合并到 `hyper_duty_ddl.sql`（结构类）和 `hyper_duty_dml.sql`（数据类），禁止零散创建独立 SQL 文件。
- 文档同步更新：
  - 平台底座变更：同步更新 `平台开发手册.md` 和 `平台需求说明书.md`
  - 值班管理模块变更：同步修订 `值班管理中心实现文档.md`