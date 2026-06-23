<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { conclusionEvaluation, fetchEvaluationDetail, fetchEvaluationTodos } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)
const conclusion = ref('')
const opinion = ref('')

onMounted(async () => {
  const todos = (await fetchEvaluationTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) detail.value = (await fetchEvaluationDetail(id)).data.data
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await conclusionEvaluation(detail.value.projectId, { conclusion: conclusion.value, opinion: opinion.value })
    ElMessage.success('评估结论已出具')
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
    <div class="crumb">首页 / 中试评估管理 / 评估结论页</div>
    <h1 class="title">评估结论页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <el-input v-model="conclusion" type="textarea" :rows="4" placeholder="评估结论" style="margin:16px 0" />
      <el-input v-model="opinion" type="textarea" :rows="2" placeholder="补充意见" />
      <el-button type="primary" style="margin-top:16px" @click="submit">出具结论</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
