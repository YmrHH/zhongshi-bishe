<script setup lang="ts">
defineProps<{
  total: number
  page: number
  pageSize: number
}>()

const emit = defineEmits<{
  change: [page: number]
  'size-change': [pageSize: number]
}>()
</script>

<template>
  <div class="list-pagination">
    <el-pagination
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :current-page="page"
      :page-size="pageSize"
      :page-sizes="[10, 20, 50]"
      @current-change="(p: number) => emit('change', p)"
      @size-change="(s: number) => emit('size-change', s)"
    />
  </div>
</template>

<style scoped>
.list-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
