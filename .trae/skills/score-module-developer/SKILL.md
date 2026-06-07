---
name: "score-module-developer"
description: "积分管理模块开发专家，负责开发和维护积分事件、积分记录、月度汇总、评选排名等积分管理功能。当用户提到积分、积分事件、积分记录、月度汇总、评选排名、评分、综合评分、PersonSelector等相关需求时，调用此技能。"
---

# 积分管理模块开发专家

这个技能专门用于开发和维护 Hyper Duty 系统的积分管理模块，包括积分事件管理、积分记录录入、月度汇总生成和评选排名等功能。

## 🎯 核心职责

- **积分事件管理**：维护加分项/扣分项模板（如"团队赋能分享"、"抽检不合格"），支持 CRUD 和启禁用
- **积分记录录入**：管理员通过 PersonSelector 选择员工，关联积分事件录入加减分记录
- **月度汇总**：自动汇总当月积分 + 加班工时 → 综合评分，支持手动触发生成
- **评选排名**：按季度/年度汇总各月数据，按综合评分降序排名
- **工时对接**：从值班管理模块 `duty_record` 表读取加班工时数据
- **权重配置**：积分权重和工时权重可在周期配置中调整

## 📋 评分公式

```
月度综合评分 = 当月积分合计 × 积分权重(0.6) + 当月工时 × 工时权重(0.4)
```

- 积分合计：当月所有加分 + 扣分（扣分为负值）
- 当月工时：从 `duty_record` 表按员工+月份汇总 `overtime_hours`
- 权重可在 `score_period_config` 表中调整

## 📋 文档查阅指南

| 规范/问题类型 | 查阅文档 | 文档位置 |
|-------------|---------|---------|
| **模块设计文档** | 员工积分管理模块设计 | `docs/2026-06-04-员工积分管理模块设计.md` |
| **数据库表结构** | 员工积分管理模块设计 | `docs/2026-06-04-员工积分管理模块设计.md` |
| **API接口设计** | 员工积分管理模块设计 | `docs/2026-06-04-员工积分管理模块设计.md` |
| **评分公式** | 员工积分管理模块设计 | `docs/2026-06-04-员工积分管理模块设计.md` |
| **工时数据对接** | 员工积分管理模块设计 | `docs/2026-06-04-员工积分管理模块设计.md` |
| **PersonSelector 使用** | 员工积分管理模块设计 | `docs/2026-06-04-员工积分管理模块设计.md` |
| **菜单与权限初始化** | SQL 脚本 | `src/main/resources/sql/hyper_duty_score_dml.sql` |
| **项目架构与技术栈** | 项目规则 | `.trae/rules/project_rules.md` |

## 🔧 关键文件和组件

### 后端模块
```
src/main/java/com/lasu/hyperduty/score/
├── controller/
│   ├── ScoreEventController.java      # 积分事件 CRUD
│   ├── ScoreRecordController.java     # 积分记录 CRUD
│   └── ScoreSummaryController.java    # 汇总与评选
├── entity/
│   ├── ScoreEvent.java
│   ├── ScoreRecord.java
│   ├── ScorePeriodConfig.java
│   └── ScoreSummary.java
├── mapper/
│   ├── ScoreEventMapper.java
│   ├── ScoreRecordMapper.java
│   ├── ScorePeriodConfigMapper.java
│   └── ScoreSummaryMapper.java
└── service/
    ├── ScoreEventService.java / impl/
    ├── ScoreRecordService.java / impl/
    └── ScoreSummaryService.java / impl/
```

### 前端模块
```
frontend/src/views/score/
├── ScoreLayout.vue        # 积分管理布局（简单路由容器，使用全局菜单）
├── ScoreEvent.vue         # 积分事件管理页
├── ScoreRecord.vue        # 积分记录录入与查看页（员工选择使用 PersonSelector）
├── ScoreSummary.vue       # 月度汇总排名页
└── ScoreEvaluation.vue    # 季度/年度评选页

frontend/src/api/score/
└── index.js               # 积分模块 API 封装
```

### 数据库表
| 表名 | 说明 |
|------|------|
| `score_event` | 积分事件定义表 |
| `score_record` | 积分记录表 |
| `score_period_config` | 评选周期配置表 |
| `score_summary` | 积分月度汇总表 |

## 💡 开发规范

### 前端规范
- **员工选择**：统一使用 `@/components/PersonSelector.vue` 通用选人组件，不得使用 `el-select` 下拉框
  - 使用模式：只读 `el-input` + 点击弹出 PersonSelector 对话框
  - 参考实现：`ScoreRecord.vue` 中的筛选区和录入对话框
- **菜单**：使用全局菜单，模块布局组件（ScoreLayout.vue）仅作 `<router-view />` 容器
- **下拉框**：所有 `el-select` 必须设置明确的 `style="width: xxxpx"` 避免展示不全
- **数值格式化**：综合评分使用 `Number(value).toFixed(2)` 保留两位小数
- **卡片头部**：使用 `.card-header` 样式（flex + space-between）确保标题和操作按钮左右排列

### 后端规范
- **Mapper 扫描**：新模块的 Mapper 包路径必须添加到 `HyperDutyApplication.java` 的 `@MapperScan` 注解中
- **响应格式**：统一使用 `ResponseResult` 包装，前端拦截器自动解包 `data.data`
- **分页查询**：使用 `PageResult` 封装分页数据（`total` + `data`）
- **关联数据填充**：在 Controller 层查询关联数据（如员工姓名、事件名称）填充到返回对象中

### 数据库规范
- SQL 脚本统一放在 `src/main/resources/sql/` 目录
- 表结构：`hyper_duty_score_ddl.sql`
- 初始数据（菜单、权限）：`hyper_duty_score_dml.sql`
- 使用 `ON CONFLICT (id) DO NOTHING` 防止重复执行报错

## 🐛 常见问题

| 问题类型 | 解决方案 |
|---------|---------|
| **Mapper 未扫描** | 在 `HyperDutyApplication.java` 的 `@MapperScan` 中添加 `"com.lasu.hyperduty.score.mapper"` |
| **菜单不显示** | 执行 `hyper_duty_score_dml.sql` 初始化菜单和权限数据，重新登录 |
| **下拉框宽度不够** | 给 `el-select` 添加 `style="width: 160px"` 等明确宽度 |
| **综合评分不显示小数** | 使用 `Number(value).toFixed(2)` 格式化 |
| **PersonSelector 弹窗层级问题** | PersonSelector 对话框放在对应页面模板中，不要嵌套在其他对话框内 |

## 📝 文档同步更新

每次修改积分管理模块后，必须同步更新 `docs/2026-06-04-员工积分管理模块设计.md`。

---

## 🚀 快速开始

当你被调用进行积分管理模块开发时：
1. **确认需求类型**：明确是积分事件、积分记录、月度汇总还是评选排名
2. **查阅设计文档**：先看 `docs/2026-06-04-员工积分管理模块设计.md` 了解整体设计
3. **遵循开发规范**：按本技能中的规范进行开发
4. **参考现有实现**：查看 `ScoreRecord.vue` 了解 PersonSelector 使用方式
5. **保持沟通**：每个阶段完成后与用户确认后再继续