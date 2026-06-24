<template>
  <view class="page">
    <view class="header">
      <text class="title">填报执行进度</text>
      <text class="tag">{{ detail?.statusLabel }}</text>
    </view>
    <view class="card" v-if="detail">
      <text class="row">编号：{{ detail.projectNo }}</text>
      <text class="row">当前进度：{{ progressPct }}%</text>
      <slider :value="progressPct" min="0" max="100" @change="onSlide" show-value />
      <textarea v-model="content" placeholder="进度说明" class="textarea" />
      <button class="btn" @click="submit">提交进度</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchDispatchDetail, reportTaskProgress } from '../../../api/request.js'

const detail = ref(null)
const taskId = ref(null)
const progressPct = ref(30)
const content = ref('')

onLoad(async (q) => {
  taskId.value = Number(q.taskId)
  if (q.projectId) {
    detail.value = await fetchDispatchDetail(Number(q.projectId))
    progressPct.value = detail.value.progressPct || 30
  }
})

function onSlide(e) {
  progressPct.value = e.detail.value
}

async function submit() {
  try {
    await reportTaskProgress(taskId.value, { progressPct: progressPct.value, content: content.value })
    uni.showToast({ title: '已提交' })
    if (progressPct.value >= 100) {
      uni.navigateBack()
    }
  } catch (e) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.tag { color: #1890ff; font-size: 24rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 12rpx; }
.textarea { width: 100%; min-height: 160rpx; border: 1px solid #eee; border-radius: 8rpx; padding: 16rpx; margin: 16rpx 0; box-sizing: border-box; }
.btn { background: #1890ff; color: #fff; }
</style>
