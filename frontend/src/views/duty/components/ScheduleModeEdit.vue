<template>
  <div class="schedule-mode-edit">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
    >
      <!-- 基本信息 -->
      <el-form-item label="排班模式名称" prop="modeName">
        <el-input v-model="formData.modeName" placeholder="请输入排班模式名称" />
      </el-form-item>

      <el-form-item label="编码" prop="modeCode">
        <el-input v-model="formData.modeCode" placeholder="请输入编码" />
      </el-form-item>

      <el-form-item label="类型" prop="modeType">
        <el-select v-model="formData.modeType" placeholder="请选择类型">
          <el-option label="轮班制" value="1" />
          <el-option label="综合制" value="2" />
          <el-option label="弹性制" value="3" />
          <el-option label="自定义" value="4" />
        </el-select>
      </el-form-item>

      <el-form-item label="描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          placeholder="请输入描述"
          :rows="3"
        />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" :max="999" />
      </el-form-item>

      <el-form-item label="状态">
        <el-switch
          v-model="formData.status"
          active-value="1"
          inactive-value="0"
        />
      </el-form-item>

      <!-- 排班模式配置 -->
      <el-form-item label="排班模式配置">
        <div class="schedule-config">
          <div class="config-header">
            <span class="day-header">天</span>
            <div
              v-for="(day, index) in daysConfig"
              :key="day.dayIndex"
              class="day-column"
            >
              <div class="day-info">
                <span>{{ day.name }}</span>
                <el-button
                  v-if="index > 2"
                  type="danger"
                  size="small"
                  circle
                  @click="removeDay(index)"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-button
              type="primary"
              size="small"
              @click="addDay"
            >
              <el-icon><Plus /></el-icon> 添加天
            </el-button>
          </div>

          <!-- 班次选择行 -->
          <div class="config-row">
            <span class="row-label">班次</span>
            <div
              v-for="(day, dayIndex) in daysConfig"
              :key="`shift-${day.dayIndex}`"
              class="day-column"
            >
              <div class="shift-container">
                <div
                  v-for="(shift, shiftIndex) in day.shifts"
                  :key="`shift-${day.dayIndex}-${shiftIndex}`"
                  class="shift-item"
                >
                  <el-select
                    v-model="shift.shiftId"
                    placeholder="选择班次"
                    class="shift-select"
                  >
                    <el-option
                      v-for="shiftConfig in enabledShifts"
                      :key="shiftConfig.id"
                      :label="shiftConfig.shiftName"
                      :value="shiftConfig.id.toString()"
                    />
                  </el-select>
                  <el-button
                    v-if="shiftIndex > 0"
                    type="danger"
                    size="small"
                    circle
                    @click="removeShift(dayIndex, shiftIndex)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <el-button
                  type="primary"
                  size="small"
                  @click="addShift(dayIndex)"
                >
                  <el-icon><Plus /></el-icon> 添加班次
                </el-button>
              </div>
            </div>
          </div>

          <!-- 人数设置行 -->
          <div class="config-row">
            <span class="row-label">人数</span>
            <div
              v-for="(day, dayIndex) in daysConfig"
              :key="`count-${day.dayIndex}`"
              class="day-column"
            >
              <div class="count-container">
                <div
                  v-for="(shift, shiftIndex) in day.shifts"
                  :key="`count-${day.dayIndex}-${shiftIndex}`"
                  class="count-item"
                >
                  <el-input-number
                    v-model="shift.count"
                    :min="0"
                    :max="99"
                    class="count-input"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="handleSubmit">保存</el-button>
          <el-button @click="handleCancel">取消</el-button>
        </div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { scheduleModeApi } from '@/api/duty/scheduleMode'
import { shiftConfigApi } from '@/api/duty/shiftConfig'
import { Delete, Plus } from '@element-plus/icons-vue'

