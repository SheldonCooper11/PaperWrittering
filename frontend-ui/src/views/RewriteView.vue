<template>
  <header class="topbar"><RouterLink class="brand" to="/">一触即改 写作牛 <span class="icon">🦅</span></RouterLink><nav class="nav"><RouterLink class="active" to="/rewrite">降重/降AI率/检测AI率 🔥</RouterLink><RouterLink to="/records">改写记录</RouterLink><RouterLink to="/redeem">卡密兑换</RouterLink><RouterLink v-if="!userStore.token" class="login" to="/login">登录/注册</RouterLink><el-dropdown v-else trigger="click"><span class="login username">{{ userStore.userInfo?.username }} ▾</span><template #dropdown><el-dropdown-menu><el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item><el-dropdown-item @click="userStore.logout(); $router.push('/')">退出登录</el-dropdown-item></el-dropdown-menu></template></el-dropdown></nav></header>
  <div class="float-service"><div class="grid">▦</div><span>加群</span></div>
  <main class="wrap">
    <section class="feature-area"><div class="feature-cards"><div class="feature-card" :class="{selected: mode === item.value}" v-for="item in modes" :key="item.value" @click="onModeChange(item.value)"><h3>{{ item.icon }} {{ item.title }}</h3><p>{{ item.desc }}</p></div></div></section>
    <section class="filters">
      <div class="row"><span class="label">文本语言</span><span class="pill" :class="{active: language === 'chinese'}" @click="onLanguageChange('chinese')">中文</span><span class="pill" :class="{active: language === 'english'}" @click="onLanguageChange('english')">英文</span></div>
      <div class="row"><span class="label">检测平台</span><div class="platforms"><span class="pill" :class="{active: platform === item}" v-for="item in platforms" :key="item" @click="onPlatformChange(item)">{{ item }}</span></div></div>
      <div class="row"><span class="label">模式选择</span><select v-model="selectedPreset" class="select"><option v-for="item in presets" :key="item.code" :value="item.code">{{ item.name }}</option></select></div>
    </section>
    <section class="editor">
      <div class="left">
        <div class="tabs"><div class="tab" :class="{active: activeTab === 'text'}" @click="activeTab = 'text'">📝 文本输入</div><div class="tab" :class="{active: activeTab === 'file'}" @click="activeTab = 'file'">📥 上传文件</div></div>
        <textarea v-if="activeTab === 'text'" v-model="text" class="textarea" placeholder="输入或粘贴中文文本（最多100000个字符）&#10;Ctrl+Enter 快速改写" maxlength="100000" @keydown.ctrl.enter="handleTextRewrite"></textarea>
        <div v-else class="upload-panel"><el-upload drag :auto-upload="false" :limit="1" :on-change="onFileChange" :on-remove="onFileRemove"><div class="upload-icon">📄</div><div>点击或拖拽文件到这里上传</div><template #tip><div class="upload-tip">支持 doc、docx、pdf、txt 等常见论文文件</div></template></el-upload></div>
        <div class="bottom-bar"><span class="count">{{ text.length }}/100000 字符</span><div class="actions"><button class="btn" @click="setExample">样例</button><button class="btn primary" :disabled="loading || !selectedPreset" @click="activeTab === 'text' ? handleTextRewrite() : handleFileRewrite()">{{ loading ? '处理中...' : '一键改写' }}</button></div></div>
      </div>
      <div class="right"><div class="result-head">处理结果</div><div v-if="!result" class="empty"><div class="doc">📄</div><h4>当前暂无改写结果</h4><p>请在输入面板添加文本并点击一键改写按钮</p></div><div v-else class="result-content"><template v-if="result.paraphrasedText"><h4>改写结果</h4><p>{{ result.paraphrasedText }}</p></template><template v-else><h4>文档改写完成</h4><p class="complete-tip">文件处理完成，请点击下方按钮下载文件</p><a class="dl-btn" v-if="result.paraphrasedOssUrl" :href="result.paraphrasedOssUrl" target="_blank">📥 下载改写文件</a></template><div class="meta">字符数：{{ result.characterCount || result.totalCharacters || '-' }}　耗时：{{ result.processingTime || '-' }} 秒　消耗：{{ result.cost || result.actualCost || '-' }} 元　剩余：{{ result.remainingBalance ?? (userStore.userInfo?.balance ?? '-') }} 元</div><div class="meta" v-if="result.presetName">使用模式：{{ result.presetName }}</div></div></div>
    </section>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { rewriteDocument, rewriteText, fetchPresets } from '@/api/rewrite'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const platforms = ['知网','维普','格子达','PaperYY','笔杆网','万方','PaperPass','华宸','paperred','writepass','papered','大雅','朱雀']
