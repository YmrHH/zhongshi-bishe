<template>
  <view class="page">
    <view class="header">
      <text class="title">需求预览确认</text>
      <text class="tag">{{ detail?.statusLabel || '草稿' }}</text>
    </view>
    <view class="card">
      <input class="input" v-model="form.title" placeholder="项目名称 *" />
      <picker :range="types" @change="onTypeChange">
        <view class="input">{{ form.pilotType || '选择中试类型' }}</view>
      </picker>
      <input class="input" v-model="form.contactName" placeholder="联系人" />
      <input class="input" v-model="form.contactPhone" type="number" maxlength="11" placeholder="联系电话（11位手机号）" />
      <textarea class="textarea" v-model="form.content" placeholder="需求说明" />
      <button class="btn" @click="saveDraft">保存草稿</button>
      <button class="btn primary" @click="goSubmit">确认并提交</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createDemandProject, fetchDemandPreview, saveDemandDraft } from '../../../api/request.js'

const projectId = ref(null)
const detail = ref(null)
const types = ['工艺放大', '产品验证', '稳定性测试']
const form = ref({ title: '', content: '', pilotType: '', contactName: '', contactPhone: '', expectedDays: 30, materials: [] })

onLoad(async (query) => {
  if (query.projectId) {
    projectId.value = Number(query.projectId)
    detail.value = await fetchDemandPreview(projectId.value)
    form.value = { ...form.value, title: detail.value.title, content: detail.value.content, pilotType: detail.value.pilotType, contactName: detail.value.contactName, contactPhone: detail.value.contactPhone }
  }
})

function onTypeChange(e) {
  form.value.pilotType = types[e.detail.value]
}

async function ensureProject() {
  if (projectId.value) return projectId.value
  if (!form.value.title) {
    uni.showToast({ title: '请填写项目名称', icon: 'none' })
    throw new Error('no title')
  }
  const res = await createDemandProject(form.value)
  projectId.value = res.projectId
  return projectId.value
}

async function saveDraft() {
  if (form.value.contactPhone && !/^1\d{10}$/.test(form.value.contactPhone)) {
    uni.showToast({ title: '联系电话须为11位手机号', icon: 'none' })
    return
  }
  try {
    const id = await ensureProject()
    await saveDemandDraft(id, form.value)
    uni.showToast({ title: '已保存' })
  } catch (e) {
    if (e.message !== 'no title') uni.showToast({ title: e.message, icon: 'none' })
  }
}

async function goSubmit() {
  if (form.value.contactPhone && !/^1\d{10}$/.test(form.value.contactPhone)) {
    uni.showToast({ title: '联系电话须为11位手机号', icon: 'none' })
    return
  }
  try {
    const id = await ensureProject()
    await saveDemandDraft(id, form.value)
    uni.navigateTo({ url: `/pages/enterprise/demand/submit?projectId=${id}` })
  } catch (e) {
    if (e.message !== 'no title') uni.showToast({ title: e.message, icon: 'none' })
  }
}
</script>

<style scoped>
.page { padding: 24rpx; }
.header { margin-bottom: 24rpx; }
.title { font-size: 36rpx; font-weight: bold; display: block; }
.tag { color: #07c160; font-size: 24rpx; }
.card { background: #fff; border-radius: 12rpx; padding: 24rpx; }
.input, .textarea { background: #f5f5f5; border-radius: 8rpx; padding: 20rpx; margin-bottom: 16rpx; width: 100%; box-sizing: border-box; }
.textarea { min-height: 160rpx; }
.btn { margin-top: 12rpx; background: #f0f0f0; }
.btn.primary { background: #07c160; color: #fff; }
</style>
