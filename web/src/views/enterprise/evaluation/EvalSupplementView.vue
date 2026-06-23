<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import MaterialUpload from '@/components/MaterialUpload.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchEvaluationDetail, supplementEvaluation } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<EvaluationDetail | null>(null)
const materials = ref<EvaluationDetail['materials']>([])

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (!id) { router.replace('/enterprise/home'); return }
  detail.value = (await fetchEvaluationDetail(id)).data.data
  materials.value = [...(detail.value.materials || [])]
})

async function submit() {
  loading.value = true
  try {
    await supplementEvaluation(Number(route.query.projectId), { materials: materials.value })
    ElMessage.success('评估材料已补充')
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
    <div class="crumb">首页 / 中试评估管理 / 评估材料补充页</div>
    <h1 class="title">评估材料补充页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <MaterialUpload v-model="materials" />
      <el-button type="primary" style="margin-top:16px" @click="submit">重新提交</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
