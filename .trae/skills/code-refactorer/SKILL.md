---
name: "code-refactorer"
description: "按照最佳实践重构Hyper Duty系统代码。优化后端（统一异常处理、DTO/VO、API版本控制）和前端（组件拆分、常量管理）。提供详细的操作步骤和完整的代码示例。"
---

# 代码重构专家

这个Skill专门用于按照架构最佳实践重构Hyper Duty系统代码，涵盖后端和前端。它提供了详细的操作指导和完整的代码示例。

## 核心职责

- **后端重构**：实现统一异常处理、DTO/VO模式、API版本控制
- **前端重构**：将大组件拆分为小的可复用组件、管理常量
- **性能优化**：优化API调用、避免N+1问题、使用缓存
- **代码质量**：提高可维护性、可读性、遵循项目规范

## 项目现状评估

✅ **已完成**：
- 统一异常处理（BusinessException + GlobalExceptionHandler）
- 任务模块的 DTO/VO 模式（TaskQueryDTO, TaskCreateDTO, TaskUpdateDTO, TaskVO）
- API 版本控制（PmTaskV1Controller）
- 任务模块的权限字段（hasPermission, hasDeletePermission）
- 前端常量管理（task.js）
- 部分组件拆分（TaskSearchForm.vue）

📋 **待完善**：
- 其他模块的 DTO/VO 改造
- 进一步组件拆分
- 统一使用 v1 API
- 权限信息随列表批量返回，减少单独查询

## 重构检查清单

### 后端重构步骤

#### 1️⃣ 统一异常处理（已完成）
项目已具备完善的异常处理机制：

```java
// src/main/java/com/lasu/hyperduty/exception/BusinessException.java
package com.lasu.hyperduty.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
```

```java
// src/main/java/com/lasu/hyperduty/exception/GlobalExceptionHandler.java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseResult<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseResult.error(e.getCode(), e.getMessage());
    }

    // ... 其他异常处理
}
```

#### 2️⃣ DTO/VO设计（任务模块已实现，可参考）

**DTO 示例结构**：
```java
// src/main/java/com/lasu/hyperduty/dto/{module}/{Entity}QueryDTO.java
@Data
public class {Entity}QueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    // 查询条件字段
}

// src/main/java/com/lasu/hyperduty/dto/{module}/{Entity}CreateDTO.java
@Data
public class {Entity}CreateDTO {
    @NotBlank(message = "名称不能为空")
    private String name;
    // 创建字段
}

// src/main/java/com/lasu/hyperduty/dto/{module}/{Entity}UpdateDTO.java
@Data
public class {Entity}UpdateDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    // 更新字段
}

// src/main/java/com/lasu/hyperduty/dto/{module}/{Entity}VO.java
@Data
public class {Entity}VO {
    private Long id;
    // 实体字段
    private Boolean hasPermission;      // 编辑权限
    private Boolean hasDeletePermission; // 删除权限
}
```

**服务层 convertToVO 示例**：
```java
private {Entity}VO convertToVO({Entity} entity, Long currentEmployeeId) {
    {Entity}VO vo = new {Entity}VO();
    BeanUtils.copyProperties(entity, vo);
    
    // 计算权限
    if (currentEmployeeId != null) {
        vo.setHasPermission(checkEditPermission(entity, currentEmployeeId));
        vo.setHasDeletePermission(checkDeletePermission(entity, currentEmployeeId));
    } else {
        vo.setHasPermission(false);
        vo.setHasDeletePermission(false);
    }
    
    return vo;
}
```

#### 3️⃣ API版本控制（任务模块已实现）

```java
// src/main/java/com/lasu/hyperduty/controller/v1/{Module}V1Controller.java
@Slf4j
@RestController
@RequestMapping("/api/v1/{module}")
@RequiredArgsConstructor
public class {Module}V1Controller {
    private final {Module}Service {module}Service;

    @GetMapping("/page")
    public ResponseResult<Page<{Entity}VO>> pageList(@Valid {Entity}QueryDTO query) {
        Long currentEmployeeId = getCurrentEmployeeId(); // 获取当前登录用户
        Page<{Entity}VO> page = {module}Service.pageList(query, currentEmployeeId);
        return ResponseResult.success(page);
    }

    @PostMapping
    public ResponseResult<{Entity}> create(@Valid @RequestBody {Entity}CreateDTO dto) {
        {Entity} created = {module}Service.create(dto);
        return ResponseResult.success(created);
    }

    @PutMapping
    public ResponseResult<{Entity}> update(@Valid @RequestBody {Entity}UpdateDTO dto) {
        {Entity} updated = {module}Service.update(dto);
        return ResponseResult.success(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        {module}Service.delete(id);
        return ResponseResult.success();
    }
}
```

### 前端重构步骤

