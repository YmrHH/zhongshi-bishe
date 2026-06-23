<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { feasibilityEvaluation, fetchEvaluationDetail, fetchEvaluationTodos } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)
const remark = ref('')

onMounted(async () => {
  const todos = (await fetchEvaluationTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) detail.value = (await fetchEvaluationDetail(id)).data.data
})

async function submit(passed: boolean) {
  if (!detail.value) return
  loading.value = true
  try {
    await feasibilityEvaluation(detail.value.projectId, { passed, remark: remark.value })
    ElMessage.success(passed ? '可行性审查通过' : '已退回补充材料')
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
    <div class="crumb">首页 / 中试评估管理 / 可行性审查页</div>
    <h1 class="title">可行性审查页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="审查意见" style="margin:16px 0" />
      <el-button type="primary" @click="submit(true)">技术可行</el-button>
      <el-button type="warning" @click="submit(false)">不可行/需补充</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
