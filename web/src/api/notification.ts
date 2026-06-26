import http from './http'
import type { ApiResult } from '@/types/api'

export interface NotificationItem {
  id: number
  projectId?: number
  type: string
  title: string
  content: string
  read: boolean
  createdAt: string
}

export function fetchNotifications() {
  return http.get<ApiResult<NotificationItem[]>>('/notifications')
}

export function fetchUnreadCount() {
  return http.get<ApiResult<{ count: number }>>('/notifications/unread-count')
}

export function markNotificationRead(id: number) {
  return http.put<ApiResult<null>>(`/notifications/${id}/read`)
}
