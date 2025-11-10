'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi, UserRole } from '@/lib/api/users';
import { roleApi, AssignRoleRequest } from '@/lib/api/roles';
import { Shield, Plus, X, AlertCircle, Calendar, Tag } from 'lucide-react';
import Button from '@/components/ui/Button';
import toast from 'react-hot-toast';

interface UserRoleManagementProps {
  userId: number;
}

export default function UserRoleManagement({ userId }: UserRoleManagementProps) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [isAddingRole, setIsAddingRole] = useState(false);
  const [selectedRoleCode, setSelectedRoleCode] = useState<string>('');
  const [expiresAt, setExpiresAt] = useState<string>('');
  const [error, setError] = useState<string | null>(null);

  // Fetch user's current roles
  const { data: userRoles = [], isLoading: isLoadingRoles } = useQuery({
    queryKey: ['user-roles', userId],
    queryFn: () => userApi.getUserRoles(userId),
  });

  // Fetch all available roles
  const { data: allRoles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleApi.getRoles(),
  });

  // Get assigned role codes
  const assignedRoleCodes = userRoles.map((ur) => ur.roleCode);

  // Filter available roles (not yet assigned)
  const availableRoles = allRoles.filter(
    (role) => !assignedRoleCodes.includes(role.code)
  );

  // Assign role to user mutation
  const assignRoleMutation = useMutation({
    mutationFn: (data: AssignRoleRequest) => roleApi.assignRole(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-roles', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      setIsAddingRole(false);
      setSelectedRoleCode('');
      setExpiresAt('');
      setError(null);
      toast.success('Role assigned successfully');
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to assign role');
      toast.error(error.message || 'Failed to assign role');
    },
  });

  // Remove role from user mutation
  const removeRoleMutation = useMutation({
    mutationFn: (userRoleId: number) => roleApi.removeUserRole(userRoleId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-roles', userId] });
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'detail'] });
      setError(null);
      toast.success('Role removed successfully');
    },
    onError: (error: any) => {
      setError(error.message || 'Failed to remove role');
      toast.error(error.message || 'Failed to remove role');
    },
  });

  const handleAssignRole = () => {
    if (!selectedRoleCode) {
      setError('Please select a role');
      return;
    }

    const assignData: AssignRoleRequest = {
      userId,
      roleCode: selectedRoleCode,
    };

    if (expiresAt) {
      assignData.expiresAt = expiresAt;
    }

    assignRoleMutation.mutate(assignData);
  };

  const handleRemoveRole = (userRole: UserRole) => {
    if (confirm(`Are you sure you want to remove the role "${userRole.roleName}"?`)) {
      removeRoleMutation.mutate(userRole.id);
    }
  };

  const getRoleDetails = (roleCode: string) => {
    return allRoles.find((role) => role.code === roleCode);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getRoleSourceColor = (source: string) => {
    switch (source.toLowerCase()) {
      case 'keycloak':
        return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'manual':
        return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'inherited':
        return 'bg-green-100 text-green-800 border-green-200';
      default:
        return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  if (isLoadingRoles) {
    return (
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center gap-2 mb-4">
          <Shield className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Role Management</h3>
        </div>
        <p className="text-gray-500">Loading roles...</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Shield className="w-5 h-5 text-gray-600" />
          <h3 className="text-lg font-semibold text-gray-900">Role Management</h3>
        </div>
        {!isAddingRole && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsAddingRole(true)}
            disabled={availableRoles.length === 0}
          >
            <Plus className="w-4 h-4 mr-1" />
            Add Role
          </Button>
        )}
      </div>

      {error && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-md flex items-start gap-2">
          <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-red-800">{error}</p>
        </div>
      )}

      {/* Add Role Form */}
      {isAddingRole && (
        <div className="mb-4 p-4 bg-gray-50 rounded-md border border-gray-200">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Select Role
          </label>
          <select
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
            value={selectedRoleCode}
            onChange={(e) => setSelectedRoleCode(e.target.value)}
          >
            <option value="">Select a role...</option>
            {availableRoles.map((role) => (
              <option key={role.code} value={role.code}>
                {role.name} ({role.code}) {role.level ? `- Level ${role.level}` : ''}
              </option>
            ))}
          </select>

          {/* Optional Expiration Date */}
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Expiration Date (Optional)
          </label>
          <input
            type="date"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3"
            value={expiresAt}
            onChange={(e) => setExpiresAt(e.target.value)}
            min={new Date().toISOString().split('T')[0]}
          />

          <div className="flex gap-2">
            <Button
              variant="primary"
              size="sm"
              onClick={handleAssignRole}
              disabled={!selectedRoleCode || assignRoleMutation.isPending}
              isLoading={assignRoleMutation.isPending}
            >
              Assign Role
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => {
                setIsAddingRole(false);
                setSelectedRoleCode('');
                setExpiresAt('');
                setError(null);
              }}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}

      {/* Role List */}
      {userRoles.length > 0 ? (
        <div className="space-y-3">
          {userRoles.map((userRole) => {
            const roleDetails = getRoleDetails(userRole.roleCode);
            return (
              <div
                key={userRole.id}
                className="flex items-start justify-between p-4 border border-gray-200 rounded-md hover:bg-gray-50 transition-colors"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <Shield className="w-4 h-4 text-gray-500" />
                    <h4 className="font-medium text-gray-900">
                      {userRole.roleName}
                    </h4>
                  </div>

                  <div className="mt-2 space-y-1">
                    <div className="flex items-center gap-2 text-sm">
                      <Tag className="w-3 h-3 text-gray-400" />
                      <span className="text-gray-600">Code:</span>
                      <code className="px-1.5 py-0.5 bg-gray-100 rounded text-xs font-mono">
                        {userRole.roleCode}
                      </code>
                    </div>

                    <div className="flex items-center gap-2 text-sm">
                      <span className="text-gray-600">Source:</span>
                      <span
                        className={`px-2 py-0.5 rounded text-xs font-medium border ${getRoleSourceColor(
                          userRole.roleSource
                        )}`}
                      >
                        {userRole.roleSource}
                      </span>
                    </div>

                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <Calendar className="w-3 h-3 text-gray-400" />
                      <span>Assigned:</span>
                      <span>{formatDate(userRole.assignedAt)}</span>
                    </div>

                    {roleDetails?.description && (
                      <p className="text-xs text-gray-500 mt-2">
                        {roleDetails.description}
                      </p>
                    )}

                    {roleDetails?.level !== undefined && (
                      <div className="text-xs text-gray-500">
                        Level: {roleDetails.level}
                      </div>
                    )}
                  </div>
                </div>

                <button
                  onClick={() => handleRemoveRole(userRole)}
                  disabled={
                    removeRoleMutation.isPending ||
                    userRole.roleSource.toLowerCase() === 'keycloak'
                  }
                  className="p-1 text-red-600 hover:bg-red-50 rounded disabled:text-gray-400 disabled:hover:bg-transparent"
                  title={
                    userRole.roleSource.toLowerCase() === 'keycloak'
                      ? 'Keycloak roles cannot be removed manually'
                      : 'Remove role'
                  }
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="text-center py-8 text-gray-500">
          <Shield className="w-12 h-12 mx-auto mb-2 text-gray-400" />
          <p>No roles assigned</p>
          <p className="text-sm mt-1">Click "Add Role" to assign one</p>
        </div>
      )}

      {availableRoles.length === 0 && !isAddingRole && userRoles.length > 0 && (
        <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-md">
          <p className="text-sm text-blue-800">
            All available roles have been assigned to this user.
          </p>
        </div>
      )}
    </div>
  );
}
