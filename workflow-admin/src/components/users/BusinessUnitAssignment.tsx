'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userBusinessUnitApi } from '@/lib/api/user-business-units';
import { businessUnitApi } from '@/lib/api/business-units';
import { Building2, Plus, X, Star, AlertCircle } from 'lucide-react';
import Button from '@/components/ui/Button';

interface BusinessUnitAssignmentProps {
  userId: number;
}

export default function BusinessUnitAssignment({ userId }: BusinessUnitAssignmentProps) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [isAddingBU, setIsAddingBU] = useState(false);
  const [selectedBusinessUnitId, setSelectedBusinessUnitId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Fetch user's business units
  const { data: userBusinessUnits, isLoading: isLoadingUserBUs } = useQuery({
    queryKey: ['user-business-units', userId],
    queryFn: () => userBusinessUnitApi.getUserBusinessUnits(userId),
  });

  // Fetch all active business units for assignment
  const { data: allBusinessUnits } = useQuery({
    queryKey: ['business-units', 'active'],
    queryFn: () => businessUnitApi.getActiveBusinessUnits(),
  });

  // Get assigned business unit IDs
  const assignedBUIds = userBusinessUnits?.map((ubu) => ubu.businessUnitId) || [];

  // Filter available business units (not yet assigned)
  const availableBusinessUnits = allBusinessUnits?.filter(
    (bu) => !assignedBUIds.includes(bu.id)
  ) || [];

  // Assign user to business unit mutation
  const assignMutation = useMutation({
    mutationFn: (businessUnitId: number) =>
      userBusinessUnitApi.assignUserToBusinessUnit({
        userId,
        businessUnitId,
        isPrimary: userBusinessUnits?.length === 0, // Set as primary if it's the first one
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-business-units', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      setIsAddingBU(false);
      setSelectedBusinessUnitId(null);
      setError(null);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to assign business unit');
    },
  });

  // Remove user from business unit mutation
  const removeMutation = useMutation({
    mutationFn: (businessUnitId: number) =>
      userBusinessUnitApi.removeUserFromBusinessUnit(userId, businessUnitId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-business-units', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      setError(null);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to remove business unit');
    },
  });

  // Set primary business unit mutation
  const setPrimaryMutation = useMutation({
    mutationFn: (businessUnitId: number) =>
      userBusinessUnitApi.setPrimaryBusinessUnit(userId, businessUnitId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-business-units', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      setError(null);
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to set primary business unit');
    },
  });

  const handleAssignBU = () => {
    if (selectedBusinessUnitId) {
      assignMutation.mutate(selectedBusinessUnitId);
    }
  };

  const handleRemoveBU = (businessUnitId: number) => {
    if (confirm('Are you sure you want to remove this business unit assignment?')) {
      removeMutation.mutate(businessUnitId);
    }
  };

  const handleSetPrimary = (businessUnitId: number) => {
    setPrimaryMutation.mutate(businessUnitId);
  };

  const getBusinessUnitDetails = (businessUnitId: number) => {
    return allBusinessUnits?.find((bu) => bu.id === businessUnitId);
  };

  if (isLoadingUserBUs) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-2 mb-4">
          <Building2 className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Business Unit Assignments</h3>
        </div>
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Building2 className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Business Unit Assignments</h3>
        </div>
        {!isAddingBU && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsAddingBU(true)}
            disabled={availableBusinessUnits.length === 0}
          >
            <Plus className="w-4 h-4 mr-1" />
            Add Business Unit
          </Button>
        )}
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md flex items-start gap-2">
          <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-red-800">{error}</p>
        </div>
      )}

      {/* Add Business Unit Form */}
      {isAddingBU && (
        <div className="mb-4 p-4 bg-gray-50 rounded-md border border-gray-200">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Select Business Unit
          </label>
          <select
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
            value={selectedBusinessUnitId || ''}
            onChange={(e) => setSelectedBusinessUnitId(Number(e.target.value))}
          >
            <option value="">Select a business unit...</option>
            {availableBusinessUnits.map((bu) => (
              <option key={bu.id} value={bu.id}>
                {bu.name} ({bu.code}) {bu.rrfCode ? `- RRF: ${bu.rrfCode}` : ''}
              </option>
            ))}
          </select>
          <div className="flex gap-2">
            <Button
              variant="primary"
              size="sm"
              onClick={handleAssignBU}
              disabled={!selectedBusinessUnitId || assignMutation.isPending}
            >
              {assignMutation.isPending ? 'Assigning...' : 'Assign'}
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setIsAddingBU(false);
                setSelectedBusinessUnitId(null);
                setError(null);
              }}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}

      {/* Business Unit List */}
      {userBusinessUnits && userBusinessUnits.length > 0 ? (
        <div className="space-y-3">
          {userBusinessUnits.map((userBU) => {
            const buDetails = getBusinessUnitDetails(userBU.businessUnitId);
            return (
              <div
                key={userBU.id}
                className="flex items-start justify-between p-4 border border-gray-200 rounded-md hover:bg-gray-50"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <Building2 className="w-4 h-4 text-gray-500" />
                    <h4
                      className="font-medium text-gray-900 cursor-pointer hover:text-blue-600 hover:underline transition-colors"
                      onClick={() => router.push(`/dashboard/business-units/${userBU.businessUnitId}`)}
                    >
                      {buDetails?.name || userBU.businessUnitName || 'Unknown Business Unit'}
                    </h4>
                    {userBU.isPrimary && (
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800">
                        <Star className="w-3 h-3 mr-1" />
                        Primary
                      </span>
                    )}
                  </div>
                  <div className="mt-1 text-sm text-gray-600">
                    <p>Code: {buDetails?.code || userBU.businessUnitCode}</p>
                    {buDetails?.rrfCode && (
                      <p className="font-medium text-blue-600">RRF Code: {buDetails.rrfCode}</p>
                    )}
                    {buDetails?.type && (
                      <p>Type: {buDetails.type.replace(/_/g, ' ')}</p>
                    )}
                    {buDetails?.city && buDetails?.country && (
                      <p>Location: {buDetails.city}, {buDetails.country}</p>
                    )}
                  </div>
                  <p className="mt-1 text-xs text-gray-500">
                    Assigned: {new Date(userBU.assignedAt).toLocaleDateString()}
                  </p>
                </div>
                <div className="flex items-center gap-2 ml-4">
                  {!userBU.isPrimary && (
                    <button
                      onClick={() => handleSetPrimary(userBU.businessUnitId)}
                      disabled={setPrimaryMutation.isPending}
                      className="text-sm text-blue-600 hover:text-blue-800 hover:underline disabled:text-gray-400"
                      title="Set as primary"
                    >
                      Set Primary
                    </button>
                  )}
                  <button
                    onClick={() => handleRemoveBU(userBU.businessUnitId)}
                    disabled={removeMutation.isPending || (userBU.isPrimary && userBusinessUnits.length > 1)}
                    className="p-1 text-red-600 hover:bg-red-50 rounded disabled:text-gray-400 disabled:hover:bg-transparent"
                    title={userBU.isPrimary && userBusinessUnits.length > 1 ? 'Cannot remove primary business unit when others exist' : 'Remove business unit'}
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="text-center py-8 text-gray-500">
          <Building2 className="w-12 h-12 mx-auto mb-2 text-gray-400" />
          <p>No business units assigned</p>
          <p className="text-sm mt-1">Click "Add Business Unit" to assign one</p>
        </div>
      )}
    </div>
  );
}
