'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { roleApi } from '@/lib/api/roles';
import { Plus, Users, Trash2, X } from 'lucide-react';
import { formatDateTime } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function RolesPage() {
  const queryClient = useQueryClient();
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [showAssignDialog, setShowAssignDialog] = useState(false);

  const [newRole, setNewRole] = useState({
    code: '',
    name: '',
    description: '',
    parentRoleCode: '',
    level: 1,
  });

  const [assignRole, setAssignRole] = useState({
    userId: '',
    roleCode: '',
  });

  const { data: roles = [], isLoading: rolesLoading } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleApi.getRoles(),
  });

  const { data: userRoles = [], isLoading: userRolesLoading } = useQuery({
    queryKey: ['user-roles'],
    queryFn: () => roleApi.getUserRoles(),
  });

  const createRoleMutation = useMutation({
    mutationFn: roleApi.createRole,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['roles'] });
      toast.success('Role created successfully');
      setShowCreateDialog(false);
      setNewRole({ code: '', name: '', description: '', parentRoleCode: '', level: 1 });
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create role');
    },
  });

  const assignRoleMutation = useMutation({
    mutationFn: (data: { userId: number; roleCode: string }) =>
      roleApi.assignRole(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-roles'] });
      toast.success('Role assigned successfully');
      setShowAssignDialog(false);
      setAssignRole({ userId: '', roleCode: '' });
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to assign role');
    },
  });

  const removeUserRoleMutation = useMutation({
    mutationFn: roleApi.removeUserRole,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user-roles'] });
      toast.success('Role removed successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to remove role');
    },
  });

  const handleCreateRole = () => {
    if (!newRole.code || !newRole.name) {
      toast.error('Please fill in required fields');
      return;
    }
    createRoleMutation.mutate(newRole);
  };

  const handleAssignRole = () => {
    if (!assignRole.userId || !assignRole.roleCode) {
      toast.error('Please fill in all fields');
      return;
    }
    assignRoleMutation.mutate({
      userId: parseInt(assignRole.userId),
      roleCode: assignRole.roleCode,
    });
  };

  const handleRemoveUserRole = (userRoleId: number) => {
    if (confirm('Are you sure you want to remove this role assignment?')) {
      removeUserRoleMutation.mutate(userRoleId);
    }
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb items={[{ label: 'Dashboard', href: '/dashboard' }, { label: 'Roles' }]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Roles</h1>
          <p className="text-gray-600 mt-1">Manage roles and permissions</p>
        </div>
        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={() => setShowAssignDialog(true)}
            className="flex items-center gap-2"
          >
            <Users className="w-4 h-4" />
            Assign Role
          </Button>
          <Button
            variant="primary"
            onClick={() => setShowCreateDialog(true)}
            className="flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            Create Role
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Roles List */}
        <Card>
          <CardHeader>
            <CardTitle>All Roles ({roles.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {rolesLoading ? (
              <div className="text-center py-12 text-gray-500">Loading roles...</div>
            ) : roles.length === 0 ? (
              <div className="text-center py-12 text-gray-500">No roles found</div>
            ) : (
              <div className="space-y-2">
                {roles.map((role) => (
                  <div
                    key={role.code}
                    className="p-4 border border-gray-200 rounded-lg hover:border-blue-300 transition-colors"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">{role.name}</p>
                        <p className="text-sm text-gray-500">{role.code}</p>
                        {role.description && (
                          <p className="text-sm text-gray-600 mt-1">{role.description}</p>
                        )}
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs px-2 py-1 bg-blue-100 text-blue-800 rounded">
                          Level {role.level}
                        </span>
                        <span
                          className={`text-xs px-2 py-1 rounded ${
                            role.isActive
                              ? 'bg-green-100 text-green-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}
                        >
                          {role.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                    </div>
                    {role.parentRoleCode && (
                      <p className="text-xs text-gray-500 mt-2">
                        Parent: {role.parentRoleCode}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* User Role Assignments */}
        <Card>
          <CardHeader>
            <CardTitle>User Role Assignments ({userRoles.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {userRolesLoading ? (
              <div className="text-center py-12 text-gray-500">Loading assignments...</div>
            ) : userRoles.length === 0 ? (
              <div className="text-center py-12 text-gray-500">No role assignments</div>
            ) : (
              <div className="space-y-2">
                {userRoles.map((userRole) => (
                  <div
                    key={userRole.id}
                    className="p-4 border border-gray-200 rounded-lg hover:border-red-200 transition-colors"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">
                          {userRole.userName || `User #${userRole.userId}`}
                        </p>
                        <p className="text-sm text-gray-500">{userRole.userEmail}</p>
                        <p className="text-sm text-blue-600 mt-1">{userRole.roleName}</p>
                      </div>
                      <button
                        onClick={() => handleRemoveUserRole(userRole.id)}
                        className="p-1 hover:bg-red-50 rounded transition-colors"
                      >
                        <Trash2 className="w-4 h-4 text-red-600" />
                      </button>
                    </div>
                    <p className="text-xs text-gray-500 mt-2">
                      Assigned {formatDateTime(userRole.assignedAt)}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Create Role Dialog */}
      {showCreateDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900">Create Role</h2>
              <button
                onClick={() => setShowCreateDialog(false)}
                className="p-1 hover:bg-gray-100 rounded transition-colors"
              >
                <X className="w-5 h-5 text-gray-500" />
              </button>
            </div>
            <div className="space-y-4">
              <Input
                label="Role Code"
                value={newRole.code}
                onChange={(e) => setNewRole({ ...newRole, code: e.target.value })}
                placeholder="e.g., ADMIN"
                required
              />
              <Input
                label="Role Name"
                value={newRole.name}
                onChange={(e) => setNewRole({ ...newRole, name: e.target.value })}
                placeholder="e.g., Administrator"
                required
              />
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={3}
                  value={newRole.description}
                  onChange={(e) => setNewRole({ ...newRole, description: e.target.value })}
                  placeholder="Role description..."
                />
              </div>
              <Select
                label="Parent Role"
                value={newRole.parentRoleCode}
                onChange={(e) => setNewRole({ ...newRole, parentRoleCode: e.target.value })}
              >
                <option value="">None</option>
                {roles.map((role) => (
                  <option key={role.code} value={role.code}>
                    {role.name}
                  </option>
                ))}
              </Select>
              <Input
                label="Level"
                type="number"
                value={newRole.level}
                onChange={(e) => setNewRole({ ...newRole, level: parseInt(e.target.value) })}
                min={1}
                max={10}
              />
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <Button variant="outline" onClick={() => setShowCreateDialog(false)}>
                Cancel
              </Button>
              <Button
                variant="primary"
                onClick={handleCreateRole}
                isLoading={createRoleMutation.isPending}
              >
                Create Role
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Assign Role Dialog */}
      {showAssignDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900">Assign Role</h2>
              <button
                onClick={() => setShowAssignDialog(false)}
                className="p-1 hover:bg-gray-100 rounded transition-colors"
              >
                <X className="w-5 h-5 text-gray-500" />
              </button>
            </div>
            <div className="space-y-4">
              <Input
                label="User ID"
                type="number"
                value={assignRole.userId}
                onChange={(e) => setAssignRole({ ...assignRole, userId: e.target.value })}
                placeholder="Enter user ID"
                required
              />
              <Select
                label="Role"
                value={assignRole.roleCode}
                onChange={(e) => setAssignRole({ ...assignRole, roleCode: e.target.value })}
                required
              >
                <option value="">Select a role</option>
                {roles
                  .filter((r) => r.isActive)
                  .map((role) => (
                    <option key={role.code} value={role.code}>
                      {role.name}
                    </option>
                  ))}
              </Select>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <Button variant="outline" onClick={() => setShowAssignDialog(false)}>
                Cancel
              </Button>
              <Button
                variant="primary"
                onClick={handleAssignRole}
                isLoading={assignRoleMutation.isPending}
              >
                Assign Role
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
