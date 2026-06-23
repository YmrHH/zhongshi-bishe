<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StatusTag from '@/components/StatusTag.vue'
import { fetchDemandPreview, rejectDemand } from '@/api/demand'
import type { DemandDetail } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DemandDetail | null>(null)
const reason = ref('')

onMounted(async () => {
  const projectId = Number(route.query.projectId)
  if (!projectId) {
    router.replace('/center/dispatch/demand/workbench')
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

async function onReject() {
  if (!reason.value) {
    ElMessage.warning('请填写退回意见')
    return
  }
  const projectId = Number(route.query.projectId)
  loading.value = true
  try {
    await rejectDemand(projectId, reason.value)
    ElMessage.success('已退回企业补充材料')
    router.push('/center/dispatch/demand/workbench')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '退回失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试需求管理 / 退回意见页</div>
    <h1 class="title">退回意见页</h1>
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" />

    <div class="page-card">
      <h3>填写退回意见</h3>
      <p>项目：{{ detail?.projectNo }} — {{ detail?.title }}</p>
      <el-input v-model="reason" type="textarea" :rows="5" placeholder="请说明材料不齐或需补充的内容" style="margin:16px 0" />
      <el-button type="warning" @click="onReject">确认退回</el-button>
      <el-button @click="router.back()">取消</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
