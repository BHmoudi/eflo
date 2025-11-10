'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useParams, useRouter } from 'next/navigation';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { documentsApi, documentValidationApi } from '@/lib/api/documents';
import {
  FileText,
  Download,
  Eye,
  Archive,
  Trash2,
  CheckCircle,
  XCircle,
  Clock,
  AlertTriangle,
  ArrowLeft,
  Upload,
  History,
  Shield,
} from 'lucide-react';
import toast from 'react-hot-toast';
import { formatDateTime } from '@/lib/utils';

export default function DocumentDetailPage() {
  const params = useParams();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const documentId = parseInt(params.id as string);

  const [showValidateModal, setShowValidateModal] = useState(false);
  const [validationComments, setValidationComments] = useState('');
  const [showPreview, setShowPreview] = useState(false);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  // Fetch document details
  const { data: document, isLoading } = useQuery({
    queryKey: ['document', documentId],
    queryFn: () => documentsApi.getDocument(documentId),
  });

  // Fetch document versions
  const { data: versions = [] } = useQuery({
    queryKey: ['document-versions', documentId],
    queryFn: () => documentsApi.getVersions(documentId),
    enabled: !!document,
  });

  // Download mutation
  const handleDownload = async () => {
    try {
      const blob = await documentsApi.downloadDocument(documentId);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = document?.originalFilename || 'document';
      a.click();
      URL.revokeObjectURL(url);
      toast.success('Document downloaded successfully');
    } catch (error) {
      toast.error('Failed to download document');
    }
  };

  // Preview mutation
  const handlePreview = async () => {
    try {
      const blob = await documentsApi.getPreview(documentId);
      const url = URL.createObjectURL(blob);
      setPreviewUrl(url);
      setShowPreview(true);
    } catch (error) {
      toast.error('Preview not available for this file type');
    }
  };

  // Validation mutation
  const validateMutation = useMutation({
    mutationFn: (isApproved: boolean) =>
      documentValidationApi.validateDocument(documentId, {
        isApproved,
        comments: validationComments,
        validatedBy: 'current-user',
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document', documentId] });
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      toast.success('Document validated successfully');
      setShowValidateModal(false);
      setValidationComments('');
    },
    onError: () => {
      toast.error('Failed to validate document');
    },
  });

  // Archive mutation
  const archiveMutation = useMutation({
    mutationFn: () => documentsApi.archiveDocument(documentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document', documentId] });
      toast.success('Document archived successfully');
    },
    onError: () => {
      toast.error('Failed to archive document');
    },
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: () => documentsApi.deleteDocument(documentId),
    onSuccess: () => {
      toast.success('Document deleted successfully');
      router.push('/dashboard/documents');
    },
    onError: () => {
      toast.error('Failed to delete document');
    },
  });

  // Rescan mutation
  const rescanMutation = useMutation({
    mutationFn: () => documentValidationApi.rescanForVirus(documentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document', documentId] });
      toast.success('Virus scan initiated');
    },
    onError: () => {
      toast.error('Failed to initiate virus scan');
    },
  });

  const getStatusBadge = (status: string) => {
    const config: any = {
      VALIDATED: { color: 'green', icon: CheckCircle, label: 'Validated' },
      PENDING: { color: 'yellow', icon: Clock, label: 'Pending' },
      REJECTED: { color: 'red', icon: XCircle, label: 'Rejected' },
      EXPIRED: { color: 'gray', icon: AlertTriangle, label: 'Expired' },
    };
    const statusConfig = config[status] || config.PENDING;
    const Icon = statusConfig.icon;
    return (
      <Badge variant={statusConfig.color as any} className="flex items-center gap-1">
        <Icon className="w-3 h-3" />
        {statusConfig.label}
      </Badge>
    );
  };

  const getScanStatusBadge = (scanStatus?: string) => {
    if (!scanStatus) return null;
    const config: any = {
      CLEAN: { color: 'green', label: 'Clean' },
      PENDING: { color: 'yellow', label: 'Scanning...' },
      INFECTED: { color: 'red', label: 'Infected' },
      ERROR: { color: 'gray', label: 'Error' },
    };
    const statusConfig = config[scanStatus] || config.PENDING;
    return (
      <Badge variant={statusConfig.color as any} className="flex items-center gap-1">
        <Shield className="w-3 h-3" />
        {statusConfig.label}
      </Badge>
    );
  };

  if (isLoading) {
    return <div className="p-6">Loading document details...</div>;
  }

  if (!document) {
    return <div className="p-6">Document not found</div>;
  }

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Documents', href: '/dashboard/documents' },
          { label: document.originalFilename },
        ]}
      />

      {/* Header */}
      <div className="flex items-start justify-between">
        <div className="flex items-start gap-4">
          <button
            onClick={() => router.back()}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-5 h-5 text-gray-600" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-3">
              <FileText className="w-7 h-7 text-blue-600" />
              {document.originalFilename}
            </h1>
            <div className="flex items-center gap-3 mt-2">
              {getStatusBadge(document.validationStatus)}
              {document.virusScanStatus && getScanStatusBadge(document.virusScanStatus)}
              {document.isArchived && <Badge variant="gray">Archived</Badge>}
              {document.isConfidential && (
                <Badge variant="purple" className="flex items-center gap-1">
                  <Shield className="w-3 h-3" />
                  Confidential
                </Badge>
              )}
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={handlePreview} className="flex items-center gap-2">
            <Eye className="w-4 h-4" />
            Preview
          </Button>
          <Button variant="outline" onClick={handleDownload} className="flex items-center gap-2">
            <Download className="w-4 h-4" />
            Download
          </Button>
          {document.validationStatus === 'PENDING' && (
            <Button
              variant="primary"
              onClick={() => setShowValidateModal(true)}
              className="flex items-center gap-2"
            >
              <CheckCircle className="w-4 h-4" />
              Validate
            </Button>
          )}
          <Button
            variant="outline"
            onClick={() => archiveMutation.mutate()}
            disabled={document.isArchived}
            className="flex items-center gap-2"
          >
            <Archive className="w-4 h-4" />
            Archive
          </Button>
          <Button
            variant="danger"
            onClick={() => {
              if (confirm('Are you sure you want to delete this document?')) {
                deleteMutation.mutate();
              }
            }}
            className="flex items-center gap-2"
          >
            <Trash2 className="w-4 h-4" />
            Delete
          </Button>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Details */}
        <div className="lg:col-span-2 space-y-6">
          {/* Document Information */}
          <Card>
            <CardHeader>
              <CardTitle>Document Information</CardTitle>
            </CardHeader>
            <CardContent>
              <dl className="grid grid-cols-2 gap-4">
                <div>
                  <dt className="text-sm font-medium text-gray-500">Document UUID</dt>
                  <dd className="mt-1 text-sm text-gray-900 font-mono">{document.documentUuid}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Document Type</dt>
                  <dd className="mt-1 text-sm text-gray-900">{document.typeName}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Order Number</dt>
                  <dd className="mt-1 text-sm text-gray-900">{document.orderNumber}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">File Size</dt>
                  <dd className="mt-1 text-sm text-gray-900">{document.fileSizeMB.toFixed(2)} MB</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">File Type</dt>
                  <dd className="mt-1 text-sm text-gray-900">{document.mimeType}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Version</dt>
                  <dd className="mt-1 text-sm text-gray-900">v{document.version}</dd>
                </div>
              </dl>

              {document.description && (
                <div className="mt-4 pt-4 border-t">
                  <dt className="text-sm font-medium text-gray-500">Description</dt>
                  <dd className="mt-1 text-sm text-gray-900">{document.description}</dd>
                </div>
              )}

              {document.tags && document.tags.length > 0 && (
                <div className="mt-4 pt-4 border-t">
                  <dt className="text-sm font-medium text-gray-500 mb-2">Tags</dt>
                  <div className="flex flex-wrap gap-2">
                    {document.tags.map((tag, idx) => (
                      <Badge key={idx} variant="blue">
                        {tag}
                      </Badge>
                    ))}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Upload & Validation Details */}
          <Card>
            <CardHeader>
              <CardTitle>Upload & Validation Details</CardTitle>
            </CardHeader>
            <CardContent>
              <dl className="grid grid-cols-2 gap-4">
                <div>
                  <dt className="text-sm font-medium text-gray-500">Uploaded By</dt>
                  <dd className="mt-1 text-sm text-gray-900">{document.uploadedBy}</dd>
                </div>
                <div>
                  <dt className="text-sm font-medium text-gray-500">Uploaded At</dt>
                  <dd className="mt-1 text-sm text-gray-900">{formatDateTime(document.uploadedAt)}</dd>
                </div>
                {document.validatedBy && (
                  <>
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Validated By</dt>
                      <dd className="mt-1 text-sm text-gray-900">{document.validatedBy}</dd>
                    </div>
                    <div>
                      <dt className="text-sm font-medium text-gray-500">Validated At</dt>
                      <dd className="mt-1 text-sm text-gray-900">
                        {document.validatedAt ? formatDateTime(document.validatedAt) : '-'}
                      </dd>
                    </div>
                  </>
                )}
                {document.expirationDate && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Expiration Date</dt>
                    <dd className="mt-1 text-sm text-gray-900">{formatDateTime(document.expirationDate)}</dd>
                  </div>
                )}
              </dl>
            </CardContent>
          </Card>

          {/* Version History */}
          {versions.length > 1 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <History className="w-5 h-5" />
                  Version History
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {versions.map((version) => (
                    <div
                      key={version.id}
                      className="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                    >
                      <div>
                        <p className="font-medium text-sm">v{version.version}</p>
                        <p className="text-xs text-gray-500">
                          {formatDateTime(version.uploadedAt)} by {version.uploadedBy}
                        </p>
                      </div>
                      <div className="flex items-center gap-2">
                        {getStatusBadge(version.validationStatus)}
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => {
                            window.open(`/dashboard/documents/${version.id}`, '_blank');
                          }}
                        >
                          View
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Right Column - Actions & Security */}
        <div className="space-y-6">
          {/* Security Scan */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="w-5 h-5" />
                Security Scan
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              {getScanStatusBadge(document.virusScanStatus)}
              {document.virusScanDate && (
                <p className="text-xs text-gray-500">
                  Last scanned: {formatDateTime(document.virusScanDate)}
                </p>
              )}
              <Button
                variant="outline"
                size="sm"
                onClick={() => rescanMutation.mutate()}
                disabled={rescanMutation.isPending}
                className="w-full"
              >
                {rescanMutation.isPending ? 'Scanning...' : 'Rescan for Viruses'}
              </Button>
            </CardContent>
          </Card>

          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <Button variant="outline" size="sm" className="w-full justify-start gap-2">
                <Upload className="w-4 h-4" />
                Replace Document
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="w-full justify-start gap-2"
                onClick={() => router.push(`/dashboard/orders/${document.orderId}`)}
              >
                <FileText className="w-4 h-4" />
                View Order
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Validation Modal */}
      {showValidateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold mb-4">Validate Document</h3>
            <textarea
              className="w-full border rounded-lg p-3 mb-4"
              rows={4}
              placeholder="Add validation comments (optional)..."
              value={validationComments}
              onChange={(e) => setValidationComments(e.target.value)}
            />
            <div className="flex items-center justify-end gap-3">
              <Button variant="outline" onClick={() => setShowValidateModal(false)}>
                Cancel
              </Button>
              <Button
                variant="danger"
                onClick={() => validateMutation.mutate(false)}
                disabled={validateMutation.isPending}
              >
                Reject
              </Button>
              <Button
                variant="primary"
                onClick={() => validateMutation.mutate(true)}
                disabled={validateMutation.isPending}
              >
                Approve
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Preview Modal */}
      {showPreview && previewUrl && (
        <div
          className="fixed inset-0 bg-black bg-opacity-90 flex items-center justify-center z-50"
          onClick={() => {
            setShowPreview(false);
            URL.revokeObjectURL(previewUrl);
            setPreviewUrl(null);
          }}
        >
          <div className="relative max-w-6xl max-h-[90vh] w-full mx-4">
            <button
              className="absolute top-4 right-4 bg-white rounded-full p-2 shadow-lg"
              onClick={() => {
                setShowPreview(false);
                URL.revokeObjectURL(previewUrl);
                setPreviewUrl(null);
              }}
            >
              <XCircle className="w-6 h-6" />
            </button>
            <iframe src={previewUrl} className="w-full h-[90vh] bg-white rounded-lg" />
          </div>
        </div>
      )}
    </div>
  );
}
