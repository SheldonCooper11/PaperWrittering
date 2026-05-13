<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-title">登录/注册</div>
      <el-tabs v-model="type" stretch>
        <el-tab-pane label="密码登录" name="password">
          <el-form :model="passwordForm" label-position="top">
            <el-form-item label="用户名"><el-input v-model="passwordForm.username" placeholder="请输入用户名" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入密码" /></el-form-item>
            <el-button type="primary" style="width:100%" :loading="loading" @click="handlePasswordLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="短信登录" name="sms">
          <el-form :model="smsForm" label-position="top">
            <el-form-item label="手机号"><el-input v-model="smsForm.phone" placeholder="请输入手机号" /></el-form-item>
            <el-form-item label="验证码"><el-input v-model="smsForm.code" placeholder="请输入验证码"><template #append><el-button :disabled="countdown>0" @click="sendCode">{{ countdown>0 ? countdown + 's' : '发送验证码' }}</el-button></template></el-input></el-form-item>
            <el-button type="primary" style="width:100%" :loading="loading" @click="handleSmsLogin">登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="auth-actions"><RouterLink to="/forgot-password">忘记密码</RouterLink><RouterLink to="/register">立即注册</RouterLink></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { passwordLogin, sendSms, smsLogin } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const type = ref('password')
const loading = ref(false)
const countdown = ref(0)
const passwordForm = reactive({ username: '', password: '' })
const smsForm = reactive({ phone: '', code: '' })

const tick = () => { countdown.value = 30; const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) }
const sendCode = async () => { if (!smsForm.phone) return ElMessage.warning('请输入手机号'); await sendSms({ phone: smsForm.phone, scene: 'login' }); ElMessage.success('验证码已发送'); tick() }
const handlePasswordLogin = async () => { loading.value = true; try { userStore.setLogin(await passwordLogin(passwordForm)); ElMessage.success('登录成功'); router.push('/rewrite') } finally { loading.value = false } }
const handleSmsLogin = async () => { loading.value = true; try { userStore.setLogin(await smsLogin(smsForm)); ElMessage.success('登录成功'); router.push('/rewrite') } finally { loading.value = false } }
</script>
