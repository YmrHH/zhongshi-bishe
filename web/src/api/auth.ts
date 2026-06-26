import http from './http'
import type { ApiResult, DashboardData, LoginResult, UserProfile } from '@/types/api'

export function login(username: string, password: string) {
  return http.post<ApiResult<LoginResult>>('/auth/login', { username, password })
}

export function fetchMe() {
  return http.get<ApiResult<UserProfile>>('/auth/me')
}

export function fetchDashboard() {
  return http.get<ApiResult<DashboardData>>('/common/dashboard')
}

export function updateProfile(data: { phone?: string; oldPassword?: string; newPassword?: string }) {
  return http.put<ApiResult<UserProfile>>('/auth/profile', data)
}
