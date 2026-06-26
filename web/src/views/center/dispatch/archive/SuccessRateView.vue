<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import * as echarts from 'echarts'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { fetchArchiveDetail, fetchArchiveTodos, fetchSuccessRate } from '@/api/archive'
import type { ArchiveDetail, ArchiveStats } from '@/types/archive'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ArchiveDetail | null>(null)
const stats = ref<ArchiveStats | null>(null)
const pieRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

onMounted(async () => {
  const todos = (await fetchArchiveTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (!id) return
  loading.value = true
  try {
    detail.value = (await fetchArchiveDetail(id)).data.data
    stats.value = (await fetchSuccessRate(id)).data.data
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => chart?.dispose())

function renderChart() {
  if (!pieRef.value || !stats.value) return
  chart?.dispose()
  chart = echarts.init(pieRef.value)
  chart.setOption({
    title: { text: `成功率 ${stats.value.successRate}%`, subtext: `结案 ${stats.value.closedProjects} / 总计 ${stats.value.totalProjects}` },
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: '55%',
      data: stats.value.stageDistribution.length
        ? stats.value.stageDistribution
        : [{ name: '暂无数据', value: 1 }]
    }]
  })
}

function nextStep() {
  if (detail.value) {
    router.push({ path: '/center/dispatch/archive/brief-generate', query: { projectId: detail.value.projectId } })
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试档案管理 / 成功率分析页</div>
    <h1 class="title">成功率分析</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <div ref="pieRef" class="chart" />
      <el-button type="primary" @click="nextStep">前往简报生成</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
.chart { height: 360px; margin: 16px 0; }
</style>
