'use client';

import React, { useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { userApi } from '@/lib/api/users';
import {
  ArrowLeft,
  Edit,
  Loader2,
  UserX,
  UserCheck,
  Key,
  Mail,
  Phone,
  Briefcase,
  MapPin,
  Calendar,
  Users,
  Shield,
  Activity,
  Building2
} from 'lucide-react';
import toast from 'react-hot-toast';
import BusinessUnitAssignment from '@/components/users/BusinessUnitAssignment';
import ManagerAssignment from '@/components/users/ManagerAssignment';
import PasswordResetModal from '@/components/users/PasswordResetModal';
import UserRoleManagement from '@/components/users/UserRoleManagement';
import ActivityLogViewer from '@/components/users/ActivityLogViewer';

export default function UserDetailPage() {
  const router = useRouter();
  const params = useParams();
  const queryClient = useQueryClient();
  const userId = parseInt(params.id as string, 10);
  const [showDeactivateConfirm, setShowDeactivateConfirm] = useState(false);
  const [showPasswordResetModal, setShowPasswordResetModal] = useState(false);

  const { data: user, isLoading: isLoadingUser } = useQuery({
    queryKey: ['user', userId],
    queryFn: () => userApi.getUserById(userId),
    enabled: !isNaN(userId),
  });

  const { data: businessUnits = [] } = useQuery({
    queryKey: ['user-business-units', userId],
    queryFn: () => userApi.getUserBusinessUnits(userId),
    enabled: !isNaN(userId),
  });

  const { data: hierarchy } = useQuery({
    queryKey: ['user-hierarchy', userId],
    queryFn: () => userApi.getUserHierarchy(userId),
    enabled: !isNaN(userId),
  });

  const { data: directReports = [] } = useQuery({
    queryKey: ['user-direct-reports', userId],
    queryFn: () => userApi.getUserDirectReports(userId),
    enabled: !isNaN(userId),
  });

  const { data: roles = [] } = useQuery({
    queryKey: ['user-roles', userId],
    queryFn: () => userApi.getUserRoles(userId),
    enabled: !isNaN(userId),
  });

  const { data: activityLogs } = useQuery({
    queryKey: ['user-activity-logs', userId],
    queryFn: () => userApi.getUserActivityLogs(userId, 0, 10),
    enabled: !isNaN(userId),
  });

  const deactivateMutation = useMutation({
    mutationFn: () => userApi.deactivateUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', userId] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User deactivated successfully');
      setShowDeactivateConfirm(false);
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to deactivate user');
    },
  });

  const reactivateMutation = useMutation({
    mutationFn: () => userApi.reactivateUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', userId] });
      queryClient.invalidateQueries({ queryKey: ['users'] });
      toast.success('User reactivated successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to reactivate user');
    },
  });

  // Remove the old resetPasswordMutation as it's now handled by the modal
  // const resetPasswordMutation = useMutation({
  //   mutationFn: () => userApi.resetUserPassword(userId),
  //   onSuccess: () => {
  //     toast.success('Password reset email sent successfully');
  //   },
  //   onError: (error: any) => {
  //     toast.error(error.message || 'Failed to reset password');
  //   },
  // });

  const handleEdit = () => {
    router.push(`/dashboard/users/${userId}/edit`);
  };

  const handleBack = () => {
    router.push('/dashboard/users');
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

  if (isNaN(userId)) {
    return (
      <div className="p-6 space-y-6">
        <Breadcrumb
          items={[
            { label: 'Dashboard', href: '/dashboard' },
            { label: 'Users', href: '/dashboard/users' },
            { label: 'User Details' },
          ]}
        />
        <Card className="max-w-4xl mx-auto">
          <CardContent className="py-12 text-center">
            <p className="text-red-600">Invalid user ID</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (isLoadingUser) {
    return (
      <div className="p-6 space-y-6">
        <Breadcrumb
          items={[
            { label: 'Dashboard', href: '/dashboard' },
            { label: 'Users', href: '/dashboard/users' },
            { label: 'User Details' },
          ]}
        />
        <Card className="max-w-4xl mx-auto">
          <CardContent className="py-12 text-center">
            <Loader2 className="w-8 h-8 animate-spin mx-auto mb-4 text-blue-600" />
            <p className="text-gray-600">Loading user details...</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="p-6 space-y-6">
        <Breadcrumb
          items={[
            { label: 'Dashboard', href: '/dashboard' },
            { label: 'Users', href: '/dashboard/users' },
            { label: 'User Details' },
          ]}
        />
        <Card className="max-w-4xl mx-auto">
          <CardContent className="py-12 text-center">
            <p className="text-red-600">User not found</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  const primaryBusinessUnit = businessUnits.find(bu => bu.isPrimary);

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Users', href: '/dashboard/users' },
          { label: 'User Details' },
        ]}
      />

      {/* Header Section */}
      <div className="flex items-start justify-between">
        <div className="flex items-center gap-3">
          <Button
            variant="ghost"
            onClick={handleBack}
            className="p-2"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div>
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold text-gray-900">
                {user.fullName || `${user.firstName} ${user.lastName}`}
              </h1>
              <span
                className={`px-2 py-1 text-xs font-semibold rounded-full ${
                  user.isActive || user.active
                    ? 'bg-green-100 text-green-800'
                    : 'bg-red-100 text-red-800'
                }`}
              >
                {user.isActive || user.active ? 'Active' : 'Inactive'}
              </span>
            </div>
            <p className="text-gray-600 mt-1">{user.jobTitle || 'No job title set'}</p>
          </div>
        </div>

        <div className="flex gap-2">
          <Button
            variant="outline"
            onClick={handleEdit}
          >
            <Edit className="w-4 h-4 mr-2" />
            Edit
          </Button>
          {user.isActive || user.active ? (
            <Button
              variant="outline"
              onClick={() => setShowDeactivateConfirm(true)}
            >
              <UserX className="w-4 h-4 mr-2" />
              Deactivate
            </Button>
          ) : (
            <Button
              variant="outline"
              onClick={() => reactivateMutation.mutate()}
              isLoading={reactivateMutation.isPending}
            >
              <UserCheck className="w-4 h-4 mr-2" />
              Reactivate
            </Button>
          )}
          <Button
            variant="outline"
            onClick={() => setShowPasswordResetModal(true)}
          >
            <Key className="w-4 h-4 mr-2" />
            Reset Password
          </Button>
        </div>
      </div>

      {/* Password Reset Modal */}
      {showPasswordResetModal && (
        <PasswordResetModal
          userId={userId}
          userName={user.fullName || `${user.firstName} ${user.lastName}`}
          onClose={() => setShowPasswordResetModal(false)}
        />
      )}

      {/* Deactivate Confirmation Modal */}
      {showDeactivateConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <Card className="max-w-md">
            <CardHeader>
              <CardTitle>Confirm Deactivation</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-gray-600 mb-6">
                Are you sure you want to deactivate this user? They will no longer be able to access the system.
              </p>
              <div className="flex gap-3 justify-end">
                <Button
                  variant="outline"
                  onClick={() => setShowDeactivateConfirm(false)}
                  disabled={deactivateMutation.isPending}
                >
                  Cancel
                </Button>
                <Button
                  variant="primary"
                  onClick={() => deactivateMutation.mutate()}
                  isLoading={deactivateMutation.isPending}
                  className="bg-red-600 hover:bg-red-700"
                >
                  Deactivate
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column - Main Info */}
        <div className="lg:col-span-2 space-y-6">
          {/* Personal & Contact Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Mail className="w-5 h-5" />
                Personal & Contact Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="text-sm font-medium text-gray-500">Email</label>
                  <p className="text-gray-900 mt-1">{user.email}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Full Name</label>
                  <p className="text-gray-900 mt-1">{user.fullName || `${user.firstName} ${user.lastName}`}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Phone Number</label>
                  <p className="text-gray-900 mt-1">{user.phoneNumber || user.phone || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Mobile Number</label>
                  <p className="text-gray-900 mt-1">{user.mobileNumber || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Office Extension</label>
                  <p className="text-gray-900 mt-1">{user.officeExtension || 'N/A'}</p>
                </div>
                {user.profilePictureUrl && (
                  <div>
                    <label className="text-sm font-medium text-gray-500">Profile Picture</label>
                    <img
                      src={user.profilePictureUrl}
                      alt="Profile"
                      className="w-16 h-16 rounded-full mt-1 object-cover"
                    />
                  </div>
                )}
              </div>
              {user.bio && (
                <div className="mt-6">
                  <label className="text-sm font-medium text-gray-500">Bio</label>
                  <p className="text-gray-900 mt-1">{user.bio}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Employment Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Briefcase className="w-5 h-5" />
                Employment Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="text-sm font-medium text-gray-500">Employee Number</label>
                  <p className="text-gray-900 mt-1">{user.employeeNumber || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">User IPN</label>
                  <p className="text-gray-900 mt-1">{user.userIpn || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Job Title</label>
                  <p className="text-gray-900 mt-1">{user.jobTitle || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Department</label>
                  <p className="text-gray-900 mt-1">{user.department || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Hire Date</label>
                  <p className="text-gray-900 mt-1">{formatDate(user.hireDate)}</p>
                </div>
                {user.terminationDate && (
                  <div>
                    <label className="text-sm font-medium text-gray-500">Termination Date</label>
                    <p className="text-gray-900 mt-1">{formatDate(user.terminationDate)}</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Address Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="w-5 h-5" />
                Address Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="md:col-span-2">
                  <label className="text-sm font-medium text-gray-500">Address Line 1</label>
                  <p className="text-gray-900 mt-1">{user.addressLine1 || 'N/A'}</p>
                </div>
                {user.addressLine2 && (
                  <div className="md:col-span-2">
                    <label className="text-sm font-medium text-gray-500">Address Line 2</label>
                    <p className="text-gray-900 mt-1">{user.addressLine2}</p>
                  </div>
                )}
                <div>
                  <label className="text-sm font-medium text-gray-500">City</label>
                  <p className="text-gray-900 mt-1">{user.city || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Postal Code</label>
                  <p className="text-gray-900 mt-1">{user.postalCode || 'N/A'}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-gray-500">Country</label>
                  <p className="text-gray-900 mt-1">{user.country || 'N/A'}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Business Units Assignment - New Interactive Component */}
          <BusinessUnitAssignment userId={userId} />

          {/* Manager Assignment - New Interactive Component */}
          <ManagerAssignment
            userId={userId}
            userEmail={user.email}
            userName={user.fullName || `${user.firstName} ${user.lastName}`}
          />

          {/* User Role Management - Enhanced Interactive Component */}
          <UserRoleManagement userId={userId} />

          {/* Activity Log Viewer - Enhanced with Filtering and Export */}
          <ActivityLogViewer userId={userId} />
        </div>

        {/* Right Column - Related Info */}
        <div className="space-y-6">
          {/* Quick Stats */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="w-5 h-5" />
                Account Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <label className="text-sm font-medium text-gray-500">Last Login</label>
                <p className="text-gray-900 mt-1">{formatDateTime(user.lastLoginAt)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">Login Count</label>
                <p className="text-gray-900 mt-1">{user.loginCount || 0} times</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">Account Created</label>
                <p className="text-gray-900 mt-1">{formatDateTime(user.createdAt)}</p>
              </div>
              <div>
                <label className="text-sm font-medium text-gray-500">Last Updated</label>
                <p className="text-gray-900 mt-1">{formatDateTime(user.updatedAt)}</p>
              </div>
              {user.keycloakId && (
                <div>
                  <label className="text-sm font-medium text-gray-500">Keycloak ID</label>
                  <p className="text-gray-900 mt-1 text-xs break-all">{user.keycloakId}</p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Manager and Direct Reports Info */}
          {(hierarchy || directReports.length > 0) && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Users className="w-5 h-5" />
                  Reporting Structure
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {hierarchy?.manager && (
                  <div>
                    <label className="text-sm font-medium text-gray-500">Reports To</label>
                    <p className="text-gray-900 mt-1">
                      {hierarchy.manager.fullName || `${hierarchy.manager.firstName} ${hierarchy.manager.lastName}`}
                    </p>
                    <p className="text-xs text-gray-500">{hierarchy.manager.email}</p>
                  </div>
                )}
                {directReports.length > 0 && (
                  <div>
                    <label className="text-sm font-medium text-gray-500">Direct Reports</label>
                    <p className="text-gray-900 mt-1">{directReports.length} employee(s)</p>
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          {/* Role Assignments Summary - Simplified view in sidebar */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="w-5 h-5" />
                Role Summary
              </CardTitle>
            </CardHeader>
            <CardContent>
              {roles.length > 0 ? (
                <div className="space-y-2">
                  {roles.map((role) => (
                    <div
                      key={role.id}
                      className="p-2 bg-gray-50 rounded"
                    >
                      <p className="text-sm font-medium text-gray-900">{role.roleName}</p>
                      <p className="text-xs text-gray-500">
                        {role.roleSource}
                      </p>
                    </div>
                  ))}
                  <p className="text-xs text-gray-500 mt-2">
                    See full role management in the main section
                  </p>
                </div>
              ) : (
                <p className="text-gray-500 text-center py-4 text-sm">No roles assigned</p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
