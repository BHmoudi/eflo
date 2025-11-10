'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useMutation, useQueryClient, useQuery } from '@tanstack/react-query';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Breadcrumb from '@/components/layout/Breadcrumb';
import FileUpload from '@/components/ui/FileUpload';
import { documentsApi, documentTypeApi } from '@/lib/api/documents';
import { ArrowLeft, Upload, FileText, Check } from 'lucide-react';
import toast from 'react-hot-toast';

export default function UploadDocumentPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const [file, setFile] = useState<File | null>(null);
  const [formData, setFormData] = useState({
    documentTypeId: 0,
    orderId: 0,
    orderNumber: '',
    description: '',
    tags: '',
    businessUnitId: undefined as number | undefined,
  });

  // Fetch document types from API
  const { data: documentTypes = [], isLoading: loadingTypes } = useQuery({
    queryKey: ['document-types'],
    queryFn: () => documentTypeApi.listAllTypes(false),
  });

  const uploadMutation = useMutation({
    mutationFn: (data: any) => documentsApi.uploadDocument(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents'] });
      queryClient.invalidateQueries({ queryKey: ['document-statistics'] });
      toast.success('Document uploaded successfully!');
      router.push('/dashboard/documents');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to upload document');
    },
  });

  const handleFileSelect = (selectedFile: File) => {
    setFile(selectedFile);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!file) {
      toast.error('Please select a file to upload');
      return;
    }

    if (!formData.orderNumber || formData.orderId === 0) {
      toast.error('Please enter order number and ID');
      return;
    }

    const tags = formData.tags.split(',').map(t => t.trim()).filter(Boolean);

    uploadMutation.mutate({
      file,
      documentTypeId: formData.documentTypeId,
      orderId: formData.orderId,
      orderNumber: formData.orderNumber,
      description: formData.description,
      tags,
      businessUnitId: formData.businessUnitId,
    });
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb items={[
        { label: 'Dashboard', href: '/dashboard' },
        { label: 'Documents', href: '/dashboard/documents' },
        { label: 'Upload' }
      ]} />

      <div className="flex items-center gap-3">
        <button
          onClick={() => router.back()}
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Upload Document</h1>
          <p className="text-gray-600 mt-1">Upload a new document to the system</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* File Upload */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <FileText className="w-5 h-5 text-blue-600" />
              Select File
            </CardTitle>
          </CardHeader>
          <CardContent>
            <FileUpload
              onFileSelect={handleFileSelect}
              accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png"
              maxSize={10 * 1024 * 1024} // 10MB
            />
            {file && (
              <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded-lg flex items-center gap-2">
                <Check className="w-5 h-5 text-green-600" />
                <span className="text-sm text-green-800">Selected: {file.name} ({(file.size / 1024 / 1024).toFixed(2)} MB)</span>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Document Information */}
        <Card>
          <CardHeader>
            <CardTitle>Document Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Order ID *"
                type="number"
                value={formData.orderId || ''}
                onChange={(e) => setFormData({ ...formData, orderId: parseInt(e.target.value) || 0 })}
                placeholder="1234"
                required
              />
              <Input
                label="Order Number *"
                value={formData.orderNumber}
                onChange={(e) => setFormData({ ...formData, orderNumber: e.target.value })}
                placeholder="ORD-2025-001"
                required
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Document Type *
                </label>
                <select
                  value={formData.documentTypeId}
                  onChange={(e) => setFormData({ ...formData, documentTypeId: parseInt(e.target.value) })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                  required
                  disabled={loadingTypes}
                >
                  <option value="0">Select document type...</option>
                  {documentTypes.map((type: any) => (
                    <option key={type.id} value={type.id}>
                      {type.typeName} ({type.typeCode})
                      {type.isMandatory && ' - Mandatory'}
                    </option>
                  ))}
                </select>
                {loadingTypes && <p className="text-xs text-gray-500 mt-1">Loading document types...</p>}
              </div>
              <Input
                label="Business Unit ID"
                type="number"
                value={formData.businessUnitId || ''}
                onChange={(e) => setFormData({ ...formData, businessUnitId: e.target.value ? parseInt(e.target.value) : undefined })}
                placeholder="Optional"
              />
            </div>

            <Input
              label="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Document description..."
            />

            <Input
              label="Tags (comma-separated)"
              value={formData.tags}
              onChange={(e) => setFormData({ ...formData, tags: e.target.value })}
              placeholder="invoice, contract, signed"
            />
          </CardContent>
        </Card>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={() => router.back()}
            disabled={uploadMutation.isPending}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={uploadMutation.isPending || !file}
            className="flex items-center gap-2"
          >
            <Upload className="w-4 h-4" />
            {uploadMutation.isPending ? 'Uploading...' : 'Upload Document'}
          </Button>
        </div>
      </form>
    </div>
  );
}
