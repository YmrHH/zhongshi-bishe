<template>
  <view class="page">
    <view class="header">
      <text class="title">我的需求项目</text>
      <text class="hint">归档后仍可查看全流程进度</text>
    </view>
    <button class="btn primary" @click="goNew">发起新需求</button>

    <view class="filter-bar">
      <view class="filter-row">
        <text class="label">阶段</text>
        <picker :range="STAGE_LABELS" :value="stageIndex" @change="onStagePick">
          <view class="picker">{{ STAGE_LABELS[stageIndex] }} ▼</view>
        </picker>
      </view>
      <view class="filter-row search-row">
        <input
          class="search-input"
          v-model="keywordInput"
          placeholder="编号或项目名称"
          confirm-type="search"
          @confirm="onSearch"
        />
        <button class="btn sm" @click="onSearch">搜索</button>
        <button class="btn sm ghost" @click="onReset">重置</button>
      </view>
    </view>

    <view v-if="loading && !projects.length" class="empty">加载中...</view>
    <view v-else-if="!projects.length" class="empty">{{ emptyText }}</view>
    <view v-for="item in projects" :key="item.projectId" class="card" @click="goProgress(item)">
      <text class="no">{{ item.projectNo }}</text>
      <text class="name">{{ item.title }}</text>
      <text class="meta">{{ item.statusLabel }} · {{ item.currentNode }}</text>
      <text class="link">{{ item.stage === 'DEMAND' ? '查看受理进度' : '查看项目进度' }} ›</text>
    </view>
    <button
      v-if="projects.length < total"
      class="btn more"
      :loading="loading"
      @click="loadMore"
    >加载更多（{{ projects.length }}/{{ total }}）</button>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { fetchEnterpriseDemandProjects } from '../../../api/request.js'
import { requireLogin } from '../../../utils/nav.js'

const STAGE_OPTIONS = [
  { value: 'ALL', label: '全部' },
  { value: 'DEMAND', label: '需求办理中' },
  { value: 'EVALUATION', label: '评估中' },
  { value: 'DISPATCH', label: '调度执行中' },
  { value: 'FEEDBACK', label: '反馈复核中' },
  { value: 'ARCHIVE', label: '档案统计中' },
  { value: 'CLOSED', label: '已结案' }
]
const STAGE_LABELS = STAGE_OPTIONS.map(o => o.label)

const loading = ref(false)
const projects = ref([])
const page = ref(1)
const total = ref(0)
const pageSize = 20
const stageIndex = ref(0)
const keywordInput = ref('')
const activeKeyword = ref('')

const emptyText = computed(() => {
  if (activeKeyword.value) return '未找到匹配的项目'
  if (STAGE_OPTIONS[stageIndex.value].value === 'ALL') return '暂无已提交项目'
  return '该阶段暂无项目'
})

onShow(async () => {
  if (!requireLogin()) return
  page.value = 1
  await loadProjects(true)
})

async function loadProjects(reset = false) {
  loading.value = true
  try {
    const stageFilter = STAGE_OPTIONS[stageIndex.value].value
    const data = await fetchEnterpriseDemandProjects(
      page.value,
      pageSize,
      stageFilter,
      activeKeyword.value
    )
    total.value = data.total
    projects.value = reset ? data.records : [...projects.value, ...data.records]
  } catch (e) {
    uni.showToast({ title: e.message, icon: 'none' })
  } finally {
    loading.value = false
  }
}

function onStagePick(e) {
  stageIndex.value = Number(e.detail.value)
  page.value = 1
  loadProjects(true)
}

function onSearch() {
  activeKeyword.value = keywordInput.value.trim()
  page.value = 1
  loadProjects(true)
}

function onReset() {
  keywordInput.value = ''
  activeKeyword.value = ''
  page.value = 1
  loadProjects(true)
}

function loadMore() {
  if (projects.value.length >= total.value) return
  page.value += 1
  loadProjects(false)
}

function goNew() {
  uni.navigateTo({ url: '/pages/enterprise/demand/preview' })
}

function goProgress(item) {
  uni.navigateTo({ url: `/pages/enterprise/demand/progress?projectId=${item.projectId}` })
}
</script>

<style scoped>
.page { padding: 24rpx; }
.header { margin-bottom: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.hint { color: #909399; font-size: 24rpx; }
.btn { margin-bottom: 16rpx; }
.primary { background: #1890ff; color: #fff; }
.filter-bar { background: #fff; border-radius: 12rpx; padding: 20rpx; margin-bottom: 16rpx; }
.filter-row { display: flex; align-items: center; margin-bottom: 16rpx; }
.filter-row:last-child { margin-bottom: 0; }
.label { font-size: 26rpx; color: #606266; margin-right: 16rpx; flex-shrink: 0; }
.picker { font-size: 28rpx; color: #303133; padding: 12rpx 20rpx; background: #f5f7fa; border-radius: 8rpx; }
.search-row { gap: 12rpx; }
.search-input {
  flex: 1;
  background: #f5f7fa;
  border-radius: 8rpx;
  padding: 12rpx 20rpx;
  font-size: 28rpx;
}
.btn.sm { margin: 0; padding: 0 24rpx; font-size: 26rpx; line-height: 64rpx; height: 64rpx; background: #1890ff; color: #fff; }
.btn.ghost { background: #fff; color: #606266; border: 1px solid #dcdfe6; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; margin-bottom: 16rpx; }
.no { color: #909399; font-size: 24rpx; display: block; }
.name { font-size: 30rpx; font-weight: 600; display: block; margin: 8rpx 0; }
.meta { color: #606266; font-size: 24rpx; display: block; }
.link { color: #1890ff; font-size: 26rpx; margin-top: 12rpx; display: block; }
.empty { text-align: center; color: #909399; padding: 48rpx; }
.more { background: #fff; color: #1890ff; border: 1px solid #1890ff; }
</style>
