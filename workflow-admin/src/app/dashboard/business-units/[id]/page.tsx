'use client';

import React from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { businessUnitApi, BusinessUnit } from '@/lib/api/business-units';
import { userBusinessUnitApi, BusinessUnitUser } from '@/lib/api/user-business-units';
import { hierarchyApi, HierarchyWithDetails } from '@/lib/api/hierarchies';
import {
  ArrowLeft,
  Edit,
  XCircle,
  CheckCircle,
  Trash2,
  Building2,
  Phone,
  Mail,
  Globe,
  MapPin,
  FileText,
  DollarSign,
  User,
  Users,
  Calendar,
  Clock,
  Tag,
  Map,
} from 'lucide-react';
import toast from 'react-hot-toast';
import Link from 'next/link';

const formatType = (type: string) => {
  return type
    .split('_')
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};

const formatDate = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const formatDateTime = (dateString?: string) => {
  if (!dateString) return 'N/A';
  return new Date(dateString).toLocaleString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export default function BusinessUnitDetailPage() {
  const params = useParams();
  const router = useRouter();
  const queryClient = useQueryClient();
  const businessUnitId = Number(params.id);

  // Fetch business unit details
  const { data: businessUnit, isLoading: isLoadingBU, error: buError } = useQuery({
    queryKey: ['business-unit', businessUnitId],
    queryFn: () => businessUnitApi.getBusinessUnitById(businessUnitId),
    enabled: !!businessUnitId,
  });

  // Fetch assigned users
  const { data: assignedUsers, isLoading: isLoadingUsers } = useQuery({
    queryKey: ['business-unit-users', businessUnitId],
    queryFn: () => userBusinessUnitApi.getBusinessUnitUsers(businessUnitId),
    enabled: !!businessUnitId,
  });

  // Fetch hierarchy within BU
  const { data: hierarchies, isLoading: isLoadingHierarchies } = useQuery({
    queryKey: ['business-unit-hierarchies', businessUnitId],
    queryFn: () => hierarchyApi.getBusinessUnitHierarchies(businessUnitId),
    enabled: !!businessUnitId,
  });

  // Mutations
  const deleteMutation = useMutation({
    mutationFn: (id: number) => businessUnitApi.deleteBusinessUnit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      toast.success('Business unit deleted successfully');
      router.push('/dashboard/business-units');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete business unit');
    },
  });

  const deactivateMutation = useMutation({
    mutationFn: (id: number) => businessUnitApi.deactivateBusinessUnit(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-unit', businessUnitId] });
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
      queryClient.invalidateQueries({ queryKey: ['business-unit', businessUnitId] });
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      toast.success('Business unit reactivated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to reactivate business unit');
    },
  });

  const handleEdit = () => {
    router.push(`/dashboard/business-units/${businessUnitId}/edit`);
  };

  const handleDelete = () => {
    if (
      confirm(
        'Are you sure you want to delete this business unit? This action cannot be undone.'
      )
    ) {
      deleteMutation.mutate(businessUnitId);
    }
  };

  const handleDeactivate = () => {
    if (confirm('Are you sure you want to deactivate this business unit?')) {
      deactivateMutation.mutate(businessUnitId);
    }
  };

  const handleReactivate = () => {
    reactivateMutation.mutate(businessUnitId);
  };

  const handleBack = () => {
    router.push('/dashboard/business-units');
  };

  const handleChangeManager = () => {
    router.push(`/dashboard/business-units/${businessUnitId}/edit?tab=manager`);
  };

  if (isLoadingBU) {
    return (
      <div className="p-6">
        <div className="text-center py-12 text-gray-500">Loading business unit details...</div>
      </div>
    );
  }

  if (buError || !businessUnit) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <p className="text-red-600 mb-4">Failed to load business unit details</p>
          <Button variant="outline" onClick={handleBack}>
            Back to Business Units
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Breadcrumbs */}
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Business Units', href: '/dashboard/business-units' },
          { label: businessUnit.name },
        ]}
      />

      {/* Header */}
      <div className="flex items-start justify-between">
        <div className="flex items-start gap-4">
          <button
            onClick={handleBack}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
            title="Back to Business Units"
          >
            <ArrowLeft className="w-5 h-5 text-gray-600" />
          </button>
          <div>
            <div className="flex items-center gap-3 mb-2">
              <h1 className="text-2xl font-bold text-gray-900">{businessUnit.name}</h1>
              <span className="text-xs px-2.5 py-1 rounded-full bg-purple-100 text-purple-800 font-medium">
                {formatType(businessUnit.type)}
              </span>
              <span
                className={`text-xs px-2.5 py-1 rounded-full font-medium ${
                  businessUnit.active
                    ? 'bg-green-100 text-green-800'
                    : 'bg-gray-100 text-gray-800'
                }`}
              >
                {businessUnit.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <p className="text-gray-600">
              <span className="font-mono font-medium">{businessUnit.code}</span>
              {businessUnit.rrfCode && (
                <>
                  {' '}
                  | RRF: <span className="font-mono">{businessUnit.rrfCode}</span>
                </>
              )}
            </p>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={handleEdit} className="flex items-center gap-1">
            <Edit className="w-4 h-4" />
            Edit
          </Button>
          {businessUnit.active ? (
            <Button
              variant="outline"
              size="sm"
              onClick={handleDeactivate}
              className="flex items-center gap-1 text-orange-600 hover:bg-orange-50 border-orange-300"
            >
              <XCircle className="w-4 h-4" />
              Deactivate
            </Button>
          ) : (
            <Button
              variant="outline"
              size="sm"
              onClick={handleReactivate}
              className="flex items-center gap-1 text-green-600 hover:bg-green-50 border-green-300"
            >
              <CheckCircle className="w-4 h-4" />
              Reactivate
            </Button>
          )}
          <Button
            variant="danger"
            size="sm"
            onClick={handleDelete}
            className="flex items-center gap-1"
          >
            <Trash2 className="w-4 h-4" />
            Delete
          </Button>
        </div>
      </div>

      {/* Two Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Basic Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Building2 className="w-5 h-5 text-blue-600" />
                Basic Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="text-sm font-medium text-gray-500">Code</label>
                  <p className="mt-1 text-gray-900 font-mono">{businessUnit.code}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Type</label>
                  <p className="mt-1 text-gray-900">{formatType(businessUnit.type)}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Name</label>
                  <p className="mt-1 text-gray-900">{businessUnit.name}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Legal Name</label>
                  <p className="mt-1 text-gray-900">{businessUnit.legalName || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Region Code</label>
                  <p className="mt-1 text-gray-900">{businessUnit.regionCode || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Region Name</label>
                  <p className="mt-1 text-gray-900">{businessUnit.regionName || 'N/A'}</p>
                </div>
                <div className="md:col-span-2">
                  <label className="text-sm font-medium text-gray-500">RRF Code</label>
                  <p className="mt-1 text-gray-900 font-mono">
                    {businessUnit.rrfCode || 'N/A'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Contact Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Phone className="w-5 h-5 text-purple-600" />
                Contact Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <Phone className="w-5 h-5 text-gray-400" />
                  <div>
                    <label className="text-sm font-medium text-gray-500">Phone</label>
                    <p className="text-gray-900">{businessUnit.phone || 'Not provided'}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <FileText className="w-5 h-5 text-gray-400" />
                  <div>
                    <label className="text-sm font-medium text-gray-500">Fax</label>
                    <p className="text-gray-900">{businessUnit.fax || 'Not provided'}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Mail className="w-5 h-5 text-gray-400" />
                  <div>
                    <label className="text-sm font-medium text-gray-500">Email</label>
                    {businessUnit.email ? (
                      <a
                        href={`mailto:${businessUnit.email}`}
                        className="text-blue-600 hover:underline"
                      >
                        {businessUnit.email}
                      </a>
                    ) : (
                      <p className="text-gray-900">Not provided</p>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Globe className="w-5 h-5 text-gray-400" />
                  <div>
                    <label className="text-sm font-medium text-gray-500">Website</label>
                    {businessUnit.website ? (
                      <a
                        href={businessUnit.website}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-blue-600 hover:underline"
                      >
                        {businessUnit.website}
                      </a>
                    ) : (
                      <p className="text-gray-900">Not provided</p>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Location Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="w-5 h-5 text-red-600" />
                Location Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <label className="text-sm font-medium text-gray-500">Address</label>
                  <div className="mt-1 text-gray-900">
                    {businessUnit.addressLine1 && <p>{businessUnit.addressLine1}</p>}
                    {businessUnit.addressLine2 && <p>{businessUnit.addressLine2}</p>}
                    {(businessUnit.city || businessUnit.postalCode) && (
                      <p>
                        {businessUnit.postalCode} {businessUnit.city}
                      </p>
                    )}
                    {businessUnit.country && <p>{businessUnit.country}</p>}
                    {!businessUnit.addressLine1 &&
                      !businessUnit.city &&
                      !businessUnit.country && (
                        <p className="text-gray-500 italic">No address provided</p>
                      )}
                  </div>
                </div>

                {(businessUnit.latitude || businessUnit.longitude) && (
                  <div>
                    <label className="text-sm font-medium text-gray-500">GPS Coordinates</label>
                    <div className="mt-1 flex items-center gap-4">
                      <div>
                        <span className="text-xs text-gray-500">Latitude:</span>
                        <p className="text-gray-900 font-mono">{businessUnit.latitude}</p>
                      </div>
                      <div>
                        <span className="text-xs text-gray-500">Longitude:</span>
                        <p className="text-gray-900 font-mono">{businessUnit.longitude}</p>
                      </div>
                      {businessUnit.latitude && businessUnit.longitude && (
                        <a
                          href={`https://www.google.com/maps?q=${businessUnit.latitude},${businessUnit.longitude}`}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="text-blue-600 hover:underline text-sm flex items-center gap-1"
                        >
                          <Map className="w-4 h-4" />
                          View on Map
                        </a>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Assigned Users Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Users className="w-5 h-5 text-indigo-600" />
                Assigned Users ({assignedUsers?.length || 0})
              </CardTitle>
            </CardHeader>
            <CardContent>
              {isLoadingUsers ? (
                <div className="text-center py-8 text-gray-500">Loading users...</div>
              ) : assignedUsers && assignedUsers.length > 0 ? (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Name</TableHead>
                        <TableHead>Email</TableHead>
                        <TableHead>Primary</TableHead>
                        <TableHead>Assigned Date</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {assignedUsers.map((user) => (
                        <TableRow key={user.id}>
                          <TableCell>
                            <Link
                              href={`/dashboard/users/${user.userId}`}
                              className="text-blue-600 hover:underline font-medium"
                            >
                              {user.userFirstName} {user.userLastName}
                            </Link>
                          </TableCell>
                          <TableCell className="text-gray-600">{user.userEmail}</TableCell>
                          <TableCell>
                            {user.isPrimary ? (
                              <span className="text-xs px-2 py-1 rounded bg-blue-100 text-blue-800">
                                Primary
                              </span>
                            ) : (
                              <span className="text-xs text-gray-400">-</span>
                            )}
                          </TableCell>
                          <TableCell className="text-gray-600">
                            {formatDate(user.assignedAt)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              ) : (
                <div className="text-center py-8">
                  <Users className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                  <p className="text-gray-500">No users assigned to this business unit</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Hierarchy Within BU Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <User className="w-5 h-5 text-teal-600" />
                Organizational Hierarchy ({hierarchies?.length || 0})
              </CardTitle>
            </CardHeader>
            <CardContent>
              {isLoadingHierarchies ? (
                <div className="text-center py-8 text-gray-500">Loading hierarchies...</div>
              ) : hierarchies && hierarchies.length > 0 ? (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Employee</TableHead>
                        <TableHead>Manager</TableHead>
                        <TableHead>Effective From</TableHead>
                        <TableHead>Status</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {hierarchies.map((hierarchy) => (
                        <TableRow key={hierarchy.id}>
                          <TableCell>
                            <Link
                              href={`/dashboard/users/${hierarchy.employeeId}`}
                              className="text-blue-600 hover:underline font-medium"
                            >
                              {hierarchy.employeeFirstName} {hierarchy.employeeLastName}
                            </Link>
                            {hierarchy.employeeEmail && (
                              <div className="text-xs text-gray-500">{hierarchy.employeeEmail}</div>
                            )}
                          </TableCell>
                          <TableCell>
                            <Link
                              href={`/dashboard/users/${hierarchy.managerId}`}
                              className="text-blue-600 hover:underline"
                            >
                              {hierarchy.managerFirstName} {hierarchy.managerLastName}
                            </Link>
                            {hierarchy.managerEmail && (
                              <div className="text-xs text-gray-500">{hierarchy.managerEmail}</div>
                            )}
                          </TableCell>
                          <TableCell className="text-gray-600">
                            {formatDate(hierarchy.effectiveFrom)}
                          </TableCell>
                          <TableCell>
                            <span
                              className={`text-xs px-2 py-1 rounded ${
                                hierarchy.isActive
                                  ? 'bg-green-100 text-green-800'
                                  : 'bg-gray-100 text-gray-800'
                              }`}
                            >
                              {hierarchy.isActive ? 'Active' : 'Inactive'}
                            </span>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              ) : (
                <div className="text-center py-8">
                  <User className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                  <p className="text-gray-500">No hierarchies defined for this business unit</p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Right Column - Sidebar */}
        <div className="space-y-6">
          {/* Manager Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <User className="w-4 h-4 text-blue-600" />
                Manager
              </CardTitle>
            </CardHeader>
            <CardContent>
              {businessUnit.managerId ? (
                <div className="space-y-3">
                  <div>
                    <Link
                      href={`/dashboard/users/${businessUnit.managerId}`}
                      className="text-blue-600 hover:underline font-medium text-lg"
                    >
                      {businessUnit.managerName}
                    </Link>
                    <p className="text-sm text-gray-500 mt-1">Business Unit Manager</p>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleChangeManager}
                    fullWidth
                  >
                    Change Manager
                  </Button>
                </div>
              ) : (
                <div className="text-center py-4">
                  <p className="text-gray-500 mb-3 text-sm">No manager assigned</p>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={handleChangeManager}
                    fullWidth
                  >
                    Assign Manager
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Financial Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <DollarSign className="w-4 h-4 text-yellow-600" />
                Financial Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <label className="text-sm font-medium text-gray-500">SIRET</label>
                  <p className="mt-1 text-gray-900 font-mono text-sm">
                    {businessUnit.siret || 'Not provided'}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">VAT Number</label>
                  <p className="mt-1 text-gray-900 font-mono text-sm">
                    {businessUnit.vatNumber || 'Not provided'}
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Additional Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <Tag className="w-4 h-4 text-green-600" />
                Additional Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <label className="text-sm font-medium text-gray-500">Brands</label>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {businessUnit.brands && businessUnit.brands.length > 0 ? (
                      businessUnit.brands.map((brand, index) => (
                        <span
                          key={index}
                          className="text-xs px-2.5 py-1 rounded-full bg-blue-100 text-blue-800 font-medium"
                        >
                          {brand}
                        </span>
                      ))
                    ) : (
                      <p className="text-gray-500 text-sm">No brands specified</p>
                    )}
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-gray-500">Opening Hours</label>
                  {businessUnit.openingHours ? (
                    <pre className="mt-1 text-gray-900 text-sm whitespace-pre-wrap">
                      {businessUnit.openingHours}
                    </pre>
                  ) : (
                    <p className="mt-1 text-gray-500 text-sm">Not provided</p>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* System Information Card */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <Clock className="w-4 h-4 text-gray-600" />
                System Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <label className="text-sm font-medium text-gray-500">Created At</label>
                  <p className="mt-1 text-gray-900 text-sm">
                    {formatDateTime(businessUnit.createdAt)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Updated At</label>
                  <p className="mt-1 text-gray-900 text-sm">
                    {formatDateTime(businessUnit.updatedAt)}
                  </p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Status</label>
                  <p className="mt-1">
                    <span
                      className={`text-xs px-2.5 py-1 rounded-full font-medium ${
                        businessUnit.active
                          ? 'bg-green-100 text-green-800'
                          : 'bg-gray-100 text-gray-800'
                      }`}
                    >
                      {businessUnit.active ? 'Active' : 'Inactive'}
                    </span>
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
