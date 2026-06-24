<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchDispatchDetail, fetchDispatchTodos, superviseDispatch } from '@/api/dispatch'
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
    await superviseDispatch(detail.value.projectId, { remark: remark.value })
    ElMessage.success('督办通知已发送')
    router.push('/center/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}

function goReassign() {
  if (detail.value) {
    router.push({ path: '/center/dispatch/dispatch/reassign', query: { projectId: detail.value.projectId } })
  }
}

function goExecConfirm() {
  if (detail.value) {
    router.push({ path: '/center/dispatch/dispatch/exec-confirm', query: { projectId: detail.value.projectId } })
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试调度管理 / 进度通报督办页</div>
    <h1 class="title">进度通报督办页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-progress :percentage="detail.progressPct || 0" style="margin:16px 0" />
      <el-table :data="detail.progressRecords || []" size="small" style="margin-bottom:16px">
        <el-table-column prop="reportTime" label="时间" width="180" />
        <el-table-column prop="progressPct" label="进度" width="80" />
        <el-table-column prop="content" label="说明" />
      </el-table>
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="督办意见" />
      <div style="margin-top:16px">
        <el-button type="primary" @click="submit">发送督办</el-button>
        <el-button type="success" @click="goExecConfirm">执行结果确认</el-button>
        <el-button @click="goReassign">异常重派</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
