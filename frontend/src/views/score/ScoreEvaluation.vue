<template>
  <div class="score-evaluation">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评选排名</span>
          <div class="header-actions">
            <el-form :inline="true">
              <el-form-item label="周期类型">
                <el-select v-model="periodType" @change="handlePeriodTypeChange" style="width: 120px">
                  <el-option label="季度" value="QUARTERLY" />
                  <el-option label="年度" value="YEARLY" />
                </el-select>
              </el-form-item>
              <el-form-item label="年份">
                <el-input-number v-model="periodYear" :min="2024" :max="2099" />
              </el-form-item>
              <el-form-item v-if="periodType === 'QUARTERLY'" label="季度">
                <el-select v-model="periodIndex" style="width: 140px">
                  <el-option
                    v-for="opt in quarterOptions"
                    :key="opt.value"
                    :label="opt.label"
                    :value="Number(opt.value)"
                  />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="fetchRanking">查询排名</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </template>

      <el-table :data="rankingList" v-loading="loading" border stripe>
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="employeeName" label="员工姓名" width="120" />
        <el-table-column prop="totalScore" label="期间积分合计" width="130" sortable>
          <template #default="{ row }">
            <span :class="row.totalScore >= 0 ? 'score-plus' : 'score-minus'">{{ row.totalScore }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalWorkHours" label="期间工时合计(h)" width="140" sortable />
        <el-table-column prop="avgComprehensiveScore" label="平均综合评分" width="140" sortable>
          <template #default="{ row }">
            <el-tag :type="getRankTag(row)" size="large">{{ Number(row.avgComprehensiveScore).toFixed(2) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEvaluationRanking } from '../../api/score/index.js'
import { useDict } from '../../composables/useDict'

// 业务枚举：季度 走字典
const { options: quarterOptions, loadDict: loadQuarterDict } = useDict('evaluation_quarter')
loadQuarterDict()

const now = new Date()
const periodType = ref('QUARTERLY')
const periodYear = ref(now.getFullYear())
const periodIndex = ref(Math.ceil(now.getMonth() / 3))
const loading = ref(false)
const rankingList = ref([])

const fetchRanking = async () => {
  loading.value = true
  try {
    const res = await getEvaluationRanking({
      periodType: periodType.value,
      year: periodYear.value,
      periodIndex: periodType.value === 'YEARLY' ? 1 : periodIndex.value
    })
    rankingList.value = res || []
  } finally {
    loading.value = false
  }
}

const getRankTag = (row) => {
  const idx = rankingList.value.indexOf(row)
  if (idx === 0) return 'danger'
  if (idx === 1) return 'warning'
  if (idx === 2) return 'success'
  return 'info'
}

const handlePeriodTypeChange = () => {
  if (periodType.value === 'YEARLY') {
    periodIndex.value = 1
  } else {
    periodIndex.value = Math.ceil(now.getMonth() / 3)
  }
}

onMounted(fetchRanking)
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