<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { collectArchive, fetchArchiveDetail, fetchArchiveTodos } from '@/api/archive'
import type { ArchiveDetail } from '@/types/archive'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ArchiveDetail | null>(null)
const collectRemark = ref('')

onMounted(async () => {
  const todos = (await fetchArchiveTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) detail.value = (await fetchArchiveDetail(id)).data.data
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await collectArchive(detail.value.projectId, { collectRemark: collectRemark.value })
    ElMessage.success('结案资料已归集')
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
    <div class="crumb">首页 / 中试档案管理 / 结案资料归集页</div>
    <h1 class="title">结案资料归集</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-input v-model="collectRemark" type="textarea" :rows="4" placeholder="归集说明" style="margin:16px 0" />
      <el-button type="primary" @click="submit">确认归集</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
