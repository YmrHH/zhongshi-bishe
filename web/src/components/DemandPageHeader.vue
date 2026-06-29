<script setup lang="ts">
import { ArrowLeft } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = withDefaults(
  defineProps<{
    title: string
    crumb?: string
    showBack?: boolean
    backLabel?: string
    backTo?: string
  }>(),
  {
    showBack: true,
    backLabel: '返回项目列表',
    backTo: '/enterprise/demand/projects'
  }
)

const router = useRouter()
</script>

<template>
  <div class="page-header">
    <div class="left">
      <div v-if="crumb" class="crumb">{{ crumb }}</div>
      <h1 class="title">{{ title }}</h1>
      <slot name="extra" />
    </div>
    <el-button
      v-if="showBack"
      type="primary"
      size="large"
      class="back-btn"
      :icon="ArrowLeft"
      @click="router.push(backTo)"
    >
      {{ backLabel }}
    </el-button>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}
.left { flex: 1; min-width: 0; }
.crumb { color: #909399; font-size: 14px; }
.title { font-size: 28px; margin: 8px 0 0; line-height: 1.2; }
.back-btn {
  flex-shrink: 0;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.35);
}
</style>
