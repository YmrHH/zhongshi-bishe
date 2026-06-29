/**
 * API 端到端测试脚本 — 不修改代码，仅调用后端接口验证主链与缺陷
 * 用法：node scripts/e2e-api-test.mjs
 */
const BASE = 'http://localhost:8080/api'

const results = []
function log(id, pass, msg, detail = '') {
  results.push({ id, pass, msg, detail })
  const mark = pass ? 'PASS' : 'FAIL'
  console.log(`[${mark}] ${id}: ${msg}${detail ? ' — ' + detail : ''}`)
}

async function req(method, path, token, body) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body != null ? JSON.stringify(body) : undefined
  })
  const json = await res.json().catch(() => ({}))
  return { status: res.status, json }
}

async function login(username) {
  const { json } = await req('POST', '/auth/login', null, { username, password: '123456' })
  if (json.code !== 200) throw new Error(`${username} login: ${json.message}`)
  return json.data.token
}

async function main() {
  console.log('=== 中试服务管理系统 API E2E 测试 ===\n')

  // AUTH
  let entTok, disTok, audTok, techTok
  try {
    entTok = await login('enterprise')
    disTok = await login('dispatcher')
    audTok = await login('auditor')
    techTok = await login('technician')
    log('AUTH-02~05', true, '四角色登录成功')
  } catch (e) {
    log('AUTH-02~05', false, '登录失败', e.message)
    printSummary()
    process.exit(1)
  }

  const bad = await req('POST', '/auth/login', null, { username: 'enterprise', password: 'wrong' })
  log('AUTH-06', bad.json.code !== 200, '错误密码拒绝登录', `code=${bad.json.code}`)

  const noTok = await req('GET', '/common/dashboard', null)
  log('AUTH-01', noTok.status === 403 || noTok.status === 401, '未登录拒绝访问', `HTTP ${noTok.status}`)

  // 越权
  const cross = await req('POST', '/projects/demand/create', disTok, { title: '越权', content: 'x' })
  log('D-15', cross.json.code === 403 || cross.json.message?.includes('权限'), '调度员不能创建需求', cross.json.message)

  // 创建项目
  const create = await req('POST', '/projects/demand/create', entTok, {
    title: 'E2E测试项目',
    content: '自动化测试需求内容',
    materials: []
  })
  if (create.json.code !== 200) {
    log('TC-D1', false, '创建项目失败', create.json.message)
    printSummary()
    process.exit(1)
  }
  const pid = create.json.data.projectId
  log('D-01', true, '创建草稿项目', `projectId=${pid}, no=${create.json.data.projectNo}`)

  // 提交需求
  const submit = await req('POST', `/projects/${pid}/demand/submit`, entTok, {
    title: 'E2E测试项目',
    content: '自动化测试需求内容-已提交',
    materials: []
  })
  log('D-02', submit.json.code === 200 && submit.json.data.status === 'SUBMITTED', '需求提交', submit.json.data?.status)

  // 调度员待办
  const disTodos1 = await req('GET', '/demand/todos', disTok)
  const hasAccept = disTodos1.json.data?.some(t => t.projectId === pid)
  log('D-04-pre', hasAccept, '提交后调度员有待办', `count=${disTodos1.json.data?.length}`)

  await req('POST', `/projects/${pid}/demand/accept-register`, disTok, {})
  const verify = await req('POST', `/projects/${pid}/demand/verify`, audTok, { complete: true, accepted: true, opinion: '材料齐全，同意受理' })
  log('D-05', verify.json.code === 200 && verify.json.data?.status === 'ACCEPTED', '材料审核一步通过', verify.json.data?.status)
  log('D-09', verify.json.code === 200 && verify.json.data?.status === 'ACCEPTED', '受理决定已合并至核验', verify.json.data?.status)
  const receipt = await req('POST', `/projects/${pid}/demand/receipt`, entTok, {})
  log('D-11', receipt.json.code === 200, '回执签收', receipt.json.data?.status)
  await req('POST', `/projects/${pid}/demand/archive`, disTok, {})

  const afterDemand = await req('GET', `/projects/${pid}/demand/preview`, entTok)
  log('D-12', afterDemand.json.data?.stage === 'EVALUATION', '需求归档进入评估', afterDemand.json.data?.stage)

  // 评估
  await req('POST', `/projects/${pid}/evaluation/precheck`, disTok, { remark: '前置通过' })
  await req('POST', `/projects/${pid}/evaluation/condition`, disTok, { passed: true, remark: '条件具备' })
  await req('POST', `/projects/${pid}/evaluation/resource`, disTok, { remark: '资源核定完成' })
  await req('POST', `/projects/${pid}/evaluation/feasibility`, audTok, { passed: true, remark: '可行' })
  await req('POST', `/projects/${pid}/evaluation/conclusion`, audTok, { passed: true, remark: '评估通过', conclusion: '同意中试' })
  await req('POST', `/projects/${pid}/evaluation/receipt`, entTok, {})
  await req('POST', `/projects/${pid}/evaluation/feedback`, entTok, { remark: '无异议' })
  const evalArch = await req('POST', `/projects/${pid}/evaluation/archive`, disTok, {})
  log('E-12', evalArch.json.code === 200 && evalArch.json.data?.stage === 'DISPATCH', '评估归档', evalArch.json.data?.stage)

  // 调度 — 获取技术人员与资源 ID
  const meTech = await req('GET', '/auth/me', techTok)
  const techId = meTech.json.data?.id
  const dispatchDetail = await req('GET', `/projects/${pid}/dispatch/progress`, disTok)
  const resourceId = dispatchDetail.json.data?.resources?.[0]?.id || 1
  await req('POST', `/projects/${pid}/dispatch/match`, disTok, { passed: true })
  const assign = await req('POST', `/projects/${pid}/dispatch/assign`, disTok, { technicianId: techId, resourceId })
  log('S-03', assign.json.code === 200, '任务派发', assign.json.message)
  await req('POST', `/projects/${pid}/dispatch/assign-notice`, disTok, {})

  const disTodosAfterNotice = await req('GET', '/dispatch/todos', disTok)
  const hasPendingReceiveTodo = disTodosAfterNotice.json.data?.some(
    t => t.projectId === pid && (t.status === 'PENDING_RECEIVE' || t.route?.includes('assign-notice'))
  )
  log('BUG-01', hasPendingReceiveTodo, '派单通知后调度员待办应含该项目', `todos=${JSON.stringify(disTodosAfterNotice.json.data?.filter(t => t.projectId === pid))}`)

  const taskId = assign.json.data?.taskId
  if (taskId) {
    await req('POST', `/tasks/${taskId}/receive`, techTok, {})
    const disTodosAfterReceive = await req('GET', '/dispatch/todos', disTok)
    const hasReceivedTodo = disTodosAfterReceive.json.data?.some(t => t.projectId === pid)
    log('BUG-16', hasReceivedTodo, '技术人员接收后调度员待办应含该项目(RECEIVED)', `status=${disTodosAfterReceive.json.data?.find(t => t.projectId === pid)?.status}`)

    await req('POST', `/tasks/${taskId}/confirm`, techTok, {})
    await req('POST', `/tasks/${taskId}/progress`, techTok, { progressPct: 100, content: '完成' })
  }

  const execConfirm = await req('POST', `/projects/${pid}/dispatch/exec-confirm`, disTok, { passed: true })
  log('S-12', execConfirm.json.code === 200, '执行结果确认', execConfirm.json.data?.status)
  await req('POST', `/projects/${pid}/dispatch/archive`, disTok, {})

  // 反馈
  await req('POST', `/projects/${pid}/feedback/submit`, techTok, { content: '试验结果合格' })
  await req('POST', `/projects/${pid}/feedback/validate`, techTok, { passed: true, remark: '校验通过' })
  await req('POST', `/projects/${pid}/feedback/audit`, audTok, { passed: true, remark: '审核通过' })
  const reviewReject = await req('POST', `/projects/${pid}/feedback/review`, audTok, { passed: false, remark: '应拒绝' })
  const reviewDetailAfter = await req('GET', `/projects/${pid}/feedback/detail`, audTok)
  const stAfterReview = reviewDetailAfter.json.data?.status
  // BUG-02: Controller 硬编码 passed=true，即使传 false 也会进入 REVIEWED
  log('BUG-02', reviewReject.json.code === 200 && stAfterReview === 'REVIEWED', '复核 API 忽略拒绝参数强制通过', `status=${stAfterReview}（预期拒绝时不应为 REVIEWED）`)
  await req('POST', `/projects/${pid}/feedback/report-archive`, audTok, {})
  await req('POST', `/projects/${pid}/feedback/review-notice`, audTok, {})
  await req('POST', `/projects/${pid}/feedback/review-feedback`, entTok, { remark: '收到' })
  const fbAudit = await req('POST', `/projects/${pid}/feedback/feedback-audit`, audTok, { passed: true, remark: '通过' })
  const caseArch = await req('POST', `/projects/${pid}/feedback/case-archive`, audTok, {})
  log('F-13', caseArch.json.code === 200 && caseArch.json.data?.stage === 'ARCHIVE', '案例归档', caseArch.json.data?.stage || caseArch.json.message)

  // 档案
  await req('PUT', '/archive/ledger', audTok, { projectId: pid, complete: true, ledgerJson: '{}' })
  await req('POST', `/archive/projects/${pid}/confirm`, audTok, {})
  await req('POST', `/archive/projects/${pid}/collect`, audTok, { collectRemark: '资料已归集' })
  const audTodosCollected = await req('GET', '/archive/todos', audTok)
  const wrongLedgerTodo = audTodosCollected.json.data?.find(
    t => t.projectId === pid && t.route?.includes('ledger')
  )
  log('BUG-03', !wrongLedgerTodo, '归集后审核员不应出现台账待办', wrongLedgerTodo ? `route=${wrongLedgerTodo.route}` : 'ok')

  await req('GET', `/archive/cycle-stats?projectId=${pid}`, disTok)
  await req('GET', `/archive/success-rate?projectId=${pid}`, disTok)
  await req('POST', '/archive/cycle-stats/confirm', disTok, { projectId: pid, passed: true, remark: '统计确认' })
  const briefGen = await req('POST', '/archive/brief/generate', disTok, { projectId: pid })
  const briefAudit = await req('POST', '/archive/brief/audit', audTok, { projectId: pid, passed: true, remark: '简报通过' })
  log('A-10', briefAudit.json.code === 200, '简报审核', briefAudit.json.data?.status)
  const finalArch = await req('POST', `/archive/projects/${pid}/archive`, audTok, {})
  const finalProj = await req('GET', `/projects/${pid}/archive/detail`, entTok)
  log('TC-X1', finalArch.json.code === 200 && finalProj.json.data?.stage === 'CLOSED', '端到端主链', `stage=${finalProj.json.data?.stage}`)

  // BUG-04: enterprise acknowledge
  // 需要单独项目测，此处记录静态验证

  // 公开 uploads
  const uploads = await fetch('http://localhost:8080/uploads/')
  log('BUG-08', uploads.status !== 403, 'uploads 目录公开访问（缺陷确认）', `HTTP ${uploads.status}`)

  printSummary()
  process.exit(results.some(r => !r.pass && r.id.startsWith('TC')) ? 1 : 0)
}

function printSummary() {
  const passed = results.filter(r => r.pass).length
  const failed = results.filter(r => !r.pass).length
  console.log(`\n=== 汇总: ${passed} 通过, ${failed} 失败, 共 ${results.length} 项 ===`)
  if (failed) {
    console.log('\n失败项:')
    results.filter(r => !r.pass).forEach(r => console.log(`  - ${r.id}: ${r.msg} ${r.detail}`))
  }
}

main().catch(e => {
  console.error('测试异常:', e)
  process.exit(1)
})
