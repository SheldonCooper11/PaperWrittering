<template>
  <div class="announce-page">
    <div class="page-header">
      <h2>公告管理</h2>
      <el-button type="primary" @click="openCreate">发布公告</el-button>
    </div>
    <el-table :data="list" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑公告' : '发布公告'" width="600px" @closed="resetForm">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="公告内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doSave">{{ isEdit ? '保存' : '发布' }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement } from '@/api/admin'

const list = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const form = reactive({ title: '', content: '', status: 1 })

const fetchList = async () => {
  loading.value = true
  try { list.value = await getAnnouncements() } finally { loading.value = false }
}

const resetForm = () => {
  form.title = ''
  form.content = ''
  form.status = 1
  isEdit.value = false
  editId.value = null
}

const openCreate = () => { resetForm(); dialogVisible.value = true }
const openEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  form.title = row.title
  form.content = row.content
  form.status = row.status
  dialogVisible.value = true
}

const doSave = async () => {
  if (!form.title.trim()) return ElMessage.warning('请输入标题')
  if (!form.content.trim()) return ElMessage.warning('请输入内容')
  saving.value = true
  try {
    if (isEdit.value) {
      await updateAnnouncement(editId.value, { ...form })
      ElMessage.success('保存成功')
    } else {
      await createAnnouncement({ ...form })
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    await fetchList()
  } finally { saving.value = false }
}

const doDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
  await deleteAnnouncement(row.id)
  ElMessage.success('删除成功')
  await fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.announce-page { padding: 0 }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 18px }
.page-header h2 { font-size: 18px; color: #333 }
</style>
