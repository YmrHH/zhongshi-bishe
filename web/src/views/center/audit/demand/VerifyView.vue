<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import MaterialUpload from '@/components/MaterialUpload.vue'
import { fetchDemandPreview, fetchDemandTodos, verifyDemand } from '@/api/demand'
import type { DemandDetail, DemandTodo } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const todos = ref<DemandTodo[]>([])
const detail = ref<DemandDetail | null>(null)
const opinion = ref('')

onMounted(async () => {
  const res = await fetchDemandTodos()
  todos.value = res.data.data
  const projectId = route.query.projectId ? Number(route.query.projectId) : todos.value[0]?.projectId
  if (projectId) await loadDetail(projectId)
})

async function loadDetail(projectId: number) {
  loading.value = true
  try {
    router.replace({ query: { projectId } })
    const res = await fetchDemandPreview(projectId)
    detail.value = res.data.data
  } finally {
    loading.value = false
  }
}

async function onVerify(complete: boolean) {
  const projectId = Number(route.query.projectId)
  loading.value = true
  try {
    await verifyDemand(projectId, complete, opinion.value)
    ElMessage.success(complete ? '材料核验通过' : '已退回企业补充')
    router.push('/center/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试需求管理 / 材料核验页</div>
    <h1 class="title">材料核验页</h1>

    <div class="page-card">
      <h3>待核验项目</h3>
      <el-table :data="todos" style="margin-top:12px" @row-click="(row: DemandTodo) => loadDetail(row.projectId)">
        <el-table-column prop="projectNo" label="编号" width="140" />
        <el-table-column prop="title" label="名称" />
        <el-table-column label="状态" width="140">
          <template #default="{ row }"><StatusTag :label="row.statusLabel" :status="row.status" /></template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="detail" class="page-card">
      <h3>{{ detail.projectNo }} 材料核验</h3>
      <MaterialUpload :model-value="detail.materials" readonly />
      <el-input v-model="opinion" type="textarea" :rows="3" placeholder="核验意见" style="margin:16px 0" />
      <el-button type="primary" @click="onVerify(true)">材料齐全</el-button>
      <el-button type="warning" @click="onVerify(false)">材料不齐，退回</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
