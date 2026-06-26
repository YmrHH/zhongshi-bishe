<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = ref('无权访问该页面')

onMounted(() => {
  if (route.query.from) {
    message.value = `您当前以「${userStore.profile?.roleLabel}」登录，无法访问 ${route.query.from} 门户下的该功能。`
  }
})

function goHome() {
  router.push(userStore.profile?.homePath || '/login')
}
</script>

<template>
  <div class="page-card" style="text-align:center;padding:48px">
    <h1>403</h1>
    <p>{{ message }}</p>
    <el-button type="primary" @click="goHome">返回我的首页</el-button>
  </div>
</template>
