<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { archiveDispatch, fetchDispatchDetail } from '@/api/dispatch'
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
    await archiveDispatch(detail.value.projectId)
    ElMessage.success('调度段已归档，项目进入反馈阶段')
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
    <div class="crumb">首页 / 中试调度管理 / 调度信息归档页</div>
    <h1 class="title">调度信息归档页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-timeline style="margin:16px 0">
        <el-timeline-item v-for="(log, idx) in detail.logs || []" :key="idx" :timestamp="log.time">
          {{ log.remark }}
        </el-timeline-item>
      </el-timeline>
      <el-button type="primary" @click="submit">确认归档</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
