<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

/** §17.12：归档已合并至受理工作台，本路由仅作兼容重定向 */
const route = useRoute()
const router = useRouter()

onMounted(() => {
  const projectId = Number(route.query.projectId)
  if (!projectId) {
    router.replace('/center/dispatch/demand/workbench')
    return
  }
  router.replace({ path: '/center/dispatch/demand/workbench', query: { projectId } })
})
</script>

<template>
  <div />
</template>
