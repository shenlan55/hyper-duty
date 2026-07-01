<template>
  <div class="process-list">
    <el-page-header title="流程定义">
      <template #extra>
        <el-button type="primary" @click="goToDesigner">设计流程</el-button>
        <el-button @click="handleSync">同步流程</el-button>
      </template>
    </el-page-header>

    <el-card class="mt-4">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="流程KEY">
          <el-input v-model="searchForm.processKey" placeholder="请输入流程KEY" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <BaseTable
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :backend-pagination="true"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #actions="{ row }">
          <el-button size="small" @click="viewProcess(row)">查看</el-button>
          <el-button size="small" @click="openVersionDialog(row)">版本</el-button>
          <el-button size="small" @click="bindFormDialog(row)">绑定表单</el-button>
          <el-button size="small" type="danger" @click="deleteProcess(row)">删除</el-button>
        </template>
      </BaseTable>
    </el-card>

    <!-- 绑定表单对话框 -->
    <el-dialog
      v-if="bindFormVisible"
      :model-value="true"
      @update:model-value="(v) => !v && (bindFormVisible = false)"
      :title="'绑定表单 - ' + bindFormData.name"
      width="500px"
    >
      <el-form :model="bindFormData" label-width="100px">
        <el-form-item label="表单">
          <el-select v-model="bindFormData.formId" placeholder="请选择表单" clearable>
            <el-option label="无" :value="null" />
            <el-option v-for="form in formList" :key="form.id" :label="form.name" :value="form.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindFormVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBindForm">确定绑定</el-button>
      </template>
    </el-dialog>

    <!-- 版本管理对话框（P1-9） -->
    <!-- v-if 包裹避免 dialog 关闭动画与路由切换冲突导致的 baseTransition null component 错误 -->
    <el-dialog
      v-if="versionDialog.visible"
      :model-value="true"
      @update:model-value="(v) => !v && (versionDialog.visible = false)"
      :title="'版本管理 - ' + versionDialog.processKey"
      width="1200px"
      :close-on-click-modal="false"
    >
      <el-alert
        type="info"
        :closable="false"
        :title="versionTip"
        show-icon
        class="version-tip"
      />
      <el-table
        ref="versionTableRef"
        :data="versionList"
        @selection-change="onVersionSelectionChange"
        style="margin-top: 12px;"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="version" label="版本号" width="100" />
        <el-table-column prop="deploymentId" label="Deployment ID" min-width="220" show-overflow-tooltip />
        <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="key" label="KEY" min-width="160" show-overflow-tooltip />
        <el-table-column prop="suspended" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.suspended" type="warning" size="small">挂起</el-tag>
            <el-tag v-else type="success" size="small">激活</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="previewVersionXml(row)">XML</el-button>
            <el-button size="small" type="warning" @click="rollbackVersion(row)">回滚</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="versionDialog.visible = false">关闭</el-button>
        <el-button type="primary" :disabled="selectedVersions.length !== 2" @click="compareSelectedVersions">对比选中版本</el-button>
      </template>
    </el-dialog>

    <!-- 版本对比结果对话框 -->
    <el-dialog
      v-if="compareDialog.visible"
      :model-value="true"
      @update:model-value="(v) => !v && (compareDialog.visible = false)"
      :title="`版本对比：v${compareDialog.data?.versionA} ↔ v${compareDialog.data?.versionB}`"
      width="1100px"
      :close-on-click-modal="false"
    >
      <div v-if="compareDialog.data">
        <h4>节点差异（{{ (compareDialog.data.nodeDiffs || []).length }}）</h4>
        <el-table :data="compareDialog.data.nodeDiffs" size="small" max-height="280">
          <el-table-column prop="id" label="节点ID" min-width="200" show-overflow-tooltip />
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'ADDED'" type="success" size="small">新增</el-tag>
              <el-tag v-else-if="row.status === 'REMOVED'" type="danger" size="small">删除</el-tag>
              <el-tag v-else-if="row.status === 'MODIFIED'" type="warning" size="small">修改</el-tag>
              <el-tag v-else type="info" size="small">不变</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="变化点" min-width="300">
            <template #default="{ row }">
              <div v-if="row.changes">
                <div v-for="(val, key) in row.changes" :key="key" class="change-item">
                  <strong>{{ key }}：</strong>
                  <span class="old">{{ val[0] }}</span>
                  <span class="arrow">→</span>
                  <span class="new">{{ val[1] }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <h4 style="margin-top: 16px;">连线差异（{{ (compareDialog.data.flowDiffs || []).length }}）</h4>
        <el-table :data="compareDialog.data.flowDiffs" size="small" max-height="200">
          <el-table-column prop="id" label="连线ID" min-width="200" show-overflow-tooltip />
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag v-if="row.status === 'ADDED'" type="success" size="small">新增</el-tag>
              <el-tag v-else-if="row.status === 'REMOVED'" type="danger" size="small">删除</el-tag>
              <el-tag v-else-if="row.status === 'MODIFIED'" type="warning" size="small">修改</el-tag>
              <el-tag v-else type="info" size="small">不变</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <!-- XML 预览对话框 -->
    <el-dialog
      v-model="xmlDialog.visible"
      :title="'BPMN XML 预览 - v' + xmlDialog.version"
      width="900px"
      :close-on-click-modal="false"
    >
      <pre class="xml-pre">{{ xmlDialog.content }}</pre>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import BaseTable from '@/components/BaseTable.vue'
import { pageProcessDefinition, deleteDeployment, syncProcessDefinition as syncProcessDefinitionApi, bindFormToProcess, pageVersions, rollbackVersion as rollbackVersionApi, compareVersions, getProcessBpmnXml } from '@/api/workflow/process'
import { listForm } from '@/api/workflow/form'

export default {
  name: 'ProcessList',
  components: { BaseTable },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const tableData = ref([])
    const formList = ref([])
    const bindFormVisible = ref(false)

    const searchForm = reactive({
      processKey: ''
    })

    const pagination = reactive({
      currentPage: 1,
      pageSize: 10,
      pageSizes: [10, 20, 50, 100],
      total: 0
    })

    const bindFormData = reactive({
      key: '',
      name: '',
      formId: null
    })

    const columns = [
      { prop: 'id', label: 'ID', width: 80 },
      { prop: 'key', label: '流程KEY', minWidth: 180 },
      { prop: 'name', label: '流程名称', minWidth: 180 },
      { prop: 'version', label: '版本', width: 80 },
      { prop: 'resourceName', label: '资源名称', minWidth: 200 },
      { prop: 'actions', label: '操作', width: 250, fixed: 'right' }
    ]

    const loadData = async () => {
      loading.value = true
      try {
        const res = await pageProcessDefinition(pagination.currentPage, pagination.pageSize, searchForm.processKey)
        tableData.value = res.records || []
        pagination.total = res.total || 0
      } catch (error) {
        ElMessage.error(error.message || '加载失败')
      } finally {
        loading.value = false
      }
    }

    const loadFormList = async () => {
      try {
        const res = await listForm({ pageNum: 1, pageSize: 100 })
        formList.value = res.records || []
      } catch (error) {
        ElMessage.error('加载表单列表失败')
      }
    }

    const resetSearch = () => {
      searchForm.processKey = ''
      pagination.currentPage = 1
      loadData()
    }

    const goToDesigner = () => {
      router.push('/workflow/designer')
    }

    const handleSync = async () => {
      try {
        await syncProcessDefinitionApi()
        ElMessage.success('同步成功')
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '同步失败')
      }
    }

    const bindFormDialog = (row) => {
      bindFormData.key = row.key
      bindFormData.name = row.name || row.key
      bindFormData.formId = row.formId || null
      bindFormVisible.value = true
    }

    const handleBindForm = async () => {
      try {
        await bindFormToProcess({
          processKey: bindFormData.key,
          formId: bindFormData.formId || null
        })
        ElMessage.success('绑定成功')
        bindFormVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.message || '绑定失败')
      }
    }

    const viewProcess = (row) => {
      router.push({
        path: '/workflow/designer',
        query: { 
          processDefinitionId: row.id,
          processName: row.name || row.key
        }
      })
    }

    const deleteProcess = async (row) => {
      try {
        await ElMessageBox.confirm('确定要删除该流程吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await deleteDeployment(row.deploymentId)
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error(error.message || '删除失败')
        }
      }
    }

    const handleSizeChange = (size) => {
      pagination.pageSize = size
      loadData()
    }

    const handleCurrentChange = (page) => {
      pagination.currentPage = page
      loadData()
    }

    // ====================== P1-9 流程版本管理 ======================
    // 版本管理对话框状态
    const versionDialog = reactive({ visible: false, processKey: '' })
    const versionList = ref([])
    const selectedVersions = ref([])
    const versionTableRef = ref(null)
    // 版本对比结果对话框
    const compareDialog = reactive({ visible: false, data: null })
    // XML 预览对话框
    const xmlDialog = reactive({ visible: false, content: '', version: '' })
    // 版本管理顶部提示文案
    const versionTip = ref('勾选 2 个版本后可点击"对比选中版本"查看节点与连线差异；点击 XML 可预览/下载 BPMN；点击回滚会把该版本设为最新部署')

    /**
     * 打开版本管理对话框：加载历史版本列表
     */
    const openVersionDialog = async (row) => {
      const key = row.key || row.processKey
      if (!key) {
        ElMessage.warning('该记录没有流程 KEY')
        return
      }
      versionDialog.processKey = key
      versionDialog.visible = true
      versionList.value = []
      selectedVersions.value = []
      try {
        const res = await pageVersions({ processKey: key, pageNum: 1, pageSize: 50 })
        versionList.value = (res.data && res.data.records) || []
      } catch (error) {
        ElMessage.error(error.message || '加载版本列表失败')
      }
    }

    /**
     * 勾选变化：最多选 2 个
     */
    const onVersionSelectionChange = (rows) => {
      if (rows.length > 2) {
        ElMessage.warning('最多只能选 2 个版本进行对比')
        // 去掉刚勾上的多出来的：保留前两个
        const keep = rows.slice(0, 2)
        versionTableRef.value && versionTableRef.value.clearSelection()
        keep.forEach((r) => versionTableRef.value.toggleRowSelection(r, true))
        return
      }
      selectedVersions.value = rows
    }

    /**
     * 对比选中的两个版本
     */
    const compareSelectedVersions = async () => {
      if (selectedVersions.value.length !== 2) {
        ElMessage.warning('请勾选 2 个版本进行对比')
        return
      }
      const [a, b] = selectedVersions.value
      try {
        const res = await compareVersions({ deploymentIdA: a.deploymentId, deploymentIdB: b.deploymentId })
        compareDialog.data = res.data
        compareDialog.visible = true
      } catch (error) {
        ElMessage.error(error.message || '对比失败')
      }
    }

    /**
     * 回滚到指定版本
     */
    const rollbackVersion = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定回滚到 v${row.version}（deploymentId: ${row.deploymentId}）？\n回滚将基于该版本 XML 重新部署为新版本，不会删除历史版本。`,
          '回滚确认',
          { type: 'warning' }
        )
        await rollbackVersionApi({ deploymentId: row.deploymentId })
        ElMessage.success('回滚成功，已生成新版本')
        // 刷新列表
        await openVersionDialog({ key: versionDialog.processKey })
      } catch (error) {
        if (error === 'cancel' || error?.message === 'cancel') return
        ElMessage.error(error.message || '回滚失败')
      }
    }

    /**
     * 预览历史版本 BPMN XML
     */
    const previewVersionXml = async (row) => {
      try {
        const res = await getProcessBpmnXml(row.id)
        xmlDialog.content = res.data || ''
        xmlDialog.version = row.version
        xmlDialog.visible = true
      } catch (error) {
        ElMessage.error(error.message || '加载 XML 失败')
      }
    }

    onMounted(() => {
      loadData()
      loadFormList()
    })

    return {
      loading,
      tableData,
      formList,
      bindFormVisible,
      searchForm,
      pagination,
      bindFormData,
      columns,
      loadData,
      resetSearch,
      goToDesigner,
      handleSync,
      bindFormDialog,
      handleBindForm,
      viewProcess,
      deleteProcess,
      handleSizeChange,
      handleCurrentChange,
      // P1-9 版本管理
      versionDialog,
      versionList,
      selectedVersions,
      versionTableRef,
      compareDialog,
      xmlDialog,
      versionTip,
      openVersionDialog,
      onVersionSelectionChange,
      compareSelectedVersions,
      rollbackVersion,
      previewVersionXml
    }
  }
}
</script>

<style scoped>
.process-list {
  padding: 20px;
}

.mt-4 {
  margin-top: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.version-tip {
  margin-bottom: 12px;
}

.change-item {
  font-size: 12px;
  line-height: 1.8;
}
.change-item .old {
  color: #f56c6c;
  text-decoration: line-through;
  margin: 0 4px;
}
.change-item .new {
  color: #67c23a;
  margin: 0 4px;
}
.change-item .arrow {
  color: #909399;
}

.xml-pre {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 16px;
  border-radius: 4px;
  max-height: 600px;
  overflow: auto;
  font-size: 12px;
  line-height: 1.5;
  font-family: 'Consolas', 'Monaco', monospace;
}
</style>
