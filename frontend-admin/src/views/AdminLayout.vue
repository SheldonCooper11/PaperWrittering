<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside"><div class="logo">论文降重后台</div><el-menu router default-active="/users" background-color="#263445" text-color="#cfd8e3" active-text-color="#fff"><el-menu-item index="/users">用户管理</el-menu-item><el-menu-item index="/presets">预设模式管理</el-menu-item><el-menu-item index="/platforms">平台管理</el-menu-item><el-menu-item index="/redeem">卡密管理</el-menu-item><el-menu-item index="/config">系统配置</el-menu-item><el-menu-item index="/announcements">公告管理</el-menu-item></el-menu></el-aside>
    <el-container><el-header class="header"><span>管理员控制台</span><el-button @click="logout">退出登录</el-button></el-header><el-main><RouterView /></el-main></el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'

const router = useRouter()
const adminStore = useAdminStore()
const logout = () => { adminStore.logout(); router.push('/login') }
</script>

<style scoped>.layout{min-height:100vh}.aside{background:#263445}.logo{height:60px;color:#fff;font-size:20px;font-weight:900;display:flex;align-items:center;justify-content:center;background:#1f2d3d}.header{height:60px;background:#fff;display:flex;align-items:center;justify-content:space-between;box-shadow:0 1px 8px rgba(0,0,0,.05)}</style>
