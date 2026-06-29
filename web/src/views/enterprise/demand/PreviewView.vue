<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import DemandPageHeader from '@/components/DemandPageHeader.vue'
import DemandPhaseStepBar from '@/components/DemandPhaseStepBar.vue'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import MaterialUpload from '@/components/MaterialUpload.vue'
import {
  createDemandProject,
  fetchDemandPreview,
  saveDemandDraft
} from '@/api/demand'
import type { DemandDetail, DemandForm } from '@/types/demand'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<DemandDetail | null>(null)
const form = ref<DemandForm>({
  title: '',
  content: '',
  pilotType: '',
  expectedDays: 30,
  contactName: '',
  contactPhone: '',
  materials: []
})

const projectId = computed(() => {
  const id = route.query.projectId
  return id ? Number(id) : null
})

onMounted(async () => {
  if (projectId.value) {
    loading.value = true
    try {
      const res = await fetchDemandPreview(projectId.value)
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
  }
})

async function ensureProject() {
  if (projectId.value) return projectId.value
  if (!form.value.title) {
    ElMessage.warning('请填写项目名称')
    throw new Error('no title')
  }
  const res = await createDemandProject(form.value)
  const id = res.data.data.projectId
  await router.replace({ query: { projectId: id } })
  return id
}

async function onSaveDraft() {
  if (form.value.contactPhone && !/^1\d{10}$/.test(form.value.contactPhone)) {
    ElMessage.warning('联系电话须为11位手机号')
    return
  }
  loading.value = true
  try {
    const id = await ensureProject()
    await saveDemandDraft(id, form.value)
    const res = await fetchDemandPreview(id)
    detail.value = res.data.data
    ElMessage.success('草稿已保存')
  } catch (e) {
    if (e instanceof Error && e.message !== 'no title') {
      ElMessage.error(e.message)
    }
  } finally {
    loading.value = false
  }
}

async function onGoSubmit() {
  if (form.value.contactPhone && !/^1\d{10}$/.test(form.value.contactPhone)) {
    ElMessage.warning('联系电话须为11位手机号')
    return
  }
  loading.value = true
  try {
    const id = await ensureProject()
    await saveDemandDraft(id, form.value)
    router.push({ path: '/enterprise/demand/submit', query: { projectId: id } })
  } catch (e) {
    if (e instanceof Error && e.message !== 'no title') {
      ElMessage.error(e.message)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <DemandPageHeader
      title="需求预览确认页"
      crumb="首页 / 中试需求管理 / 需求预览确认页"
    />
    <StatusTag v-if="detail" :label="detail.statusLabel" :status="detail.status" style="margin-bottom:8px" />
    <DemandPhaseStepBar v-if="detail?.phaseSteps?.length" :steps="detail.phaseSteps" />

    <div class="page-card">
      <h3>需求信息预览</h3>
      <el-form label-width="120px" style="margin-top:16px">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.title" placeholder="请输入中试项目名称" />
        </el-form-item>
        <el-form-item label="中试类型">
          <el-select v-model="form.pilotType" placeholder="请选择" style="width:100%">
            <el-option label="工艺放大" value="工艺放大" />
            <el-option label="产品验证" value="产品验证" />
            <el-option label="稳定性测试" value="稳定性测试" />
          </el-select>
        </el-form-item>
        <el-form-item label="期望周期(天)">
          <el-input-number v-model="form.expectedDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input v-model="form.contactName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" maxlength="11" placeholder="11位手机号" show-word-limit />
        </el-form-item>
        <el-form-item label="需求说明">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="附件材料">
          <MaterialUpload v-model="form.materials" />
        </el-form-item>
      </el-form>
      <div style="margin-top:20px">
        <el-button type="primary" @click="onSaveDraft">保存草稿</el-button>
        <el-button type="success" @click="onGoSubmit">确认并填报提交</el-button>
      </div>
    </div>
  </div>
</template>
