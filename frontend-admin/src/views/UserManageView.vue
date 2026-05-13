<template>
  <el-card>
    <template #header><div class="card-head"><span>用户管理</span><div><el-input v-model="keyword" placeholder="用户名/手机号" clearable style="width:240px;margin-right:12px" @keyup.enter="loadUsers" /><el-button type="primary" @click="loadUsers">查询</el-button></div></div></template>
    <el-table :data="users" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="balance" label="余额(元)" width="120" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column prop="status" label="状态" width="120"><template #default="{ row }"><el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag></template></el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="190" />
      <el-table-column label="操作" width="250"><template #default="{ row }"><el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" @click="changeStatus(row)">{{ row.status === 1 ? '禁用' : '启用' }}</el-button><el-button size="small" @click="showDetail(row)">详情</el-button><el-button size="small" type="warning" @click="showRecharge(row)">充值</el-button><el-button size="small" @click="showRecords(row)">记录</el-button></template></el-table-column>
    </el-table>
    <div class="pager"><el-pagination background layout="prev, pager, next, total" :total="total" :page-size="pageSize" v-model:current-page="current" @current-change="loadUsers" /></div>
  </el-card>
  <el-dialog v-model="detailVisible" title="用户详情" width="420px"><el-descriptions :column="1" border v-if="detail"><el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item><el-descriptions-item label="用户名">{{ detail.username }}</el-descriptions-item><el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item><el-descriptions-item label="余额">{{ detail.balance ?? 0 }} 元</el-descriptions-item><el-descriptions-item label="角色">{{ detail.role }}</el-descriptions-item><el-descriptions-item label="状态">{{ detail.status === 1 ? '启用' : '禁用' }}</el-descriptions-item><el-descriptions-item label="注册时间">{{ detail.createTime }}</el-descriptions-item></el-descriptions></el-dialog>
  <el-dialog v-model="rechargeVisible" title="用户充值" width="420px">
    <el-form :model="rechargeForm" label-width="80px">
      <el-form-item label="用户">{{ rechargeUserTarget?.username }}</el-form-item>
      <el-form-item label="当前余额">{{ rechargeUserTarget?.balance ?? 0 }} 元</el-form-item>
      <el-form-item label="充值金额"><el-input-number v-model="rechargeForm.amount" :min="0.01" :precision="2" style="width:100%" /></el-form-item>
      <el-form-item label="备注"><el-input v-model="rechargeForm.remark" placeholder="充值备注" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="rechargeVisible = false">取消</el-button><el-button type="primary" :loading="recharging" @click="doRecharge">确认充值</el-button></template>
  </el-dialog>
  <el-dialog v-model="recordsVisible" title="充值记录" width="700px">
    <el-table :data="records" border>
      <el-table-column prop="amount" label="金额(元)" width="120" />
      <el-table-column prop="balanceBefore" label="充值前余额" width="120" />
      <el-table-column prop="balanceAfter" label="充值后余额" width="120" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="createTime" label="时间" width="190" />
    </el-table>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRechargeRecords, getUserDetail, getUsers, rechargeUser, updateUserStatus } from '@/api/admin'

const users = ref([])
const total = ref(0)
const current = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref(null)
const rechargeVisible = ref(false)
const rechargeUserTarget = ref(null)
const rechargeForm = reactive({ amount: 0, remark: '' })
const recharging = ref(false)
const recordsVisible = ref(false)
const records = ref([])

const loadUsers = async () => {
  loading.value = true
  try {
    const data = await getUsers({ current: current.value, size: pageSize.value, keyword: keyword.value })
    users.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}
const changeStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确定${nextStatus === 1 ? '启用' : '禁用'}用户 ${row.username} 吗？`, '提示')
  await updateUserStatus(row.id, nextStatus)
  ElMessage.success('操作成功')
  loadUsers()
}
const showDetail = async (row) => { detail.value = await getUserDetail(row.id); detailVisible.value = true }

const showRecharge = (row) => {
  rechargeUserTarget.value = row
  rechargeForm.amount = 0
  rechargeForm.remark = ''
  rechargeVisible.value = true
}
const doRecharge = async () => {
  if (!rechargeForm.amount || rechargeForm.amount <= 0) return ElMessage.warning('请输入充值金额')
  recharging.value = true
  try {
    await rechargeUser(rechargeUserTarget.value.id, { amount: rechargeForm.amount, remark: rechargeForm.remark })
    ElMessage.success('充值成功')
    rechargeVisible.value = false
    loadUsers()
  } finally {
    recharging.value = false
  }
}
const showRecords = async (row) => {
  records.value = await getRechargeRecords(row.id)
  recordsVisible.value = true
}

onMounted(loadUsers)
</script>

<style scoped>.card-head{display:flex;align-items:center;justify-content:space-between}.pager{display:flex;justify-content:flex-end;margin-top:18px}</style>
