export enum TaskStatus {
  PENDING = 'PENDING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  REJECTED = 'REJECTED',
  ON_HOLD = 'ON_HOLD',
}

export enum TaskType {
  DOCUMENT = 'DOCUMENT',
  ACTION = 'ACTION',
  CONTROL = 'CONTROL',
}

export interface WorkflowTask {
  id?: number;
  code: string;
  name: string;
  description?: string;
  taskType: TaskType;
  mandatory: boolean;
  requiresApproval: boolean;
  roleCode?: string;
  slaHours?: number;
  order: number;
  // Additional fields for advanced task configuration
  approvalChainId?: number;
  requiredDocuments?: string[];
  inputFields?: any[];
  formDefinition?: any;
  validationRules?: any;
  configuration?: Record<string, any>;
  dependencies?: any[];
  conditions?: any[];
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkflowState {
  id?: number;
  code: string;
  name: string;
  description?: string;
  isFinal: boolean;
  isInitial: boolean;
  tasks: WorkflowTask[];
  order: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkflowTransition {
  id?: number;
  name: string;
  fromStateCode: string;
  toStateCode: string;
  condition?: string;
  roleCode?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkflowProcess {
  id?: number;
  code: string;
  name: string;
  description?: string;
  version: number;
  isActive: boolean;
  states: WorkflowState[];
  transitions: WorkflowTransition[];
  createdAt?: string;
  updatedAt?: string;
}

export interface WorkflowInstance {
  id: number;
  processCode: string;
  processName?: string;
  currentStateCode: string;
  currentStateName?: string;
  status: string;
  initiatorId: number;
  initiatorName?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string;
  metadata?: Record<string, any>;
}

export interface TaskInstance {
  id: number;
  workflowInstanceId: number;
  taskCode: string;
  taskName?: string;
  taskType: TaskType;
  status: TaskStatus;
  assignedToUserId?: number;
  assignedToUserName?: string;
  assignedToRoleCode?: string;
  dueDate?: string;
  startedAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
  metadata?: Record<string, any>;
}
