import apiClient from './client';

export interface EmployeeHierarchy {
  id: number;
  employeeId: number;
  managerId: number;
  businessUnitId?: number;
  effectiveFrom: string;
  effectiveTo?: string;
  isActive: boolean;
  level?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface HierarchyWithDetails extends EmployeeHierarchy {
  employeeEmail?: string;
  employeeFirstName?: string;
  employeeLastName?: string;
  managerEmail?: string;
  managerFirstName?: string;
  managerLastName?: string;
  businessUnitName?: string;
  businessUnitCode?: string;
}

export interface CreateHierarchyDto {
  employeeId: number;
  managerId: number;
  businessUnitId?: number;
  effectiveFrom: string;
  effectiveTo?: string;
}

export interface UpdateHierarchyDto {
  managerId?: number;
  businessUnitId?: number;
  effectiveFrom?: string;
  effectiveTo?: string;
  isActive?: boolean;
}

export interface ManagementChainNode {
  id: number;
  employeeId: number;
  email: string;
  firstName: string;
  lastName: string;
  jobTitle?: string;
  level: number;
  managerId?: number;
}

export interface DirectReport {
  id: number;
  employeeId: number;
  email: string;
  firstName: string;
  lastName: string;
  jobTitle?: string;
  businessUnitId?: number;
  businessUnitName?: string;
  effectiveFrom: string;
}

export const hierarchyApi = {
  /**
   * Create a new hierarchy relationship (assign manager to employee)
   */
  async createHierarchy(data: CreateHierarchyDto): Promise<EmployeeHierarchy> {
    const response = await apiClient.post('/api/hierarchies', data);
    return response.data;
  },

  /**
   * Update an existing hierarchy relationship
   */
  async updateHierarchy(id: number, data: UpdateHierarchyDto): Promise<EmployeeHierarchy> {
    const response = await apiClient.put(`/api/hierarchies/${id}`, data);
    return response.data;
  },

  /**
   * Deactivate a hierarchy relationship (set effectiveTo to current date)
   */
  async deactivateHierarchy(id: number): Promise<void> {
    await apiClient.post(`/api/hierarchies/${id}/deactivate`);
  },

  /**
   * Get the current hierarchy for an employee (their current manager)
   */
  async getEmployeeHierarchy(employeeId: number): Promise<HierarchyWithDetails> {
    const response = await apiClient.get(`/api/hierarchies/employee/${employeeId}`);
    return response.data;
  },

  /**
   * Get all direct reports for a manager
   */
  async getDirectReports(managerId: number): Promise<DirectReport[]> {
    const response = await apiClient.get(`/api/hierarchies/manager/${managerId}/direct-reports`);
    return response.data;
  },

  /**
   * Get the full management chain for an employee (all managers up to top)
   */
  async getManagementChain(employeeId: number): Promise<ManagementChainNode[]> {
    const response = await apiClient.get(`/api/hierarchies/employee/${employeeId}/management-chain`);
    return response.data;
  },

  /**
   * Get all subordinates for a manager (direct and indirect reports)
   */
  async getAllSubordinates(managerId: number): Promise<DirectReport[]> {
    const response = await apiClient.get(`/api/hierarchies/manager/${managerId}/all-subordinates`);
    return response.data;
  },

  /**
   * Get hierarchies by business unit
   */
  async getBusinessUnitHierarchies(businessUnitId: number): Promise<HierarchyWithDetails[]> {
    const response = await apiClient.get(`/api/hierarchies/business-unit/${businessUnitId}`);
    return response.data;
  },

  /**
   * Get hierarchy by ID
   */
  async getHierarchyById(id: number): Promise<HierarchyWithDetails> {
    const response = await apiClient.get(`/api/hierarchies/${id}`);
    return response.data;
  },
};

export default hierarchyApi;
