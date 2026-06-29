<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CenterPageHeader from '@/components/CenterPageHeader.vue'
import ListPagination from '@/components/ListPagination.vue'
import StatusTag from '@/components/StatusTag.vue'
import DemandPhaseStepBar from '@/components/DemandPhaseStepBar.vue'
import MaterialUpload from '@/components/MaterialUpload.vue'
import { acceptRegisterDemand, archiveDemand, fetchDemandPreview, fetchDemandTodos } from '@/api/demand'
import type { DemandDetail, DemandTodo } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const todos = ref<DemandTodo[]>([])
const todoTotal = ref(0)
const todoPage = ref(1)
const todoPageSize = ref(10)
const detail = ref<DemandDetail | null>(null)
const selectedId = ref<number | null>(null)
const registerRemark = ref('')

const isProcessing = computed(() => !!detail.value && selectedId.value !== null)

onMounted(async () => {
  await loadTodos()
  const projectId = route.query.projectId ? Number(route.query.projectId) : null
  if (projectId) {
    selectedId.value = projectId
    await loadDetail(projectId)
  }
})

async function loadTodos() {
  loading.value = true
  try {
    const res = await fetchDemandTodos({ page: todoPage.value, pageSize: todoPageSize.value })
    todos.value = res.data.data.records
    todoTotal.value = res.data.data.total
  } finally {
    loading.value = false
  }
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

async function loadDetail(projectId: number) {
  loading.value = true
  try {
    const res = await fetchDemandPreview(projectId)
    detail.value = res.data.data
  } finally {
    loading.value = false
  }
}

async function onSelect(row: DemandTodo) {
  selectedId.value = row.projectId
  router.replace({ query: { projectId: row.projectId } })
  await loadDetail(row.projectId)
}

function backToList() {
  selectedId.value = null
  detail.value = null
  registerRemark.value = ''
  router.replace({ path: '/center/dispatch/demand/workbench' })
}

async function onAcceptRegister() {
  if (!selectedId.value) return
  loading.value = true
  try {
    await acceptRegisterDemand(selectedId.value, registerRemark.value || '调度员受理登记')
    ElMessage.success('受理登记完成，已转审核员材料审核')
    await loadDetail(selectedId.value)
    await loadTodos()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}

function goReject() {
  if (!selectedId.value) return
  router.push({ path: '/center/dispatch/demand/reject', query: { projectId: selectedId.value } })
}

async function onConfirmArchive() {
  if (!selectedId.value) return
  loading.value = true
  try {
    await archiveDemand(selectedId.value)
    ElMessage.success('需求段已归档，项目进入评估阶段')
    await loadTodos()
    backToList()
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '归档失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <CenterPageHeader
      title="需求受理工作台"
      crumb="首页 / 中试需求管理 / 需求受理工作台"
      :show-back="isProcessing"
      @back="backToList"
    />
    <p v-if="!isProcessing" class="hint">受理登记前请核对企业信息、需求说明与附件材料清单（参照政务办件受理规范）。</p>

    <div v-if="!isProcessing" class="page-card">
      <h3>待办列表</h3>
      <el-table :data="todos" style="width:100%;margin-top:12px" @row-click="onSelect" highlight-current-row>
        <el-table-column prop="projectNo" label="项目编号" width="140" />
        <el-table-column prop="title" label="项目名称" />
        <el-table-column prop="currentNode" label="当前环节" width="140" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }">
            <StatusTag :label="row.statusLabel" :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="onSelect(row)">办理</el-button>
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

      <h4 class="section">申请概要</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.submittedAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="当前环节">{{ detail.currentNode }}</el-descriptions-item>
        <el-descriptions-item label="附件数量">{{ detail.materialCount ?? detail.materials?.length ?? 0 }} 件</el-descriptions-item>
      </el-descriptions>

      <h4 class="section">企业信息</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="企业名称">{{ detail.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone || '—' }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="section">需求信息</h4>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="中试类型">{{ detail.pilotType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="期望周期">{{ detail.expectedDays ? detail.expectedDays + ' 天' : '—' }}</el-descriptions-item>
        <el-descriptions-item label="需求说明" :span="2">{{ detail.content || '—' }}</el-descriptions-item>
      </el-descriptions>

      <h4 class="section">材料清单（只读）</h4>
      <MaterialUpload :model-value="detail.materials" readonly />

      <div v-if="detail.status === 'SUBMITTED'" class="actions">
        <el-input v-model="registerRemark" type="textarea" :rows="2" placeholder="受理登记备注（可选）" style="margin-bottom:12px" />
        <el-button type="primary" @click="onAcceptRegister">确认受理登记</el-button>
        <el-button type="warning" @click="goReject">退回企业补充</el-button>
      </div>
      <div v-else-if="detail.status === 'ACCEPTING'" class="actions">
        <el-alert type="info" :closable="false" title="已登记，等待审核员完成材料审核与受理决定" />
      </div>
      <div v-else-if="detail.status === 'RECEIPTED'" class="actions">
        <el-alert type="success" :closable="false" title="企业已签收回执，请确认后将需求段归档并进入评估阶段。" style="margin-bottom:12px" />
        <el-button type="primary" @click="onConfirmArchive">确认归档</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.hint { color: #909399; font-size: 14px; margin-bottom: 16px; }
.detail-head { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.section { margin: 20px 0 12px; font-size: 15px; color: #303133; }
.actions { margin-top: 20px; }
</style>
