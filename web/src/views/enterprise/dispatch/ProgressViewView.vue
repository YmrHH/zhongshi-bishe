<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import EvaluationSummaryPanel from '@/components/EvaluationSummaryPanel.vue'
import { acknowledgeDispatchProgress, fetchDispatchDetail } from '@/api/dispatch'
import type { DispatchDetail } from '@/types/dispatch'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const remark = ref('')
const detail = ref<DispatchDetail | null>(null)

const isDispatchStage = computed(() => detail.value?.stage === 'DISPATCH')
const isFeedbackStage = computed(() => detail.value?.stage === 'FEEDBACK')
const canAcknowledge = computed(() =>
  isDispatchStage.value && detail.value?.status && ['EXECUTING', 'CONFIRMED', 'EXEC_DONE'].includes(detail.value.status)
)
const alreadyAcked = computed(() => detail.value?.status === 'PROGRESS_ACKED')

async function load(projectId: number) {
  loading.value = true
  try {
    detail.value = (await fetchDispatchDetail(projectId)).data.data
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const projectId = Number(route.query.projectId)
  if (!projectId) {
    router.replace('/enterprise/home')
    return
  }
  await load(projectId)
})

async function onAcknowledge() {
  if (!detail.value) return
  loading.value = true
  try {
    await acknowledgeDispatchProgress(detail.value.projectId, { remark: remark.value })
    ElMessage.success('已确认知悉，待办已更新')
    router.push('/enterprise/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}

function goHome() {
  router.push('/enterprise/home')
}

async function onRefresh() {
  const projectId = Number(route.query.projectId)
  if (projectId) await load(projectId)
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试调度管理 / 执行进度查看页</div>
    <h1 class="title">执行进度查看页</h1>
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" />
    <ProjectStepBar v-if="detail?.steps?.length" :steps="detail.steps" />

    <el-alert
      v-if="alreadyAcked"
      type="success"
      :closable="false"
      show-icon
      style="margin:16px 0"
      title="您已确认知悉当前进度，本环节待办已结束；技术人员再次更新进度后将收到新待办。"
    />
    <el-alert
      v-else-if="isDispatchStage && canAcknowledge"
      type="info"
      :closable="false"
      show-icon
      style="margin:16px 0"
      title="执行进度由技术人员填报，本页供企业查看跟踪；查看后请点击「确认知悉」完成本环节办理。"
    />
    <el-alert
      v-else-if="isFeedbackStage"
      type="success"
      :closable="false"
      show-icon
      style="margin:16px 0"
      title="中试执行已完成，项目已进入「试验结果反馈」阶段，请返回首页关注后续待办。"
    />

    <div class="page-card">
      <h3>项目信息</h3>
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail?.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="当前环节">{{ detail?.currentNode }}</el-descriptions-item>
        <el-descriptions-item label="项目名称" :span="2">{{ detail?.title }}</el-descriptions-item>
        <el-descriptions-item label="中试资源">{{ detail?.resourceName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="技术人员">{{ detail?.technicianName || '—' }}</el-descriptions-item>
      </el-descriptions>
      <el-progress :percentage="detail?.progressPct || 0" style="margin-bottom:8px" />
      <span class="hint">当前完成度 {{ detail?.progressPct || 0 }}%</span>
    </div>

    <EvaluationSummaryPanel :summary="detail?.evaluationSummary" />

    <div class="page-card">
      <h3>进度记录</h3>
      <el-empty v-if="!(detail?.progressRecords?.length)" description="技术人员尚未填报进度，请稍后刷新查看" />
      <el-timeline v-else style="margin-top:16px">
        <el-timeline-item v-for="(r, idx) in detail?.progressRecords || []" :key="idx" :timestamp="r.reportTime">
          {{ r.progressPct }}% — {{ r.content }}
        </el-timeline-item>
      </el-timeline>
    </div>

    <div class="page-card">
      <h3>流程日志</h3>
      <el-timeline style="margin-top:16px">
        <el-timeline-item v-for="(log, idx) in detail?.logs || []" :key="idx" :timestamp="log.time">
          {{ log.remark }}
        </el-timeline-item>
      </el-timeline>
    </div>

    <div class="page-card actions">
      <el-input
        v-if="canAcknowledge"
        v-model="remark"
        type="textarea"
        :rows="2"
        placeholder="知悉说明（选填）"
        style="margin-bottom:16px"
      />
      <el-button v-if="canAcknowledge" type="primary" @click="onAcknowledge">确认知悉</el-button>
      <el-button @click="onRefresh">刷新进度</el-button>
      <el-button @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
.hint { color: #909399; font-size: 13px; }
.actions { margin-top: 16px; }
</style>
