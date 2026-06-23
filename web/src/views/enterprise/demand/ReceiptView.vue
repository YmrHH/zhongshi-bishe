<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchDemandPreview, receiptDemand } from '@/api/demand'
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
    const res = await fetchDemandPreview(projectId)
    detail.value = res.data.data
  } finally {
    loading.value = false
  }
})

async function onReceipt() {
  const projectId = Number(route.query.projectId)
  loading.value = true
  try {
    await receiptDemand(projectId)
    ElMessage.success('受理回执已签收')
    router.push({ path: '/enterprise/demand/progress', query: { projectId } })
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '签收失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试需求管理 / 受理回执签收页</div>
    <h1 class="title">受理回执签收页</h1>
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" />
    <ProjectStepBar v-if="detail?.steps?.length" :steps="detail.steps" />

    <div class="page-card">
      <h3>受理结果回执</h3>
      <el-descriptions :column="1" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail?.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="项目名称">{{ detail?.title }}</el-descriptions-item>
        <el-descriptions-item label="受理意见">{{ detail?.acceptOpinion || '同意受理' }}</el-descriptions-item>
      </el-descriptions>
      <el-button type="primary" @click="onReceipt">确认签收</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
