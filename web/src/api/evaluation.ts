import http from './http'
import type { ApiResult } from '@/types/api'
import type { DemandTodo } from '@/types/demand'
import type { EvaluationDetail, EvaluationRequest } from '@/types/evaluation'

export function fetchEvaluationTodos() {
  return http.get<ApiResult<DemandTodo[]>>('/evaluation/todos')
}

export function fetchEvaluationDetail(projectId: number) {
  return http.get<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/detail`)
}

export function precheckEvaluation(projectId: number, data?: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/precheck`, data || {})
}

export function conditionEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/condition`, data)
}

export function rectifyNoticeEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/rectify-notice`, data)
}

export function conditionSupplementEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/condition-supplement`, data)
}

export function resourceEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/resource`, data)
}

export function feasibilityEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/feasibility`, data)
}

export function supplementEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/supplement`, data)
}

export function conclusionEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/conclusion`, data)
}

export function receiptEvaluation(projectId: number) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/receipt`)
}

export function feedbackEvaluation(projectId: number, data: EvaluationRequest) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/feedback`, data)
}

export function archiveEvaluation(projectId: number) {
  return http.post<ApiResult<EvaluationDetail>>(`/projects/${projectId}/evaluation/archive`)
}
