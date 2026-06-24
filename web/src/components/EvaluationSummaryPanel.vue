<script setup lang="ts">
defineProps<{
  summary?: {
    conclusion?: string
    conclusionOpinion?: string
    resourceRequirement?: string
    resourceRemark?: string
    conditionResult?: string
    feasibilityResult?: string
    feedbackContent?: string
  } | null
}>()
</script>

<template>
  <div v-if="summary" class="page-card eval-summary">
    <h3>评估归档信息（上一阶段）</h3>
    <el-descriptions :column="2" border size="small">
      <el-descriptions-item label="评估结论">{{ summary.conclusion || '—' }}</el-descriptions-item>
      <el-descriptions-item label="条件评估">{{ summary.conditionResult || '—' }}</el-descriptions-item>
      <el-descriptions-item label="可行性结论">{{ summary.feasibilityResult || '—' }}</el-descriptions-item>
      <el-descriptions-item label="企业反馈">{{ summary.feedbackContent || '—' }}</el-descriptions-item>
      <el-descriptions-item label="资源需求" :span="2">{{ summary.resourceRequirement || '—' }}</el-descriptions-item>
      <el-descriptions-item label="资源核定意见" :span="2">{{ summary.resourceRemark || '—' }}</el-descriptions-item>
      <el-descriptions-item label="结论意见" :span="2">{{ summary.conclusionOpinion || '—' }}</el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<style scoped>
.eval-summary { margin-bottom: 16px; }
.eval-summary h3 { margin: 0 0 12px; font-size: 16px; }
</style>
