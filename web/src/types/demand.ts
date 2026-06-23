export interface MaterialItem {
  fileUrl?: string
  fileName?: string
  materialType?: string
}

export interface DemandForm {
  title: string
  content?: string
  pilotType?: string
  expectedDays?: number
  contactName?: string
  contactPhone?: string
  materials?: MaterialItem[]
}

export interface DemandDetail {
  projectId: number
  projectNo: string
  title: string
  stage: string
  status: string
  statusLabel: string
  currentNode: string
  content?: string
  pilotType?: string
  expectedDays?: number
  contactName?: string
  contactPhone?: string
  rejectReason?: string
  acceptOpinion?: string
  acceptResult?: string
  enterpriseName?: string
  materials: Array<MaterialItem & { id?: number; version?: number }>
  steps: Array<{ node: string; status: string }>
  logs: Array<{ fromStatus: string; toStatus: string; remark: string; time: string }>
}

export interface DemandTodo {
  projectId: number
  projectNo: string
  title: string
  status: string
  statusLabel: string
  currentNode: string
  action: string
  route: string
  updatedAt: string
}

export interface ProjectCreateResult {
  projectId: number
  projectNo: string
}
