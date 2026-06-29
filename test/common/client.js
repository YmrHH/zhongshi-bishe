import { config } from '../config/env.js'
import { assertApiOk } from './assert.js'

/**
 * 统一 HTTP 客户端（JSON + multipart）
 */
export class ApiClient {
  constructor(token = null) {
    this.token = token
  }

  setToken(token) {
    this.token = token
  }

  async request(method, path, { body, formData, headers = {} } = {}) {
    const url = `${config.baseUrl}${path.startsWith('/') ? path : `/${path}`}`
    const h = { ...headers }
    if (this.token) {
      h.Authorization = `Bearer ${this.token}`
    }
    if (body != null && !formData) {
      h['Content-Type'] = 'application/json'
    }

    const controller = new AbortController()
    const timer = setTimeout(() => controller.abort(), config.timeoutMs)

    try {
      const res = await fetch(url, {
        method,
        headers: h,
        body: formData ?? (body != null ? JSON.stringify(body) : undefined),
        signal: controller.signal
      })
      const json = await res.json().catch(() => ({}))
      return { status: res.status, json }
    } finally {
      clearTimeout(timer)
    }
  }

  async get(path) {
    const { json } = await this.request('GET', path)
    return json
  }

  async post(path, body) {
    const { json } = await this.request('POST', path, { body })
    return json
  }

  async postOk(path, body, step) {
    return assertApiOk(await this.post(path, body), step)
  }

  async uploadMultipart(path, formData, step) {
    const { json } = await this.request('POST', path, { formData })
    return assertApiOk(json, step)
  }
}
