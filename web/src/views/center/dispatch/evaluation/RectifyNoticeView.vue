<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { fetchEvaluationDetail, rectifyNoticeEvaluation } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)
const notice = ref('')

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (!id) { router.replace('/center/dispatch/evaluation/condition'); return }
  detail.value = (await fetchEvaluationDetail(id)).data.data
})

async function submit() {
  loading.value = true
  try {
    await rectifyNoticeEvaluation(Number(route.query.projectId), { notice: notice.value })
    ElMessage.success('整改通知已发送')
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
    <div class="crumb">首页 / 中试评估管理 / 条件整改通知页</div>
    <h1 class="title">条件整改通知页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <el-input v-model="notice" type="textarea" :rows="4" placeholder="整改要求" style="margin:16px 0" />
      <el-button type="primary" @click="submit">发送通知</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
