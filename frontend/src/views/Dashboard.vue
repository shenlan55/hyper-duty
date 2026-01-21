<template>
  <div class="dashboard-container">
    <h1>欢迎使用 Hyper Duty 系统</h1>
    <div class="dashboard-stats">
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-number">{{ statistics.deptCount || 0 }}</div>
            <div class="stat-label">部门总数</div>
          </div>
          <div class="stat-icon">
            <el-icon class="icon-large"><OfficeBuilding /></el-icon>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-number">{{ statistics.employeeCount || 0 }}</div>
            <div class="stat-label">人员总数</div>
          </div>
          <div class="stat-icon">
            <el-icon class="icon-large"><UserFilled /></el-icon>
          </div>
        </div>
      </el-card>
      <el-card shadow="hover" class="stat-card">
        <div class="stat-content">
          <div class="stat-info">
            <div class="stat-number">{{ statistics.todayLoginCount || 0 }}</div>
            <div class="stat-label">今日登录</div>
          </div>
          <div class="stat-icon">
            <el-icon class="icon-large"><View /></el-icon>
          </div>
        </div>
      </el-card>
    </div>
    <div class="dashboard-tips">
      <el-alert
        title="系统提示"
        type="info"
        description="欢迎使用Hyper Duty系统，这是一个基于Spring Boot+Vue3的部门和人员管理系统。您可以通过左侧菜单访问各种功能模块。"
        show-icon
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { OfficeBuilding, UserFilled, View } from '@element-plus/icons-vue'
import { getDashboardStatistics } from '../api/system/statistics'

const statistics = ref({
  deptCount: 0,
  employeeCount: 0,
  todayLoginCount: 0
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    const response = await getDashboardStatistics()
    if (response.code === 200) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 页面挂载时加载数据
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

h1 {
  font-size: 24px;
  margin-bottom: 15px;
  color: #303133;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 10px;
  margin-bottom: 15px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 10px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.stat-icon {
  color: #409eff;
  margin-left: 10px;
}

.icon-large {
  font-size: 48px;
}

.dashboard-tips {
  margin-top: 10px;
}
</style>