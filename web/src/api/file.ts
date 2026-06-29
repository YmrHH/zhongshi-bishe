import http from './http'

/** 带 JWT 拉取附件 Blob（预览/下载共用） */
export async function fetchFileBlob(fileUrl: string): Promise<Blob> {
  let path = fileUrl
  if (path.startsWith('/api')) path = path.slice(4)
  else if (path.startsWith('http')) {
    const u = new URL(path)
    path = u.pathname.replace(/^\/api/, '') + u.search
  }
  const res = await http.get(path, { responseType: 'blob' })
  const blob = res.data as Blob
  if (blob.type?.includes('json')) {
    const text = await blob.text()
    try {
      const err = JSON.parse(text) as { message?: string }
      throw new Error(err.message || '文件获取失败')
    } catch (e) {
      if (e instanceof Error && e.message !== '文件获取失败') throw e
      throw new Error('文件获取失败')
    }
  }
  return blob
}

export function downloadBlob(blob: Blob, fileName: string) {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName || '附件'
  a.click()
  window.URL.revokeObjectURL(url)
}

export async function previewBlob(blob: Blob, fileName?: string) {
  const url = window.URL.createObjectURL(blob)
  const w = window.open(url, '_blank')
  if (!w) {
    window.URL.revokeObjectURL(url)
    throw new Error('请允许弹出窗口以预览文件')
  }
  w.document.title = fileName || '附件预览'
  setTimeout(() => window.URL.revokeObjectURL(url), 60_000)
}
