<template>
  <div class="test-container">
    <h1>人员选择框测试</h1>
    <div class="test-content">
      <PersonSelector
        v-model="selectedPersons"
        @change="handleSelectionChange"
      />
      <div class="button-group">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确认</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PersonSelector from '@/components/PersonSelector.vue'

const selectedPersons = ref([])

const handleSelectionChange = (persons) => {
  console.log('Selected persons:', persons)
}

const handleCancel = () => {
  console.log('取消选择')
  selectedPersons.value = []
}

const handleConfirm = () => {
  console.log('确认选择:', selectedPersons.value)
  // 这里可以添加确认后的逻辑，比如关闭弹窗、提交数据等
  ElMessage.success(`已确认选择 ${selectedPersons.value.length} 人`)
}
</script>

<style scoped>
.test-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-content {
  margin: 20px 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 20px;
  background-color: #f9fafc;
  position: relative;
}

.button-group {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

h1 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #303133;
}

:deep(.person-selector) {
  height: 500px;
}
</style>