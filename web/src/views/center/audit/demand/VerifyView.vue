<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import CenterPageHeader from '@/components/CenterPageHeader.vue'
import ListPagination from '@/components/ListPagination.vue'
import StatusTag from '@/components/StatusTag.vue'
import DemandPhaseStepBar from '@/components/DemandPhaseStepBar.vue'
import MaterialUpload from '@/components/MaterialUpload.vue'
import { fetchDemandPreview, fetchDemandTodos, verifyDemand } from '@/api/demand'
import type { DemandDetail, DemandTodo } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const todos = ref<DemandTodo[]>([])
const todoTotal = ref(0)
const todoPage = ref(1)
const todoPageSize = ref(10)
const detail = ref<DemandDetail | null>(null)
const opinion = ref('')

const materialChecklist = [
  '中试方案 / 可行性说明',
  '企业资质或营业执照复印件',
  '技术资料 / 产品说明',
  '其他补充材料'
]

const projectId = computed(() => (route.query.projectId ? Number(route.query.projectId) : null))
const isProcessing = computed(() => !!detail.value && projectId.value !== null)

onMounted(async () => {
  await loadTodos()
  if (projectId.value) await loadDetail(projectId.value)
})

async function loadTodos() {
  const res = await fetchDemandTodos({ page: todoPage.value, pageSize: todoPageSize.value })
  todos.value = res.data.data.records
  todoTotal.value = res.data.data.total
}

function onTodoPageChange(p: number) {
  todoPage.value = p
  loadTodos()
}

function onTodoPageSizeChange(s: number) {
  todoPageSize.value = s
  todoPage.value = 1
  loadTodos()
}

async function loadDetail(id: number) {
  loading.value = true
  try {
    router.replace({ query: { projectId: id } })
    const res = await fetchDemandPreview(id)
    detail.value = res.data.data
  } finally {
    loading.value = false
  }
}

function backToList() {
  detail.value = null
  opinion.value = ''
  router.replace({ path: '/center/audit/demand/verify' })
}

async function onIncomplete() {
  const id = projectId.value
  if (!id) return
  if (!opinion.value.trim()) {
    ElMessage.warning('请填写材料不齐说明')
    return
  }
  loading.value = true
  try {
    await verifyDemand(id, false, undefined, opinion.value)
    ElMessage.success('已退回企业补充材料')
    backToList()
    await loadTodos()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}

async function onAccept(accepted: boolean) {
  const id = projectId.value
  if (!id) return
  loading.value = true
  try {
    if (!accepted) {
      await ElMessageBox.confirm('确认不予受理？项目将终止。', '不予受理', { type: 'warning' })
    }
    await verifyDemand(id, true, accepted, opinion.value)
    ElMessage.success(accepted ? '材料审核通过，已同意受理' : '已录入不予受理决定')
    backToList()
    await loadTodos()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e instanceof Error ? e.message : '操作失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <CenterPageHeader
      title="受理材料审核页"
      crumb="首页 / 中试需求管理 / 受理材料审核页"
      :show-back="isProcessing"
      @back="backToList"
    />
    <p v-if="!isProcessing" class="hint">请下载并核对附件材料，在同一页面完成材料核验与是否同意受理决定。</p>

    <div v-if="!isProcessing" class="page-card">
      <h3>待审核项目</h3>
      <el-table :data="todos" style="margin-top:12px" @row-click="(row: DemandTodo) => loadDetail(row.projectId)">
        <el-table-column prop="projectNo" label="编号" width="140" />
        <el-table-column prop="title" label="名称" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }"><StatusTag :label="row.statusLabel" :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="loadDetail(row.projectId)">办理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <ListPagination
        v-if="todoTotal > 0"
        :total="todoTotal"
        :page="todoPage"
        :page-size="todoPageSize"
        @change="onTodoPageChange"
        @size-change="onTodoPageSizeChange"
      />
    </div>

    <div v-if="detail" class="page-card">
      <DemandPhaseStepBar v-if="detail.phaseSteps?.length" :steps="detail.phaseSteps" />
      <div class="detail-head">
        <h3>{{ detail.projectNo }} · {{ detail.title }}</h3>
        <StatusTag :label="detail.statusLabel" :status="detail.status" />
      </div>

      <el-row :gutter="24">
        <el-col :span="14">
          <h4 class="section">申请信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="企业">{{ detail.enterpriseName }}</el-descriptions-item>
            <el-descriptions-item label="联系人">{{ detail.contactName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detail.contactPhone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="中试类型">{{ detail.pilotType || '—' }}</el-descriptions-item>
            <el-descriptions-item label="期望周期">{{ detail.expectedDays ? detail.expectedDays + ' 天' : '—' }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detail.submittedAt || '—' }}</el-descriptions-item>
            <el-descriptions-item label="需求说明" :span="2">{{ detail.content || '—' }}</el-descriptions-item>
          </el-descriptions>

          <h4 class="section">附件材料（请下载核对）</h4>
          <MaterialUpload :model-value="detail.materials" readonly />
        </el-col>
        <el-col :span="10">
          <h4 class="section">材料要件参考</h4>
          <ul class="checklist">
            <li v-for="item in materialChecklist" :key="item">{{ item }}</li>
          </ul>
          <el-alert type="info" :closable="false" title="材料不齐可退回企业补充；材料齐全请当场作出受理决定。" style="margin-top:16px" />
        </el-col>
      </el-row>

      <h4 class="section">审核意见</h4>
      <el-input v-model="opinion" type="textarea" :rows="4" placeholder="核验/受理意见（退回或不予受理时必填）" />

      <div class="actions">
        <el-button type="warning" @click="onIncomplete">材料不齐，退回补充</el-button>
        <el-button type="primary" @click="onAccept(true)">材料齐全，同意受理</el-button>
        <el-button type="danger" @click="onAccept(false)">材料齐全，不同意受理</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hint { color: #909399; font-size: 14px; margin-bottom: 16px; }
.detail-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.section { margin: 16px 0 10px; font-size: 15px; color: #303133; }
.checklist { margin: 0; padding-left: 20px; color: #606266; line-height: 1.8; }
.actions { margin-top: 20px; display: flex; gap: 12px; flex-wrap: wrap; }
</style>
