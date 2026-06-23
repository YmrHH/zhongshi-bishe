<template>
  <view class="page">
    <view class="header"><text class="title">评估意见反馈</text></view>
    <view class="card">
      <textarea class="textarea" v-model="content" placeholder="反馈意见" />
      <button class="btn" @click="submit">提交</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from '@dcloudio/uni-app'
import { feedbackEvaluation } from '../../../api/request.js'

const projectId = ref(0)
const content = ref('')

onLoad((q) => { projectId.value = Number(q.projectId) })

async function submit() {
  try {
    await feedbackEvaluation(projectId.value, { content: content.value })
    uni.showToast({ title: '已反馈' })
    uni.reLaunch({ url: '/pages/enterprise/home' })
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.textarea { background: #f5f5f5; padding: 20rpx; min-height: 160rpx; width: 100%; box-sizing: border-box; }
.btn { margin-top: 20rpx; background: #07c160; color: #fff; }
</style>
