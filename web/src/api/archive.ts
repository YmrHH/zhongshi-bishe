import http from './http'
import type { ApiResult } from '@/types/api'
import type { DemandTodo } from '@/types/demand'
import type { ArchiveDetail, ArchiveRequest, ArchiveStats, BriefDetail } from '@/types/archive'

export function fetchArchiveTodos() {
  return http.get<ApiResult<DemandTodo[]>>('/archive/todos')
}

export function fetchArchiveLedger(projectId: number) {
  return http.get<ApiResult<ArchiveDetail>>('/archive/ledger', { params: { projectId } })
}

export function updateArchiveLedger(data: ArchiveRequest) {
  return http.put<ApiResult<ArchiveDetail>>('/archive/ledger', data)
}

export function fetchArchiveDetail(projectId: number) {
  return http.get<ApiResult<ArchiveDetail>>(`/projects/${projectId}/archive/detail`)
}

export function confirmArchive(projectId: number, data?: ArchiveRequest) {
  return http.post<ApiResult<ArchiveDetail>>(`/archive/projects/${projectId}/confirm`, data || {})
}

export function collectArchive(projectId: number, data: ArchiveRequest) {
  return http.post<ApiResult<ArchiveDetail>>(`/archive/projects/${projectId}/collect`, data)
}

export function fetchCycleStats(projectId: number) {
  return http.get<ApiResult<ArchiveStats>>('/archive/cycle-stats', { params: { projectId } })
}

export function fetchSuccessRate(projectId: number) {
  return http.get<ApiResult<ArchiveStats>>('/archive/success-rate', { params: { projectId } })
}

export function confirmCycleStats(data: ArchiveRequest) {
  return http.post<ApiResult<ArchiveDetail>>('/archive/cycle-stats/confirm', data)
}

export function generateBrief(data: ArchiveRequest) {
  return http.post<ApiResult<ArchiveDetail>>('/archive/brief/generate', data)
}

export function auditBrief(data: ArchiveRequest) {
  return http.post<ApiResult<ArchiveDetail>>('/archive/brief/audit', data)
}

export function fetchBrief(briefId: number) {
  return http.get<ApiResult<BriefDetail>>(`/archive/brief/${briefId}`)
}

export function archiveProject(projectId: number, data?: ArchiveRequest) {
  return http.post<ApiResult<ArchiveDetail>>(`/archive/projects/${projectId}/archive`, data || {})
}