const props = defineProps({
  modeId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['save-success', 'cancel'])

const formRef = ref(null)
const formData = reactive({
  id: null,
  modeName: '',
  modeCode: '',
  modeType: '1',
  algorithmClass: '',
  configJson: '',
  description: '',
  status: '1',
  sort: 0
})

const rules = {
  modeName: [
    { required: true, message: '请输入排班模式名称', trigger: 'blur' }
  ],
  modeCode: [
    { required: true, message: '请输入编码', trigger: 'blur' }
  ],
  modeType: [
    { required: true, message: '请选择类型', trigger: 'change' }
  ]
}

const daysConfig = ref([
  {
    dayIndex: 1,
    name: '第一天',
    shifts: [
      { shiftId: '', shiftName: '', count: 0 }
    ]
  },
  {
    dayIndex: 2,
    name: '第二天',
    shifts: [
      { shiftId: '', shiftName: '', count: 0 }
    ]
  },
  {
    dayIndex: 3,
    name: '第三天',
    shifts: [
      { shiftId: '', shiftName: '', count: 0 }
    ]
  }
])

const enabledShifts = ref([])
const modeApi = scheduleModeApi()
const shiftApi = shiftConfigApi()

// 获取启用的班次配置
const fetchEnabledShifts = async () => {
  try {
    const response = await shiftApi.getEnabledShifts()
    if (response.code === 200) {
      enabledShifts.value = response.data
    }
  } catch (error) {
    ElMessage.error('获取班次配置失败')
  }
}

// 获取排班模式详情
const fetchModeDetail = async () => {
  if (!props.modeId) return

  try {
    const response = await modeApi.getById(props.modeId)
    if (response.code === 200) {
      const data = response.data
      formData.id = data.id
      formData.modeName = data.modeName
      formData.modeCode = data.modeCode
      formData.modeType = data.modeType.toString()
      formData.algorithmClass = data.algorithmClass
      formData.description = data.description
      formData.status = data.status.toString()
      formData.sort = data.sort

      // 解析配置JSON
      if (data.configJson) {
        try {
          const config = JSON.parse(data.configJson)
          if (config.days && config.days.length > 0) {
            // 为每个班次添加count字段的默认值（如果不存在）
            const processedDays = config.days.map(day => {
              const processedShifts = day.shifts.map(shift => {
                // 查找对应的班次配置，获取中文名称
                const shiftConfig = enabledShifts.value.find(s => s.id === shift.shiftId.toString())
                return {
                  ...shift,
                  shiftId: shift.shiftId.toString(), // 确保shiftId为字符串类型
                  shiftName: shiftConfig ? shiftConfig.shiftName : '', // 确保显示中文名称
                  count: shift.count !== undefined && shift.count !== null ? Number(shift.count) : 0 // 确保count字段存在且为数字类型，默认为0
                }
              })
              return {
                ...day,
                shifts: processedShifts
              }
            })
            daysConfig.value = processedDays
          }
        } catch (error) {
          console.error('解析配置JSON失败', error)
        }
      }
    }
  } catch (error) {
    ElMessage.error('获取排班模式详情失败')
  }
}

// 生成配置JSON
const generateConfigJson = () => {
  // 处理班次名称和确保数据类型正确
  const processedDays = daysConfig.value.map(day => {
    const processedShifts = day.shifts.map(shift => {
      const shiftConfig = enabledShifts.value.find(s => s.id === shift.shiftId)
      return {
        ...shift,
        shiftId: shift.shiftId, // 保持shiftId为字符串类型
        count: shift.count !== undefined && shift.count !== null ? Number(shift.count) : 0, // 确保count为数字类型，默认为0
        shiftName: shiftConfig ? shiftConfig.shiftName : ''
      }
    })
    return {
      ...day,
      shifts: processedShifts
    }
  })

  return JSON.stringify({ days: processedDays })
}

// 添加天
const addDay = () => {
  const newDayIndex = daysConfig.value.length + 1
  daysConfig.value.push({
    dayIndex: newDayIndex,
    name: `第${newDayIndex}天`,
    shifts: [
      { shiftId: '', shiftName: '', count: 0 } // 确保count为数字类型，默认为0
    ]
  })
}

// 删除天
const removeDay = (index) => {
  if (daysConfig.value.length <= 1) {
    ElMessage.warning('至少保留一天')
    return
  }
  daysConfig.value.splice(index, 1)
  // 重新编号
  daysConfig.value.forEach((day, i) => {
    day.dayIndex = i + 1
    day.name = `第${i + 1}天`
  })
}

// 添加班次
const addShift = (dayIndex) => {
  daysConfig.value[dayIndex].shifts.push({
    shiftId: '',
    shiftName: '',
    count: 0 // 确保count为数字类型，默认为0
  })
}

// 删除班次
const removeShift = (dayIndex, shiftIndex) => {
  if (daysConfig.value[dayIndex].shifts.length <= 1) {
    ElMessage.warning('每天至少保留一个班次')
    return
  }
  daysConfig.value[dayIndex].shifts.splice(shiftIndex, 1)
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    // 生成配置JSON
    formData.configJson = generateConfigJson()

    let response
    if (formData.id) {
      // 编辑
      response = await modeApi.update(formData)
    } else {
      // 新增
      response = await modeApi.add(formData)
    }

    if (response.code === 200) {
      emit('save-success')
    } else {
      ElMessage.error('保存失败')
    }
  } catch (error) {
    console.error('保存失败', error)
    ElMessage.error('保存失败，请稍后重试')
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 监听班次选择，更新班次名称
watch(
  () => daysConfig.value,
  (newVal) => {
    newVal.forEach(day => {
      day.shifts.forEach(shift => {
        // 确保count属性存在且为数字类型，默认为0
        if (shift.count === undefined || shift.count === null || isNaN(shift.count)) {
          shift.count = 0
        } else {
          // 确保count为数字类型
          shift.count = Number(shift.count)
        }
        // 查找对应的班次配置，获取中文名称（处理类型匹配问题）
        const shiftConfig = enabledShifts.value.find(s => {
          return s.id.toString() === shift.shiftId.toString()
        })
        if (shiftConfig) {
          shift.shiftName = shiftConfig.shiftName
          // 确保shiftId与选项值类型一致
          shift.shiftId = shiftConfig.id.toString()
        }
      })
    })
  },
  { deep: true }
)

// 初始化
onMounted(async () => {
  await fetchEnabledShifts()
  await fetchModeDetail()
})
</script>

<style scoped>
.schedule-mode-edit {
  max-height: 600px;
  overflow-y: auto;
}

.schedule-config {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 15px;
  margin-top: 10px;
}

.config-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.day-header {
  width: 80px;
  font-weight: bold;
}

.day-column {
  flex: 1;
  min-width: 150px;
  margin: 0 10px;
}

.day-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.config-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.row-label {
  width: 80px;
  font-weight: bold;
}

.shift-container,
.count-container {
  width: 100%;
}

.shift-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.shift-select {
  width: 120px;
  margin-right: 5px;
}

.count-item {
  margin-bottom: 10px;
}

.count-input {
  width: 100px;
  min-width: 100px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.form-actions button {
  margin-left: 10px;
}
</style>
