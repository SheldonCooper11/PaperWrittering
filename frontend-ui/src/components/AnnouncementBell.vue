<template>
  <span class="bell-trigger" @click="openDialog">🔔<span v-if="unread" class="red-dot"></span></span>

  <el-dialog v-model="visible" :close-on-click-modal="false" :show-close="false" width="840px" top="5vh" class="announce-dialog" @opened="fetchAnnouncements">
    <template #header>
      <div class="dialog-header">
        <span class="dialog-title">公告</span>
        <span class="dialog-close" @click="visible = false">×</span>
      </div>
    </template>

    <div class="dialog-body">
      <div v-if="list.length === 0" class="empty">暂无公告</div>
      <section v-for="item in list" :key="item.id" class="timeline-item">
        <div class="time-box">
          <div class="ago">{{ agoText(item.createTime) }}</div>
          <div class="date">{{ formatDate(item.createTime) }}</div>
          <div class="clock">{{ formatClock(item.createTime) }}</div>
        </div>
        <div class="axis"><span class="dot"></span></div>
        <article class="article">
          <h2>{{ item.title }}</h2>
          <p>{{ item.content }}</p>
        </article>
      </section>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <button class="read-btn" @click="visible = false">已阅览</button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { getAnnouncements } from '@/api/auth'

const list = ref([])
const unread = ref(true)
const visible = ref(false)

const openDialog = () => { visible.value = true }

const fetchAnnouncements = async () => {
  try {
    list.value = await getAnnouncements()
    unread.value = false
  } catch { /* ignore */ }
}

const agoText = (time) => {
  if (!time) return ''
  const now = Date.now()
  const then = new Date(time).getTime()
  const diff = now - then
  const minutes = Math.floor(diff / 60000)
  if (minutes < 60) return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  const months = Math.floor(days / 30)
  if (months < 12) return `${months}月前`
  return `${Math.floor(months / 12)}年前`
}

const formatDate = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getFullYear()}/${String(d.getMonth() + 1).padStart(2, '0')}/${String(d.getDate()).padStart(2, '0')}`
}

const formatClock = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}
</script>

<style scoped>
.bell-trigger {
  cursor: pointer;
  font-size: 18px;
  position: relative;
  user-select: none;
}
.red-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 8px;
  height: 8px;
  background: #f56;
  border-radius: 50%;
}

.dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.dialog-title {
  color: #5366db;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 0.5px;
}
.dialog-close {
  font-size: 34px;
  line-height: 1;
  color: #303133;
  cursor: pointer;
  font-weight: 300;
}
.dialog-close:hover { opacity: 0.7; }

.dialog-body {
  max-height: 520px;
  overflow-y: auto;
  padding: 8px 28px 8px 44px;
  font-weight: 400;
}
.dialog-body::-webkit-scrollbar { width: 7px; }
.dialog-body::-webkit-scrollbar-track { background: transparent; }
.dialog-body::-webkit-scrollbar-thumb { background: #d7d7d7; border-radius: 7px; }

.empty { color: #999; font-size: 16px; text-align: center; padding: 40px 0; }

.timeline-item {
  display: grid;
  grid-template-columns: 100px 28px 1fr;
  column-gap: 14px;
  position: relative;
}
.timeline-item:not(:last-child) { padding-bottom: 28px; }

.time-box { text-align: right; padding-top: 0; color: #303133; font-weight: 400; }
.ago { font-size: 16px; margin-bottom: 8px; color: #333; font-weight: 400; }
.date, .clock { font-size: 14px; line-height: 22px; color: #333; font-weight: 400; }

.axis {
  position: relative;
  display: flex;
  justify-content: center;
}
.axis::before {
  content: "";
  position: absolute;
  top: 16px;
  bottom: 0;
  left: 13px;
  width: 3px;
  background: #e1e1e1;
}
.timeline-item:last-child .axis::before { bottom: calc(100% - 16px); }

.dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #6267dc;
  border: 4px solid #f0c3cc;
  position: relative;
  z-index: 1;
  margin-top: 2px;
}

.article {
  padding-right: 32px;
  line-height: 1.85;
  font-size: 16px;
  color: #333;
  font-weight: 400;
}
.article h2 {
  font-size: 19px;
  line-height: 1.4;
  margin: -2px 0 8px;
  font-weight: 800;
  color: #303133;
}
.article p {
  margin: 16px 0;
  white-space: pre-wrap;
  font-weight: 400;
}

.dialog-footer {
  display: flex;
  align-items: center;
  justify-content: center;
}
.read-btn {
  width: 180px;
  height: 42px;
  border-radius: 22px;
  border: 1px solid #5065ff;
  color: #5266ff;
  background: #fff;
  font-size: 15px;
  cursor: pointer;
  transition: 0.2s;
}
.read-btn:hover { background: #f5f7ff; }
</style>

<style>
.announce-dialog .el-dialog__header { padding: 0 28px; height: 60px; display: flex; align-items: center; border-bottom: 1px solid #e5e6eb; margin: 0; }
.announce-dialog .el-dialog__body { padding: 0; }
.announce-dialog .el-dialog__footer { padding: 0 28px; height: 72px; display: flex; align-items: center; justify-content: center; border-top: 1px solid #e5e6eb; }
</style>
