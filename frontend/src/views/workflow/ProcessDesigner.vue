<template>
  <MobileGuard>
  <div class="process-designer">
    <el-page-header @back="goBack" :title="isEditing ? '编辑流程' : '流程设计器'">
      <template #extra>
        <el-button type="primary" @click="handleDeployProcess">{{ isEditing ? '更新部署' : '部署流程' }}</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4" style="height: calc(100vh - 160px)">
      <template #header>
        <div class="card-header">
          <span>流程设计</span>
          <el-form inline size="small">
            <el-form-item label="流程名称">
              <el-input v-model="processName" placeholder="请输入流程名称" :disabled="isEditing" />
            </el-form-item>
          </el-form>
        </div>
      </template>
      <!-- 只有当 loading 结束时才渲染 BpmnDesigner，确保 initialXml 已经加载 -->
      <div v-if="!loading" style="height: 100%;">
        <BpmnDesigner ref="designerRef" :xml="initialXml" @save="handleSave" @change="handleXmlChange" />
      </div>
      <div v-else style="height: 100%; display: flex; align-items: center; justify-content: center;">
        <el-icon class="is-loading" style="font-size: 40px;"><loading /></el-icon>
        <span style="margin-left: 10px;">加载中...</span>
      </div>
    </el-card>
  </div>
</MobileGuard>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import BpmnDesigner from '@/components/BpmnDesigner.vue'
import MobileGuard from '@/components/MobileGuard.vue'
import { deployProcess as deployProcessApi } from '@/api/workflow/process'

export default {
  name: 'ProcessDesigner',
  components: { BpmnDesigner, MobileGuard },
  setup() {
    const router = useRouter()
    const route = useRoute()
    const designerRef = ref(null)
    const processName = ref('')
    const xmlContent = ref('')
    const initialXml = ref('')
    const isEditing = ref(false)
    const loading = ref(false)

    const goBack = () => {
      router.back()
    }

    const loadProcess = async (processDefinitionId) => {
      loading.value = true
      try {
        console.log('正在加载流程, processDefinitionId:', processDefinitionId)
        
        // 直接用 axios 原始调用，并且用 Vite 代理
        const axios = (await import('axios')).default
        const response = await axios.get(`/api/workflow/process/definition/bpmn/${processDefinitionId}`, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        console.log('loadProcess axios 完整响应:', response)
        console.log('loadProcess response.data:', response.data)
        
        // 获取真正的 XML 内容
        let xml = null
        if (response.data && response.data.code === 200) {
          xml = response.data.data
        } else if (typeof response.data === 'string') {
          xml = response.data
        }
        
        console.log('最终使用的XML:', xml)
        
        if (!xml) {
          console.warn('流程XML为空，使用默认模板')
          ElMessage.warning('流程数据为空，将显示默认模板')
        }
        
        initialXml.value = xml || ''
        xmlContent.value = xml || ''
        isEditing.value = true
      } catch (error) {
        console.error('加载流程失败:', error)
        ElMessage.error(error.message || '加载流程失败')
      } finally {
        loading.value = false
      }
    }

    const handleSave = (xml) => {
      xmlContent.value = xml
      ElMessage.success('保存成功')
    }

    const handleXmlChange = (xml) => {
      xmlContent.value = xml
    }

    const handleDeployProcess = async () => {
      if (!processName.value) {
        ElMessage.warning('请输入流程名称')
        return
      }

      if (!xmlContent.value) {
        ElMessage.warning('请设计流程')
        return
      }

      try {
        await deployProcessApi(processName.value, xmlContent.value)
        ElMessage.success('部署成功')
        router.back()
      } catch (error) {
        ElMessage.error(error.message || '部署失败')
      }
    }

    onMounted(() => {
      const processDefinitionId = route.query.processDefinitionId
      const processNameFromQuery = route.query.processName
      if (processNameFromQuery) {
        processName.value = processNameFromQuery
      }
      if (processDefinitionId) {
        // 如果有 processDefinitionId，则加载已有流程
        loadProcess(processDefinitionId)
      } else {
        // 没有流程 ID，直接渲染空设计器
        loading.value = false
      }
    })

    return {
      goBack,
      designerRef,
      processName,
      initialXml,
      isEditing,
      loading,
      handleSave,
      handleXmlChange,
      handleDeployProcess
    }
  }
}
</script>

<style scoped>
.process-designer {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
