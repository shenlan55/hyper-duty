<template>
  <MobileGuard>
    <div class="process-start-list">
      <el-page-header title="发起流程">
        <template #extra>
          <el-button @click="loadData">
            <el-icon><refresh /></el-icon>
            刷新
          </el-button>
        </template>
      </el-page-header>

      <!-- 顶部统计与筛选 -->
      <el-card class="mt-4 header-card" shadow="never">
        <div class="header-row">
          <div class="stat-block">
            <div class="stat-value">{{ processList.length }}</div>
            <div class="stat-label">可发起流程</div>
          </div>
          <el-divider direction="vertical" />
          <div class="stat-block">
            <div class="stat-value">{{ filteredList.length }}</div>
            <div class="stat-label">当前显示</div>
          </div>
        </div>
        <el-divider />
        <el-form inline size="default">
          <el-form-item label="流程分类">
            <el-select
              v-model="filterCategoryId"
              placeholder="全部分类"
              clearable
              style="width: 200px"
              @change="onCategoryChange"
            >
              <el-option label="未分类" :value="0" />
              <el-option
                v-for="cat in categoryList"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="搜索">
            <el-input
              v-model="searchKeyword"
              placeholder="按流程名称 / KEY 搜索"
              clearable
              style="width: 260px"
              :prefix-icon="Search"
            />
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 分类 Tab 切换 -->
      <el-tabs v-model="activeCategory" class="mt-4 category-tabs" @tab-change="handleCategoryTabChange">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane
          v-for="cat in categoryList"
          :key="cat.id"
          :label="cat.name"
          :name="String(cat.id)"
        />
        <el-tab-pane label="未分类" name="0" />
      </el-tabs>

      <!-- 卡片网格 -->
      <div v-loading="loading" class="card-grid">
        <el-empty v-if="!loading && filteredList.length === 0" description="暂无可发起的流程" />

        <div
          v-for="item in filteredList"
          :key="item.key"
          class="process-card"
          @click="goStart(item)"
        >
          <div class="card-top" :style="{ background: pickColor(item.key) }">
            <el-icon class="card-icon"><document /></el-icon>
            <span class="card-version">v{{ item.version || 1 }}</span>
          </div>
          <div class="card-body">
            <div class="card-title" :title="item.name || item.key">
              {{ item.name || item.key }}
            </div>
            <div class="card-key" :title="item.key">KEY: {{ item.key }}</div>
            <div class="card-desc">
              {{ item.remark || '点击进入填写表单，发起审批流程' }}
            </div>
            <div class="card-meta">
              <el-tag v-if="getCategoryName(item.categoryId)" size="small" effect="plain" type="info">
                {{ getCategoryName(item.categoryId) }}
              </el-tag>
              <el-tag v-if="item.formId" size="small" effect="plain" type="success">
                <el-icon><tickets /></el-icon>
                <span style="margin-left: 2px">已绑表单</span>
              </el-tag>
              <el-tag v-else size="small" effect="plain" type="warning">无表单</el-tag>
            </div>
          </div>
          <div class="card-footer">
            <span class="footer-time">
              {{ item.deploymentTime ? formatTime(item.deploymentTime) : '' }}
            </span>
            <el-button type="primary" size="small" :icon="Promotion" @click.stop="goStart(item)">
              发起
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </MobileGuard>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  Tickets,
  Promotion,
  Search,
  Refresh
} from '@element-plus/icons-vue'
import MobileGuard from '@/components/MobileGuard.vue'
import { listCategory } from '@/api/workflow/category'
import { listProcessDefinition } from '@/api/workflow/process'

const router = useRouter()

// 状态
const loading = ref(false)
const processList = ref([])
const categoryList = ref([])
const searchKeyword = ref('')
const filterCategoryId = ref(null)
const activeCategory = ref('all')

