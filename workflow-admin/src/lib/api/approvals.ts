import apiClient from './client';
import { ApprovalChain, ApprovalLevel, Approval, ApprovalAction } from '@/types/approval';

export interface CreateChainRequest {
  code: string;
  name: string;
  description?: string;
  workflowCode?: string;
  taskCode?: string;
}

export interface CreateLevelRequest {
  level: number;
  roleCode: string;
  minApprovers: number;
  order: number;
}

export interface ApprovalActionRequest {
  action: 'APPROVED' | 'REJECTED';
  comments?: string;
}

export const approvalApi = {
  // Get all approval chains
  async getApprovalChains(): Promise<ApprovalChain[]> {
    const response = await apiClient.get('/api/approval-chains');
    return response.data;
  },

  // Get approval chain by code
  async getChainByCode(code: string): Promise<ApprovalChain> {
    const response = await apiClient.get(`/api/approval-chains/${code}`);
    return response.data;
  },

  // Create approval chain
  async createChain(data: CreateChainRequest): Promise<ApprovalChain> {
    const response = await apiClient.post('/api/approval-chains', data);
    return response.data;
  },

  // Update approval chain
  async updateChain(code: string, data: Partial<CreateChainRequest>): Promise<ApprovalChain> {
    const response = await apiClient.put(`/api/approval-chains/${code}`, data);
    return response.data;
  },

  // Delete approval chain
  async deleteChain(code: string): Promise<void> {
    await apiClient.delete(`/api/approval-chains/${code}`);
  },

  // Add level to chain
  async addLevel(chainCode: string, data: CreateLevelRequest): Promise<ApprovalLevel> {
    const response = await apiClient.post(`/api/approval-chains/${chainCode}/levels`, data);
    return response.data;
  },

  // Update level
  async updateLevel(
    chainCode: string,
    levelId: number,
    data: Partial<CreateLevelRequest>
  ): Promise<ApprovalLevel> {
    const response = await apiClient.put(`/api/approval-chains/${chainCode}/levels/${levelId}`, data);
    return response.data;
  },

  // Delete level
  async deleteLevel(chainCode: string, levelId: number): Promise<void> {
    await apiClient.delete(`/api/approval-chains/${chainCode}/levels/${levelId}`);
  },

  // Get all approvals
  async getApprovals(filters?: {
    status?: string;
    chainCode?: string;
    workflowInstanceId?: number;
  }): Promise<Approval[]> {
    const response = await apiClient.get('/api/approvals', { params: filters });
    return response.data;
  },

  // Get approval by ID
  async getApprovalById(id: number): Promise<Approval> {
    const response = await apiClient.get(`/api/approvals/${id}`);
    return response.data;
  },

  // Get pending approvals for current user
  async getPendingApprovals(): Promise<Approval[]> {
    const response = await apiClient.get('/api/approvals/pending');
    return response.data;
  },

  // Request approval
  async requestApproval(data: {
    chainCode: string;
    workflowInstanceId: number;
    taskInstanceId?: number;
    metadata?: any;
  }): Promise<Approval> {
    const response = await apiClient.post('/api/approvals', data);
    return response.data;
  },

  // Approve/Reject (Legacy - kept for backward compatibility)
  async submitApprovalAction(
    approvalId: number,
    data: ApprovalActionRequest
  ): Promise<ApprovalAction> {
    const response = await apiClient.post(`/api/approvals/${approvalId}/action`, data);
    return response.data;
  },

  // Approve
  async approve(approvalId: number, comments?: string): Promise<Approval> {
    const response = await apiClient.post(`/api/approvals/${approvalId}/approve`, { comments });
    return response.data;
  },

  // Reject
  async reject(approvalId: number, comments?: string): Promise<Approval> {
    const response = await apiClient.post(`/api/approvals/${approvalId}/reject`, { comments });
    return response.data;
  },

  // Delegate
  async delegate(approvalId: number, toUserId: number, reason?: string): Promise<Approval> {
    const response = await apiClient.post(`/api/approvals/${approvalId}/delegate`, { toUserId, reason });
    return response.data;
  },

  // Get approval actions
  async getApprovalActions(approvalId: number): Promise<ApprovalAction[]> {
    const response = await apiClient.get(`/api/approvals/${approvalId}/actions`);
    return response.data;
  },

  // Cancel approval
  async cancelApproval(approvalId: number): Promise<Approval> {
    const response = await apiClient.post(`/api/approvals/${approvalId}/cancel`);
    return response.data;
  },
};

export default approvalApi;
