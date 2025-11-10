'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import DataTable, { DataTableColumn, BulkAction } from '@/components/ui/DataTable';
import EmptyState from '@/components/common/EmptyState';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { userApi, User } from '@/lib/api/users';
import { roleApi } from '@/lib/api/roles';
import {
  Users as UsersIcon,
  UserCheck,
  UserX,
  UserPlus,
  Mail,
  Phone,
  Plus,
  Edit,
  Trash2,
  Download,
  Shield,
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function UsersPage() {
  const router = useRouter();
  const { t } = useTranslation();
  const [roleFilter, setRoleFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);

  const queryClient = useQueryClient();

  const { data: paginatedData, isLoading } = useQuery({
    queryKey: ['users', page, pageSize],
    queryFn: () => userApi.getUsers(page, pageSize),
  });

  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleApi.getRoles(),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => userApi.deleteUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete user');
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => Promise.all(ids.map(id => userApi.deleteUser(id))),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('Users deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete users');
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: number) => userApi.deactivateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User deactivated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to deactivate user');
    },
  });

  const reactivateMutation = useMutation({
    mutationFn: (id: number) => userApi.reactivateUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User reactivated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to reactivate user');
    },
  });

  const handleDeleteUser = (userId: number) => {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      deleteMutation.mutate(userId);
    }
  };

  const handleBulkDelete = (ids: (string | number)[]) => {
    if (confirm(`Are you sure you want to delete ${ids.length} user(s)? This action cannot be undone.`)) {
      bulkDeleteMutation.mutate(ids as number[]);
    }
  };

  const handleBulkExport = (ids: (string | number)[]) => {
    const selectedUsers = filteredUsers.filter((user: any) => ids.includes(user.id));
    const csv = convertToCSV(selectedUsers);
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `users-${new Date().toISOString().split('T')[0]}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success('Selected users exported successfully');
  };

  const convertToCSV = (data: any[]) => {
    if (data.length === 0) return '';
    const headers = ['ID', 'First Name', 'Last Name', 'Email', 'Role', 'Phone', 'Status'].join(',');
    const rows = data.map(user =>
      [user.id, user.firstName, user.lastName, user.email, user.role, user.phone || '', user.active ? 'Active' : 'Inactive'].join(',')
    );
    return [headers, ...rows].join('\n');
  };

  const handleDeactivateUser = (userId: number) => {
    if (confirm('Are you sure you want to deactivate this user?')) {
      deactivateMutation.mutate(userId);
    }
  };

  const handleReactivateUser = (userId: number) => {
    reactivateMutation.mutate(userId);
  };

  const handleEditUser = (userId: number) => {
    router.push(`/dashboard/users/${userId}/edit`);
  };

  const handleCreateUser = () => {
    router.push('/dashboard/users/create');
  };

  const users = paginatedData?.content || [];
  const totalPages = paginatedData?.totalPages || 0;
  const totalElements = paginatedData?.totalElements || 0;

  const filteredUsers = users.filter((user) => {
    const matchesRole = !roleFilter || user.role === roleFilter;
    const matchesStatus =
      statusFilter === 'all' ||
      (statusFilter === 'active' && user.active) ||
      (statusFilter === 'inactive' && !user.active);
    return matchesRole && matchesStatus;
  });

  // Calculate statistics
  const stats = {
    totalUsers: totalElements,
    activeUsers: users.filter((user: any) => user.active).length,
    inactiveUsers: users.filter((user: any) => !user.active).length,
    admins: users.filter((user: any) => user.role === 'ADMIN').length,
  };

  // Define DataTable columns
  const columns: DataTableColumn[] = [
    {
      key: 'firstName',
      label: 'First Name',
      sortable: true,
      render: (value: string) => (
        <span className="font-medium text-gray-900 dark:text-white">{value}</span>
      ),
    },
    {
      key: 'lastName',
      label: 'Last Name',
      sortable: true,
      render: (value: string) => (
        <span className="font-medium text-gray-900 dark:text-white">{value}</span>
      ),
    },
    {
      key: 'email',
      label: 'Email',
      sortable: true,
      render: (value: string) => (
        <div className="flex items-center gap-2">
          <Mail className="w-4 h-4 text-gray-400" />
          <span className="text-sm text-gray-600 dark:text-gray-400">{value}</span>
        </div>
      ),
    },
    {
      key: 'role',
      label: 'Role',
      sortable: true,
      render: (value: string) => (
        <span className="text-xs px-2 py-1 rounded bg-brand-100 text-brand-800 dark:bg-brand-900 dark:text-brand-200">
          {value || 'N/A'}
        </span>
      ),
    },
    {
      key: 'phone',
      label: 'Phone',
      sortable: true,
      render: (value: string) =>
        value ? (
          <div className="flex items-center gap-2">
            <Phone className="w-4 h-4 text-gray-400" />
            <span className="text-sm text-gray-600 dark:text-gray-400">{value}</span>
          </div>
        ) : (
          <span className="text-sm text-gray-400">-</span>
        ),
    },
    {
      key: 'active',
      label: 'Status',
      sortable: true,
      render: (value: boolean) => (
        <span
          className={`text-xs px-2 py-1 rounded font-medium ${
            value
              ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200'
              : 'bg-gray-100 text-gray-800 dark:bg-gray-800 dark:text-gray-300'
          }`}
        >
          {value ? 'Active' : 'Inactive'}
        </span>
      ),
    },
    {
      key: 'id',
      label: 'Actions',
      render: (value: number, row: any) => (
        <div className="flex items-center justify-end gap-2">
          <button
            onClick={() => handleEditUser(value)}
            className="p-1 hover:bg-gray-100 dark:hover:bg-gray-800 rounded transition-colors"
            title="Edit User"
          >
            <Edit className="w-4 h-4 text-blue-600 dark:text-blue-400" />
          </button>
          {row.active ? (
            <button
              onClick={() => handleDeactivateUser(value)}
              className="p-1 hover:bg-orange-50 dark:hover:bg-orange-900/20 rounded transition-colors"
              title="Deactivate User"
            >
              <UserX className="w-4 h-4 text-orange-600 dark:text-orange-400" />
            </button>
          ) : (
            <button
              onClick={() => handleReactivateUser(value)}
              className="p-1 hover:bg-green-50 dark:hover:bg-green-900/20 rounded transition-colors"
              title="Reactivate User"
            >
              <UserCheck className="w-4 h-4 text-green-600 dark:text-green-400" />
            </button>
          )}
          <button
            onClick={() => handleDeleteUser(value)}
            className="p-1 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
            title="Delete User"
          >
            <Trash2 className="w-4 h-4 text-red-600 dark:text-red-400" />
          </button>
        </div>
      ),
    },
  ];

  // Define bulk actions
  const bulkActions: BulkAction[] = [
    {
      label: 'Delete Selected',
      icon: <Trash2 className="w-4 h-4" />,
      onClick: handleBulkDelete,
      variant: 'danger',
    },
    {
      label: 'Export Selected',
      icon: <Download className="w-4 h-4" />,
      onClick: handleBulkExport,
      variant: 'secondary',
    },
  ];

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb items={[{ label: t('nav.dashboard'), href: '/dashboard' }, { label: t('nav.users') }]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{t('users.users')}</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">{t('users.manageUsers')}</p>
        </div>
        <Button
          variant="primary"
          onClick={handleCreateUser}
          className="flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          {t('users.createUser')}
        </Button>
      </div>

      {/* Quick Action Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">{t('users.users')}</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.totalUsers}
                </p>
              </div>
              <div className="w-12 h-12 bg-brand-100 dark:bg-brand-900/30 rounded-lg flex items-center justify-center">
                <UsersIcon className="w-6 h-6 text-brand-600 dark:text-brand-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">{t('users.activeUsers')}</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.activeUsers}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-lg flex items-center justify-center">
                <UserCheck className="w-6 h-6 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Inactive Users</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.inactiveUsers}
                </p>
              </div>
              <div className="w-12 h-12 bg-gray-100 dark:bg-gray-800 rounded-lg flex items-center justify-center">
                <UserX className="w-6 h-6 text-gray-600 dark:text-gray-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Administrators</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.admins}
                </p>
              </div>
              <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 rounded-lg flex items-center justify-center">
                <Shield className="w-6 h-6 text-purple-600 dark:text-purple-400" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between flex-wrap gap-4">
            <CardTitle>All Users ({totalElements})</CardTitle>
            <div className="flex items-center gap-3">
              <select
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value)}
                className="px-3 py-2 text-sm border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:ring-2 focus:ring-brand-500/12 focus:border-brand-500 transition-colors"
              >
                <option value="">All Roles</option>
                {roles.map((role) => (
                  <option key={role.code} value={role.code}>
                    {role.name}
                  </option>
                ))}
              </select>
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="px-3 py-2 text-sm border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:ring-2 focus:ring-brand-500/12 focus:border-brand-500 transition-colors"
              >
                <option value="all">All Status</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
              </select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-12 text-gray-500">Loading users...</div>
          ) : filteredUsers.length === 0 ? (
            <EmptyState
              icon={<UserPlus className="w-12 h-12" />}
              title="No users found"
              description="Get started by adding your first user to the system"
              actionLabel="Add User"
              onAction={handleCreateUser}
            />
          ) : (
            <DataTable
              columns={columns}
              data={filteredUsers}
              keyExtractor={(row: any) => row.id}
              isLoading={isLoading}
              searchable={true}
              searchPlaceholder="Search users by name, email..."
              searchKeys={['firstName', 'lastName', 'email', 'phone']}
              sortable={true}
              selectable={true}
              bulkActions={bulkActions}
              pagination={true}
              pageSize={20}
              pageSizeOptions={[10, 20, 50, 100]}
              emptyMessage="No users found"
              emptyIcon={<UsersIcon className="w-12 h-12" />}
              stickyHeader={false}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
