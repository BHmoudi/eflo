import apiClient from './client';

export interface WorkflowInstance {
  id: number;
  processId: number;
  processCode: string;
  processName: string;
  orderId?: number;
  currentStateId: number;
  currentStateName: string;
  status: 'RUNNING' | 'PAUSED' | 'COMPLETED' | 'CANCELLED' | 'ERROR';
  startedAt?: string;
  completedAt?: string;
  cancelledAt?: string;
  initiatedBy: string;
  metadata?: Record<string, any>;
  createdAt: string;
  updatedAt?: string;
}

export interface CreateInstanceRequest {
  processCode: string;
  orderId?: number;
  metadata?: Record<string, any>;
}

export interface TransitionRequest {
  toStateCode: string;
  metadata?: Record<string, any>;
}

export const instanceApi = {
  // Get all instances
  async getInstances(filters?: {
    status?: string;
    processCode?: string;
  }): Promise<WorkflowInstance[]> {
    const url = filters?.status
      ? `/api/instances?status=${filters.status}`
      : '/api/instances';
    const response = await apiClient.get(url, { params: filters?.processCode ? { processCode: filters.processCode } : {} });
    return response.data;
  },

  // Get instance by ID
  async getInstanceById(id: number): Promise<WorkflowInstance> {
    const response = await apiClient.get(`/api/instances/${id}`);
    return response.data;
  },

  // Create instance
  async createInstance(data: CreateInstanceRequest): Promise<WorkflowInstance> {
    const response = await apiClient.post('/api/instances', data);
    return response.data;
  },

  // Start instance
  async startInstance(id: number): Promise<WorkflowInstance> {
    const response = await apiClient.post(`/api/instances/${id}/start`);
    return response.data;
  },

  // Execute transition
  async executeTransition(id: number, data: TransitionRequest): Promise<void> {
    await apiClient.post(`/api/instances/${id}/transition`, data);
  },

  // Pause instance
  async pauseInstance(id: number): Promise<void> {
    await apiClient.post(`/api/instances/${id}/pause`);
  },

  // Resume instance
  async resumeInstance(id: number): Promise<void> {
    await apiClient.post(`/api/instances/${id}/resume`);
  },

  // Cancel instance
  async cancelInstance(id: number, reason?: string): Promise<void> {
    const url = reason
      ? `/api/instances/${id}/cancel?reason=${encodeURIComponent(reason)}`
      : `/api/instances/${id}/cancel`;
    await apiClient.post(url);
  },
};

export default instanceApi;
