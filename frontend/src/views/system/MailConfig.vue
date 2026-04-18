<template>
  <div class="mail-config-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>邮件服务配置</span>
        </div>
      </template>

      <el-form
        ref="mailConfigFormRef"
        :model="mailConfigForm"
        :rules="rules"
        label-width="160px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SMTP服务器地址" prop="smtpHost">
              <el-input v-model="mailConfigForm.smtpHost" placeholder="例如：smtp.qq.com" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SMTP端口" prop="smtpPort">
              <el-input-number v-model="mailConfigForm.smtpPort" :min="1" :max="65535" placeholder="例如：465" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用SSL">
              <el-switch v-model="mailConfigForm.enableSsl" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用TLS">
              <el-switch v-model="mailConfigForm.enableTls" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="发件人邮箱" prop="fromEmail">
              <el-input v-model="mailConfigForm.fromEmail" placeholder="例如：xxx@qq.com" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="发件人名称" prop="fromName">
              <el-input v-model="mailConfigForm.fromName" placeholder="例如：Hyper Duty" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="授权码/密码" prop="authPassword">
          <el-input v-model="mailConfigForm.authPassword" type="password" placeholder="请输入邮箱授权码" show-password />
        </el-form-item>

        <el-divider content-position="left">邮件模板配置</el-divider>

        <el-form-item label="登录验证码模板">
          <el-input
            v-model="mailConfigForm.loginCodeTemplate"
            type="textarea"
            :rows="3"
            placeholder="可用变量：{code} 验证码，{expire} 有效期（分钟）"
          />
          <div class="template-tip">
            默认模板：您的登录验证码是：{code}，{expire}分钟内有效。
          </div>
        </el-form-item>

        <el-form-item label="密码找回模板">
          <el-input
            v-model="mailConfigForm.passwordResetTemplate"
            type="textarea"
            :rows="3"
            placeholder="可用变量：{code} 验证码，{expire} 有效期（分钟）"
          />
        </el-form-item>

        <el-form-item label="异地登录提醒模板">
          <el-input
            v-model="mailConfigForm.remoteLoginTemplate"
            type="textarea"
            :rows="3"
            placeholder="可用变量：{ip} IP地址，{time} 登录时间，{location} 登录地点"
          />
        </el-form-item>

        <el-divider content-position="left">功能开关</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="启用邮件验证码登录">
              <el-switch v-model="mailConfigForm.enableEmailLogin" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="验证码有效期（分钟）" prop="codeExpireMinutes">
              <el-input-number v-model="mailConfigForm.codeExpireMinutes" :min="1" :max="60" placeholder="验证码有效期" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="备注">
          <el-input
            v-model="mailConfigForm.remark"
            type="textarea"
            :rows="2"
            placeholder="备注信息"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleTestConnection">
            测试连接
          </el-button>
          <el-button type="success" @click="handleSaveConfig">
            保存配置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentMailConfig, saveMailConfig, testMailConnection } from '../../api/mailConfig'

const mailConfigFormRef = ref()

const mailConfigForm = reactive({
  id: null,
  smtpHost: '',
  smtpPort: 465,
  enableSsl: true,
  enableTls: false,
  fromEmail: '',
  fromName: 'Hyper Duty',
  authPassword: '',
  loginCodeTemplate: '',
  passwordResetTemplate: '',
  remoteLoginTemplate: '',
  enableEmailLogin: false,
  codeExpireMinutes: 5,
  remark: ''
})

const rules = {
  smtpHost: [
    { required: true, message: '请输入SMTP服务器地址', trigger: 'blur' }
  ],
  smtpPort: [
    { required: true, message: '请输入SMTP端口', trigger: 'blur' }
  ],
  fromEmail: [
    { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  authPassword: [
    { required: true, message: '请输入授权码/密码', trigger: 'blur' }
  ]
}

const fetchCurrentConfig = async () => {
  try {
    const data = await getCurrentMailConfig()
    if (data) {
      Object.assign(mailConfigForm, {
        id: data.id,
        smtpHost: data.smtpHost || '',
        smtpPort: data.smtpPort || 465,
        enableSsl: Boolean(data.enableSsl),
        enableTls: Boolean(data.enableTls),
        fromEmail: data.fromEmail || '',
        fromName: data.fromName || 'Hyper Duty',
        authPassword: '', // 密码不回显
        loginCodeTemplate: data.loginCodeTemplate || '',
        passwordResetTemplate: data.passwordResetTemplate || '',
        remoteLoginTemplate: data.remoteLoginTemplate || '',
        enableEmailLogin: Boolean(data.enableEmailLogin),
        codeExpireMinutes: data.codeExpireMinutes || 5,
        remark: data.remark || ''
      })
    }
  } catch (error) {
    ElMessage.error('获取邮件配置失败')
  }
}

// 转换数据格式：Boolean -> Integer (1/0)
const transformFormData = (data) => {
  return {
    ...data,
    enableSsl: data.enableSsl ? 1 : 0,
    enableTls: data.enableTls ? 1 : 0,
    enableEmailLogin: data.enableEmailLogin ? 1 : 0
  }
}

const handleTestConnection = async () => {
  try {
    await mailConfigFormRef.value.validate()
    const data = transformFormData(mailConfigForm)
    await testMailConnection(data)
    ElMessage.success('邮件连接测试成功')
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '邮件连接测试失败')
    }
  }
}

const handleSaveConfig = async () => {
  try {
    await mailConfigFormRef.value.validate()
    const data = transformFormData(mailConfigForm)
    await saveMailConfig(data)
    ElMessage.success('邮件配置保存成功')
    fetchCurrentConfig()
  } catch (error) {
    if (error !== false) {
      ElMessage.error(error.message || '邮件配置保存失败')
    }
  }
}

onMounted(() => {
  fetchCurrentConfig()
})
</script>

<style scoped>
.mail-config-container {
  padding: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.template-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>
