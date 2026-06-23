<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchEvaluationDetail, receiptEvaluation } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (!id) { router.replace('/enterprise/home'); return }
  detail.value = (await fetchEvaluationDetail(id)).data.data
})

async function submit() {
  loading.value = true
  try {
    await receiptEvaluation(Number(route.query.projectId))
    ElMessage.success('评估结论已签收')
    router.push({ path: '/enterprise/evaluation/feedback', query: { projectId: route.query.projectId } })
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试评估管理 / 评估结论签收页</div>
    <h1 class="title">评估结论签收页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <p style="margin:16px 0">{{ detail.conclusion }}</p>
      <el-button type="primary" @click="submit">确认签收</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
