<template>
  <view class="page">
    <view class="header">
      <text class="title">{{ pageTitle }}</text>
      <text class="tag">{{ detail?.statusLabel }}</text>
    </view>

    <view v-if="detail?.moduleSteps?.length" class="card steps-card">
      <text class="section">{{ detail.moduleName }}办理进度</text>
      <view v-for="(step, idx) in detail.moduleSteps" :key="idx" class="step-row">
        <text :class="['dot', step.status]" />
        <text :class="['step-name', step.status]">{{ step.node }}</text>
      </view>
    </view>

    <view class="card">
      <text class="row">编号：{{ detail?.projectNo }}</text>
      <text class="row">模块：{{ detail?.moduleName }}</text>
      <text class="row">环节：{{ detail?.currentNode }}</text>
      <text class="row">名称：{{ detail?.title }}</text>
      <text v-if="detail?.content" class="row">内容：{{ detail.content }}</text>
      <text v-if="detail?.acceptOpinion || detail?.rejectReason" class="row">
        意见：{{ detail?.acceptOpinion || detail?.rejectReason }}
      </text>
    </view>

    <view v-if="detail?.status === 'ACCEPTED'" class="card action-card">
      <text class="section">受理回执签收</text>
      <text class="row">受理意见：{{ demandDetail?.acceptOpinion || '同意受理' }}</text>
      <button class="btn primary" @click="onReceipt">确认签收</button>
    </view>

    <view v-if="detail?.status === 'RETURNED'" class="card action-card">
      <text class="section">补充材料</text>
      <view v-if="detail.rejectReason" class="alert">退回原因：{{ detail.rejectReason }}</view>
      <textarea class="textarea" v-model="supplementContent" placeholder="补充说明" />
      <button class="btn primary" @click="onSupplement">重新提交</button>
    </view>

    <view class="card">
      <text class="section">流程日志</text>
      <view v-for="(log, idx) in detail?.logs || []" :key="idx" class="log">
        <text>{{ log.remark }}</text>
        <text class="time">{{ log.time }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  fetchDemandPreview,
  fetchProjectProgress,
  receiptDemand,
  supplementDemand
} from '../../../api/request.js'

const detail = ref(null)
const demandDetail = ref(null)
const pageTitle = ref('项目进度')
const projectId = ref(0)
const supplementContent = ref('')

async function reload() {
  detail.value = await fetchProjectProgress(projectId.value)
  pageTitle.value = detail.value.stage === 'DEMAND' ? '受理进度' : '项目进度'
  if (detail.value.stage === 'DEMAND' && ['ACCEPTED', 'RETURNED'].includes(detail.value.status)) {
    demandDetail.value = await fetchDemandPreview(projectId.value)
    if (detail.value.status === 'RETURNED') {
      supplementContent.value = demandDetail.value.content || ''
    }
  } else {
    demandDetail.value = null
  }
}

onLoad(async (query) => {
  if (!query.projectId) {
    uni.redirectTo({ url: '/pages/enterprise/demand/projects' })
    return
  }
  projectId.value = Number(query.projectId)
  try {
    await reload()
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
})

async function onReceipt() {
  try {
    await receiptDemand(projectId.value)
    uni.showToast({ title: '已签收' })
    await reload()
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}

async function onSupplement() {
  const materials = demandDetail.value?.materials || []
  if (!materials.length) {
    uni.showToast({ title: '请在 Web 端上传材料后提交', icon: 'none' })
    return
  }
  try {
    await supplementDemand(projectId.value, { content: supplementContent.value, materials })
    uni.showToast({ title: '已重新提交' })
    await reload()
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.tag { color: #07c160; font-size: 24rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-top: 24rpx; }
.row { display: block; margin-bottom: 12rpx; font-size: 28rpx; }
.section { font-weight: bold; display: block; margin-bottom: 16rpx; }
.log { padding: 12rpx 0; border-bottom: 1px solid #f0f0f0; }
.time { display: block; color: #999; font-size: 22rpx; margin-top: 4rpx; }
.step-row { display: flex; align-items: center; margin-bottom: 12rpx; }
.dot { width: 16rpx; height: 16rpx; border-radius: 50%; margin-right: 12rpx; background: #dcdfe6; }
.dot.done { background: #67c23a; }
.dot.active { background: #1890ff; }
.step-name { font-size: 26rpx; color: #606266; }
.step-name.active { color: #1890ff; font-weight: 600; }
.step-name.done { color: #303133; }
.action-card .btn { margin-top: 16rpx; }
.btn.primary { background: #1890ff; color: #fff; }
.alert { background: #fff7e6; color: #d48806; padding: 20rpx; border-radius: 8rpx; margin-bottom: 16rpx; font-size: 26rpx; }
.textarea { background: #f5f5f5; border-radius: 8rpx; padding: 20rpx; min-height: 200rpx; width: 100%; box-sizing: border-box; font-size: 28rpx; }
</style>
