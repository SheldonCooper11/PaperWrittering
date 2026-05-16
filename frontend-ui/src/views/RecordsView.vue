<template>
  <header class="topbar">
    <RouterLink class="brand" to="/">有道写作</RouterLink>
    <nav class="nav">
      <RouterLink to="/rewrite">降重/降AI率/检测AI率 🔥</RouterLink>
      <RouterLink class="active" to="/records">改写记录</RouterLink>
      <RouterLink to="/redeem">卡密兑换</RouterLink>
      <AnnouncementBell />
      <span v-if="userStore.token && userStore.userInfo?.balance != null" class="balance">余额 ¥{{ Number(userStore.userInfo.balance).toFixed(2) }}</span>
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
  <div class="page">
    <div class="notice" v-if="!noticeClosed"><span class="notice-icon">!</span><span>本站仅保留2天内处理记录，若有需要请及时下载保存处理结果~</span><span class="notice-close" @click="noticeClosed = true">×</span></div>
    <section class="panel" v-if="userStore.token">
      <h1 class="title">使用明细</h1>
      <div v-if="records.length === 0 && !loading" class="empty">暂无使用记录</div>
      <div class="record-card" v-for="r in records" :key="r.id">
        <div class="record-head">
          <span class="record-name">{{ r.rewriteMode || '改写' }}{{ r.platform ? '（' + r.platform + '）' : '' }}{{ r.language ? ' - ' + (r.language === 'chinese' ? '中文' : '英文') : '' }}</span>
          <span class="tag" :class="r.rewriteType === 'TEXT' ? 'tag-text' : 'tag-file'">{{ r.rewriteType === 'TEXT' ? '文本' : '文件' }}</span>
          <span class="tag tag-done"><span class="dot"></span>已完成</span>
        </div>
        <div class="meta">{{ r.createTime?.replace('T', ' ') }}</div>
        <div class="actions">
          <template v-if="r.rewriteType === 'TEXT'">
            <button class="btn" @click="downloadOriginal(r)"><span>⇩</span><span>下载原文</span></button>
            <button class="btn btn-primary" @click="downloadResult(r)"><span>⇩</span><span>下载结果</span></button>
            <button class="btn btn-small" @click="previewText = r; previewVisible = true">预览</button>
          </template>
          <template v-else>
            <button class="btn" v-if="r.originalFileUrl" @click="window.open(r.originalFileUrl)"><span>⇩</span><span>下载原文</span></button>
            <button class="btn" v-else disabled><span>⇩</span><span>下载原文</span></button>
            <button class="btn btn-primary" v-if="r.paraphrasedFileUrl" @click="window.open(r.paraphrasedFileUrl)"><span>⇩</span><span>下载结果</span></button>
            <button class="btn btn-primary" v-else @click="downloadResult(r)"><span>⇩</span><span>下载结果</span></button>
          </template>
        </div>
        <div class="info-box">
          <div><div class="field-label">内容摘要</div><div class="field-value">{{ summaryOf(r) }}</div></div>
          <div><div class="field-label">{{ r.rewriteType === 'TEXT' ? '文本长度' : '文档字符' }}</div><div class="field-value">{{ r.totalCharacters ?? '-' }} 字</div></div>
          <div><div class="field-label">收费标准</div><div class="field-value">1.500元/千字</div></div>
          <div><div class="field-label">计费金额</div><div class="field-value price">{{ r.cost ?? '-' }}</div></div>
        </div>
      </div>
    </section>
    <section class="panel" v-else>
      <h1 class="title">使用明细</h1>
      <div class="empty">请先登录</div>
    </section>
  </div>

  <!-- 预览弹窗 -->
  <el-dialog v-model="previewVisible" width="1000px" :close-on-click-modal="true" class="preview-dialog">
    <template #header>
      <div class="preview-header"><span class="preview-title">预览</span><span class="preview-close" @click="previewVisible = false">×</span></div>
    </template>
    <div class="preview-section">
      <div class="preview-section-head"><span>原文</span><div class="preview-tools"><span class="preview-tool" @click="copyText(previewText?.originalText)"><el-icon><DocumentCopy /></el-icon> 复制</span><span class="preview-tool" @click="downloadPreviewDoc(previewText?.id, 'original')"><el-icon><Download /></el-icon> 下载DOCX</span></div></div>
      <div class="preview-text-box">{{ previewText?.originalText }}</div>
    </div>
    <div class="preview-section">
      <div class="preview-section-head"><span>结果</span><div class="preview-tools"><span class="preview-tool" @click="copyText(previewText?.paraphrasedText)"><el-icon><DocumentCopy /></el-icon> 复制</span><span class="preview-tool blue" @click="downloadPreviewDoc(previewText?.id, 'result')"><el-icon><Download /></el-icon> 下载DOCX</span></div></div>
      <div class="preview-text-box preview-result-box">{{ previewText?.paraphrasedText }}</div>
    </div>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy, Download } from '@element-plus/icons-vue'
import { rewriteRecords } from '@/api/rewrite'
import { useUserStore } from '@/stores/user'
import AnnouncementBell from '@/components/AnnouncementBell.vue'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)
const noticeClosed = ref(false)
const previewVisible = ref(false)
const previewText = ref(null)
const window = globalThis

onMounted(async () => {
  if (!userStore.token) return
  loading.value = true
  try { records.value = await rewriteRecords() } finally { loading.value = false }
})

const summaryOf = (r) => {
  if (r.rewriteType === 'TEXT') {
    const t = r.paraphrasedText || r.originalText || ''
    return t.substring(0, 30) + (t.length > 30 ? '...' : '') || '-'
  }
  return r.originalFilename || '-'
}

