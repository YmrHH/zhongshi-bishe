<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import ProjectStepBar from '@/components/ProjectStepBar.vue'
import StatusTag from '@/components/StatusTag.vue'
import { confirmCycleStats, fetchArchiveDetail, fetchArchiveTodos, fetchCycleStats } from '@/api/archive'
import type { ArchiveDetail, ArchiveStats } from '@/types/archive'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const detail = ref<ArchiveDetail | null>(null)
const stats = ref<ArchiveStats | null>(null)
const chartRef = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

onMounted(async () => {
  const todos = (await fetchArchiveTodos()).data.data
  const id = route.query.projectId ? Number(route.query.projectId) : todos[0]?.projectId
  if (!id) return
  loading.value = true
  try {
    detail.value = (await fetchArchiveDetail(id)).data.data
    stats.value = (await fetchCycleStats(id)).data.data
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
})

onUnmounted(() => chart?.dispose())

function renderChart() {
  if (!chartRef.value || !stats.value) return
  chart?.dispose()
  chart = echarts.init(chartRef.value)
  chart.setOption({
    title: { text: '中试周期统计（天）' },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: stats.value.months },
    yAxis: { type: 'value', name: '平均周期(天)' },
    series: [{ type: 'bar', data: stats.value.cycleDays, itemStyle: { color: '#1890ff' } }]
  })
}

async function submit(passed: boolean) {
  if (!detail.value) return
  loading.value = true
  try {
    await confirmCycleStats({ projectId: detail.value.projectId, passed })
    ElMessage.success(passed ? '统计已确认' : '已标记统计不可用')
    router.push(passed
      ? { path: '/center/dispatch/archive/success-rate', query: { projectId: detail.value.projectId } }
      : '/center/home')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">首页 / 中试档案管理 / 周期统计页</div>
    <h1 class="title">中试周期统计</h1>
    <div v-if="detail" class="page-card">
      <StatusTag :label="detail.statusLabel" :status="detail.status" />
      <ProjectStepBar :steps="detail.steps" />
      <el-alert v-if="stats && !stats.available" :title="stats.message" type="warning" show-icon style="margin:16px 0" />
      <div ref="chartRef" class="chart" />
      <el-button type="primary" :disabled="!stats?.available" @click="submit(true)">确认统计</el-button>
      <el-button type="warning" @click="submit(false)">统计不可用</el-button>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
.chart { height: 360px; margin: 16px 0; }
</style>
