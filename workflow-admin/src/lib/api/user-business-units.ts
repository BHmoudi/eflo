import apiClient from './client';

export interface UserBusinessUnit {
  id: number;
  userId: number;
  businessUnitId: number;
  businessUnitName?: string;
  businessUnitCode?: string;
  isPrimary: boolean;
  assignedAt: string;
  assignedBy?: number;
}

export interface AssignUserToBusinessUnitDto {
  userId: number;
  businessUnitId: number;
  isPrimary?: boolean;
}

export interface BusinessUnitUser {
  id: number;
  userId: number;
  userEmail: string;
  userFirstName: string;
  userLastName: string;
  isPrimary: boolean;
  assignedAt: string;
}

export const userBusinessUnitApi = {
  /**
   * Assign a user to a business unit
   */
  async assignUserToBusinessUnit(data: AssignUserToBusinessUnitDto): Promise<UserBusinessUnit> {
    const response = await apiClient.post('/api/user-business-units/assign', data);
    return response.data;
  },

  /**
   * Remove a user from a business unit
   */
  async removeUserFromBusinessUnit(userId: number, businessUnitId: number): Promise<void> {
    await apiClient.delete(`/api/user-business-units/remove`, {
      params: { userId, businessUnitId }
    });
  },

  /**
   * Set a business unit as the primary for a user
   */
  async setPrimaryBusinessUnit(userId: number, businessUnitId: number): Promise<UserBusinessUnit> {
    const response = await apiClient.put('/api/user-business-units/set-primary', null, {
      params: { userId, businessUnitId }
    });
    return response.data;
  },

  /**
   * Get all business units for a specific user
   */
  async getUserBusinessUnits(userId: number): Promise<UserBusinessUnit[]> {
    const response = await apiClient.get(`/api/user-business-units/user/${userId}`);
    return response.data;
  },

  /**
   * Get the primary business unit for a user
   */
  async getPrimaryBusinessUnit(userId: number): Promise<UserBusinessUnit> {
    const response = await apiClient.get(`/api/user-business-units/user/${userId}/primary`);
    return response.data;
  },

  /**
   * Get all users in a specific business unit
   */
  async getBusinessUnitUsers(businessUnitId: number): Promise<BusinessUnitUser[]> {
    const response = await apiClient.get(`/api/user-business-units/business-unit/${businessUnitId}/users`);
    return response.data;
  },

  /**
   * Check if a user is assigned to a specific business unit
   */
  async isUserAssigned(userId: number, businessUnitId: number): Promise<boolean> {
    const response = await apiClient.get('/api/user-business-units/is-assigned', {
      params: { userId, businessUnitId }
    });
    return response.data;
  },
};

export default userBusinessUnitApi;
