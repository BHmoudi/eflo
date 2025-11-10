import apiClient from './client';
import { Role, UserRole, RoleHierarchy } from '@/types/role';

export interface CreateRoleRequest {
  code: string;
  name: string;
  description?: string;
  parentRoleCode?: string;
  level: number;
}

export interface AssignRoleRequest {
  userId: number;
  roleCode: string;
  expiresAt?: string;
}

export const roleApi = {
  // Get all roles
  async getRoles(): Promise<Role[]> {
    const response = await apiClient.get('/api/roles');
    return response.data;
  },

  // Get role by code
  async getRoleByCode(code: string): Promise<Role> {
    const response = await apiClient.get(`/api/roles/${code}`);
    return response.data;
  },

  // Create role
  async createRole(data: CreateRoleRequest): Promise<Role> {
    const response = await apiClient.post('/api/roles', data);
    return response.data;
  },

  // Update role
  async updateRole(code: string, data: Partial<CreateRoleRequest>): Promise<Role> {
    const response = await apiClient.put(`/api/roles/${code}`, data);
    return response.data;
  },

  // Delete role
  async deleteRole(code: string): Promise<void> {
    await apiClient.delete(`/api/roles/${code}`);
  },

  // Get role hierarchy
  async getRoleHierarchy(): Promise<RoleHierarchy[]> {
    const response = await apiClient.get('/api/roles/hierarchy');
    return response.data;
  },

  // Assign role to user
  async assignRole(data: AssignRoleRequest): Promise<UserRole> {
    const response = await apiClient.post('/api/user-roles', data);
    return response.data;
  },

  // Remove role from user
  async removeUserRole(userRoleId: number): Promise<void> {
    await apiClient.delete(`/api/user-roles/${userRoleId}`);
  },

  // Get user roles
  async getUserRoles(userId?: number): Promise<UserRole[]> {
    // Default to user ID 1 if not provided (current user)
    const uid = userId || 1;
    const response = await apiClient.get(`/api/user-roles/user/${uid}`);
    return response.data;
  },

  // Get users by role
  async getUsersByRole(roleCode: string): Promise<UserRole[]> {
    const response = await apiClient.get(`/api/roles/${roleCode}/users`);
    return response.data;
  },
};

export default roleApi;
