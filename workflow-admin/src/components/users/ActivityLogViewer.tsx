'use client';

import React, { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { userApi, ActivityLog } from '@/lib/api/users';
import {
  Activity,
  Filter,
  Download,
  ChevronLeft,
  ChevronRight,
  Calendar,
  AlertCircle,
  Loader2,
  Search,
  X,
} from 'lucide-react';
import Button from '@/components/ui/Button';
import toast from 'react-hot-toast';

interface ActivityLogViewerProps {
  userId: number;
}

// Activity type options for filtering
const ACTIVITY_TYPES = [
  'LOGIN',
  'LOGOUT',
  'PASSWORD_CHANGE',
  'PASSWORD_RESET',
  'PROFILE_UPDATE',
  'ROLE_ASSIGNED',
  'ROLE_REMOVED',
  'BUSINESS_UNIT_ASSIGNED',
  'BUSINESS_UNIT_REMOVED',
  'MANAGER_ASSIGNED',
  'WORKFLOW_CREATED',
  'WORKFLOW_APPROVED',
  'WORKFLOW_REJECTED',
  'TASK_COMPLETED',
  'OTHER',
];

export default function ActivityLogViewer({ userId }: ActivityLogViewerProps) {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [selectedActivityType, setSelectedActivityType] = useState<string>('');
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [showFilters, setShowFilters] = useState(false);

  // Fetch activity logs
  const { data: activityLogsData, isLoading } = useQuery({
    queryKey: ['user-activity-logs', userId, page, pageSize],
    queryFn: () => userApi.getUserActivityLogs(userId, page, pageSize),
  });

  // Client-side filtering (since API might not support all filters)
  const filteredLogs = useMemo(() => {
    if (!activityLogsData?.content) return [];

    let filtered = activityLogsData.content;

    // Filter by activity type
    if (selectedActivityType) {
      filtered = filtered.filter(
        (log) => log.activityType.toUpperCase() === selectedActivityType
      );
    }

    // Filter by date range
    if (startDate) {
      filtered = filtered.filter(
        (log) => new Date(log.timestamp) >= new Date(startDate)
      );
    }
    if (endDate) {
      const endDateTime = new Date(endDate);
      endDateTime.setHours(23, 59, 59, 999);
      filtered = filtered.filter(
        (log) => new Date(log.timestamp) <= endDateTime
      );
    }

    // Filter by search term
    if (searchTerm) {
      const lowerSearch = searchTerm.toLowerCase();
      filtered = filtered.filter(
        (log) =>
          log.description.toLowerCase().includes(lowerSearch) ||
          log.activityType.toLowerCase().includes(lowerSearch) ||
          log.ipAddress?.toLowerCase().includes(lowerSearch)
      );
    }

    return filtered;
  }, [activityLogsData?.content, selectedActivityType, startDate, endDate, searchTerm]);

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  };

  const getActivityTypeColor = (type: string) => {
    const typeUpper = type.toUpperCase();
    if (typeUpper.includes('LOGIN')) return 'bg-green-100 text-green-800 border-green-200';
    if (typeUpper.includes('LOGOUT')) return 'bg-gray-100 text-gray-800 border-gray-200';
    if (typeUpper.includes('PASSWORD')) return 'bg-yellow-100 text-yellow-800 border-yellow-200';
    if (typeUpper.includes('ROLE')) return 'bg-purple-100 text-purple-800 border-purple-200';
    if (typeUpper.includes('APPROVED')) return 'bg-green-100 text-green-800 border-green-200';
    if (typeUpper.includes('REJECTED')) return 'bg-red-100 text-red-800 border-red-200';
    if (typeUpper.includes('CREATE') || typeUpper.includes('ASSIGN'))
      return 'bg-blue-100 text-blue-800 border-blue-200';
    if (typeUpper.includes('REMOVE') || typeUpper.includes('DELETE'))
      return 'bg-red-100 text-red-800 border-red-200';
    return 'bg-gray-100 text-gray-800 border-gray-200';
  };

  const handleExport = () => {
    if (!filteredLogs.length) {
      toast.error('No activity logs to export');
      return;
    }

    try {
      // Create CSV content
      const headers = ['Timestamp', 'Activity Type', 'Description', 'IP Address', 'User Agent'];
      const csvContent = [
        headers.join(','),
        ...filteredLogs.map((log) =>
          [
            new Date(log.timestamp).toISOString(),
            log.activityType,
            `"${log.description.replace(/"/g, '""')}"`,
            log.ipAddress || 'N/A',
            log.userAgent ? `"${log.userAgent.replace(/"/g, '""')}"` : 'N/A',
          ].join(',')
        ),
      ].join('\n');

      // Create and download file
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `user-${userId}-activity-logs-${Date.now()}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      toast.success('Activity logs exported successfully');
    } catch (error) {
      toast.error('Failed to export activity logs');
    }
  };

  const clearFilters = () => {
    setSelectedActivityType('');
    setStartDate('');
    setEndDate('');
    setSearchTerm('');
  };

  const hasActiveFilters =
    selectedActivityType || startDate || endDate || searchTerm;

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-2 mb-4">
          <Activity className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Activity Log</h3>
        </div>
        <div className="flex items-center justify-center py-8">
          <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
          <p className="ml-3 text-gray-600">Loading activity logs...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Activity className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Activity Log</h3>
          {filteredLogs.length !== activityLogsData?.content.length && (
            <span className="text-sm text-gray-500">
              ({filteredLogs.length} of {activityLogsData?.content.length})
            </span>
          )}
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setShowFilters(!showFilters)}
          >
            <Filter className="w-4 h-4 mr-1" />
            Filters
            {hasActiveFilters && (
              <span className="ml-1 w-2 h-2 bg-blue-600 rounded-full"></span>
            )}
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={handleExport}
            disabled={filteredLogs.length === 0}
          >
            <Download className="w-4 h-4 mr-1" />
            Export
          </Button>
        </div>
      </div>

      {/* Filter Panel */}
      {showFilters && (
        <div className="mb-4 p-4 bg-gray-50 rounded-md border border-gray-200 space-y-3">
          {/* Search */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Search
            </label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search description, type, or IP address..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            {/* Activity Type Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Activity Type
              </label>
              <select
                value={selectedActivityType}
                onChange={(e) => setSelectedActivityType(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Types</option>
                {ACTIVITY_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {type.replace(/_/g, ' ')}
                  </option>
                ))}
              </select>
            </div>

            {/* Start Date Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Start Date
              </label>
              <input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* End Date Filter */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                End Date
              </label>
              <input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                min={startDate}
              />
            </div>
          </div>

          {hasActiveFilters && (
            <div className="flex justify-end">
              <Button variant="outline" size="sm" onClick={clearFilters}>
                <X className="w-4 h-4 mr-1" />
                Clear Filters
              </Button>
            </div>
          )}
        </div>
      )}

      {/* Activity List */}
      {filteredLogs.length > 0 ? (
        <div className="space-y-3">
          {filteredLogs.map((log) => (
            <div
              key={log.id}
              className="flex items-start gap-3 p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
            >
              <Activity className="w-4 h-4 text-gray-400 mt-1 flex-shrink-0" />
              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span
                        className={`px-2 py-0.5 rounded text-xs font-medium border ${getActivityTypeColor(
                          log.activityType
                        )}`}
                      >
                        {log.activityType.replace(/_/g, ' ')}
                      </span>
                      <span className="text-xs text-gray-500 flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {formatDateTime(log.timestamp)}
                      </span>
                    </div>
                    <p className="text-sm text-gray-900">{log.description}</p>
                    <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
                      {log.ipAddress && (
                        <span className="flex items-center gap-1">
                          <span className="font-medium">IP:</span>
                          <code className="px-1.5 py-0.5 bg-gray-100 rounded font-mono">
                            {log.ipAddress}
                          </code>
                        </span>
                      )}
                      {log.userAgent && (
                        <span className="truncate" title={log.userAgent}>
                          <span className="font-medium">User Agent:</span>{' '}
                          {log.userAgent.substring(0, 50)}
                          {log.userAgent.length > 50 ? '...' : ''}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="text-center py-8 text-gray-500">
          <Activity className="w-12 h-12 mx-auto mb-2 text-gray-400" />
          {hasActiveFilters ? (
            <>
              <p>No activity logs match your filters</p>
              <Button
                variant="outline"
                size="sm"
                onClick={clearFilters}
                className="mt-3"
              >
                Clear Filters
              </Button>
            </>
          ) : (
            <p>No activity logs found</p>
          )}
        </div>
      )}

      {/* Pagination */}
      {activityLogsData && activityLogsData.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-between">
          <div className="text-sm text-gray-600">
            Page {page + 1} of {activityLogsData.totalPages}
            <span className="ml-2">
              ({activityLogsData.totalElements} total records)
            </span>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(page - 1)}
              disabled={page === 0}
            >
              <ChevronLeft className="w-4 h-4" />
              Previous
            </Button>
            <select
              value={pageSize}
              onChange={(e) => {
                setPageSize(Number(e.target.value));
                setPage(0);
              }}
              className="px-3 py-1.5 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="10">10 per page</option>
              <option value="25">25 per page</option>
              <option value="50">50 per page</option>
              <option value="100">100 per page</option>
            </select>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setPage(page + 1)}
              disabled={page >= activityLogsData.totalPages - 1}
            >
              Next
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
