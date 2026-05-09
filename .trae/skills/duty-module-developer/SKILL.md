---
name: "duty-module-developer"
description: "值班管理模块开发专家，负责开发和维护排班、请假、调班、统计等值班管理功能。"
---

# 值班管理模块开发专家

这个技能专门用于开发和维护 Hyper Duty 系统的值班管理模块，包括排班、请假、调班和统计功能。

## 核心职责

- **排班管理**：实现和优化自动排班和手动排班功能
- **请假管理**：开发请假申请提交、审批和替班分配功能
- **调班管理**：实现调班申请和确认工作流
- **统计报表**：创建和维护值班统计和加班计算
- **节假日管理**：在排班逻辑中处理节假日识别
- **批量操作**：支持批量排班和批量更新
- **缓存管理**：为值班相关热点数据实现和维护 Redis 缓存

## 关键文件和组件

### 前端组件
```
frontend/src/views/duty/
├── DutyAssignment.vue                  # 值班排班
├── LeaveRequest.vue                    # 请假申请
├── LeaveApproval.vue                   # 请假审批
├── SwapRequest.vue                     # 调班管理
├── Statistics.vue                      # 值班统计
└── OperationLog.vue                    # 操作日志
```

### 后端服务
```
src/main/java/com/lasu/hyperduty/service/
├── DutyAssignmentService.java          # 值班排班管理
├── LeaveRequestService.java            # 请假申请处理
├── SwapRequestService.java             # 调班管理
├── DutyStatisticsService.java          # 值班统计
├── AutoScheduleService.java            # 自动排班
├── DutyHolidayService.java             # 节假日管理（带缓存）
├── DutyRecordService.java              # 加班记录（带缓存）
├── DutyScheduleModeService.java        # 排班模式（带缓存）
└── DutyShiftConfigService.java         # 班次配置（带缓存）
```

### 缓存管理基类
- `src/main/java/com/lasu/hyperduty/service/impl/CacheableServiceImpl.java` - 缓存管理基类
- `src/main/java/com/lasu/hyperduty/utils/CacheUtil.java` - 缓存操作工具类

### 控制器
```
src/main/java/com/lasu/hyperduty/controller/
├── DutyAssignmentController.java
├── LeaveRequestController.java
├── SwapRequestController.java
├── DutyStatisticsController.java
└── AutoScheduleController.java
```

## 开发规范

### 1. API 响应处理
使用响应拦截器模式，成功响应直接返回 `data.data`：
```javascript
const data = await apiFunction();
data.value = data || [];
```

### 2. 错误处理
在前端和后端都实现统一的错误处理

### 3. 数据验证
验证所有用户输入和 API 参数

### 4. 性能优化
优化数据库查询，避免 N+1 问题

### 5. 安全性
遵循 Spring Security 最佳实践和 JWT Token 管理

### 6. 缓存管理
- **缓存键设计**：值班相关缓存键使用 `duty::{key}_{params}` 格式
- **缓存清除机制**：
  - 简单场景：使用 @CacheEvict 注解
  - 复杂场景：继承 CacheableServiceImpl 并实现 clearCache 方法
  - 特殊场景：使用 CacheUtil 工具类
- **缓存一致性**：创建/更新/删除操作后始终清除相关缓存
- **缓存监控**：定期检查 Redis 缓存使用情况避免膨胀

## 常见问题和解决方案

1. **数据显示问题**：确保 API 调用遵循简化模式，不检查 `response.code`
2. **排班冲突**：在排班算法中实现适当的冲突检测
3. **请假替班分配**：确保员工请假时适当的替班分配
4. **调班审批**：为调班实现适当的审批工作流
5. **统计计算**：确保准确的加班和调休计算

## 测试指南

- 测试所有排班场景，包括节假日和周末
- 验证请假申请和审批工作流
- 使用不同用户角色测试调班功能
- 验证各种值班场景下的统计计算
- 确保大数据集下批量操作正常工作

## 值班管理流程示例

### 排班流程
1. **班次配置**：配置早班、中班、晚班等班次信息（使用 DutyShiftConfigService）
2. **排班模式设置**：设置轮班模式（使用 DutyScheduleModeService）
3. **自动排班**：使用 AutoScheduleService 进行自动排班
4. **手动调整**：在 DutyAssignment.vue 中进行手动调整
5. **保存排班**：保存最终的排班计划

### 请假流程
1. **员工申请**：在 LeaveRequest.vue 中提交请假申请
2. **选择替班**：选择或自动分配替班人员
3. **领导审批**：在 LeaveApproval.vue 中进行审批
4. **更新排班**：自动更新相关排班信息
5. **记录日志**：记录操作日志

### 调班流程
1. **发起调班**：在 SwapRequest.vue 中发起调班申请
2. **对方确认**：调班对象进行确认
3. **领导审批**：领导进行审批（如需要）
4. **更新排班**：自动更新双方的排班信息
5. **记录日志**：记录操作日志

## 统计功能要点

- 按日期范围统计值班次数
- 计算加班时长和加班费
- 统计请假和调班次数
- 生成值班统计报表
- 支持导出 Excel 格式

## 缓存管理实现示例

```java
@Service
public class DutyHolidayServiceImpl extends CacheableServiceImpl implements DutyHolidayService {

    @Override
    @Cacheable(value = "duty::holidays", key = "#year")
    public List<DutyHoliday> getHolidaysByYear(Integer year) {
        // 查询数据库获取指定年份的节假日
        return holidayMapper.selectByYear(year);
    }

    @Override
    @CacheEvict(value = "duty::holidays", allEntries = true)
    public void addHoliday(DutyHoliday holiday) {
        // 添加节假日
        holidayMapper.insert(holiday);
    }

    @Override
    protected void clearCache() {
        // 清除所有值班相关缓存
        CacheUtil.clearCacheByPattern("duty::*");
    }
}
```

## 项目规则参考

始终参考 `.trae/rules/project_rules.md`：
- 技术栈约束（仅使用指定技术栈）
- API 调用模式（简化响应处理）
- 缓存管理规范（值班模块使用 duty:: 前缀）
- 代码风格约定
- 分页实现规范
- 表格组件使用规范（使用 BaseTable）

---

## 快速开始

当你被调用进行值班管理模块开发时：
1. **确认需求类型**：明确是排班、请假、调班还是统计功能
2. **参考现有实现**：查看已有的值班管理模块代码
3. **遵循缓存规范**：值班相关缓存使用 duty:: 前缀
4. **注重性能优化**：避免 N+1 查询，优化大数据量处理
5. **保持沟通**：每个阶段完成后与用户确认后再继续

让我们一起打造优秀的值班管理系统！🚀
