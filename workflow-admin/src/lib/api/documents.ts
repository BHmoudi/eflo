import apiClient from './client';

// ============================================================================
// TYPE DEFINITIONS
// ============================================================================

export interface DocumentUploadRequest {
  file: File;
  documentTypeId: number;
  orderId: number;
  orderNumber: string;
  description?: string;
  tags?: string[];
  businessUnitId?: number;
}

export interface Document {
  id: number;
  documentUuid: string;
  originalFilename: string;
  storedFilename: string;
  fileExtension: string;
  mimeType: string;
  fileSizeBytes: number;
  fileSizeMB: number;
  typeCode: string;
  typeName: string;
  orderId: number;
  orderNumber: string;
  description?: string;
  tags: string[];
  uploadedBy: string;
  uploadedAt: string;
  validatedBy?: string;
  validatedAt?: string;
  validationStatus: string;
  status: string;
  expirationDate?: string;
  isArchived: boolean;
  isDeleted: boolean;
  isConfidential?: boolean;
  version: number;
  virusScanStatus?: string;
  virusScanDate?: string;
  businessUnitId?: number;
}

export interface DocumentSummary {
  id: number;
  documentUuid: string;
  originalFilename: string;
  fileExtension: string;
  fileSizeMB: number;
  typeCode: string;
  typeName: string;
  orderNumber: string;
  status: string;
  uploadedAt: string;
  validationStatus: string;
}

export interface UploadResult {
  success: boolean;
  documentId: number;
  documentUuid: string;
  typeCode: string;
  fileSizeMB: number;
  uploadedAt: string;
  message: string;
}

export interface BulkUploadResult {
  totalFiles: number;
  successCount: number;
  failureCount: number;
  results: Array<{
    filename: string;
    success: boolean;
    documentId?: number;
    error?: string;
  }>;
}

export interface DocumentStatistics {
  totalDocuments: number;
  activeDocuments: number;
  pendingDocuments: number;
  validatedDocuments: number;
  rejectedDocuments: number;
  expiredDocuments: number;
  archivedDocuments: number;
  deletedDocuments: number;
  expiringDocuments: number;
  virusScanPending: number;
  infectedDocuments: number;
  confidentialDocuments: number;
  totalStorageSizeBytes: number;
  totalStorageSizeGB: number;
  averageFileSizeMB: number;
  largestFileSizeMB: number;
  smallestFileSizeMB: number;
  documentsByStatus?: Record<string, number>;
  documentsByType?: Record<string, number>;
  documentsByExtension?: Record<string, number>;
}

export interface DocumentValidationRequest {
  isApproved: boolean;
  comments?: string;
  validatedBy: string;
}

export interface DocumentValidationResponse {
  documentId: number;
  validationStatus: string;
  validatedBy: string;
  validatedAt: string;
  comments?: string;
}

export interface ValidationResult {
  valid: boolean;
  message?: string;
}

export interface ScanResult {
  scanStatus: string;
  scanDate?: string;
  details?: Record<string, any>;
  message?: string;
}

