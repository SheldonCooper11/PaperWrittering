<template>
  <el-card>
    <template #header><span>系统配置</span></template>
    <el-table :data="configs" border v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="configKey" label="配置键" />
      <el-table-column prop="configValue" label="当前值" />
      <el-table-column prop="description" label="说明" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="showEdit(row)">修改</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
  <el-dialog v-model="editVisible" title="修改配置" width="400px">
    <el-form label-width="80px">
      <el-form-item label="配置键">{{ editingRow?.configKey }}</el-form-item>
      <el-form-item label="说明">{{ editingRow?.description }}</el-form-item>
      <el-form-item label="新值">
        <el-input v-model="editValue" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="doSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfigs, updateConfig } from '@/api/admin'

const configs = ref([])
const loading = ref(false)
const editVisible = ref(false)
const editingRow = ref(null)
const editValue = ref('')
const saving = ref(false)

const loadConfigs = async () => {
  loading.value = true
  try { configs.value = await getConfigs() } finally { loading.value = false }
}

const showEdit = (row) => {
  editingRow.value = row
  editValue.value = row.configValue
  editVisible.value = true
}

const doSave = async () => {
  saving.value = true
  try {
    await updateConfig(editingRow.value.id, editValue.value)
    ElMessage.success('保存成功')
    editVisible.value = false
    loadConfigs()
  } finally { saving.value = false }
}

onMounted(loadConfigs)
</script>
