/**
 * 中试需求管理模块 API 封装（对齐 web/src/api/demand.ts）
 */
export class DemandApi {
  /** @param {import('../../common/client.js').ApiClient} client */
  constructor(client) {
    this.client = client
  }

  createProject(form) {
    return this.client.postOk('/projects/demand/create', form, '创建需求项目')
  }

  async getPreview(projectId) {
    const json = await this.client.get(`/projects/${projectId}/demand/preview`)
    if (json.code !== 200) throw new Error(json.message)
    return json.data
  }

  saveDraft(projectId, form) {
    return this.client.postOk(`/projects/${projectId}/demand/draft`, form, '保存草稿')
  }

  submit(projectId, form) {
    return this.client.postOk(`/projects/${projectId}/demand/submit`, form, '提交需求')
  }

  acceptRegister(projectId, remark) {
    return this.client.postOk(
      `/projects/${projectId}/demand/accept-register`,
      { remark: remark || '自动化测试：调度员受理登记' },
      '受理登记'
    )
  }

  /**
   * 材料核验（与 Web VerifyView 一致：complete + accepted）
   * complete=false → 退回；complete=true + accepted=true → 同意受理
   */
  verify(projectId, { complete, accepted, opinion }) {
    return this.client.postOk(
      `/projects/${projectId}/demand/verify`,
      { complete, accepted, opinion },
      '材料核验'
    )
  }

  receipt(projectId) {
    return this.client.postOk(`/projects/${projectId}/demand/receipt`, {}, '回执签收')
  }

  listTodos() {
    return this.client.get('/demand/todos').then((j) => {
      if (j.code !== 200) throw new Error(j.message)
      return j.data
    })
  }

  progress(projectId) {
    return this.client.get(`/projects/${projectId}/demand/progress`).then((j) => {
      if (j.code !== 200) throw new Error(j.message)
      return j.data
    })
  }
}
