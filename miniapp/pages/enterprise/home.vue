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
        <view class="action" @click="goTodo">待办办理</view>
        <view class="action" @click="go('/pages/enterprise/evaluation/conclusion-detail')">评估详情</view>
      </view>
    </view>
    <view class="section">
      <view class="section-head">
        <text class="section-title">待办事项</text>
        <text class="refresh" @click="loadData">刷新</text>
      </view>
      <view v-if="loading" class="hint">加载中...</view>
      <view v-else-if="!(dashboard?.todos?.length)" class="hint">暂无待办，可发起新需求或下拉刷新</view>
      <view v-for="item in dashboard?.todos || []" :key="item.projectNo + item.time" class="todo" @click="handleTodo(item)">
        <text class="todo-title">{{ item.title }}</text>
        <text class="todo-meta">{{ item.module }} · {{ item.status }}</text>
        <text class="todo-meta">{{ item.projectNo }} · {{ item.time }}</text>
      </view>
    </view>
    <button class="logout" size="mini" @click="logout">退出登录</button>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app'
import { fetchDashboard } from '../../api/request.js'
import { openTodo, requireLogin, logout as doLogout } from '../../utils/nav.js'

const profile = ref(uni.getStorageSync('profile'))
const dashboard = ref(null)
const loading = ref(false)
const labels = { pending: '待处理', processing: '办理中', returned: '已退回', completed: '已完成' }

async function loadData() {
  loading.value = true
  try {
    dashboard.value = await fetchDashboard()
  } catch (e) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
    uni.stopPullDownRefresh()
  }
}

onShow(() => {
  if (!requireLogin()) return
  profile.value = uni.getStorageSync('profile')
  loadData()
})

onPullDownRefresh(() => {
  loadData()
})

function go(url) {
  uni.navigateTo({ url })
}

function goTodo() {
  const item = dashboard.value?.todos?.[0]
  if (item) handleTodo(item)
  else go('/pages/enterprise/demand/preview')
}

function handleTodo(item) {
  openTodo(item)
}

function logout() {
  doLogout()
}
</script>

<style scoped>
.page { padding: 24rpx; padding-bottom: 48rpx; }
.banner { background: #07c160; color: #fff; border-radius: 16rpx; padding: 32rpx; margin-bottom: 24rpx; }
.welcome { font-size: 36rpx; font-weight: bold; display: block; }
.role { font-size: 26rpx; opacity: 0.9; margin-top: 8rpx; display: block; }
.stats { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16rpx; margin-bottom: 24rpx; }
.stat { background: #fff; border-radius: 12rpx; padding: 24rpx; text-align: center; }
.num { display: block; font-size: 40rpx; color: #07c160; font-weight: bold; }
.label { font-size: 24rpx; color: #888; }
.section { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 24rpx; }
.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.section-title { font-weight: bold; }
.refresh { color: #07c160; font-size: 26rpx; }
.actions { display: flex; gap: 16rpx; }
.action { flex: 1; background: #f0f9f4; color: #07c160; text-align: center; padding: 20rpx; border-radius: 8rpx; font-size: 26rpx; }
.todo { padding: 16rpx 0; border-bottom: 1px solid #f0f0f0; }
.todo-title { display: block; font-size: 28rpx; }
.todo-meta { font-size: 24rpx; color: #999; display: block; }
.hint { color: #999; font-size: 26rpx; padding: 16rpx 0; }
.logout { margin-top: 16rpx; }
</style>
