# 人员选择框组件使用文档

## 组件说明

`PersonSelector` 是一个通用的人员选择组件，具有三栏布局，支持部门选择、人员列表动态刷新和左右传人选人功能。

## 功能特点

- **三栏布局**：左侧部门树、中间人员列表、右侧已选人员
- **部门选择**：树形结构展示部门层级
- **人员列表**：根据选中部门动态加载人员，支持搜索
- **左右传人**：支持在中间和右侧列表之间进行人员的多选转移
- **实时同步**：选择人员变化时触发事件通知父组件

## 使用方法

### 基本使用

```vue
<template>
  <div>
    <PersonSelector
      v-model="selectedPersons"
      @change="handleSelectionChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PersonSelector from '@/components/PersonSelector.vue'

const selectedPersons = ref([])

const handleSelectionChange = (persons) => {
  console.log('Selected persons:', persons)
}
</script>
```

### 带初始值的使用

```vue
<template>
  <div>
    <PersonSelector
      v-model="selectedPersons"
      @change="handleSelectionChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PersonSelector from '@/components/PersonSelector.vue'

// 初始已选人员
const selectedPersons = ref([
  {
    id: 1,
    employeeName: '张三',
    employeeCode: 'EMP001',
    deptName: '技术部',
    phone: '13800138001'
  },
  {
    id: 2,
    employeeName: '李四',
    employeeCode: 'EMP002',
    deptName: '产品部',
    phone: '13800138002'
  }
])

const handleSelectionChange = (persons) => {
  console.log('Selected persons:', persons)
}
</script>
```

## 属性说明

| 属性名 | 类型 | 默认值 | 说明 |
|-------|------|-------|------|
| modelValue | Array | [] | 已选人员列表，支持 v-model 双向绑定 |
| placeholder | String | '请选择人员' | 占位符文本 |

## 事件说明

| 事件名 | 参数 | 说明 |
|-------|------|------|
| update:modelValue | Array | 当选择人员变化时触发，返回新的已选人员列表 |
| change | Array | 当选择人员变化时触发，返回新的已选人员列表 |

## 数据结构

### 人员对象结构

```javascript
{
  id: Number,          // 人员ID
  employeeName: String, // 姓名
  employeeCode: String, // 工号
  deptName: String,     // 部门名称
  phone: String         // 手机号
}
```

### 部门对象结构

```javascript
{
  id: Number,          // 部门ID
  deptName: String,     // 部门名称
  children: Array       // 子部门列表
}
```

## 操作说明

1. **部门选择**：点击左侧部门树中的部门，中间列表会动态加载该部门下的人员
2. **人员搜索**：在中间列表的搜索框中输入关键词，会实时过滤人员列表
3. **人员选择**：在中间列表中勾选人员，然后点击「选择 >>」按钮将选中人员转移到右侧
4. **全部选择**：点击「全部 >>」按钮将中间列表中的所有人员转移到右侧
5. **人员移除**：在右侧列表中勾选人员，然后点击「<< 移除」按钮将选中人员转移回左侧
6. **全部移除**：点击「<< 全部」按钮将右侧列表中的所有人员转移回左侧

## 注意事项

1. 组件依赖后端API接口：
   - `/dept/tree`：获取部门树结构
   - `/employee/list/{deptId}`：根据部门ID获取人员列表

2. 组件会自动过滤已选中的人员，确保中间列表和右侧列表的数据不重复

3. 组件支持响应式设计，可根据容器大小自动调整布局

4. 组件使用 Element Plus 组件库，确保项目中已引入 Element Plus

## 示例场景

### 场景1：选择值班人员

```vue
<template>
  <div>
    <h3>选择值班人员</h3>
    <PersonSelector
      v-model="dutyPersons"
      @change="handleDutyPersonsChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PersonSelector from '@/components/PersonSelector.vue'

const dutyPersons = ref([])

const handleDutyPersonsChange = (persons) => {
  console.log('值班人员:', persons)
  // 保存值班人员信息
}
</script>
```

### 场景2：选择项目成员

```vue
<template>
  <div>
    <h3>选择项目成员</h3>
    <PersonSelector
      v-model="projectMembers"
      @change="handleProjectMembersChange"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import PersonSelector from '@/components/PersonSelector.vue'

const projectMembers = ref([])

const handleProjectMembersChange = (persons) => {
  console.log('项目成员:', persons)
  // 保存项目成员信息
}
</script>
```

## 浏览器兼容性

- 支持 Chrome、Firefox、Safari、Edge 等现代浏览器
- 不支持 IE 浏览器

## 性能优化

1. 部门树数据会在组件初始化时一次性加载，减少重复请求
2. 人员列表会根据部门选择动态加载，避免一次性加载所有人员
3. 组件使用虚拟滚动技术，支持处理大量人员数据
4. 搜索功能使用前端过滤，提高响应速度

## 维护说明

- 组件使用 Vue 3 Composition API 开发
- 依赖 Element Plus 组件库和 Axios 网络请求库
- 代码结构清晰，易于维护和扩展

## 版本历史

- v1.0.0：初始版本，实现基本功能
- v1.0.1：优化搜索功能，添加实时过滤
- v1.0.2：修复数据同步问题，确保左右列表数据不重复