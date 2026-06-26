<template>
  <view class="page">
    <view class="header"><text class="title">评估材料补充</text></view>
    <view class="card">
      <textarea class="textarea" v-model="remark" placeholder="补充说明" />
      <button class="btn" @click="submit">重新提交</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { fetchEvaluationDetail, supplementEvaluation } from '../../../api/request.js'

const projectId = ref(0)
const detail = ref(null)
const remark = ref('')

onLoad(async (q) => {
  projectId.value = Number(q.projectId)
  detail.value = await fetchEvaluationDetail(projectId.value)
})

async function submit() {
  try {
    await supplementEvaluation(projectId.value, { materials: [] })
    uni.showToast({ title: '已提交' })
    uni.navigateBack()
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
