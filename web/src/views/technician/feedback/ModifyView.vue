<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchFeedbackDetail, fetchFeedbackTodos, modifyFeedback } from '@/api/feedback'
import type { FeedbackDetail } from '@/types/feedback'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<FeedbackDetail | null>(null)
const content = ref('')
const remark = ref('')

onMounted(async () => {
  const todos = (await fetchFeedbackTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) {
    detail.value = (await fetchFeedbackDetail(id)).data.data
    content.value = detail.value.reportContent || ''
    remark.value = detail.value.validateRemark || ''
  }
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await modifyFeedback(detail.value.projectId, { content: content.value, remark: remark.value })
    ElMessage.success('已重新提交，待数据校验')
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
    <div class="crumb">首页 / 中试反馈管理 / 结果修改页</div>
    <h1 class="title">试验结果修改</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-alert v-if="detail.validateRemark" :title="detail.validateRemark" type="warning" show-icon style="margin:16px 0" />
      <el-form label-width="100px" style="max-width:640px">
        <el-form-item label="修改说明">
          <el-input v-model="remark" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="试验结果">
          <el-input v-model="content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">重新提交</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
