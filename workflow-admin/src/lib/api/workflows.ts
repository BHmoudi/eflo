import apiClient from './client';
import {
  WorkflowProcess,
  WorkflowState,
  WorkflowTask,
  WorkflowTransition,
  WorkflowInstance,
} from '@/types/workflow';

export interface CreateWorkflowRequest {
  code: string;
  name: string;
  description?: string;
  version?: number;
}

export interface CreateStateRequest {
  code: string;
  name: string;
  description?: string;
  isFinal?: boolean;
  isInitial?: boolean;
  order: number;
}

export interface CreateTaskRequest {
  code: string;
  name: string;
  description?: string;
  taskType: string;
  mandatory: boolean;
  requiresApproval: boolean;
  roleCode?: string;
  slaHours?: number;
  order: number;
}

export interface CreateTransitionRequest {
  name: string;
  fromStateCode: string;
  toStateCode: string;
  condition?: string;
  roleCode?: string;
}

export const workflowApi = {
  // Get all workflows
  async getWorkflows(): Promise<WorkflowProcess[]> {
    const response = await apiClient.get('/api/workflows');
    return response.data;
  },

  // Get workflow by code
  async getWorkflowByCode(code: string): Promise<WorkflowProcess> {
    const response = await apiClient.get(`/api/workflows/${code}`);
    return response.data;
  },

  // Create workflow
  async createWorkflow(data: CreateWorkflowRequest): Promise<WorkflowProcess> {
    const response = await apiClient.post('/api/workflows', data);
    return response.data;
  },

  // Update workflow
  async updateWorkflow(
    code: string,
    data: Partial<CreateWorkflowRequest>
  ): Promise<WorkflowProcess> {
    const response = await apiClient.put(`/api/workflows/${code}`, data);
    return response.data;
  },

  // Delete workflow
  async deleteWorkflow(code: string): Promise<void> {
    await apiClient.delete(`/api/workflows/${code}`);
  },

  // Activate/Deactivate workflow
  async setWorkflowStatus(code: string, isActive: boolean): Promise<WorkflowProcess> {
    const response = await apiClient.patch(`/api/workflows/${code}/status`, { isActive });
    return response.data;
  },

  // Add state to workflow
  async addState(workflowCode: string, data: CreateStateRequest): Promise<WorkflowState> {
    const response = await apiClient.post(`/api/workflows/${workflowCode}/states`, data);
    return response.data;
  },

  // Update state
  async updateState(
    workflowCode: string,
    stateCode: string,
    data: Partial<CreateStateRequest>
  ): Promise<WorkflowState> {
    const response = await apiClient.put(`/api/workflows/${workflowCode}/states/${stateCode}`, data);
    return response.data;
  },

  // Delete state
  async deleteState(workflowCode: string, stateCode: string): Promise<void> {
    await apiClient.delete(`/api/workflows/${workflowCode}/states/${stateCode}`);
  },

  // Add task to state
  async addTask(
    workflowCode: string,
    stateCode: string,
    data: CreateTaskRequest
  ): Promise<WorkflowTask> {
    const response = await apiClient.post(
      `/api/workflows/${workflowCode}/states/${stateCode}/tasks`,
      data
    );
    return response.data;
  },

  // Update task
  async updateTask(
    workflowCode: string,
    stateCode: string,
    taskCode: string,
    data: Partial<CreateTaskRequest>
  ): Promise<WorkflowTask> {
    const response = await apiClient.put(
      `/api/workflows/${workflowCode}/states/${stateCode}/tasks/${taskCode}`,
      data
    );
    return response.data;
  },

  // Delete task
  async deleteTask(workflowCode: string, stateCode: string, taskCode: string): Promise<void> {
    await apiClient.delete(`/api/workflows/${workflowCode}/states/${stateCode}/tasks/${taskCode}`);
  },

  // Add transition
  async addTransition(
    workflowCode: string,
    data: CreateTransitionRequest
  ): Promise<WorkflowTransition> {
    const response = await apiClient.post(`/api/workflows/${workflowCode}/transitions`, data);
    return response.data;
  },

  // Update transition
  async updateTransition(
    workflowCode: string,
    transitionId: number,
    data: Partial<CreateTransitionRequest>
  ): Promise<WorkflowTransition> {
    const response = await apiClient.put(
      `/api/workflows/${workflowCode}/transitions/${transitionId}`,
      data
    );
    return response.data;
  },

  // Delete transition
  async deleteTransition(workflowCode: string, transitionId: number): Promise<void> {
    await apiClient.delete(`/api/workflows/${workflowCode}/transitions/${transitionId}`);
  },

  // Get workflow instances
  async getInstances(filters?: {
    processCode?: string;
    status?: string;
    initiatorId?: number;
  }): Promise<WorkflowInstance[]> {
    const response = await apiClient.get('/api/instances', { params: filters });
    return response.data;
  },

  // Get instance by ID
  async getInstanceById(id: number): Promise<WorkflowInstance> {
    const response = await apiClient.get(`/api/instances/${id}`);
    return response.data;
  },

  // Start workflow instance
  async startInstance(processCode: string, metadata?: any): Promise<WorkflowInstance> {
    const response = await apiClient.post('/api/instances', {
      processCode,
      metadata,
    });
    return response.data;
  },

  // Transition instance
  async transitionInstance(
    instanceId: number,
    toStateCode: string,
    metadata?: any
  ): Promise<WorkflowInstance> {
    const response = await apiClient.post(`/api/instances/${instanceId}/transition`, {
      toStateCode,
      metadata,
    });
    return response.data;
  },
};

export default workflowApi;
