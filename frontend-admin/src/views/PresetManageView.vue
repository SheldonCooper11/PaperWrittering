<template>
  <div class="preset-page">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane label="平台预设配置" name="mapping">
        <div class="module-section" v-for="mod in configModules" :key="mod.code">
          <div class="module-head">{{ mod.name }}</div>
          <el-tabs v-model="langTabs[mod.code]" type="card" class="lang-tabs">
            <el-tab-pane v-for="lang in languages" :key="lang.value" :label="lang.label" :name="lang.value">
              <div class="platform-rows">
                <div class="plat-row" v-for="plat in platforms" :key="plat">
                  <span class="plat-name">{{ plat }}</span>
                  <div class="plat-presets">
                    <el-tag
                      v-for="pp in getMappings(plat, mod.code, lang.value)"
                      :key="pp.id"
                      closable size="small"
                      @close="removeMapping(pp)"
                    >{{ getPresetName(pp.presetCode) }}</el-tag>
                    <span class="empty-hint" v-if="getMappings(plat, mod.code, lang.value).length === 0">暂无</span>
                  </div>
                  <el-button size="small" @click="openEditMapping(plat, mod, lang.value)">编辑</el-button>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>

      <el-tab-pane label="预设库管理" name="presets">
        <div class="toolbar"><span>共 {{ presets.length }} 条预设</span><el-button type="primary" @click="openCreate">新增预设</el-button></div>
        <el-table :data="presets" stripe border size="small">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="code" label="代码" width="220" />
          <el-table-column prop="name" label="名称" min-width="260" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="80" />
          <el-table-column prop="price" label="单价" width="70" />
          <el-table-column prop="status" label="状态" width="70">
            <template #default="{ row }">
              <el-switch :model-value="row.status === 1" size="small" @change="togglePreset(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button size="small" @click="openEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- Preset edit dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑预设' : '新增预设'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="代码"><el-input v-model="form.code" :disabled="isEdit" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%"><el-option v-for="c in ['降AI','降重','双降']" :key="c" :label="c" :value="c" /></el-select>
        </el-form-item>
        <el-form-item label="单价"><el-input-number v-model="form.price" :precision="4" :step="0.01" style="width:100%" /></el-form-item>
        <el-form-item label="状态"><el-switch :model-value="form.status === 1" @change="form.status = $event ? 1 : 0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="handleSave">保存</el-button></template>
    </el-dialog>

    <!-- Edit module-language-platform presets dialog -->
    <el-dialog v-model="editDialogVisible" :title="`${editModule?.name} — ${editLanguage === 'chinese' ? '中文' : '英文'} — ${editPlatform}`" width="650px">
      <div class="edit-body">
        <div class="edit-left">
          <h4>可选预设</h4>
          <el-input v-model="searchKey" placeholder="搜索预设" size="small" clearable class="search-input" />
          <div class="preset-list">
            <div
              v-for="p in filteredAvailable"
              :key="p.code"
              class="preset-item"
              @click="addToMapping(p.code)"
            >{{ p.name }} <span class="code">{{ p.code }}</span></div>
            <div v-if="filteredAvailable.length === 0" class="none-hint">无可添加的预设</div>
          </div>
        </div>
        <div class="edit-right">
          <h4>已配置预设 ({{ currentMappings.length }})</h4>
          <div class="preset-list">
            <div
              v-for="(pp, idx) in currentMappings"
              :key="pp.id"
              class="preset-item selected"
            >
              <span class="order">{{ idx + 1 }}</span>
              {{ getPresetName(pp.presetCode) }} <span class="code">{{ pp.presetCode }}</span>
              <el-button size="small" type="danger" text @click="removeMapping(pp)">×</el-button>
            </div>
            <div v-if="currentMappings.length === 0" class="none-hint">暂未配置预设</div>
          </div>
        </div>
      </div>
      <template #footer><el-button @click="editDialogVisible = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPresets, updatePreset, createPreset, deletePreset, getPlatformPresets, createPlatformPreset, deletePlatformPreset } from '@/api/admin'

const activeTab = ref('mapping')
const platforms = ['知网','维普','格子达','PaperYY','笔杆网','万方','PaperPass','华宸','paperred','writepass','papered','大雅','朱雀']
const languages = [
  { value: 'chinese', label: '中文' },
  { value: 'english', label: 'English' },
]
const configModules = [
  { code: 'repeat_reduce', name: '降重复率' },
  { code: 'ai_reduce', name: '降AI率' },
  { code: 'dual_reduce', name: '降重+降AIGC' },
]

const langTabs = reactive({ repeat_reduce: 'chinese', ai_reduce: 'chinese', dual_reduce: 'chinese' })

const presets = ref([])
const mappings = ref([])

// preset CRUD
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ code: '', name: '', category: '降AI', price: 0.44, status: 1 })
let editingId = null

