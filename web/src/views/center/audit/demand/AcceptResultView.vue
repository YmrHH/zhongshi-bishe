<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onMounted(() => {
  const q = route.query.projectId ? { projectId: route.query.projectId } : {}
  router.replace({ path: '/center/audit/demand/verify', query: q })
})
</script>

<template>
  <div class="hint">受理结果录入已合并至「受理材料审核页」，正在跳转…</div>
</template>

<style scoped>
.hint { padding: 48px; text-align: center; color: #909399; }
</style>