#### 1️⃣ 常量管理（任务模块已实现）

```javascript
// frontend/src/constants/{module}.js
export const {MODULE}_STATUS = {
    ACTIVE: 1,
    INACTIVE: 2
};

export const {MODULE}_STATUS_MAP = {
    [{MODULE}_STATUS.ACTIVE]: '活跃',
    [{MODULE}_STATUS.INACTIVE]: '停用'
};

export const {MODULE}_PRIORITY = {
    HIGH: 1,
    MEDIUM: 2,
    LOW: 3
};

export const {MODULE}_PRIORITY_MAP = {
    [{MODULE}_PRIORITY.HIGH]: '高',
    [{MODULE}_PRIORITY.MEDIUM]: '中',
    [{MODULE}_PRIORITY.LOW]: '低'
};
```

#### 2️⃣ 组件拆分原则

- **单一职责**：每个组件只做一件事
- **可复用**：提取公共逻辑到独立组件
- **Propx Down, Events Up**：使用 props 传递数据，emits 传递事件
- **500行原则**：超过500行的组件考虑拆分

**搜索表单组件示例**（TaskSearchForm.vue 已实现）：
```vue
<template>
  <el-form :inline="true" :model="searchForm" class="search-form">
    <!-- 表单项 -->
    <el-form-item>
      <el-button type="primary" @click="handleSearch">查询</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { {MODULE}_STATUS_MAP, {MODULE}_PRIORITY_MAP } from '@/constants/{module}'

const props = defineProps({
  searchForm: { type: Object, required: true },
  projectList: { type: Array, default: () => [] }
})

const emit = defineEmits(['search', 'reset', 'projectChange'])

const handleSearch = () => emit('search')
const handleReset = () => emit('reset')
const handleProjectChange = (value) => emit('projectChange', value)
</script>
```

**编辑对话框组件示例**：
```vue
<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="800px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <!-- 表单项 -->
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  editData: { type: Object, default: null }
})

const emit = defineEmits(['update:modelValue', 'success'])

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const form = reactive({ /* 表单字段 */ })
const rules = { /* 验证规则 */ }
const dialogTitle = computed(() => props.editData ? '编辑' : '新建')

watch(() => props.editData, (data) => {
  if (data) {
    Object.assign(form, data)
  }
}, { immediate: true })

const handleSubmit = async () => {
  await formRef.value.validate()
  emit('success', { ...form })
  dialogVisible.value = false
}

const handleClose = () => {
  formRef.value?.resetFields()
}
</script>
```

## 常见重构场景完整指南

### 🎯 场景1：模块后端完整重构（以值班排班为例）

**步骤1：创建 DTO/VO**
```java
// src/main/java/com/lasu/hyperduty/dto/duty/DutyScheduleQueryDTO.java
@Data
public class DutyScheduleQueryDTO {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private Long scheduleId;
    private LocalDate startDate;
    private LocalDate endDate;
}

// src/main/java/com/lasu/hyperduty/dto/duty/DutyScheduleVO.java
@Data
public class DutyScheduleVO {
    private Long id;
    private Long scheduleId;
    private Long employeeId;
    private String employeeName;
    private LocalDate dutyDate;
    private Integer shiftId;
    private String shiftName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean hasPermission;
    private Boolean hasDeletePermission;
}

// src/main/java/com/lasu/hyperduty/dto/duty/DutyScheduleCreateDTO.java
@Data
public class DutyScheduleCreateDTO {
    @NotNull(message = "排班计划不能为空")
    private Long scheduleId;
    @NotNull(message = "员工不能为空")
    private Long employeeId;
    @NotNull(message = "值班日期不能为空")
    private LocalDate dutyDate;
    @NotNull(message = "班次不能为空")
    private Integer shiftId;
}

// src/main/java/com/lasu/hyperduty/dto/duty/DutyScheduleUpdateDTO.java
@Data
public class DutyScheduleUpdateDTO {
    @NotNull(message = "ID不能为空")
    private Long id;
    private Long employeeId;
    private LocalDate dutyDate;
    private Integer shiftId;
}
```

