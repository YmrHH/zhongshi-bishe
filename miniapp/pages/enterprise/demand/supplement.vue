<template>
  <view class="page">
    <view class="header"><text class="title">材料补充</text></view>
    <view v-if="detail?.rejectReason" class="alert">退回原因：{{ detail.rejectReason }}</view>
    <view class="card">
      <textarea class="textarea" v-model="content" placeholder="补充说明" />
      <button class="btn primary" @click="onSubmit">重新提交</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchDemandPreview, supplementDemand } from '../../../api/request.js'

const projectId = ref(0)
const detail = ref(null)
const content = ref('')

onLoad(async (query) => {
  projectId.value = Number(query.projectId)
  detail.value = await fetchDemandPreview(projectId.value)
  content.value = detail.value.content || ''
})

async function onSubmit() {
  try {
    await supplementDemand(projectId.value, { content: content.value, materials: detail.value.materials || [] })
    uni.showToast({ title: '已重新提交' })
    uni.redirectTo({ url: `/pages/enterprise/demand/progress?projectId=${projectId.value}` })
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; }
.alert { background: #fff7e6; color: #d48806; padding: 20rpx; border-radius: 8rpx; margin: 16rpx 0; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; }
.textarea { background: #f5f5f5; border-radius: 8rpx; padding: 20rpx; min-height: 200rpx; width: 100%; box-sizing: border-box; }
.btn.primary { margin-top: 24rpx; background: #07c160; color: #fff; }
</style>
