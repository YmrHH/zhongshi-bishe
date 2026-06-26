<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchArchiveDetail, fetchArchiveTodos, fetchBrief } from '@/api/archive'
import type { ArchiveDetail, BriefDetail } from '@/types/archive'

const route = useRoute()
const loading = ref(false)
const detail = ref<ArchiveDetail | null>(null)
const brief = ref<BriefDetail | null>(null)

onMounted(async () => {
  const todos = (await fetchArchiveTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (!id) return
  loading.value = true
  try {
    detail.value = (await fetchArchiveDetail(id)).data.data
    if (detail.value.briefId) {
      brief.value = (await fetchBrief(detail.value.briefId)).data.data
    }
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试档案管理 / 简报查看页</div>
    <h1 class="title">查看服务简报</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <div v-if="brief" style="margin-top:16px">
        <h3>{{ brief.briefTitle }}</h3>
        <p style="line-height:1.8;color:#606266">{{ brief.content }}</p>
        <el-descriptions :column="2" border style="margin-top:16px">
          <el-descriptions-item label="项目编号">{{ brief.projectNo }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ brief.createdAt }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="简报尚未发布" />
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
