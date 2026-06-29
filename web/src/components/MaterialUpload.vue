<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { renderAsync } from 'docx-preview'
import * as XLSX from 'xlsx'
import http from '@/api/http'
import { downloadBlob, fetchFileBlob } from '@/api/file'

const model = defineModel<Array<{ fileUrl?: string; fileName?: string; materialType?: string; version?: number }>>({ default: () => [] })
defineProps<{ readonly?: boolean }>()

type PreviewKind = 'image' | 'pdf' | 'docx' | 'xlsx' | 'office-fallback' | 'other'

const uploading = ref(false)
const loadingIdx = ref<number | null>(null)
const previewVisible = ref(false)
const previewUrl = ref('')
const previewTitle = ref('')
const previewKind = ref<PreviewKind>('other')
const xlsxHtml = ref('')
const docxContainer = ref<HTMLElement | null>(null)

function detectPreviewKind(fileName?: string, blobType?: string): PreviewKind {
  const name = (fileName || '').toLowerCase()
  if (blobType?.startsWith('image/') || /\.(png|jpe?g|gif|webp|bmp)$/i.test(name)) return 'image'
  if (blobType === 'application/pdf' || name.endsWith('.pdf')) return 'pdf'
  if (name.endsWith('.docx') || name.endsWith('.doc')) return 'docx'
  if (name.endsWith('.xlsx') || name.endsWith('.xls')) return 'xlsx'
  if (name.endsWith('.ppt') || name.endsWith('.pptx')) return 'office-fallback'
  return 'other'
}

async function handleFileChange(upload: { raw?: File }) {
  if (!upload.raw) return
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', upload.raw)
    const res = await http.post<{ data: { fileUrl: string; fileName: string } }>('/files/upload', form)
    model.value = [...model.value, {
      fileUrl: res.data.data.fileUrl,
      fileName: res.data.data.fileName,
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

function resetPreviewState() {
  if (previewUrl.value) window.URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
  xlsxHtml.value = ''
  if (docxContainer.value) docxContainer.value.innerHTML = ''
  previewKind.value = 'other'
}

function closePreview() {
  resetPreviewState()
  previewVisible.value = false
}

async function download(row: { fileUrl?: string; fileName?: string }, idx: number) {
  if (!row.fileUrl) return
  loadingIdx.value = idx
  try {
    const blob = await fetchFileBlob(row.fileUrl)
    downloadBlob(blob, row.fileName || '附件')
  } catch (e) {
    ElMessage.error(e instanceof Error ? e.message : '下载失败')
  } finally {
    loadingIdx.value = null
  }
}

async function preview(row: { fileUrl?: string; fileName?: string }, idx: number) {
  if (!row.fileUrl) return
  loadingIdx.value = idx
  try {
    const blob = await fetchFileBlob(row.fileUrl)
    resetPreviewState()
    previewTitle.value = row.fileName || '附件预览'
    const kind = detectPreviewKind(row.fileName, blob.type)
    previewKind.value = kind

    if (kind === 'image' || kind === 'pdf') {
      previewUrl.value = window.URL.createObjectURL(blob)
      previewVisible.value = true
      return
    }

    previewVisible.value = true
    await nextTick()

    if (kind === 'docx') {
      if (!docxContainer.value) return
      await renderAsync(blob, docxContainer.value, undefined, { inWrapper: true })
      return
    }

    if (kind === 'xlsx') {
      const wb = XLSX.read(await blob.arrayBuffer())
      const sheetName = wb.SheetNames[0]
      if (sheetName) {
        xlsxHtml.value = XLSX.utils.sheet_to_html(wb.Sheets[sheetName])
      }
    }
  } catch (e) {
    previewVisible.value = false
    ElMessage.error(e instanceof Error ? e.message : '预览失败')
  } finally {
    loadingIdx.value = null
  }
}
</script>

<template>
  <div>
    <el-upload v-if="!readonly" :auto-upload="false" :show-file-list="false" :on-change="handleFileChange">
      <el-button type="primary" :loading="uploading">上传材料</el-button>
    </el-upload>
    <el-table :data="model" style="width:100%;margin-top:12px" empty-text="暂无材料">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="fileName" label="文件名" min-width="180" />
      <el-table-column prop="materialType" label="类型" width="100" />
      <el-table-column prop="version" label="版本" width="70" />
      <el-table-column label="操作" :width="readonly ? 160 : 100">
        <template #default="{ row, $index }">
          <template v-if="readonly">
            <el-button type="primary" link :loading="loadingIdx === $index" @click="download(row, $index)">下载</el-button>
            <el-button type="primary" link :loading="loadingIdx === $index" @click="preview(row, $index)">预览</el-button>
          </template>
          <el-button v-else type="danger" link @click="remove($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="previewVisible"
      :title="previewTitle"
      width="80%"
      top="5vh"
      destroy-on-close
      @close="closePreview"
    >
      <div class="preview-body">
        <img v-if="previewKind === 'image'" :src="previewUrl" alt="预览" class="preview-img" />
        <iframe v-else-if="previewKind === 'pdf'" :src="previewUrl" class="preview-frame" title="PDF预览" />
        <div v-else-if="previewKind === 'docx'" ref="docxContainer" class="docx-wrap" />
        <div v-else-if="previewKind === 'xlsx'" class="xlsx-wrap" v-html="xlsxHtml" />
        <el-empty
          v-else-if="previewKind === 'office-fallback'"
          description="演示文稿暂不支持在线预览，请使用下载按钮查看"
        />
        <el-empty v-else description="该文件类型暂不支持在线预览，请使用下载按钮" />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.preview-body { min-height: 50vh; display: flex; align-items: flex-start; justify-content: center; overflow: auto; }
.preview-img { max-width: 100%; max-height: 70vh; object-fit: contain; margin: auto; }
.preview-frame { width: 100%; height: 70vh; border: none; }
.docx-wrap { width: 100%; max-height: 70vh; overflow: auto; }
.xlsx-wrap { width: 100%; max-height: 70vh; overflow: auto; }
.xlsx-wrap :deep(table) { border-collapse: collapse; width: 100%; }
.xlsx-wrap :deep(td), .xlsx-wrap :deep(th) { border: 1px solid #dcdfe6; padding: 6px 8px; font-size: 13px; }
</style>
