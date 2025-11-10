'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import DataTable, { DataTableColumn } from '@/components/ui/DataTable';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { documentTypeApi } from '@/lib/api/documents';
import { Plus, Edit2, Trash2, Copy, FileType } from 'lucide-react';
import toast from 'react-hot-toast';

export default function DocumentTypesPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingType, setEditingType] = useState<any>(null);
  const [formData, setFormData] = useState({
    typeCode: '',
    typeName: '',
    description: '',
    category: 'ORDER',
    isMandatory: false,
    requiresValidation: true,
    maxFileSizeMB: 10,
    allowedExtensions: 'pdf,doc,docx,jpg,png',
  });

  const { data: documentTypes = [], isLoading } = useQuery({
    queryKey: ['document-types'],
    queryFn: () => documentTypeApi.listAllTypes(true),
  });

  const createMutation = useMutation({
    mutationFn: (data: any) => documentTypeApi.createType(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document-types'] });
      toast.success('Document type created successfully');
      setShowCreateModal(false);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create document type');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: any) => documentTypeApi.updateType(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document-types'] });
      toast.success('Document type updated successfully');
      setEditingType(null);
      resetForm();
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to update document type');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => documentTypeApi.deleteType(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document-types'] });
      toast.success('Document type deleted');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete document type');
    },
  });

  const duplicateMutation = useMutation({
    mutationFn: ({ id, code, name }: any) => documentTypeApi.duplicateType(id, code, name),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document-types'] });
      toast.success('Document type duplicated');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to duplicate document type');
    },
  });

  const resetForm = () => {
    setFormData({
      typeCode: '',
      typeName: '',
      description: '',
      category: 'ORDER',
      isMandatory: false,
      requiresValidation: true,
      maxFileSizeMB: 10,
      allowedExtensions: 'pdf,doc,docx,jpg,png',
    });
  };

  const handleCreate = () => {
    setEditingType(null);
    resetForm();
    setShowCreateModal(true);
  };

  const handleEdit = (type: any) => {
    setEditingType(type);
    setFormData({
      typeCode: type.typeCode,
      typeName: type.typeName,
      description: type.description || '',
      category: type.category,
      isMandatory: type.isMandatory,
      requiresValidation: type.requiresValidation,
      maxFileSizeMB: type.maxFileSizeMB || 10,
      allowedExtensions: type.allowedExtensions?.join(',') || '',
    });
    setShowCreateModal(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const data = {
      ...formData,
      allowedExtensions: formData.allowedExtensions.split(',').map(e => e.trim()).filter(Boolean),
    };

    if (editingType) {
      updateMutation.mutate({ id: editingType.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this document type?')) {
      deleteMutation.mutate(id);
    }
  };

  const handleDuplicate = (type: any) => {
    const newCode = prompt('Enter code for duplicated type:', `${type.typeCode}_COPY`);
    const newName = prompt('Enter name for duplicated type:', `${type.typeName} (Copy)`);
    if (newCode && newName) {
      duplicateMutation.mutate({ id: type.id, code: newCode, name: newName });
    }
  };

  const columns: DataTableColumn[] = [
    { key: 'typeCode', label: 'Code', sortable: true },
    { key: 'typeName', label: 'Name', sortable: true },
    { key: 'category', label: 'Category', sortable: true },
    {
      key: 'isMandatory',
      label: 'Mandatory',
      render: (value: boolean) => (
        <span className={`text-xs px-2 py-1 rounded ${value ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-800'}`}>
          {value ? 'Yes' : 'No'}
        </span>
      ),
    },
    {
      key: 'isActive',
      label: 'Status',
      render: (value: boolean) => (
        <span className={`text-xs px-2 py-1 rounded ${value ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
          {value ? 'Active' : 'Inactive'}
        </span>
      ),
    },
    {
      key: 'id',
      label: 'Actions',
      render: (value: number, row: any) => (
        <div className="flex items-center gap-2">
          <button onClick={() => handleEdit(row)} className="p-1 hover:bg-gray-100 rounded" title="Edit">
            <Edit2 className="w-4 h-4 text-blue-600" />
          </button>
          <button onClick={() => handleDuplicate(row)} className="p-1 hover:bg-gray-100 rounded" title="Duplicate">
            <Copy className="w-4 h-4 text-purple-600" />
          </button>
          <button onClick={() => handleDelete(value)} className="p-1 hover:bg-red-50 rounded" title="Delete">
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
        { label: 'Document Types' }
      ]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Document Types</h1>
          <p className="text-gray-600 mt-1">Manage document type definitions</p>
        </div>
        <Button variant="primary" onClick={handleCreate} className="flex items-center gap-2">
          <Plus className="w-4 h-4" />
          Create Document Type
        </Button>
      </div>

      <Card>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-12">Loading...</div>
          ) : (
            <DataTable
              columns={columns}
              data={documentTypes}
              keyExtractor={(row: any) => row.id}
              searchable={true}
              searchPlaceholder="Search document types..."
              searchKeys={['typeCode', 'typeName', 'category']}
            />
          )}
        </CardContent>
      </Card>

      {/* Create/Edit Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl p-6 max-h-[90vh] overflow-y-auto">
            <h2 className="text-xl font-semibold mb-4">
              {editingType ? 'Edit Document Type' : 'Create Document Type'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <Input
                  label="Type Code *"
                  value={formData.typeCode}
                  onChange={(e) => setFormData({ ...formData, typeCode: e.target.value.toUpperCase() })}
                  placeholder="INVOICE"
                  required
                  disabled={!!editingType}
                />
                <Input
                  label="Type Name *"
                  value={formData.typeName}
                  onChange={(e) => setFormData({ ...formData, typeName: e.target.value })}
                  placeholder="Invoice"
                  required
                />
              </div>

              <Input
                label="Description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Document type description..."
              />

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">Category</label>
                  <select
                    value={formData.category}
                    onChange={(e) => setFormData({ ...formData, category: e.target.value })}
                    className="w-full px-3 py-2 border rounded-lg"
                  >
                    <option value="ORDER">Order</option>
                    <option value="LEGAL">Legal</option>
                    <option value="FINANCIAL">Financial</option>
                    <option value="TECHNICAL">Technical</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                <Input
                  label="Max File Size (MB)"
                  type="number"
                  value={formData.maxFileSizeMB}
                  onChange={(e) => setFormData({ ...formData, maxFileSizeMB: parseInt(e.target.value) })}
                />
              </div>

              <Input
                label="Allowed Extensions (comma-separated)"
                value={formData.allowedExtensions}
                onChange={(e) => setFormData({ ...formData, allowedExtensions: e.target.value })}
                placeholder="pdf,doc,docx,jpg,png"
              />

              <div className="flex items-center gap-4">
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={formData.isMandatory}
                    onChange={(e) => setFormData({ ...formData, isMandatory: e.target.checked })}
                    className="w-4 h-4"
                  />
                  <span className="text-sm">Mandatory</span>
                </label>
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={formData.requiresValidation}
                    onChange={(e) => setFormData({ ...formData, requiresValidation: e.target.checked })}
                    className="w-4 h-4"
                  />
                  <span className="text-sm">Requires Validation</span>
                </label>
              </div>

              <div className="flex justify-end gap-3 pt-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setShowCreateModal(false);
                    setEditingType(null);
                    resetForm();
                  }}
                >
                  Cancel
                </Button>
                <Button type="submit" variant="primary">
                  {editingType ? 'Update' : 'Create'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
