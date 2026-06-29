/** 从数组中随机选取 n 个不重复元素（至少 1 个） */
export function pickRandomItems(arr, min = 1, max = arr.length) {
  const count = min + Math.floor(Math.random() * (Math.min(max, arr.length) - min + 1))
  const shuffled = [...arr].sort(() => Math.random() - 0.5)
  return shuffled.slice(0, count)
}

export function randomPilotType() {
  return pickRandomItems(['工艺放大', '产品验证', '稳定性测试'], 1, 1)[0]
}

export function randomInt(min, max) {
  return min + Math.floor(Math.random() * (max - min + 1))
}

export function timestampSuffix() {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`
}
