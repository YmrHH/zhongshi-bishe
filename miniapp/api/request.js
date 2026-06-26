// 微信开发者工具模拟器可用 127.0.0.1；真机调试请改为电脑局域网 IP，如 http://192.168.x.x:8080/api
const BASE_URL = 'http://127.0.0.1:8080/api'

function request(url, method, data) {
  const token = uni.getStorageSync('token')
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : ''
      },
      success(res) {
        const body = res.data
        if (body && body.code === 200) {
          resolve(body.data)
        } else {
          reject(new Error((body && body.message) || `请求失败(${res.statusCode})`))
        }
      },
      fail(err) {
        reject(new Error(
          (err && err.errMsg) || '网络请求失败：请确认后端已启动(8080)，并在微信开发者工具中勾选「不校验合法域名」'
        ))
      }
    })
  })
}

export function login(username, password) {
  return request('/auth/login', 'POST', { username, password })
}

export function fetchDashboard() {
  return request('/common/dashboard', 'GET')
}

export function fetchEvaluationDetail(projectId) {
  return request(`/projects/${projectId}/evaluation/detail`, 'GET')
}

export function conditionSupplementEvaluation(projectId, data) {
  return request(`/projects/${projectId}/evaluation/condition-supplement`, 'POST', data)
}

export function supplementEvaluation(projectId, data) {
  return request(`/projects/${projectId}/evaluation/supplement`, 'POST', data)
}

export function receiptEvaluation(projectId) {
  return request(`/projects/${projectId}/evaluation/receipt`, 'POST')
}

export function feedbackEvaluation(projectId, data) {
  return request(`/projects/${projectId}/evaluation/feedback`, 'POST', data)
}

export function fetchMe() {
  return request('/auth/me', 'GET')
}

export function createDemandProject(data) {
  return request('/projects/demand/create', 'POST', data)
}

export function fetchDemandPreview(projectId) {
  return request(`/projects/${projectId}/demand/preview`, 'GET')
}

export function saveDemandDraft(projectId, data) {
  return request(`/projects/${projectId}/demand/draft`, 'POST', data)
}

export function submitDemand(projectId, data) {
  return request(`/projects/${projectId}/demand/submit`, 'POST', data)
}

export function supplementDemand(projectId, data) {
  return request(`/projects/${projectId}/demand/supplement`, 'POST', data)
}

export function receiptDemand(projectId) {
  return request(`/projects/${projectId}/demand/receipt`, 'POST')
}

export function fetchDemandProgress(projectId) {
  return request(`/projects/${projectId}/demand/progress`, 'GET')
}

export function fetchDemandTodos() {
  return request('/demand/todos', 'GET')
}

export function fetchDispatchDetail(projectId) {
  return request(`/projects/${projectId}/dispatch/progress`, 'GET')
}

export function receiveTask(taskId) {
  return request(`/tasks/${taskId}/receive`, 'POST')
}

export function confirmTask(taskId) {
  return request(`/tasks/${taskId}/confirm`, 'POST')
}

export function reportTaskProgress(taskId, data) {
  return request(`/tasks/${taskId}/progress`, 'POST', data)
}

export function acknowledgeDispatchProgress(projectId, data) {
  return request(`/projects/${projectId}/dispatch/acknowledge`, 'POST', data || {})
}

export function fetchFeedbackDetail(projectId) {
  return request(`/projects/${projectId}/feedback/detail`, 'GET')
}

export function fetchReviewDetail(projectId) {
  return request(`/projects/${projectId}/feedback/review-detail`, 'GET')
}

export function submitFeedback(projectId, data) {
  return request(`/projects/${projectId}/feedback/submit`, 'POST', data)
}

export function validateFeedback(projectId, data) {
  return request(`/projects/${projectId}/feedback/validate`, 'POST', data)
}

export function modifyFeedback(projectId, data) {
  return request(`/projects/${projectId}/feedback/modify`, 'POST', data)
}

export function reviewFeedbackOpinion(projectId, data) {
  return request(`/projects/${projectId}/feedback/review-feedback`, 'POST', data)
}

export function fetchArchiveDetail(projectId) {
  return request(`/projects/${projectId}/archive/detail`, 'GET')
}

export function fetchBrief(briefId) {
  return request(`/archive/brief/${briefId}`, 'GET')
}
