<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchDemandProgress } from '@/api/demand'
import type { DemandDetail } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DemandDetail | null>(null)

onMounted(async () => {
  const projectId = Number(route.query.projectId)
  if (!projectId) {
    router.replace('/enterprise/home')
    return
  }
  loading.value = true
  try {
    const res = await fetchDemandProgress(projectId)
    detail.value = res.data.data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试需求管理 / 受理进度详情页</div>
    <h1 class="title">受理进度详情页</h1>
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" />
    <ProjectStepBar v-if="detail?.steps?.length" :steps="detail.steps" />

    <div class="page-card">
      <h3>项目信息</h3>
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail?.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="当前环节">{{ detail?.currentNode }}</el-descriptions-item>
        <el-descriptions-item label="项目名称" :span="2">{{ detail?.title }}</el-descriptions-item>
        <el-descriptions-item label="需求内容" :span="2">{{ detail?.content }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="page-card">
      <h3>流程日志</h3>
      <el-timeline style="margin-top:16px">
        <el-timeline-item v-for="(log, idx) in detail?.logs || []" :key="idx" :timestamp="log.time">
          {{ log.remark }}（{{ log.fromStatus }} → {{ log.toStatus }}）
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
