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
      <!-- 始终挂载 BpmnDesigner，避免先卸载后重挂时旧实例的 importXML 残留访问已销毁 canvas
           loading 时显示加载层覆盖在画布之上 -->
      <div style="height: 100%; position: relative;">
        <BpmnDesigner ref="designerRef" :xml="initialXml" @save="handleSave" @change="handleXmlChange" />
        <div v-if="loading" style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: rgba(255,255,255,0.7); z-index: 10;">
          <el-icon class="is-loading" style="font-size: 40px;"><loading /></el-icon>
          <span style="margin-left: 10px;">加载中...</span>
        </div>
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
import { deployProcess as deployProcessApi, getProcessBpmnXml } from '@/api/workflow/process'

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
        // request.js 响应拦截器已自动解包 ResponseResult.data
        const xml = await getProcessBpmnXml(processDefinitionId)
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
