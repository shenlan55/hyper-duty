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
    <div class="dashboard-todo">
      <el-card shadow="hover" class="todo-card">
        <template #header>
          <div class="card-header">
            <span>待办事项</span>
            <el-button type="primary" size="small" @click="refreshTodoList">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </template>
        <div v-if="todoLoading" class="loading-container">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else-if="todoList.length === 0" class="empty-todo">
          <el-empty description="暂无待办事项" />
        </div>
        <div v-else class="todo-list">
          <div v-for="(group, groupIndex) in groupedTodoList" :key="group.type" class="todo-group">
            <el-divider :content-position="'left'">
              {{ group.title }} ({{ group.items.length }})
            </el-divider>
            <el-card
              v-for="(todo, index) in group.items"
              :key="todo.id"
              class="todo-item-card"
              @click="handleTodoClick(todo)"
            >
              <div class="todo-item-content">
                <div class="todo-item-header">
                  <span class="todo-item-title">{{ todo.title }}</span>
                  <el-tag :type="getTodoTypeColor(todo.type)">{{ getTodoTypeName(todo.type) }}</el-tag>
                </div>
                <div class="todo-item-info">
                  <div class="todo-item-meta">
                    <span class="todo-item-creator">{{ todo.creator || '未知' }}</span>
                    <span class="todo-item-time">{{ formatDateTime(todo.createTime) }}</span>
                  </div>
                  <div class="todo-item-description">{{ todo.description }}</div>
                </div>
              </div>
            </el-card>
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
import { ref, onMounted, computed } from 'vue'
import { OfficeBuilding, UserFilled, View, Refresh } from '@element-plus/icons-vue'
import { getDashboardStatistics } from '../api/system/statistics'
import { getPendingApprovals } from '../api/duty/leaveRequest'
import { getMySwapRequests } from '../api/duty/swapRequest'
import { getPendingOvertimeApprovals } from '../api/duty/overtimeRequest'
import { getAssignmentList } from '../api/duty/assignment'
import { getAllSchedules } from '../api/duty/schedule'
import { getEmployeeList } from '../api/employee'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { formatDateTime } from '../utils/dateUtils'

const router = useRouter()
const userStore = useUserStore()

const statistics = ref({
  deptCount: 0,
  employeeCount: 0,
  todayLoginCount: 0
})

const todoList = ref([])
const todoLoading = ref(false)

