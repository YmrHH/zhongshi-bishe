<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchFeedbackDetail, fetchFeedbackTodos, validateFeedback } from '@/api/feedback'
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

async function submit(passed: boolean) {
  if (!detail.value) return
  loading.value = true
  try {
    await validateFeedback(detail.value.projectId, { passed, remark: remark.value })
    ElMessage.success(passed ? '数据校验通过' : '已退回修改')
    router.push('/technician/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试反馈管理 / 数据校验页</div>
    <h1 class="title">试验数据校验</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="1" border style="margin:16px 0">
        <el-descriptions-item label="试验结果">{{ detail.reportContent }}</el-descriptions-item>
        <el-descriptions-item label="附件">{{ detail.fileName || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="校验意见" style="margin-bottom:16px" />
      <el-button type="primary" @click="submit(true)">校验通过</el-button>
      <el-button type="warning" @click="submit(false)">退回修改</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
