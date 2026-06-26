<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const homePath = computed(() => userStore.profile?.homePath || '/login')

function goHome() {
  router.push(homePath.value)
}
</script>

<template>
  <div class="shell">
    <header class="top">
      <el-link type="primary" :underline="false" @click="goHome">← 返回工作台</el-link>
      <span class="user">{{ userStore.profile?.roleLabel }} · {{ userStore.profile?.realName || userStore.profile?.username }}</span>
    </header>
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.shell { min-height: 100vh; background: #f6f8fb; }
.top {
  height: 64px; background: #fff; display: flex; align-items: center;
  justify-content: space-between; padding: 0 32px; border-bottom: 1px solid #ebeef5;
}
.content { padding: 32px; max-width: 960px; margin: 0 auto; }
.user { color: #606266; font-size: 14px; }
</style>
