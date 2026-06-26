<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchReviewDetail } from '@/api/feedback'
import type { FeedbackDetail } from '@/types/feedback'

const route = useRoute()
const loading = ref(false)
const detail = ref<FeedbackDetail | null>(null)

onMounted(async () => {
  const id = route.query.projectId ? Number(route.query.projectId) : undefined
  if (id) {
    loading.value = true
    try {
      detail.value = (await fetchReviewDetail(id)).data.data
    } finally {
      loading.value = false
    }
  }
})
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试反馈管理 / 复核结果详情页</div>
    <h1 class="title">查看复核结果</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="1" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="复核意见">{{ detail.reviewOpinion || '—' }}</el-descriptions-item>
        <el-descriptions-item label="试验结果">{{ detail.reportContent }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ detail.currentNode }}</el-descriptions-item>
      </el-descriptions>
    </div>
    <el-empty v-else description="请从待办或消息进入并携带 projectId" />
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
