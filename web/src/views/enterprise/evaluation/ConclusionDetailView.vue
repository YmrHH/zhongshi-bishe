<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchEvaluationDetail } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'

const route = useRoute()
const router = useRouter()
const detail = ref<EvaluationDetail | null>(null)

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (!id) { router.replace('/enterprise/home'); return }
  detail.value = (await fetchEvaluationDetail(id)).data.data
})
</script>

<template>
  <div>
    <div class="crumb">首页 / 中试评估管理 / 评估结论详情页</div>
    <h1 class="title">评估结论详情页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="1" border style="margin-top:16px">
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="评估结论">{{ detail.conclusion }}</el-descriptions-item>
        <el-descriptions-item label="结论意见">{{ detail.conclusionOpinion }}</el-descriptions-item>
        <el-descriptions-item label="反馈意见">{{ detail.feedbackContent || '—' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
