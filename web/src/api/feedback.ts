import http from './http'
import type { ApiResult } from '@/types/api'
import type { DemandTodo } from '@/types/demand'
import type { FeedbackDetail, FeedbackRequest } from '@/types/feedback'

export function fetchFeedbackTodos() {
  return http.get<ApiResult<DemandTodo[]>>('/feedback/todos')
}

export function fetchFeedbackDetail(projectId: number) {
  return http.get<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/detail`)
}

export function submitFeedback(projectId: number, data: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/submit`, data)
}

export function validateFeedback(projectId: number, data: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/validate`, data)
}

export function auditFeedback(projectId: number, data: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/audit`, data)
}

export function modifyFeedback(projectId: number, data: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/modify`, data)
}

export function reviewFeedbackConfirm(projectId: number, data?: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/review`, data || {})
}

export function reportArchiveFeedback(projectId: number, data?: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/report-archive`, data || {})
}

export function reviewNoticeFeedback(projectId: number, data?: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/review-notice`, data || {})
}

export function reviewFeedbackOpinion(projectId: number, data: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/review-feedback`, data)
}

export function feedbackAudit(projectId: number, data: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/feedback-audit`, data)
}

export function caseArchiveFeedback(projectId: number, data?: FeedbackRequest) {
  return http.post<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/case-archive`, data || {})
}

export function fetchReviewDetail(projectId: number) {
  return http.get<ApiResult<FeedbackDetail>>(`/projects/${projectId}/feedback/review-detail`)
}
