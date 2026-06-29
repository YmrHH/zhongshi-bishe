/**
 * 中试需求管理 — 全流程场景
 *
 * 流程：企业发起 → 填表上传 → 提交 → 调度受理 → 审核核验 → 企业回执签收
 */
import { loginAs } from '../../common/auth.js'
import { assertEqual, assertTrue } from '../../common/assert.js'
import { step, ok, banner } from '../../common/logger.js'
import { randomInt, randomPilotType, timestampSuffix } from '../../common/random.js'
import { uploadRandomMaterials } from '../../lib/files/upload.js'
import { DemandApi } from '../../lib/demand/api.js'

/**
 * @returns {Promise<{ projectId: number, projectNo: string, finalStatus: string }>}
 */
export async function runDemandFullFlow() {
  banner('中试需求管理 — 全流程自动化测试')

  // ── 1. 企业：发起需求、填写信息、随机上传材料、提交 ──
  step('enterprise', '阶段 1/4', '发起需求并提交')
  const enterprise = await loginAs('enterprise')
  const demandEnt = new DemandApi(enterprise)

  const materials = await uploadRandomMaterials(enterprise)

  const form = {
    title: `自动化测试中试需求-${timestampSuffix()}`,
    content: '本项目为自动化测试脚本提交的中试需求，验证需求受理全流程。',
    pilotType: randomPilotType(),
    expectedDays: randomInt(15, 90),
    contactName: '测试联系人',
    contactPhone: '13800138000',
    materials
  }

  step('enterprise', '创建项目', form.title)
  const created = await demandEnt.createProject(form)
  const projectId = created.projectId
  ok(`项目已创建 projectId=${projectId} projectNo=${created.projectNo}`)

  step('enterprise', '提交需求')
  const submitted = await demandEnt.submit(projectId, form)
  assertEqual(submitted.status, 'SUBMITTED', '提交后状态')
  ok(`状态=${submitted.status} (${submitted.statusLabel})`)

  // ── 2. 调度员：受理登记 ──
  step('dispatcher', '阶段 2/4', '需求受理登记')
  const dispatcher = await loginAs('dispatcher')
  const demandDis = new DemandApi(dispatcher)

  const disTodos = await demandDis.listTodos()
  assertTrue(
    disTodos.some((t) => t.projectId === projectId),
    '调度员待办应包含本项目'
  )
  ok('调度员待办已出现本项目')

  const accepting = await demandDis.acceptRegister(projectId)
  assertEqual(accepting.status, 'ACCEPTING', '受理登记后状态')
  ok(`状态=${accepting.status} (${accepting.statusLabel})`)

  // ── 3. 审核员：材料核验 + 同意受理 ──
  step('auditor', '阶段 3/4', '材料核验与受理审核')
  const auditor = await loginAs('auditor')
  const demandAud = new DemandApi(auditor)

  const audTodos = await demandAud.listTodos()
  assertTrue(
    audTodos.some((t) => t.projectId === projectId && t.status === 'ACCEPTING'),
    '审核员待办应包含待核验项目'
  )
  ok('审核员待办已出现本项目')

  const verified = await demandAud.verify(projectId, {
    complete: true,
    accepted: true,
    opinion: '自动化测试：材料齐全，同意受理'
  })
  assertEqual(verified.status, 'ACCEPTED', '核验通过后状态')
  ok(`状态=${verified.status} (${verified.statusLabel})`)
  assertTrue(
    (verified.materials?.length || 0) >= materials.length,
    '审核详情应包含已上传材料'
  )
  ok(`材料数量=${verified.materials?.length ?? 0}`)

  // ── 4. 企业：受理回执签收 ──
  step('enterprise', '阶段 4/4', '受理回执签收')
  const receipted = await demandEnt.receipt(projectId)
  assertEqual(receipted.status, 'RECEIPTED', '回执签收后状态')
  ok(`状态=${receipted.status} (${receipted.statusLabel})`)

  const progress = await demandEnt.progress(projectId)
  assertEqual(progress.stage, 'DEMAND', '需求阶段未归档前 stage')
  assertEqual(progress.status, 'RECEIPTED', '进度详情状态')
  ok(`进度节点=${progress.currentNode}`)

  return {
    projectId,
    projectNo: created.projectNo,
    finalStatus: receipted.status
  }
}
