export interface FeedbackDetail {
  projectId: number
  projectNo: string
  title: string
  stage: string
  status: string
  statusLabel: string
  currentNode: string
  enterpriseName?: string
  technicianName?: string
  reportContent?: string
  fileUrl?: string
  fileName?: string
  validateRemark?: string
  modifyRemark?: string
  reviewOpinion?: string
  reviewFeedback?: string
  steps?: { node: string; status: string }[]
  logs?: { fromStatus: string; toStatus: string; remark: string; time: string }[]
}

export interface FeedbackRequest {
  passed?: boolean
  remark?: string
  content?: string
  fileUrl?: string
  fileName?: string
}
