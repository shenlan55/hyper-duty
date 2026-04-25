<template>
  <div class="ai-report-config">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI报告配置管理</span>
          <div class="header-actions">
            <el-button type="info" @click="showTemplateHelp">
              <el-icon><QuestionFilled /></el-icon>
              模板帮助
            </el-button>
            <el-button type="primary" @click="handleAdd">
              <el-icon><Plus /></el-icon>
              新增配置
            </el-button>
          </div>
        </div>
      </template>

      <BaseTable
        :data="tableData"
        :columns="columns"
        :loading="loading"
        :pagination="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #reportType="{ row }">
          <el-tag :type="row.reportType === 'daily' ? 'primary' : 'success'">
            {{ row.reportType === 'daily' ? '日报' : '周报' }}
          </el-tag>
        </template>
        <template #status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
        <template #promptTemplate="{ row }">
          <div class="prompt-preview">
            {{ row.promptTemplate?.substring(0, 50) }}...
            <el-tooltip content="点击查看完整模板">
              <el-button type="text" size="small" @click="viewTemplate(row)">查看</el-button>
            </el-tooltip>
          </div>
        </template>
        <template #modelParams="{ row }">
          <div class="model-params-preview">
            T:{{ row.temperature || 0.7 }} | Max:{{ row.maxTokens || 4000 }}
          </div>
        </template>
        <template #createTime="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
        <template #updateTime="{ row }">
          {{ formatDateTime(row.updateTime) }}
        </template>
        <template #operation="{ row }">
          <el-button type="primary" size="small" @click="handleEdit(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="warning" size="small" @click="copyTemplate(row)">
            <el-icon><DocumentCopy /></el-icon>
            复制
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </BaseTable>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="1200px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="配置名称" prop="configName">
              <el-input v-model="form.configName" placeholder="请输入配置名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置编码" prop="configCode">
              <el-input v-model="form.configCode" placeholder="请输入配置编码" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="报告类型" prop="reportType">
              <el-select v-model="form.reportType" placeholder="请选择报告类型" style="width: 100%;">
                <el-option label="日报" value="daily" />
                <el-option label="周报" value="weekly" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="form.modelName" placeholder="默认 glm-4-flash" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">模型参数</el-divider>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="温度">
              <el-slider
                v-model="form.temperature"
                :min="0"
                :max="1"
                :step="0.1"
                :marks="{ 0: '精确', 0.5: '平衡', 1: '创意' }"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="最大Token">
              <el-input-number
                v-model="form.maxTokens"
                :min="500"
                :max="8000"
                :step="500"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="Top-P">
              <el-slider
                v-model="form.topP"
                :min="0"
                :max="1"
                :step="0.1"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="重试次数">
              <el-input-number
                v-model="form.maxRetries"
                :min="1"
                :max="5"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">System Prompt</el-divider>
        <el-form-item label="角色设定">
          <el-input
            v-model="form.systemPrompt"
            type="textarea"
            :rows="4"
            placeholder="定义AI的角色和行为，留空使用默认值"
          />
        </el-form-item>

        <el-divider content-position="left">提示词模板</el-divider>
        <el-form-item label="提示词模板" prop="promptTemplate">
          <div class="template-toolbar">
            <span class="template-help">快速加载模板：</span>
            <el-button type="primary" size="small" @click="loadDefaultTemplate">
              <el-icon><Refresh /></el-icon>
              加载默认模板
            </el-button>
          </div>
          <el-input
            v-model="form.promptTemplate"
            type="textarea"
            :rows="12"
            placeholder="请输入提示词模板，数据将以JSON格式提供"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          <el-icon><Close /></el-icon>
          取消
        </el-button>
        <el-button type="primary" @click="handleSubmit">
          <el-icon><Check /></el-icon>
          确定
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="templatePreviewVisible"
      title="提示词模板预览"
      width="800px"
    >
      <div class="template-preview-content">
        <pre>{{ currentTemplate }}</pre>
      </div>
      <template #footer>
        <el-button @click="copyCurrentTemplate">
          <el-icon><DocumentCopy /></el-icon>
          复制模板
        </el-button>
        <el-button type="primary" @click="templatePreviewVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="helpVisible"
      title="模板使用帮助"
      width="700px"
    >
      <div class="help-content">
        <h3>📋 数据说明</h3>
        <p>现在数据以结构化JSON格式提供给AI，包含以下内容：</p>
        <ul>
          <li><strong>projectInfo</strong> - 项目基本信息列表</li>
          <li><strong>dailyTaskData</strong> - 日报任务数据（重点任务+高优先级）</li>
          <li><strong>weeklyTaskData</strong> - 周报任务数据（含每日更新）</li>
        </ul>

        <h3>🎛️ 模型参数说明</h3>
        <ul>
          <li><strong>温度 (Temperature)</strong> - 0=精确/确定性，1=创意/多样性</li>
          <li><strong>最大Token</strong> - 限制AI输出的长度</li>
          <li><strong>Top-P</strong> - 核采样参数，控制输出随机性</li>
          <li><strong>重试次数</strong> - 生成失败时的自动重试次数</li>
        </ul>

        <h3>💡 使用建议</h3>
        <ul>
          <li>日报建议温度0.6-0.7，更注重准确性</li>
          <li>周报建议温度0.7-0.8，可适当发挥总结</li>
          <li>System Prompt定义AI角色，会显著影响输出风格</li>
          <li>重点任务在数据中标记为 isFocus=1，可提示AI特别关注</li>
        </ul>

        <h3>📝 默认模板示例</h3>
        <el-collapse>
          <el-collapse-item title="日报默认模板">
            <pre>请基于上方的JSON项目数据，生成一份高质量的日报。

