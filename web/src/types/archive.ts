export interface ArchiveDetail {
  projectId: number
  projectNo: string
  title: string
  stage: string
  status: string
  statusLabel: string
  currentNode: string
  enterpriseName?: string
  ledgerJson?: string
  collectRemark?: string
  briefId?: number
  briefTitle?: string
  briefContent?: string
  briefAuditStatus?: string
  briefAuditRemark?: string
  steps?: { node: string; status: string }[]
}

export interface ArchiveRequest {
  projectId?: number
  complete?: boolean
  passed?: boolean
  remark?: string
  ledgerJson?: string
  title?: string
  content?: string
  collectRemark?: string
}

export interface ArchiveStats {
  available: boolean
  message: string
  months: string[]
  cycleDays: number[]
  stageDistribution: { name: string; value: number }[]
  totalProjects: number
  closedProjects: number
  successRate: number
}

export interface BriefDetail {
  id: number
  projectId: number
  projectNo: string
  title: string
  briefTitle: string
  content: string
  auditStatus: string
  statsJson?: string
  createdAt: string
}
