<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchArchiveDetail, fetchArchiveTodos, generateBrief } from '@/api/archive'
import type { ArchiveDetail } from '@/types/archive'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ArchiveDetail | null>(null)
const title = ref('')
const content = ref('')

onMounted(async () => {
  const todos = (await fetchArchiveTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) {
    detail.value = (await fetchArchiveDetail(id)).data.data
    title.value = detail.value.briefTitle || `${detail.value.projectNo} 中试服务简报`
    content.value = detail.value.briefContent || ''
  }
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await generateBrief({ projectId: detail.value.projectId, title: title.value, content: content.value })
    ElMessage.success('服务简报已生成，待审核员审核')
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
    <div class="crumb">首页 / 中试档案管理 / 简报生成页</div>
    <h1 class="title">服务简报生成</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-form label-width="80px" style="max-width:640px;margin-top:16px">
        <el-form-item label="标题">
          <el-input v-model="title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">生成简报</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
