<template>
  <div class="base-table">
    <!-- 表格 -->
    <el-table
      v-loading="loading"
      :data="processedData"
      style="width: 100%"
      :border="border"
      :stripe="stripe"
      :size="size"
      @sort-change="handleSortChange"
    >
      <!-- 自定义列 -->
      <template v-for="column in columns" :key="column.prop">
        <el-table-column
          v-if="!column.type"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
          :min-width="column.minWidth"
          :sortable="column.sortable"
          :fixed="column.fixed"
        >
          <template #default="scope">
            <slot :name="column.prop" v-bind="scope">{{ scope.row[column.prop] }}</slot>
          </template>
        </el-table-column>
        
        <!-- 操作列 -->
        <el-table-column
          v-else-if="column.type === 'operation'"
          :label="column.label || '操作'"
          :width="column.width || 200"
          :fixed="column.fixed"
        >
          <template #default="scope">
            <slot name="operation" v-bind="scope"></slot>
          </template>
        </el-table-column>
      </template>
    </el-table>
    
    <!-- 分页 -->
    <div class="pagination-container" v-if="showPagination">
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="pagination.pageSizes"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

// Props
const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 列配置
  columns: {
    type: Array,
    required: true
  },
  // 加载状态
  loading: {
    type: Boolean,
    default: false
  },
  // 是否显示分页
  showPagination: {
    type: Boolean,
    default: true
  },
  // 分页配置
  pagination: {
    type: Object,
    default: () => ({
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 50, 100],
      total: 0
    })
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: true
  },
  // 是否显示斑马纹
  stripe: {
    type: Boolean,
    default: true
  },
  // 表格大小
  size: {
    type: String,
    default: 'default'
  }
})

// Emits
const emit = defineEmits([
  'size-change',
  'current-change',
  'sort-change'
])

// 处理分页大小变化
const handleSizeChange = (size) => {
  emit('size-change', size)
}

// 处理当前页码变化
const handleCurrentChange = (current) => {
  emit('current-change', current)
}

// 处理排序变化
const handleSortChange = (sort) => {
  emit('sort-change', sort)
}

// 处理后的数据
const processedData = computed(() => {
  return props.data
})
</script>

<style scoped>
.base-table {
  width: 100%;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>