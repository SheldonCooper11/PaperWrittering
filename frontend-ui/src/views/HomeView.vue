<template>
  <header class="topbar">
    <RouterLink class="brand" to="/">一触即改 写作牛 <span class="icon">🦅</span></RouterLink>
    <nav class="nav">
      <RouterLink to="/rewrite">降重/降AI率/检测AI率 🔥</RouterLink>
      <RouterLink to="/records">改写记录</RouterLink>
      <RouterLink to="/redeem">卡密兑换</RouterLink>
      <AnnouncementBell />
      <RouterLink v-if="!userStore.token" class="login" to="/login">登录/注册</RouterLink>
      <el-dropdown v-else trigger="click">
        <span class="login username">{{ userStore.userInfo?.username }} ▾</span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$router.push('/profile')">个人中心</el-dropdown-item>
            <el-dropdown-item @click="userStore.logout(); $router.push('/')">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </nav>
  </header>
  <div class="float-service"><div class="grid">▦</div><span>加群</span></div>
  <section class="hero">
    <div class="network"><canvas ref="canvasRef"></canvas></div>
    <div class="content">
      <h1 class="title">一触即改 写作牛</h1>
      <p class="sub">智能论文助手，让论文写作更轻松。快速去重，降低AI特征，数据可视化，一站式解决论文查重难题。</p>
      <div class="cards">
        <div class="card"><div class="icon-box">🧐</div><h3>专业模型</h3><p>基于AI大语言模型，对上下文语境深度理解降重</p></div>
        <div class="card"><div class="icon-box">📊</div><h3>效果保证</h3><p>保证查重率低于15%，AIGC低于20%，超过可退款</p></div>
        <div class="card"><div class="icon-box">📈</div><h3>保留术语</h3><p>保留核心观点和高质量学术表达，避免口水话！</p></div>
      </div>
      <RouterLink class="cta" to="/rewrite">立即体验</RouterLink>
    </div>
  </section>
  <section class="feedback">
    <h2 class="section-title">用户反馈</h2><div class="line"></div>
    <div class="fb-grid">
      <div class="fb-card" v-for="item in feedbacks" :key="item.name">
        <div class="fb-top"><div class="avatar">{{ item.name.slice(0, 1) }}</div><p>{{ item.text }}</p></div>
        <div class="name">{{ item.name }}</div><div class="major">{{ item.major }}</div>
      </div>
    </div>
  </section>
  <section class="showcase">
    <h2 class="section-title">效果展示</h2><div class="line"></div>
    <div class="cases">
      <div class="case" v-for="item in cases" :key="item.title">
        <div class="case-head"><h3>{{ item.title }}</h3><span class="tag">{{ item.tag }}</span></div>
        <div class="cols"><div><div class="col-title">原文</div><div class="text-block">{{ item.before }}</div></div><div><div class="col-title">处理后</div><div class="text-block after">{{ item.after }}</div></div></div>
        <div class="more"></div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import AnnouncementBell from '@/components/AnnouncementBell.vue'

const userStore = useUserStore()
const canvasRef = ref(null)
const feedbacks = [
  { name: '张同学', major: '计算机科学与技术', text: '一触即改帮我快速检查了论文中的重复内容，节省了很多时间。AI降重功能让我的论文表达更加自然，导师也给了很好的评价。' },
  { name: '李同学', major: '人工智能', text: '作为一名研究生，经常需要写论文。一触即改的数据可视化功能帮助我更好地理解论文结构，让写作过程更加顺畅。' },
  { name: '王同学', major: '机器学习', text: '论文查重和降重一直是我的痛点，一触即改完美解决了这个问题。它的去重和降重功能非常强大，让我的论文质量有了很大提升。' },
]
const cases = [
  { title: '案例 1', tag: '去重', before: '本研究采用深度学习技术对图像进行分类。本研究采用深度学习技术对图像进行分类。本研究采用深度学习技术对图像进行分类。', after: '本研究采用深度学习技术对图像进行分类。通过构建卷积神经网络模型，实现了对输入图像的精确分类。实验结果表明，该方法在多个数据集上都取得了优异的性能。' },
  { title: '案例 2', tag: '降重', before: '本文提出了一种基于深度学习的图像识别方法。该方法使用了卷积神经网络进行特征提取，并通过全连接层进行分类。实验结果表明，该方法具有较高的准确率。', after: '本研究探索了一种创新的图像识别框架。通过构建多层次的神经网络结构，实现了对图像特征的深度提取和智能分类。经过大量实验验证，该方案展现出卓越的识别性能。' },
]

