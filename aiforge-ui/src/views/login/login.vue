<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { loginApi, registerApi } from '@/api/auth.ts'
import type { AuthParams } from '@/types/auth.ts'
import { useAuthStore } from '@/stores/auth.ts'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

// 控制滑动门状态
const isRegister = ref(false)
const loading = ref(false)

// 获取表单 DOM 实例
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

// 表单数据
const formData = reactive<AuthParams>({
  userName: '',
  password: '',
  confirmPassword: ''
})

// === 自定义密码一致性校验逻辑 ===
const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== formData.password) {
    callback(new Error('两次输入的密码不一致，请重新输入'))
  } else {
    callback()
  }
}

// === 登录表单校验规则 ===
const loginRules = reactive<FormRules>({
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

// === 注册表单校验规则 ===
const registerRules = reactive<FormRules>({
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' } // 引入自定义校验
  ]
})

// 切换面板方法
const togglePanel = () => {
  isRegister.value = !isRegister.value
  // 切换面板时，重置两个表单的数据和校验状态 (清空红字提示)
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()
}

// 处理提交 (接收表单实例进行校验)
const handleSubmit = async (type: 'login' | 'register', formEl: FormInstance | undefined) => {
  if (!formEl) return

  // 触发 Element Plus 内置校验
  await formEl.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (type === 'login') {
          const res = await loginApi(formData)
          if (res.data?.token) {
            ElMessage.success('登录成功')
            authStore.setToken(res.data.token)
            router.push('/')
          }
        } else {
          await registerApi(formData)
          ElMessage.success('注册成功，请登录')
          togglePanel()
        }
      } catch (error) {
        console.error('Auth Error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<template>
  <div class="login-container">
    <el-row class="layout-row" justify="center" align="middle">
      <el-col :xs="24" :sm="20" :md="16" :lg="12" :xl="10" class="flex-center">
        <div class="sliding-container" :class="{ 'right-panel-active': isRegister }">

          <div class="form-container sign-up-container">
            <div class="form-wrapper">
              <h2 class="title">创建账号</h2>
              <el-form ref="registerFormRef" :model="formData" :rules="registerRules" @keyup.enter="handleSubmit('register', registerFormRef)" size="large" class="auth-form">
                <el-form-item prop="userName">
                  <el-input v-model="formData.userName" placeholder="请输入用户名" clearable />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
                </el-form-item>
                <el-form-item prop="confirmPassword">
                  <el-input v-model="formData.confirmPassword" type="password" placeholder="请确认密码" show-password />
                </el-form-item>
                <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit('register', registerFormRef)">
                  注 册
                </el-button>
              </el-form>
            </div>
          </div>

          <div class="form-container sign-in-container">
            <div class="form-wrapper">
              <h2 class="title">登录</h2>
              <el-form ref="loginFormRef" :model="formData" :rules="loginRules" @keyup.enter="handleSubmit('login', loginFormRef)" size="large" class="auth-form">
                <el-form-item prop="userName">
                  <el-input v-model="formData.userName" placeholder="请输入用户名" clearable />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
                </el-form-item>
                <el-button type="primary" class="submit-btn" :loading="loading" @click="handleSubmit('login', loginFormRef)">
                  登 录
                </el-button>
              </el-form>
            </div>
          </div>

          <div class="overlay-container">
            <div class="overlay">
              <div class="overlay-panel overlay-left">
                <h2>已有账号？</h2>
                <p>请登录开始探索之旅</p>
                <el-button plain class="ghost-btn" @click="togglePanel">去登录</el-button>
              </div>
              <div class="overlay-panel overlay-right">
                <h2>你好，朋友！</h2>
                <p>立即注册，开启你的探索之旅</p>
                <el-button plain class="ghost-btn" @click="togglePanel">去注册</el-button>
              </div>
            </div>
          </div>

        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.login-container {
  height: 100%;
  width: 100%;

  /* 添加背景图片 */
  background-image: url('@/assets/images/login.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  overflow: hidden;
}

.layout-row {
  height: 100%;
}

.flex-center {
  display: flex;
  justify-content: center;
  align-items: center;
}

/* === 滑动门核心容器样式 === */
.sliding-container {
 position: relative;
  width: 768px;
  max-width: 100%;
  height: 480px;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(2px);
  -webkit-backdrop-filter: blur(2px);
  border-radius: 12px;
  box-shadow: 0 14px 28px rgba(0,0,0,0.1), 0 10px 10px rgba(0,0,0,0.05);
  overflow: hidden;
}

/* === 表单区基础样式 === */
.form-container {
  position: absolute;
  top: 0;
  height: 100%;
  width: 50%;
  transition: all 0.6s ease-in-out;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
}

.form-wrapper {
  width: 80%;
  text-align: center;
}

/* 需求3：标题改成纯黑色 */
.title {
  color: #000000 !important; /* 加上 !important 确保不被覆盖 */
  margin-bottom: 30px;
  font-size: 24px;
  font-weight: bold;
}

.auth-form {
  margin-top: 20px;
}

/* 需求1：修改登录/注册按钮颜色，去除默认蓝色，改为高级深灰色 */
.submit-btn {
  width: 100%;
  margin-top: 10px;
  border-radius: 20px;
  background-color: #434446 !important; /* 深灰色 */
  border-color: #434446 !important;
  color: #ffffff !important;
  transition: all 0.3s ease;
}
.submit-btn:hover, .submit-btn:focus {
  background-color: #5c5d61 !important; /* 悬浮时稍微变亮 */
  border-color: #5c5d61 !important;
}

/* 需求2：修改输入框颜色，适配暗黑系，确保输入内容清晰 */
:deep(.el-input__wrapper) {
  /* 背景改为半透明白色，既保留玻璃感，又能突出输入框 */
  background-color: rgba(255, 255, 255, 0.15) !important;
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.3) inset !important; /* 柔和的边框 */
}
:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.6) inset !important; /* 鼠标悬浮边框变亮 */
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #ffffff inset !important; /* 聚焦时边框变白 */
}
:deep(.el-input__inner) {
  color: #ffffff !important; /* 输入的文字设为纯白，在暗背景下最清晰 */
}
:deep(.el-input__inner::placeholder) {
  color: #e0e0e0 !important; /* 占位符设为浅灰，和用户输入的内容做区分 */
}

/* === 登录与注册区的层级与动画初始状态 === */
.sign-in-container {
  left: 0;
  z-index: 2;
}

.sign-up-container {
  left: 0;
  opacity: 0;
  z-index: 1;
  visibility: hidden;
}

/* === 覆盖层主容器 === */
.overlay-container {
  position: absolute;
  top: 0;
  left: 50%;
  width: 50%;
  height: 100%;
  overflow: hidden;
  transition: transform 0.6s ease-in-out;
  z-index: 100;
}

/* === 覆盖层背景与动画 === */
.overlay {
  position: relative;
  left: -100%;
  height: 100%;
  width: 200%;
  background: linear-gradient(to right, rgba(43, 48, 59, 0.85), rgba(20, 20, 24, 0.9));
  color: #fff;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
}

/* === 覆盖层内部的文本面板 === */
.overlay-panel {
  position: absolute;
  top: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 50%;
  height: 100%;
  padding: 0 40px;
  text-align: center;
  transform: translateX(0);
  transition: transform 0.6s ease-in-out;
  box-sizing: border-box;
}

.overlay-panel h2 {
  margin-bottom: 15px;
  font-weight: 600;
}

.overlay-panel p {
  margin-bottom: 30px;
  font-size: 14px;
  line-height: 1.5;
}

.ghost-btn {
  background-color: transparent !important;
  border-color: #fff !important;
  color: #fff !important;
  border-radius: 20px;
  padding: 10px 30px;
}
.ghost-btn:hover {
  background-color: rgba(255,255,255,0.2) !important;
}

/* 左右面板的初始位移 */
.overlay-left {
  transform: translateX(-20%);
}
.overlay-right {
  right: 0;
  transform: translateX(0);
}

/* =========================================
   激活状态 (isRegister = true) 的动画属性
   ========================================= */

.sliding-container.right-panel-active .sign-in-container {
  transform: translateX(100%);
  opacity: 0;
  pointer-events: none;
}

.sliding-container.right-panel-active .sign-up-container {
  transform: translateX(100%);
  opacity: 1;
  z-index: 5;
  visibility: visible;
  animation: show 0.6s;
}

.sliding-container.right-panel-active .overlay-container {
  transform: translateX(-100%);
}

.sliding-container.right-panel-active .overlay {
  transform: translateX(50%);
}

.sliding-container.right-panel-active .overlay-left {
  transform: translateX(0);
}
.sliding-container.right-panel-active .overlay-right {
  transform: translateX(20%);
}

/* 防止层级闪烁的动画 */
@keyframes show {
  0%, 49.99% {
    opacity: 0;
    z-index: 1;
  }
  50%, 100% {
    opacity: 1;
    z-index: 5;
  }
}
</style>
