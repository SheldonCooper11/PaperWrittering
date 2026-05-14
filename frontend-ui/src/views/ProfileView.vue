<template>
  <header class="topbar">
    <RouterLink class="brand" to="/">有道写作</RouterLink>
    <nav class="nav">
      <RouterLink to="/rewrite">降重/降AI率/检测AI率 🔥</RouterLink>
      <RouterLink to="/records">改写记录</RouterLink>
      <RouterLink to="/redeem">卡密兑换</RouterLink>
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
  <main class="profile-page">
    <div class="profile-card">
      <h2>个人中心</h2>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfo?.phone || '未绑定' }}</el-descriptions-item>
        <el-descriptions-item label="余额">{{ userInfo?.balance ?? 0 }} 元</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userInfo?.role === 'ADMIN' ? '管理员' : '普通用户' }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <div class="profile-card">
      <h3>修改密码</h3>
      <el-form :model="form" label-width="100px" style="max-width:400px">
        <el-form-item label="旧密码">
          <el-input v-model="form.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="doChangePassword">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { changePassword } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import AnnouncementBell from '@/components/AnnouncementBell.vue'

const userStore = useUserStore()
const userInfo = ref(userStore.userInfo)
const form = reactive({ oldPassword: '', newPassword: '' })
const saving = ref(false)

const doChangePassword = async () => {
  if (!form.oldPassword) return ElMessage.warning('请输入旧密码')
  if (!form.newPassword) return ElMessage.warning('请输入新密码')
  if (form.newPassword.length < 8) return ElMessage.warning('密码至少8位')
  saving.value = true
  try {
    await changePassword(form)
    ElMessage.success('密码修改成功')
    form.oldPassword = ''
    form.newPassword = ''
  } finally { saving.value = false }
}

onMounted(async () => {
  await userStore.refreshBalance()
  userInfo.value = userStore.userInfo
})
</script>

<style scoped>
.profile-page{width:800px;margin:40px auto}.profile-card{background:#fff;border-radius:12px;padding:28px 32px;margin-bottom:24px;box-shadow:0 2px 12px rgba(0,0,0,.06)}.profile-card h2{font-size:22px;margin-bottom:20px;color:#1a2772}.profile-card h3{font-size:18px;margin-bottom:18px;color:#333}
</style>
