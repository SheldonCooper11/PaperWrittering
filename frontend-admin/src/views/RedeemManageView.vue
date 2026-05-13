<template>
  <el-card>
    <template #header>
      <div class="card-head">
        <span>卡密管理</span>
        <div>
          <el-input v-model="keyword" placeholder="搜索卡密/批次号" clearable style="width:220px;margin-right:12px" @keyup.enter="loadCodes" />
          <el-button type="primary" @click="loadCodes">查询</el-button>
          <el-button type="success" @click="showGenerate">生成卡密</el-button>
        </div>
      </div>
    </template>
    <el-table :data="codes" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="卡密" min-width="180" show-overflow-tooltip />
      <el-table-column prop="amount" label="面额(元)" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'info'">{{ row.status === 0 ? '未使用' : '已使用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="usedBy" label="使用者ID" width="100" />
      <el-table-column prop="usedAt" label="使用时间" width="170" />
      <el-table-column prop="batchNo" label="批次号" width="160" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
    </el-table>
    <div class="pager">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="current" @current-change="loadCodes" />
    </div>
  </el-card>
  <el-dialog v-model="genVisible" title="生成卡密" width="420px">
    <el-form label-width="80px">
      <el-form-item label="面额(元)"><el-input-number v-model="genForm.amount" :min="0.01" :precision="2" style="width:100%" /></el-form-item>
      <el-form-item label="数量"><el-input-number v-model="genForm.count" :min="1" :max="500" style="width:100%" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="genVisible = false">取消</el-button>
      <el-button type="primary" :loading="generating" @click="doGenerate">生成</el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="resultVisible" title="生成结果" width="500px">
    <p style="color:#666;margin-bottom:12px">以下卡密仅显示一次，请复制保存：</p>
    <div class="code-list">
      <div v-for="(c, i) in genResult" :key="i" class="code-item">
        <span>{{ c.code }}</span>
        <span class="amount">{{ c.amount }} 元</span>
      </div>
    </div>
    <template #footer><el-button type="primary" @click="resultVisible = false; loadCodes()">已复制，关闭</el-button></template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateRedeemCodes, getRedeemCodes } from '@/api/admin'

const codes = ref([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(false)

const loadCodes = async () => {
  loading.value = true
  try {
    const data = await getRedeemCodes({ current: current.value, size: pageSize.value, keyword: keyword.value })
    codes.value = data.records || []
    total.value = data.total || 0
  } finally { loading.value = false }
}

const genVisible = ref(false)
const genForm = reactive({ amount: 0, count: 10 })
const generating = ref(false)

const showGenerate = () => {
  genForm.amount = 0
  genForm.count = 10
  genVisible.value = true
}

const resultVisible = ref(false)
const genResult = ref([])

const doGenerate = async () => {
  if (!genForm.amount || genForm.amount <= 0) return ElMessage.warning('请输入面额')
  if (!genForm.count || genForm.count <= 0) return ElMessage.warning('请输入数量')
  generating.value = true
  try {
    genResult.value = await generateRedeemCodes({ amount: genForm.amount, count: genForm.count })
    genVisible.value = false
    resultVisible.value = true
  } finally { generating.value = false }
}

onMounted(loadCodes)
</script>

<style scoped>
.card-head{display:flex;align-items:center;justify-content:space-between}.pager{display:flex;justify-content:flex-end;margin-top:18px}
.code-list{max-height:400px;overflow-y:auto}.code-item{display:flex;justify-content:space-between;padding:8px 12px;background:#f5f7fb;border-radius:6px;margin-bottom:6px;font-family:monospace;font-size:13px}.code-item .amount{color:#2c3e8f;font-weight:700}
</style>
