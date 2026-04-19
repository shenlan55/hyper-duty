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

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
              <el-input v-model="form.modelName" placeholder="请输入模型名称（可选）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="提示词模板" prop="promptTemplate">
          <div class="template-toolbar">
            <span class="template-help">快速插入占位符：</span>
            <el-button type="primary" size="small" @click="insertPlaceholder('{projectInfo}')">项目信息</el-button>
            <el-button type="primary" size="small" @click="insertPlaceholder('{taskUpdates}')">任务更新</el-button>
            <el-button type="primary" size="small" @click="insertPlaceholder('{weeklyTaskData}')">周任务数据</el-button>
            <el-button type="info" size="small" @click="loadDefaultTemplate">
              <el-icon><Refresh /></el-icon>
              加载默认模板
            </el-button>
          </div>
          <el-input
            v-model="form.promptTemplate"
            type="textarea"
            :rows="18"
            placeholder="请输入提示词模板，支持占位符如 {projectInfo}, {taskUpdates}, {weeklyTaskData} 等"
          />
          <div class="placeholder-info">
            <p><strong>📝 支持的占位符说明：</strong></p>
            <p>• <code>{projectInfo}</code> - 项目基本信息</p>
            <p>• <code>{taskUpdates}</code> - 今日任务更新（日报专用）</p>
            <p>• <code>{weeklyTaskData}</code> - 一周任务汇总（周报专用）</p>
          </div>
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

    <!-- 模板预览对话框 -->
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

    <!-- 帮助对话框 -->
    <el-dialog
      v-model="helpVisible"
      title="模板使用帮助"
      width="700px"
    >
      <div class="help-content">
        <h3>🎯 占位符说明</h3>
        <ul>
          <li><strong>{projectInfo}</strong> - 项目基本信息，包含项目名称、负责人等</li>
          <li><strong>{taskUpdates}</strong> - 今日任务更新，用于日报生成</li>
          <li><strong>{weeklyTaskData}</strong> - 一周任务汇总，用于周报生成</li>
        </ul>
        
        <h3>💡 使用建议</h3>
        <ul>
          <li>提示词模板应明确报告的输出格式和结构要求</li>
          <li>可以指定报告的风格（简洁、详细、技术导向等）</li>
          <li>建议包含对重点内容的突出显示要求</li>
          <li>可以指定报告的语言风格（正式、友好、专业等）</li>
        </ul>

        <h3>📋 默认模板示例</h3>
        <el-collapse>
          <el-collapse-item title="日报默认模板">
            <pre>请基于以下项目任务数据，按项目分组，生成一份结构化的日报：

【项目信息】
{projectInfo}

【今日任务更新】
{taskUpdates}

重要要求：
1. 按项目分组，每个项目独立成一个大章节
2. 每个项目下分"今日已完成"、"今日进行中"、"明日计划"三部分
3. 严格按照"一、二、三"、"1.2.3."、"①②③"的三级层级输出
4. 【核心要求】：不要简单罗列任务更新，而是要有总结提炼能力！</pre>
          </el-collapse-item>
          <el-collapse-item title="周报默认模板">
            <pre>请基于以下一周的任务数据，按项目分组，生成一份结构化的周报：

【项目信息】
{projectInfo}

【本周任务汇总】
{weeklyTaskData}

重要要求：
1. 按项目分组，每个项目独立成一个大章节
2. 包含"一、周报基本信息"、"二、本周核心成果（按项目）"、"三、本周工作进度复盘"等
3. 【核心要求】：不要简单罗列每天的任务更新，而是要有总结提炼能力！</pre>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Edit, Delete, DocumentCopy, Refresh, Check, Close, QuestionFilled 
} from '@element-plus/icons-vue'
import BaseTable from '@/components/BaseTable.vue'
import { formatDateTime } from '@/utils/dateUtils'
import {
  getConfigList,
  getConfigById,
  saveConfig,
  deleteConfig
} from '@/api/ai-report'

// 响应式数据
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
  modelConfig: null,
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
  { prop: 'configCode', label: '配置编码', width: 150 },
  { prop: 'reportType', label: '报告类型', width: 100, slot: 'reportType' },
  { prop: 'promptTemplate', label: '提示词模板', minWidth: 200, slot: 'promptTemplate' },
  { prop: 'modelName', label: '模型名称', width: 120 },
  { prop: 'status', label: '状态', width: 80, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 180, slot: 'createTime' },
  { prop: 'updateTime', label: '更新时间', width: 180, slot: 'updateTime' },
  { prop: 'operation', label: '操作', width: 250, slot: 'operation', fixed: 'right' }
]

