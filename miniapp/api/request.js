const BASE_URL = 'http://localhost:8080/api'

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
        if (body.code === 200) {
          resolve(body.data)
        } else {
          reject(new Error(body.message || '请求失败'))
        }
      },
      fail(err) {
        reject(err)
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
