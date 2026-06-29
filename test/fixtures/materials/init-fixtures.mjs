import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import { MATERIAL_CATALOG } from '../../lib/files/upload.js'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const dir = path.join(__dirname, '.')

const CONTENTS = {
  'pilot-plan.txt': '【自动化测试】中试方案与可行性说明\n项目目标：验证工艺放大可行性。\n',
  'enterprise-license.txt': '【自动化测试】企业资质复印件\n统一社会信用代码：91440000MA00000000\n',
  'tech-spec.txt': '【自动化测试】技术资料与产品说明\n产品类别：新材料中试验证。\n',
  'supplement.txt': '【自动化测试】其他补充材料\n附件说明：测试用补充文档。\n'
}

if (!fs.existsSync(dir)) {
  fs.mkdirSync(dir, { recursive: true })
}

for (const item of MATERIAL_CATALOG) {
  const p = path.join(dir, item.file)
  fs.writeFileSync(p, CONTENTS[item.file] || `测试文件 ${item.type}\n`, 'utf8')
  console.log('已生成:', p)
}

console.log('材料 fixtures 就绪。')