报告要求：
1. 按项目分组，每个项目独立章节
2. 包含「今日完成」、「进行中」、「明日计划」三个部分
3. 重点任务（isFocus=1）用★特别标注
4. 不要简单罗列，要有总结提炼
5. 使用层级结构：一、二、三 → 1.2.3. → (1)(2)(3)</pre>
          </el-collapse-item>
          <el-collapse-item title="周报默认模板">
            <pre>请基于上方的JSON项目数据，生成一份高质量的周报。

报告要求：
1. 按项目分组，每个项目独立章节
2. 包含「周报概览」、「本周成果」、「进度复盘」、「下周计划」
3. 重点任务（isFocus=1）用★特别标注
4. 要有数据洞察，不要流水账
5. 使用层级结构：一、二、三 → 1.2.3. → (1)(2)(3)</pre>
          </el-collapse-item>
          <el-collapse-item title="System Prompt默认值">
            <pre>你是一位专业的项目管理报告专家，擅长从项目任务数据中提炼有价值的洞察。
你的职责是：
1. 理解并分析提供的JSON格式项目数据
2. 生成结构清晰、内容详实的项目报告
3. 突出重点任务和关键里程碑
4. 语言精炼专业，避免简单罗列
5. 严格按照用户要求的格式输出

报告风格要求：
• 使用清晰的层级结构（一、二、三 → 1.2.3. → (1)(2)(3)）
• 重点内容用★标记
• 保持客观专业的语气</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, DocumentCopy, Refresh, Check, Close, QuestionFilled } from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import { formatDateTime } from '@/utils/dateUtils'
import { getConfigList, getConfigById, saveConfig, deleteConfig } from '@/api/ai-report'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const tableData = ref([])
const templatePreviewVisible = ref(false)
const helpVisible = ref(false)
const currentTemplate = ref('')

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  id: null,
  configName: '',
  configCode: '',
  reportType: 'daily',
  promptTemplate: '',
  modelName: '',
  temperature: 0.7,
  maxTokens: 4000,
  topP: 0.9,
  maxRetries: 3,
  systemPrompt: '',
  status: 1
})

const rules = {
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  configCode: [
    { required: true, message: '请输入配置编码', trigger: 'blur' }
  ],
  reportType: [
    { required: true, message: '请选择报告类型', trigger: 'change' }
  ],
  promptTemplate: [
    { required: true, message: '请输入提示词模板', trigger: 'blur' }
  ]
}

const columns = [
  { prop: 'configName', label: '配置名称', minWidth: 150 },
  { prop: 'configCode', label: '配置编码', width: 120 },
  { prop: 'reportType', label: '报告类型', width: 100, slot: 'reportType' },
  { prop: 'modelParams', label: '模型参数', width: 150, slot: 'modelParams' },
  { prop: 'promptTemplate', label: '提示词模板', minWidth: 200, slot: 'promptTemplate' },
  { prop: 'status', label: '状态', width: 80, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 180, slot: 'createTime' },
  { prop: 'updateTime', label: '更新时间', width: 180, slot: 'updateTime' },
  { prop: 'operation', label: '操作', width: 250, slot: 'operation', fixed: 'right' }
]

