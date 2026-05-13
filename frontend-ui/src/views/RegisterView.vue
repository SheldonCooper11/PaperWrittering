<template>
  <div class="auth-page"><div class="auth-card"><div class="auth-title">用户注册</div>
    <el-form :model="form" label-position="top">
      <el-form-item label="用户名"><el-input v-model="form.username" placeholder="请输入用户名" @blur="checkName"><template #suffix><span :class="usernameAvailable ? 'ok' : 'bad'" v-if="usernameTip">{{ usernameTip }}</span></template></el-input></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" @blur="checkMobile"><template #suffix><span :class="phoneAvailable ? 'ok' : 'bad'" v-if="phoneTip">{{ phoneTip }}</span></template></el-input></el-form-item>
      <el-form-item label="验证码"><el-input v-model="form.code" placeholder="请输入验证码"><template #append><el-button :disabled="countdown>0 || !phoneAvailable" @click="sendCode">{{ countdown>0 ? countdown + 's' : '发送验证码' }}</el-button></template></el-input></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="至少8位，且同时包含字母和数字" /></el-form-item>
      <el-button type="primary" style="width:100%" :loading="loading" @click="handleRegister">注册</el-button>
    </el-form>
    <div class="auth-actions"><RouterLink to="/login">已有账号，去登录</RouterLink><RouterLink to="/">返回首页</RouterLink></div>
  </div></div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { checkPhone, checkUsername, register, sendSms } from '@/api/auth'

const router = useRouter()
const form = reactive({ username: '', phone: '', code: '', password: '' })
const loading = ref(false)
const countdown = ref(0)
const usernameTip = ref('')
const phoneTip = ref('')
const usernameAvailable = ref(false)
const phoneAvailable = ref(false)

const checkName = async () => { if (!form.username) return; const res = await checkUsername(form.username); usernameAvailable.value = res.available; usernameTip.value = res.available ? '可用' : '已存在' }
const checkMobile = async () => { if (!form.phone) return; const res = await checkPhone(form.phone); phoneAvailable.value = res.available; phoneTip.value = res.available ? '可用' : '已存在' }
const tick = () => { countdown.value = 30; const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) }
const sendCode = async () => { await sendSms({ phone: form.phone, scene: 'register' }); ElMessage.success('验证码已发送'); tick() }
const handleRegister = async () => { loading.value = true; try { await register(form); ElMessage.success('注册成功'); router.push('/login') } finally { loading.value = false } }
</script>

<style scoped>.ok{color:#67c23a}.bad{color:#f56c6c}</style>
