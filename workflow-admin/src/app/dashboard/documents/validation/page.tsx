'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Badge from '@/components/ui/Badge';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { documentsApi, documentValidationApi, DocumentSummary } from '@/lib/api/documents';
import { CheckCircle, XCircle, Eye, Download, Clock, FileText } from 'lucide-react';
import toast from 'react-hot-toast';
import { formatDateTime } from '@/lib/utils';

export default function DocumentValidationPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [selectedDocs, setSelectedDocs] = useState<Set<number>>(new Set());
  const [showValidateModal, setShowValidateModal] = useState(false);
  const [currentDocId, setCurrentDocId] = useState<number | null>(null);
  const [validationComments, setValidationComments] = useState('');

  // Fetch pending documents
  const { data: pendingData, isLoading } = useQuery({
    queryKey: ['pending-validation'],
    queryFn: () => documentsApi.getPendingValidation(0, 100),
  });

  const documents = pendingData?.content || [];

  // Validate single document
  const validateMutation = useMutation({
    mutationFn: ({ id, isApproved }: { id: number; isApproved: boolean }) =>
      documentValidationApi.validateDocument(id, {
        isApproved,
        comments: validationComments,
        validatedBy: 'current-user',
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pending-validation'] });
      queryClient.invalidateQueries({ queryKey: ['document-statistics'] });
      toast.success('Document validated successfully');
      setShowValidateModal(false);
      setCurrentDocId(null);
      setValidationComments('');
    },
    onError: () => {
      toast.error('Failed to validate document');
    },
  });

  // Batch validate documents
  const batchValidateMutation = useMutation({
    mutationFn: (comments: string) =>
      documentValidationApi.validateBatch(Array.from(selectedDocs), comments),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pending-validation'] });
      queryClient.invalidateQueries({ queryKey: ['document-statistics'] });
      toast.success(`${selectedDocs.size} documents validated successfully`);
      setSelectedDocs(new Set());
    },
    onError: () => {
      toast.error('Failed to batch validate documents');
    },
  });

  const handleSelectDoc = (id: number) => {
    const newSelected = new Set(selectedDocs);
    if (newSelected.has(id)) {
      newSelected.delete(id);
    } else {
      newSelected.add(id);
    }
    setSelectedDocs(newSelected);
  };

  const handleSelectAll = () => {
    if (selectedDocs.size === documents.length) {
      setSelectedDocs(new Set());
    } else {
      setSelectedDocs(new Set(documents.map((doc) => doc.id)));
    }
  };

  const handleValidate = (id: number) => {
    setCurrentDocId(id);
    setShowValidateModal(true);
  };

  const handleBatchValidate = () => {
    if (selectedDocs.size === 0) {
      toast.error('Please select documents to validate');
      return;
    }
    if (confirm(`Approve ${selectedDocs.size} documents?`)) {
      batchValidateMutation.mutate('Batch approved');
    }
  };

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

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Documents', href: '/dashboard/documents' },
          { label: 'Pending Validation' },
        ]}
      />

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Clock className="w-7 h-7 text-yellow-600" />
            Pending Validation
          </h1>
          <p className="text-gray-600 mt-1">Review and validate pending documents</p>
        </div>
        {selectedDocs.size > 0 && (
          <Button
            variant="primary"
            onClick={handleBatchValidate}
            disabled={batchValidateMutation.isPending}
            className="flex items-center gap-2"
          >
            <CheckCircle className="w-4 h-4" />
            Approve Selected ({selectedDocs.size})
          </Button>
        )}
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total Pending</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{documents.length}</p>
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
                <p className="text-sm text-gray-600">Selected</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">{selectedDocs.size}</p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Remaining</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {documents.length - selectedDocs.size}
                </p>
              </div>
              <div className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center">
                <FileText className="w-6 h-6 text-gray-600" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Documents List */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Pending Documents</CardTitle>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={selectedDocs.size === documents.length && documents.length > 0}
                onChange={handleSelectAll}
                className="w-4 h-4 rounded border-gray-300"
              />
              <span className="text-sm text-gray-600">Select All</span>
            </label>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-12">Loading pending documents...</div>
          ) : documents.length === 0 ? (
            <div className="text-center py-12">
              <CheckCircle className="w-16 h-16 text-green-400 mx-auto mb-4" />
              <p className="text-gray-500 text-lg">All documents validated!</p>
              <p className="text-gray-400 text-sm mt-2">No documents pending validation</p>
            </div>
          ) : (
            <div className="space-y-3">
              {documents.map((doc: DocumentSummary) => (
                <div
                  key={doc.id}
                  className={`flex items-center gap-4 p-4 border rounded-lg transition-all ${
                    selectedDocs.has(doc.id)
                      ? 'border-blue-500 bg-blue-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                >
                  {/* Checkbox */}
                  <input
                    type="checkbox"
                    checked={selectedDocs.has(doc.id)}
                    onChange={() => handleSelectDoc(doc.id)}
                    className="w-5 h-5 rounded border-gray-300"
                  />

                  {/* Document Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-3">
                      <FileText className="w-5 h-5 text-gray-400 flex-shrink-0" />
                      <div className="flex-1 min-w-0">
                        <h3 className="font-medium text-gray-900 truncate">{doc.originalFilename}</h3>
                        <div className="flex items-center gap-3 mt-1 text-sm text-gray-500">
                          <span>{doc.typeName}</span>
                          <span>•</span>
                          <span>{doc.orderNumber}</span>
                          <span>•</span>
                          <span>{doc.fileSizeMB.toFixed(2)} MB</span>
                          <span>•</span>
                          <span>{formatDateTime(doc.uploadedAt)}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center gap-2 flex-shrink-0">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => router.push(`/dashboard/documents/${doc.id}`)}
                      className="flex items-center gap-1"
                    >
                      <Eye className="w-4 h-4" />
                      View
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDownload(doc.id, doc.originalFilename)}
                      className="flex items-center gap-1"
                    >
                      <Download className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => handleValidate(doc.id)}
                      className="flex items-center gap-1"
                    >
                      <CheckCircle className="w-4 h-4" />
                      Validate
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Validation Modal */}
      {showValidateModal && currentDocId && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold mb-4">Validate Document</h3>
            <p className="text-sm text-gray-600 mb-4">
              Add comments to explain your validation decision (optional)
            </p>
            <textarea
              className="w-full border rounded-lg p-3 mb-4"
              rows={4}
              placeholder="Validation comments..."
              value={validationComments}
              onChange={(e) => setValidationComments(e.target.value)}
            />
            <div className="flex items-center justify-end gap-3">
              <Button
                variant="outline"
                onClick={() => {
                  setShowValidateModal(false);
                  setCurrentDocId(null);
                  setValidationComments('');
                }}
              >
                Cancel
              </Button>
              <Button
                variant="danger"
                onClick={() => validateMutation.mutate({ id: currentDocId, isApproved: false })}
                disabled={validateMutation.isPending}
                className="flex items-center gap-2"
              >
                <XCircle className="w-4 h-4" />
                Reject
              </Button>
              <Button
                variant="primary"
                onClick={() => validateMutation.mutate({ id: currentDocId, isApproved: true })}
                disabled={validateMutation.isPending}
                className="flex items-center gap-2"
              >
                <CheckCircle className="w-4 h-4" />
                Approve
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
