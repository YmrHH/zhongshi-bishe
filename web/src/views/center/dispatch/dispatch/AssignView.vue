<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { assignDispatch, fetchDispatchDetail, fetchDispatchTodos } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DispatchDetail | null>(null)
const resourceId = ref<number>()
const technicianId = ref<number>()
const remark = ref('')

onMounted(async () => {
  const todos = (await fetchDispatchTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (id) {
    detail.value = (await fetchDispatchDetail(id)).data.data
    resourceId.value = detail.value.resourceId
    technicianId.value = detail.value.technicianId
  }
})

async function submit() {
  if (!detail.value || !resourceId.value || !technicianId.value) {
    ElMessage.warning('请选择资源与技术人员')
    return
  }
  loading.value = true
  try {
    await assignDispatch(detail.value.projectId, {
      resourceId: resourceId.value,
      technicianId: technicianId.value,
      remark: remark.value
    })
    ElMessage.success('任务派发完成')
    router.push('/center/dispatch/dispatch/assign-notice?projectId=' + detail.value.projectId)
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试调度管理 / 中试任务派发页</div>
    <h1 class="title">中试任务派发页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-form label-width="120px" style="margin-top:16px;max-width:560px">
        <el-form-item label="中试资源">
          <el-select v-model="resourceId" placeholder="选择资源" style="width:100%">
            <el-option v-for="r in detail.resources || []" :key="r.id" :label="`${r.name}（${r.capacity}）`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="技术人员">
          <el-select v-model="technicianId" placeholder="选择技术人员" style="width:100%">
            <el-option v-for="t in detail.technicians || []" :key="t.id" :label="`${t.realName} - ${t.orgName}`" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="派单说明">
          <el-input v-model="remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">确认派发</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
