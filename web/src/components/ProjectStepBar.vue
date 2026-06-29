<script setup lang="ts">
withDefaults(
  defineProps<{
    steps?: Array<{ node: string; status: string }>
  }>(),
  { steps: () => [] }
)
</script>

<template>
  <div class="steps">
    <div
      v-for="(step, idx) in steps"
      :key="idx"
      :class="['step', step.status]"
    >
      <i>{{ idx + 1 }}</i>
      <span>{{ step.node }}</span>
    </div>
  </div>
</template>

<style scoped>
.steps { display: flex; gap: 12px; margin: 20px 0; flex-wrap: wrap; }
.step {
  flex: 1; min-width: 120px; border-top: 4px solid #dcdfe6;
  padding-top: 10px; color: #909399; font-size: 13px;
}
.step i {
  display: inline-flex; width: 26px; height: 26px; border-radius: 50%;
  align-items: center; justify-content: center; background: #dcdfe6;
  color: #fff; font-style: normal; margin-right: 6px; font-size: 12px;
}
.step.done { border-color: var(--primary); color: #303133; }
.step.done i { background: var(--primary); }
.step.active { border-color: var(--primary); color: var(--primary); font-weight: 600; }
.step.active i { background: var(--primary); }
</style>
