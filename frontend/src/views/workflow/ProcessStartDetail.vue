<template>
  <MobileGuard>
    <div class="process-start-detail" v-loading="loading">
      <el-page-header @back="goBack" :title="processName || '发起流程'">
        <template #extra>
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" :icon="Promotion" @click="handleSubmit">
            提交发起
          </el-button>
        </template>
      </el-page-header>

      <div class="detail-content">
        <!-- 流程基本信息 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon class="section-icon"><info-filled /></el-icon>
              <span class="section-title">流程信息</span>
            </div>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="流程名称">{{ processName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="流程KEY">{{ processKey || '-' }}</el-descriptions-item>
            <el-descriptions-item label="版本">v{{ processVersion || 1 }}</el-descriptions-item>
            <el-descriptions-item label="分类" :span="3">
              {{ categoryName || '未分类' }}
            </el-descriptions-item>
            <el-descriptions-item label="流程说明" :span="3">
              {{ processDesc || '请如实填写表单内容，发起后流转给对应审批人' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 动态表单 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon class="section-icon"><edit /></el-icon>
              <span class="section-title">填写表单</span>
              <el-tag v-if="formRule.length === 0" type="info" size="small">该流程未配置表单</el-tag>
            </div>
          </template>

          <div v-if="formRule.length > 0" class="form-area">
            <form-create
              :rule="formRule"
              :option="formOption"
              v-model="formData"
              v-model:api="formApi"
            />
          </div>
          <div v-else class="no-form-tip">
            <el-empty description="该流程未配置表单，可直接提交" :image-size="80" />
          </div>
        </el-card>

        <!-- 意见与附件 -->
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon class="section-icon"><chat-line-square /></el-icon>
              <span class="section-title">发起意见</span>
            </div>
          </template>
          <el-form :model="submitForm" label-width="100px">
            <el-form-item label="发起意见">
              <el-input
                v-model="submitForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入您的发起意见，将一并提交给审批人"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
            <el-form-item label="首节点处理人">
              <div class="assignee-picker">
                <el-tag
                  v-for="u in submitForm.nextAssignees"
                  :key="u.id"
                  closable
                  @close="removeAssignee(u)"
                  style="margin-right: 8px; margin-bottom: 4px"
                >
                  {{ u.name }}
                </el-tag>
                <el-button
                  v-if="!submitForm.nextAssignees || submitForm.nextAssignees.length === 0"
                  type="primary"
                  size="small"
                  :icon="Plus"
                  @click="openAssigneeDialog"
                >
                  选择人员
                </el-button>
                <el-button
                  v-else
                  size="small"
                  :icon="Plus"
                  @click="openAssigneeDialog"
                >
                  添加
                </el-button>
                <span class="picker-hint">（可选，不选则由流程引擎根据规则自动计算）</span>
              </div>
            </el-form-item>
            <el-form-item label="抄送人">
              <div class="cc-picker">
                <el-tag
                  v-for="u in submitForm.ccUsers"
                  :key="'cc-' + u.id"
                  type="info"
                  closable
                  @close="removeCcUser(u)"
                  style="margin-right: 8px; margin-bottom: 4px"
                >
                  {{ u.name }}
                </el-tag>
                <el-button
                  type="info"
                  size="small"
                  :icon="Plus"
                  @click="openCcDialog"
                >
                  选择抄送人
                </el-button>
                <span class="picker-hint">（可选，流程发起后自动通知抄送人）</span>
              </div>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 流程图预览 -->
        <el-card v-if="bpmnXml" class="section-card" shadow="never">
          <template #header>
            <div class="section-header">
              <el-icon class="section-icon"><view /></el-icon>
              <span class="section-title">流程图预览</span>
            </div>
          </template>
          <BpmnViewer :bpmn-xml="bpmnXml" style="height: 360px" />
        </el-card>
      </div>

      <!-- 底部固定操作栏 -->
      <div class="footer-action">
        <el-button @click="goBack">取消</el-button>
        <el-button type="primary" :icon="Promotion" @click="handleSubmit">
          提交发起
        </el-button>
      </div>

      <!-- 选人弹窗 -->
      <el-dialog
        v-model="assigneeDialogVisible"
        title="选择首节点处理人"
        width="1100px"
        :close-on-click-modal="false"
        destroy-on-close
      >
        <PersonSelector
          v-if="assigneeDialogVisible"
          :multiple="true"
          :selected="submitForm.nextAssignees"
          @confirm="onAssigneeConfirm"
          @cancel="assigneeDialogVisible = false"
        />
      </el-dialog>

      <el-dialog
        v-model="ccDialogVisible"
        title="选择抄送人"
        width="1100px"
        :close-on-click-modal="false"
        destroy-on-close
      >
        <PersonSelector
          v-if="ccDialogVisible"
          :multiple="true"
          :selected="submitForm.ccUsers"
          @confirm="onCcConfirm"
          @cancel="ccDialogVisible = false"
        />
      </el-dialog>
    </div>
  </MobileGuard>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  InfoFilled,
  Edit,
  ChatLineSquare,
  View,
  Plus,
  Promotion
} from '@element-plus/icons-vue'
import MobileGuard from '@/components/MobileGuard.vue'
import BpmnViewer from '@/components/BpmnViewer.vue'
import PersonSelector from '@/components/PersonSelector.vue'
import { getForm } from '@/api/workflow/form'
import {
  startProcess,
  getLatestProcessDefinition,
  getProcessBpmnXml
} from '@/api/workflow/process'
import { listCategory } from '@/api/workflow/category'

const route = useRoute()
const router = useRouter()

// 路由参数
const processKey = ref('')
const processName = ref('')

// 流程信息
const loading = ref(false)
const processVersion = ref(1)
const processDesc = ref('')
const categoryName = ref('')
const processDefinitionId = ref('')
const formId = ref(null)
const formRule = ref([])
const bpmnXml = ref('')
const categoryList = ref([])

// 表单
const formData = reactive({})
const formApi = ref({})
const formOption = {
  submitBtn: false,
  resetBtn: false,
  form: { labelPosition: 'right', labelWidth: '110px' }
}

// 提交表单
const submitForm = reactive({
  comment: '',
  nextAssignees: [],
  ccUsers: []
})

// 选人弹窗
const assigneeDialogVisible = ref(false)
const ccDialogVisible = ref(false)

// 清空 formData
const clearFormData = () => {
  Object.keys(formData).forEach(k => delete formData[k])
}

const onAssigneeConfirm = (selected) => {
  submitForm.nextAssignees = Array.isArray(selected) ? [...selected] : [selected]
  assigneeDialogVisible.value = false
}

const onCcConfirm = (selected) => {
  submitForm.ccUsers = Array.isArray(selected) ? [...selected] : [selected]
  ccDialogVisible.value = false
}

const openAssigneeDialog = () => {
  assigneeDialogVisible.value = true
}

const openCcDialog = () => {
  ccDialogVisible.value = true
}

const removeAssignee = (u) => {
  submitForm.nextAssignees = submitForm.nextAssignees.filter(x => x.id !== u.id)
}

const removeCcUser = (u) => {
  submitForm.ccUsers = submitForm.ccUsers.filter(x => x.id !== u.id)
}

// 加载流程定义
const loadProcess = async () => {
  if (!processKey.value) {
    ElMessage.error('缺少流程KEY')
    return
  }
  loading.value = true
  try {
    const pd = await getLatestProcessDefinition(processKey.value)
    processDefinitionId.value = pd?.id || ''
    formId.value = pd?.formId || null
    processVersion.value = pd?.version || 1
    processDesc.value = pd?.remark || ''

    // 分类名称
    if (pd?.categoryId && categoryList.value.length === 0) {
      try {
        const catRes = await listCategory()
        categoryList.value = catRes.records || catRes || []
      } catch (e) {
        // ignore
      }
    }
    if (pd?.categoryId) {
      const cat = categoryList.value.find(c => c.id === pd.categoryId)
      categoryName.value = cat?.name || ''
    }

    // 表单
    if (formId.value) {
      try {
        const res = await getForm(formId.value)
        if (res && res.formContent) {
          try {
            const parsed = JSON.parse(res.formContent)
            formRule.value = Array.isArray(parsed) ? parsed : []
          } catch (e) {
            console.warn('解析表单内容失败', e)
            formRule.value = []
          }
        } else {
          formRule.value = []
        }
      } catch (error) {
        console.error('加载表单失败', error)
        formRule.value = []
      }
    } else {
      formRule.value = []
    }

    // 流程图 XML
    if (processDefinitionId.value) {
      try {
        const xml = await getProcessBpmnXml(processDefinitionId.value)
        bpmnXml.value = xml || ''
      } catch (e) {
        console.warn('加载流程图失败', e)
      }
    }
  } catch (error) {
    ElMessage.error(error.message || '加载流程信息失败')
  } finally {
    loading.value = false
  }
}

// 提交发起
const handleSubmit = async () => {
  // 1. 校验 form-create 表单
  if (formApi.value && typeof formApi.value.validate === 'function') {
    try {
      await formApi.value.validate()
    } catch (e) {
      ElMessage.warning('请检查表单内容')
      return
    }
  }

  try {
    await ElMessageBox.confirm(
      submitForm.nextAssignees?.length
        ? `确认提交给 [${submitForm.nextAssignees.map(u => u.name).join(', ')}] 审批？`
        : '确认提交发起流程？',
      '提交确认',
      { type: 'info', confirmButtonText: '确认发起', cancelButtonText: '再看看' }
    )
  } catch (e) {
    return
  }

  loading.value = true
  try {
    // 合并 form-create 的数据
    let formValues = {}
    if (formApi.value && typeof formApi.value.formData === 'function') {
      try {
        formValues = formApi.value.formData() || {}
      } catch (e) {
        formValues = { ...formData }
      }
    } else {
      formValues = { ...formData }
    }

    const variables = {
      ...formValues,
      comment: submitForm.comment,
      nextAssignees: submitForm.nextAssignees.map(u => u.id),
      nextAssigneeNames: submitForm.nextAssignees.map(u => u.name).join(','),
      ccUsers: submitForm.ccUsers.map(u => u.id),
      initiatorComment: submitForm.comment
    }

    const result = await startProcess({
      processDefinitionKey: processKey.value,
      variables
    })

    ElMessage.success('流程发起成功')
    // 跳转到流程跟踪页
    if (result && result.id) {
      router.push({
        path: '/workflow/instance-list',
        query: { highlight: result.id }
      })
    } else {
      router.push('/workflow/instance-list')
    }
  } catch (error) {
    ElMessage.error(error.message || '发起失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

// 路由参数变化时重新加载
watch(
  () => [route.query.processDefinitionKey, route.query.processName],
  ([k, n]) => {
    if (k) {
      processKey.value = k
      processName.value = n || k
      clearFormData()
      submitForm.comment = ''
      submitForm.nextAssignees = []
      submitForm.ccUsers = []
      formRule.value = []
      bpmnXml.value = ''
      loadProcess()
    }
  },
  { immediate: true }
)

onMounted(() => {
  // 初始化由 watch immediate 触发
})
</script>

<style scoped>
.process-start-detail {
  padding: 12px;
  padding-bottom: 80px;
}

.detail-content {
  margin-top: 16px;
  max-width: 1100px;
  margin-left: auto;
  margin-right: auto;
}

.section-card {
  margin-bottom: 16px;
  border-radius: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-icon {
  color: #409eff;
  font-size: 18px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.form-area {
  background: #fafbfc;
  padding: 16px;
  border-radius: 6px;
}

.no-form-tip {
  padding: 20px 0;
  text-align: center;
}

.assignee-picker,
.cc-picker {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.picker-hint {
  margin-left: 12px;
  color: #909399;
  font-size: 12px;
}

.footer-action {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  border-top: 1px solid #ebeef5;
  padding: 12px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  z-index: 100;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}
</style>
