import apiClient from './client';

export interface BusinessUnit {
  id: number;
  code: string;
  name: string;
  legalName?: string;
  type: 'HEADQUARTERS' | 'REGIONAL_OFFICE' | 'BRANCH' | 'WAREHOUSE' | 'STORE' | 'FACTORY' | 'SERVICE_CENTER';
  regionCode?: string;
  regionName?: string;
  rrfCode?: string;
  managerId?: number;
  managerName?: string;
  phone?: string;
  fax?: string;
  email?: string;
  website?: string;
  addressLine1?: string;
  addressLine2?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  siret?: string;
  vatNumber?: string;
  brands?: string[];
  openingHours?: string;
  active: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateBusinessUnitDto {
  code: string;
  name: string;
  legalName?: string;
  type: 'HEADQUARTERS' | 'REGIONAL_OFFICE' | 'BRANCH' | 'WAREHOUSE' | 'STORE' | 'FACTORY' | 'SERVICE_CENTER';
  regionCode?: string;
  regionName?: string;
  rrfCode?: string;
  managerId?: number;
  phone?: string;
  fax?: string;
  email?: string;
  website?: string;
  addressLine1?: string;
  addressLine2?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  siret?: string;
  vatNumber?: string;
  brands?: string[];
  openingHours?: string;
}

export interface UpdateBusinessUnitDto {
  code?: string;
  name?: string;
  legalName?: string;
  type?: 'HEADQUARTERS' | 'REGIONAL_OFFICE' | 'BRANCH' | 'WAREHOUSE' | 'STORE' | 'FACTORY' | 'SERVICE_CENTER';
  regionCode?: string;
  regionName?: string;
  rrfCode?: string;
  phone?: string;
  fax?: string;
  email?: string;
  website?: string;
  addressLine1?: string;
  addressLine2?: string;
  postalCode?: string;
  city?: string;
  country?: string;
  latitude?: number;
  longitude?: number;
  siret?: string;
  vatNumber?: string;
  brands?: string[];
  openingHours?: string;
}

export interface PaginatedBusinessUnits {
  content: BusinessUnit[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const businessUnitApi = {
  // Get all business units (paginated)
  async getBusinessUnits(page: number = 0, size: number = 20): Promise<PaginatedBusinessUnits> {
    const response = await apiClient.get('/api/business-units', {
      params: { page, size }
    });
    return response.data;
  },

  // Get business unit by ID
  async getBusinessUnitById(id: number): Promise<BusinessUnit> {
    const response = await apiClient.get(`/api/business-units/${id}`);
    return response.data;
  },

  // Create new business unit
  async createBusinessUnit(data: CreateBusinessUnitDto): Promise<BusinessUnit> {
    const response = await apiClient.post('/api/business-units', data);
    return response.data;
  },

  // Update business unit
  async updateBusinessUnit(id: number, data: UpdateBusinessUnitDto): Promise<BusinessUnit> {
    const response = await apiClient.put(`/api/business-units/${id}`, data);
    return response.data;
  },

  // Delete business unit
  async deleteBusinessUnit(id: number): Promise<void> {
    await apiClient.delete(`/api/business-units/${id}`);
  },

  // Assign manager to business unit
  async assignManager(businessUnitId: number, managerId: number): Promise<BusinessUnit> {
    const response = await apiClient.post(`/api/business-units/${businessUnitId}/assign-manager/${managerId}`);
    return response.data;
  },

  // Remove manager from business unit
  async removeManager(businessUnitId: number): Promise<BusinessUnit> {
    const response = await apiClient.post(`/api/business-units/${businessUnitId}/remove-manager`);
    return response.data;
  },

  // Deactivate business unit
  async deactivateBusinessUnit(id: number): Promise<void> {
    await apiClient.post(`/api/business-units/${id}/deactivate`);
  },

  // Reactivate business unit
  async reactivateBusinessUnit(id: number): Promise<void> {
    await apiClient.post(`/api/business-units/${id}/reactivate`);
  },

  // Get active business units only
  async getActiveBusinessUnits(): Promise<BusinessUnit[]> {
    const response = await apiClient.get('/api/business-units/active');
    return response.data;
  },

  // Search business units
  async searchBusinessUnits(query: string): Promise<BusinessUnit[]> {
    const response = await apiClient.get('/api/business-units/search', {
      params: { q: query }
    });
    return response.data;
  },

  // Get business units by type
  async getBusinessUnitsByType(type: string): Promise<BusinessUnit[]> {
    const response = await apiClient.get(`/api/business-units/type/${type}`);
    return response.data;
  },

  // Get business units by region
  async getBusinessUnitsByRegion(regionCode: string): Promise<BusinessUnit[]> {
    const response = await apiClient.get(`/api/business-units/region/${regionCode}`);
    return response.data;
  },

  // Get business unit by code
  async getBusinessUnitByCode(code: string): Promise<BusinessUnit> {
    const response = await apiClient.get(`/api/business-units/code/${code}`);
    return response.data;
  },
};

export default businessUnitApi;
