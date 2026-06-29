import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import { pickRandomItems } from '../../common/random.js'
import { step } from '../../common/logger.js'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const FIXTURES_DIR = path.join(__dirname, '../../fixtures/materials')

/** 与 Web VerifyView 材料清单一致 */
export const MATERIAL_CATALOG = [
  { type: '中试方案 / 可行性说明', file: 'pilot-plan.txt' },
  { type: '企业资质或营业执照复印件', file: 'enterprise-license.txt' },
  { type: '技术资料 / 产品说明', file: 'tech-spec.txt' },
  { type: '其他补充材料', file: 'supplement.txt' }
]

/**
 * 随机选取 1~3 种材料类型，上传对应 fixture 文件
 * @returns {Promise<Array<{fileUrl,fileName,materialType}>>}
 */
export async function uploadRandomMaterials(client) {
  const selected = pickRandomItems(MATERIAL_CATALOG, 1, 3)
  step('enterprise', '随机选取材料类型', selected.map((s) => s.type).join('、'))

  const materials = []
  for (const item of selected) {
    const filePath = path.join(FIXTURES_DIR, item.file)
    if (!fs.existsSync(filePath)) {
      throw new Error(`材料样例不存在: ${filePath}，请先运行 npm run fixtures:init`)
    }
    const uploaded = await uploadSingleFile(client, filePath)
    materials.push({
      fileUrl: uploaded.fileUrl,
      fileName: uploaded.fileName,
      materialType: item.type
    })
    step('enterprise', '上传材料', `${item.type} ← ${uploaded.fileName}`)
  }
  return materials
}

async function uploadSingleFile(client, filePath) {
  const buffer = fs.readFileSync(filePath)
  const fileName = path.basename(filePath)
  const form = new FormData()
  form.append('file', new Blob([buffer], { type: 'text/plain' }), fileName)
  return client.uploadMultipart('/files/upload', form, `上传 ${fileName}`)
}
