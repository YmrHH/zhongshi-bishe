<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchEvaluationDetail, fetchEvaluationTodos, resourceEvaluation } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)
const requirement = ref('')
const remark = ref('')

onMounted(async () => {
  const todos = (await fetchEvaluationTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) detail.value = (await fetchEvaluationDetail(id)).data.data
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await resourceEvaluation(detail.value.projectId, { requirement: requirement.value, remark: remark.value })
    ElMessage.success('资源核定完成')
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
    <div class="crumb">首页 / 中试评估管理 / 资源核定页</div>
    <h1 class="title">资源核定页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-input v-model="requirement" placeholder="资源需求" style="margin:16px 0" />
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="核定意见" />
      <el-button type="primary" style="margin-top:16px" @click="submit">确认核定</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
