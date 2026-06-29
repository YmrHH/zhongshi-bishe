<script setup lang="ts">
/** 中试需求管理五阶段进度条：填报 → 登记 → 审核 → 签收 → 归档查进度 */
withDefaults(
  defineProps<{
    steps?: Array<{ node: string; status: string }>
    title?: string
  }>(),
  { steps: () => [], title: '需求办理进度（五阶段）' }
)

const active = (steps: Array<{ status: string }>) => {
  const idx = steps.findIndex(s => s.status === 'active')
  if (idx >= 0) return idx
  const done = steps.filter(s => s.status === 'done').length
  return done >= steps.length ? steps.length : done
}
</script>

<template>
  <div v-if="steps.length" class="phase-wrap">
    <div class="phase-title">{{ title }}</div>
    <el-steps :active="active(steps)" finish-status="success" align-center class="phase-steps">
      <el-step
        v-for="(step, idx) in steps"
        :key="idx"
        :title="step.node"
        :status="step.status === 'done' ? 'success' : step.status === 'active' ? 'process' : 'wait'"
      />
    </el-steps>
  </div>
</template>

<style scoped>
.phase-wrap {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px 20px 8px;
  margin: 16px 0;
}
.phase-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.phase-steps :deep(.el-step__title) {
  font-size: 13px;
}
</style>
