<template>
  <view class="page">
    <view class="header"><text class="title">试验数据校验</text></view>
    <view class="card" v-if="detail">
      <text class="row">{{ detail.reportContent }}</text>
      <textarea v-model="remark" placeholder="校验意见" class="textarea" />
      <button class="btn" @click="submit(true)">校验通过</button>
      <button class="btn warn" @click="submit(false)">退回修改</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchFeedbackDetail, validateFeedback } from '../../../api/request.js'

const detail = ref(null)
const remark = ref('')

onLoad(async (q) => {
  if (q.projectId) detail.value = await fetchFeedbackDetail(Number(q.projectId))
})

async function submit(passed) {
  try {
    await validateFeedback(detail.value.projectId, { passed, remark: remark.value })
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
.textarea { width: 100%; min-height: 120rpx; border: 1px solid #eee; border-radius: 8rpx; padding: 16rpx; margin: 16rpx 0; box-sizing: border-box; }
.btn { background: #07c160; color: #fff; margin-bottom: 12rpx; }
.warn { background: #e6a23c; }
</style>
