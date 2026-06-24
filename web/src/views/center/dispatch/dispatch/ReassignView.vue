<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchDispatchDetail, reassignDispatch } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DispatchDetail | null>(null)
const technicianId = ref<number>()
const remark = ref('')

onMounted(async () => {
  const id = Number(route.query.projectId)
  if (id) {
    detail.value = (await fetchDispatchDetail(id)).data.data
    technicianId.value = detail.value.technicianId
  }
})

async function submit() {
  if (!detail.value || !technicianId.value) {
    ElMessage.warning('请选择技术人员')
    return
  }
  loading.value = true
  try {
    await reassignDispatch(detail.value.projectId, { technicianId: technicianId.value, remark: remark.value })
    ElMessage.success('任务已重新派发')
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
    <div class="crumb">首页 / 中试调度管理 / 异常任务重派页</div>
    <h1 class="title">异常任务重派页</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-form label-width="120px" style="margin-top:16px;max-width:560px">
        <el-form-item label="原技术人员">{{ detail.technicianName || '—' }}</el-form-item>
        <el-form-item label="新技术人员">
          <el-select v-model="technicianId" placeholder="选择技术人员" style="width:100%">
            <el-option v-for="t in detail.technicians || []" :key="t.id" :label="`${t.realName} - ${t.orgName}`" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="重派说明">
          <el-input v-model="remark" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit">确认重派</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
