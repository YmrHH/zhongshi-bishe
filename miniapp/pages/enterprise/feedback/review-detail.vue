<template>
  <view class="page">
    <view class="header"><text class="title">复核结果详情</text></view>
    <view class="card" v-if="detail">
      <text class="row">编号：{{ detail.projectNo }}</text>
      <text class="row">复核意见：{{ detail.reviewOpinion || '—' }}</text>
      <text class="row">试验结果：{{ detail.reportContent }}</text>
      <text class="row">状态：{{ detail.statusLabel }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchReviewDetail } from '../../../api/request.js'

const detail = ref(null)

onLoad(async (q) => {
  if (q.projectId) detail.value = await fetchReviewDetail(Number(q.projectId))
})
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 12rpx; }
</style>
