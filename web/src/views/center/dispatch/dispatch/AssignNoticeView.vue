<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { assignNoticeDispatch, fetchDispatchDetail } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DispatchDetail | null>(null)

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (id) detail.value = (await fetchDispatchDetail(id)).data.data
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await assignNoticeDispatch(detail.value.projectId)
    ElMessage.success('派单通知已发送')
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
    <div class="crumb">首页 / 中试调度管理 / 派单结果通知页</div>
    <h1 class="title">派单结果通知页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="企业">{{ detail.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="中试资源">{{ detail.resourceName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="技术人员">{{ detail.technicianName || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-button type="primary" @click="submit">发送派单通知</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
