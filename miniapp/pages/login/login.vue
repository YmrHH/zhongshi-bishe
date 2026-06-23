<template>
  <view class="page">
    <view class="header">
      <text class="title">中试服务管理</text>
      <text class="sub">广州生产力促进中心</text>
    </view>
    <view class="form">
      <input v-model="username" class="input" placeholder="用户名" />
      <input v-model="password" class="input" password placeholder="密码" />
      <button class="btn" type="primary" :loading="loading" @click="onLogin">登录</button>
    </view>
    <view class="tips">
      <text>企业：enterprise / 123456</text>
      <text>技术：technician / 123456</text>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { login } from '../../api/request.js'

const username = ref('enterprise')
const password = ref('123456')
const loading = ref(false)

async function onLogin() {
  loading.value = true
  try {
    const data = await login(username.value, password.value)
    uni.setStorageSync('token', data.token)
    uni.setStorageSync('profile', data)
    if (data.role === 'TECHNICIAN') {
      uni.reLaunch({ url: '/pages/technician/home' })
    } else if (data.role === 'ENTERPRISE') {
      uni.reLaunch({ url: '/pages/enterprise/home' })
    } else {
      uni.showToast({ title: '请使用 Web 中心管理端', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.page { padding: 48rpx; }
.header { margin-bottom: 48rpx; }
.title { display: block; font-size: 40rpx; font-weight: bold; color: #07c160; }
.sub { display: block; margin-top: 12rpx; color: #888; font-size: 28rpx; }
.input {
  background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 24rpx;
  border: 1px solid #eee;
}
.btn { background: #07c160; color: #fff; border-radius: 12rpx; }
.tips { margin-top: 32rpx; color: #999; font-size: 24rpx; display: flex; flex-direction: column; gap: 8rpx; }
</style>
