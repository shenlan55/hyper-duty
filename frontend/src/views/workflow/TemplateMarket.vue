<template>
  <div class="template-market">
    <el-page-header @back="goBack" content="流程模板市场" style="margin-bottom: 16px;" />

    <el-card>
      <!-- 顶部筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="category" @change="loadList" size="default">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="general">通用审批</el-radio-button>
          <el-radio-button label="leave">请假申请</el-radio-button>
          <el-radio-button label="reimburse">费用报销</el-radio-button>
          <el-radio-button label="trip">出差申请</el-radio-button>
        </el-radio-group>
        <el-input
          v-model="keyword"
          placeholder="搜索模板名称"
          style="width: 240px; margin-left: 16px;"
          clearable
          @clear="loadList"
          @keyup.enter="loadList"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <!-- 模板卡片 -->
      <el-row :gutter="16" v-loading="loading" style="margin-top: 16px;">
        <el-col
          v-for="tpl in filteredList"
          :key="tpl.id"
          :xs="24" :sm="12" :md="8" :lg="6"
          style="margin-bottom: 16px;"
        >
          <el-card shadow="hover" class="tpl-card" @click="previewTemplate(tpl)">
            <div class="tpl-icon">
              <el-icon :size="48"><component :is="iconMap[tpl.icon] || Document" /></el-icon>
            </div>
            <div class="tpl-name">{{ tpl.templateName }}</div>
            <div class="tpl-desc">{{ tpl.description }}</div>
            <div class="tpl-meta">
              <el-tag size="small" :type="categoryTagType(tpl.category)">
                {{ categoryLabel(tpl.category) }}
              </el-tag>
              <span class="use-count">使用 {{ tpl.useCount }} 次</span>
            </div>
            <div class="tpl-actions">
              <el-button type="primary" size="small" @click.stop="useTemplate(tpl)">
                使用此模板新建
              </el-button>
              <el-button size="small" @click.stop="previewTemplate(tpl)">
                预览
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && filteredList.length === 0" description="该分类下暂无模板" />
    </el-card>

    <!-- 预览弹窗（用 base64 svg 渲染） -->
    <el-dialog
      v-if="previewDialog.visible"
      :model-value="true"
      @update:model-value="(v) => !v && (previewDialog.visible = false)"
      :title="previewDialog.title"
      width="800px"
    >
      <div v-html="previewDialog.svg" style="text-align: center; background: #f5f5f5; padding: 20px;" />
      <template #footer>
        <el-button @click="previewDialog.visible = false">关闭</el-button>
        <el-button type="primary" @click="useTemplate(previewDialog.template)">使用此模板新建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, Calendar, Money, Promotion } from '@element-plus/icons-vue'
import { pageTemplates, useTemplate, getTemplate } from '@/api/workflow/template'
import bpmnHelper from '@/utils/bpmn-helper' // 提供 renderBpmnToSvg(xml) 方法（基于 bpmn.js）

export default {
  name: 'TemplateMarket',
  components: { Search },
  setup() {
    const router = useRouter()
    const loading = ref(false)
    const list = ref([])
    const category = ref('')
    const keyword = ref('')
    const previewDialog = reactive({ visible: false, title: '', svg: '', template: null })

    const iconMap = { Document, Calendar, Money, Promotion }

    const categoryLabel = (cat) => ({
      general: '通用', leave: '请假', reimburse: '报销', trip: '出差'
    }[cat] || cat || '其他')

    const categoryTagType = (cat) => ({
      general: 'info', leave: 'success', reimburse: 'warning', trip: 'primary'
    }[cat] || '')

    const filteredList = computed(() => {
      if (!keyword.value) return list.value
      const k = keyword.value.toLowerCase()
      return list.value.filter(t =>
        t.templateName.toLowerCase().includes(k) ||
        (t.description || '').toLowerCase().includes(k)
      )
    })

    const goBack = () => router.push('/workflow/process')

    const loadList = async () => {
      loading.value = true
      try {
        const res = await pageTemplates({ category: category.value || undefined, status: 1, pageNum: 1, pageSize: 100 })
        list.value = res?.data?.records || res?.data || []
      } catch (e) {
        ElMessage.error('加载模板失败：' + (e.message || e))
      } finally {
        loading.value = false
      }
    }

    const previewTemplate = async (tpl) => {
      try {
        const res = await getTemplate(tpl.id)
        const data = res?.data || tpl
        const svg = await bpmnHelper.renderBpmnToSvg(data.bpmnXml)
        previewDialog.title = `预览 - ${data.templateName}`
        previewDialog.svg = svg
        previewDialog.template = data
        previewDialog.visible = true
      } catch (e) {
        ElMessage.error('预览失败：' + (e.message || e))
      }
    }

    const useTemplate = async (tpl) => {
      try {
        await useTemplate(tpl.id)
        // 把模板 XML 写到 sessionStorage，ProcessDesigner 加载时优先使用
        const res = await getTemplate(tpl.id)
        const data = res?.data || tpl
        sessionStorage.setItem('__workflow_pending_xml__', data.bpmnXml)
        ElMessage.success('已加载模板，正在打开设计器...')
        // 关闭预览弹窗
        previewDialog.visible = false
        router.push('/workflow/designer')
      } catch (e) {
        ElMessage.error('加载模板失败：' + (e.message || e))
      }
    }

    onMounted(loadList)

    return {
      loading, list, category, keyword, filteredList, iconMap,
      categoryLabel, categoryTagType,
      previewDialog,
      goBack, loadList, previewTemplate, useTemplate
    }
  }
}
</script>

<style scoped>
.template-market {
  padding: 20px;
}
.filter-bar {
  display: flex;
  align-items: center;
}
.tpl-card {
  cursor: pointer;
  transition: transform .15s;
}
.tpl-card:hover { transform: translateY(-2px); }
.tpl-icon {
  text-align: center;
  color: #409eff;
  margin-bottom: 8px;
}
.tpl-name {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 4px;
}
.tpl-desc {
  font-size: 12px;
  color: #909399;
  min-height: 36px;
  line-height: 1.4;
  margin-bottom: 8px;
}
.tpl-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.use-count { font-size: 12px; color: #909399; }
.tpl-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
}
</style>
