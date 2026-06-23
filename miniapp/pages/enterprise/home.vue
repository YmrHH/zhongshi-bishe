<template>
  <view class="page">
    <view class="banner">
      <text class="welcome">企业移动办公</text>
      <text class="role">{{ profile?.roleLabel }} · {{ profile?.realName }}</text>
    </view>
    <view class="stats" v-if="dashboard">
      <view class="stat" v-for="(val, key) in dashboard.stats" :key="key">
        <text class="num">{{ val }}</text>
        <text class="label">{{ labels[key] || key }}</text>
      </view>
    </view>
    <view class="section">
      <text class="section-title">快捷入口</text>
      <view class="actions">
        <view class="action" @click="go('/pages/enterprise/demand/preview')">提交需求</view>
        <view class="action" @click="goTodo">查看进度</view>
        <view class="action" @click="goTodo">材料补充</view>
      </view>
    </view>
    <view class="section">
      <text class="section-title">待办事项</text>
      <view v-for="item in dashboard?.todos || []" :key="item.projectNo" class="todo" @click="openTodo(item)">
        <text class="todo-title">{{ item.title }}</text>
        <text class="todo-meta">{{ item.projectNo }} · {{ item.status }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { fetchDashboard } from '../../api/request.js'

const profile = ref(uni.getStorageSync('profile'))
const dashboard = ref(null)
const labels = { pending: '待处理', processing: '办理中', returned: '已退回', completed: '已完成' }

onMounted(async () => {
  if (!uni.getStorageSync('token')) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  dashboard.value = await fetchDashboard()
})

function go(url) {
  uni.navigateTo({ url })
}

function goTodo() {
  const item = dashboard.value?.todos?.[0]
  if (item?.projectId) {
    openTodo(item)
  } else {
    go('/pages/enterprise/demand/preview')
  }
}

function openTodo(item) {
  const map = {
    '/enterprise/demand/preview': '/pages/enterprise/demand/preview',
    '/enterprise/demand/supplement': '/pages/enterprise/demand/supplement',
    '/enterprise/demand/receipt': '/pages/enterprise/demand/receipt',
    '/enterprise/demand/progress': '/pages/enterprise/demand/progress',
    '/enterprise/evaluation/condition-supplement': '/pages/enterprise/evaluation/condition-supplement',
    '/enterprise/evaluation/eval-supplement': '/pages/enterprise/evaluation/eval-supplement',
    '/enterprise/evaluation/conclusion-receipt': '/pages/enterprise/evaluation/conclusion-receipt',
    '/enterprise/evaluation/feedback': '/pages/enterprise/evaluation/feedback',
    '/enterprise/evaluation/conclusion-detail': '/pages/enterprise/evaluation/conclusion-detail'
  }
  const path = map[item.route] || '/pages/enterprise/demand/progress'
  uni.navigateTo({ url: `${path}?projectId=${item.projectId}` })
}
</script>

<style scoped>
.page { padding: 24rpx; }
.banner { background: #07c160; color: #fff; border-radius: 16rpx; padding: 32rpx; margin-bottom: 24rpx; }
.welcome { font-size: 36rpx; font-weight: bold; display: block; }
.role { font-size: 26rpx; opacity: 0.9; margin-top: 8rpx; display: block; }
.stats { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin-bottom: 24rpx; }
.stat { background: #fff; border-radius: 12rpx; padding: 24rpx; text-align: center; }
.num { display: block; font-size: 40rpx; color: #07c160; font-weight: bold; }
.label { font-size: 24rpx; color: #888; }
.section { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 24rpx; }
.section-title { font-weight: bold; margin-bottom: 16rpx; display: block; }
.actions { display: flex; gap: 16rpx; }
.action { flex: 1; background: #f0f9f4; color: #07c160; text-align: center; padding: 20rpx; border-radius: 8rpx; font-size: 26rpx; }
.todo { padding: 16rpx 0; border-bottom: 1px solid #f0f0f0; }
.todo-title { display: block; font-size: 28rpx; }
.todo-meta { font-size: 24rpx; color: #999; }
</style>