// 调色板（按 key 哈希分配背景色）
const colorPalette = [
  'linear-gradient(135deg, #409EFF 0%, #79bbff 100%)',
  'linear-gradient(135deg, #67C23A 0%, #95d475 100%)',
  'linear-gradient(135deg, #E6A23C 0%, #eebe77 100%)',
  'linear-gradient(135deg, #F56C6C 0%, #f89898 100%)',
  'linear-gradient(135deg, #909399 0%, #b1b3b8 100%)',
  'linear-gradient(135deg, #8e44ad 0%, #b16cea 100%)',
  'linear-gradient(135deg, #16a085 0%, #1abc9c 100%)',
  'linear-gradient(135deg, #d35400 0%, #e67e22 100%)'
]
const pickColor = (key = '') => {
  let h = 0
  for (let i = 0; i < key.length; i++) h = (h * 31 + key.charCodeAt(i)) >>> 0
  return colorPalette[h % colorPalette.length]
}

// 过滤后的列表
const filteredList = computed(() => {
  let list = processList.value
  if (activeCategory.value !== 'all') {
    const cid = activeCategory.value === '0' ? 0 : Number(activeCategory.value)
    list = list.filter(p => (p.categoryId || 0) === cid)
  }
  if (filterCategoryId.value !== null && filterCategoryId.value !== undefined) {
    const cid = filterCategoryId.value
    list = list.filter(p => (p.categoryId || 0) === cid)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase().trim()
    list = list.filter(p =>
      (p.name || '').toLowerCase().includes(kw) ||
      (p.key || '').toLowerCase().includes(kw)
    )
  }
  return list
})

// 分类名称映射
const categoryMap = computed(() => {
  const m = new Map()
  categoryList.value.forEach(c => m.set(c.id, c.name))
  return m
})
const getCategoryName = (cid) => {
  if (!cid) return ''
  return categoryMap.value.get(cid) || ''
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const [catRes, procRes] = await Promise.all([
      listCategory(),
      listProcessDefinition()
    ])
    categoryList.value = catRes.records || catRes || []
    // 后端 list 接口可能返回字符串/对象数组，统一处理
    processList.value = (procRes || []).map(p => ({
      id: p.id,
      key: p.key,
      name: p.name,
      version: p.version,
      categoryId: p.categoryId,
      formId: p.formId,
      deploymentTime: p.deploymentTime,
      remark: p.remark
    }))
  } catch (error) {
    ElMessage.error(error.message || '加载流程失败')
  } finally {
    loading.value = false
  }
}

const onCategoryChange = () => {
  // 与 Tab 联动：保持简单，两种筛选取并集由 computed 完成
}

const handleCategoryTabChange = (name) => {
  filterCategoryId.value = name === 'all' ? null : Number(name)
}

const goStart = (item) => {
  if (!item.key) {
    ElMessage.warning('该流程缺少KEY，无法发起')
    return
  }
  router.push({
    path: '/workflow/start/detail',
    query: {
      processDefinitionKey: item.key,
      processName: item.name || item.key
    }
  })
}

const formatTime = (t) => {
  if (!t) return ''
  try {
    const d = new Date(t)
    const pad = (n) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch (e) {
    return ''
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.process-start-list {
  padding: 12px;
}

.header-card {
  border-radius: 8px;
}

.header-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-block {
  text-align: center;
  padding: 0 16px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.category-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 0 12px;
}

.category-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  padding: 16px 0 24px;
}

.process-card {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.25s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  border: 1px solid #ebeef5;
}

.process-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.18);
  border-color: #c6e2ff;
}

.card-top {
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  color: #fff;
}

.card-icon {
  font-size: 32px;
  opacity: 0.95;
}

.card-version {
  font-size: 12px;
  background: rgba(255, 255, 255, 0.25);
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 500;
}

.card-body {
  padding: 14px 18px 10px;
  flex: 1;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.card-key {
  font-size: 12px;
  color: #909399;
  font-family: 'Consolas', monospace;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  height: 40px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 10px;
}

.card-meta {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.card-footer {
  border-top: 1px solid #f0f2f5;
  padding: 10px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fafbfc;
}

.footer-time {
  font-size: 12px;
  color: #909399;
}
</style>
