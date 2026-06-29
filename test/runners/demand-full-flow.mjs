#!/usr/bin/env node
/**
 * 入口：中试需求管理全流程
 *
 * 用法：
 *   node runners/demand-full-flow.mjs
 *   npm run demand:flow
 */
import { config } from '../config/env.js'
import { banner, elapsed, fail } from '../common/logger.js'
import { clearAuthCache } from '../common/auth.js'
import { runDemandFullFlow } from '../scenarios/demand/full-flow.mjs'

async function main() {
  console.log(`API: ${config.baseUrl}`)
  console.log('请确保后端已启动: cd server && mvn spring-boot:run\n')

  try {
    clearAuthCache()
    const result = await runDemandFullFlow()

    banner('测试通过')
    console.log(`项目编号: ${result.projectNo}`)
    console.log(`项目 ID:  ${result.projectId}`)
    console.log(`最终状态: ${result.finalStatus}`)
    console.log(`耗时:     ${elapsed()}`)
    process.exit(0)
  } catch (e) {
    banner('测试失败')
    fail(e instanceof Error ? e.message : String(e))
    if (e instanceof Error && e.stack) {
      console.error(e.stack)
    }
    console.log(`耗时: ${elapsed()}`)
    process.exit(1)
  }
}

main()