const modes = [
  { icon:'📋', title:'降重复率(深度)', value:'降重strong', desc:'全网领先的降重技术，一键大幅降低重复率，保证学术表达' },
  { icon:'🪄', title:'降AI率', value:'降AI', desc:'优化文本内容，降低AI生成痕迹，使文章更具人文特色' },
  { icon:'🧾', title:'降重+降AIGC', value:'双降', desc:'同时降低重复率和AI生成痕迹，全面提升文章质量' },
  { icon:'🧾', title:'AI率预测', value:'降低AIplus', desc:'选择对应的算法，比如对应平台更为严格，只要在10%内，基本通过对应的所有平台' },
]

const modeToModule = {
  '降重strong': 'repeat_reduce',
  '降AI': 'ai_reduce',
  '双降': 'dual_reduce',
  '降低AIplus': null,
}

const language = ref('chinese')
const platform = ref('知网')
const mode = ref('降AI')
const selectedPreset = ref('')
const presets = ref([])
const activeTab = ref('text')
const text = ref('')
const file = ref(null)
const result = ref(null)
const loading = ref(false)

const loadPresets = async (moduleCode) => {
  if (!moduleCode) { presets.value = []; return }
  try {
    const res = await fetchPresets(moduleCode, platform.value, language.value)
    presets.value = res || []
    if (presets.value.length > 0) selectedPreset.value = presets.value[0].code
  } catch {
    presets.value = []
  }
}

const onLanguageChange = (lang) => {
  language.value = lang
  loadPresets(modeToModule[mode.value] || null)
}

const onModeChange = (val) => {
  mode.value = val
  loadPresets(modeToModule[val] || null)
}

const onPlatformChange = (val) => {
  platform.value = val
  loadPresets(modeToModule[mode.value] || null)
}

loadPresets(modeToModule[mode.value])

const setExample = () => { text.value = '人工智能技术正在快速发展，为各行各业带来了巨大的变革。这些技术不仅提高了工作效率，还创造了许多新的商业机会。' }
const onFileChange = (uploadFile) => { file.value = uploadFile.raw }
const onFileRemove = () => { file.value = null }

const handleTextRewrite = async () => {
  if (!localStorage.getItem('user_token')) return ElMessage.warning('请先登录')
  if (!text.value.trim()) return ElMessage.warning('请输入需要改写的文本')
  if (!selectedPreset.value) return ElMessage.warning('请选择改写模式')
  loading.value = true
  try {
    result.value = await rewriteText({ text: text.value, preset: selectedPreset.value })
    await userStore.refreshBalance()
  } finally { loading.value = false }
}
const handleFileRewrite = async () => {
  if (!localStorage.getItem('user_token')) return ElMessage.warning('请先登录')
  if (!file.value) return ElMessage.warning('请先选择文件')
  if (!selectedPreset.value) return ElMessage.warning('请选择改写模式')
  const formData = new FormData()
  formData.append('file', file.value)
  formData.append('preset', selectedPreset.value)
  loading.value = true
  try {
    result.value = await rewriteDocument(formData)
    await userStore.refreshBalance()
  } finally { loading.value = false }
}
</script>

