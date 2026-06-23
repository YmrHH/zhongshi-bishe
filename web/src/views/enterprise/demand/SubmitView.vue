<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import MaterialUpload from '@/components/MaterialUpload.vue'
import { fetchDemandPreview, submitDemand } from '@/api/demand'
import type { DemandDetail, DemandForm } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DemandDetail | null>(null)
const form = ref<DemandForm>({ title: '', content: '', materials: [] })

onMounted(async () => {
  const projectId = Number(route.query.projectId)
  if (!projectId) {
    router.replace('/enterprise/demand/preview')
    return
  }
  loading.value = true
  try {
    const res = await fetchDemandPreview(projectId)
    detail.value = res.data.data
    form.value = {
      title: detail.value.title,
      content: detail.value.content,
      pilotType: detail.value.pilotType,
      expectedDays: detail.value.expectedDays,
      contactName: detail.value.contactName,
      contactPhone: detail.value.contactPhone,
      materials: detail.value.materials || []
    }
  } finally {
    loading.value = false
  }
})

async function onSubmit() {
  const projectId = Number(route.query.projectId)
  if (!form.value.content) {
    ElMessage.warning('请填写需求内容')
    return
  }
  loading.value = true
  try {
    await submitDemand(projectId, form.value)
    ElMessage.success('需求已提交，等待中心受理')
    router.push({ path: '/enterprise/demand/progress', query: { projectId } })
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试需求管理 / 需求填报页</div>
    <h1 class="title">需求填报页</h1>
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" />
    <ProjectStepBar v-if="detail?.steps?.length" :steps="detail.steps" />

    <div class="page-card">
      <h3>中试需求提交</h3>
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail?.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ detail?.enterpriseName }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="120px">
        <el-form-item label="需求内容" required>
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请详细描述中试需求" />
        </el-form-item>
        <el-form-item label="附件材料">
          <MaterialUpload v-model="form.materials" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="onSubmit">提交需求</el-button>
      <el-button @click="router.back()">返回预览</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
