import apiClient from './client';

export interface KeycloakSyncStatus {
  lastSyncAt?: string;
  totalUsers: number;
  syncedUsers: number;
  failedUsers: number;
  isRunning: boolean;
  errors: SyncError[];
}

export interface SyncError {
  userId?: number;
  keycloakId?: string;
  message: string;
  timestamp: string;
}

export interface UserSyncInfo {
  id: number;
  keycloakId?: string;
  keycloakUsername?: string;
  email: string;
  firstName: string;
  lastName: string;
  lastSyncedAt?: string;
  syncStatus: 'synced' | 'out_of_sync' | 'error' | 'never_synced';
  syncError?: string;
  isActive: boolean;
}

export interface SyncAllResponse {
  message: string;
  syncedCount: number;
  failedCount: number;
  errors: SyncError[];
}

export interface SyncUserResponse {
  message: string;
  user: UserSyncInfo;
}

export const keycloakSyncApi = {
  /**
   * Trigger a full sync of all users from Keycloak
   */
  async syncAllUsersFromKeycloak(): Promise<SyncAllResponse> {
    const response = await apiClient.post('/api/keycloak-sync/sync-all');
    return response.data;
  },

  /**
   * Sync a single user from Keycloak by their Keycloak ID
   */
  async syncSingleUser(keycloakId: string): Promise<SyncUserResponse> {
    const response = await apiClient.post(`/api/keycloak-sync/sync-user/${keycloakId}`);
    return response.data;
  },

  /**
   * Get the current sync status including last sync time, counts, and errors
   */
  async getSyncStatus(): Promise<KeycloakSyncStatus> {
    const response = await apiClient.get('/api/keycloak-sync/status');
    return response.data;
  },

  /**
   * Get all users with their Keycloak sync information
   */
  async getUsersWithSyncInfo(page: number = 0, size: number = 20): Promise<{
    content: UserSyncInfo[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  }> {
    const response = await apiClient.get('/api/keycloak-sync/users', {
      params: { page, size }
    });
    return response.data;
  },
};

export default keycloakSyncApi;
