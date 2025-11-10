'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import DataTable, { DataTableColumn } from '@/components/ui/DataTable';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { Plus, Edit2, Trash2, Eye, FileText, CheckCircle, XCircle } from 'lucide-react';
import { formatDateTime } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function TemplatesPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  const { data: templates = [], isLoading } = useQuery({
    queryKey: ['templates'],
    queryFn: async () => {
      const res = await fetch('/api/templates');
      if (!res.ok) throw new Error('Failed to fetch templates');
      return res.json();
    },
  });

  const deleteTemplateMutation = useMutation({
    mutationFn: async (templateCode: string) => {
      const res = await fetch(`/api/templates/${templateCode}`, { method: 'DELETE' });
      if (!res.ok) throw new Error('Failed to delete template');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['templates'] });
      toast.success('Template deleted');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete template');
    },
  });

  const toggleStatusMutation = useMutation({
    mutationFn: async ({ code, isActive }: { code: string; isActive: boolean }) => {
      const res = await fetch(`/api/templates/${code}/status?isActive=${!isActive}`, { method: 'PUT' });
      if (!res.ok) throw new Error('Failed to update status');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['templates'] });
      toast.success('Template status updated');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to update status');
    },
  });

  const handleDelete = (code: string) => {
    if (confirm('Are you sure you want to delete this template?')) {
      deleteTemplateMutation.mutate(code);
    }
  };

  const columns: DataTableColumn[] = [
    {
      key: 'templateCode',
      label: 'Code',
      sortable: true,
      render: (value: string) => (
        <span className="font-mono text-xs font-medium">{value}</span>
      ),
    },
    {
      key: 'templateName',
      label: 'Template Name',
      sortable: true,
      render: (value: string, row: any) => (
        <div>
          <p className="font-medium text-gray-900">{value}</p>
          {row.description && (
            <p className="text-sm text-gray-500">{row.description}</p>
          )}
        </div>
      ),
    },
    {
      key: 'templateType',
      label: 'Type',
      sortable: true,
      render: (value: string) => (
        <span className="text-xs px-2.5 py-1 rounded-full bg-purple-100 text-purple-800 font-medium">
          {value}
        </span>
      ),
    },
    {
      key: 'templateFormat',
      label: 'Format',
      sortable: true,
      render: (value: string) => (
        <span className="text-xs px-2 py-1 rounded bg-gray-100 text-gray-700 font-mono">
          {value}
        </span>
      ),
    },
    {
      key: 'requiresApproval',
      label: 'Approval',
      render: (value: boolean, row: any) => (
        <div>
          {value ? (
            <div>
              <span className="text-xs px-2 py-1 rounded bg-orange-100 text-orange-800">
                Required
              </span>
              {row.approvalRoles && (
                <p className="text-xs text-gray-500 mt-1">{row.approvalRoles}</p>
              )}
            </div>
          ) : (
            <span className="text-xs px-2 py-1 rounded bg-gray-100 text-gray-600">
              Not Required
            </span>
          )}
        </div>
      ),
    },
    {
      key: 'isActive',
      label: 'Status',
      sortable: true,
      render: (value: boolean) => (
        <span
          className={`text-xs px-2 py-1 rounded font-medium ${
            value ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
          }`}
        >
          {value ? 'Active' : 'Inactive'}
        </span>
      ),
    },
    {
      key: 'templateCode',
      label: 'Actions',
      render: (value: string, row: any) => (
        <div className="flex items-center gap-2">
          <button
            onClick={() => router.push(`/dashboard/templates/${value}/edit`)}
            className="p-1 hover:bg-gray-100 rounded"
            title="Edit"
          >
            <Edit2 className="w-4 h-4 text-blue-600" />
          </button>
          <button
            onClick={() => router.push(`/dashboard/templates/${value}/preview`)}
            className="p-1 hover:bg-gray-100 rounded"
            title="Preview"
          >
            <Eye className="w-4 h-4 text-purple-600" />
          </button>
          <button
            onClick={() => toggleStatusMutation.mutate({ code: value, isActive: row.isActive })}
            className="p-1 hover:bg-gray-100 rounded"
            title={row.isActive ? 'Deactivate' : 'Activate'}
          >
            {row.isActive ? (
              <XCircle className="w-4 h-4 text-yellow-600" />
            ) : (
              <CheckCircle className="w-4 h-4 text-green-600" />
            )}
          </button>
          <button
            onClick={() => handleDelete(value)}
            className="p-1 hover:bg-red-50 rounded"
            title="Delete"
          >
            <Trash2 className="w-4 h-4 text-red-600" />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Document Templates' },
        ]}
      />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Document Templates</h1>
          <p className="text-gray-600 mt-1">
            Create and manage templates for auto-generated documents
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => router.push('/dashboard/templates/create')}
          className="flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          Create Template
        </Button>
      </div>

      <Card>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-12">Loading templates...</div>
          ) : (
            <DataTable
              columns={columns}
              data={templates}
              keyExtractor={(row: any) => row.templateCode}
              searchable={true}
              searchPlaceholder="Search templates..."
              searchKeys={['templateName', 'templateCode', 'templateType']}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
