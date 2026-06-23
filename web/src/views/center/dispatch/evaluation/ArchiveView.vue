<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { archiveEvaluation, fetchEvaluationDetail } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (!id) { router.replace('/center/home'); return }
  detail.value = (await fetchEvaluationDetail(id)).data.data
})

async function submit() {
  loading.value = true
  try {
    await archiveEvaluation(Number(route.query.projectId))
    ElMessage.success('评估段已归档，进入调度阶段')
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
    <div class="crumb">首页 / 中试评估管理 / 评估归档页</div>
    <h1 class="title">评估归档页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-button type="primary" style="margin-top:16px" @click="submit">确认归档</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
