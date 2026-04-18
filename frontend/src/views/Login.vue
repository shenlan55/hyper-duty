<template>
  <div class="login-container">
    <div class="login-form-wrapper">
      <h2 class="login-title">Hyper Duty 登录</h2>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            autocomplete="off"
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleSubmit"
            class="login-btn"
            full-width
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 邮箱验证码弹窗 -->
    <el-dialog
      v-model="codeDialogVisible"
      title="邮箱验证"
      width="400px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-form
        ref="codeFormRef"
        :model="codeForm"
        :rules="codeRules"
        label-position="top"
      >
        <el-alert
          :title="`验证码已发送至 ${maskedEmail}`"
          type="info"
          show-icon
          style="margin-bottom: 20px"
        />
        <el-form-item label="邮箱验证码" prop="code">
          <div class="code-input-wrapper">
            <el-input
              v-model="codeForm.code"
              placeholder="请输入验证码"
              prefix-icon="Key"
              maxlength="6"
            />
            <el-button
              type="primary"
              :disabled="codeCountdown > 0"
              @click="handleResendCode"
            >
              {{ codeCountdown > 0 ? `${codeCountdown}s` : '重新发送' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="codeDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="codeLoading"
          @click="handleVerifyCode"
        >
          验证
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { login } from '../api/auth'
import { sendVerificationCode, getCurrentMailConfig } from '../api/mailConfig'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const codeFormRef = ref()
const loading = ref(false)
const codeLoading = ref(false)
const codeDialogVisible = ref(false)
const showVerificationCode = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null
let maskedEmail = ref('')

const loginForm = reactive({
  username: '',
  password: ''
})

const codeForm = reactive({
  code: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

const codeRules = {
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' }
  ]
}

const checkMailConfig = async () => {
  try {
    const config = await getCurrentMailConfig()
    showVerificationCode.value = Boolean(config && config.enableEmailLogin)
  } catch (error) {
    // 如果获取失败，默认不显示验证码
    showVerificationCode.value = false
  }
}

const handleSubmit = async () => {
  try {
    await loginFormRef.value.validate()
    await doLogin()
  } catch (error) {
    if (error !== false) {
      console.error('操作失败:', error)
    }
  }
}

const handleSendCode = async () => {
  try {
    loading.value = true
    const result = await sendVerificationCode({ username: loginForm.username })
    maskedEmail.value = result.email
    startCountdown()
    ElMessage.success('验证码已发送')
  } catch (error) {
    ElMessage.error(error.message || '发送验证码失败')
  } finally {
    loading.value = false
  }
}

const handleResendCode = async () => {
  await handleSendCode()
}

const startCountdown = () => {
  codeCountdown.value = 60
  countdownTimer = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const handleVerifyCode = async () => {
  try {
    await codeFormRef.value.validate()
    await doLogin(codeForm.code)
  } catch (error) {
    if (error !== false) {
      console.error('验证失败:', error)
    }
  }
}

const doLogin = async (code) => {
  try {
    loading.value = true
    codeLoading.value = !!code
    
    const result = await login({
      username: loginForm.username,
      password: loginForm.password,
      code: code
    })
    
    // 检查是否需要验证码
    if (result && result.needCode) {
      // 需要验证码，发送并显示弹框
      await handleSendCode()
      codeDialogVisible.value = true
      return
    }
    
    // 登录成功
    userStore.login(result)
    ElMessage.success('登录成功')
    codeDialogVisible.value = false
    if (countdownTimer) {
      clearInterval(countdownTimer)
    }
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error(error.message || '登录失败，请重试')
  } finally {
    loading.value = false
    codeLoading.value = false
  }
}

onMounted(() => {
  // 不主动调用，避免循环刷新
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-form-wrapper {
  width: 400px;
  padding: 40px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
  font-size: 24px;
}

.login-form {
  width: 100%;
}

.code-input-wrapper {
  display: flex;
  gap: 10px;
}

.code-input-wrapper .el-input {
  flex: 1;
}

.login-btn {
  margin-top: 20px;
  height: 40px;
  font-size: 16px;
}
</style>