export interface DocumentType {
  id: number;
  typeCode: string;
  typeName: string;
  description?: string;
  category: string;
  isMandatory: boolean;
  isActive: boolean;
  maxFileSizeMB?: number;
  allowedExtensions: string[];
  expirationWarningDays?: number;
  retentionPeriodDays?: number;
  requiresValidation: boolean;
  validationRoles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface DocumentTypeCreateRequest {
  typeCode: string;
  typeName: string;
  description?: string;
  category: string;
  isMandatory?: boolean;
  isActive?: boolean;
  maxFileSizeMB?: number;
  allowedExtensions?: string[];
  expirationWarningDays?: number;
  retentionPeriodDays?: number;
  requiresValidation?: boolean;
  validationRoles?: string[];
}

export interface DocumentTypeUpdateRequest {
  typeName?: string;
  description?: string;
  category?: string;
  isMandatory?: boolean;
  isActive?: boolean;
  maxFileSizeMB?: number;
  allowedExtensions?: string[];
  expirationWarningDays?: number;
  retentionPeriodDays?: number;
  requiresValidation?: boolean;
  validationRoles?: string[];
}

export interface DocumentSearchRequest {
  searchTerm?: string;
  typeCode?: string;
  orderId?: number;
  orderNumber?: string;
  status?: string;
  validationStatus?: string;
  uploadedBy?: string;
  uploadedFrom?: string;
  uploadedTo?: string;
  validatedFrom?: string;
  validatedTo?: string;
  expiringBefore?: string;
  tags?: string[];
  businessUnitId?: number;
  isConfidential?: boolean;
  latestVersionOnly?: boolean;
  page?: number;
  size?: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ============================================================================
// DOCUMENT MANAGEMENT API
// ============================================================================

export const documentsApi = {
  // Upload single document
  async uploadDocument(data: DocumentUploadRequest): Promise<UploadResult> {
    const formData = new FormData();
    formData.append('file', data.file);
    formData.append('documentTypeId', data.documentTypeId.toString());
    formData.append('orderId', data.orderId.toString());
    formData.append('orderNumber', data.orderNumber);
    if (data.description) formData.append('description', data.description);
    if (data.tags) data.tags.forEach(tag => formData.append('tags', tag));
    if (data.businessUnitId) formData.append('businessUnitId', data.businessUnitId.toString());

    const response = await apiClient.post('/api/documents/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  // Upload multiple documents
  async uploadMultiple(files: File[], documentTypeId: number, orderId: number, orderNumber: string): Promise<BulkUploadResult> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    formData.append('documentTypeId', documentTypeId.toString());
    formData.append('orderId', orderId.toString());
    formData.append('orderNumber', orderNumber);

    const response = await apiClient.post('/api/documents/upload-multiple', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  // Get document by ID
  async getDocument(id: number): Promise<Document> {
    const response = await apiClient.get(`/api/documents/${id}`);
    return response.data;
  },

  // Get documents by order
  async getOrderDocuments(orderId: number, page: number = 0, size: number = 20): Promise<PageResponse<DocumentSummary>> {
    const response = await apiClient.get(`/api/documents/order/${orderId}?page=${page}&size=${size}`);
    return response.data;
  },

  // Get documents by order and type
  async getByOrderAndType(orderId: number, typeCode: string): Promise<Document[]> {
    const response = await apiClient.get(`/api/documents/order/${orderId}/type/${typeCode}`);
    return response.data;
  },

  // Download document
  async downloadDocument(id: number): Promise<Blob> {
    const response = await apiClient.get(`/api/documents/${id}/download`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Get document preview
  async getPreview(id: number): Promise<Blob> {
    const response = await apiClient.get(`/api/documents/${id}/preview`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Delete document
  async deleteDocument(id: number): Promise<void> {
    await apiClient.delete(`/api/documents/${id}`);
  },

  // Replace document
  async replaceDocument(id: number, file: File, reason?: string): Promise<Document> {
    const formData = new FormData();
    formData.append('file', file);
    if (reason) formData.append('reason', reason);

    const response = await apiClient.put(`/api/documents/${id}/replace`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  // Archive document
  async archiveDocument(id: number): Promise<Document> {
    const response = await apiClient.post(`/api/documents/${id}/archive`);
    return response.data;
  },

  // Get document versions
  async getVersions(id: number): Promise<Document[]> {
    const response = await apiClient.get(`/api/documents/${id}/versions`);
    return response.data;
  },

  // Get pending validation documents
  async getPendingValidation(page: number = 0, size: number = 20): Promise<PageResponse<DocumentSummary>> {
    const response = await apiClient.get(`/api/documents/pending-validation?page=${page}&size=${size}`);
    return response.data;
  },

  // Get expiring documents
  async getExpiringDocuments(days: number = 30, page: number = 0, size: number = 20): Promise<PageResponse<DocumentSummary>> {
    const response = await apiClient.get(`/api/documents/expiring?days=${days}&page=${page}&size=${size}`);
    return response.data;
  },

  // Get statistics
  async getStatistics(): Promise<DocumentStatistics> {
    const response = await apiClient.get('/api/documents/statistics');
    return response.data;
  },
};

// ============================================================================
// DOCUMENT VALIDATION API
// ============================================================================

export const documentValidationApi = {
  // Validate document
  async validateDocument(id: number, request: DocumentValidationRequest): Promise<DocumentValidationResponse> {
    const response = await apiClient.post(`/api/documents/${id}/validate`, request);
    return response.data;
  },

  // Reject document
  async rejectDocument(id: number, request: DocumentValidationRequest): Promise<DocumentValidationResponse> {
    const response = await apiClient.post(`/api/documents/${id}/reject`, request);
    return response.data;
  },

  // Batch validate documents
  async validateBatch(documentIds: number[], comments?: string): Promise<Record<number, ValidationResult>> {
    const response = await apiClient.post('/api/documents/validate-batch', null, {
      params: { documentIds: documentIds.join(','), comments },
    });
    return response.data;
  },

  // Get order validation status
  async getOrderValidationStatus(orderId: number): Promise<Record<string, any>> {
    const response = await apiClient.get(`/api/documents/validation-status/${orderId}`);
    return response.data;
  },

  // Rescan for virus
  async rescanForVirus(id: number): Promise<ScanResult> {
    const response = await apiClient.post(`/api/documents/${id}/rescan`);
    return response.data;
  },

  // Get scan status
  async getScanStatus(documentIds?: number[]): Promise<Record<number, ScanResult>> {
    const params = documentIds ? { documentIds: documentIds.join(',') } : {};
    const response = await apiClient.get('/api/documents/scan-status', { params });
    return response.data;
  },
};

// ============================================================================
// DOCUMENT TYPE API
// ============================================================================

export const documentTypeApi = {
  // List all document types
  async listAllTypes(includeInactive: boolean = false): Promise<DocumentType[]> {
    try {
      const response = await apiClient.get(`/api/document-types?includeInactive=${includeInactive}`);
      console.log('Document types response:', response.data);
      return Array.isArray(response.data) ? response.data : [];
    } catch (error) {
      console.error('Failed to fetch document types:', error);
      return [];
    }
  },

  // Create document type
  async createType(request: DocumentTypeCreateRequest): Promise<DocumentType> {
    const response = await apiClient.post('/api/document-types', request);
    return response.data;
  },

  // Get document type by ID
  async getType(id: number): Promise<DocumentType> {
    const response = await apiClient.get(`/api/document-types/${id}`);
    return response.data;
  },

  // Update document type
  async updateType(id: number, request: DocumentTypeUpdateRequest): Promise<DocumentType> {
    const response = await apiClient.put(`/api/document-types/${id}`, request);
    return response.data;
  },

  // Delete document type
  async deleteType(id: number): Promise<void> {
    await apiClient.delete(`/api/document-types/${id}`);
  },

  // Get mandatory document types
  async getMandatoryTypes(): Promise<DocumentType[]> {
    const response = await apiClient.get('/api/document-types/mandatory');
    return response.data;
  },

  // Get document types by category
  async getByCategory(category: string): Promise<DocumentType[]> {
    const response = await apiClient.get(`/api/document-types/category/${category}`);
    return response.data;
  },

  // Duplicate document type
  async duplicateType(id: number, newTypeCode: string, newTypeName: string): Promise<DocumentType> {
    const response = await apiClient.post(`/api/document-types/${id}/duplicate`, null, {
      params: { newTypeCode, newTypeName },
    });
    return response.data;
  },

  // Get templates
  async getTemplates(): Promise<DocumentType[]> {
    const response = await apiClient.get('/api/document-types/templates');
    return response.data;
  },
};

// ============================================================================
// DOCUMENT REPORT API
// ============================================================================

export const documentReportApi = {
  // Get detailed statistics
  async getStats(fromDate?: string, toDate?: string, businessUnitId?: number): Promise<DocumentStatistics> {
    const params: any = {};
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;
    if (businessUnitId) params.businessUnitId = businessUnitId;
    const response = await apiClient.get('/api/documents/stats', { params });
    return response.data;
  },

  // Get compliance report
  async getComplianceReport(orderId?: number, businessUnitId?: number): Promise<Record<string, any>> {
    const params: any = {};
    if (orderId) params.orderId = orderId;
    if (businessUnitId) params.businessUnitId = businessUnitId;
    const response = await apiClient.get('/api/documents/report/compliance', { params });
    return response.data;
  },

  // Get validation report
  async getValidationReport(fromDate?: string, toDate?: string, validatorId?: string): Promise<Record<string, any>> {
    const params: any = {};
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;
    if (validatorId) params.validatorId = validatorId;
    const response = await apiClient.get('/api/documents/report/validation', { params });
    return response.data;
  },

  // Get access report
  async getAccessReport(fromDate?: string, toDate?: string, documentId?: number): Promise<Record<string, any>> {
    const params: any = {};
    if (fromDate) params.fromDate = fromDate;
    if (toDate) params.toDate = toDate;
    if (documentId) params.documentId = documentId;
    const response = await apiClient.get('/api/documents/report/access', { params });
    return response.data;
  },

  // Get dashboard data
  async getDashboardData(): Promise<Record<string, any>> {
    const response = await apiClient.get('/api/documents/dashboard');
    return response.data;
  },

  // Export audit log
  async exportAuditLog(fromDate: string, toDate: string, format: 'csv' | 'excel' = 'csv'): Promise<Blob> {
    const response = await apiClient.get('/api/documents/export/audit', {
      params: { fromDate, toDate, format },
      responseType: 'blob',
    });
    return response.data;
  },
};

// ============================================================================
// DOCUMENT SEARCH API
// ============================================================================

export const documentSearchApi = {
  // Advanced search
  async advancedSearch(request: DocumentSearchRequest): Promise<PageResponse<DocumentSummary>> {
    const response = await apiClient.post('/api/documents/search', request);
    return response.data;
  },

  // Get recent documents
  async getRecentDocuments(days: number = 7, page: number = 0, size: number = 20): Promise<PageResponse<DocumentSummary>> {
    const response = await apiClient.get(`/api/documents/search/recent?days=${days}&page=${page}&size=${size}`);
    return response.data;
  },

  // Get my uploads
  async getMyUploads(page: number = 0, size: number = 20): Promise<PageResponse<DocumentSummary>> {
    const response = await apiClient.get(`/api/documents/search/my-uploads?page=${page}&size=${size}`);
    return response.data;
  },

  // Search by metadata
  async searchByMetadata(metadata: Record<string, any>, page: number = 0, size: number = 20): Promise<PageResponse<Document>> {
    const response = await apiClient.post(`/api/documents/search/metadata?page=${page}&size=${size}`, metadata);
    return response.data;
  },
};

// Default export
export default {
  ...documentsApi,
  validation: documentValidationApi,
  types: documentTypeApi,
  reports: documentReportApi,
  search: documentSearchApi,
};
