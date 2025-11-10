'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import DataTable, { DataTableColumn, BulkAction } from '@/components/ui/DataTable';
import EmptyState from '@/components/common/EmptyState';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { orderApi } from '@/lib/api/orders';
import {
  Plus,
  Download,
  Eye,
  Edit2,
  Trash2,
  X,
  ShoppingCart,
  DollarSign,
  TrendingUp,
  AlertCircle,
} from 'lucide-react';
import { formatDate, getStatusColor, downloadFile } from '@/lib/utils';
import toast from 'react-hot-toast';
import { OrderStatus, OrderType, OrderFilters } from '@/types/order';

const ORDER_STATUSES: { value: OrderStatus; label: string }[] = [
  { value: 'DRAFT', label: 'Draft' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'CONFIRMED', label: 'Confirmed' },
  { value: 'IN_PRODUCTION', label: 'In Production' },
  { value: 'READY_FOR_DELIVERY', label: 'Ready for Delivery' },
  { value: 'DELIVERED', label: 'Delivered' },
  { value: 'INVOICED', label: 'Invoiced' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'ON_HOLD', label: 'On Hold' },
];

const ORDER_TYPES: { value: OrderType; label: string }[] = [
  { value: 'VN', label: 'New Vehicle (VN)' },
  { value: 'VO', label: 'Used Vehicle (VO)' },
  { value: 'EVO', label: 'Evolution (EVO)' },
];

