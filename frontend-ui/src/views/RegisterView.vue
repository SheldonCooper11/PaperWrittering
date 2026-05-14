<template>
  <div class="auth-page"><div class="auth-card"><div class="auth-title">注册</div>
    <el-form :model="form" label-position="top">
      <el-form-item label="用户名"><el-input v-model="form.username" placeholder="请输入用户名" @blur="checkName"><template #suffix><span :class="usernameAvailable ? 'ok' : 'bad'" v-if="usernameTip">{{ usernameTip }}</span></template></el-input></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="请输入手机号" @blur="checkMobile"><template #suffix><span :class="phoneAvailable ? 'ok' : 'bad'" v-if="phoneTip">{{ phoneTip }}</span></template></el-input></el-form-item>
      <el-form-item label="验证码">
        <div class="code-row"><el-input v-model="form.code" placeholder="请输入验证码" /><el-button class="send-btn" :disabled="countdown>0" :loading="sending" @click="sendCode">{{ countdown>0 ? countdown + 's 后重发' : '发送验证码' }}</el-button></div>
      </el-form-item>
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
const sending = ref(false)
const countdown = ref(0)
const usernameTip = ref('')
const phoneTip = ref('')
const usernameAvailable = ref(false)
const phoneAvailable = ref(false)

const checkName = async () => { if (!form.username) return; const res = await checkUsername(form.username); usernameAvailable.value = res.available; usernameTip.value = res.available ? '可用' : '已存在' }
const checkMobile = async () => { if (!form.phone) return; const res = await checkPhone(form.phone); phoneAvailable.value = res.available; phoneTip.value = res.available ? '可用' : '已存在' }
const tick = () => { countdown.value = 30; const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) }
const sendCode = async () => {
  if (!form.phone) return ElMessage.warning('请输入手机号')
  sending.value = true
  try {
    if (!phoneTip.value) await checkMobile()
    if (!phoneAvailable.value) return ElMessage.warning('该手机号已被注册')
    await sendSms({ phone: form.phone, scene: 'register' })
    ElMessage.success('验证码已发送')
    tick()
  } finally {
    sending.value = false
  }
}
const handleRegister = async () => { loading.value = true; try { await register(form); ElMessage.success('注册成功'); router.push('/login') } finally { loading.value = false } }
</script>

<style scoped>
.ok{color:#67c23a}.bad{color:#f56c6c}
.code-row{display:flex;gap:10px}.code-row .el-input{flex:1}.send-btn{height:32px;padding:0 16px;border:1px solid #5967d9;background:#f0f1ff;color:#5967d9;border-radius:4px;font-size:14px;white-space:nowrap;cursor:pointer;transition:all .2s}.send-btn:hover:not(:disabled){background:#5967d9;color:#fff}.send-btn:disabled{opacity:.5;cursor:not-allowed}.send-btn.is-loading{background:#5967d9;color:#fff}
</style>
