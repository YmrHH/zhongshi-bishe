import { config } from '../config/env.js'
import { ApiClient } from './client.js'
import { assertApiOk } from './assert.js'
import { step } from './logger.js'

const cache = new Map()

/**
 * 按角色登录并返回带 Token 的 ApiClient
 */
export async function loginAs(role) {
  const username = config.accounts[role]
  if (!username) {
    throw new Error(`未知角色: ${role}`)
  }
  if (cache.has(role)) {
    return cache.get(role)
  }

  step(role, '登录', username)
  const guest = new ApiClient()
  const json = await guest.post('/auth/login', {
    username,
    password: config.password
  })
  const data = assertApiOk(json, `${role} 登录`)
  const client = new ApiClient(data.token)
  cache.set(role, client)
  return client
}

export function clearAuthCache() {
  cache.clear()
}
