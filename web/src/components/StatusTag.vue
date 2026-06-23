<script setup lang="ts">
defineProps<{ label: string; status?: string }>()

const typeMap: Record<string, '' | 'success' | 'warning' | 'danger' | 'info'> = {
  DRAFT: 'info',
  SUBMITTED: 'warning',
  ACCEPTING: 'warning',
  VERIFYING: 'warning',
  RETURNED: 'danger',
  ACCEPTED: 'success',
  RECEIPTED: 'success',
  ARCHIVED: 'success',
  CLOSED: 'info',
  PRECHECK: 'warning',
  CONDITION_EVAL: 'warning',
  CONDITION_RETURNED: 'danger',
  RESOURCE: 'warning',
  FEASIBILITY: 'warning',
  FEASIBILITY_RETURNED: 'danger',
  CONCLUSION: 'success',
  FEEDBACK: 'info'
}
</script>

<template>
  <el-tag :type="typeMap[status || ''] || ''">{{ label }}</el-tag>
</template>
