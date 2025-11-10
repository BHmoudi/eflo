import apiClient from './client';
import { TaskInstance } from '@/types/workflow';

export interface TaskFilters {
  workflowInstanceId?: number;
  status?: string;
  assignedToUserId?: number;
  assignedToRoleCode?: string;
  taskType?: string;
}

export interface CompleteTaskRequest {
  metadata?: any;
  documents?: string[];
}

export const taskApi = {
  // Get all tasks with filters
  async getTasks(filters?: TaskFilters): Promise<TaskInstance[]> {
    const response = await apiClient.get('/api/workflow-tasks', { params: filters });
    return response.data;
  },

  // Get task by ID
  async getTaskById(id: number): Promise<TaskInstance> {
    const response = await apiClient.get(`/api/workflow-tasks/${id}`);
    return response.data;
  },

  // Get my tasks (assigned to current user)
  async getMyTasks(filters?: { status?: string }): Promise<TaskInstance[]> {
    const response = await apiClient.get('/api/workflow-tasks/my-tasks', { params: filters });
    return response.data;
  },

  // Get tasks assigned to my roles
  async getMyRoleTasks(filters?: { status?: string }): Promise<TaskInstance[]> {
    const response = await apiClient.get('/api/workflow-tasks/my-role-tasks', { params: filters });
    return response.data;
  },

  // Claim task (assign to self)
  async claimTask(taskId: number): Promise<TaskInstance> {
    const response = await apiClient.post(`/api/workflow-tasks/${taskId}/claim`);
    return response.data;
  },

  // Start task
  async startTask(taskId: number, metadata?: any): Promise<TaskInstance> {
    const response = await apiClient.post(`/api/workflow-tasks/${taskId}/start`, { metadata });
    return response.data;
  },

  // Complete task
  async completeTask(taskId: number, data?: CompleteTaskRequest): Promise<TaskInstance> {
    const response = await apiClient.post(`/api/workflow-tasks/${taskId}/complete`, data);
    return response.data;
  },

  // Reassign task
  async reassignTask(taskId: number, userId: number): Promise<TaskInstance> {
    const response = await apiClient.post(`/api/workflow-tasks/${taskId}/reassign`, { userId });
    return response.data;
  },

  // Upload document for task
  async uploadDocument(taskId: number, file: File): Promise<{ url: string; filename: string }> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await apiClient.post(`/api/workflow-tasks/${taskId}/documents`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Get task documents
  async getTaskDocuments(taskId: number): Promise<{ url: string; filename: string }[]> {
    const response = await apiClient.get(`/api/workflow-tasks/${taskId}/documents`);
    return response.data;
  },

  // Add task comment
  async addComment(taskId: number, comment: string): Promise<any> {
    const response = await apiClient.post(`/api/workflow-tasks/${taskId}/comments`, { comment });
    return response.data;
  },

  // Get task comments
  async getComments(taskId: number): Promise<any[]> {
    const response = await apiClient.get(`/api/workflow-tasks/${taskId}/comments`);
    return response.data;
  },
};

export default taskApi;
