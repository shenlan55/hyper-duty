<template>
  <div class="process-start">
    <el-page-header title="发起流程">
      <template #extra>
        <el-button type="primary" @click="handleSubmit" :disabled="!selectedProcess">提交发起</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4">
      <div class="grid-container">
        <!-- 左侧：流程选择树 -->
        <div class="left-panel">
          <h3>选择流程</h3>
          <el-tree
            :data="treeData"
            :props="treeProps"
            node-key="id"
            default-expand-all
            @node-click="handleNodeClick"
            :highlight-current="true"
          >
            <template #default="{ node }">
              <span v-if="node.type === 'category'" class="category-node">
                <el-icon><folder /></el-icon>
                {{ node.label }}
              </span>
              <span v-else class="process-node">
                <el-icon><file-text /></el-icon>
                {{ node.label }}
              </span>
            </template>
          </el-tree>
        </div>

        <!-- 右侧：表单区域 -->
        <div class="right-panel">
          <div v-if="selectedProcess" class="form-wrapper">
            <h3>{{ selectedProcess.name }}</h3>
            <p class="process-desc">{{ selectedProcess.remark || '暂无描述' }}</p>
            
            <!-- 表单内容 -->
            <div v-if="formRule.length > 0" class="form-content">
              <form-create
                :rule="formRule"
                :option="formOption"
                v-model="formData"
                v-model:api="formApi"
              />
            </div>
            <div v-else class="no-form">
              <el-empty description="该流程未关联表单" />
              <p class="hint">可以直接提交发起流程</p>
            </div>
          </div>
          <div v-else class="select-tip">
            <el-empty description="请从左侧选择一个流程" />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElTree, ElEmpty } from 'element-plus'
import { Folder as folder, Document as fileText } from '@element-plus/icons-vue'
import { listCategory } from '@/api/workflow/category'
import { listProcessDefinition } from '@/api/workflow/process'
import { getForm } from '@/api/workflow/form'
import { startProcess } from '@/api/workflow/process'

const treeData = ref([])
const selectedProcess = ref(null)
const formRule = ref([])
const formData = reactive({})
const formApi = ref({})

const treeProps = {
  children: 'children',
  label: 'label'
}

const formOption = {
  submitBtn: false,
  resetBtn: false
}

const handleNodeClick = async (data) => {
  if (data.type === 'process') {
    selectedProcess.value = data
    formData = reactive({})
    
    if (data.formId) {
      try {
        const res = await getForm(data.formId)
        if (res.formContent) {
          try {
            formRule.value = JSON.parse(res.formContent)
          } catch (e) {
            formRule.value = []
          }
        } else {
          formRule.value = []
        }
      } catch (error) {
        ElMessage.error('获取表单失败')
        formRule.value = []
      }
    } else {
      formRule.value = []
    }
  } else {
    selectedProcess.value = null
    formRule.value = []
  }
}

const handleSubmit = async () => {
  if (!selectedProcess.value) {
    ElMessage.warning('请先选择一个流程')
    return
  }

  try {
    const variables = { ...formData }
    const response = await startProcess({
      processDefinitionKey: selectedProcess.value.key,
      variables
    })
    
    ElMessage.success('流程发起成功')
    console.log('流程实例ID:', response.processInstanceId)
    
    selectedProcess.value = null
    formRule.value = []
    Object.keys(formData).forEach(key => delete formData[key])
  } catch (error) {
    ElMessage.error(error.message || '流程发起失败')
  }
}

const loadTreeData = async () => {
  try {
    const [categoryRes, processRes] = await Promise.all([
      listCategory(),
      listProcessDefinition()
    ])

    const categories = categoryRes.records || []
    const processes = processRes || []

    const categoryMap = new Map()
    categories.forEach(cat => {
      categoryMap.set(cat.id, {
        id: 'cat_' + cat.id,
        label: cat.name,
        type: 'category',
        children: []
      })
    })

    processes.forEach(proc => {
      const processNode = {
        id: 'proc_' + proc.id,
        label: proc.name,
        key: proc.key,
        type: 'process',
        formId: proc.formId,
        remark: proc.remark,
        categoryId: proc.categoryId
      }

      if (proc.categoryId && categoryMap.has(proc.categoryId)) {
        categoryMap.get(proc.categoryId).children.push(processNode)
      } else {
        const uncategorized = categoryMap.get('uncategorized') || {
          id: 'cat_uncategorized',
          label: '未分类',
          type: 'category',
          children: []
        }
        uncategorized.children.push(processNode)
        categoryMap.set('uncategorized', uncategorized)
      }
    })

    treeData.value = Array.from(categoryMap.values())
  } catch (error) {
    ElMessage.error('加载流程列表失败')
  }
}

onMounted(() => {
  loadTreeData()
})
</script>

<style scoped>
.process-start {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.grid-container {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  min-height: 500px;
}

.left-panel {
  border-right: 1px solid #e4e7ed;
  padding-right: 20px;
}

.left-panel h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
}

.right-panel {
  padding-left: 20px;
}

.category-node {
  font-weight: 600;
  color: #606266;
}

.process-node {
  color: #409eff;
}

.form-wrapper {
  background: #fafafa;
  padding: 20px;
  border-radius: 8px;
}

.form-wrapper h3 {
  margin-bottom: 5px;
  font-size: 18px;
}

.process-desc {
  color: #909399;
  margin-bottom: 20px;
}

.form-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
}

.no-form {
  text-align: center;
  padding: 40px;
}

.hint {
  color: #909399;
  margin-top: 10px;
}

.select-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
}
</style>
