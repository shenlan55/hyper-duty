<template>
  <div class="statistics-container">
    <div class="page-header">
      <h2>排班统计</h2>
      <el-button type="primary" @click="exportExcel" :loading="exportLoading">
        <el-icon><Download /></el-icon>
        导出Excel
      </el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">排班概览</div>
          <div class="stat-content">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.overall?.totalSchedules || 0 }}</div>
              <div class="stat-label">总排班数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statistics.overall?.totalAssignments || 0 }}</div>
              <div class="stat-label">总值班安排</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statistics.overall?.totalRecords || 0 }}</div>
              <div class="stat-label">总值班记录</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">出勤统计</div>
          <div class="stat-content">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.overall?.avgDailyHours || 0 }}</div>
              <div class="stat-label">日均时长(小时)</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statistics.overall?.totalOvertimeHours || 0 }}</div>
              <div class="stat-label">总加班时长(小时)</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ statistics.overall?.totalLeaveRequests || 0 }}</div>
              <div class="stat-label">请假申请数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">部门统计</div>
          <div class="stat-content">
            <div class="stat-item">
              <div class="stat-value">{{ statistics.dept?.length || 0 }}</div>
              <div class="stat-label">部门数量</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ getTotalAssignments }}</div>
              <div class="stat-label">总值班次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ getAvgAssignments }}</div>
              <div class="stat-label">平均值班次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <div class="chart-title">班次分布</div>
          <div ref="shiftChartRef" style="height: 300px" v-loading="loading"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <div class="chart-title">月度出勤趋势</div>
          <div ref="trendChartRef" style="height: 300px" v-loading="loading"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="table-card">
      <div class="chart-header">
        <h3>员工排班统计</h3>
        <div class="filter-controls">
          <el-date-picker
            v-model="selectedDate"
            type="month"
            placeholder="选择月份"
            format="YYYY年MM月"
            value-format="YYYY-MM"
            @change="handleDateChange"
            style="margin-right: 10px"
          />
          <el-button type="primary" @click="fetchEmployeeStatistics" :loading="employeeLoading">
            查询
          </el-button>
        </div>
      </div>
      <el-table :data="employeeStatistics" border stripe v-loading="employeeLoading">
        <el-table-column prop="employeeName" label="员工姓名" width="120" />
        <el-table-column label="年月" width="100">
          <template #default="scope">
            {{ scope.row.year }}年{{ scope.row.month }}月
          </template>
        </el-table-column>
        <el-table-column prop="plannedHours" label="计划工时(小时)" width="150" />
        <el-table-column prop="actualHours" label="实际工时(小时)" width="150" />
        <el-table-column prop="actualDays" label="实际天数" width="120" />
        <el-table-column label="完成率" width="120">
          <template #default="scope">
            {{ getCompletionRate(scope.row) }}%
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { getAllStatistics, exportStatisticsExcel, getEmployeeStatistics } from '@/api/duty/statistics'

const loading = ref(false)
const exportLoading = ref(false)
const employeeLoading = ref(false)
const shiftChartRef = ref()
const trendChartRef = ref()

const statistics = reactive({
  overall: {},
  dept: [],
  shift: [],
  trend: []
})

const employeeStatistics = ref([])
// 默认查询当前月份
const currentDate = new Date()
const formattedMonth = currentDate.getFullYear() + '-' + String(currentDate.getMonth() + 1).padStart(2, '0')
const selectedDate = ref(formattedMonth)

const getTotalAssignments = computed(() => {
  return statistics.dept.reduce((sum, dept) => sum + (dept.assignmentCount || 0), 0)
})

const getAvgAssignments = computed(() => {
  const total = getTotalAssignments.value
  const count = statistics.dept.length || 0
  return count > 0 ? (total / count).toFixed(1) : 0
})

const getCompletionRate = (row) => {
  if (!row.plannedHours || row.plannedHours === 0) return 0
  return ((row.actualHours / row.plannedHours) * 100).toFixed(1)
}

const fetchStatistics = async () => {
  loading.value = true
  try {
    const response = await getAllStatistics()
    if (response.code === 200) {
      Object.assign(statistics, response.data)
      await nextTick()
      initCharts()
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  initShiftChart()
  initTrendChart()
}

const initShiftChart = () => {
  if (!shiftChartRef.value) return
  
  const chart = echarts.init(shiftChartRef.value)
  
  const shiftData = statistics.shift || []
  const data = shiftData.map(item => ({
    value: item.count,
    name: item.shiftName
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '班次分布',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        labelLine: {
          show: false
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{b}: {c}'
        }
      }
    ]
  }
  chart.setOption(option)
  
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

const initTrendChart = () => {
  if (!trendChartRef.value) return
  
  const chart = echarts.init(trendChartRef.value)
  
  const trendData = statistics.trend || []
  const months = trendData.map(item => item.month)
  const hours = trendData.map(item => item.hours || 0)
  const overtime = trendData.map(item => item.overtime || 0)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['出勤时长', '加班时长']
    },
    xAxis: {
      type: 'category',
      data: months
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '出勤时长',
        type: 'bar',
        data: hours,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '加班时长',
        type: 'bar',
        data: overtime,
        itemStyle: {
          color: '#F56C6C'
        }
      }
    ]
  }
  chart.setOption(option)
  
  window.addEventListener('resize', () => {
    chart.resize()
  })
}

const exportExcel = async () => {
  exportLoading.value = true
  try {
    const response = await exportStatisticsExcel()
    
    const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = `排班统计_${new Date().getTime()}.xlsx`
    link.click()
    URL.revokeObjectURL(link.href)
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const handleDateChange = (value) => {
  selectedDate.value = value
}

const fetchEmployeeStatistics = async () => {
  employeeLoading.value = true
  try {
    let year = null
    let month = null
    if (selectedDate.value) {
      const [yearStr, monthStr] = selectedDate.value.split('-')
      year = parseInt(yearStr)
      month = parseInt(monthStr)
    }
    
    const response = await getEmployeeStatistics(year, month)
    if (response.code === 200) {
      employeeStatistics.value = response.data
    }
  } catch (error) {
    console.error('获取员工统计数据失败:', error)
    ElMessage.error('获取员工统计数据失败')
  } finally {
    employeeLoading.value = false
  }
}

onMounted(async () => {
  await fetchStatistics()
  await fetchEmployeeStatistics()
})
</script>

<style scoped>
.statistics-container {
  padding: 10px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
}

.stat-content {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-card {
  margin-bottom: 20px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.chart-title {
  font-size: 14px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 10px;
}
</style>
