import apiClient from './client';

export interface ApprovalChain {
  id: number;
  chainCode: string;
  chainName: string;
  description?: string;
  processCode?: string;
  taskCode?: string;
  isActive: boolean;
  levels?: ApprovalLevel[];
  createdAt: string;
  updatedAt?: string;
}

export interface ApprovalLevel {
  id: number;
  chainId: number;
  level: number;
  roleCode: string;
  roleName?: string;
  minApprovers: number;
  order: number;
  createdAt: string;
}

export interface CreateChainRequest {
  chainCode: string;
  chainName: string;
  description?: string;
  processCode?: string;
  taskCode?: string;
}

export interface CreateLevelRequest {
  level: number;
  roleCode: string;
  minApprovers: number;
  order: number;
}

export const approvalChainApi = {
  // Get all approval chains
  async getApprovalChains(): Promise<ApprovalChain[]> {
    const response = await apiClient.get('/api/approval-chains');
    return response.data;
  },

  // Get approval chain by ID
  async getChainById(id: number): Promise<ApprovalChain> {
    const response = await apiClient.get(`/api/approval-chains/${id}`);
    return response.data;
  },

  // Create approval chain
  async createChain(data: CreateChainRequest): Promise<ApprovalChain> {
    const response = await apiClient.post('/api/approval-chains', data);
    return response.data;
  },

  // Update approval chain
  async updateChain(id: number, data: Partial<CreateChainRequest>): Promise<ApprovalChain> {
    const response = await apiClient.put(`/api/approval-chains/${id}`, data);
    return response.data;
  },

  // Get levels for a chain
  async getChainLevels(chainId: number): Promise<ApprovalLevel[]> {
    const response = await apiClient.get(`/api/approval-chains/${chainId}/levels`);
    return response.data;
  },

  // Add level to chain
  async addLevel(chainId: number, data: CreateLevelRequest): Promise<ApprovalLevel> {
    const response = await apiClient.post(`/api/approval-chains/${chainId}/levels`, data);
    return response.data;
  },

  // Delete level
  async deleteLevel(chainId: number, levelId: number): Promise<void> {
    await apiClient.delete(`/api/approval-chains/${chainId}/levels/${levelId}`);
  },
};

export default approvalChainApi;
