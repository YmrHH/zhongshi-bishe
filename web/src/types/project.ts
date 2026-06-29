export interface ProjectProgress {
  projectId: number
  projectNo: string
  title: string
  stage: string
  status: string
  statusLabel: string
  currentNode: string
  moduleName: string
  moduleSteps: Array<{ node: string; status: string }>
  content?: string
  acceptOpinion?: string
  rejectReason?: string
  submittedAt?: string
  logs: Array<{ fromStatus: string; toStatus: string; remark: string; time: string }>
}
