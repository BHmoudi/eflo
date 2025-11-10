'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { hierarchyApi } from '@/lib/api/hierarchies';
import { userApi, User } from '@/lib/api/users';
import { UserCog, Users, TrendingUp, Edit2, X, AlertCircle, ChevronDown, ChevronUp } from 'lucide-react';
import Button from '@/components/ui/Button';
import UserSelector from '@/components/ui/UserSelector';

interface ManagerAssignmentProps {
  userId: number;
  userEmail?: string;
  userName?: string;
}

export default function ManagerAssignment({ userId, userEmail, userName }: ManagerAssignmentProps) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [isEditingManager, setIsEditingManager] = useState(false);
  const [selectedManagerId, setSelectedManagerId] = useState<number | undefined>(undefined);
  const [showManagementChain, setShowManagementChain] = useState(false);
  const [showDirectReports, setShowDirectReports] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Fetch current hierarchy
  const { data: currentHierarchy, isLoading: isLoadingHierarchy } = useQuery({
    queryKey: ['hierarchy', 'employee', userId],
    queryFn: () => hierarchyApi.getEmployeeHierarchy(userId),
    retry: false,
  });

  // Fetch management chain
  const { data: managementChain, isLoading: isLoadingChain } = useQuery({
    queryKey: ['hierarchy', 'management-chain', userId],
    queryFn: () => hierarchyApi.getManagementChain(userId),
    enabled: showManagementChain,
    retry: false,
  });

  // Fetch direct reports
  const { data: directReports, isLoading: isLoadingReports } = useQuery({
    queryKey: ['hierarchy', 'direct-reports', userId],
    queryFn: () => hierarchyApi.getDirectReports(userId),
    enabled: showDirectReports,
    retry: false,
  });

  // Fetch manager details if we have a manager ID
  const { data: managerDetails } = useQuery({
    queryKey: ['users', currentHierarchy?.managerId],
    queryFn: () => userApi.getUserById(currentHierarchy!.managerId),
    enabled: !!currentHierarchy?.managerId,
  });

  // Create hierarchy mutation
  const createHierarchyMutation = useMutation({
    mutationFn: (managerId: number) =>
      hierarchyApi.createHierarchy({
        employeeId: userId,
        managerId,
        effectiveFrom: new Date().toISOString().split('T')[0],
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hierarchy', 'employee', userId] });
      queryClient.invalidateQueries({ queryKey: ['hierarchy', 'management-chain', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'hierarchy'] });
      setIsEditingManager(false);
      setSelectedManagerId(undefined);
      setError(null);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to assign manager');
    },
  });

  // Update hierarchy mutation
  const updateHierarchyMutation = useMutation({
    mutationFn: (managerId: number) =>
      hierarchyApi.updateHierarchy(currentHierarchy!.id, {
        managerId,
        effectiveFrom: new Date().toISOString().split('T')[0],
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hierarchy', 'employee', userId] });
      queryClient.invalidateQueries({ queryKey: ['hierarchy', 'management-chain', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'hierarchy'] });
      setIsEditingManager(false);
      setSelectedManagerId(undefined);
      setError(null);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to update manager');
    },
  });

  // Deactivate hierarchy mutation
  const deactivateHierarchyMutation = useMutation({
    mutationFn: () => hierarchyApi.deactivateHierarchy(currentHierarchy!.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['hierarchy', 'employee', userId] });
      queryClient.invalidateQueries({ queryKey: ['hierarchy', 'management-chain', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'hierarchy'] });
      setError(null);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to remove manager');
    },
  });

  const handleSaveManager = () => {
    if (selectedManagerId) {
      if (currentHierarchy) {
        updateHierarchyMutation.mutate(selectedManagerId);
      } else {
        createHierarchyMutation.mutate(selectedManagerId);
      }
    }
  };

  const handleRemoveManager = () => {
    if (currentHierarchy && confirm('Are you sure you want to remove the manager assignment?')) {
      deactivateHierarchyMutation.mutate();
    }
  };

  if (isLoadingHierarchy) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-2 mb-4">
          <UserCog className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Manager & Hierarchy</h3>
        </div>
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <UserCog className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Manager & Hierarchy</h3>
        </div>
        {!isEditingManager && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsEditingManager(true)}
          >
            <Edit2 className="w-4 h-4 mr-1" />
            {currentHierarchy ? 'Change Manager' : 'Assign Manager'}
          </Button>
        )}
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md flex items-start gap-2">
          <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-red-800">{error}</p>
        </div>
      )}

      {/* Edit Manager Form */}
      {isEditingManager && (
        <div className="mb-4 p-4 bg-gray-50 rounded-md border border-gray-200">
          <UserSelector
            value={selectedManagerId}
            onChange={(value) => setSelectedManagerId(value as number | undefined)}
            label="Select Manager"
            placeholder="Choose a manager..."
            activeOnly={true}
          />
          <div className="flex gap-2 mt-3">
            <Button
              variant="primary"
              size="sm"
              onClick={handleSaveManager}
              disabled={
                !selectedManagerId ||
                createHierarchyMutation.isPending ||
                updateHierarchyMutation.isPending
              }
            >
              {createHierarchyMutation.isPending || updateHierarchyMutation.isPending
                ? 'Saving...'
                : 'Save'}
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setIsEditingManager(false);
                setSelectedManagerId(undefined);
                setError(null);
              }}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}

      {/* Current Manager */}
      {currentHierarchy && managerDetails ? (
        <div className="space-y-4">
          <div className="p-4 border border-gray-200 rounded-md bg-blue-50">
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <UserCog className="w-5 h-5 text-blue-600" />
                  <h4 className="font-medium text-gray-900">Current Manager</h4>
                </div>
                <div className="ml-7">
                  <p
                    className="font-semibold text-gray-900 cursor-pointer hover:text-blue-600 hover:underline transition-colors"
                    onClick={() => router.push(`/dashboard/users/${managerDetails.id}`)}
                  >
                    {managerDetails.firstName} {managerDetails.lastName}
                  </p>
                  <p className="text-sm text-gray-600">{managerDetails.email}</p>
                  {managerDetails.jobTitle && (
                    <p className="text-sm text-gray-600">{managerDetails.jobTitle}</p>
                  )}
                  <p className="text-xs text-gray-500 mt-1">
                    Effective from: {new Date(currentHierarchy.effectiveFrom).toLocaleDateString()}
                  </p>
                </div>
              </div>
              <button
                onClick={handleRemoveManager}
                disabled={deactivateHierarchyMutation.isPending}
                className="p-1 text-red-600 hover:bg-red-50 rounded disabled:text-gray-400"
                title="Remove manager"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          {/* Management Chain */}
          <div className="border border-gray-200 rounded-md">
            <button
              onClick={() => setShowManagementChain(!showManagementChain)}
              className="w-full flex items-center justify-between p-4 hover:bg-gray-50"
            >
              <div className="flex items-center gap-2">
                <TrendingUp className="w-5 h-5 text-gray-600" />
                <span className="font-medium text-gray-900">Management Chain</span>
              </div>
              {showManagementChain ? (
                <ChevronUp className="w-5 h-5 text-gray-600" />
              ) : (
                <ChevronDown className="w-5 h-5 text-gray-600" />
              )}
            </button>
            {showManagementChain && (
              <div className="p-4 border-t border-gray-200">
                {isLoadingChain ? (
                  <p className="text-gray-500 text-sm">Loading management chain...</p>
                ) : managementChain && managementChain.length > 0 ? (
                  <div className="space-y-2">
                    {managementChain.map((node, index) => (
                      <div
                        key={node.id}
                        className="flex items-center gap-2 p-2 bg-gray-50 rounded"
                        style={{ marginLeft: `${index * 16}px` }}
                      >
                        <span className="text-xs font-medium text-gray-500 min-w-[60px]">
                          Level {node.level}
                        </span>
                        <div className="flex-1">
                          <p
                            className="text-sm font-medium text-gray-900 cursor-pointer hover:text-blue-600 hover:underline transition-colors"
                            onClick={() => router.push(`/dashboard/users/${node.userId}`)}
                          >
                            {node.firstName} {node.lastName}
                          </p>
                          <p className="text-xs text-gray-600">{node.email}</p>
                          {node.jobTitle && (
                            <p className="text-xs text-gray-500">{node.jobTitle}</p>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-gray-500 text-sm">No management chain available</p>
                )}
              </div>
            )}
          </div>
        </div>
      ) : (
        !isEditingManager && (
          <div className="text-center py-8 text-gray-500">
            <UserCog className="w-12 h-12 mx-auto mb-2 text-gray-400" />
            <p>No manager assigned</p>
            <p className="text-sm mt-1">Click "Assign Manager" to set one</p>
          </div>
        )
      )}

      {/* Direct Reports Section */}
      <div className="mt-4 border border-gray-200 rounded-md">
        <button
          onClick={() => setShowDirectReports(!showDirectReports)}
          className="w-full flex items-center justify-between p-4 hover:bg-gray-50"
        >
          <div className="flex items-center gap-2">
            <Users className="w-5 h-5 text-gray-600" />
            <span className="font-medium text-gray-900">Direct Reports</span>
            {directReports && directReports.length > 0 && (
              <span className="px-2 py-0.5 text-xs font-medium bg-blue-100 text-blue-800 rounded-full">
                {directReports.length}
              </span>
            )}
          </div>
          {showDirectReports ? (
            <ChevronUp className="w-5 h-5 text-gray-600" />
          ) : (
            <ChevronDown className="w-5 h-5 text-gray-600" />
          )}
        </button>
        {showDirectReports && (
          <div className="p-4 border-t border-gray-200">
            {isLoadingReports ? (
              <p className="text-gray-500 text-sm">Loading direct reports...</p>
            ) : directReports && directReports.length > 0 ? (
              <div className="space-y-2">
                {directReports.map((report) => (
                  <div key={report.id} className="flex items-center gap-3 p-3 bg-gray-50 rounded-md">
                    <Users className="w-4 h-4 text-gray-500" />
                    <div className="flex-1">
                      <p
                        className="text-sm font-medium text-gray-900 cursor-pointer hover:text-blue-600 hover:underline transition-colors"
                        onClick={() => router.push(`/dashboard/users/${report.userId}`)}
                      >
                        {report.firstName} {report.lastName}
                      </p>
                      <p className="text-xs text-gray-600">{report.email}</p>
                      {report.jobTitle && (
                        <p className="text-xs text-gray-500">{report.jobTitle}</p>
                      )}
                      {report.businessUnitName && (
                        <p className="text-xs text-blue-600">BU: {report.businessUnitName}</p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 text-sm">
                {userName || 'This user'} has no direct reports
              </p>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
