'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import DataTable, { DataTableColumn, BulkAction } from '@/components/ui/DataTable';
import EmptyState from '@/components/common/EmptyState';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { workflowApi } from '@/lib/api/workflows';
import {
  Plus,
  Edit2,
  Trash2,
  Play,
  Pause,
  GitBranch,
  Activity,
  CheckCircle,
  XCircle,
  Download,
} from 'lucide-react';
import { formatDateTime, getStatusColor } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function WorkflowsPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'inactive'>('all');

  const { data: workflows = [], isLoading } = useQuery({
    queryKey: ['workflows'],
    queryFn: () => workflowApi.getWorkflows(),
  });

  const toggleStatusMutation = useMutation({
    mutationFn: ({ code, isActive }: { code: string; isActive: boolean }) =>
      workflowApi.setWorkflowStatus(code, isActive),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      toast.success('Workflow status updated');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to update workflow status');
    },
  });

  const deleteWorkflowMutation = useMutation({
    mutationFn: (code: string) => workflowApi.deleteWorkflow(code),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      toast.success('Workflow deleted');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete workflow');
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: (codes: string[]) => Promise.all(codes.map(code => workflowApi.deleteWorkflow(code))),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      toast.success('Workflows deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete workflows');
    },
  });

  const bulkActivateMutation = useMutation({
    mutationFn: (codes: string[]) => Promise.all(codes.map(code => workflowApi.setWorkflowStatus(code, true))),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      toast.success('Workflows activated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to activate workflows');
    },
  });

  const bulkDeactivateMutation = useMutation({
    mutationFn: (codes: string[]) => Promise.all(codes.map(code => workflowApi.setWorkflowStatus(code, false))),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      toast.success('Workflows deactivated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to deactivate workflows');
    },
  });

  const handleToggleStatus = (code: string, currentStatus: boolean) => {
    toggleStatusMutation.mutate({ code, isActive: !currentStatus });
  };

  const handleDelete = (code: string) => {
    if (confirm('Are you sure you want to delete this workflow?')) {
      deleteWorkflowMutation.mutate(code);
    }
  };

  const handleBulkDelete = (ids: (string | number)[]) => {
    if (confirm(`Are you sure you want to delete ${ids.length} workflow(s)? This action cannot be undone.`)) {
      bulkDeleteMutation.mutate(ids as string[]);
    }
  };

  const handleBulkActivate = (ids: (string | number)[]) => {
    bulkActivateMutation.mutate(ids as string[]);
  };

  const handleBulkDeactivate = (ids: (string | number)[]) => {
    bulkDeactivateMutation.mutate(ids as string[]);
  };

  const handleBulkExport = (ids: (string | number)[]) => {
    const selectedWorkflows = filteredWorkflows.filter((workflow: any) =>
      ids.includes(workflow.processCode)
    );
    const json = JSON.stringify(selectedWorkflows, null, 2);
    const blob = new Blob([json], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `workflows-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
    toast.success('Selected workflows exported successfully');
  };

  const filteredWorkflows = workflows.filter((workflow: any) => {
    if (statusFilter === 'active') return workflow.isActive;
    if (statusFilter === 'inactive') return !workflow.isActive;
    return true;
  });

  // Calculate statistics
  const stats = {
    totalWorkflows: workflows.length,
    activeWorkflows: workflows.filter((w: any) => w.isActive).length,
    inactiveWorkflows: workflows.filter((w: any) => !w.isActive).length,
    totalStates: workflows.reduce((sum: number, w: any) => sum + (w.stateCount || 0), 0),
  };

  // Define DataTable columns
  const columns: DataTableColumn[] = [
    {
      key: 'processCode',
      label: 'Code',
      sortable: true,
      render: (value: string) => (
        <span className="font-mono text-xs font-medium">{value}</span>
      ),
    },
    {
      key: 'processName',
      label: 'Name',
      sortable: true,
      render: (value: string, row: any) => (
        <div>
          <p className="font-medium text-gray-900 dark:text-white">{value}</p>
          {row.description && (
            <p className="text-sm text-gray-500 dark:text-gray-400">{row.description}</p>
          )}
        </div>
      ),
    },
    {
      key: 'orderType',
      label: 'Order Type',
      sortable: true,
      render: (value: string) => {
        const typeConfig: Record<string, { label: string; color: string; bg: string }> = {
          'VN': { label: 'VN - Véhicule Neuf', color: 'text-blue-800 dark:text-blue-200', bg: 'bg-blue-100 dark:bg-blue-900/30' },
          'VO': { label: 'VO - Véhicule Occasion', color: 'text-purple-800 dark:text-purple-200', bg: 'bg-purple-100 dark:bg-purple-900/30' },
          'EVO': { label: 'EVO - Évolution', color: 'text-orange-800 dark:text-orange-200', bg: 'bg-orange-100 dark:bg-orange-900/30' },
        };
        const config = typeConfig[value] || { label: value, color: 'text-gray-800', bg: 'bg-gray-100' };

        return (
          <span className={`text-xs px-2.5 py-1 rounded-full font-semibold ${config.bg} ${config.color}`}>
            {config.label}
          </span>
        );
      },
    },
    {
      key: 'version',
      label: 'Version',
      sortable: true,
      render: (value: number) => (
        <span className="text-sm text-gray-600 dark:text-gray-400">v{value || '1'}</span>
      ),
    },
    {
      key: 'stateCount',
      label: 'States',
      sortable: true,
      render: (value: number) => (
        <span className="text-sm font-medium text-gray-900 dark:text-white">
          {value || 0}
        </span>
      ),
    },
    {
      key: 'isActive',
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
      key: 'createdAt',
      label: 'Created',
      sortable: true,
      render: (value: string) => (
        <span className="text-sm text-gray-500 dark:text-gray-400">
          {value ? formatDateTime(value) : 'N/A'}
        </span>
      ),
    },
    {
      key: 'processCode',
      label: 'Actions',
      render: (value: string, row: any) => (
        <div className="flex items-center gap-2">
          <button
            onClick={() => router.push(`/dashboard/workflows/builder?code=${value}`)}
            className="p-1 hover:bg-gray-100 dark:hover:bg-gray-800 rounded transition-colors"
            title="Edit"
          >
            <Edit2 className="w-4 h-4 text-gray-600 dark:text-gray-400" />
          </button>
          <button
            onClick={() => handleToggleStatus(value, row.isActive)}
            className="p-1 hover:bg-gray-100 dark:hover:bg-gray-800 rounded transition-colors"
            title={row.isActive ? 'Deactivate' : 'Activate'}
          >
            {row.isActive ? (
              <Pause className="w-4 h-4 text-yellow-600 dark:text-yellow-400" />
            ) : (
              <Play className="w-4 h-4 text-green-600 dark:text-green-400" />
            )}
          </button>
          <button
            onClick={() => handleDelete(value)}
            className="p-1 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
            title="Delete"
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
      label: 'Activate Selected',
      icon: <Play className="w-4 h-4" />,
      onClick: handleBulkActivate,
      variant: 'secondary',
    },
    {
      label: 'Deactivate Selected',
      icon: <Pause className="w-4 h-4" />,
      onClick: handleBulkDeactivate,
      variant: 'secondary',
    },
    {
      label: 'Export Selected',
      icon: <Download className="w-4 h-4" />,
      onClick: handleBulkExport,
      variant: 'secondary',
    },
    {
      label: 'Delete Selected',
      icon: <Trash2 className="w-4 h-4" />,
      onClick: handleBulkDelete,
      variant: 'danger',
    },
  ];

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb items={[{ label: t('nav.dashboard'), href: '/dashboard' }, { label: t('nav.workflows') }]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{t('workflows.workflows')}</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">{t('workflows.manageWorkflows')}</p>
        </div>
        <Button
          variant="primary"
          onClick={() => router.push('/dashboard/workflows/builder')}
          className="flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          {t('workflows.createWorkflow')}
        </Button>
      </div>

      {/* Quick Action Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">{t('workflows.totalWorkflows')}</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.totalWorkflows}
                </p>
              </div>
              <div className="w-12 h-12 bg-brand-100 dark:bg-brand-900/30 rounded-lg flex items-center justify-center">
                <GitBranch className="w-6 h-6 text-brand-600 dark:text-brand-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Active Workflows</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.activeWorkflows}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-lg flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Inactive Workflows</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.inactiveWorkflows}
                </p>
              </div>
              <div className="w-12 h-12 bg-gray-100 dark:bg-gray-800 rounded-lg flex items-center justify-center">
                <XCircle className="w-6 h-6 text-gray-600 dark:text-gray-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Total States</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.totalStates}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center">
                <Activity className="w-6 h-6 text-blue-600 dark:text-blue-400" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>All Workflows</CardTitle>
            <div className="flex items-center gap-3">
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as 'all' | 'active' | 'inactive')}
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
            <div className="text-center py-12 text-gray-500">Loading workflows...</div>
          ) : filteredWorkflows.length === 0 ? (
            <EmptyState
              icon={<GitBranch className="w-12 h-12" />}
              title="No workflows found"
              description="Get started by creating your first workflow process"
              actionLabel="Create Workflow"
              onAction={() => router.push('/dashboard/workflows/builder')}
            />
          ) : (
            <DataTable
              columns={columns}
              data={filteredWorkflows}
              keyExtractor={(row: any) => row.processCode || row.id}
              isLoading={isLoading}
              searchable={true}
              searchPlaceholder="Search workflows by name or code..."
              searchKeys={['processName', 'processCode', 'description']}
              sortable={true}
              selectable={true}
              bulkActions={bulkActions}
              pagination={true}
              pageSize={20}
              pageSizeOptions={[10, 20, 50, 100]}
              emptyMessage="No workflows found"
              emptyIcon={<GitBranch className="w-12 h-12" />}
              stickyHeader={false}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
