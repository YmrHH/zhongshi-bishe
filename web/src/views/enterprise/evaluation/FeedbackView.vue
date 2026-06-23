<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { feedbackEvaluation, fetchEvaluationDetail } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)
const content = ref('')

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (!id) { router.replace('/enterprise/home'); return }
  detail.value = (await fetchEvaluationDetail(id)).data.data
})

async function submit() {
  loading.value = true
  try {
    await feedbackEvaluation(Number(route.query.projectId), { content: content.value })
    ElMessage.success('意见已反馈')
    router.push('/enterprise/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试评估管理 / 评估意见反馈页</div>
    <h1 class="title">评估意见反馈页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <el-input v-model="content" type="textarea" :rows="4" placeholder="反馈意见（可选）" style="margin:16px 0" />
      <el-button type="primary" @click="submit">提交反馈</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