let animationId = 0
function startParticles() {
  const canvas = canvasRef.value
  const ctx = canvas?.getContext('2d')
  if (!canvas || !ctx) return
  let w = 0, h = 0, dpr = 1, points = []
  const mouse = { x: null, y: null, radius: 190 }
  const resize = () => {
    const rect = canvas.parentElement.getBoundingClientRect()
    dpr = Math.min(window.devicePixelRatio || 1, 2)
    w = rect.width
    h = rect.height
    canvas.width = Math.floor(w * dpr)
    canvas.height = Math.floor(h * dpr)
    canvas.style.width = `${w}px`
    canvas.style.height = `${h}px`
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    points = Array.from({ length: Math.max(72, Math.floor(w * h / 11500)) }, () => ({ x: Math.random() * w, y: Math.random() * h, vx: (Math.random() - 0.5) * 0.42, vy: (Math.random() - 0.5) * 0.42, r: Math.random() * 1.4 + 1.1 }))
  }
  const draw = () => {
    ctx.clearRect(0, 0, w, h)
    for (const p of points) {
      p.x += p.vx; p.y += p.vy
      if (p.x < -20 || p.x > w + 20) p.vx *= -1
      if (p.y < -20 || p.y > h + 20) p.vy *= -1
      if (mouse.x !== null) {
        const dx = p.x - mouse.x, dy = p.y - mouse.y, dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < mouse.radius && dist > 0) { const force = (mouse.radius - dist) / mouse.radius * 0.018; p.vx += dx / dist * force; p.vy += dy / dist * force }
      }
      p.vx *= 0.997; p.vy *= 0.997
    }
    for (let i = 0; i < points.length; i++) for (let j = i + 1; j < points.length; j++) {
      const a = points[i], b = points[j], dx = a.x - b.x, dy = a.y - b.y, dist = Math.sqrt(dx * dx + dy * dy)
      if (dist < 150) { ctx.strokeStyle = `rgba(18, 24, 34, ${(1 - dist / 150) * 0.42})`; ctx.lineWidth = 1; ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke() }
    }
    for (const p of points) { ctx.fillStyle = 'rgba(18, 24, 34, 0.62)'; ctx.beginPath(); ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2); ctx.fill() }
    animationId = requestAnimationFrame(draw)
  }
  const move = (e) => { const rect = canvas.getBoundingClientRect(); mouse.x = e.clientX - rect.left; mouse.y = e.clientY - rect.top }
  const leave = () => { mouse.x = null; mouse.y = null }
  window.addEventListener('resize', resize)
  window.addEventListener('mousemove', move)
  window.addEventListener('mouseleave', leave)
  resize(); draw()
}

onMounted(startParticles)
onUnmounted(() => cancelAnimationFrame(animationId))
</script>

<style scoped>
.hero{position:relative;min-height:650px;padding-top:42px;text-align:center;overflow:hidden}.network{position:absolute;inset:0;z-index:0;opacity:.95;background:linear-gradient(#f7fbff,#f3f7fb)}canvas{position:absolute;inset:0;width:100%;height:100%;z-index:0;display:block;pointer-events:none}.content{position:relative;z-index:1}.title{font-size:55px;line-height:1.1;color:#5b66da;font-weight:900;letter-spacing:2px;text-shadow:0 6px 8px rgba(30,45,160,.25);margin-bottom:18px}.sub{font-size:18px;line-height:1.7;max-width:620px;margin:0 auto;color:#111}.cards{margin:88px auto 50px;display:grid;grid-template-columns:repeat(3,270px);gap:180px;justify-content:center}.card{height:170px;background:rgba(255,255,255,.95);border-radius:12px;box-shadow:0 10px 30px rgba(0,0,0,.06);padding:20px 22px;display:flex;flex-direction:column;align-items:center;justify-content:center}.icon-box{font-size:35px;margin-bottom:18px}.card h3{font-size:24px;color:#5967d9;margin-bottom:16px;font-weight:900}.card p{font-size:15px;line-height:1.65;color:#222}.cta{display:inline-block;margin-top:6px;background:#b9d8ef;border-radius:28px;padding:18px 44px;box-shadow:0 8px 18px rgba(90,130,170,.25);font-size:17px;font-weight:900;color:#222}.feedback{position:relative;z-index:1;background:#f3f7fb;text-align:center;padding:38px 0 50px}.section-title{font-size:38px;font-weight:900;color:#333;margin-bottom:8px}.line{width:52px;height:4px;border-radius:3px;background:#dedede;margin:0 auto 46px}.fb-grid{width:1180px;margin:0 auto;display:grid;grid-template-columns:repeat(3,1fr);gap:62px}.fb-card{min-height:185px;background:#fff;border-radius:13px;box-shadow:0 10px 30px rgba(0,0,0,.08);padding:28px;text-align:left}.fb-top{display:flex;gap:18px;align-items:flex-start}.avatar{flex:none;width:47px;height:47px;border-radius:50%;background:#b9d8ef;color:#fff;font-size:20px;font-weight:900;display:grid;place-items:center}.fb-card p{font-size:15px;line-height:1.75;color:#333}.name{margin-top:24px;font-size:17px;font-weight:900}.major{font-size:14px;color:#666;margin-top:10px}.showcase{background:#f3f7fb;text-align:center;padding:62px 0 90px}.cases{width:1210px;margin:42px auto 0;display:grid;grid-template-columns:1fr 1fr;gap:64px;text-align:left}.case{background:#fff;border-radius:13px;box-shadow:0 10px 30px rgba(0,0,0,.07);padding:28px 28px 24px;min-height:335px}.case-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:26px}.case h3{font-size:25px;font-weight:900}.tag{font-size:15px;color:#5967d9;font-weight:800}.cols{display:grid;grid-template-columns:1fr 1fr;gap:38px}.col-title{font-size:16px;font-weight:900;margin-bottom:14px}.text-block{border-left:4px solid #6675ff;border-radius:4px;padding:14px 16px;min-height:118px;font-size:15px;line-height:1.78;color:#333}.text-block.after{color:#5967ff}.more{height:70px;background:#f7f9fd;border-radius:10px;margin-top:24px}@media(max-width:1200px){.cards{gap:40px}.fb-grid,.cases{width:92%;gap:24px}.topbar{padding:0 26px}.title{font-size:42px}}@media(max-width:900px){.cases,.cards,.fb-grid{grid-template-columns:1fr}.hero{min-height:auto;padding-bottom:60px}.cols{gap:18px}}
</style>
