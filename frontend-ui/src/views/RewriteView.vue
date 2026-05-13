<template>
  <header class="topbar"><RouterLink class="brand" to="/">一触即改 写作牛 <span class="icon">🦅</span></RouterLink><nav class="nav"><RouterLink class="active" to="/rewrite">降重/降AI率/检测AI率 🔥</RouterLink><RouterLink to="/records">改写记录</RouterLink><RouterLink to="/redeem">卡密兑换</RouterLink><AnnouncementBell /><RouterLink v-if="!userStore.token" class="login" to="/login">登录/注册</RouterLink><el-dropdown v-else trigger="click"><span class="login username">{{ userStore.userInfo?.username }} ▾</span><template #dropdown><el-dropdown-menu><el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item><el-dropdown-item @click="userStore.logout(); $router.push('/')">退出登录</el-dropdown-item></el-dropdown-menu></template></el-dropdown></nav></header>
  <div class="float-service"><div class="grid">▦</div><span>加群</span></div>
  <main class="wrap">
    <section class="feature-area"><div class="feature-cards"><div class="feature-card" :class="{selected: mode === item.value}" v-for="item in modes" :key="item.value" @click="onModeChange(item.value)"><h3>{{ item.icon }} {{ item.title }}</h3><p>{{ item.desc }}</p></div></div></section>

    <section class="filters">
      <template v-if="!isDetection">
        <div class="row"><span class="label">文本语言</span><span class="pill" :class="{active: language === 'chinese'}" @click="onLanguageChange('chinese')">中文</span><span class="pill" :class="{active: language === 'english'}" @click="onLanguageChange('english')">英文</span></div>
        <div class="row"><span class="label">检测平台</span><div class="platforms"><span class="pill" :class="{active: platform === item}" v-for="item in platforms" :key="item" @click="onPlatformChange(item)">{{ item }}</span></div></div>
        <div class="row"><span class="label">模式选择</span><select v-model="selectedPreset" class="select"><option v-for="item in presets" :key="item.code" :value="item.code">{{ item.name }}</option></select></div>
      </template>
      <template v-else>
        <div class="row detection-row"><span class="label">检测模式</span><select class="select" disabled><option>文本检测</option></select><span class="label">语言</span><select v-model="detectionLang" class="select" @change="onDetectionLangChange"><option value="zh">中文</option><option value="en">English</option></select><span class="label">平台</span><select v-model="detectionPlatform" class="select"><option v-for="p in detectionPlatforms" :key="p.task_platform" :value="p.task_platform">{{ p.task_platform }}</option></select></div>
      </template>
    </section>

    <section class="editor">
      <div class="left">
        <template v-if="!isDetection">
          <div class="tabs"><div class="tab" :class="{active: activeTab === 'text'}" @click="activeTab = 'text'">📝 文本输入</div><div class="tab" :class="{active: activeTab === 'file'}" @click="activeTab = 'file'">📥 上传文件</div></div>
          <textarea v-if="activeTab === 'text'" v-model="text" class="textarea" placeholder="输入或粘贴中文文本（最多100000个字符）&#10;Ctrl+Enter 快速改写" maxlength="100000" @keydown.ctrl.enter="handleTextRewrite"></textarea>
          <div v-else class="upload-panel"><el-upload drag :auto-upload="false" :limit="1" :on-change="onFileChange" :on-remove="onFileRemove"><div class="upload-icon">📄</div><div>点击或拖拽文件到这里上传</div><template #tip><div class="upload-tip">仅支持 docx、txt 格式</div></template></el-upload></div>
          <div class="bottom-bar"><span class="count">{{ text.length }}/100000 字符</span><div class="actions"><button class="btn" @click="setExample">样例</button><button class="btn primary" :disabled="loading || !selectedPreset" @click="activeTab === 'text' ? handleTextRewrite() : handleFileRewrite()">{{ loading ? '处理中...' : '一键改写' }}</button></div></div>
        </template>
        <template v-else>
          <textarea v-model="text" class="detection-textarea" placeholder="AI辅写风险检测，单次最大检测1W字内容" maxlength="10000"></textarea>
          <div class="bottom-bar"><span class="count">{{ text.length }}/10000</span><div class="actions"><button class="btn primary" :disabled="loading || !detectionPlatform" @click="handleDetection">{{ loading ? '处理中...' : '开始检测' }}</button></div></div>
        </template>
      </div>
      <div class="right">
        <div class="result-head">处理结果</div>
        <template v-if="!isDetection">
          <div v-if="!result" class="empty"><div class="doc">📄</div><h4>当前暂无改写结果</h4><p>请在输入面板添加文本并点击一键改写按钮</p></div>
          <div v-else class="result-content">
            <template v-if="result.paraphrasedText"><h4>改写结果</h4><p>{{ result.paraphrasedText }}</p></template>
            <template v-else><h4>文档改写完成</h4><p class="complete-tip">文件处理完成，请点击下方按钮下载文件</p><a class="dl-btn" v-if="result.paraphrasedOssUrl" :href="result.paraphrasedOssUrl" target="_blank">📥 下载改写文件</a></template>
            <div class="meta" v-if="result.presetName">使用模式：{{ result.presetName }}</div>
          </div>
        </template>
        <template v-else>
          <div class="detection-result" v-if="result && result.score !== undefined">
            <div class="result-top">
              <div class="score-ring" :class="riskClass">{{ result.score != null ? result.score.toFixed(1) : '-' }}<span class="pct">%</span></div>
              <div class="level-tag" :class="riskClass">{{ result.level || '未知' }}</div>
            </div>
            <div class="detection-meta">
              <div class="dm-row"><span>最高分</span><span>{{ result.maxScore }}</span></div>
              <div class="dm-row"><span>字符数</span><span>{{ result.totalChars }}</span></div>
              <div class="dm-row"><span>扣费</span><span>{{ result.userCost }} 元</span></div>
              <div class="dm-row"><span>剩余</span><span>{{ result.remainingBalance }} 元</span></div>
            </div>
            <div class="segments" v-if="result.segments && result.segments.length">
              <h4>逐段分析</h4>
              <div class="seg-item" v-for="(seg, i) in result.segments" :key="i">
                <span class="seg-label" :class="segLabelClass(seg.label)">{{ seg.label }}</span>
                <span class="seg-text">{{ seg.segment }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty"><div class="doc">📄</div><h4>当前暂无检测结果</h4><p>请在左侧输入文本并点击开始检测</p></div>
        </template>
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { precheckDocument, rewriteDocument, rewriteText, fetchPresets, fetchPlatforms, fetchDetectionOptions, checkDetection } from '@/api/rewrite'
import { useUserStore } from '@/stores/user'
import AnnouncementBell from '@/components/AnnouncementBell.vue'

const userStore = useUserStore()
const platforms = ref([])
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

const isDetection = computed(() => mode.value === '降低AIplus')
const language = ref('chinese')
const platform = ref('')
const mode = ref('降AI')
const selectedPreset = ref('')
const presets = ref([])
const activeTab = ref('text')
const text = ref('')
const file = ref(null)
const result = ref(null)
const loading = ref(false)
const detectionPlatforms = ref([])
const detectionPlatform = ref('')
const detectionLang = ref('zh')

const riskClass = computed(() => {
  if (!result.value || result.value.score === undefined) return ''
  const s = result.value.score
  if (s >= 80) return 'high'
  if (s >= 50) return 'mid'
  return 'low'
})
const segLabelClass = (label) => {
  if (!label) return ''
  if (label.includes('高')) return 'high'
  if (label.includes('中')) return 'mid'
  return 'low'
}

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

const loadPlatforms = async (moduleCode) => {
  if (!moduleCode) { platforms.value = []; return }
  try {
    const list = await fetchPlatforms(moduleCode, language.value)
    platforms.value = list && list.length > 0 ? list : []
    if (platforms.value.length > 0) {
      platform.value = platforms.value[0]
    }
  } catch {
    platforms.value = []
  }
}

const loadDetectionPlatforms = async (lang) => {
  try {
    const res = await fetchDetectionOptions(lang)
    detectionPlatforms.value = res?.platforms || []
    if (detectionPlatforms.value.length > 0) {
      detectionPlatform.value = detectionPlatforms.value[0].task_platform
    }
  } catch {
    detectionPlatforms.value = []
  }
}

const refreshModule = async (moduleCode) => {
  if (isDetection.value) { return }
  await loadPlatforms(moduleCode)
  await loadPresets(moduleCode)
}

const onLanguageChange = (lang) => {
  language.value = lang
  refreshModule(modeToModule[mode.value] || null)
}

const onModeChange = (val) => {
  mode.value = val
  result.value = null
  text.value = ''
  if (val === '降低AIplus') {
    detectionLang.value = 'zh'
    loadDetectionPlatforms('zh')
  } else {
    refreshModule(modeToModule[val] || null)
  }
}

const onDetectionLangChange = () => {
  detectionPlatform.value = ''
  loadDetectionPlatforms(detectionLang.value)
}

const onPlatformChange = (val) => {
  platform.value = val
  loadPresets(modeToModule[mode.value] || null)
}

refreshModule(modeToModule[mode.value])

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
  } catch (e) {
    const msg = e?.msg || e?.message || ''
    if (msg.includes('余额不足')) {
      ElMessageBox.alert(msg, '余额不足', { confirmButtonText: '去充值', type: 'warning' })
    }
  } finally { loading.value = false }
}
const handleFileRewrite = async () => {
  if (!localStorage.getItem('user_token')) return ElMessage.warning('请先登录')
  if (!file.value) return ElMessage.warning('请先选择文件')
  if (!selectedPreset.value) return ElMessage.warning('请选择改写模式')

  loading.value = true
  try {
    const preForm = new FormData()
    preForm.append('file', file.value)
    const pre = await precheckDocument(preForm)
    const { charCount, estimatedCost, balance } = pre

    if (balance < estimatedCost) {
      loading.value = false
      const shortage = (estimatedCost - balance).toFixed(3)
      ElMessageBox.alert(
        `账户余额${balance}元，总共需要${estimatedCost}，还需要最低充值${shortage}元。`,
        '余额不足',
        { confirmButtonText: '知道了', type: 'warning' }
      )
      return
    }

    await ElMessageBox.confirm(
      `预计费用为 ${estimatedCost} 元，（字符数）为 ${charCount} 个，具体处理以实际处理为准，是否继续?`,
      '确认改写',
      { confirmButtonText: '继续', cancelButtonText: '取消', type: 'info' }
    )
  } catch (e) {
    loading.value = false
    if (e === 'cancel' || e === 'close') return
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', file.value)
    formData.append('preset', selectedPreset.value)
    result.value = await rewriteDocument(formData)
    await userStore.refreshBalance()
  } catch (e) {
    const msg = e?.msg || e?.message || ''
    if (msg.includes('余额不足')) {
      ElMessageBox.alert(msg, '余额不足', { confirmButtonText: '知道了', type: 'warning' })
    }
  } finally { loading.value = false }
}

