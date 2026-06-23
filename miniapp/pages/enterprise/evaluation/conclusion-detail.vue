<template>
  <view class="page">
    <view class="header">
      <text class="title">评估结论详情</text>
      <text class="tag">{{ detail?.statusLabel }}</text>
    </view>
    <view class="card">
      <text class="row">编号：{{ detail?.projectNo }}</text>
      <text class="row">结论：{{ detail?.conclusion }}</text>
      <text class="row">意见：{{ detail?.conclusionOpinion || '—' }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from '@dcloudio/uni-app'
import { fetchEvaluationDetail } from '../../../api/request.js'

const detail = ref(null)

onLoad(async (q) => {
  detail.value = await fetchEvaluationDetail(Number(q.projectId))
})
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.tag { color: #07c160; font-size: 24rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 12rpx; }
</style>
