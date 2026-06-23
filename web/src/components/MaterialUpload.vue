<script setup lang="ts">
import { ref } from 'vue'
import { uploadFile as uploadDemandFile } from '@/api/demand'
import type { MaterialItem } from '@/types/demand'
import { ElMessage } from 'element-plus'

const model = defineModel<MaterialItem[]>({ default: () => [] })
defineProps<{ readonly?: boolean }>()

const uploading = ref(false)

async function handleFileChange(upload: { raw?: File }) {
  if (!upload.raw) return
  uploading.value = true
  try {
    const res = await uploadDemandFile(upload.raw)
    model.value = [...model.value, {
      fileUrl: res.fileUrl,
      fileName: res.fileName,
      materialType: '附件'
    }]
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '上传失败')
  } finally {
    uploading.value = false
  }
}

function remove(idx: number) {
  model.value = model.value.filter((_, i) => i !== idx)
}
</script>

<template>
  <div>
    <el-upload v-if="!readonly" :auto-upload="false" :show-file-list="false" :on-change="handleFileChange">
      <el-button type="primary" :loading="uploading">上传材料</el-button>
    </el-upload>
    <el-table :data="model" style="width:100%;margin-top:12px" empty-text="暂无材料">
      <el-table-column prop="fileName" label="文件名" />
      <el-table-column prop="materialType" label="类型" width="120" />
      <el-table-column label="操作" width="100" v-if="!readonly">
        <template #default="{ $index }">
          <el-button type="danger" link @click="remove($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
