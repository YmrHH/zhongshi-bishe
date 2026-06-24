<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { MODULES } from '@/router'

defineProps<{ portalTitle: string }>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => (route.meta.module as string) || 'home')
const portalPrefix = computed(() => route.path.split('/')[1])

function goHome() {
  router.push(`/${portalPrefix.value}/home`)
}

function menuEnabled(mod: typeof MODULES[number]) {
  const role = userStore.profile?.role
  if (mod.path === 'dispatch') {
    return role === 'DISPATCHER' || role === 'ENTERPRISE' || role === 'TECHNICIAN'
  }
  return mod.path === 'demand' || mod.path === 'evaluation'
}

function goModule(mod: typeof MODULES[number]) {
  if (!menuEnabled(mod)) return
  const role = userStore.profile?.role
  if (mod.path === 'demand') {
    if (role === 'ENTERPRISE') router.push('/enterprise/demand/preview')
    else if (role === 'DISPATCHER') router.push('/center/dispatch/demand/workbench')
    else if (role === 'AUDITOR') router.push('/center/audit/demand/verify')
    return
  }
  if (mod.path === 'evaluation') {
    if (role === 'ENTERPRISE') router.push('/enterprise/evaluation/conclusion-detail')
    else if (role === 'DISPATCHER') router.push('/center/dispatch/evaluation/precheck')
    else if (role === 'AUDITOR') router.push('/center/audit/evaluation/feasibility')
    return
  }
  if (mod.path === 'dispatch') {
    if (role === 'ENTERPRISE') router.push('/enterprise/dispatch/progress-view')
    else if (role === 'DISPATCHER') router.push('/center/dispatch/dispatch/match')
    else if (role === 'TECHNICIAN') router.push('/technician/dispatch/receive')
    else if (role === 'AUDITOR') {
      ElMessage.warning('中试调度管理仅中试调度员可操作，审核员请使用「中试评估管理」')
    }
  }
}

function logout() {
  userStore.clear()
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <aside class="side">
      <div class="brand">中试服务管理系统</div>
      <div class="sub">广州生产力促进中心</div>
      <nav class="menu">
        <div :class="['item', activeMenu === 'home' ? 'on' : '']" @click="goHome">工作台首页</div>
        <div
          v-for="m in MODULES"
          :key="m.path"
          :class="['item', activeMenu === m.path ? 'on' : '', menuEnabled(m) ? '' : 'disabled']"
          @click="goModule(m)"
        >
          {{ m.title }}
        </div>
      </nav>
    </aside>
    <main class="main">
      <header class="top">
        <b>广州生产力促进中心中试服务管理系统</b>
        <span class="user">
          {{ userStore.profile?.roleLabel }}　
          <el-link type="primary" :underline="false">消息</el-link>　
          <el-link type="primary" :underline="false">个人中心</el-link>　
          <el-link type="danger" :underline="false" @click="logout">退出</el-link>
        </span>
      </header>
      <div class="content">
        <slot />
      </div>
    </main>
  </div>
</template>

<style scoped>
.layout { display: flex; min-height: 100vh; }
.side {
  width: 220px; background: var(--sidebar-bg); color: #c7d4e4;
  padding: 28px 18px; flex-shrink: 0;
}
.brand { color: #fff; font-size: 22px; font-weight: 700; }
.sub { font-size: 13px; margin-top: 8px; color: #b8c7d9; }
.menu { margin-top: 44px; }
.item {
  padding: 14px 18px; border-radius: 10px; margin-bottom: 10px; cursor: pointer;
}
.item.on { background: var(--primary); color: #fff; }
.item.disabled { opacity: 0.55; cursor: default; }
.main { flex: 1; background: #f6f8fb; display: flex; flex-direction: column; }
.top {
  height: 64px; background: #fff; display: flex; align-items: center;
  justify-content: space-between; padding: 0 32px; border-bottom: 1px solid #ebeef5;
}
.content { padding: 32px; flex: 1; max-width: 1440px; }
.user { color: #606266; font-size: 14px; }
</style>
