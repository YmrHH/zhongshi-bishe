<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchArchiveLedger, fetchArchiveTodos, updateArchiveLedger } from '@/api/archive'
import type { ArchiveDetail } from '@/types/archive'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ArchiveDetail | null>(null)
const ledgerJson = ref('{"projectNo":"","enterprise":"","materials":"齐全","remark":""}')

onMounted(async () => {
  const todos = (await fetchArchiveTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) {
    detail.value = (await fetchArchiveLedger(id)).data.data
    if (detail.value.ledgerJson) ledgerJson.value = detail.value.ledgerJson
  }
})

async function submit(complete: boolean) {
  if (!detail.value) return
  loading.value = true
  try {
    await updateArchiveLedger({ projectId: detail.value.projectId, complete, ledgerJson: ledgerJson.value })
    ElMessage.success(complete ? '台账维护完成' : '已标记为不完整')
    router.push(complete ? '/center/audit/archive/confirm' : '/center/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试档案管理 / 台账维护页</div>
    <h1 class="title">项目台账维护</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="企业">{{ detail.enterpriseName }}</el-descriptions-item>
      </el-descriptions>
      <el-input v-model="ledgerJson" type="textarea" :rows="6" placeholder="台账 JSON 内容" />
      <div style="margin-top:16px">
        <el-button type="primary" @click="submit(true)">台账完整，提交</el-button>
        <el-button type="warning" @click="submit(false)">台账不完整</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
