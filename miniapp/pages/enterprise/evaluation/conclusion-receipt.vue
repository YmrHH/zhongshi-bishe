<template>
  <view class="page">
    <view class="header"><text class="title">评估结论签收</text></view>
    <view class="card">
      <text class="row">{{ detail?.conclusion }}</text>
      <button class="btn" @click="submit">确认签收</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from '@dcloudio/uni-app'
import { fetchEvaluationDetail, receiptEvaluation } from '../../../api/request.js'

const projectId = ref(0)
const detail = ref(null)

onLoad(async (q) => {
  projectId.value = Number(q.projectId)
  detail.value = await fetchEvaluationDetail(projectId.value)
})

async function submit() {
  try {
    await receiptEvaluation(projectId.value)
    uni.showToast({ title: '签收成功' })
    uni.redirectTo({ url: `/pages/enterprise/evaluation/feedback?projectId=${projectId.value}` })
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 20rpx; }
.btn { background: #07c160; color: #fff; }
</style>
