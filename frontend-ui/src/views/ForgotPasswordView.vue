<template>
  <div class="auth-page"><div class="auth-card"><div class="auth-title">找回密码</div>
    <el-form :model="form" label-position="top">
      <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入注册手机号" @blur="checkMobile"><template #suffix><span :class="phoneExists ? 'ok' : 'bad'" v-if="phoneTip">{{ phoneTip }}</span></template></el-input></el-form-item>
      <el-form-item label="验证码"><el-input v-model="form.code" placeholder="请输入验证码"><template #append><el-button :disabled="countdown>0 || !phoneExists" @click="sendCode">{{ countdown>0 ? countdown + 's' : '发送验证码' }}</el-button></template></el-input></el-form-item>
      <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" show-password placeholder="至少8位，且同时包含字母和数字" /></el-form-item>
      <el-button type="primary" style="width:100%" :loading="loading" @click="handleReset">重置密码</el-button>
    </el-form>
    <div class="auth-actions"><RouterLink to="/login">返回登录</RouterLink><RouterLink to="/">返回首页</RouterLink></div>
  </div></div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { checkPhone, resetPassword, sendSms } from '@/api/auth'

const router = useRouter()
const form = reactive({ phone: '', code: '', newPassword: '' })
const loading = ref(false)
const countdown = ref(0)
const phoneTip = ref('')
const phoneExists = ref(false)

const checkMobile = async () => { if (!form.phone) return; const res = await checkPhone(form.phone); phoneExists.value = !res.available; phoneTip.value = phoneExists.value ? '账号存在' : '手机号不存在' }
const tick = () => { countdown.value = 30; const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) }
const sendCode = async () => { await sendSms({ phone: form.phone, scene: 'reset' }); ElMessage.success('验证码已发送'); tick() }
const handleReset = async () => { loading.value = true; try { await resetPassword(form); ElMessage.success('密码已重置'); router.push('/login') } finally { loading.value = false } }
</script>

<style scoped>.ok{color:#67c23a}.bad{color:#f56c6c}</style>