const handleDetection = async () => {
  if (!localStorage.getItem('user_token')) return ElMessage.warning('请先登录')
  if (!text.value.trim()) return ElMessage.warning('请输入需要检测的文本')
  if (!detectionPlatform.value) return ElMessage.warning('请选择检测平台')
  loading.value = true
  try {
    result.value = await checkDetection({ text: text.value, task_platform: detectionPlatform.value })
    await userStore.refreshBalance()
  } catch (e) {
    const msg = e?.msg || e?.message || ''
    if (msg.includes('余额不足')) {
      ElMessageBox.alert(msg, '余额不足', { confirmButtonText: '去充值', type: 'warning' })
    }
  } finally { loading.value = false }
}
</script>

<style scoped>
.wrap{width:1120px;margin:0 auto;background:#fff;min-height:calc(100vh - 68px)}.feature-area{background:linear-gradient(135deg,#2c3e8f,#1a2772);padding:24px 22px 22px}.feature-cards{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}.feature-card{min-height:112px;background:#fff;border-radius:12px;padding:18px 16px 16px;box-shadow:0 2px 8px rgba(0,0,0,.06);position:relative;cursor:pointer;transition:transform .2s,box-shadow .2s;border:2px solid transparent}.feature-card:hover{transform:translateY(-3px);box-shadow:0 8px 24px rgba(44,62,143,.15)}.feature-card:active{transform:scale(.97)}.feature-card.selected{border-color:#2c3e8f;box-shadow:0 4px 20px rgba(44,62,143,.18)}.feature-card.selected:after{content:"✓";position:absolute;right:10px;top:10px;width:22px;height:22px;border-radius:50%;background:#2c3e8f;color:#fff;text-align:center;line-height:22px;font-weight:800;font-size:13px}.feature-card h3{font-size:18px;margin-bottom:10px;color:#1a2772;display:flex;gap:8px;align-items:center}.feature-card p{font-size:13px;line-height:1.6;color:#555}.filters{background:#eef2ff;padding:16px 18px 18px;font-size:14px;color:#333}.row{display:flex;align-items:center;gap:12px;margin-bottom:14px}.label{color:#444;margin-right:2px}.pill{padding:5px 8px;border-radius:4px;cursor:pointer}.pill.active{background:#2c3e8f;color:#fff}.platforms{display:flex;gap:18px;flex-wrap:wrap}.select{height:30px;width:320px;border:1px solid #d3d7df;border-radius:4px;background:#fff;color:#555;padding:0 9px}.detection-row .select{width:200px}.editor{display:grid;grid-template-columns:1fr 1fr;border-top:1px solid #e7e7e7;min-height:535px}.left,.right{position:relative;background:#fff}.left{border-right:18px solid #f5f7fb}.tabs{height:46px;display:flex;align-items:center;padding-left:22px;gap:16px;border-bottom:1px solid #e5e5e5}.tab{height:30px;line-height:30px;padding:0 18px;border-radius:4px;color:#333;cursor:pointer}.tab.active{background:#2c3e8f;color:#fff}.textarea{position:absolute;left:0;right:0;top:46px;bottom:48px;width:100%;border:0;resize:none;padding:28px 16px;font-size:14px;line-height:1.8;outline:none;color:#333}.upload-panel{padding:80px 34px}.upload-icon{font-size:44px;margin-bottom:12px}.upload-tip{color:#999;font-size:13px;margin-top:8px}.bottom-bar{position:absolute;left:0;right:0;bottom:0;height:48px;border-top:1px solid #e5e5e5;display:flex;align-items:center;padding:0 14px}.count{font-size:13px;color:#555}.actions{margin-left:auto;display:flex;gap:10px}.btn{border:1px solid #dfe3ec;background:#f7f8fb;border-radius:5px;padding:8px 18px;color:#444}.btn.primary{background:#2c3e8f;border-color:#2c3e8f;color:#fff}.btn.primary:disabled{opacity:.5;cursor:not-allowed}.result-head{height:46px;border-bottom:1px solid #e5e5e5;display:flex;align-items:center;padding:0 14px;font-weight:800}.complete-tip{color:#555;margin:12px 0 20px}.dl-btn{display:inline-block;background:#2c3e8f;color:#fff;text-decoration:none;border-radius:6px;padding:12px 28px;font-size:16px;font-weight:700;transition:opacity .2s}.dl-btn:hover{opacity:.9}.empty{position:absolute;top:245px;left:50%;transform:translateX(-50%);text-align:center;color:#999;white-space:nowrap}.empty .doc{font-size:43px;margin-bottom:18px}.empty h4{font-size:18px;color:#666;margin-bottom:12px;font-weight:500}.empty p{font-size:13px;color:#aaa}.result-content{padding:24px;font-size:15px;line-height:1.9}.result-content h4{font-size:20px;margin-bottom:14px;color:#5967d9}.result-content p{white-space:pre-wrap}.result-content a{color:#5967d9;font-weight:800}.meta{margin-top:18px;color:#777;font-size:13px}

/* 检测模式左侧输入框 */
.detection-textarea{position:absolute;left:0;right:0;top:0;bottom:48px;width:100%;border:0;resize:none;padding:28px 16px;font-size:14px;line-height:1.8;outline:none;color:#333}

/* 检测结果 */
.detection-result{padding:24px;display:flex;flex-direction:column;align-items:center;height:100%;overflow-y:auto}
.result-top{display:flex;align-items:center;gap:20px;margin-bottom:16px}
.score-ring{width:90px;height:90px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:28px;font-weight:800;color:#fff;flex-direction:column;flex-shrink:0}
.score-ring .pct{font-size:13px;font-weight:400;opacity:.85}
.score-ring.high{background:#e74c3c}.score-ring.mid{background:#f39c12}.score-ring.low{background:#27ae60}
.level-tag{display:inline-block;padding:4px 18px;border-radius:12px;font-size:15px;font-weight:700;flex-shrink:0}
.level-tag.high{background:#fde8e8;color:#c0392b}
.level-tag.mid{background:#fef3cd;color:#b8730a}
.level-tag.low{background:#d4edda;color:#1a6e30}
.detection-meta{display:grid;grid-template-columns:1fr 1fr;gap:6px;width:100%;margin-bottom:16px;font-size:13px;color:#666}
.dm-row{display:flex;justify-content:space-between;padding:4px 8px;background:#f9fafb;border-radius:4px}
.segments{width:100%}
.segments h4{font-size:15px;margin-bottom:8px;color:#333}
.seg-item{display:flex;gap:6px;align-items:flex-start;margin-bottom:6px;font-size:13px;line-height:1.6}
.seg-label{flex-shrink:0;padding:1px 6px;border-radius:4px;font-size:11px;font-weight:700;color:#fff}
.seg-label.high{background:#e74c3c}.seg-label.mid{background:#f39c12}.seg-label.low{background:#27ae60}
.seg-text{color:#444;word-break:break-all}

@media(max-width:1180px){.wrap{width:100%}.topbar{padding:0 28px}.feature-cards{grid-template-columns:repeat(2,1fr)}}
</style>
