<script setup>
import { onLoad } from '@dcloudio/uni-app'

onLoad((query) => {
  const q = query.projectId ? `?projectId=${query.projectId}` : ''
  uni.redirectTo({ url: `/pages/enterprise/demand/progress${q}` })
})
</script>

<template>
  <view />
</template>
