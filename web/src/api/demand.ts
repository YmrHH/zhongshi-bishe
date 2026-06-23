import http from './http'
import type { ApiResult } from '@/types/api'
import type { DemandDetail, DemandForm, DemandTodo, ProjectCreateResult } from '@/types/demand'

export function fetchDemandTodos() {
  return http.get<ApiResult<DemandTodo[]>>('/demand/todos')
}

export function createDemandProject(data: DemandForm) {
  return http.post<ApiResult<ProjectCreateResult>>('/projects/demand/create', data)
}

export function fetchDemandPreview(projectId: number) {
  return http.get<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/preview`)
}

export function saveDemandDraft(projectId: number, data: DemandForm) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/draft`, data)
}

export function submitDemand(projectId: number, data: DemandForm) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/submit`, data)
}

export function acceptRegisterDemand(projectId: number, remark?: string) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/accept-register`, { remark })
}

export function verifyDemand(projectId: number, complete: boolean, opinion?: string) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/verify`, { complete, opinion })
}

export function rejectDemand(projectId: number, reason: string) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/reject`, { reason })
}

export function supplementDemand(projectId: number, data: { content?: string; materials?: DemandForm['materials'] }) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/supplement`, data)
}

export function acceptResultDemand(projectId: number, accepted: boolean, opinion?: string) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/accept-result`, { accepted, opinion })
}

export function receiptDemand(projectId: number) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/receipt`)
}

export function archiveDemand(projectId: number) {
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/archive`)
}

export function fetchDemandProgress(projectId: number) {
  return http.get<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/progress`)
}

export async function uploadFile(file: File) {
  const form = new FormData()
  form.append('file', file)
  const res = await http.post<ApiResult<{ fileUrl: string; fileName: string }>>('/files/upload', form)
  return res.data.data
}