// 加载统计数据
const loadStatistics = async () => {
  try {
    const data = await getDashboardStatistics()
    statistics.value = data || {}
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 加载待办事项
const loadTodoList = async () => {
  todoLoading.value = true
  try {
    const employeeId = userStore.employeeId || 1
    
    // 初始化待办事项数组
    let leaveTodos = []
    let swapTodos = []
    let overtimeTodos = []
    
    // 获取值班安排、值班表和员工信息
    let assignments = []
    let schedules = []
    let employees = []
    
    try {
      // 获取所有值班安排
      const assignmentData = await getAssignmentList()
      if (Array.isArray(assignmentData)) {
        assignments = assignmentData
      }
    } catch (error) {
      console.error('获取值班安排失败:', error)
    }
    
    try {
      // 获取所有值班表
      const scheduleData = await getAllSchedules()
      if (Array.isArray(scheduleData)) {
        schedules = scheduleData
      }
    } catch (error) {
      console.error('获取值班表失败:', error)
    }
    
    try {
      // 获取所有员工信息
      const employeeData = await getEmployeeList()
      if (employeeData?.records && Array.isArray(employeeData.records)) {
        employees = employeeData.records
      }
    } catch (error) {
      console.error('获取员工信息失败:', error)
    }
    
    try {
      // 获取待审批的请假申请
      const leaveData = await getPendingApprovals(employeeId)
      if (Array.isArray(leaveData)) {
        leaveTodos = leaveData
          // 只显示待审批的请假申请
          .filter(item => item.approvalStatus === '待审批' || item.approvalStatus === 'pending')
          .map(item => {
            // 获取提出人姓名，首先尝试直接从item中获取，然后根据employeeId匹配
            let creatorName = item.employeeName || item.name || item.employee || '未知'
            if (creatorName === '未知' && item.employeeId) {
              const employee = employees.find(e => e.id === item.employeeId)
              if (employee) {
                creatorName = employee.name || employee.employeeName || '未知'
              }
            }
            
            return {
              id: item.id,
              type: 'leave',
              title: `请假审批 - ${item.requestNo || item.id}`,
              creator: creatorName,
              createTime: item.createTime,
              description: `${creatorName} 申请 ${item.leaveType ? item.leaveType === 1 ? '事假' : item.leaveType === 2 ? '病假' : item.leaveType === 3 ? '年假' : item.leaveType === 4 ? '调休' : '其他' : '未知'}，时长 ${item.totalHours || 0} 小时`,
              data: {
                ...item,
                employeeName: creatorName,
                scheduleId: item.scheduleId
              }
            }
          })
      }
    } catch (error) {
      console.error('获取请假审批失败:', error)
    }
    
    try {
      // 获取待审批的调班申请
      const swapData = await getMySwapRequests(employeeId)
      if (Array.isArray(swapData)) {
        swapTodos = swapData
          // 只显示待当前用户处理的调班申请
          .filter(item => item.approvalStatus === 'pending' && item.targetEmployeeId === employeeId)
          .map(item => {
          // 获取提出人姓名，首先尝试直接从item中获取，然后根据originalEmployeeId匹配
          let creatorName = item.originalEmployeeName || item.employeeName || item.name || '未知'
          if (creatorName === '未知' && item.originalEmployeeId) {
            const employee = employees.find(e => e.id === item.originalEmployeeId)
            if (employee) {
              creatorName = employee.name || employee.employeeName || '未知'
            }
          }
          
          return {
            id: item.id,
            type: 'swap',
            title: `调班确认 - ${item.requestNo || item.id}`,
            creator: creatorName,
            createTime: item.createTime,
            description: `${creatorName} 申请与您调班，日期：${item.originalSwapDate || item.swapDate}`,
            data: {
              ...item,
              originalEmployeeName: creatorName,
              scheduleId: item.scheduleId
            }
          }
        })
      }
    } catch (error) {
      console.error('获取调班确认失败:', error)
    }
    
    try {
      // 获取待审批的加班申请
      const overtimeData = await getPendingOvertimeApprovals(employeeId)
      if (Array.isArray(overtimeData)) {
        overtimeTodos = overtimeData.map(item => {
          // 根据assignmentId找到对应的值班安排
          const assignment = assignments.find(a => a.id === item.assignmentId)
          let scheduleId = null
          let scheduleName = '未知值班表'
          
          if (assignment) {
            // 根据值班安排的scheduleId找到对应的值班表
            scheduleId = assignment.scheduleId
            const schedule = schedules.find(s => s.id === assignment.scheduleId)
            if (schedule) {
              scheduleName = schedule.scheduleName
            }
          }
          
          // 获取提出人姓名，首先尝试直接从item中获取，然后根据employeeId匹配
          let creatorName = item.employeeName || item.name || item.employee || '未知'
          if (creatorName === '未知' && item.employeeId) {
            const employee = employees.find(e => e.id === item.employeeId)
            if (employee) {
              creatorName = employee.name || employee.employeeName || '未知'
            }
          }
          
          return {
            id: item.id,
            type: 'overtime',
            title: `加班审批 - ${item.requestNo || item.id}`,
            creator: creatorName,
            createTime: item.createTime,
            description: `${creatorName} 申请加班，时长 ${item.overtimeHours || item.totalHours || 0} 小时，值班表：${scheduleName}`,
            data: {
              ...item,
              scheduleId: scheduleId,
              scheduleName: scheduleName,
              employeeName: creatorName
            }
          }
        })
      }
    } catch (error) {
      console.error('获取加班审批失败:', error)
    }
    
    // 合并待办事项
    // 后端已经处理了值班长权限判断，前端直接显示返回的数据
    let allTodos = [...leaveTodos, ...swapTodos, ...overtimeTodos]
    
    todoList.value = allTodos.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  } catch (error) {
    console.error('获取待办事项失败:', error)
    todoList.value = []
  } finally {
    todoLoading.value = false
  }
}

// 按类型分组待办事项
const groupedTodoList = computed(() => {
  const groups = {
    leave: {
      type: 'leave',
      title: '请假审批',
      items: []
    },
    swap: {
      type: 'swap',
      title: '调班确认',
      items: []
    },
    overtime: {
      type: 'overtime',
      title: '加班审批',
      items: []
    }
  }
  
  todoList.value.forEach(todo => {
    if (groups[todo.type]) {
      groups[todo.type].items.push(todo)
    }
  })
  
  return Object.values(groups).filter(group => group.items.length > 0)
})

// 处理待办事项点击
const handleTodoClick = (todo) => {
  if (todo.type === 'leave') {
    // 跳转到请假审批页面
    router.push({
      path: '/duty/leave-approval',
      query: {
        activeTab: 'pending',
        scheduleId: todo.data.scheduleId
      }
    })
  } else if (todo.type === 'swap') {
    // 跳转到调班管理页面
    router.push({
      path: '/duty/swap-request',
      query: {
        approvalStatus: 'pending',
        scheduleId: todo.data.scheduleId
      }
    })
  } else if (todo.type === 'overtime') {
    // 跳转到加班审批页面
    router.push({
      path: '/duty/record',
      query: {
        activeTab: 'approval',
        scheduleId: todo.data.scheduleId
      }
    })
  }
}

// 获取待办事项类型名称
const getTodoTypeName = (type) => {
  const typeMap = {
    leave: '请假审批',
    swap: '调班确认',
    overtime: '加班审批'
  }
  return typeMap[type] || '未知'
}

// 获取待办事项类型颜色
const getTodoTypeColor = (type) => {
  const colorMap = {
    leave: 'warning',
    swap: 'primary',
    overtime: 'success'
  }
  return colorMap[type] || 'info'
}

// 刷新待办事项列表
const refreshTodoList = () => {
  loadTodoList()
}

// 页面挂载时加载数据
onMounted(() => {
  loadStatistics()
  loadTodoList()
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

.stat-icon {
  color: #409eff;
  margin-left: 10px;
}

.icon-large {
  font-size: 48px;
}

.dashboard-todo {
  margin-bottom: 15px;
}

.todo-card {
  margin-bottom: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.empty-todo {
  padding: 40px 0;
  text-align: center;
}

.todo-list {
  margin-top: 10px;
}

.todo-group {
  margin-bottom: 20px;
}

.todo-item-card {
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.todo-item-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #c6e2ff;
}

.todo-item-content {
  padding: 10px 0;
}

.todo-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.todo-item-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 10px;
}

.todo-item-info {
  margin-top: 8px;
}

.todo-item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
  font-size: 12px;
  color: #909399;
}

.todo-item-creator {
  flex: 1;
}

.todo-item-time {
  text-align: right;
}

.todo-item-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
  margin-top: 5px;
  word-break: break-all;
}

.dashboard-tips {
  margin-top: 10px;
}
</style>