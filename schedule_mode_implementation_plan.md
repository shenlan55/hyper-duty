# 排班模式管理功能实现方案

## 1. 需求分析

根据用户需求，排班模式管理功能需要实现以下内容：

1. **排班模式管理**：支持新增、编辑、查看、删除排班模式
2. **排班模式编排界面**：
   - 横头是天，可以拓展第一天、第二天、第三天
   - 每个天下面一行可以选择班次配置里面启用状态的班次
   - 每天班次也可以横向追加
   - 再下一行可以对应班次设置班次人数
3. **批量排班功能**：从下拉菜单读取排班模式

## 2. 现有代码分析

### 2.1 后端现有代码

- **实体类**：`DutyScheduleMode.java` - 已存在，包含所有必要字段
- **控制器**：`DutyScheduleModeController.java` - 已存在，但只实现了查询接口
- **服务层**：`DutyScheduleModeServiceImpl.java` - 已存在，实现了基本的查询和算法实例化功能
- **Mapper**：`DutyScheduleModeMapper.java` - 已存在，基于MyBatis-Plus
- **数据库表**：`duty_schedule_mode` - 已存在，结构完整

### 2.2 前端现有代码

- 暂未找到排班模式管理相关页面
- 批量排班功能可能需要修改，以支持从下拉菜单读取排班模式

## 3. 实现方案

### 3.1 后端API扩展

需要扩展 `DutyScheduleModeController`，添加以下接口：

1. **新增排班模式**：`POST /duty/schedule-mode`
2. **编辑排班模式**：`PUT /duty/schedule-mode`
3. **删除排班模式**：`DELETE /duty/schedule-mode/{id}`
4. **获取所有排班模式（包括禁用的）**：`GET /duty/schedule-mode/all`

### 3.2 前端页面实现

#### 3.2.1 排班模式管理页面

- **路由**：`/duty/schedule-mode`
- **组件**：`ScheduleMode.vue`
- **功能**：
  - 排班模式列表展示
  - 新增排班模式按钮
  - 编辑排班模式按钮
  - 删除排班模式按钮
  - 启用/禁用排班模式

#### 3.2.2 排班模式编排界面

- **组件**：`ScheduleModeEdit.vue`
- **功能**：
  - 基本信息填写（名称、编码、类型、描述等）
  - 天数配置（默认3天，可拓展）
  - 班次选择（从启用的班次配置中选择）
  - 班次人数设置
  - 配置参数JSON生成

#### 3.2.3 批量排班功能修改

- **修改文件**：`DutySchedule.vue`
- **功能**：
  - 添加排班模式下拉选择框
  - 从后端获取所有启用的排班模式
  - 根据选择的排班模式进行批量排班

## 4. 数据结构设计

### 4.1 排班模式配置JSON结构

```json
{
  "days": [
    {
      "dayIndex": 1,
      "name": "第一天",
      "shifts": [
        {
          "shiftId": 1,
          "shiftName": "早班",
          "count": 2
        },
        {
          "shiftId": 2,
          "shiftName": "晚班",
          "count": 1
        }
      ]
    },
    {
      "dayIndex": 2,
      "name": "第二天",
      "shifts": [
        {
          "shiftId": 3,
          "shiftName": "夜班",
          "count": 1
        }
      ]
    },
    {
      "dayIndex": 3,
      "name": "第三天",
      "shifts": [
        {
          "shiftId": 1,
          "shiftName": "早班",
          "count": 2
        }
      ]
    }
  ]
}
```

## 5. 实现步骤

### 5.1 后端实现

1. 扩展 `DutyScheduleModeController`，添加CRUD接口
2. 确保 `DutyScheduleModeServiceImpl` 实现所有必要的业务逻辑
3. 测试后端API接口

### 5.2 前端实现

1. 创建排班模式管理页面 `ScheduleMode.vue`
2. 创建排班模式编辑页面 `ScheduleModeEdit.vue`，实现编排界面
3. 修改 `DutySchedule.vue`，添加排班模式下拉选择
4. 添加相关API调用
5. 测试前端功能

## 6. 技术要点

### 6.1 后端技术要点

- 使用MyBatis-Plus实现CRUD操作
- 确保JSON配置参数的正确处理
- 保持与现有代码风格的一致性

### 6.2 前端技术要点

- 使用Element Plus组件库实现界面
- 实现动态添加天数和班次的功能
- 实现班次选择和人数设置的交互
- 确保数据绑定和表单验证

## 7. 测试计划

1. **后端API测试**：测试所有CRUD接口的正确性
2. **前端功能测试**：测试排班模式管理页面的所有功能
3. **集成测试**：测试批量排班功能与排班模式的集成
4. **边界测试**：测试拓展天数、添加班次等边界情况

## 8. 预期成果

1. 完整的排班模式管理功能
2. 直观易用的排班模式编排界面
3. 改进的批量排班功能，支持从下拉菜单选择排班模式
4. 符合用户需求的排班模式配置能力
