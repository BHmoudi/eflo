'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { keycloakSyncApi, UserSyncInfo } from '@/lib/api/keycloak-sync';
import {
  RefreshCw,
  CheckCircle,
  XCircle,
  AlertTriangle,
  Clock,
  Users,
  Shield,
  ChevronLeft,
  ChevronRight,
  Loader2,
} from 'lucide-react';
import toast from 'react-hot-toast';

export default function KeycloakSyncPage() {
  const [page, setPage] = useState(0);
  const [pageSize] = useState(20);
  const [isSyncing, setIsSyncing] = useState(false);
  const queryClient = useQueryClient();

  // Fetch sync status
  const { data: syncStatus, isLoading: isLoadingStatus } = useQuery({
    queryKey: ['keycloak-sync-status'],
    queryFn: () => keycloakSyncApi.getSyncStatus(),
    refetchInterval: 5000, // Refresh every 5 seconds to monitor ongoing sync
  });

  // Fetch users with sync info
  const { data: usersData, isLoading: isLoadingUsers } = useQuery({
    queryKey: ['keycloak-sync-users', page, pageSize],
    queryFn: () => keycloakSyncApi.getUsersWithSyncInfo(page, pageSize),
  });

  // Sync all users mutation
  const syncAllMutation = useMutation({
    mutationFn: () => keycloakSyncApi.syncAllUsersFromKeycloak(),
    onMutate: () => {
      setIsSyncing(true);
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['keycloak-sync-status'] });
      queryClient.invalidateQueries({ queryKey: ['keycloak-sync-users'] });
      toast.success(
        `Sync completed! ${data.syncedCount} users synced${
          data.failedCount > 0 ? `, ${data.failedCount} failed` : ''
        }`
      );
      setIsSyncing(false);
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to sync users from Keycloak');
      setIsSyncing(false);
    },
  });

  // Sync single user mutation
  const syncUserMutation = useMutation({
    mutationFn: (keycloakId: string) => keycloakSyncApi.syncSingleUser(keycloakId),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['keycloak-sync-status'] });
      queryClient.invalidateQueries({ queryKey: ['keycloak-sync-users'] });
      toast.success(`User ${data.user.email} synced successfully`);
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to sync user from Keycloak');
    },
  });

  const handleSyncAll = () => {
    if (
      confirm(
        'This will sync all users from Keycloak to the local database. This may take several minutes. Continue?'
      )
    ) {
      syncAllMutation.mutate();
    }
  };

  const handleSyncUser = (keycloakId: string) => {
    if (!keycloakId) {
      toast.error('This user does not have a Keycloak ID');
      return;
    }
    syncUserMutation.mutate(keycloakId);
  };

  const handleRefresh = () => {
    queryClient.invalidateQueries({ queryKey: ['keycloak-sync-status'] });
    queryClient.invalidateQueries({ queryKey: ['keycloak-sync-users'] });
    toast.success('Data refreshed');
  };

  const users = usersData?.content || [];
  const totalPages = usersData?.totalPages || 0;
  const totalElements = usersData?.totalElements || 0;

  const getSyncStatusBadge = (status: string) => {
    switch (status) {
      case 'synced':
        return (
          <span className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded bg-green-100 text-green-800">
            <CheckCircle className="w-3 h-3" />
            Synced
          </span>
        );
      case 'out_of_sync':
        return (
          <span className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded bg-yellow-100 text-yellow-800">
            <AlertTriangle className="w-3 h-3" />
            Out of Sync
          </span>
        );
      case 'error':
        return (
          <span className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded bg-red-100 text-red-800">
            <XCircle className="w-3 h-3" />
            Error
          </span>
        );
      case 'never_synced':
        return (
          <span className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded bg-gray-100 text-gray-800">
            <Clock className="w-3 h-3" />
            Never Synced
          </span>
        );
      default:
        return (
          <span className="text-xs px-2 py-1 rounded bg-gray-100 text-gray-800">Unknown</span>
        );
    }
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'Never';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const isAnySyncing = isSyncing || syncStatus?.isRunning || syncAllMutation.isPending;

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Keycloak Sync' },
        ]}
      />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Keycloak Synchronization</h1>
          <p className="text-gray-600 mt-1">Manage user synchronization with Keycloak</p>
        </div>
        <div className="flex items-center gap-3">
          <Button variant="outline" onClick={handleRefresh} className="flex items-center gap-2">
            <RefreshCw className="w-4 h-4" />
            Refresh
          </Button>
          <Button
            variant="primary"
            onClick={handleSyncAll}
            disabled={isAnySyncing}
            className="flex items-center gap-2"
          >
            {isAnySyncing ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Syncing...
              </>
            ) : (
              <>
                <Shield className="w-4 h-4" />
                Sync All Users
              </>
            )}
          </Button>
        </div>
      </div>

      {/* Sync Status Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Total Users</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {isLoadingStatus ? '-' : syncStatus?.totalUsers || 0}
                </p>
              </div>
              <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                <Users className="w-6 h-6 text-blue-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Synced Users</p>
                <p className="text-2xl font-bold text-green-600 mt-1">
                  {isLoadingStatus ? '-' : syncStatus?.syncedUsers || 0}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                <CheckCircle className="w-6 h-6 text-green-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Failed Users</p>
                <p className="text-2xl font-bold text-red-600 mt-1">
                  {isLoadingStatus ? '-' : syncStatus?.failedUsers || 0}
                </p>
              </div>
              <div className="w-12 h-12 bg-red-100 rounded-lg flex items-center justify-center">
                <XCircle className="w-6 h-6 text-red-600" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Last Sync</p>
                <p className="text-sm font-medium text-gray-900 mt-1">
                  {isLoadingStatus ? '-' : formatDate(syncStatus?.lastSyncAt)}
                </p>
              </div>
              <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                <Clock className="w-6 h-6 text-purple-600" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Error Log */}
      {syncStatus && syncStatus.errors && syncStatus.errors.length > 0 && (
        <Card className="border-red-200 bg-red-50">
          <CardHeader>
            <CardTitle className="text-red-800 flex items-center gap-2">
              <AlertTriangle className="w-5 h-5" />
              Sync Errors ({syncStatus.errors.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2 max-h-48 overflow-y-auto">
              {syncStatus.errors.map((error, index) => (
                <div
                  key={index}
                  className="p-3 bg-white border border-red-200 rounded-lg text-sm"
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex-1">
                      <p className="font-medium text-red-800">{error.message}</p>
                      {error.keycloakId && (
                        <p className="text-red-600 text-xs mt-1">
                          Keycloak ID: {error.keycloakId}
                        </p>
                      )}
                    </div>
                    <span className="text-xs text-red-600 whitespace-nowrap">
                      {formatDate(error.timestamp)}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Users List */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Users Sync Status ({totalElements})</CardTitle>
            {isAnySyncing && (
              <div className="flex items-center gap-2 text-blue-600">
                <Loader2 className="w-4 h-4 animate-spin" />
                <span className="text-sm font-medium">Sync in progress...</span>
              </div>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {isLoadingUsers ? (
            <div className="text-center py-12 text-gray-500">
              <Loader2 className="w-8 h-8 animate-spin mx-auto mb-3 text-blue-600" />
              Loading users...
            </div>
          ) : (
            <>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead>Keycloak ID</TableHead>
                    <TableHead>Keycloak Username</TableHead>
                    <TableHead>Last Synced</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {users.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={7} className="text-center py-12">
                        <Users className="w-12 h-12 text-gray-400 mx-auto mb-3" />
                        <p className="text-gray-500">No users found</p>
                      </TableCell>
                    </TableRow>
                  ) : (
                    users.map((user) => (
                      <TableRow key={user.id}>
                        <TableCell>
                          <div className="font-medium text-gray-900">
                            {user.firstName} {user.lastName}
                          </div>
                        </TableCell>
                        <TableCell>
                          <span className="text-sm text-gray-600">{user.email}</span>
                        </TableCell>
                        <TableCell>
                          <span className="text-xs font-mono text-gray-600">
                            {user.keycloakId || '-'}
                          </span>
                        </TableCell>
                        <TableCell>
                          <span className="text-sm text-gray-600">
                            {user.keycloakUsername || '-'}
                          </span>
                        </TableCell>
                        <TableCell>
                          <span className="text-xs text-gray-600">
                            {formatDate(user.lastSyncedAt)}
                          </span>
                        </TableCell>
                        <TableCell>{getSyncStatusBadge(user.syncStatus)}</TableCell>
                        <TableCell>
                          <div className="flex items-center justify-end">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => handleSyncUser(user.keycloakId || '')}
                              disabled={
                                !user.keycloakId ||
                                syncUserMutation.isPending ||
                                isAnySyncing
                              }
                              className="flex items-center gap-1"
                            >
                              {syncUserMutation.isPending ? (
                                <Loader2 className="w-3 h-3 animate-spin" />
                              ) : (
                                <RefreshCw className="w-3 h-3" />
                              )}
                              Sync
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-200">
                  <p className="text-sm text-gray-600">
                    Page {page + 1} of {totalPages} ({totalElements} total users)
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
