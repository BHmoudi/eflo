export enum ApprovalStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CANCELLED = 'CANCELLED',
}

export interface ApprovalLevel {
  id?: number;
  level: number;
  roleCode: string;
  roleName?: string;
  minApprovers: number;
  order: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface ApprovalChain {
  id?: number;
  code: string;
  name: string;
  description?: string;
  workflowCode?: string;
  taskCode?: string;
  isActive: boolean;
  levels: ApprovalLevel[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Approval {
  id: number;
  chainCode: string;
  chainName?: string;
  workflowInstanceId: number;
  taskInstanceId?: number;
  currentLevel: number;
  status: ApprovalStatus;
  requestedBy: number;
  requestedByName?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  metadata?: Record<string, any>;
}

export interface ApprovalAction {
  id: number;
  approvalId: number;
  level: number;
  userId: number;
  userName?: string;
  roleCode: string;
  action: 'APPROVED' | 'REJECTED';
  comments?: string;
  createdAt: string;
}
