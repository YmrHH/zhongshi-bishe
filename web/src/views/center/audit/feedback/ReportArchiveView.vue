<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchFeedbackDetail, fetchFeedbackTodos, reportArchiveFeedback } from '@/api/feedback'
import type { FeedbackDetail } from '@/types/feedback'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<FeedbackDetail | null>(null)
const remark = ref('')

onMounted(async () => {
  const todos = (await fetchFeedbackTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) detail.value = (await fetchFeedbackDetail(id)).data.data
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await reportArchiveFeedback(detail.value.projectId, { remark: remark.value })
    ElMessage.success('报告已归档')
    router.push('/center/audit/feedback/review-notice')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试反馈管理 / 报告归档页</div>
    <h1 class="title">中试报告归档</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="归档说明" style="margin:16px 0" />
      <el-button type="primary" @click="submit">确认归档</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