<style scoped>
.wrap{width:1120px;margin:0 auto;background:#fff;min-height:calc(100vh - 68px)}.feature-area{background:linear-gradient(135deg,#2c3e8f,#1a2772);padding:24px 22px 22px}.feature-cards{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}.feature-card{min-height:112px;background:#fff;border-radius:12px;padding:18px 16px 16px;box-shadow:0 2px 8px rgba(0,0,0,.06);position:relative;cursor:pointer;transition:transform .2s,box-shadow .2s;border:2px solid transparent}.feature-card:hover{transform:translateY(-3px);box-shadow:0 8px 24px rgba(44,62,143,.15)}.feature-card:active{transform:scale(.97)}.feature-card.selected{border-color:#2c3e8f;box-shadow:0 4px 20px rgba(44,62,143,.18)}.feature-card.selected:after{content:"✓";position:absolute;right:10px;top:10px;width:22px;height:22px;border-radius:50%;background:#2c3e8f;color:#fff;text-align:center;line-height:22px;font-weight:800;font-size:13px}.feature-card h3{font-size:18px;margin-bottom:10px;color:#1a2772;display:flex;gap:8px;align-items:center}.feature-card p{font-size:13px;line-height:1.6;color:#555}.filters{background:#eef2ff;padding:16px 18px 18px;font-size:14px;color:#333}.row{display:flex;align-items:center;gap:12px;margin-bottom:14px}.label{color:#444;margin-right:2px}.pill{padding:5px 8px;border-radius:4px;cursor:pointer}.pill.active{background:#2c3e8f;color:#fff}.platforms{display:flex;gap:18px;flex-wrap:wrap}.select{height:30px;width:320px;border:1px solid #d3d7df;border-radius:4px;background:#fff;color:#555;padding:0 9px}.editor{display:grid;grid-template-columns:1fr 1fr;border-top:1px solid #e7e7e7;min-height:535px}.left,.right{position:relative;background:#fff}.left{border-right:18px solid #f5f7fb}.tabs{height:46px;display:flex;align-items:center;padding-left:22px;gap:16px;border-bottom:1px solid #e5e5e5}.tab{height:30px;line-height:30px;padding:0 18px;border-radius:4px;color:#333;cursor:pointer}.tab.active{background:#2c3e8f;color:#fff}.textarea{position:absolute;left:0;right:0;top:46px;bottom:48px;width:100%;border:0;resize:none;padding:28px 16px;font-size:14px;line-height:1.8;outline:none;color:#333}.upload-panel{padding:80px 34px}.upload-icon{font-size:44px;margin-bottom:12px}.upload-tip{color:#999;font-size:13px;margin-top:8px}.bottom-bar{position:absolute;left:0;right:0;bottom:0;height:48px;border-top:1px solid #e5e5e5;display:flex;align-items:center;padding:0 14px}.count{font-size:13px;color:#555}.actions{margin-left:auto;display:flex;gap:10px}.btn{border:1px solid #dfe3ec;background:#f7f8fb;border-radius:5px;padding:8px 18px;color:#444}.btn.primary{background:#2c3e8f;border-color:#2c3e8f;color:#fff}.btn.primary:disabled{opacity:.5;cursor:not-allowed}.result-head{height:46px;border-bottom:1px solid #e5e5e5;display:flex;align-items:center;padding:0 14px;font-weight:800}.complete-tip{color:#555;margin:12px 0 20px}.dl-btn{display:inline-block;background:#2c3e8f;color:#fff;text-decoration:none;border-radius:6px;padding:12px 28px;font-size:16px;font-weight:700;transition:opacity .2s}.dl-btn:hover{opacity:.9}.empty{position:absolute;top:245px;left:50%;transform:translateX(-50%);text-align:center;color:#999;white-space:nowrap}.empty .doc{font-size:43px;margin-bottom:18px}.empty h4{font-size:18px;color:#666;margin-bottom:12px;font-weight:500}.empty p{font-size:13px;color:#aaa}.result-content{padding:24px;font-size:15px;line-height:1.9}.result-content h4{font-size:20px;margin-bottom:14px;color:#5967d9}.result-content p{white-space:pre-wrap}.result-content a{color:#5967d9;font-weight:800}.meta{margin-top:18px;color:#777;font-size:13px}@media(max-width:1180px){.wrap{width:100%}.topbar{padding:0 28px}.feature-cards{grid-template-columns:repeat(2,1fr)}}
</style>
