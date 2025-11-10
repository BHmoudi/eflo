'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { approvalApi } from '@/lib/api/approvals';
import { roleApi } from '@/lib/api/roles';
import { Plus, CheckCircle, XCircle, X, Clock } from 'lucide-react';
import { formatDateTime, getStatusColor } from '@/lib/utils';
import toast from 'react-hot-toast';

export default function ApprovalsPage() {
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [selectedApproval, setSelectedApproval] = useState<any>(null);

  const [newChain, setNewChain] = useState({
    code: '',
    name: '',
    description: '',
  });

  const { data: chains = [], isLoading: chainsLoading } = useQuery({
    queryKey: ['approval-chains'],
    queryFn: () => approvalApi.getApprovalChains(),
  });

  const { data: approvals = [], isLoading: approvalsLoading } = useQuery({
    queryKey: ['approvals'],
    queryFn: () => approvalApi.getApprovals(),
  });

  const { data: pendingApprovals = [] } = useQuery({
    queryKey: ['pending-approvals'],
    queryFn: () => approvalApi.getPendingApprovals(),
  });

  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleApi.getRoles(),
  });

  const createChainMutation = useMutation({
    mutationFn: approvalApi.createChain,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['approval-chains'] });
      toast.success('Approval chain created successfully');
      setShowCreateDialog(false);
      setNewChain({ code: '', name: '', description: '' });
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create approval chain');
    },
  });

  const approveActionMutation = useMutation({
    mutationFn: ({ id, comments }: { id: number; comments?: string }) =>
      approvalApi.submitApprovalAction(id, { action: 'APPROVED', comments }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['approvals'] });
      queryClient.invalidateQueries({ queryKey: ['pending-approvals'] });
      toast.success('Approval submitted successfully');
      setSelectedApproval(null);
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to submit approval');
    },
  });

  const rejectActionMutation = useMutation({
    mutationFn: ({ id, comments }: { id: number; comments?: string }) =>
      approvalApi.submitApprovalAction(id, { action: 'REJECTED', comments }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['approvals'] });
      queryClient.invalidateQueries({ queryKey: ['pending-approvals'] });
      toast.success('Rejection submitted successfully');
      setSelectedApproval(null);
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to submit rejection');
    },
  });

  const handleCreateChain = () => {
    if (!newChain.code || !newChain.name) {
      toast.error('Please fill in required fields');
      return;
    }
    createChainMutation.mutate(newChain);
  };

  const handleApprove = (id: number) => {
    const comments = prompt('Add comments (optional):');
    if (comments !== null) {
      approveActionMutation.mutate({ id, comments: comments || undefined });
    }
  };

  const handleReject = (id: number) => {
    const comments = prompt('Add rejection reason (optional):');
    if (comments !== null) {
      rejectActionMutation.mutate({ id, comments: comments || undefined });
    }
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb items={[{ label: t('nav.dashboard'), href: '/dashboard' }, { label: t('nav.approvals') }]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{t('approvals.approvals')}</h1>
          <p className="text-gray-600 mt-1">{t('approvals.pendingApprovals')}</p>
        </div>
        <Button
          variant="primary"
          onClick={() => setShowCreateDialog(true)}
          className="flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          Create Chain
        </Button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Pending Approvals</p>
                <p className="text-3xl font-bold text-yellow-600 mt-2">
                  {pendingApprovals.length}
                </p>
              </div>
              <Clock className="w-8 h-8 text-yellow-600" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Total Chains</p>
                <p className="text-3xl font-bold text-blue-600 mt-2">{chains.length}</p>
              </div>
              <Plus className="w-8 h-8 text-blue-600" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">All Approvals</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{approvals.length}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-gray-600" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Pending Approvals */}
      {pendingApprovals.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Pending Your Approval ({pendingApprovals.length})</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {pendingApprovals.map((approval) => (
                <div
                  key={approval.id}
                  className="p-4 border border-yellow-200 bg-yellow-50 rounded-lg"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <p className="font-medium text-gray-900">{approval.chainName}</p>
                      <p className="text-sm text-gray-600 mt-1">
                        Workflow Instance #{approval.workflowInstanceId}
                      </p>
                      <p className="text-sm text-gray-500 mt-1">
                        Level {approval.currentLevel} • Requested by {approval.requestedByName}
                      </p>
                      <p className="text-xs text-gray-500 mt-1">
                        {formatDateTime(approval.createdAt)}
                      </p>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => handleApprove(approval.id)}
                        className="flex items-center gap-1 bg-green-600 text-white hover:bg-green-700"
                      >
                        <CheckCircle className="w-4 h-4" />
                        Approve
                      </Button>
                      <Button
                        size="sm"
                        variant="danger"
                        onClick={() => handleReject(approval.id)}
                        className="flex items-center gap-1"
                      >
                        <XCircle className="w-4 h-4" />
                        Reject
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Approval Chains */}
        <Card>
          <CardHeader>
            <CardTitle>Approval Chains ({chains.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {chainsLoading ? (
              <div className="text-center py-12 text-gray-500">Loading chains...</div>
            ) : chains.length === 0 ? (
              <div className="text-center py-12 text-gray-500">No approval chains</div>
            ) : (
              <div className="space-y-2">
                {chains.map((chain) => (
                  <div
                    key={chain.code}
                    className="p-4 border border-gray-200 rounded-lg hover:border-blue-300 transition-colors"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">{chain.name}</p>
                        <p className="text-sm text-gray-500">{chain.code}</p>
                        {chain.description && (
                          <p className="text-sm text-gray-600 mt-1">{chain.description}</p>
                        )}
                      </div>
                      <span
                        className={`text-xs px-2 py-1 rounded ${getStatusColor(
                          chain.isActive ? 'ACTIVE' : 'INACTIVE'
                        )}`}
                      >
                        {chain.isActive ? 'Active' : 'Inactive'}
                      </span>
                    </div>
                    {chain.levels && chain.levels.length > 0 && (
                      <div className="mt-2">
                        <p className="text-xs text-gray-500">
                          {chain.levels.length} level{chain.levels.length !== 1 ? 's' : ''}
                        </p>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* All Approvals */}
        <Card>
          <CardHeader>
            <CardTitle>Recent Approvals ({approvals.length})</CardTitle>
          </CardHeader>
          <CardContent>
            {approvalsLoading ? (
              <div className="text-center py-12 text-gray-500">Loading approvals...</div>
            ) : approvals.length === 0 ? (
              <div className="text-center py-12 text-gray-500">No approvals yet</div>
            ) : (
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {approvals.slice(0, 10).map((approval) => (
                  <div
                    key={approval.id}
                    className="p-3 border border-gray-200 rounded-lg hover:border-gray-300 transition-colors"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="text-sm font-medium text-gray-900">{approval.chainName}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          Instance #{approval.workflowInstanceId}
                        </p>
                      </div>
                      <span
                        className={`text-xs px-2 py-1 rounded ${getStatusColor(approval.status)}`}
                      >
                        {approval.status}
                      </span>
                    </div>
                    <p className="text-xs text-gray-500 mt-2">
                      {formatDateTime(approval.createdAt)}
                    </p>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Create Chain Dialog */}
      {showCreateDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-gray-900">Create Approval Chain</h2>
              <button
                onClick={() => setShowCreateDialog(false)}
                className="p-1 hover:bg-gray-100 rounded transition-colors"
              >
                <X className="w-5 h-5 text-gray-500" />
              </button>
            </div>
            <div className="space-y-4">
              <Input
                label="Chain Code"
                value={newChain.code}
                onChange={(e) => setNewChain({ ...newChain, code: e.target.value })}
                placeholder="e.g., APPROVAL_001"
                required
              />
              <Input
                label="Chain Name"
                value={newChain.name}
                onChange={(e) => setNewChain({ ...newChain, name: e.target.value })}
                placeholder="e.g., Document Approval"
                required
              />
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={3}
                  value={newChain.description}
                  onChange={(e) => setNewChain({ ...newChain, description: e.target.value })}
                  placeholder="Chain description..."
                />
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <Button variant="outline" onClick={() => setShowCreateDialog(false)}>
                Cancel
              </Button>
              <Button
                variant="primary"
                onClick={handleCreateChain}
                isLoading={createChainMutation.isPending}
              >
                Create Chain
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
