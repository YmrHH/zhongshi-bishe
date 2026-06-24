import http from './http'
import type { ApiResult } from '@/types/api'
import type { DemandTodo } from '@/types/demand'
import type { DispatchDetail, DispatchRequest } from '@/types/dispatch'

export function fetchDispatchTodos() {
  return http.get<ApiResult<DemandTodo[]>>('/dispatch/todos')
}

export function fetchDispatchDetail(projectId: number) {
  return http.get<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/progress`)
}

export function matchDispatch(projectId: number, data: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/match`, data)
}

export function assignDispatch(projectId: number, data: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/assign`, data)
}

export function assignNoticeDispatch(projectId: number, data?: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/assign-notice`, data || {})
}

export function superviseDispatch(projectId: number, data?: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/supervise`, data || {})
}

export function reassignDispatch(projectId: number, data: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/reassign`, data)
}

export function execConfirmDispatch(projectId: number, data?: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/exec-confirm`, data || {})
}

export function archiveDispatch(projectId: number) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/archive`)
}

export function acknowledgeDispatchProgress(projectId: number, data?: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/projects/${projectId}/dispatch/acknowledge`, data || {})
}

export function receiveTask(taskId: number) {
  return http.post<ApiResult<DispatchDetail>>(`/tasks/${taskId}/receive`)
}

export function confirmTask(taskId: number) {
  return http.post<ApiResult<DispatchDetail>>(`/tasks/${taskId}/confirm`)
}

export function reportTaskProgress(taskId: number, data: DispatchRequest) {
  return http.post<ApiResult<DispatchDetail>>(`/tasks/${taskId}/progress`, data)
}