const downloadOriginal = (r) => {
  if (!r.originalText) return
  const token = localStorage.getItem('user_token')
  fetch(`/api/rewrite/records/${r.id}/download?type=original`, { headers: { Authorization: token } })
    .then(res => res.blob())
    .then(blob => {
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = '原文.docx'
      a.click()
      URL.revokeObjectURL(url)
    })
}

const downloadResult = (r) => {
  const token = localStorage.getItem('user_token')
  const a = document.createElement('a')
  a.href = `/api/rewrite/records/${r.id}/download`
  a.download = '改写结果.docx'
  if (token) a.setAttribute('data-token', token)
  // 用 fetch 下载，带上 Authorization
  fetch(a.href, { headers: { Authorization: token } })
    .then(res => res.blob())
    .then(blob => {
      const url = URL.createObjectURL(blob)
      const el = document.createElement('a')
      el.href = url
      el.download = '改写结果.docx'
      el.click()
      URL.revokeObjectURL(url)
    })
}

const copyText = (text) => {
  if (!text) return
  try {
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const downloadPreviewDoc = (id, type) => {
  if (!id) return
  const token = localStorage.getItem('user_token')
  fetch(`/api/rewrite/records/${id}/download?type=${type}`, { headers: { Authorization: token } })
    .then(res => res.blob())
    .then(blob => {
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = (type === 'original' ? '原文' : '改写结果') + '.docx'
      a.click()
      URL.revokeObjectURL(url)
    })
}
</script>

<style scoped>
.page{width:1100px;margin:0 auto;min-height:calc(100vh - 68px);padding:20px 0 26px}
.notice{width:100%;height:60px;border:1px solid #f8ca8c;border-radius:4px;background:#fff8ef;display:flex;align-items:center;padding:0 17px;color:#0b1634;font-size:18px;margin-bottom:14px}
.notice-icon{width:25px;height:25px;border-radius:50%;background:#f39a16;color:#fff;display:inline-flex;align-items:center;justify-content:center;font-weight:700;margin-right:14px;font-size:16px;flex-shrink:0}
.notice-close{margin-left:auto;color:#666;font-size:28px;cursor:pointer}
.panel{background:#fff;border-radius:18px;padding:18px 48px 25px;box-shadow:0 1px 2px rgba(0,0,0,.04);min-height:400px;margin-bottom:16px}
.title{margin:0 0 15px;font-size:26px;font-weight:800}
.empty{color:#999;text-align:center;padding:80px 0;font-size:16px}
.record-card{border:1px solid #e9edf3;border-radius:14px;padding:21px 21px 20px;box-shadow:0 2px 8px rgba(18,32,61,.055);background:#fff;margin-bottom:16px}
.record-head{display:flex;align-items:center;gap:7px;flex-wrap:wrap;margin-bottom:4px}
.record-name{font-size:18px;font-weight:800}
.tag{height:26px;padding:0 10px;border-radius:4px;font-size:14px;display:inline-flex;align-items:center;justify-content:center;line-height:1}
.tag-text,.tag-file{color:#673cff;background:#f0e9ff}
.tag-done{color:#079168;background:#dcfaef;border:1px solid #9ce9ce;gap:5px}
.dot{width:7px;height:7px;border-radius:50%;background:#10b981;display:inline-block}
.meta{font-size:15px;color:#8a96ad;margin-bottom:10px}
.actions{display:flex;align-items:center;gap:10px;margin-bottom:15px}
.btn{min-width:130px;height:40px;border-radius:7px;border:1px solid #d8dde7;background:#fff;color:#111827;font-size:16px;display:inline-flex;align-items:center;justify-content:center;gap:8px;cursor:pointer;box-shadow:0 1px 2px rgba(0,0,0,.04);font-family:inherit}
.btn:disabled{opacity:.4;cursor:not-allowed}
.btn-primary{background:#5545eb;color:#fff;border-color:#5545eb;box-shadow:0 4px 10px rgba(85,69,235,.24)}
.btn-small{min-width:76px}
.info-box{background:#f7f8fa;border-radius:8px;min-height:100px;padding:15px 16px;display:grid;grid-template-columns:1fr 1fr;column-gap:60px;row-gap:14px}
.field-label{font-size:14px;color:#8a96ad;margin-bottom:5px}
.field-value{font-size:15px;color:#07142f;line-height:1.45}
.price{color:#3633ff;font-weight:800}
.preview-header{display:flex;align-items:center;justify-content:space-between;width:100%}
.preview-title{font-size:20px;font-weight:600}
.preview-close{width:28px;height:28px;border-radius:4px;background:#eef0f2;color:#555;font-size:22px;display:flex;align-items:center;justify-content:center;cursor:pointer;line-height:1}
.preview-section{margin-bottom:16px}
.preview-section-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px;color:#6b7280;font-size:15px;font-weight:700}
.preview-tools{display:flex;gap:24px;align-items:center}
.preview-tool{display:inline-flex;align-items:center;gap:4px;cursor:pointer;color:#4b5563;font-size:14px;font-weight:400;user-select:none}
.preview-tool:hover{color:#5545eb}
.preview-tool.blue{color:#4c45ff}
.preview-text-box{border:1px solid #edf0f4;border-radius:8px;background:#fafafa;padding:14px 16px;font-size:15px;line-height:1.7;color:#374151;max-height:260px;overflow-y:auto;white-space:pre-wrap}
.preview-result-box{border:1px solid #b9efcf;background:#ecfff6}
@media(max-width:900px){.page{padding:14px}.panel{padding:18px}.info-box{grid-template-columns:1fr}.actions{flex-wrap:wrap}}
</style>
