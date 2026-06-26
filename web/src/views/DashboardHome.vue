<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchDashboard } from '@/api/auth'
import type { DashboardData } from '@/types/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const dashboard = ref<DashboardData | null>(null)
let pollTimer: ReturnType<typeof setInterval> | undefined

async function loadDashboard() {
  const res = await fetchDashboard()
  dashboard.value = res.data.data
}

onMounted(async () => {
  await loadDashboard()
  if (route.meta.module === 'home') {
    pollTimer = setInterval(loadDashboard, 30000)
  }
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})

const statLabels: Record<string, string> = {
  pending: '待处理',
  processing: '办理中',
  returned: '已退回',
  completed: '已完成'
}

function handleTodo(row: DashboardData['todos'][number]) {
  if (row.route) {
    const query: Record<string, number> = {}
    if (row.projectId) query.projectId = row.projectId
    if (row.taskId) query.taskId = row.taskId
    router.push({ path: row.route, query: Object.keys(query).length ? query : undefined })
    return
  }
  if (userStore.profile?.role === 'ENTERPRISE') {
    router.push('/enterprise/demand/preview')
  } else if (userStore.profile?.role === 'DISPATCHER') {
    router.push('/center/dispatch/demand/workbench')
  } else if (userStore.profile?.role === 'AUDITOR') {
    router.push('/center/audit/demand/verify')
  }
}
</script>

<template>
  <div>
    <div class="crumb">首页 / 公共 / {{ $route.meta.title }}</div>
    <h1 class="title">{{ $route.meta.title }}</h1>
    <el-tag type="primary">公共</el-tag>
    <el-tag style="margin-left:8px">{{ userStore.profile?.roleLabel }}</el-tag>

    <div v-if="dashboard" class="stat-grid">
      <div v-for="(val, key) in dashboard.stats" :key="key" class="stat-item">
        {{ statLabels[key] || key }}<b>{{ val }}</b>
      </div>
    </div>

    <div class="page-card">
      <h3>待办列表</h3>
      <el-table :data="dashboard?.todos || []" style="width:100%;margin-top:12px">
        <el-table-column prop="projectNo" label="项目编号" width="140" />
        <el-table-column prop="title" label="项目名称" />
        <el-table-column prop="module" label="当前模块" width="140" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="time" label="时间" width="160" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleTodo(row)">办理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!(dashboard?.todos?.length)" class="hint">暂无待办。企业用户可从左侧「中试需求管理」发起新需求。</p>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
.hint { color: #909399; margin-top: 16px; font-size: 13px; }
</style>
