<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { execConfirmDispatch, fetchDispatchDetail, fetchDispatchTodos } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DispatchDetail | null>(null)
const remark = ref('')

onMounted(async () => {
  const todos = (await fetchDispatchTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) detail.value = (await fetchDispatchDetail(id)).data.data
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await execConfirmDispatch(detail.value.projectId, { remark: remark.value })
    ElMessage.success('执行结果已确认，请继续办理调度归档')
    router.push({ path: '/center/dispatch/dispatch/archive', query: { projectId: detail.value.projectId } })
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试调度管理 / 执行结果确认页</div>
    <h1 class="title">执行结果确认页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-progress :percentage="detail.progressPct || 0" style="margin:16px 0" />
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="确认意见" style="margin-bottom:16px" />
      <el-button type="primary" @click="submit">确认执行完成</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
