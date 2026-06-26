<template>
  <view class="page">
    <view class="header"><text class="title">试验结果提交</text></view>
    <view class="card" v-if="detail">
      <text class="row">编号：{{ detail.projectNo }}</text>
      <textarea v-model="content" placeholder="试验结果" class="textarea" />
      <button class="btn" @click="submit">提交</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchFeedbackDetail, submitFeedback } from '../../../api/request.js'

const detail = ref(null)
const content = ref('')

onLoad(async (q) => {
  if (q.projectId) {
    detail.value = await fetchFeedbackDetail(Number(q.projectId))
    content.value = detail.value.reportContent || ''
  }
})

async function submit() {
  try {
    await submitFeedback(detail.value.projectId, { content: content.value })
    uni.showToast({ title: '已提交' })
    uni.navigateBack()
  } catch (e) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 12rpx; }
.textarea { width: 100%; min-height: 200rpx; border: 1px solid #eee; border-radius: 8rpx; padding: 16rpx; margin: 16rpx 0; box-sizing: border-box; }
.btn { background: #07c160; color: #fff; }
</style>
