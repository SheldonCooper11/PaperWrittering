<template>
  <div class="platform-page">
    <div class="toolbar">
      <span>共 {{ list.length }} 个平台</span>
      <el-button type="primary" @click="openCreate">新增平台</el-button>
    </div>
    <el-table :data="list" stripe border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="平台名称" min-width="200" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" size="small" @change="toggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑平台' : '新增平台'" width="450px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="平台名称">
          <el-input v-model="form.name" placeholder="例如：知网" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch :model-value="form.status === 1" @change="form.status = $event ? 1 : 0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doSave">{{ isEdit ? '保存' : '新增' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPlatforms, createPlatform, updatePlatform, deletePlatform } from '@/api/admin'

const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
let editId = null
const form = reactive({ name: '', sortOrder: 0, status: 1 })

const load = async () => {
  const res = await getPlatforms()
  list.value = res || []
}

const resetForm = () => {
  form.name = ''
  form.sortOrder = 0
  form.status = 1
  isEdit.value = false
  editId = null
}

const openCreate = () => { resetForm(); dialogVisible.value = true }
const openEdit = (row) => {
  isEdit.value = true
  editId = row.id
  form.name = row.name
  form.sortOrder = row.sortOrder
  form.status = row.status
  dialogVisible.value = true
}

const doSave = async () => {
  if (!form.name.trim()) return ElMessage.warning('请输入平台名称')
  saving.value = true
  try {
    if (isEdit.value) {
      await updatePlatform(editId, { ...form })
      ElMessage.success('已保存')
    } else {
      await createPlatform({ ...form })
      ElMessage.success('已新增')
    }
    dialogVisible.value = false
    await load()
  } finally { saving.value = false }
}

const doDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除「${row.name}」？`, '确认', { type: 'warning' })
  await deletePlatform(row.id)
  await load()
  ElMessage.success('已删除')
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  await updatePlatform(row.id, { ...row, status: newStatus })
  row.status = newStatus
}

onMounted(load)
</script>

<style scoped>
.platform-page { padding: 0 }
.toolbar { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px }
</style>
