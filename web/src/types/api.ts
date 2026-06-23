export interface LoginResult {
  token: string
  username: string
  role: string
  roleLabel: string
  realName: string
  orgName: string
  homePath: string
}

export interface UserProfile extends LoginResult {
  id: number
  phone?: string
}

export interface DashboardData {
  stats: Record<string, number>
  todos: Array<{
    projectId?: number
    projectNo: string
    title: string
    module: string
    status: string
    time: string
    route?: string
  }>
}

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}