// 默认模板
const defaultTemplates = {
  daily: `请基于以下项目任务数据，按项目分组，生成一份结构化的日报：

【项目信息】
{projectInfo}

【今日任务更新】
{taskUpdates}

重要要求：
1. 按项目分组，每个项目独立成一个大章节
2. 每个项目下分"今日已完成"、"今日进行中"、"明日计划"三部分
3. 严格按照"一、二、三"、"1.2.3."、"①②③"的三级层级输出
4. 【核心要求】：不要简单罗列任务更新，而是要有总结提炼能力！
   - 从各个人的任务更新中总结出项目整体的进展
   - 提取关键点和里程碑成果
   - 把相似的更新合并，突出整体趋势
   - 语言要精炼、专业，突出价值
5. 【非常重要】：任务数据中明确标记了"★★★ 重点任务区域 ★★★"和"◆◆◆ 高优先级任务区域 ◆◆◆"，请在最终报告中严格保留这两个独立区域的划分，重点任务区域放在前面，两个区域之间用明显的分隔线或标题隔开！
6. 只展示你选择的项目的内容，不要展示其他项目的内容！
7. 对于重点任务，要详细描述进展和成果；对于高优先级任务，也要适当展开，但篇幅可以相对短一些！`,
  
  weekly: `请基于以下一周的任务数据，按项目分组，生成一份结构化的周报：

【项目信息】
{projectInfo}

【本周任务汇总】
{weeklyTaskData}

重要要求：
1. 按项目分组，每个项目独立成一个大章节
2. 包含"一、周报基本信息"、"二、本周核心成果（按项目）"、"三、本周工作进度复盘"、"四、项目风险与问题"、"五、下周工作计划（按项目）"
3. 严格按照"一、二、三"、"1.2.3."、"①②③"的三级层级输出
4. 【核心要求】：不要简单罗列每天的任务更新，而是要有总结提炼能力！
   - 把一周的工作归纳成核心成果，不要流水账
   - 突出亮点和关键里程碑
   - 分析进度差异和趋势
   - 语言要精炼、专业，有高度
   - 合并相似任务，提炼共同主题
5. 【非常重要】：任务数据中明确标记了"★★★ 重点任务区域 ★★★"和"◆◆◆ 高优先级任务区域 ◆◆◆"，请在最终报告中严格保留这两个独立区域的划分，重点任务区域放在前面，两个区域之间用明显的分隔线或标题隔开！
6. 只展示你选择的项目的内容，不要展示其他项目的内容！
7. 对于重点任务，要详细描述一周的整体进展；对于高优先级任务，也要适当展开，但篇幅可以相对短一些！`
}

// 方法
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
    modelConfig: null,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑配置'
  try {
    const data = await getConfigById(row.id)
    Object.assign(form, data)
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

// 新增功能方法
const viewTemplate = (row) => {
  currentTemplate.value = row.promptTemplate || ''
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

const insertPlaceholder = (placeholder) => {
  const textarea = document.querySelector('.el-textarea__inner')
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = form.promptTemplate || ''
    form.promptTemplate = text.substring(0, start) + placeholder + text.substring(end)
    
    // 恢复光标位置
    nextTick(() => {
      textarea.focus()
      textarea.setSelectionRange(start + placeholder.length, start + placeholder.length)
    })
  } else {
    form.promptTemplate = (form.promptTemplate || '') + placeholder
  }
}

const loadDefaultTemplate = () => {
  const template = defaultTemplates[form.reportType] || ''
  form.promptTemplate = template
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

.placeholder-info {
  margin-top: 12px;
  padding: 12px;
  background: #f0f9ff;
  border: 1px solid #c6e2ff;
  border-radius: 4px;
  font-size: 13px;
}

.placeholder-info p {
  margin: 4px 0;
  color: #606266;
}

.placeholder-info code {
  background: #e6f7ff;
  padding: 2px 6px;
  border-radius: 3px;
  color: #1890ff;
  font-family: 'Consolas', 'Monaco', monospace;
}

.prompt-preview {
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
