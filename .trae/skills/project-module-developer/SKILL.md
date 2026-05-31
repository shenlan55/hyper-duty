---
name: "project-module-developer"
description: "项目管理模块开发专家，负责开发和维护项目管理、任务管理、进度跟踪等功能，包括批量新建任务和批量更新进度。当用户提到项目、任务、批量新建任务、批量更新进度、甘特图、进度跟踪、项目管理、任务管理、父子任务等相关需求时，调用此技能。"
---

# 项目管理模块开发专家

这个技能专门用于开发和维护 Hyper Duty 系统的项目管理模块，包括项目管理、任务管理、进度跟踪等功能。

## 核心职责

- **项目管理**：实现项目创建、编辑、删除和查看功能
- **任务管理**：开发任务的增删改查功能，包括父子任务关系
- **批量操作**：实现批量新建任务和批量更新进度功能
- **进度跟踪**：实现任务进度更新和跟踪功能
- **参与人管理**：管理任务的参与人和权限控制
- **附件管理**：支持任务附件上传和下载
- **甘特图**：实现甘特图展示和导出功能
- **统计报表**：生成项目进度和任务统计报表
- **缓存管理**：为项目和任务相关热点数据实现和维护 Redis 缓存

## 关键文件和组件

### 前端组件
```
frontend/src/views/project/
├── ProjectList.vue                     # 项目列表
├── ProjectDetail.vue                   # 项目详情
├── TaskList.vue                        # 任务列表
├── TaskDetail.vue                      # 任务详情
└── GanttChart.vue                      # 甘特图
frontend/src/components/
├── BatchCreateTasks.vue                # 批量新建任务
├── BatchUpdateProgress.vue             # 批量更新进度
├── RichTextEditor.vue                  # 富文本编辑器
└── FileUpload.vue                      # 文件上传
```

### 后端服务
```
src/main/java/com/lasu/hyperduty/service/
├── PmProjectService.java               # 项目服务
├── PmTaskService.java                  # 任务服务
├── PmTaskProgressUpdateService.java    # 任务进度更新服务
├── PmTaskCommentService.java           # 任务评论服务
└── PmAttachmentService.java            # 附件服务
```

### DTO 和 VO
```
src/main/java/com/lasu/hyperduty/dto/
├── PmTaskQueryDTO.java                 # 任务查询 DTO
├── PmTaskCreateDTO.java                # 任务创建 DTO
├── PmTaskUpdateDTO.java                # 任务更新 DTO
├── PmTaskVO.java                       # 任务 VO（含权限）
├── BatchTaskCreateDTO.java             # 批量创建任务 DTO
└── BatchProgressUpdateDTO.java         # 批量更新进度 DTO
```

### 控制器
```
src/main/java/com/lasu/hyperduty/controller/
├── PmProjectController.java            # 项目控制器
├── PmTaskController.java               # 任务控制器
├── PmTaskProgressUpdateController.java # 进度更新控制器
└── PmTaskV1Controller.java             # 任务 V1 控制器（API 版本控制）
```

## 开发规范

### 1. API 响应处理
使用简化模式，成功响应直接返回 `data.data`：
```javascript
const data = await apiFunction();
data.value = data || [];
```

### 2. DTO/VO 模式
任务模块已实现完整的 DTO/VO 模式，其他模块可参考：
- 查询使用 QueryDTO
- 创建使用 CreateDTO
- 更新使用 UpdateDTO
- 返回使用 VO（包含权限信息）

### 3. API 版本控制
使用 V1 版本的 API 控制器，路径以 `/api/v1/` 开头

### 4. 权限管理
- 在 VO 中包含 hasPermission 和 hasDeletePermission 字段
- 权限信息随列表批量返回，避免单独查询

### 5. 缓存管理
- **缓存键设计**：项目和任务相关缓存键使用 `project::{key}_{params}` 格式
- **缓存清除机制**：
  - 简单场景：使用 @CacheEvict 注解
  - 复杂场景：继承 CacheableServiceImpl 并实现 clearCache 方法
- **缓存一致性**：创建/更新/删除操作后始终清除相关缓存

### 6. 树形结构处理
- 任务支持父子关系树形结构
- 使用 BaseTable 组件的树形展示功能
- 避免重复的展开/收起箭头

## 批量新建任务功能说明

### 功能特性
- 支持同时创建多个任务
- 每个任务可以独立设置名称、优先级、是否重点
- 每个任务可以独立设置开始/结束时间
- 每个任务可以独立编辑富文本描述
- 每个任务可以独立上传附件
- 每个任务可以独立选择参与人
- 支持共享默认值（项目、负责人等）
- 支持动态添加/删除任务行

