export interface EvaluationDetail {
  projectId: number
  projectNo: string
  title: string
  stage: string
  status: string
  statusLabel: string
  currentNode: string
  enterpriseName?: string
  precheckRemark?: string
  conditionResult?: string
  conditionRemark?: string
  rectifyNotice?: string
  resourceRemark?: string
  resourceRequirement?: string
  feasibilityResult?: string
  feasibilityRemark?: string
  conclusion?: string
  conclusionOpinion?: string
  feedbackContent?: string
  materials: Array<{ fileUrl?: string; fileName?: string; materialType?: string }>
  steps: Array<{ node: string; status: string }>
  logs: Array<{ remark: string; time: string; fromStatus: string; toStatus: string }>
}

export interface EvaluationRequest {
  remark?: string
  notice?: string
  requirement?: string
  conclusion?: string
  opinion?: string
  content?: string
  passed?: boolean
  materials?: Array<{ fileUrl?: string; fileName?: string; materialType?: string }>
}
