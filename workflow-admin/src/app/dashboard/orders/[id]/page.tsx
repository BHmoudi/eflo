'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter, useParams } from 'next/navigation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import FileUpload from '@/components/ui/FileUpload';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { orderApi } from '@/lib/api/orders';
import toast from 'react-hot-toast';
import {
  ArrowLeft,
  Edit2,
  Printer,
  X,
  FileText,
  Upload,
  Download,
  Trash2,
  Clock,
  CheckCircle,
  AlertCircle,
  User,
  Car,
  Building2,
  Calendar,
  DollarSign,
  CreditCard,
  Package,
  MessageSquare,
} from 'lucide-react';
import { formatDate, formatDateTime, getStatusColor } from '@/lib/utils';
import { OrderStatus } from '@/types/order';

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

export default function OrderDetailPage() {
  const router = useRouter();
  const params = useParams();
  const queryClient = useQueryClient();
  const orderId = parseInt(params.id as string);

  const [selectedStatus, setSelectedStatus] = useState<OrderStatus | ''>('');
  const [showStatusModal, setShowStatusModal] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => orderApi.getOrder(orderId),
  });

  const changeStatusMutation = useMutation({
    mutationFn: (status: OrderStatus) => orderApi.changeOrderStatus(orderId, status as any),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      toast.success('Order status updated successfully');
      setShowStatusModal(false);
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to update order status');
    },
  });

  const cancelOrderMutation = useMutation({
    mutationFn: (reason: string) => orderApi.cancelOrder(orderId, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      toast.success('Order cancelled successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to cancel order');
    },
  });

  const deleteDocumentMutation = useMutation({
    mutationFn: (documentId: number) => orderApi.deleteDocument(orderId, documentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      toast.success('Document deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete document');
    },
  });

  const handleStatusChange = () => {
    if (selectedStatus) {
      changeStatusMutation.mutate(selectedStatus);
    }
  };

  const handleCancelOrder = () => {
    const reason = prompt('Please enter a reason for cancelling this order:');
    if (reason !== null && reason.trim()) {
      cancelOrderMutation.mutate(reason);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const handleFilesSelected = (files: File[]) => {
    // Files are selected and ready for upload
    console.log('Files selected:', files);
  };

  const handleFileUpload = async (file: File, onProgress: (progress: number) => void) => {
    // Simulate upload progress
    return new Promise<void>((resolve, reject) => {
      let progress = 0;
      const interval = setInterval(() => {
        progress += 10;
        onProgress(progress);

        if (progress >= 100) {
          clearInterval(interval);
          // In real implementation, you would call the API here
          // await orderApi.uploadDocument(orderId, file);
          queryClient.invalidateQueries({ queryKey: ['order', orderId] });
          resolve();
        }
      }, 200);
    });
  };

  const handleFileRemove = (fileId: string) => {
    console.log('File removed:', fileId);
  };

  const handleUploadComplete = () => {
    setShowUploadModal(false);
    toast.success('Documents uploaded successfully');
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
      <span className={`text-sm px-3 py-1 rounded-full font-medium ${colorClass}`}>
        {ORDER_STATUSES.find((s) => s.value === status)?.label || status}
      </span>
    );
  };

  const getTypeBadge = (type: string) => {
    const typeLabels: Record<string, string> = {
      VN: 'New Vehicle (VN)',
      VO: 'Used Vehicle (VO)',
      EVO: 'Evolution (EVO)',
    };
    return typeLabels[type] || type;
  };

  const getPaymentStatusBadge = (status: string) => {
    if (!status) return null;

    const colors: Record<string, string> = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      DEPOSIT_RECEIVED: 'bg-blue-100 text-blue-800',
      PARTIALLY_PAID: 'bg-orange-100 text-orange-800',
      PAID: 'bg-green-100 text-green-800',
      REFUNDED: 'bg-gray-100 text-gray-800',
    };
    return (
      <span className={`text-xs px-2 py-1 rounded-full font-medium ${colors[status] || 'bg-gray-100 text-gray-800'}`}>
        {status.replace(/_/g, ' ')}
      </span>
    );
  };

  if (isLoading) {
    return (
      <div className="p-6">
        <div className="text-center py-12 text-gray-500">Loading order details...</div>
      </div>
    );
  }

  if (!order) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <p className="text-gray-500">Order not found</p>
          <Button variant="outline" onClick={() => router.back()} className="mt-4">
            Go Back
          </Button>
        </div>
      </div>
    );
  }

  const canEdit = order.status !== 'DELIVERED' && order.status !== 'CANCELLED';

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Orders', href: '/dashboard/orders' },
          { label: order.orderNumber },
        ]}
      />

      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-900">Order {order.orderNumber}</h1>
            {getStatusBadge(order.status)}
          </div>
          <p className="text-gray-600 mt-1">Created on {order.createdAt ? formatDateTime(order.createdAt) : 'N/A'}</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" onClick={() => router.back()}>
            <ArrowLeft className="w-4 h-4 mr-2" />
            Back
          </Button>
          <Button variant="outline" onClick={handlePrint}>
            <Printer className="w-4 h-4 mr-2" />
            Print
          </Button>
          {canEdit && (
            <>
              <Button
                variant="outline"
                onClick={() => setShowStatusModal(true)}
              >
                Change Status
              </Button>
              <Button
                variant="outline"
                onClick={() => router.push(`/dashboard/orders/${orderId}/edit`)}
              >
                <Edit2 className="w-4 h-4 mr-2" />
                Edit
              </Button>
              <Button
                variant="danger"
                onClick={handleCancelOrder}
              >
                <X className="w-4 h-4 mr-2" />
                Cancel Order
              </Button>
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="col-span-2 space-y-6">
          {/* Order Summary */}
          <Card>
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-6">
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-gray-500">Order Number</p>
                    <p className="font-mono font-semibold">{order.orderNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Type</p>
                    <p className="font-medium">{(order as any).type ? getTypeBadge((order as any).type) : 'N/A'}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Status</p>
                    {getStatusBadge(order.status)}
                  </div>
                </div>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-gray-500">Created</p>
                    <p className="font-medium">{order.createdAt ? formatDateTime(order.createdAt) : 'N/A'}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Last Updated</p>
                    <p className="font-medium">{order.updatedAt ? formatDateTime(order.updatedAt) : 'N/A'}</p>
                  </div>
                  {(order as any).expectedDeliveryDate && (
                    <div>
                      <p className="text-sm text-gray-500">Est. Delivery</p>
                      <p className="font-medium">{formatDate((order as any).expectedDeliveryDate, 'PPP')}</p>
                    </div>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Customer Information */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <User className="w-5 h-5 text-gray-600" />
                <CardTitle>Customer Information</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-6">
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-gray-500">Name</p>
                    <p className="font-medium">
                      {(order as any).customer?.firstName || (order as any).customerFirstName} {(order as any).customer?.lastName || (order as any).customerLastName}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Email</p>
                    <p className="font-medium">{(order as any).customer?.email || (order as any).customerEmail}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Phone</p>
                    <p className="font-medium">{(order as any).customer?.phone || (order as any).customerPhone}</p>
                  </div>
                </div>
                <div className="space-y-3">
                  {((order as any).customer?.address || (order as any).customerAddress) && (
                    <div>
                      <p className="text-sm text-gray-500">Address</p>
                      <p className="font-medium">
                        {(order as any).customer?.address || (order as any).customerAddress}
                        {((order as any).customer?.city || (order as any).customerCity) && `, ${(order as any).customer?.city || (order as any).customerCity}`}
                        {((order as any).customer?.state || (order as any).customerState) && `, ${(order as any).customer?.state || (order as any).customerState}`}
                        {((order as any).customer?.zipCode || (order as any).customerPostalCode) && ` ${(order as any).customer?.zipCode || (order as any).customerPostalCode}`}
                      </p>
                    </div>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Vehicle Details */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Car className="w-5 h-5 text-gray-600" />
                <CardTitle>Vehicle Details</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex gap-6">
                {((order as any).vehicle?.imageUrl) && (
                  <img
                    src={(order as any).vehicle.imageUrl}
                    alt={`${(order as any).make || (order as any).vehicle?.make} ${(order as any).model || (order as any).vehicle?.model}`}
                    className="w-48 h-32 object-cover rounded-lg"
                  />
                )}
                <div className="flex-1 space-y-3">
                  <div>
                    <p className="text-sm text-gray-500">Vehicle</p>
                    <p className="text-lg font-semibold">
                      {(order as any).year || (order as any).vehicle?.year} {(order as any).make || (order as any).vehicle?.make} {(order as any).model || (order as any).vehicle?.model}
                    </p>
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    {((order as any).vin || (order as any).vehicle?.vin) && (
                      <div>
                        <p className="text-sm text-gray-500">VIN</p>
                        <p className="font-mono text-sm">{(order as any).vin || (order as any).vehicle.vin}</p>
                      </div>
                    )}
                    {((order as any).trim || (order as any).vehicle?.trim) && (
                      <div>
                        <p className="text-sm text-gray-500">Trim</p>
                        <p className="font-medium">{(order as any).trim || (order as any).vehicle.trim}</p>
                      </div>
                    )}
                    {((order as any).colorExterior || (order as any).vehicle?.color) && (
                      <div>
                        <p className="text-sm text-gray-500">Color</p>
                        <p className="font-medium">{(order as any).colorExterior || (order as any).vehicle.color}</p>
                      </div>
                    )}
                    {((order as any).vehicle?.stockNumber) && (
                      <div>
                        <p className="text-sm text-gray-500">Stock #</p>
                        <p className="font-medium">{(order as any).vehicle.stockNumber}</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Options & Accessories */}
          {(order as any).options && (order as any).options.length > 0 && (
            <Card>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <Package className="w-5 h-5 text-gray-600" />
                  <CardTitle>Options & Accessories</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  {(order as any).options.map((option: any) => (
                    <div key={option.id} className="flex justify-between items-center py-2 border-b border-gray-100 last:border-0">
                      <div>
                        <p className="font-medium">{option.name}</p>
                        {option.description && (
                          <p className="text-sm text-gray-500">{option.description}</p>
                        )}
                        <p className="text-xs text-gray-400 mt-1">{option.category}</p>
                      </div>
                      <p className="font-semibold">{formatCurrency(option.price)}</p>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Pricing Breakdown */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <DollarSign className="w-5 h-5 text-gray-600" />
                <CardTitle>Pricing Breakdown</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-600">Base Price</span>
                  <span className="font-semibold">{formatCurrency(order.basePrice)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Options Total</span>
                  <span className="font-semibold">{formatCurrency((order as any).optionsTotal || 0)}</span>
                </div>
                {((order as any).discount || (order as any).discountAmount) > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Discount</span>
                    <span className="font-semibold">-{formatCurrency((order as any).discount || (order as any).discountAmount || 0)}</span>
                  </div>
                )}
                {((order as any).tradeInValue || (order as any).tradeinValue) > 0 && (
                  <div className="flex justify-between text-green-600">
                    <span>Trade-In Value</span>
                    <span className="font-semibold">-{formatCurrency((order as any).tradeInValue || (order as any).tradeinValue || 0)}</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-gray-600">Tax</span>
                  <span className="font-semibold">{formatCurrency((order as any).taxAmount || (order as any).vatAmount || 0)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Fees</span>
                  <span className="font-semibold">{formatCurrency((order as any).fees || 0)}</span>
                </div>
                <div className="flex justify-between pt-3 border-t-2 border-gray-300 text-lg">
                  <span className="font-bold">Total Price</span>
                  <span className="font-bold text-blue-600">{formatCurrency((order as any).totalPrice || (order as any).totalAmount || 0)}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Payment Information */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <CreditCard className="w-5 h-5 text-gray-600" />
                <CardTitle>Payment Information</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Payment Status</span>
                  {(order as any).paymentStatus ? getPaymentStatusBadge((order as any).paymentStatus) : <span className="text-gray-500 text-sm">N/A</span>}
                </div>
                <div className="grid grid-cols-3 gap-4">
                  <div>
                    <p className="text-sm text-gray-500">Amount Paid</p>
                    <p className="text-lg font-semibold text-green-600">
                      {formatCurrency((order as any).amountPaid || 0)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Amount Due</p>
                    <p className="text-lg font-semibold text-red-600">
                      {formatCurrency((order as any).amountDue || 0)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Deposit</p>
                    <p className="text-lg font-semibold">
                      {formatCurrency((order as any).depositAmount || 0)}
                    </p>
                  </div>
                </div>

                {(order as any).payments && (order as any).payments.length > 0 && (
                  <div className="mt-6 pt-4 border-t border-gray-200">
                    <h4 className="font-medium mb-3">Payment History</h4>
                    <div className="space-y-2">
                      {(order as any).payments.map((payment: any) => (
                        <div key={payment.id} className="flex justify-between items-center text-sm">
                          <div>
                            <p className="font-medium">{formatDate(payment.date, 'PPP')}</p>
                            <p className="text-gray-500">{payment.method} {payment.reference && `- ${payment.reference}`}</p>
                          </div>
                          <div className="text-right">
                            <p className="font-semibold">{formatCurrency(payment.amount)}</p>
                            {getPaymentStatusBadge(payment.status)}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Documents */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <FileText className="w-5 h-5 text-gray-600 dark:text-gray-400" />
                  <CardTitle>Documents</CardTitle>
                </div>
                {canEdit && (
                  <Button variant="outline" size="sm" onClick={() => setShowUploadModal(true)}>
                    <Upload className="w-4 h-4 mr-2" />
                    Upload
                  </Button>
                )}
              </div>
            </CardHeader>
            <CardContent>
              {(order as any).documents && (order as any).documents.length > 0 ? (
                <div className="space-y-2">
                  {(order as any).documents.map((doc: any) => (
                    <div key={doc.id} className="flex items-center justify-between p-3 bg-gray-50 dark:bg-meta-4 rounded-lg border border-gray-200 dark:border-strokedark">
                      <div className="flex items-center gap-3">
                        <FileText className="w-5 h-5 text-gray-400 dark:text-gray-500" />
                        <div>
                          <p className="font-medium text-gray-900 dark:text-white">{doc.name}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">
                            Uploaded by {doc.uploadedBy} on {formatDateTime(doc.uploadedAt)}
                          </p>
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <a
                          href={doc.url}
                          download
                          className="p-1 hover:bg-gray-200 dark:hover:bg-gray-700 rounded transition-colors"
                        >
                          <Download className="w-4 h-4 text-gray-600 dark:text-gray-400" />
                        </a>
                        {canEdit && (
                          <button
                            onClick={() => deleteDocumentMutation.mutate(doc.id)}
                            className="p-1 hover:bg-red-50 dark:hover:bg-red-900/20 rounded transition-colors"
                          >
                            <Trash2 className="w-4 h-4 text-red-600 dark:text-red-400" />
                          </button>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-8">
                  <FileText className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                  <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">No documents uploaded</p>
                  {canEdit && (
                    <Button variant="outline" size="sm" onClick={() => setShowUploadModal(true)}>
                      <Upload className="w-4 h-4 mr-2" />
                      Upload Documents
                    </Button>
                  )}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Notes */}
          {(order as any).notes && (
            <Card>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <MessageSquare className="w-5 h-5 text-gray-600" />
                  <CardTitle>Notes</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-gray-700 whitespace-pre-wrap">{(order as any).notes}</p>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Quick Actions */}
          <Card>
            <CardHeader>
              <CardTitle>Quick Actions</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {canEdit && (
                  <Button
                    variant="outline"
                    fullWidth
                    onClick={() => router.push(`/dashboard/orders/${orderId}/edit`)}
                  >
                    <Edit2 className="w-4 h-4 mr-2" />
                    Edit Order
                  </Button>
                )}
                <Button variant="outline" fullWidth onClick={handlePrint}>
                  <Printer className="w-4 h-4 mr-2" />
                  Print Order
                </Button>
                {canEdit && (
                  <Button variant="danger" fullWidth onClick={handleCancelOrder}>
                    <X className="w-4 h-4 mr-2" />
                    Cancel Order
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>

          {/* Business Information */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-2">
                <Building2 className="w-5 h-5 text-gray-600" />
                <CardTitle>Business Info</CardTitle>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <div>
                  <p className="text-sm text-gray-500">Business Unit</p>
                  <p className="font-medium">{(order as any).businessUnitName || (order as any).establishmentName || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500">Sales Person</p>
                  <p className="font-medium">{(order as any).salesPersonName || (order as any).salespersonName || 'N/A'}</p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Workflow Status */}
          {(order as any).workflowInstanceId && (
            <Card>
              <CardHeader>
                <CardTitle>Workflow Status</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm text-gray-500">Instance ID</p>
                    <p className="font-mono text-sm">{(order as any).workflowInstanceId}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500">Status</p>
                    <span className={`text-xs px-2 py-1 rounded ${getStatusColor((order as any).workflowStatus || (order as any).workflowCurrentState || 'PENDING')}`}>
                      {(order as any).workflowStatus || (order as any).workflowCurrentState}
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Order Timeline */}
          {(order as any).history && (order as any).history.length > 0 && (
            <Card>
              <CardHeader>
                <div className="flex items-center gap-2">
                  <Clock className="w-5 h-5 text-gray-600" />
                  <CardTitle>Order Timeline</CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {(order as any).history.map((entry: any, index: number) => (
                    <div key={entry.id} className="flex gap-3">
                      <div className="flex flex-col items-center">
                        <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                          <CheckCircle className="w-4 h-4 text-blue-600" />
                        </div>
                        {index < (order as any).history!.length - 1 && (
                          <div className="w-0.5 h-full bg-gray-200 my-1" />
                        )}
                      </div>
                      <div className="flex-1 pb-4">
                        <p className="font-medium text-sm">{entry.action}</p>
                        <p className="text-xs text-gray-500 mt-1">{entry.description}</p>
                        <p className="text-xs text-gray-400 mt-1">
                          {entry.performedBy} - {formatDateTime(entry.performedAt)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Commission Information */}
          {(order as any).commissions && (order as any).commissions.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle>Commissions</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {(order as any).commissions.map((commission: any) => (
                    <div key={commission.id} className="pb-3 border-b border-gray-200 last:border-0">
                      <div className="flex justify-between items-start">
                        <div>
                          <p className="font-medium">{commission.salesPersonName}</p>
                          <p className="text-xs text-gray-500">{commission.type}</p>
                        </div>
                        <div className="text-right">
                          <p className="font-semibold">{formatCurrency(commission.amount)}</p>
                          <p className="text-xs text-gray-500">{commission.percentage}%</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>

      {/* Status Change Modal */}
      {showStatusModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white dark:bg-boxdark rounded-lg p-6 w-96">
            <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">Change Order Status</h3>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                New Status
              </label>
              <Select
                value={selectedStatus}
                onChange={(e) => setSelectedStatus(e.target.value as OrderStatus)}
              >
                <option value="">Select status</option>
                {ORDER_STATUSES.map((status) => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </Select>
            </div>
            <div className="flex gap-3">
              <Button
                variant="outline"
                fullWidth
                onClick={() => setShowStatusModal(false)}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                fullWidth
                onClick={handleStatusChange}
                disabled={!selectedStatus || changeStatusMutation.isPending}
                isLoading={changeStatusMutation.isPending}
              >
                Update Status
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Document Upload Modal */}
      {showUploadModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-boxdark rounded-lg p-6 w-full max-w-3xl max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-bold text-gray-900 dark:text-white">Upload Documents</h3>
              <button
                onClick={() => setShowUploadModal(false)}
                className="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded transition-colors"
              >
                <X className="w-5 h-5 text-gray-500 dark:text-gray-400" />
              </button>
            </div>

            <div className="mb-4">
              <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
                Upload order documents such as contracts, agreements, proof of identity, or vehicle documents.
                Supported formats: Images (JPG, PNG), PDFs, and documents (DOC, DOCX, TXT).
              </p>

              <FileUpload
                accept="image/*,.pdf,.doc,.docx,.txt"
                maxSize={10 * 1024 * 1024} // 10MB
                multiple={true}
                maxFiles={10}
                onFilesSelected={handleFilesSelected}
                onUpload={handleFileUpload}
                onRemove={handleFileRemove}
                showPreview={true}
                uploadButtonText="Upload Documents"
                className="w-full"
              />
            </div>

            <div className="flex gap-3 mt-6 pt-6 border-t border-gray-200 dark:border-strokedark">
              <Button
                variant="outline"
                fullWidth
                onClick={() => setShowUploadModal(false)}
              >
                Close
              </Button>
              <Button
                variant="primary"
                fullWidth
                onClick={handleUploadComplete}
              >
                Done
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
