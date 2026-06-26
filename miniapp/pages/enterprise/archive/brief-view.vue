<template>
  <view class="page">
    <view class="header"><text class="title">服务简报</text></view>
    <view class="card" v-if="brief">
      <text class="row title2">{{ brief.briefTitle }}</text>
      <text class="row">{{ brief.content }}</text>
      <text class="row muted">项目：{{ brief.projectNo }}</text>
      <text class="row muted">时间：{{ brief.createdAt }}</text>
    </view>
    <view v-else class="empty">简报尚未发布</view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchArchiveDetail, fetchBrief } from '../../../api/request.js'

const brief = ref(null)

onLoad(async (q) => {
  if (!q.projectId) return
  const detail = await fetchArchiveDetail(Number(q.projectId))
  if (detail.briefId) {
    brief.value = await fetchBrief(detail.briefId)
  }
})
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.title2 { font-size: 32rpx; font-weight: bold; margin-bottom: 16rpx; }
.row { display: block; margin-bottom: 12rpx; line-height: 1.6; }
.muted { color: #909399; font-size: 24rpx; }
.empty { text-align: center; color: #909399; margin-top: 80rpx; }
</style>