// edit mapping dialog
const editDialogVisible = ref(false)
const editPlatform = ref('')
const editModule = ref(null)
const editLanguage = ref('chinese')
const searchKey = ref('')

const getMappings = (plat, modCode, lang) => mappings.value.filter(m => m.platform === plat && m.moduleCode === modCode && m.language === lang)
const getPresetName = (code) => presets.value.find(p => p.code === code)?.name || code

const currentMappings = computed(() => {
  if (!editModule.value) return []
  return mappings.value.filter(m => m.platform === editPlatform.value && m.moduleCode === editModule.value.code && m.language === editLanguage.value)
})

const filteredAvailable = computed(() => {
  if (!editModule.value) return []
  const usedCodes = currentMappings.value.map(m => m.presetCode)
  let list = presets.value.filter(p => p.status === 1 && !usedCodes.includes(p.code))
  if (searchKey.value) {
    const kw = searchKey.value.toLowerCase()
    list = list.filter(p => p.name.includes(kw) || p.code.toLowerCase().includes(kw))
  }
  return list
})

const loadAll = async () => {
  const [pr, pp] = await Promise.all([getPresets(), getPlatformPresets()])
  presets.value = pr || []
  mappings.value = pp || []
}

const togglePreset = async (row) => {
  await updatePreset(row.id, { ...row, status: row.status === 1 ? 0 : 1 })
  row.status = row.status === 1 ? 0 : 1
}

const openCreate = () => {
  isEdit.value = false; editingId = null
  form.value = { code: '', name: '', category: '降AI', price: 0.44, status: 1 }
  dialogVisible.value = true
}
const openEdit = (row) => {
  isEdit.value = true; editingId = row.id
  form.value = { ...row }
  dialogVisible.value = true
}
const handleSave = async () => {
  if (!form.value.code || !form.value.name) return ElMessage.warning('代码和名称不能为空')
  if (isEdit.value) { await updatePreset(editingId, form.value) } else { await createPreset(form.value) }
  dialogVisible.value = false
  loadAll()
  ElMessage.success(isEdit.value ? '已更新' : '已创建')
}
const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定删除「${row.name}」？`, '确认', { type: 'warning' })
  await deletePreset(row.id)
  loadAll()
  ElMessage.success('已删除')
}

const openEditMapping = (plat, mod, lang) => {
  editPlatform.value = plat
  editModule.value = mod
  editLanguage.value = lang
  searchKey.value = ''
  editDialogVisible.value = true
}

const addToMapping = async (presetCode) => {
  const maxSort = currentMappings.value.reduce((max, m) => Math.max(max, m.sortOrder), 0)
  await createPlatformPreset({
    platform: editPlatform.value,
    moduleCode: editModule.value.code,
    moduleName: editModule.value.name,
    language: editLanguage.value,
    presetCode,
    sortOrder: maxSort + 1,
    status: 1,
  })
  loadAll()
}

const removeMapping = async (row) => {
  await deletePlatformPreset(row.id)
  loadAll()
}

onMounted(loadAll)
</script>

<style scoped>
.preset-page{padding:0}
.toolbar{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px}

.module-section{margin-bottom:24px;border:1px solid #e5e5e5;border-radius:8px;overflow:hidden}
.module-head{background:#2c3e8f;color:#fff;padding:12px 18px;font-size:17px;font-weight:700}
.lang-tabs{margin:0;padding:0}
.lang-tabs :deep(.el-tabs__header){margin:0;padding:0 12px;background:#f5f7fb}
.lang-tabs :deep(.el-tabs__nav-wrap::after){display:none}
.platform-rows{padding:6px 0}
.plat-row{display:flex;align-items:center;gap:12px;padding:10px 18px;border-bottom:1px solid #f2f2f2}
.plat-row:last-child{border-bottom:none}
.plat-name{font-weight:700;font-size:14px;min-width:70px;color:#333}
.plat-presets{flex:1;display:flex;flex-wrap:wrap;gap:4px;align-items:center}
.empty-hint{color:#bbb;font-size:13px}

.edit-body{display:grid;grid-template-columns:1fr 1fr;gap:20px;max-height:420px}
.edit-body h4{font-size:15px;margin-bottom:10px;color:#333}
.search-input{margin-bottom:10px}
.preset-list{max-height:320px;overflow-y:auto;border:1px solid #eee;border-radius:4px}
.preset-item{padding:8px 12px;border-bottom:1px solid #f5f5f5;cursor:pointer;font-size:14px;display:flex;align-items:center;gap:8px}
.preset-item:hover{background:#eef2ff}
.preset-item .code{font-size:11px;color:#999;margin-left:auto}
.preset-item.selected{background:#f0f4ff;cursor:default}
.preset-item.selected:hover{background:#f0f4ff}
.preset-item .order{font-size:12px;color:#999;min-width:20px}
.none-hint{color:#bbb;font-size:13px;padding:20px;text-align:center}
</style>
