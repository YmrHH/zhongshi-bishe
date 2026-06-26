<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { auditBrief, fetchArchiveDetail, fetchArchiveTodos } from '@/api/archive'
import type { ArchiveDetail } from '@/types/archive'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ArchiveDetail | null>(null)
const remark = ref('')

onMounted(async () => {
  const todos = (await fetchArchiveTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) detail.value = (await fetchArchiveDetail(id)).data.data
})

async function submit(passed: boolean) {
  if (!detail.value) return
  loading.value = true
  try {
    await auditBrief({ projectId: detail.value.projectId, passed, remark: remark.value })
    ElMessage.success(passed ? '简报审核通过' : '已退回重新生成')
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
    <div class="crumb">首页 / 中试档案管理 / 简报审核页</div>
    <h1 class="title">简报内容审核</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="1" border style="margin:16px 0">
        <el-descriptions-item label="简报标题">{{ detail.briefTitle }}</el-descriptions-item>
        <el-descriptions-item label="简报内容">{{ detail.briefContent }}</el-descriptions-item>
      </el-descriptions>
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="审核意见" style="margin-bottom:16px" />
      <el-button type="primary" @click="submit(true)">审核通过</el-button>
      <el-button type="warning" @click="submit(false)">退回修改</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
