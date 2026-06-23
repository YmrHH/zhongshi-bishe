<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import MaterialUpload from '@/components/MaterialUpload.vue'
import { fetchDemandPreview, supplementDemand } from '@/api/demand'
import type { DemandDetail } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DemandDetail | null>(null)
const content = ref('')
const materials = ref<DemandDetail['materials']>([])

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
    content.value = detail.value.content || ''
    materials.value = [...(detail.value.materials || [])]
  } finally {
    loading.value = false
  }
})

async function onSubmit() {
  const projectId = Number(route.query.projectId)
  loading.value = true
  try {
    await supplementDemand(projectId, { content: content.value, materials: materials.value })
    ElMessage.success('材料已补充并重新提交')
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
    <div class="crumb">首页 / 中试需求管理 / 材料补充页</div>
    <h1 class="title">材料补充页</h1>
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" />
    <ProjectStepBar v-if="detail?.steps?.length" :steps="detail.steps" />

    <el-alert v-if="detail?.rejectReason" type="warning" :title="'退回原因：' + detail.rejectReason" show-icon style="margin-top:16px" />

    <div class="page-card">
      <h3>补充需求材料</h3>
      <el-form label-width="120px" style="margin-top:16px">
        <el-form-item label="需求说明">
          <el-input v-model="content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="补充材料">
          <MaterialUpload v-model="materials" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="onSubmit">重新提交</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
