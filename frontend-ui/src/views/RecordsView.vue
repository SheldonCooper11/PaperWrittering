<template>
  <header class="topbar">
    <RouterLink class="brand" to="/">一触即改 写作牛 <span class="icon">🦅</span></RouterLink>
    <nav class="nav">
      <RouterLink to="/rewrite">降重/降AI率/检测AI率 🔥</RouterLink>
      <RouterLink class="active" to="/records">改写记录</RouterLink>
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
  <main class="records-page">
    <h2>改写记录</h2>
    <el-table :data="records" border v-loading="loading" empty-text="暂无改写记录">
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column prop="rewriteType" label="类型" width="80">
        <template #default="{ row }">{{ row.rewriteType === 'TEXT' ? '文本' : '文档' }}</template>
      </el-table-column>
      <el-table-column prop="rewriteMode" label="模式" width="200" show-overflow-tooltip />
      <el-table-column label="内容" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          <template v-if="row.rewriteType === 'TEXT'">{{ row.paraphrasedText?.substring(0, 60) }}{{ row.paraphrasedText?.length > 60 ? '...' : '' }}</template>
          <template v-else>
            <a v-if="row.paraphrasedFileUrl" :href="row.paraphrasedFileUrl" target="_blank">{{ row.paraphrasedFilename || '下载' }}</a>
            <span v-else>-</span>
          </template>
        </template>
      </el-table-column>
      <el-table-column prop="totalCharacters" label="字符数" width="90" />
      <el-table-column prop="cost" label="消耗(元)" width="100" />
      <el-table-column label="余额变化" width="200">
        <template #default="{ row }">{{ row.balanceBefore ?? '-' }} → {{ row.balanceAfter ?? '-' }}</template>
      </el-table-column>
    </el-table>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { rewriteRecords } from '@/api/rewrite'
import { useUserStore } from '@/stores/user'
import AnnouncementBell from '@/components/AnnouncementBell.vue'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)

onMounted(async () => {
  if (!userStore.token) return
  loading.value = true
  try { records.value = await rewriteRecords() } finally { loading.value = false }
})
</script>

<style scoped>
.records-page{width:1100px;margin:30px auto;background:#fff;border-radius:12px;padding:28px 32px;box-shadow:0 2px 12px rgba(0,0,0,.06)}.records-page h2{font-size:20px;margin-bottom:18px;color:#1a2772}
</style>
