<template>
  <div class="process-designer">
    <el-page-header @back="goBack" title="流程设计器">
      <template #extra>
        <el-button type="primary" @click="handleDeployProcess">部署流程</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4" style="height: calc(100vh - 160px)">
      <template #header>
        <div class="card-header">
          <span>流程设计</span>
          <el-form inline size="small">
            <el-form-item label="流程名称">
              <el-input v-model="processName" placeholder="请输入流程名称" />
            </el-form-item>
          </el-form>
        </div>
      </template>
      <BpmnDesigner ref="designerRef" @save="handleSave" @change="handleXmlChange" />
    </el-card>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BpmnDesigner from '@/components/BpmnDesigner.vue'
import { deployProcess as deployProcessApi } from '@/api/workflow/process'

export default {
  name: 'ProcessDesigner',
  components: { BpmnDesigner },
  setup() {
    const router = useRouter()
    const designerRef = ref(null)
    const processName = ref('')
    const xmlContent = ref('')

    const goBack = () => {
      router.back()
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

    return {
      goBack,
      designerRef,
      processName,
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
