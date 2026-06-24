<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { confirmTask, fetchDispatchDetail } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DispatchDetail | null>(null)
const taskId = ref<number>()

onMounted(async () => {
  const projectId = Number(route.query.projectId)
  taskId.value = Number(route.query.taskId)
  if (projectId) detail.value = (await fetchDispatchDetail(projectId)).data.data
})

async function submit() {
  if (!taskId.value) return
  loading.value = true
  try {
    const res = await confirmTask(taskId.value)
    detail.value = res.data.data
    ElMessage.success('任务签收确认完成')
    router.push({ path: '/technician/dispatch/progress-report', query: { projectId: detail.value?.projectId, taskId: taskId.value } })
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试调度管理 / 任务接收确认页</div>
    <h1 class="title">任务接收确认页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="中试资源">{{ detail.resourceName || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-button type="primary" @click="submit">确认签收</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
