<template>
  <view class="page">
    <view class="header">
      <text class="title">执行进度查看</text>
      <text class="tag">{{ detail?.statusLabel }}</text>
    </view>

    <view class="tip success" v-if="detail?.status === 'PROGRESS_ACKED'">
      您已确认知悉，本环节待办已结束；有新进度更新时将再次通知。
    </view>
    <view class="tip" v-else-if="detail?.stage === 'DISPATCH'">
      执行进度由技术人员填报，查看后请点击「确认知悉」完成办理。
    </view>
    <view class="tip success" v-else-if="detail?.stage === 'FEEDBACK'">
      中试执行已完成，已进入结果反馈阶段，请返回首页关注待办。
    </view>

    <view class="card" v-if="detail">
      <text class="row">编号：{{ detail.projectNo }}</text>
      <text class="row">项目：{{ detail.title }}</text>
      <text class="row">资源：{{ detail.resourceName || '—' }}</text>
      <text class="row">技术人员：{{ detail.technicianName || '—' }}</text>
      <text class="row">进度：{{ detail.progressPct || 0 }}%</text>
    </view>

    <view class="card" v-if="detail?.progressRecords?.length">
      <text class="section">进度记录</text>
      <view v-for="(r, i) in detail.progressRecords" :key="i" class="record">
        <text>{{ r.reportTime }} · {{ r.progressPct }}%</text>
        <text class="sub">{{ r.content }}</text>
      </view>
    </view>
    <view class="card empty" v-else-if="detail">
      <text class="sub">技术人员尚未填报进度</text>
    </view>

    <view class="card" v-if="canAcknowledge">
      <textarea v-model="remark" placeholder="知悉说明（选填）" class="textarea" />
      <button class="btn primary" @click="onAcknowledge">确认知悉</button>
    </view>

    <view class="actions">
      <button class="btn" @click="onRefresh">刷新进度</button>
      <button class="btn" @click="goHome">返回首页</button>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { acknowledgeDispatchProgress, fetchDispatchDetail } from '../../../api/request.js'

const detail = ref(null)
const remark = ref('')
let projectId = null

const canAcknowledge = computed(() =>
  detail.value?.stage === 'DISPATCH'
  && ['EXECUTING', 'CONFIRMED', 'EXEC_DONE'].includes(detail.value?.status)
)

onLoad(async (q) => {
  projectId = Number(q.projectId)
  if (projectId) {
    detail.value = await fetchDispatchDetail(projectId)
  }
})

async function onAcknowledge() {
  try {
    await acknowledgeDispatchProgress(projectId, { remark: remark.value })
    uni.showToast({ title: '已确认知悉' })
    uni.reLaunch({ url: '/pages/enterprise/home' })
  } catch (e) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}

async function onRefresh() {
  if (projectId) {
    detail.value = await fetchDispatchDetail(projectId)
    uni.showToast({ title: '已刷新' })
  }
}

function goHome() {
  uni.reLaunch({ url: '/pages/enterprise/home' })
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.tag { color: #1890ff; font-size: 24rpx; }
.tip { background: #ecf5ff; color: #1890ff; padding: 20rpx; border-radius: 12rpx; margin: 24rpx 0; font-size: 26rpx; }
.tip.success { background: #f0f9eb; color: #67c23a; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 24rpx; }
.row { display: block; margin-bottom: 12rpx; }
.section { font-weight: bold; display: block; margin-bottom: 12rpx; }
.record { padding: 12rpx 0; border-bottom: 1px solid #f0f0f0; }
.sub { display: block; color: #888; font-size: 24rpx; }
.empty { text-align: center; }
.textarea { width: 100%; min-height: 120rpx; border: 1px solid #eee; border-radius: 8rpx; padding: 16rpx; margin-bottom: 16rpx; box-sizing: border-box; }
.actions { display: flex; gap: 16rpx; }
.btn { flex: 1; background: #f5f5f5; color: #333; font-size: 28rpx; }
.btn.primary { background: #07c160; color: #fff; margin-bottom: 16rpx; }
</style>
