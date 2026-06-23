<template>
  <view class="page">
    <view class="header"><text class="title">受理回执签收</text></view>
    <view class="card">
      <text class="row">项目编号：{{ detail?.projectNo }}</text>
      <text class="row">项目名称：{{ detail?.title }}</text>
      <text class="row">受理意见：{{ detail?.acceptOpinion || '同意受理' }}</text>
      <button class="btn primary" @click="onReceipt">确认签收</button>
    </view>
  </view>
</template>

<script setup>
import { ref, onLoad } from '@dcloudio/uni-app'
import { fetchDemandPreview, receiptDemand } from '../../../api/request.js'

const projectId = ref(0)
const detail = ref(null)

onLoad(async (query) => {
  projectId.value = Number(query.projectId)
  detail.value = await fetchDemandPreview(projectId.value)
})

async function onReceipt() {
  try {
    await receiptDemand(projectId.value)
    uni.showToast({ title: '签收成功' })
    uni.redirectTo({ url: `/pages/enterprise/demand/progress?projectId=${projectId.value}` })
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 16rpx; color: #333; }
.btn.primary { margin-top: 24rpx; background: #07c160; color: #fff; }
</style>
