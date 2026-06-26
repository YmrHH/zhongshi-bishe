<template>
  <view class="page">
    <view class="header"><text class="title">复核意见反馈</text></view>
    <view class="card" v-if="detail">
      <text class="row">复核意见：{{ detail.reviewOpinion }}</text>
      <textarea v-model="remark" placeholder="请填写意见" class="textarea" />
      <button class="btn" @click="submit">提交反馈</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchFeedbackDetail, reviewFeedbackOpinion } from '../../../api/request.js'

const detail = ref(null)
const remark = ref('')

onLoad(async (q) => {
  if (q.projectId) detail.value = await fetchFeedbackDetail(Number(q.projectId))
})

async function submit() {
  try {
    await reviewFeedbackOpinion(detail.value.projectId, { remark: remark.value, content: remark.value })
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
.textarea { width: 100%; min-height: 160rpx; border: 1px solid #eee; border-radius: 8rpx; padding: 16rpx; margin: 16rpx 0; box-sizing: border-box; }
.btn { background: #1890ff; color: #fff; }
</style>