**步骤2：创建 v1 控制器**
```java
// src/main/java/com/lasu/hyperduty/controller/v1/DutyScheduleV1Controller.java
@Slf4j
@RestController
@RequestMapping("/api/v1/duty/schedule")
@RequiredArgsConstructor
public class DutyScheduleV1Controller {
    private final DutyScheduleService dutyScheduleService;

    @GetMapping("/page")
    public ResponseResult<Page<DutyScheduleVO>> pageList(@Valid DutyScheduleQueryDTO query) {
        Long currentEmployeeId = null; // 从Security上下文获取
        Page<DutyScheduleVO> page = dutyScheduleService.pageListWithPermission(query, currentEmployeeId);
        return ResponseResult.success(page);
    }

    @GetMapping("/{id}")
    public ResponseResult<DutyScheduleVO> getDetail(@PathVariable Long id) {
        Long currentEmployeeId = null;
        DutyScheduleVO vo = dutyScheduleService.getDetailWithPermission(id, currentEmployeeId);
        return ResponseResult.success(vo);
    }

    @PostMapping
    public ResponseResult<DutySchedule> create(@Valid @RequestBody DutyScheduleCreateDTO dto) {
        DutySchedule created = dutyScheduleService.create(dto);
        return ResponseResult.success(created);
    }

    @PutMapping
    public ResponseResult<DutySchedule> update(@Valid @RequestBody DutyScheduleUpdateDTO dto) {
        DutySchedule updated = dutyScheduleService.update(dto);
        return ResponseResult.success(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        dutyScheduleService.delete(id);
        return ResponseResult.success();
    }
}
```

**步骤3：服务层实现**
```java
// 在 DutyScheduleService 接口添加方法
Page<DutyScheduleVO> pageListWithPermission(DutyScheduleQueryDTO query, Long currentEmployeeId);
DutyScheduleVO getDetailWithPermission(Long id, Long currentEmployeeId);

// 在 DutyScheduleServiceImpl 中实现
@Override
public Page<DutyScheduleVO> pageListWithPermission(DutyScheduleQueryDTO query, Long currentEmployeeId) {
    Page<DutySchedule> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), buildQueryWrapper(query));
    
    List<DutyScheduleVO> voList = page.getRecords().stream()
        .map(entity -> convertToVO(entity, currentEmployeeId))
        .collect(Collectors.toList());
    
    Page<DutyScheduleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
    voPage.setRecords(voList);
    return voPage;
}

private DutyScheduleVO convertToVO(DutySchedule entity, Long currentEmployeeId) {
    DutyScheduleVO vo = new DutyScheduleVO();
    BeanUtils.copyProperties(entity, vo);
    
    // 关联查询员工姓名和班次名称（避免N+1）
    // ... 填充关联数据
    
    // 计算权限
    if (currentEmployeeId != null) {
        vo.setHasPermission(checkEditPermission(entity, currentEmployeeId));
        vo.setHasDeletePermission(checkDeletePermission(entity, currentEmployeeId));
    } else {
        vo.setHasPermission(false);
        vo.setHasDeletePermission(false);
    }
    
    return vo;
}
```

### 🎯 场景2：模块前端组件完整重构

**目标**：将大组件拆分为小组件，提高可维护性

**示例目录结构**：
```
frontend/src/
├── constants/
│   └── duty.js                          # 值班模块常量
├── components/
│   ├── duty/
│   │   ├── DutyScheduleSearchForm.vue   # 搜索表单
│   │   ├── DutyScheduleEditDialog.vue   # 编辑对话框
│   │   └── DutyScheduleDetailDialog.vue # 详情对话框
│   └── ...
└── views/
    └── duty/
        └── DutyScheduleList.vue         # 简化后的主列表
```

**主组件简化示例**：
```vue
<template>
  <div class="duty-schedule-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>值班排班</span>
          <el-button type="primary" @click="handleAdd">新建</el-button>
        </div>
      </template>

      <!-- 使用提取的搜索表单组件 -->
      <DutyScheduleSearchForm
        :search-form="searchForm"
        :schedule-list="scheduleList"
        @search="handleSearch"
        @reset="handleReset"
      />

      <!-- 使用 BaseTable 组件 -->
      <BaseTable
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #operation="{ row }">
          <el-button v-if="row.hasPermission" type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button type="info" size="small" @click="handleView(row)">详情</el-button>
          <el-button v-if="row.hasDeletePermission" type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 使用提取的对话框组件 -->
    <DutyScheduleEditDialog
      v-model="editDialogVisible"
      :edit-data="currentEditData"
      @success="handleEditSuccess"
    />

    <DutyScheduleDetailDialog
      v-model="detailDialogVisible"
      :detail-data="currentDetailData"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import DutyScheduleSearchForm from '@/components/duty/DutyScheduleSearchForm.vue'
import DutyScheduleEditDialog from '@/components/duty/DutyScheduleEditDialog.vue'
import DutyScheduleDetailDialog from '@/components/duty/DutyScheduleDetailDialog.vue'
import { getDutySchedulePage, createDutySchedule, updateDutySchedule, deleteDutySchedule } from '@/api/duty/schedule'

const loading = ref(false)
const tableData = ref([])
const scheduleList = ref([])
const editDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentEditData = ref(null)
const currentDetailData = ref(null)

const searchForm = reactive({
  scheduleId: null,
  startDate: null,
  endDate: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50, 100],
  total: 0
})

const columns = [
  { prop: 'employeeName', label: '值班人员', width: 120 },
  { prop: 'dutyDate', label: '值班日期', width: 120 },
  { prop: 'shiftName', label: '班次', width: 100 },
  { prop: 'createTime', label: '创建时间', width: 160 },
  { prop: 'operation', label: '操作', width: 200, fixed: 'right', slot: 'operation' }
]

const loadData = async () => {
  loading.value = true
  try {
    const data = await getDutySchedulePage({
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...searchForm
    })
    tableData.value = data.records || []
    pagination.total = data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  currentEditData.value = null
  editDialogVisible.value = true
}

const handleEdit = (row) => {
  currentEditData.value = { ...row }
  editDialogVisible.value = true
}

const handleView = (row) => {
  currentDetailData.value = row
  detailDialogVisible.value = true
}

const handleEditSuccess = () => {
  loadData()
  ElMessage.success('操作成功')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDutySchedule(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const handleReset = () => {
  Object.assign(searchForm, { scheduleId: null, startDate: null, endDate: null })
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadData()
}

onMounted(() => {
  loadData()
})
</script>
```

