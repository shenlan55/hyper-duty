<template>
  <div class="score-evaluation">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title-with-tip">
            评选排名
            <el-popover
              placement="bottom-start"
              :width="360"
              trigger="hover"
              popper-class="eval-rule-popover"
            >
              <template #reference>
                <el-icon class="rule-tip-icon" :size="16">
                  <QuestionFilled />
                </el-icon>
              </template>
              <div class="rule-tip">
                <div class="rule-title">综合评分计算规则</div>
                <div class="rule-formula">
                  月度综合评分 = 当月积分合计 × 积分权重
                  + 当月工时(h) × 工时权重
                </div>
                <div class="rule-formula-sub">
                  评选排名 = 期间内各月综合评分的平均值
                </div>
                <el-divider style="margin: 10px 0" />
                <div class="rule-current">
                  <div class="rule-current-title">当前周期权重</div>
                  <div class="rule-weight-row" v-if="evalConfig">
                    <span>积分权重：</span>
                    <el-tag size="small" type="primary">
                      {{ evalConfig.pointWeight }}
                    </el-tag>
                  </div>
                  <div class="rule-weight-row" v-if="evalConfig">
                    <span>工时权重：</span>
                    <el-tag size="small" type="success">
                      {{ evalConfig.hourWeight }}
                    </el-tag>
                  </div>
                  <div class="rule-weight-row">
                    <span>权重来源：</span>
                    <el-tag size="small" :type="evalConfig?.source === 'config' ? 'warning' : 'info'">
                      {{ evalConfig?.source === 'config' ? '管理员配置' : '系统默认' }}
                    </el-tag>
                  </div>
                  <div class="rule-weight-note" v-if="evalConfig?.source === 'default'">
                    提示：当前周期未配置权重，公示默认权重 0.6 / 0.4
                  </div>
                </div>
              </div>
            </el-popover>
          </span>
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
                <el-button type="primary" @click="handleQuery">查询排名</el-button>
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
import { QuestionFilled } from '@element-plus/icons-vue'
import { getEvaluationRanking, getEvaluationConfig } from '../../api/score/index.js'
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
// 当前评选周期权重配置（"?"悬浮提示数据源）
const evalConfig = ref(null)

/**
 * 拉取排名数据
 */
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

/**
 * 拉取当前周期权重配置（供"?"悬浮提示展示）
 */
const fetchEvalConfig = async () => {
  try {
    evalConfig.value = await getEvaluationConfig({
      periodType: periodType.value,
      year: periodYear.value,
      periodIndex: periodType.value === 'YEARLY' ? 1 : periodIndex.value
    })
  } catch (e) {
    // 失败时清空，让悬浮提示走"无配置"兜底
    evalConfig.value = null
  }
}

/**
 * 查询按钮：同时刷新排名 + 权重（保持悬浮提示与排名同步）
 */
const handleQuery = () => {
  fetchRanking()
  fetchEvalConfig()
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

onMounted(() => {
  handleQuery()
})
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
/* 标题 + "?" 图标 */
.title-with-tip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.rule-tip-icon {
  cursor: help;
  color: #909399;
  transition: color 0.2s;
}
.rule-tip-icon:hover {
  color: #409eff;
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

<style>
/* el-popover 默认在 scoped 样式里不生效，这里用非 scoped 控制悬浮提示内部样式 */
.eval-rule-popover .rule-tip {
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
}
.eval-rule-popover .rule-title {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
  margin-bottom: 6px;
}
.eval-rule-popover .rule-formula {
  background: #f5f7fa;
  padding: 8px 10px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  color: #303133;
}
.eval-rule-popover .rule-formula-sub {
  margin-top: 6px;
  color: #606266;
  font-size: 12px;
}
.eval-rule-popover .rule-current-title {
  font-weight: 600;
  margin-bottom: 6px;
  color: #303133;
}
.eval-rule-popover .rule-weight-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.eval-rule-popover .rule-weight-note {
  margin-top: 4px;
  color: #e6a23c;
  font-size: 12px;
}
</style>
