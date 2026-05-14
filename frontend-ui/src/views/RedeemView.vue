<template>
  <header class="topbar">
    <RouterLink class="brand" to="/">有道写作</RouterLink>
    <nav class="nav">
      <RouterLink to="/rewrite">降重/降AI率/检测AI率 🔥</RouterLink>
      <RouterLink to="/records">改写记录</RouterLink>
      <RouterLink class="active" to="/redeem">卡密兑换</RouterLink>
      <AnnouncementBell />
      <el-dropdown v-if="userStore.token" trigger="click">
        <span class="login username">{{ userStore.userInfo?.username }} ▾</span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
            <el-dropdown-item @click="userStore.logout(); $router.push('/')">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <RouterLink v-else class="login" to="/login">登录/注册</RouterLink>
    </nav>
  </header>
  <main class="redeem-page">
    <h2>卡密兑换</h2>
    <p class="tip">输入管理员提供的卡密，兑换余额到当前账户</p>
    <div class="input-row">
      <el-input v-model="code" placeholder="请输入卡密" style="width:320px" clearable @keyup.enter="doRedeem" />
      <el-button type="primary" :loading="loading" @click="doRedeem">立即兑换</el-button>
    </div>
    <div v-if="lastResult" class="result">
      <p v-if="lastResult.success">兑换成功，获得 <strong>{{ lastResult.amount }}</strong> 元余额</p>
      <p v-else class="fail">{{ lastResult.msg }}</p>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { redeemCode } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import AnnouncementBell from '@/components/AnnouncementBell.vue'

const userStore = useUserStore()
const code = ref('')
const loading = ref(false)
const lastResult = ref(null)

const doRedeem = async () => {
  if (!code.value.trim()) return ElMessage.warning('请输入卡密')
  loading.value = true
  try {
    const res = await redeemCode(code.value.trim())
    lastResult.value = { success: true, amount: res.amount }
    ElMessage.success('兑换成功，获得 ' + res.amount + ' 元余额')
    code.value = ''
    await userStore.refreshBalance()
  } catch (e) {
    lastResult.value = { success: false, msg: e.msg || '兑换失败' }
  } finally { loading.value = false }
}
</script>

<style scoped>
.redeem-page{width:600px;margin:60px auto;background:#fff;border-radius:12px;padding:36px 40px;box-shadow:0 2px 12px rgba(0,0,0,.06)}.redeem-page h2{font-size:22px;margin-bottom:8px;color:#1a2772}.tip{color:#999;margin-bottom:24px;font-size:14px}.input-row{display:flex;gap:12px}.result{margin-top:20px;padding:16px;background:#f5f7fb;border-radius:8px;font-size:16px}.result strong{color:#2c3e8f}.fail{color:#e33}
</style>
