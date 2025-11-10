import apiClient from './client';

export interface DashboardStats {
  instanceStatistics: {
    totalInstances: number;
    runningInstances: number;
    pausedInstances: number;
    completedInstances: number;
    cancelledInstances: number;
    errorInstances: number;
    overdueInstances: number;
  };
  taskStatistics: {
    totalTasks: number;
    pendingTasks: number;
    assignedTasks: number;
    inProgressTasks: number;
    completedTasks: number;
    overdueTasks: number;
    escalatedTasks: number;
  };
  processCounts: Record<string, number>;
}

export const dashboardApi = {
  // Get dashboard statistics
  async getDashboardStats(): Promise<DashboardStats> {
    const response = await apiClient.get('/api/dashboard');
    return response.data;
  },
};

export default dashboardApi;
