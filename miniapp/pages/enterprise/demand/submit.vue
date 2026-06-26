<template>
  <view class="page">
    <view class="header"><text class="title">需求填报</text></view>
    <view class="card">
      <text class="label">项目编号：{{ detail?.projectNo }}</text>
      <textarea class="textarea" v-model="content" placeholder="请填写需求内容 *" />
      <button class="btn primary" @click="onSubmit">提交需求</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchDemandPreview, submitDemand } from '../../../api/request.js'

const projectId = ref(0)
const detail = ref(null)
const content = ref('')

onLoad(async (query) => {
  projectId.value = Number(query.projectId)
  detail.value = await fetchDemandPreview(projectId.value)
  content.value = detail.value.content || ''
})

async function onSubmit() {
  if (!content.value) {
    uni.showToast({ title: '请填写需求内容', icon: 'none' })
    return
  }
  try {
    await submitDemand(projectId.value, { ...detail.value, title: detail.value.title, content: content.value, materials: detail.value.materials || [] })
    uni.showToast({ title: '提交成功' })
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
.label { display: block; margin-bottom: 16rpx; color: #666; }
.textarea { background: #f5f5f5; border-radius: 8rpx; padding: 20rpx; min-height: 200rpx; width: 100%; box-sizing: border-box; }
.btn.primary { margin-top: 24rpx; background: #07c160; color: #fff; }
</style>
