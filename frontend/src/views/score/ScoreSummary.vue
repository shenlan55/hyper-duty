<template>
  <div class="score-summary">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>月度汇总</span>
          <div class="header-actions">
            <el-form :inline="true">
              <el-form-item label="年份">
                <el-input-number v-model="year" :min="2024" :max="2099" />
              </el-form-item>
              <el-form-item label="月份">
                <el-select v-model="month" placeholder="选择月份" style="width: 120px">
                  <el-option v-for="m in 12" :key="m" :label="`${m}月`" :value="m" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="fetchSummary">查询</el-button>
                <el-button type="warning" @click="handleGenerate" :loading="generating">生成汇总</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>

      <el-table :data="summaryList" v-loading="loading" border stripe show-summary :summary-method="getSummaries">
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="employeeName" label="员工姓名" width="120" />
        <el-table-column prop="totalScore" label="积分合计" width="100" sortable>
          <template #default="{ row }">
            <span :class="row.totalScore >= 0 ? 'score-plus' : 'score-minus'">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="加班工时(h)" width="110" sortable />
        <el-table-column prop="comprehensiveScore" label="综合评分" width="120" sortable>
          <template #default="{ row }">
            <el-tag type="warning" size="large">{{ Number(row.comprehensiveScore).toFixed(2) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMonthlySummary, generateMonthlySummary } from '../../api/score/index.js'

const now = new Date()
const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)
const loading = ref(false)
const generating = ref(false)
const summaryList = ref([])

const fetchSummary = async () => {
  loading.value = true
  try {
    const res = await getMonthlySummary({ year: year.value, month: month.value })
    summaryList.value = res || []
  } finally {
    loading.value = false
  }
}

const handleGenerate = async () => {
  generating.value = true
  try {
    await generateMonthlySummary({ year: year.value, month: month.value })
    ElMessage.success('汇总生成成功')
    fetchSummary()
  } finally {
    generating.value = false
  }
}

const getSummaries = (param) => {
  const { columns, data } = param
  const sums = []
  columns.forEach((col, index) => {
    if (index === 0) {
      sums[index] = '合计'
      return
    }
    if (col.property === 'totalScore' || col.property === 'workHours' || col.property === 'comprehensiveScore') {
      const values = data.map(item => Number(item[col.property]) || 0)
      const sum = values.reduce((prev, curr) => prev + curr, 0)
      sums[index] = col.property === 'comprehensiveScore' ? Number(sum).toFixed(2) : sum
    } else {
      sums[index] = ''
    }
  })
  return sums
}

onMounted(fetchSummary)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
}
.score-plus {
  color: #67c23a;
  font-weight: bold;
}
.score-minus {
  color: #f56c6c;
  font-weight: bold;
}
</style>