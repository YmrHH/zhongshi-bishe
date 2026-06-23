<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchEvaluationDetail, fetchEvaluationTodos, precheckEvaluation } from '@/api/evaluation'
import type { EvaluationDetail } from '@/types/evaluation'
import type { DemandTodo } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const todos = ref<DemandTodo[]>([])
const detail = ref<EvaluationDetail | null>(null)
const remark = ref('')

async function load(projectId: number) {
  router.replace({ query: { projectId } })
  detail.value = (await fetchEvaluationDetail(projectId)).data.data
}

onMounted(async () => {
  todos.value = (await fetchEvaluationTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos.value[0]?.projectId
  if (id) await load(id)
})

async function submit() {
  if (!detail.value) return
  loading.value = true
  try {
    await precheckEvaluation(detail.value.projectId, { remark: remark.value })
    ElMessage.success('前置核查完成')
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
    <div class="crumb">首页 / 中试评估管理 / 评估前置核查页</div>
    <h1 class="title">评估前置核查页</h1>
    <div class="page-card">
      <el-table :data="todos" @row-click="(r: DemandTodo) => load(r.projectId)">
        <el-table-column prop="projectNo" label="编号" width="140" />
        <el-table-column prop="title" label="名称" />
        <el-table-column label="状态" width="140"><template #default="{ row }"><StatusTag :label="row.statusLabel" :status="row.status" /></template></el-table-column>
      </el-table>
    </div>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-input v-model="remark" type="textarea" :rows="3" placeholder="核查意见" style="margin:16px 0" />
      <el-button type="primary" @click="submit">核查通过</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
</style>
