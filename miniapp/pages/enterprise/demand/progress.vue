<template>
  <view class="page">
    <view class="header">
      <text class="title">受理进度</text>
      <text class="tag">{{ detail?.statusLabel }}</text>
    </view>
    <view class="card">
      <text class="row">编号：{{ detail?.projectNo }}</text>
      <text class="row">环节：{{ detail?.currentNode }}</text>
      <text class="row">名称：{{ detail?.title }}</text>
      <text class="row">内容：{{ detail?.content }}</text>
    </view>
    <view class="card">
      <text class="section">流程日志</text>
      <view v-for="(log, idx) in detail?.logs || []" :key="idx" class="log">
        <text>{{ log.remark }}</text>
        <text class="time">{{ log.time }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchDemandProgress } from '../../../api/request.js'

const detail = ref(null)

onLoad(async (query) => {
  detail.value = await fetchDemandProgress(Number(query.projectId))
})
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.tag { color: #07c160; font-size: 24rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 12rpx; }
.section { font-weight: bold; display: block; margin-bottom: 16rpx; }
.log { padding: 12rpx 0; border-bottom: 1px solid #f0f0f0; }
.time { display: block; color: #999; font-size: 22rpx; margin-top: 4rpx; }
</style>