## 重构执行流程

当需要重构某个模块时，按照以下流程执行：

### 阶段1：分析与计划
1. **审查现有代码**：了解当前实现
2. **识别问题点**：大组件、魔法数字、重复代码等
3. **制定重构计划**：列出需要创建/修改的文件清单

### 阶段2：后端重构
1. 创建/完善 DTO/VO
2. 创建 v1 控制器
3. 更新服务层（添加 convertToVO、权限计算）
4. 测试 API（使用 Postman）

### 阶段3：前端重构
1. 创建/完善常量文件
2. 拆分组件（搜索表单、对话框等）
3. 简化主组件
4. 更新 API 调用（使用 v1 接口）
5. 测试功能

### 阶段4：验证与文档
1. 验证功能完整性
2. 检查向后兼容性
3. 更新相关文档

## 关键文件和组件参考

### 后端文件结构
```
src/main/java/com/lasu/hyperduty/
├── exception/
│   ├── BusinessException.java          # 业务异常（已实现）
│   └── GlobalExceptionHandler.java     # 全局异常处理器（已实现）
├── dto/
│   └── {module}/
│       ├── {Entity}QueryDTO.java       # 查询DTO
│       ├── {Entity}VO.java             # 视图VO（含权限）
│       ├── {Entity}CreateDTO.java      # 创建DTO
│       └── {Entity}UpdateDTO.java      # 更新DTO
├── controller/v1/
│   └── {Module}V1Controller.java       # V1版本API
└── service/
    ├── {Module}Service.java
    └── impl/
        └── {Module}ServiceImpl.java    # 服务实现（含convertToVO）
```

### 前端文件结构
```
frontend/src/
├── constants/
│   └── {module}.js                     # 模块常量
├── components/
│   ├── {module}/
│   │   ├── {Module}SearchForm.vue      # 搜索表单组件
│   │   ├── {Module}EditDialog.vue      # 编辑对话框
│   │   └── {Module}DetailDialog.vue    # 详情对话框
│   └── ...
└── views/
    └── {module}/
        └── {Module}List.vue            # 主列表（简化后）
```

## 开发规范

1. **API响应处理**：遵循项目的响应拦截器模式，直接使用 `data.data`
2. **参数校验**：DTO上使用 `jakarta.validation` 注解
3. **缓存管理**：遵循项目缓存规范（参考 `project_rules.md`）
4. **组件设计**：遵循Vue 3 Composition API最佳实践
5. **XSS防护**：用户输入使用 `xssUtil` 处理
6. **表格组件**：使用 `BaseTable` 组件统一表格样式
7. **虚拟列表**：长列表使用 `VirtualList` 组件
8. **分页实现**：遵循 `project_rules.md` 中的分页规范
9. **权限优化**：权限信息随列表批量返回，避免逐个查询

## 测试指南

- 使用 Postman 或类似工具测试所有 API 端点
- 测试前端组件交互
- 验证向后兼容性
- 检查错误处理和边界情况
- 验证权限检查正确工作
- 性能测试：验证批量返回权限后 API 响应速度提升

## 项目规则参考

始终参考 `.trae/rules/project_rules.md`：
- 技术栈约束
- API调用模式
- 缓存管理规范
- 代码风格约定
- 分页实现规范

---

## 快速开始

当你被调用执行重构时：
1. **确认目标模块**：明确要重构哪个模块
2. **参考任务模块**：任务模块已经完成了完整重构，可作为参考
3. **按阶段执行**：按照上述"重构执行流程"分步进行
4. **保持沟通**：每完成一个阶段，与用户确认后再继续

让我们一起将代码变得更好！🚀
