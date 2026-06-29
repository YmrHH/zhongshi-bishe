import http from './http'
import type { ApiResult } from '@/types/api'
import type { PageResult } from '@/types/page'
import type { DemandDetail, DemandEnterpriseProject, DemandForm, DemandTodo, ProjectCreateResult } from '@/types/demand'

export function fetchDemandTodos(params?: { page?: number; pageSize?: number }) {
  return http.get<ApiResult<PageResult<DemandTodo>>>('/demand/todos', { params })
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

export function verifyDemand(projectId: number, complete: boolean, accepted?: boolean, opinion?: string) {
  const body: { complete: boolean; accepted?: boolean; opinion?: string } = { complete, opinion }
  if (complete && accepted !== undefined) body.accepted = accepted
  return http.post<ApiResult<DemandDetail>>(`/projects/${projectId}/demand/verify`, body)
}

export type EnterpriseStageFilter = 'ALL' | 'DEMAND' | 'EVALUATION' | 'DISPATCH' | 'FEEDBACK' | 'ARCHIVE' | 'CLOSED'

export function fetchEnterpriseDemandProjects(params?: {
  page?: number
  pageSize?: number
  stageFilter?: EnterpriseStageFilter
  keyword?: string
}) {
  return http.get<ApiResult<PageResult<DemandEnterpriseProject>>>('/demand/enterprise/projects', { params })
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
