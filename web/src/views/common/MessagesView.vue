<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchNotifications, markNotificationRead, type NotificationItem } from '@/api/notification'

const loading = ref(false)
const list = ref<NotificationItem[]>([])
let timer: ReturnType<typeof setInterval> | undefined

async function load() {
  loading.value = true
  try {
    list.value = (await fetchNotifications()).data.data
  } finally {
    loading.value = false
  }
}

async function markRead(row: NotificationItem) {
  if (row.read) return
  await markNotificationRead(row.id)
  row.read = true
  ElMessage.success('已标记为已读')
}

onMounted(() => {
  load()
  timer = setInterval(load, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div v-loading="loading">
    <div class="crumb">公共 / 消息中心</div>
    <h1 class="title">消息中心</h1>
    <div class="page-card">
      <el-table :data="list" style="width:100%">
        <el-table-column prop="title" label="标题" width="180" />
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="createdAt" label="时间" width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.read ? 'info' : 'warning'" size="small">{{ row.read ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="!row.read" type="primary" link @click="markRead(row)">标记已读</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="!list.length" class="hint">暂无消息。业务流程推进后会在此显示通知。</p>
    </div>
  </div>
</template>

<style scoped>
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 30px; margin: 12px 0; }
.hint { color: #909399; margin-top: 16px; font-size: 13px; }
</style>
