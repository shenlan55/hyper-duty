<template>
  <div class="project-statistics">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>项目统计</span>
        </div>
      </template>

      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value primary">{{ projectStats.total || 0 }}</div>
              <div class="stat-label">项目总数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value success">{{ projectStats.completed || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value warning">{{ projectStats.inProgress || 0 }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-item">
              <div class="stat-value danger">{{ projectStats.overdue || 0 }}</div>
              <div class="stat-label">已逾期</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>项目状态分布</span>
          </template>
          <div ref="projectStatusChart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>任务状态分布</span>
          </template>
          <div ref="taskStatusChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>项目进度统计</span>
          </template>
          <div ref="projectProgressChart" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getProjectPage, getProjectsByStatus } from '@/api/project'
import { getTaskPage } from '@/api/task'

const projectStatusChart = ref(null)
const taskStatusChart = ref(null)
const projectProgressChart = ref(null)

let projectStatusChartInstance = null
let taskStatusChartInstance = null
let projectProgressChartInstance = null

const projectStats = ref({
  total: 0,
  completed: 0,
  inProgress: 0,
  overdue: 0
})

const loadProjectStats = async () => {
  try {
    const [allData, completedData, inProgressData, overdueData] = await Promise.all([
      getProjectPage({ pageNum: 1, pageSize: 1000 }),
      getProjectsByStatus(3),
      getProjectsByStatus(2),
      getProjectsByStatus(4)
    ])
    
    projectStats.value = {
      total: allData.total || 0,
      completed: completedData.length || 0,
      inProgress: inProgressData.length || 0,
      overdue: overdueData.length || 0
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

const initProjectStatusChart = () => {
  if (!projectStatusChart.value) return
  
  projectStatusChartInstance = echarts.init(projectStatusChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '项目状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: projectStats.value.completed, name: '已完成', itemStyle: { color: '#67c23a' } },
          { value: projectStats.value.inProgress, name: '进行中', itemStyle: { color: '#409eff' } },
          { value: projectStats.value.overdue, name: '已逾期', itemStyle: { color: '#f56c6c' } },
          { value: Math.max(0, projectStats.value.total - projectStats.value.completed - projectStats.value.inProgress - projectStats.value.overdue), name: '未开始', itemStyle: { color: '#909399' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  projectStatusChartInstance.setOption(option)
}

const initTaskStatusChart = () => {
  if (!taskStatusChart.value) return
  
  taskStatusChartInstance = echarts.init(taskStatusChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: 0, name: '已完成', itemStyle: { color: '#67c23a' } },
          { value: 0, name: '进行中', itemStyle: { color: '#409eff' } },
          { value: 0, name: '已暂停', itemStyle: { color: '#e6a23c' } },
          { value: 0, name: '未开始', itemStyle: { color: '#909399' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  taskStatusChartInstance.setOption(option)
}

const initProjectProgressChart = () => {
  if (!projectProgressChart.value) return
  
  projectProgressChartInstance = echarts.init(projectProgressChart.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['已完成', '进行中']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: []
    },
    series: [
      {
        name: '已完成',
        type: 'bar',
        stack: 'total',
        data: [],
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '进行中',
        type: 'bar',
        stack: 'total',
        data: [],
        itemStyle: { color: '#409eff' }
      }
    ]
  }
  
  projectProgressChartInstance.setOption(option)
}

const handleResize = () => {
  projectStatusChartInstance?.resize()
  taskStatusChartInstance?.resize()
  projectProgressChartInstance?.resize()
}

onMounted(async () => {
  await loadProjectStats()
  
  setTimeout(() => {
    initProjectStatusChart()
    initTaskStatusChart()
    initProjectProgressChart()
  }, 100)
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  projectStatusChartInstance?.dispose()
  taskStatusChartInstance?.dispose()
  projectProgressChartInstance?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.project-statistics {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-value.primary {
  color: #409eff;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.warning {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.chart-container {
  height: 300px;
  width: 100%;
}
</style>
