<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import EvaluationSummaryPanel from '@/components/EvaluationSummaryPanel.vue'
import { fetchDispatchDetail, fetchDispatchTodos, matchDispatch } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'
import type { DemandTodo } from '@/types/demand'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const todos = ref<DemandTodo[]>([])
const detail = ref<DispatchDetail | null>(null)
const remark = ref('')

async function load(projectId: number) {
  router.replace({ query: { projectId } })
  try {
    detail.value = (await fetchDispatchDetail(projectId)).data.data
  } catch (e) {
    detail.value = null
    ElMessage.error(e instanceof Error ? e.message : '加载项目失败')
  }
}

onMounted(async () => {
  await userStore.loadProfile()
  if (userStore.profile?.role !== 'DISPATCHER') {
    ElMessage.warning('请使用调度员账号 dispatcher 登录后再操作资源匹配')
    router.replace('/center/home')
    return
  }
  todos.value = (await fetchDispatchTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos.value[0]?.projectId
  if (id) await load(id)
})

async function submit(passed: boolean) {
  if (!detail.value) return
  await userStore.loadProfile()
  if (userStore.profile?.role !== 'DISPATCHER') {
    ElMessage.error('当前登录账号不是中试调度员，请退出后使用 dispatcher / 123456 重新登录')
    return
  }
  loading.value = true
  try {
    await matchDispatch(detail.value.projectId, { passed, remark: remark.value })
    ElMessage.success(passed ? '资源匹配成功' : '已标记匹配失败')
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
    <div class="crumb">首页 / 中试调度管理 / 中试资源匹配页</div>
    <h1 class="title">中试资源匹配页</h1>
    <el-tag type="warning">当前账号：{{ userStore.profile?.roleLabel || '未登录' }}</el-tag>
    <div class="page-card">
      <el-table :data="todos" @row-click="(r: DemandTodo) => load(r.projectId)">
        <el-table-column prop="projectNo" label="编号" width="140" />
        <el-table-column prop="title" label="名称" />
        <el-table-column label="状态" width="140"><template #default="{ row }"><StatusTag :label="row.statusLabel" :status="row.status" /></template></el-table-column>
      </el-table>
      <p v-if="!todos.length" class="hint">暂无调度待办。请先在「中试评估管理 → 评估归档页」完成评估归档，项目才会进入本列表。</p>
    </div>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <EvaluationSummaryPanel :summary="detail.evaluationSummary" />
      <h3 style="margin:16px 0">可用资源</h3>
      <el-table :data="detail.resources || []" size="small">
        <el-table-column prop="name" label="资源名称" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="capacity" label="产能" width="140" />
      </el-table>
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="匹配意见" style="margin:16px 0" />
      <el-button type="primary" @click="submit(true)">匹配成功</el-button>
      <el-button @click="submit(false)">暂无合适资源</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
.hint { color: #909399; margin-top: 12px; font-size: 13px; }
</style>
