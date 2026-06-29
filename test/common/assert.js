export function assertEqual(actual, expected, message) {
  if (actual !== expected) {
    throw new Error(`${message}: 期望 ${expected}，实际 ${actual}`)
  }
}

export function assertTrue(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

export function assertApiOk(json, step) {
  if (!json || json.code !== 200) {
    throw new Error(`${step} 失败: ${json?.message || '未知错误'}`)
  }
  return json.data
}
