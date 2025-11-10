import apiClient from './client';

export interface User {
  id: number;
  keycloakId?: string;
  keycloakUsername?: string;
  employeeNumber?: string;
  userIpn?: string;
  email: string;
  firstName: string;
  lastName: string;
  fullName?: string;
  phoneNumber?: string;
  mobileNumber?: string;
  officeExtension?: string;
  jobTitle?: string;
  department?: string;
  addressLine1?: string;
  addressLine2?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  hireDate?: string;
  terminationDate?: string;
  isActive: boolean;
  lastLoginAt?: string;
  loginCount?: number;
  profilePictureUrl?: string;
  bio?: string;
  preferences?: string;
  createdAt?: string;
  updatedAt?: string;
  // Legacy fields for backward compatibility
  role?: string;
  phone?: string;
  active?: boolean;
}

export interface CreateUserDto {
  email: string;
  firstName: string;
  lastName: string;
  employeeNumber?: string;
  userIpn?: string;
  phoneNumber?: string;
  mobileNumber?: string;
  officeExtension?: string;
  jobTitle?: string;
  department?: string;
  addressLine1?: string;
  addressLine2?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  hireDate?: string;
  profilePictureUrl?: string;
  bio?: string;
  preferences?: string;
  password?: string;
  // Legacy fields for backward compatibility
  role?: string;
  phone?: string;
}

export interface UpdateUserDto {
  email?: string;
  firstName?: string;
  lastName?: string;
  employeeNumber?: string;
  userIpn?: string;
  phoneNumber?: string;
  mobileNumber?: string;
  officeExtension?: string;
  jobTitle?: string;
  department?: string;
  addressLine1?: string;
  addressLine2?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  hireDate?: string;
  terminationDate?: string;
  profilePictureUrl?: string;
  bio?: string;
  preferences?: string;
  // Legacy fields for backward compatibility
  role?: string;
  phone?: string;
}

export interface BusinessUnit {
  id: number;
  code: string;
  name: string;
  rrfCode: string;
  type: string;
  description?: string;
  isActive: boolean;
}

export interface UserBusinessUnit {
  id: number;
  userId: number;
  businessUnitId: number;
  businessUnit: BusinessUnit;
  isPrimary: boolean;
  assignedAt: string;
}

export interface Hierarchy {
  id: number;
  employeeId: number;
  managerId: number;
  effectiveFrom: string;
  effectiveTo?: string;
  manager?: User;
}

export interface ActivityLog {
  id: number;
  userId: number;
  activityType: string;
  description: string;
  ipAddress?: string;
  userAgent?: string;
  timestamp: string;
}

export interface UserRole {
  id: number;
  userId: number;
  roleCode: string;
  roleName: string;
  roleSource: string;
  assignedAt: string;
}

export interface UserDetailResponse {
  user: User;
  businessUnits: UserBusinessUnit[];
  roles: UserRole[];
  primaryBusinessUnit?: UserBusinessUnit;
  hierarchy?: Hierarchy;
  directReports?: User[];
}

export interface PaginatedUsers {
  content: User[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const userApi = {
  // Get all users (paginated)
  async getUsers(page: number = 0, size: number = 20): Promise<PaginatedUsers> {
    const response = await apiClient.get('/api/users', {
      params: { page, size }
    });
    return response.data;
  },

  // Get user by ID
  async getUserById(id: number): Promise<User> {
    const response = await apiClient.get(`/api/users/${id}`);
    return response.data;
  },

  // Create new user
  async createUser(userData: CreateUserDto): Promise<User> {
    const response = await apiClient.post('/api/users', userData);
    return response.data;
  },

  // Update user
  async updateUser(id: number, userData: UpdateUserDto): Promise<User> {
    const response = await apiClient.put(`/api/users/${id}`, userData);
    return response.data;
  },

  // Delete user
  async deleteUser(id: number): Promise<void> {
    await apiClient.delete(`/api/users/${id}`);
  },

  // Deactivate user
  async deactivateUser(id: number): Promise<void> {
    await apiClient.post(`/api/users/${id}/deactivate`);
  },

  // Reactivate user
  async reactivateUser(id: number): Promise<void> {
    await apiClient.post(`/api/users/${id}/reactivate`);
  },

  // Get active users only
  async getActiveUsers(): Promise<User[]> {
    const response = await apiClient.get('/api/users/active');
    return response.data;
  },

  // Search users
  async searchUsers(query: string): Promise<User[]> {
    const response = await apiClient.get('/api/users/search', {
      params: { q: query }
    });
    return response.data;
  },

  // Get users by role
  async getUsersByRole(role: string): Promise<User[]> {
    const response = await apiClient.get(`/api/users/by-role`, { params: { role } });
    return response.data;
  },

  // Get user's manager
  async getUserManager(userId: number): Promise<User> {
    const response = await apiClient.get(`/api/users/${userId}/manager`);
    return response.data;
  },

  // Get user detail with all relationships
  async getUserDetail(userId: number): Promise<UserDetailResponse> {
    const response = await apiClient.get(`/api/users/${userId}/detail`);
    return response.data;
  },

  // Get user's business units
  async getUserBusinessUnits(userId: number): Promise<UserBusinessUnit[]> {
    const response = await apiClient.get(`/api/users/${userId}/business-units`);
    return response.data;
  },

  // Get user's hierarchy (manager)
  async getUserHierarchy(userId: number): Promise<Hierarchy | null> {
    try {
      const response = await apiClient.get(`/api/users/${userId}/hierarchy`);
      return response.data;
    } catch (error) {
      return null;
    }
  },

  // Get user's direct reports
  async getUserDirectReports(userId: number): Promise<User[]> {
    const response = await apiClient.get(`/api/users/${userId}/direct-reports`);
    return response.data;
  },

  // Get user's roles
  async getUserRoles(userId: number): Promise<UserRole[]> {
    const response = await apiClient.get(`/api/users/${userId}/roles`);
    return response.data;
  },

  // Get user's activity logs
  async getUserActivityLogs(userId: number, page: number = 0, size: number = 10): Promise<{
    content: ActivityLog[];
    totalElements: number;
    totalPages: number;
  }> {
    const response = await apiClient.get(`/api/users/${userId}/activity-logs`, {
      params: { page, size }
    });
    return response.data;
  },

  // Reset user password
  async resetUserPassword(userId: number): Promise<void> {
    await apiClient.post(`/api/users/${userId}/reset-password`);
  },
};

export default userApi;