### 组件结构
```vue
<!-- BatchCreateTasks.vue -->
<template>
  <el-dialog title="批量新建任务" width="90%">
    <!-- 共享配置区域 -->
    <el-form>
      <!-- 项目选择 -->
      <!-- 负责人选择 -->
      <!-- 默认优先级 -->
      <!-- 默认是否重点 -->
      <!-- 默认开始/结束时间 -->
    </el-form>
    
    <!-- 任务列表区域 -->
    <el-card v-for="(task, index) in taskList" :key="index">
      <template #header>
        <div>
          <span>任务 {{ index + 1 }}</span>
          <el-button type="danger" link @click="removeTaskRow(index)">删除</el-button>
        </div>
      </template>
      
      <!-- 任务表单 -->
      <el-form>
        <!-- 任务名称 -->
        <!-- 优先级 -->
        <!-- 是否重点 -->
        <!-- 开始/结束时间 -->
        <!-- 富文本描述 -->
        <!-- 附件上传 -->
        <!-- 参与人选择 -->
      </el-form>
    </el-card>
    
    <!-- 添加任务按钮 -->
    <el-button @click="addTaskRow">添加任务</el-button>
    
    <!-- 底部操作 -->
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>
```

## 批量更新进度功能说明

### 功能特性
- 在任务列表中勾选需要更新的任务
- 显示已选择任务数量的提示
- 设置统一的进度值（滑块选择 0-100%）
- 添加统一的进展描述（富文本）
- 支持上传统一的附件
- 预览已选择的任务列表
- 显示每个任务的当前进度

### 使用流程
1. **选择任务**：在任务列表中勾选一个或多个任务
2. **显示提示**：顶部显示「已选择 X 个任务」的提示条
3. **点击按钮**：点击「批量更新进度」按钮
4. **设置参数**：设置进度值、描述、附件
5. **确认提交**：确认后批量更新所有任务

### 组件结构
```vue
<!-- BatchUpdateProgress.vue -->
<template>
  <el-dialog title="批量更新任务进度" width="1400px">
    <!-- 选择信息提示 -->
    <div class="selected-info">已选择 {{ selectedTasks.length }} 个任务</div>
    
    <!-- 更新表单 -->
    <el-form>
      <!-- 进度滑块 -->
      <!-- 进展描述 -->
      <!-- 附件上传 -->
    </el-form>
    
    <!-- 任务预览 -->
    <el-divider content-position="left">任务列表预览</el-divider>
    <el-table :data="selectedTasks" border max-height="300">
      <!-- 任务名称 -->
      <!-- 当前进度 -->
      <!-- 负责人 -->
      <!-- 所属项目 -->
    </el-table>
    
    <!-- 底部操作 -->
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>
```

## 任务列表使用规范

### BaseTable 配置
```vue
<BaseTable
  :data="tableData"
  :columns="columns"
  :loading="loading"
  :pagination="{
    currentPage: pagination.currentPage,
    pageSize: pagination.pageSize,
    pageSizes: pagination.pageSizes,
    total: pagination.total
  }"
  :row-key="'id'"
  :indent="20"
  :backend-pagination="true"
  :show-selection="true"
  @size-change="handleSizeChange"
  @current-change="handleCurrentChange"
  @selection-change="handleSelectionChange"
/>
```

### 注意事项
- **不要添加** `:tree-props` 属性，会导致重复箭头
- 使用 `:indent` 属性设置树形缩进
- 使用 `:row-key="'id'"` 确保行唯一标识
- 使用 `:show-selection="true"` 启用选择功能

## 常见问题和解决方案

1. **父任务重复箭头问题**：移除 BaseTable 的 `:tree-props` 属性
2. **批量新建任务缺少要素**：每个任务卡片包含完整的表单字段
3. **批量更新进度功能不明显**：选择任务后显示醒目的提示条
4. **任务进度更新不同步**：更新后清除相关缓存并刷新列表
5. **附件上传失败**：检查文件大小限制和类型限制
6. **树形数据展示异常**：确保后端返回正确的 children 结构

## 测试指南

### 批量新建任务测试
- 测试单个任务创建
- 测试多个任务同时创建（5个、10个、20个）
- 测试每个任务独立设置不同属性
- 测试附件上传功能
- 测试参与人选择功能
- 测试共享默认值功能

### 批量更新进度测试
- 测试单个任务进度更新
- 测试多个任务同时更新进度
- 测试不同进度值设置
- 测试添加进展描述
- 测试上传附件
- 测试权限控制（只能更新有编辑权限的任务）

### 任务列表测试
- 测试树形结构展示
- 测试父子任务关系
- 测试选择功能
- 测试分页功能
- 测试权限判断（hasPermission/hasDeletePermission）

## 项目规则参考

始终参考 `.trae/rules/project_rules.md`：
- 技术栈约束（仅使用指定技术栈）
- API 调用模式（简化响应处理）
- 缓存管理规范（项目模块使用 project:: 前缀）
- 代码风格约定
- 分页实现规范
- 表格组件使用规范（使用 BaseTable）
- XSS 防护规范（使用 xssUtil.js）

---

## 快速开始

当你被调用进行项目管理模块开发时：
1. **确认需求类型**：明确是项目管理、任务管理、批量操作还是其他功能
2. **参考现有实现**：任务模块已经完成完整重构，可作为参考
3. **遵循 DTO/VO 模式**：使用已有的模式进行开发
4. **实现权限管理**：在 VO 中包含权限信息并批量返回
5. **保持沟通**：每个阶段完成后与用户确认后再继续

让我们一起打造出色的项目管理系统！🚀
