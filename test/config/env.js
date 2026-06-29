/** 测试环境配置（可通过环境变量覆盖） */
export const config = {
  baseUrl: process.env.API_BASE_URL || 'http://localhost:8080/api',
  password: process.env.TEST_PASSWORD || '123456',
  accounts: {
    enterprise: process.env.TEST_USER_ENTERPRISE || 'enterprise',
    dispatcher: process.env.TEST_USER_DISPATCHER || 'dispatcher',
    auditor: process.env.TEST_USER_AUDITOR || 'auditor',
    technician: process.env.TEST_USER_TECHNICIAN || 'technician'
  },
  timeoutMs: Number(process.env.TEST_TIMEOUT_MS || 15000)
}
