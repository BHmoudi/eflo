'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import Button from '@/components/ui/Button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { businessUnitApi, BusinessUnit } from '@/lib/api/business-units';
import { Search, Building2, Plus, Edit, Trash2, UserPlus, XCircle, CheckCircle, ChevronLeft, ChevronRight } from 'lucide-react';
import toast from 'react-hot-toast';

const BUSINESS_UNIT_TYPES = [
  { value: '', label: 'All Types' },
  { value: 'HEADQUARTERS', label: 'Headquarters' },
  { value: 'REGIONAL_OFFICE', label: 'Regional Office' },
  { value: 'BRANCH', label: 'Branch' },
  { value: 'WAREHOUSE', label: 'Warehouse' },
  { value: 'STORE', label: 'Store' },
  { value: 'FACTORY', label: 'Factory' },
  { value: 'SERVICE_CENTER', label: 'Service Center' },
];

export default function BusinessUnitsPage() {
  const router = useRouter();
  const { t } = useTranslation();
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('');
  const [regionFilter, setRegionFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);

  const queryClient = useQueryClient();

  const { data: paginatedData, isLoading } = useQuery({
    queryKey: ['business-units', page, pageSize],
    queryFn: () => businessUnitApi.getBusinessUnits(page, pageSize),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => businessUnitApi.deleteBusinessUnit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      toast.success('Business unit deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete business unit');
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: number) => businessUnitApi.deactivateBusinessUnit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      toast.success('Business unit deactivated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to deactivate business unit');
    },
  });

  const reactivateMutation = useMutation({
    mutationFn: (id: number) => businessUnitApi.reactivateBusinessUnit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      toast.success('Business unit reactivated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to reactivate business unit');
    },
  });

  const handleDeleteBusinessUnit = (id: number) => {
    if (confirm('Are you sure you want to delete this business unit? This action cannot be undone.')) {
      deleteMutation.mutate(id);
    }
  };

  const handleDeactivateBusinessUnit = (id: number) => {
    if (confirm('Are you sure you want to deactivate this business unit?')) {
      deactivateMutation.mutate(id);
    }
  };

  const handleReactivateBusinessUnit = (id: number) => {
    reactivateMutation.mutate(id);
  };

  const handleEditBusinessUnit = (id: number) => {
    router.push(`/dashboard/business-units/${id}/edit`);
  };

  const handleAssignManager = (id: number) => {
    router.push(`/dashboard/business-units/${id}/edit?tab=manager`);
  };

  const handleCreateBusinessUnit = () => {
    router.push('/dashboard/business-units/create');
  };

  const businessUnits = paginatedData?.content || [];
  const totalPages = paginatedData?.totalPages || 0;
  const totalElements = paginatedData?.totalElements || 0;

  // Extract unique regions for filter
  const uniqueRegions = Array.from(
    new Set(businessUnits.map((bu) => bu.regionCode).filter(Boolean))
  );

  const filteredBusinessUnits = businessUnits.filter((bu) => {
    const matchesSearch =
      bu.code?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      bu.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      bu.rrfCode?.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = !typeFilter || bu.type === typeFilter;
    const matchesRegion = !regionFilter || bu.regionCode === regionFilter;
    const matchesStatus =
      statusFilter === 'all' ||
      (statusFilter === 'active' && bu.active) ||
      (statusFilter === 'inactive' && !bu.active);
    return matchesSearch && matchesType && matchesRegion && matchesStatus;
  });

  const formatType = (type: string) => {
    return type
      .split('_')
      .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb items={[{ label: t('nav.dashboard'), href: '/dashboard' }, { label: t('nav.businessUnits') }]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{t('businessUnits.businessUnits')}</h1>
          <p className="text-gray-600 mt-1">{t('businessUnits.businessUnits')}</p>
        </div>
        <Button
          variant="primary"
          onClick={handleCreateBusinessUnit}
          className="flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          {t('businessUnits.createBusinessUnit')}
        </Button>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between flex-wrap gap-4">
            <CardTitle>All Business Units ({totalElements})</CardTitle>
            <div className="flex items-center gap-3 flex-wrap">
              <div className="relative w-64">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <Input
                  placeholder="Search by code, name, or RRF..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
              <Select
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value)}
                className="w-44"
              >
                {BUSINESS_UNIT_TYPES.map((type) => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </Select>
              <Select
                value={regionFilter}
                onChange={(e) => setRegionFilter(e.target.value)}
                className="w-40"
              >
                <option value="">All Regions</option>
                {uniqueRegions.map((region) => (
                  <option key={region} value={region}>
                    {region}
                  </option>
                ))}
              </Select>
              <Select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="w-32"
              >
                <option value="all">All Status</option>
                <option value="active">Active</option>
                <option value="inactive">Inactive</option>
              </Select>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-12 text-gray-500">Loading business units...</div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Code</TableHead>
                      <TableHead>Name</TableHead>
                      <TableHead>Type</TableHead>
                      <TableHead>Region</TableHead>
                      <TableHead>RRF Code</TableHead>
                      <TableHead>Manager</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredBusinessUnits.length === 0 ? (
                      <TableRow>
                        <TableCell colSpan={8} className="text-center py-12">
                          <Building2 className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                          <p className="text-gray-500">No business units found</p>
                        </TableCell>
                      </TableRow>
                    ) : (
                      filteredBusinessUnits.map((bu) => (
                        <TableRow key={bu.id}>
                          <TableCell>
                            <span className="font-mono text-sm font-medium text-gray-900">{bu.code}</span>
                          </TableCell>
                          <TableCell>
                            <span className="font-medium text-gray-900">{bu.name}</span>
                          </TableCell>
                          <TableCell>
                            <span className="text-xs px-2 py-1 rounded bg-purple-100 text-purple-800">
                              {formatType(bu.type)}
                            </span>
                          </TableCell>
                          <TableCell>
                            {bu.regionCode && (
                              <div className="flex flex-col">
                                <span className="text-sm text-gray-900">{bu.regionCode}</span>
                                {bu.regionName && (
                                  <span className="text-xs text-gray-500">{bu.regionName}</span>
                                )}
                              </div>
                            )}
                          </TableCell>
                          <TableCell>
                            <span className="font-mono text-sm text-gray-600">{bu.rrfCode || '-'}</span>
                          </TableCell>
                          <TableCell>
                            {bu.managerName ? (
                              <span className="text-sm text-gray-900">{bu.managerName}</span>
                            ) : (
                              <span className="text-xs text-gray-400 italic">No manager</span>
                            )}
                          </TableCell>
                          <TableCell>
                            <span
                              className={`text-xs px-2 py-1 rounded ${
                                bu.active
                                  ? 'bg-green-100 text-green-800'
                                  : 'bg-gray-100 text-gray-800'
                              }`}
                            >
                              {bu.active ? 'Active' : 'Inactive'}
                            </span>
                          </TableCell>
                          <TableCell>
                            <div className="flex items-center justify-end gap-2">
                              <button
                                onClick={() => handleEditBusinessUnit(bu.id)}
                                className="p-1 hover:bg-gray-100 rounded transition-colors"
                                title="Edit Business Unit"
                              >
                                <Edit className="w-4 h-4 text-blue-600" />
                              </button>
                              <button
                                onClick={() => handleAssignManager(bu.id)}
                                className="p-1 hover:bg-blue-50 rounded transition-colors"
                                title="Assign Manager"
                              >
                                <UserPlus className="w-4 h-4 text-blue-600" />
                              </button>
                              {bu.active ? (
                                <button
                                  onClick={() => handleDeactivateBusinessUnit(bu.id)}
                                  className="p-1 hover:bg-orange-50 rounded transition-colors"
                                  title="Deactivate Business Unit"
                                >
                                  <XCircle className="w-4 h-4 text-orange-600" />
                                </button>
                              ) : (
                                <button
                                  onClick={() => handleReactivateBusinessUnit(bu.id)}
                                  className="p-1 hover:bg-green-50 rounded transition-colors"
                                  title="Reactivate Business Unit"
                                >
                                  <CheckCircle className="w-4 h-4 text-green-600" />
                                </button>
                              )}
                              <button
                                onClick={() => handleDeleteBusinessUnit(bu.id)}
                                className="p-1 hover:bg-red-50 rounded transition-colors"
                                title="Delete Business Unit"
                              >
                                <Trash2 className="w-4 h-4 text-red-600" />
                              </button>
                            </div>
                          </TableCell>
                        </TableRow>
                      ))
                    )}
                  </TableBody>
                </Table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
                  <p className="text-sm text-gray-600">
                    Page {page + 1} of {totalPages} ({totalElements} total business units)
                  </p>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage((p) => Math.max(0, p - 1))}
                      disabled={page === 0}
                      className="flex items-center gap-1"
                    >
                      <ChevronLeft className="w-4 h-4" />
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                      disabled={page >= totalPages - 1}
                      className="flex items-center gap-1"
                    >
                      Next
                      <ChevronRight className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
