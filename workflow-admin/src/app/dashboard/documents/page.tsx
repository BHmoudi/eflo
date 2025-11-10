'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import DataTable, { DataTableColumn } from '@/components/ui/DataTable';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { documentsApi, documentSearchApi } from '@/lib/api/documents';
import {
  FileText,
  Upload,
  Download,
  Trash2,
  Eye,
  Archive,
  Clock,
  CheckCircle,
  XCircle,
  AlertCircle,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { formatDateTime } from '@/lib/utils';

export default function DocumentsPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const [statusFilter, setStatusFilter] = useState<string>('all');

  // Fetch document statistics
  const { data: stats } = useQuery({
    queryKey: ['document-statistics'],
    queryFn: () => documentsApi.getStatistics(),
  });

  // Fetch all documents using search endpoint
  const { data: documentsResponse, isLoading } = useQuery({
    queryKey: ['documents', statusFilter],
    queryFn: async () => {
      const searchRequest: any = {
        page: 0,
        size: 100,
        latestVersionOnly: true,
      };

      if (statusFilter !== 'all') {
        searchRequest.validationStatus = statusFilter.toUpperCase();
      }

      return documentSearchApi.advancedSearch(searchRequest);
    },
  });

  const documents = documentsResponse?.content || [];

  const deleteMutation = useMutation({
    mutationFn: (id: number) => documentsApi.deleteDocument(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      queryClient.invalidateQueries({ queryKey: ['document-statistics'] });
      toast.success('Document deleted successfully');
    },
    onError: () => {
      toast.error('Failed to delete document');
    },
  });

  const archiveMutation = useMutation({
    mutationFn: (id: number) => documentsApi.archiveDocument(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      toast.success('Document archived successfully');
    },
    onError: () => {
      toast.error('Failed to archive document');
    },
  });

  const handleDownload = async (id: number, filename: string) => {
    try {
      const blob = await documentsApi.downloadDocument(id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = filename;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('Document downloaded');
    } catch (error) {
      toast.error('Failed to download document');
    }
  };

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this document?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleArchive = (id: number) => {
    archiveMutation.mutate(id);
  };

  const columns: DataTableColumn[] = [
    {
      key: 'originalFilename',
      label: 'Filename',
      sortable: true,
      render: (value: string, row: any) => (
        <div className="flex items-center gap-2">
          <FileText className="w-4 h-4 text-gray-400" />
          <span className="font-medium">{value}</span>
        </div>
      ),
    },
    {
      key: 'typeName',
      label: 'Type',
      sortable: true,
    },
    {
      key: 'orderNumber',
      label: 'Order',
      sortable: true,
    },
    {
      key: 'fileSizeMB',
      label: 'Size',
      sortable: true,
      render: (value: number) => `${value.toFixed(2)} MB`,
    },
    {
      key: 'validationStatus',
      label: 'Status',
      sortable: true,
      render: (value: string) => {
        const statusConfig: any = {
          VALIDATED: { color: 'green', icon: CheckCircle, label: 'Validated' },
          PENDING: { color: 'yellow', icon: Clock, label: 'Pending' },
          REJECTED: { color: 'red', icon: XCircle, label: 'Rejected' },
          EXPIRED: { color: 'gray', icon: AlertCircle, label: 'Expired' },
        };
        const config = statusConfig[value] || statusConfig.PENDING;
        const Icon = config.icon;
        return (
          <span className={`inline-flex items-center gap-1 text-xs px-2 py-1 rounded font-medium bg-${config.color}-100 text-${config.color}-800`}>
            <Icon className="w-3 h-3" />
            {config.label}
          </span>
        );
      },
    },
    {
      key: 'uploadedAt',
      label: 'Uploaded',
      sortable: true,
      render: (value: string) => formatDateTime(value),
    },
    {
      key: 'id',
      label: 'Actions',
      render: (value: number, row: any) => (
        <div className="flex items-center gap-2">
          <button
            onClick={() => handleDownload(value, row.originalFilename)}
            className="p-1 hover:bg-gray-100 rounded"
            title="Download"
          >
            <Download className="w-4 h-4 text-blue-600" />
          </button>
          <button
            onClick={() => handleArchive(value)}
            className="p-1 hover:bg-gray-100 rounded"
            title="Archive"
          >
            <Archive className="w-4 h-4 text-yellow-600" />
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
      <Breadcrumb items={[
        { label: 'Dashboard', href: '/dashboard' },
        { label: 'Documents' }
      ]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Documents</h1>
          <p className="text-gray-600 mt-1">Manage order documents and files</p>
        </div>
        <Button
          variant="primary"
          onClick={() => router.push('/dashboard/documents/upload')}
          className="flex items-center gap-2"
        >
          <Upload className="w-4 h-4" />
          Upload Document
        </Button>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Documents</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{stats?.totalDocuments || 0}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <FileText className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Pending Validation</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{stats?.pendingDocuments || 0}</p>
              </div>
              <div className="w-12 h-12 bg-yellow-100 rounded-lg flex items-center justify-center">
                <Clock className="w-6 h-6 text-yellow-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Validated</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{stats?.validatedDocuments || 0}</p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Storage Used</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{stats?.totalStorageSizeGB?.toFixed(2) || '0.00'} GB</p>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <Archive className="w-6 h-6 text-purple-600" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Documents Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>All Documents</CardTitle>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-2 text-sm border border-gray-300 rounded-lg"
            >
              <option value="all">All Status</option>
              <option value="validated">Validated</option>
              <option value="pending">Pending</option>
              <option value="rejected">Rejected</option>
              <option value="expired">Expired</option>
            </select>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-12">Loading documents...</div>
          ) : documents.length === 0 ? (
            <div className="text-center py-12">
              <FileText className="w-16 h-16 text-gray-400 mx-auto mb-4" />
              <p className="text-gray-500">No documents found</p>
              <Button
                variant="primary"
                onClick={() => router.push('/dashboard/documents/upload')}
                className="mt-4"
              >
                Upload First Document
              </Button>
            </div>
          ) : (
            <DataTable
              columns={columns}
              data={documents}
              keyExtractor={(row: any) => row.id}
              searchable={true}
              searchPlaceholder="Search documents..."
              searchKeys={['originalFilename', 'typeCode', 'orderNumber']}
              pagination={true}
              pageSize={20}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
