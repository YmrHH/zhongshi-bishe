<template>
  <view class="page">
    <view class="header">
      <text class="title">任务签收确认</text>
      <text class="tag">{{ detail?.statusLabel }}</text>
    </view>
    <view class="card" v-if="detail">
      <text class="row">编号：{{ detail.projectNo }}</text>
      <text class="row">资源：{{ detail.resourceName || '—' }}</text>
      <button class="btn" @click="submit">确认签收</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { confirmTask, fetchDispatchDetail } from '../../../api/request.js'

const detail = ref(null)
const taskId = ref(null)

onLoad(async (q) => {
  taskId.value = Number(q.taskId)
  if (q.projectId) {
    detail.value = await fetchDispatchDetail(Number(q.projectId))
  }
})

async function submit() {
  try {
    await confirmTask(taskId.value)
    uni.showToast({ title: '已签收' })
    uni.navigateTo({ url: `/pages/technician/dispatch/progress-report?projectId=${detail.value.projectId}&taskId=${taskId.value}` })
  } catch (e) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.tag { color: #07c160; font-size: 24rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 12rpx; }
.btn { margin-top: 24rpx; background: #1890ff; color: #fff; }
</style>
