/** Web 待办 route → 小程序页面路径 */
export const ROUTE_MAP = {
  '/enterprise/demand/preview': '/pages/enterprise/demand/preview',
  '/enterprise/demand/submit': '/pages/enterprise/demand/submit',
  '/enterprise/demand/supplement': '/pages/enterprise/demand/supplement',
  '/enterprise/demand/receipt': '/pages/enterprise/demand/receipt',
  '/enterprise/demand/progress': '/pages/enterprise/demand/progress',
  '/enterprise/evaluation/condition-supplement': '/pages/enterprise/evaluation/condition-supplement',
  '/enterprise/evaluation/eval-supplement': '/pages/enterprise/evaluation/eval-supplement',
  '/enterprise/evaluation/conclusion-receipt': '/pages/enterprise/evaluation/conclusion-receipt',
  '/enterprise/evaluation/feedback': '/pages/enterprise/evaluation/feedback',
  '/enterprise/evaluation/conclusion-detail': '/pages/enterprise/evaluation/conclusion-detail',
  '/enterprise/dispatch/progress-view': '/pages/enterprise/dispatch/progress-view',
  '/enterprise/feedback/review-feedback': '/pages/enterprise/feedback/review-feedback',
  '/enterprise/feedback/review-detail': '/pages/enterprise/feedback/review-detail',
  '/enterprise/archive/brief-view': '/pages/enterprise/archive/brief-view',
  '/technician/dispatch/receive': '/pages/technician/dispatch/receive',
  '/technician/dispatch/confirm': '/pages/technician/dispatch/confirm',
  '/technician/dispatch/progress-report': '/pages/technician/dispatch/progress-report',
  '/technician/feedback/submit': '/pages/technician/feedback/submit',
  '/technician/feedback/validate': '/pages/technician/feedback/validate',
  '/technician/feedback/modify': '/pages/technician/feedback/modify'
}

export function requireLogin() {
  if (!uni.getStorageSync('token')) {
    uni.reLaunch({ url: '/pages/login/login' })
    return false
  }
  return true
}

export function openTodo(item) {
  if (!item?.route) {
    uni.showToast({ title: '暂无办理页面', icon: 'none' })
    return
  }
  const path = ROUTE_MAP[item.route]
  if (!path) {
    uni.showToast({ title: '请使用 Web 端办理', icon: 'none' })
    return
  }
  const q = []
  if (item.projectId) q.push(`projectId=${item.projectId}`)
  if (item.taskId) q.push(`taskId=${item.taskId}`)
  uni.navigateTo({ url: q.length ? `${path}?${q.join('&')}` : path })
}

export function logout() {
  uni.removeStorageSync('token')
  uni.removeStorageSync('profile')
  uni.reLaunch({ url: '/pages/login/login' })
}