const defaultTemplates = {
  daily: `请基于上方的JSON项目数据，生成一份高质量的日报。

报告要求：
1. 按项目分组，每个项目独立章节
2. 包含「今日完成」、「进行中」、「明日计划」三个部分
3. 重点任务（isFocus=1）用★特别标注
4. 不要简单罗列，要有总结提炼
5. 使用层级结构：一、二、三 → 1.2.3. → (1)(2)(3)`,
  weekly: `请基于上方的JSON项目数据，生成一份高质量的周报。

报告要求：
1. 按项目分组，每个项目独立章节
2. 包含「周报概览」、「本周成果」、「进度复盘」、「下周计划」
3. 重点任务（isFocus=1）用★特别标注
4. 要有数据洞察，不要流水账
5. 使用层级结构：一、二、三 → 1.2.3. → (1)(2)(3)`
}

const defaultSystemPrompt = `你是一位专业的项目管理报告专家，擅长从项目任务数据中提炼有价值的洞察。
你的职责是：
1. 理解并分析提供的JSON格式项目数据
2. 生成结构清晰、内容详实的项目报告
3. 突出重点任务和关键里程碑
4. 语言精炼专业，避免简单罗列
5. 严格按照用户要求的格式输出

报告风格要求：
• 使用清晰的层级结构（一、二、三 → 1.2.3. → (1)(2)(3)）
• 重点内容用★标记
• 保持客观专业的语气`

const loadData = async () => {
  loading.value = true
  try {
    const data = await getConfigList()
    tableData.value = data || []
    pagination.total = data.length
  } catch (error) {
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增配置'
  Object.assign(form, {
    id: null,
    configName: '',
    configCode: '',
    reportType: 'daily',
    promptTemplate: '',
    modelName: '',
    temperature: 0.7,
    maxTokens: 4000,
    topP: 0.9,
    maxRetries: 3,
    systemPrompt: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑配置'
  try {
    const data = await getConfigById(row.id)
    Object.assign(form, {
      ...data,
      temperature: data.temperature ?? 0.7,
      maxTokens: data.maxTokens ?? 4000,
      topP: data.topP ?? 0.9,
      maxRetries: data.maxRetries ?? 3
    })
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取配置详情失败')
  }
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    await saveConfig(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (error) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个配置吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteConfig(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadData()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadData()
}

const viewTemplate = (row) => {
  let preview = `【配置名称】${row.configName}\n\n`
  preview += `【System Prompt】\n${row.systemPrompt || '(使用默认值)'}\n\n`
  preview += `【提示词模板】\n${row.promptTemplate || ''}\n\n`
  preview += `【模型参数】\n`
  preview += `• 温度: ${row.temperature ?? 0.7}\n`
  preview += `• 最大Token: ${row.maxTokens ?? 4000}\n`
  preview += `• Top-P: ${row.topP ?? 0.9}\n`
  preview += `• 重试次数: ${row.maxRetries ?? 3}`

  currentTemplate.value = preview
  templatePreviewVisible.value = true
}

const copyTemplate = async (row) => {
  try {
    await navigator.clipboard.writeText(row.promptTemplate || '')
    ElMessage.success('模板已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const copyCurrentTemplate = async () => {
  try {
    await navigator.clipboard.writeText(currentTemplate.value)
    ElMessage.success('模板已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const loadDefaultTemplate = () => {
  const template = defaultTemplates[form.reportType] || ''
  form.promptTemplate = template
  form.systemPrompt = defaultSystemPrompt
  ElMessage.success('已加载默认模板')
}

const showTemplateHelp = () => {
  helpVisible.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.ai-report-config {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.template-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.template-help {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.prompt-preview,
.model-params-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
}

.template-preview-content {
  max-height: 500px;
  overflow-y: auto;
}

.template-preview-content pre {
  white-space: pre-wrap;
  word-wrap: break-word;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.6;
  color: #303133;
  margin: 0;
}

.help-content {
  line-height: 1.8;
}

.help-content h3 {
  color: #303133;
  margin-bottom: 12px;
  font-size: 16px;
}

.help-content ul {
  margin: 0 0 24px 0;
  padding-left: 20px;
  color: #606266;
}

.help-content li {
  margin-bottom: 8px;
}

.help-content pre {
  background: #f5f7fa;
  padding: 16px;
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.6;
  overflow-x: auto;
  white-space: pre-wrap;
  word-wrap: break-word;
  max-height: 300px;
  overflow-y: auto;
}
</style>
