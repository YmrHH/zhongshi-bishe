<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { acceptRegisterDemand, fetchDemandPreview, fetchDemandTodos } from '@/api/demand'
import type { DemandDetail, DemandTodo } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const todos = ref<DemandTodo[]>([])
const detail = ref<DemandDetail | null>(null)
const selectedId = ref<number | null>(null)

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
    const res = await fetchDemandTodos()
    todos.value = res.data.data
  } finally {
    loading.value = false
  }
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

async function onAcceptRegister() {
  if (!selectedId.value) return
  loading.value = true
  try {
    await acceptRegisterDemand(selectedId.value, '调度员受理登记')
    ElMessage.success('受理登记完成')
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
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试需求管理 / 需求受理工作台</div>
    <h1 class="title">需求受理工作台</h1>

    <div class="page-card">
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
        <el-table-column prop="updatedAt" label="时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="onSelect(row)">办理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="detail" class="page-card">
      <h3>当前项目：{{ detail.projectNo }}</h3>
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="企业">{{ detail.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="需求内容" :span="2">{{ detail.content }}</el-descriptions-item>
      </el-descriptions>
      <el-button v-if="detail.status === 'SUBMITTED'" type="primary" @click="onAcceptRegister">受理登记</el-button>
      <el-button v-if="detail.status === 'ACCEPTING' || detail.status === 'VERIFYING'" type="warning" @click="goReject">退回</el-button>
      <el-button v-if="detail.status === 'RECEIPTED'" type="success" @click="router.push({ path: '/center/dispatch/demand/archive', query: { projectId: selectedId } })">去归档</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
