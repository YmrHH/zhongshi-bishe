<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { conditionEvaluation, fetchEvaluationDetail, fetchEvaluationTodos } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'
import type { DemandTodo } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const todos = ref<DemandTodo[]>([])
const detail = ref<EvaluationDetail | null>(null)
const remark = ref('')

async function load(projectId: number) {
  router.replace({ query: { projectId } })
  detail.value = (await fetchEvaluationDetail(projectId)).data.data
}

onMounted(async () => {
  todos.value = (await fetchEvaluationTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos.value[0]?.projectId
  if (id) await load(id)
})

async function submit(passed: boolean) {
  if (!detail.value) return
  loading.value = true
  try {
    await conditionEvaluation(detail.value.projectId, { passed, remark: remark.value })
    ElMessage.success(passed ? '条件评估通过' : '已发起条件整改')
    router.push(passed
      ? { path: '/center/dispatch/evaluation/resource', query: { projectId: detail.value.projectId } }
      : { path: '/center/dispatch/evaluation/rectify-notice', query: { projectId: detail.value.projectId } })
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试评估管理 / 条件评估页</div>
    <h1 class="title">条件评估页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="评估意见" style="margin:16px 0" />
      <el-button type="primary" @click="submit(true)">具备条件</el-button>
      <el-button type="warning" @click="submit(false)">不具备条件</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
