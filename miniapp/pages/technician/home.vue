<template>
  <view class="page">
    <view class="banner">
      <text class="welcome">技术人员移动办公</text>
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
        <view class="action">接收任务</view>
        <view class="action">进度填报</view>
        <view class="action">结果提交</view>
      </view>
    </view>
    <view class="section">
      <text class="section-title">我的任务</text>
      <view v-for="item in dashboard?.todos || []" :key="item.projectNo" class="todo">
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
const labels = { pending: '待接收', processing: '执行中', returned: '已退回', completed: '已完成' }

onMounted(async () => {
  if (!uni.getStorageSync('token')) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  dashboard.value = await fetchDashboard()
})
</script>

<style scoped>
.page { padding: 24rpx; }
.banner { background: #1890ff; color: #fff; border-radius: 16rpx; padding: 32rpx; margin-bottom: 24rpx; }
.welcome { font-size: 36rpx; font-weight: bold; display: block; }
.role { font-size: 26rpx; opacity: 0.9; margin-top: 8rpx; display: block; }
.stats { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin-bottom: 24rpx; }
.stat { background: #fff; border-radius: 12rpx; padding: 24rpx; text-align: center; }
.num { display: block; font-size: 40rpx; color: #1890ff; font-weight: bold; }
.label { font-size: 24rpx; color: #888; }
.section { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 24rpx; }
.section-title { font-weight: bold; margin-bottom: 16rpx; display: block; }
.actions { display: flex; gap: 16rpx; }
.action { flex: 1; background: #ecf5ff; color: #1890ff; text-align: center; padding: 20rpx; border-radius: 8rpx; font-size: 26rpx; }
.todo { padding: 16rpx 0; border-bottom: 1px solid #f0f0f0; }
.todo-title { display: block; font-size: 28rpx; }
.todo-meta { font-size: 24rpx; color: #999; }
</style>
