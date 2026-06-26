<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchFeedbackDetail, fetchFeedbackTodos, submitFeedback } from '@/api/feedback'
import type { FeedbackDetail } from '@/types/feedback'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<FeedbackDetail | null>(null)
const content = ref('')
const fileUrl = ref('')
const fileName = ref('')

onMounted(async () => {
  const todos = (await fetchFeedbackTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) {
    detail.value = (await fetchFeedbackDetail(id)).data.data
    content.value = detail.value.reportContent || ''
    fileUrl.value = detail.value.fileUrl || ''
    fileName.value = detail.value.fileName || ''
  }
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await submitFeedback(detail.value.projectId, { content: content.value, fileUrl: fileUrl.value, fileName: fileName.value })
    ElMessage.success('试验结果已提交')
    router.push('/technician/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试反馈管理 / 结果提交页</div>
    <h1 class="title">试验结果提交</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ detail.enterpriseName }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="100px" style="max-width:640px">
        <el-form-item label="试验结果">
          <el-input v-model="content" type="textarea" :rows="5" placeholder="填写试验结果摘要" />
        </el-form-item>
        <el-form-item label="报告附件">
          <el-input v-model="fileUrl" placeholder="附件 URL（可先上传后粘贴）" />
          <el-input v-model="fileName" placeholder="附件名称" style="margin-top:8px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">提交结果</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
