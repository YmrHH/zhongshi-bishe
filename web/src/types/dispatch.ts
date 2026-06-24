export interface DispatchDetail {
  projectId: number
  projectNo: string
  title: string
  stage: string
  status: string
  statusLabel: string
  currentNode: string
  enterpriseName?: string
  taskId?: number
  resourceId?: number
  resourceName?: string
  technicianId?: number
  technicianName?: string
  progressPct?: number
  taskRemark?: string
  evaluationSummary?: {
    conclusion?: string
    conclusionOpinion?: string
    resourceRequirement?: string
    resourceRemark?: string
    conditionResult?: string
    feasibilityResult?: string
    feedbackContent?: string
  }
  resources?: Array<{ id: number; name: string; type: string; capacity: string; status: string }>
  technicians?: Array<{ id: number; realName: string; orgName: string }>
  progressRecords?: Array<{ progressPct: number; content: string; reportTime: string }>
  steps: Array<{ node: string; status: string }>
  logs: Array<{ fromStatus: string; toStatus: string; remark: string; time: string }>
}

export interface DispatchRequest {
  resourceId?: number
  technicianId?: number
  passed?: boolean
  remark?: string
  content?: string
  progressPct?: number
}
