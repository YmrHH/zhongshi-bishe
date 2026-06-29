import http from './http'
import type { ApiResult } from '@/types/api'
import type { ProjectProgress } from '@/types/project'

export function fetchProjectProgress(projectId: number) {
  return http.get<ApiResult<ProjectProgress>>(`/projects/${projectId}/progress`)
}
