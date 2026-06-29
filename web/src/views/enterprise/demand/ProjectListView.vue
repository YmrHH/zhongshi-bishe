<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DemandPageHeader from '@/components/DemandPageHeader.vue'
import ListPagination from '@/components/ListPagination.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchEnterpriseDemandProjects, type EnterpriseStageFilter } from '@/api/demand'
import type { DemandEnterpriseProject } from '@/types/demand'

const STAGE_OPTIONS: Array<{ value: EnterpriseStageFilter; label: string }> = [
  { value: 'ALL', label: '全部' },
  { value: 'DEMAND', label: '需求办理中' },
  { value: 'EVALUATION', label: '评估中' },
  { value: 'DISPATCH', label: '调度执行中' },
  { value: 'FEEDBACK', label: '反馈复核中' },
  { value: 'ARCHIVE', label: '档案统计中' },
  { value: 'CLOSED', label: '已结案' }
]

const router = useRouter()
const loading = ref(false)
const projects = ref<DemandEnterpriseProject[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const stageFilter = ref<EnterpriseStageFilter>('ALL')
const keywordInput = ref('')
const activeKeyword = ref('')

const emptyText = computed(() => {
  if (activeKeyword.value) return '未找到匹配的项目'
  if (stageFilter.value === 'ALL') return '暂无已提交项目，请先发起新需求'
  return '该阶段暂无项目'
})

onMounted(() => loadProjects())

async function loadProjects() {
  loading.value = true
  try {
    const params: {
      page: number
      pageSize: number
      stageFilter: EnterpriseStageFilter
      keyword?: string
    } = {
      page: page.value,
      pageSize: pageSize.value,
      stageFilter: stageFilter.value
    }
    if (activeKeyword.value) params.keyword = activeKeyword.value
    const res = await fetchEnterpriseDemandProjects(params)
    projects.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onStageChange() {
  page.value = 1
  loadProjects()
}

function onSearch() {
  activeKeyword.value = keywordInput.value.trim()
  page.value = 1
  loadProjects()
}

function onReset() {
  keywordInput.value = ''
  activeKeyword.value = ''
  page.value = 1
  loadProjects()
}

function onPageChange(p: number) {
  page.value = p
  loadProjects()
}

function onPageSizeChange(s: number) {
  pageSize.value = s
  page.value = 1
  loadProjects()
}

function goProgress(row: DemandEnterpriseProject) {
  router.push({ path: '/enterprise/demand/progress', query: { projectId: row.projectId } })
}

function progressActionLabel(row: DemandEnterpriseProject) {
  return row.stage === 'DEMAND' ? '查看受理进度' : '查看项目进度'
}

function goNewDemand() {
  router.push('/enterprise/demand/preview')
}
</script>

<template>
  <div v-loading="loading">
    <DemandPageHeader
      title="我的需求项目"
      crumb="首页 / 中试需求管理 / 我的需求项目"
      :show-back="false"
    />
    <p class="hint">已提交的需求可在此查看办理进度；归档后仍可查阅全流程记录。</p>

    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <span class="filter-label">阶段</span>
          <el-select
            v-model="stageFilter"
            placeholder="全部阶段"
            style="width: 160px"
            @change="onStageChange"
          >
            <el-option
              v-for="opt in STAGE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <span class="filter-label">搜索</span>
          <el-input
            v-model="keywordInput"
            placeholder="编号或项目名称"
            clearable
            style="width: 220px"
            @keyup.enter="onSearch"
          />
          <el-button type="primary" @click="onSearch">搜索</el-button>
          <el-button @click="onReset">重置</el-button>
        </div>
        <el-button type="primary" size="large" @click="goNewDemand">发起新需求</el-button>
      </div>

      <el-table :data="projects" style="width:100%;margin-top:16px" :empty-text="emptyText">
        <el-table-column prop="projectNo" label="项目编号" width="140" />
        <el-table-column prop="title" label="项目名称" min-width="160" />
        <el-table-column prop="currentNode" label="当前环节" width="140" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <StatusTag :label="row.statusLabel" :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.canViewProgress" type="primary" link @click="goProgress(row)">
              {{ progressActionLabel(row) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <ListPagination
        v-if="total > 0"
        :total="total"
        :page="page"
        :page-size="pageSize"
        @change="onPageChange"
        @size-change="onPageSizeChange"
      />
    </div>
  </div>
</template>

<style scoped>
.hint { color: #909399; font-size: 14px; margin-bottom: 16px; }
.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.filters {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.filter-label { color: #606266; font-size: 14px; }
</style>
