<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import DemandPageHeader from '@/components/DemandPageHeader.vue'
import DemandPhaseStepBar from '@/components/DemandPhaseStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import MaterialUpload from '@/components/MaterialUpload.vue'
import { fetchProjectProgress } from '@/api/project'
import { fetchDemandPreview, receiptDemand, supplementDemand } from '@/api/demand'
import type { DemandDetail } from '@/types/demand'
import type { ProjectProgress } from '@/types/project'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ProjectProgress | null>(null)
const demandDetail = ref<DemandDetail | null>(null)
const supplementContent = ref('')
const newMaterials = ref<DemandDetail['materials']>([])

const pageTitle = ref('项目进度详情')
const pageCrumb = ref('首页 / 中试需求管理 / 项目进度详情')

async function reload(projectId: number) {
  const res = await fetchProjectProgress(projectId)
  detail.value = res.data.data
  if (detail.value.stage === 'DEMAND') {
    pageTitle.value = '受理进度详情页'
    pageCrumb.value = '首页 / 中试需求管理 / 受理进度详情页'
    if (detail.value.status === 'ACCEPTED' || detail.value.status === 'RETURNED') {
      const d = await fetchDemandPreview(projectId)
      demandDetail.value = d.data.data
      if (detail.value.status === 'RETURNED') {
        supplementContent.value = demandDetail.value.content || ''
        newMaterials.value = []
      }
    } else {
      demandDetail.value = null
    }
  } else {
    pageTitle.value = '项目进度详情'
    pageCrumb.value = `首页 / ${detail.value.moduleName} / 项目进度详情`
    demandDetail.value = null
  }
}

onMounted(async () => {
  const projectId = Number(route.query.projectId)
  if (!projectId) {
    router.replace('/enterprise/demand/projects')
    return
  }
  loading.value = true
  try {
    await reload(projectId)
  } finally {
    loading.value = false
  }
})

async function onReceipt() {
  const projectId = Number(route.query.projectId)
  loading.value = true
  try {
    await receiptDemand(projectId)
    ElMessage.success('受理回执已签收')
    await reload(projectId)
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '签收失败')
  } finally {
    loading.value = false
  }
}

async function onSupplement() {
  const projectId = Number(route.query.projectId)
  const existing = demandDetail.value?.materials || []
  const materials = [...existing, ...newMaterials.value]
  if (!materials.length) {
    ElMessage.warning('请至少保留或上传一份材料')
    return
  }
  loading.value = true
  try {
    await supplementDemand(projectId, { content: supplementContent.value, materials })
    ElMessage.success('材料已补充并重新提交')
    await reload(projectId)
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '提交失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <DemandPageHeader :title="pageTitle" :crumb="pageCrumb" />
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" style="margin-bottom:8px" />

    <DemandPhaseStepBar
      v-if="detail?.moduleSteps?.length"
      :title="`${detail.moduleName}办理进度`"
      :steps="detail.moduleSteps"
    />

    <div class="page-card">
      <h3>项目信息</h3>
      <el-descriptions :column="2" border style="margin:16px 0">
        <el-descriptions-item label="项目编号">{{ detail?.projectNo }}</el-descriptions-item>
        <el-descriptions-item label="当前主模块">{{ detail?.moduleName }}</el-descriptions-item>
        <el-descriptions-item label="当前环节">{{ detail?.currentNode }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail?.submittedAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="项目名称" :span="2">{{ detail?.title }}</el-descriptions-item>
        <el-descriptions-item v-if="detail?.acceptOpinion || detail?.rejectReason" label="受理意见" :span="2">
          {{ detail?.acceptOpinion || detail?.rejectReason || '—' }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail?.content" label="需求内容" :span="2">{{ detail.content }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div v-if="detail?.status === 'ACCEPTED' && demandDetail" class="page-card action-card">
      <h3>受理回执签收</h3>
      <el-descriptions :column="1" border style="margin:16px 0">
        <el-descriptions-item label="受理意见">{{ demandDetail.acceptOpinion || '同意受理' }}</el-descriptions-item>
      </el-descriptions>
      <el-button type="primary" @click="onReceipt">确认签收</el-button>
    </div>

    <div v-if="detail?.status === 'RETURNED' && demandDetail" class="page-card action-card">
      <h3>补充材料</h3>
      <el-alert v-if="detail.rejectReason" type="warning" :title="'退回原因：' + detail.rejectReason" show-icon style="margin-bottom:16px" />
      <el-form label-width="120px">
        <el-form-item label="需求说明">
          <el-input v-model="supplementContent" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item v-if="demandDetail.materials?.length" label="已有材料">
          <MaterialUpload :model-value="demandDetail.materials" readonly />
        </el-form-item>
        <el-form-item label="补充上传">
          <MaterialUpload v-model="newMaterials" />
        </el-form-item>
      </el-form>
      <el-button type="primary" @click="onSupplement">重新提交</el-button>
    </div>

    <div class="page-card">
      <h3>流程日志</h3>
      <el-timeline style="margin-top:16px">
        <el-timeline-item v-for="(log, idx) in detail?.logs || []" :key="idx" :timestamp="String(log.time)">
          {{ log.remark }}（{{ log.fromStatus }} → {{ log.toStatus }}）
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<style scoped>
.action-card { margin-top: 0; }
</style>
