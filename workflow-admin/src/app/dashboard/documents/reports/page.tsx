'use client';

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { documentReportApi } from '@/lib/api/documents';
import {
  BarChart3,
  FileText,
  Download,
  TrendingUp,
  CheckCircle,
  Clock,
  XCircle,
  AlertTriangle,
  Calendar,
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function DocumentReportsPage() {
  const [dateRange, setDateRange] = useState({ fromDate: '', toDate: '' });

  // Fetch detailed statistics
  const { data: stats, isLoading: statsLoading } = useQuery({
    queryKey: ['document-stats', dateRange],
    queryFn: () =>
      documentReportApi.getStats(
        dateRange.fromDate || undefined,
        dateRange.toDate || undefined
      ),
  });

  // Fetch dashboard data
  const { data: dashboardData } = useQuery({
    queryKey: ['document-dashboard'],
    queryFn: () => documentReportApi.getDashboardData(),
  });

  // Fetch validation report
  const { data: validationReport } = useQuery({
    queryKey: ['validation-report', dateRange],
    queryFn: () =>
      documentReportApi.getValidationReport(
        dateRange.fromDate || undefined,
        dateRange.toDate || undefined
      ),
  });

  const handleExportAudit = async () => {
    if (!dateRange.fromDate || !dateRange.toDate) {
      toast.error('Please select date range for export');
      return;
    }
    try {
      const blob = await documentReportApi.exportAuditLog(
        dateRange.fromDate,
        dateRange.toDate,
        'csv'
      );
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `audit-log-${dateRange.fromDate}-to-${dateRange.toDate}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      toast.success('Audit log exported successfully');
    } catch (error) {
      toast.error('Failed to export audit log');
    }
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Documents', href: '/dashboard/documents' },
          { label: 'Reports & Analytics' },
        ]}
      />

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <BarChart3 className="w-7 h-7 text-blue-600" />
            Reports & Analytics
          </h1>
          <p className="text-gray-600 mt-1">Document statistics and insights</p>
        </div>
        <Button
          variant="outline"
          onClick={handleExportAudit}
          disabled={!dateRange.fromDate || !dateRange.toDate}
          className="flex items-center gap-2"
        >
          <Download className="w-4 h-4" />
          Export Audit Log
        </Button>
      </div>

      {/* Date Range Filter */}
      <Card>
        <CardContent className="p-4">
          <div className="flex items-center gap-4">
            <Calendar className="w-5 h-5 text-gray-400" />
            <input
              type="date"
              value={dateRange.fromDate}
              onChange={(e) => setDateRange({ ...dateRange, fromDate: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg"
            />
            <span className="text-gray-500">to</span>
            <input
              type="date"
              value={dateRange.toDate}
              onChange={(e) => setDateRange({ ...dateRange, toDate: e.target.value })}
              className="px-3 py-2 border border-gray-300 rounded-lg"
            />
            <Button
              variant="outline"
              onClick={() => setDateRange({ fromDate: '', toDate: '' })}
              size="sm"
            >
              Clear
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Overview Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Documents</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {stats?.totalDocuments || 0}
                </p>
                <p className="text-xs text-green-600 mt-1 flex items-center gap-1">
                  <TrendingUp className="w-3 h-3" />
                  Active: {stats?.activeDocuments || 0}
                </p>
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
                <p className="text-sm text-gray-600">Validated</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {stats?.validatedDocuments || 0}
                </p>
                <p className="text-xs text-gray-500 mt-1">
                  {stats?.totalDocuments
                    ? ((stats.validatedDocuments / stats.totalDocuments) * 100).toFixed(1)
                    : 0}
                  % of total
                </p>
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
                <p className="text-sm text-gray-600">Pending</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {stats?.pendingDocuments || 0}
                </p>
                <p className="text-xs text-gray-500 mt-1">Awaiting validation</p>
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
                <p className="text-sm text-gray-600">Storage Used</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {stats?.totalStorageSizeGB?.toFixed(2) || '0.00'} GB
                </p>
                <p className="text-xs text-gray-500 mt-1">
                  Avg: {stats?.averageFileSizeMB?.toFixed(2) || '0.00'} MB
                </p>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <BarChart3 className="w-6 h-6 text-purple-600" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Status Breakdown */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Documents by Status</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {stats?.documentsByStatus &&
                Object.entries(stats.documentsByStatus).map(([status, count]) => (
                  <div key={status} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                    <span className="font-medium text-gray-700">{status}</span>
                    <span className="text-lg font-bold text-gray-900">{count as number}</span>
                  </div>
                ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Documents by Type</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {stats?.documentsByType &&
                Object.entries(stats.documentsByType)
                  .slice(0, 5)
                  .map(([type, count]) => (
                    <div key={type} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                      <span className="font-medium text-gray-700">{type}</span>
                      <span className="text-lg font-bold text-gray-900">{count as number}</span>
                    </div>
                  ))}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Validation Report */}
      {validationReport && (
        <Card>
          <CardHeader>
            <CardTitle>Validation Report</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="p-4 bg-green-50 rounded-lg">
                <p className="text-sm text-green-600 font-medium">Total Validations</p>
                <p className="text-2xl font-bold text-green-900 mt-1">
                  {validationReport.totalValidations || 0}
                </p>
              </div>
              <div className="p-4 bg-blue-50 rounded-lg">
                <p className="text-sm text-blue-600 font-medium">Approved</p>
                <p className="text-2xl font-bold text-blue-900 mt-1">
                  {validationReport.approved || 0}
                </p>
              </div>
              <div className="p-4 bg-red-50 rounded-lg">
                <p className="text-sm text-red-600 font-medium">Rejected</p>
                <p className="text-2xl font-bold text-red-900 mt-1">
                  {validationReport.rejected || 0}
                </p>
              </div>
            </div>
            {validationReport.avgValidationTimeHours && (
              <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                <p className="text-sm text-gray-600">Average Validation Time</p>
                <p className="text-lg font-bold text-gray-900 mt-1">
                  {validationReport.avgValidationTimeHours.toFixed(1)} hours
                </p>
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* Additional Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-red-100 rounded-lg flex items-center justify-center">
                <XCircle className="w-5 h-5 text-red-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Rejected</p>
                <p className="text-xl font-bold text-gray-900">{stats?.rejectedDocuments || 0}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
                <AlertTriangle className="w-5 h-5 text-orange-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Expiring Soon</p>
                <p className="text-xl font-bold text-gray-900">{stats?.expiringDocuments || 0}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-gray-100 rounded-lg flex items-center justify-center">
                <FileText className="w-5 h-5 text-gray-600" />
              </div>
              <div>
                <p className="text-sm text-gray-600">Archived</p>
                <p className="text-xl font-bold text-gray-900">{stats?.archivedDocuments || 0}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