export default function OrdersPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const [showFilters, setShowFilters] = useState(false);
  const [filters, setFilters] = useState<OrderFilters>({
    page: 0,
    limit: 20,
    sortBy: 'createdAt',
    sortOrder: 'desc',
  });

  const { data: ordersData, isLoading } = useQuery({
    queryKey: ['orders', filters],
    queryFn: () => orderApi.getOrders(filters),
  });

  const deleteOrderMutation = useMutation({
    mutationFn: (id: number) => orderApi.deleteOrder(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast.success('Order deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete order');
    },
  });

  const bulkDeleteMutation = useMutation({
    mutationFn: (ids: number[]) => Promise.all(ids.map(id => orderApi.deleteOrder(id))),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast.success('Orders deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete orders');
    },
  });

  const cancelOrderMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason?: string }) =>
      orderApi.cancelOrder(id, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
      toast.success('Order cancelled successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to cancel order');
    },
  });

  const exportMutation = useMutation({
    mutationFn: () => orderApi.exportOrders(filters),
    onSuccess: (blob) => {
      downloadFile(blob, `orders-${new Date().toISOString().split('T')[0]}.csv`);
      toast.success('Orders exported successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to export orders');
    },
  });

  const handleDelete = (id: number) => {
    if (confirm('Are you sure you want to delete this order? This action cannot be undone.')) {
      deleteOrderMutation.mutate(id);
    }
  };

  const handleBulkDelete = (ids: (string | number)[]) => {
    if (confirm(`Are you sure you want to delete ${ids.length} order(s)? This action cannot be undone.`)) {
      bulkDeleteMutation.mutate(ids as number[]);
    }
  };

  const handleBulkExport = (ids: (string | number)[]) => {
    const selectedOrders = orders.filter((order: any) => ids.includes(order.id));
    const csv = convertToCSV(selectedOrders);
    const blob = new Blob([csv], { type: 'text/csv' });
    downloadFile(blob, `selected-orders-${new Date().toISOString().split('T')[0]}.csv`);
    toast.success('Selected orders exported successfully');
  };

  const convertToCSV = (data: any[]) => {
    if (data.length === 0) return '';
    const headers = Object.keys(data[0]).join(',');
    const rows = data.map(row => Object.values(row).join(','));
    return [headers, ...rows].join('\n');
  };

  const handleCancel = (id: number) => {
    const reason = prompt('Please enter a reason for cancelling this order:');
    if (reason !== null) {
      cancelOrderMutation.mutate({ id, reason });
    }
  };

  const handleStatusFilterChange = (status: OrderStatus) => {
    setFilters((prev) => {
      const currentStatuses = prev.status || [];
      const newStatuses = currentStatuses.includes(status)
        ? currentStatuses.filter((s) => s !== status)
        : [...currentStatuses, status];
      return { ...prev, status: newStatuses.length > 0 ? newStatuses : undefined, page: 0 };
    });
  };

  const handleTypeFilterChange = (type: OrderType) => {
    setFilters((prev) => {
      const currentTypes = prev.type || [];
      const newTypes = currentTypes.includes(type)
        ? currentTypes.filter((t) => t !== type)
        : [...currentTypes, type];
      return { ...prev, type: newTypes.length > 0 ? newTypes : undefined, page: 0 };
    });
  };

  const clearFilters = () => {
    setFilters({
      page: 0,
      limit: 20,
      sortBy: 'createdAt',
      sortOrder: 'desc',
    });
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const getStatusBadge = (status: OrderStatus) => {
    const colorClass = getStatusColor(status);
    return (
      <span className={`text-xs px-2 py-1 rounded-full font-medium ${colorClass}`}>
        {ORDER_STATUSES.find((s) => s.value === status)?.label || status}
      </span>
    );
  };

  const getTypeBadge = (type: OrderType) => {
    const colors: Record<OrderType, string> = {
      VN: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
      VO: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
      EVO: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    };
    return (
      <span className={`text-xs px-2 py-1 rounded-full font-medium ${colors[type]}`}>
        {ORDER_TYPES.find((t) => t.value === type)?.label || type}
      </span>
    );
  };

  const orders = ordersData?.content || [];
  const total = ordersData?.totalElements || 0;
  const hasActiveFilters = filters.status || filters.type || filters.dateFrom || filters.dateTo;

  // Calculate statistics
  const stats = {
    totalOrders: total,
    totalRevenue: orders.reduce((sum: number, order: any) => sum + order.totalPrice, 0),
    pendingOrders: orders.filter((order: any) => order.status === 'PENDING').length,
    overduePayments: orders.filter((order: any) => order.amountDue > 0).length,
  };

  // Define DataTable columns
  const columns: DataTableColumn[] = [
    {
      key: 'orderNumber',
      label: 'Order #',
      sortable: true,
      render: (value: string) => (
        <span className="font-mono text-sm font-medium">{value}</span>
      ),
    },
    {
      key: 'customerName',
      label: 'Customer',
      sortable: true,
    },
    {
      key: 'vehicleName',
      label: 'Vehicle',
      sortable: true,
    },
    {
      key: 'vin',
      label: 'VIN',
      sortable: true,
      render: (value: string) => (
        <span className="font-mono text-xs">{value || '-'}</span>
      ),
    },
    {
      key: 'type',
      label: 'Type',
      sortable: true,
      render: (value: OrderType) => getTypeBadge(value),
    },
    {
      key: 'status',
      label: 'Status',
      sortable: true,
      render: (value: OrderStatus) => getStatusBadge(value),
    },
    {
      key: 'totalPrice',
      label: 'Total',
      sortable: true,
      render: (value: number) => (
        <span className="font-semibold">{formatCurrency(value)}</span>
      ),
    },
    {
      key: 'amountDue',
      label: 'Amount Due',
      sortable: true,
      render: (value: number) => (
        <span className={value > 0 ? 'text-red-600 font-semibold' : ''}>
          {formatCurrency(value)}
        </span>
      ),
    },
    {
      key: 'businessUnitName',
      label: 'Business Unit',
      sortable: true,
      render: (value: string) => <span className="text-sm">{value}</span>,
    },
    {
      key: 'salesPersonName',
      label: 'Sales Person',
      sortable: true,
      render: (value: string) => <span className="text-sm">{value}</span>,
    },
    {
      key: 'createdAt',
      label: 'Date',
      sortable: true,
      render: (value: string) => (
        <span className="text-sm text-gray-500">{formatDate(value, 'PP')}</span>
      ),
    },
    {
      key: 'id',
      label: 'Actions',
      render: (value: number, row: any) => (
        <div className="flex items-center gap-2">
          <button
            onClick={() => router.push(`/dashboard/orders/${value}`)}
            className="p-1 hover:bg-gray-100 dark:hover:bg-gray-800 rounded transition-colors"
            title="View Details"
          >
            <Eye className="w-4 h-4 text-gray-600 dark:text-gray-400" />
          </button>
          <button
            onClick={() => router.push(`/dashboard/orders/${value}/edit`)}
            className="p-1 hover:bg-gray-100 dark:hover:bg-gray-800 rounded transition-colors"
            title="Edit"
            disabled={row.status === 'DELIVERED' || row.status === 'CANCELLED'}
          >
            <Edit2 className="w-4 h-4 text-gray-600 dark:text-gray-400" />
          </button>
          <button
            onClick={() => handleCancel(value)}
            className="p-1 hover:bg-yellow-50 dark:hover:bg-yellow-900/20 rounded transition-colors"
            title="Cancel Order"
            disabled={row.status === 'DELIVERED' || row.status === 'CANCELLED'}
          >
            <X className="w-4 h-4 text-yellow-600 dark:text-yellow-400" />
          </button>
          <button
            onClick={() => handleDelete(value)}
            className="p-1 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
            title="Delete"
          >
            <Trash2 className="w-4 h-4 text-red-600 dark:text-red-400" />
          </button>
        </div>
      ),
    },
  ];

  // Define bulk actions
  const bulkActions: BulkAction[] = [
    {
      label: 'Delete Selected',
      icon: <Trash2 className="w-4 h-4" />,
      onClick: handleBulkDelete,
      variant: 'danger',
    },
    {
      label: 'Export Selected',
      icon: <Download className="w-4 h-4" />,
      onClick: handleBulkExport,
      variant: 'secondary',
    },
  ];

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[{ label: t('nav.dashboard'), href: '/dashboard' }, { label: t('nav.orders') }]}
      />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{t('orders.orders')}</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">{t('orders.manageOrders')}</p>
        </div>
        <div className="flex gap-3">
          <Button
            variant="outline"
            onClick={() => exportMutation.mutate()}
            disabled={exportMutation.isPending}
            className="flex items-center gap-2"
          >
            <Download className="w-4 h-4" />
            {t('orders.exportOrders')}
          </Button>
          <Button
            variant="primary"
            onClick={() => router.push('/dashboard/orders/create')}
            className="flex items-center gap-2"
          >
            <Plus className="w-4 h-4" />
            {t('orders.createOrder')}
          </Button>
        </div>
      </div>

      {/* Quick Action Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">{t('orders.totalOrders')}</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.totalOrders}
                </p>
              </div>
              <div className="w-12 h-12 bg-brand-100 dark:bg-brand-900/30 rounded-lg flex items-center justify-center">
                <ShoppingCart className="w-6 h-6 text-brand-600 dark:text-brand-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Total Revenue</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {formatCurrency(stats.totalRevenue)}
                </p>
              </div>
              <div className="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-lg flex items-center justify-center">
                <DollarSign className="w-6 h-6 text-green-600 dark:text-green-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Pending Orders</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.pendingOrders}
                </p>
              </div>
              <div className="w-12 h-12 bg-yellow-100 dark:bg-yellow-900/30 rounded-lg flex items-center justify-center">
                <TrendingUp className="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 dark:text-gray-400">Overdue Payments</p>
                <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">
                  {stats.overduePayments}
                </p>
              </div>
              <div className="w-12 h-12 bg-red-100 dark:bg-red-900/30 rounded-lg flex items-center justify-center">
                <AlertCircle className="w-6 h-6 text-red-600 dark:text-red-400" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>All Orders ({total})</CardTitle>
            <Button
              variant="outline"
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center gap-2"
            >
              Filters
              {hasActiveFilters && (
                <span className="ml-1 px-2 py-0.5 text-xs bg-brand-600 text-white rounded-full">
                  {(filters.status?.length || 0) + (filters.type?.length || 0)}
                </span>
              )}
            </Button>
          </div>
        </CardHeader>

        {showFilters && (
          <div className="px-6 pb-4 border-b border-gray-200 dark:border-gray-800">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Status
                </label>
                <div className="flex flex-wrap gap-2">
                  {ORDER_STATUSES.map((status) => (
                    <button
                      key={status.value}
                      onClick={() => handleStatusFilterChange(status.value)}
                      className={`px-3 py-1 text-xs rounded-full transition-colors ${
                        filters.status?.includes(status.value)
                          ? 'bg-brand-600 text-white'
                          : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
                      }`}
                    >
                      {status.label}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Type</label>
                <div className="flex flex-wrap gap-2">
                  {ORDER_TYPES.map((type) => (
                    <button
                      key={type.value}
                      onClick={() => handleTypeFilterChange(type.value)}
                      className={`px-3 py-1 text-xs rounded-full transition-colors ${
                        filters.type?.includes(type.value)
                          ? 'bg-brand-600 text-white'
                          : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
                      }`}
                    >
                      {type.label}
                    </button>
                  ))}
                </div>
              </div>

              {hasActiveFilters && (
                <div className="flex justify-end">
                  <Button variant="ghost" onClick={clearFilters} className="flex items-center gap-2">
                    <X className="w-4 h-4" />
                    Clear All Filters
                  </Button>
                </div>
              )}
            </div>
          </div>
        )}

        <CardContent>
          {isLoading ? (
            <div className="text-center py-12 text-gray-500">Loading orders...</div>
          ) : orders.length === 0 ? (
            <EmptyState
              icon={<ShoppingCart className="w-12 h-12" />}
              title="No orders found"
              description="Get started by creating your first order"
              actionLabel="Create Order"
              onAction={() => router.push('/dashboard/orders/create')}
            />
          ) : (
            <DataTable
              columns={columns}
              data={orders}
              keyExtractor={(row: any) => row.id}
              isLoading={isLoading}
              searchable={true}
              searchPlaceholder="Search by order number, customer, VIN..."
              searchKeys={['orderNumber', 'customerName', 'vehicleName', 'vin']}
              sortable={true}
              selectable={true}
              bulkActions={bulkActions}
              pagination={true}
              pageSize={20}
              pageSizeOptions={[10, 20, 50, 100]}
              emptyMessage="No orders found"
              emptyIcon={<ShoppingCart className="w-12 h-12" />}
              stickyHeader={false}
            />
          )}
        </CardContent>
      </Card>
    </div>
  );
}
