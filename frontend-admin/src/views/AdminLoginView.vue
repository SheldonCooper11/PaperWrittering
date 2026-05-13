<template>
  <div class="admin-login">
    <div class="login-card">
      <div class="login-title">管理员登录</div>
      <el-form :model="form" label-position="top">
        <el-form-item label="用户名"><el-input v-model="form.username" placeholder="请输入管理员用户名" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="请输入密码" /></el-form-item>
        <el-button type="primary" style="width:100%" :loading="loading" @click="handleLogin">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminLogin } from '@/api/admin'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  loading.value = true
  try {
    adminStore.setLogin(await adminLogin(form))
    ElMessage.success('登录成功')
    router.push('/users')
  } finally {
    loading.value = false
  }
}
</script>
