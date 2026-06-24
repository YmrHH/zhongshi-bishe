<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchDispatchDetail, reportTaskProgress } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DispatchDetail | null>(null)
const taskId = ref<number>()
const progressPct = ref(30)
const content = ref('')

onMounted(async () => {
  const projectId = Number(route.query.projectId)
  taskId.value = Number(route.query.taskId)
  if (projectId) {
    detail.value = (await fetchDispatchDetail(projectId)).data.data
    progressPct.value = detail.value.progressPct || 30
  }
})

async function submit() {
  if (!taskId.value) return
  loading.value = true
  try {
    const res = await reportTaskProgress(taskId.value, { progressPct: progressPct.value, content: content.value })
    detail.value = res.data.data
    ElMessage.success('进度已提交')
    if (progressPct.value >= 100) {
      router.push('/technician/home')
    }
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试调度管理 / 填报执行进度页</div>
    <h1 class="title">填报执行进度页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-form label-width="100px" style="margin-top:16px;max-width:560px">
        <el-form-item label="当前进度">
          <el-slider v-model="progressPct" :min="0" :max="100" show-input />
        </el-form-item>
        <el-form-item label="进度说明">
          <el-input v-model="content" type="textarea" :rows="4" placeholder="描述当前执行进展" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交进度</el-button>
        </el-form-item>
      </el-form>
      <h3>历史记录</h3>
      <el-table :data="detail.progressRecords || []" size="small" style="margin-top:12px">
        <el-table-column prop="reportTime" label="时间" width="180" />
        <el-table-column prop="progressPct" label="进度" width="80" />
        <el-table-column prop="content" label="说明" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
