'use client';

import React, { useState } from 'react';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import FileUpload from '@/components/ui/FileUpload';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { orderApi } from '@/lib/api/orders';
import { businessUnitApi } from '@/lib/api/business-units';
import { userApi } from '@/lib/api/users';
import toast from 'react-hot-toast';
import {
  ArrowLeft,
  ArrowRight,
  Save,
  User,
  Car,
  Package,
  DollarSign,
  FileText,
} from 'lucide-react';
import { OrderType, CreateOrderRequest } from '@/types/order';

type Step = 'customer' | 'vehicle' | 'options' | 'pricing' | 'documents';

const STEPS: { id: Step; label: string; icon: any }[] = [
  { id: 'customer', label: 'Customer Information', icon: User },
  { id: 'vehicle', label: 'Vehicle Selection', icon: Car },
  { id: 'options', label: 'Options & Accessories', icon: Package },
  { id: 'pricing', label: 'Pricing & Payment', icon: DollarSign },
  { id: 'documents', label: 'Documents', icon: FileText },
];

const ORDER_TYPES: { value: OrderType; label: string }[] = [
  { value: 'VN', label: 'New Vehicle (VN)' },
  { value: 'VO', label: 'Used Vehicle (VO)' },
  { value: 'EVO', label: 'Evolution (EVO)' },
];

export default function CreateOrderPage() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState<Step>('customer');
  const [formData, setFormData] = useState<Partial<CreateOrderRequest>>({
    type: 'VN',
  });
  const [uploadedFiles, setUploadedFiles] = useState<File[]>([]);

  // Mock data queries - replace with real API calls
  const { data: businessUnits = [] } = useQuery({
    queryKey: ['business-units-active'],
    queryFn: () => businessUnitApi.getActiveBusinessUnits(),
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users-active'],
    queryFn: () => userApi.getActiveUsers(),
  });

  // Mock vehicles - replace with real API
  const mockVehicles = [
    { id: 1, name: '2024 Toyota Camry - Silver', basePrice: 28000 },
    { id: 2, name: '2024 Honda Accord - Black', basePrice: 30000 },
    { id: 3, name: '2024 Ford F-150 - Blue', basePrice: 45000 },
  ];

  // Mock options - replace with real API
  const mockOptions = [
    { id: 1, name: 'Premium Sound System', price: 1500, category: 'Audio' },
    { id: 2, name: 'Leather Seats', price: 2000, category: 'Interior' },
    { id: 3, name: 'Sunroof', price: 1200, category: 'Exterior' },
    { id: 4, name: 'Navigation System', price: 800, category: 'Technology' },
    { id: 5, name: 'Backup Camera', price: 500, category: 'Safety' },
  ];

  const [selectedOptions, setSelectedOptions] = useState<number[]>([]);

  const createOrderMutation = useMutation({
    mutationFn: (data: CreateOrderRequest) => orderApi.createOrder(data as any),
    onSuccess: (order) => {
      toast.success('Order created successfully');
      router.push(`/dashboard/orders/${order.id}`);
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create order');
    },
  });

  const handleNext = () => {
    const currentIndex = STEPS.findIndex((s) => s.id === currentStep);
    if (currentIndex < STEPS.length - 1) {
      setCurrentStep(STEPS[currentIndex + 1].id);
    }
  };

  const handlePrevious = () => {
    const currentIndex = STEPS.findIndex((s) => s.id === currentStep);
    if (currentIndex > 0) {
      setCurrentStep(STEPS[currentIndex - 1].id);
    }
  };

  const handleSubmit = async () => {
    if (!formData.customerId) {
      toast.error('Please select a customer');
      setCurrentStep('customer');
      return;
    }
    if (!formData.vehicleId) {
      toast.error('Please select a vehicle');
      setCurrentStep('vehicle');
      return;
    }
    if (!formData.businessUnitId) {
      toast.error('Please select a business unit');
      setCurrentStep('customer');
      return;
    }
    if (!formData.salesPersonId) {
      toast.error('Please select a sales person');
      setCurrentStep('customer');
      return;
    }

    const orderData: CreateOrderRequest = {
      orderType: formData.type!,
      customerId: formData.customerId!,
      vehicleId: formData.vehicleId,
      businessUnitId: formData.businessUnitId!,
      salespersonId: formData.salesPersonId!,
      basePrice: selectedVehicle?.basePrice || 0,
      optionIds: selectedOptions,
      depositAmount: formData.depositAmount || 0,
      discount: formData.discount || 0,
      tradeInValue: formData.tradeInValue || 0,
      notes: formData.notes,
      estimatedDeliveryDate: formData.estimatedDeliveryDate,
    };

    createOrderMutation.mutate(orderData);
  };

  const handleFilesSelected = (files: File[]) => {
    setUploadedFiles((prev) => [...prev, ...files]);
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
          resolve();
        }
      }, 200);

      // In a real implementation, you would upload to your API:
      // try {
      //   const formData = new FormData();
      //   formData.append('file', file);
      //
      //   const xhr = new XMLHttpRequest();
      //   xhr.upload.addEventListener('progress', (e) => {
      //     if (e.lengthComputable) {
      //       const percentComplete = (e.loaded / e.total) * 100;
      //       onProgress(percentComplete);
      //     }
      //   });
      //
      //   xhr.addEventListener('load', () => {
      //     if (xhr.status === 200) {
      //       resolve();
      //     } else {
      //       reject(new Error('Upload failed'));
      //     }
      //   });
      //
      //   xhr.addEventListener('error', () => reject(new Error('Upload failed')));
      //   xhr.open('POST', '/api/upload');
      //   xhr.send(formData);
      // } catch (error) {
      //   reject(error);
      // }
    });
  };

  const handleFileRemove = (fileId: string) => {
    // Handle file removal if needed
    console.log('File removed:', fileId);
  };

  const selectedVehicle = mockVehicles.find((v) => v.id === formData.vehicleId);
  const selectedOptionsList = mockOptions.filter((o) => selectedOptions.includes(o.id));

  const basePrice = selectedVehicle?.basePrice || 0;
  const optionsTotal = selectedOptionsList.reduce((sum, opt) => sum + opt.price, 0);
  const subtotal = basePrice + optionsTotal;
  const discount = formData.discount || 0;
  const tradeInValue = formData.tradeInValue || 0;
  const taxRate = 0.08;
  const taxAmount = (subtotal - discount) * taxRate;
  const totalPrice = subtotal - discount - tradeInValue + taxAmount;

  const currentStepIndex = STEPS.findIndex((s) => s.id === currentStep);
  const isLastStep = currentStepIndex === STEPS.length - 1;

  const toggleOption = (optionId: number) => {
    setSelectedOptions((prev) =>
      prev.includes(optionId) ? prev.filter((id) => id !== optionId) : [...prev, optionId]
    );
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Orders', href: '/dashboard/orders' },
          { label: 'Create Order' },
        ]}
      />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Create New Order</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">Fill in the order details step by step</p>
        </div>
        <Button variant="outline" onClick={() => router.back()}>
          <ArrowLeft className="w-4 h-4 mr-2" />
          Back
        </Button>
      </div>

      {/* Step Indicator */}
      <div className="bg-white dark:bg-boxdark rounded-lg shadow-sm border border-gray-200 dark:border-strokedark p-6">
        <div className="flex items-center justify-between">
          {STEPS.map((step, index) => {
            const Icon = step.icon;
            const isActive = step.id === currentStep;
            const isCompleted = index < currentStepIndex;

            return (
              <React.Fragment key={step.id}>
                <div className="flex flex-col items-center flex-1">
                  <div
                    className={`w-12 h-12 rounded-full flex items-center justify-center border-2 transition-colors ${
                      isActive
                        ? 'bg-blue-600 dark:bg-primary border-blue-600 dark:border-primary text-white'
                        : isCompleted
                        ? 'bg-green-600 border-green-600 text-white'
                        : 'bg-white dark:bg-boxdark border-gray-300 dark:border-strokedark text-gray-400 dark:text-gray-500'
                    }`}
                  >
                    <Icon className="w-6 h-6" />
                  </div>
                  <span
                    className={`mt-2 text-sm font-medium ${
                      isActive ? 'text-blue-600 dark:text-primary' : isCompleted ? 'text-green-600' : 'text-gray-500 dark:text-gray-400'
                    }`}
                  >
                    {step.label}
                  </span>
                </div>
                {index < STEPS.length - 1 && (
                  <div
                    className={`flex-1 h-0.5 mx-4 ${
                      isCompleted ? 'bg-green-600' : 'bg-gray-300 dark:bg-strokedark'
                    }`}
                  />
                )}
              </React.Fragment>
            );
          })}
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Form */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>{STEPS.find((s) => s.id === currentStep)?.label}</CardTitle>
            </CardHeader>
            <CardContent>
              {/* Customer Information */}
              {currentStep === 'customer' && (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Order Type *
                    </label>
                    <Select
                      value={formData.type}
                      onChange={(e) => setFormData({ ...formData, type: e.target.value as OrderType })}
                    >
                      {ORDER_TYPES.map((type) => (
                        <option key={type.value} value={type.value}>
                          {type.label}
                        </option>
                      ))}
                    </Select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Customer *
                    </label>
                    <Select
                      value={formData.customerId || ''}
                      onChange={(e) =>
                        setFormData({ ...formData, customerId: parseInt(e.target.value) })
                      }
                    >
                      <option value="">Select a customer</option>
                      {users.slice(0, 10).map((user: any) => (
                        <option key={user.id} value={user.id}>
                          {user.firstName} {user.lastName} - {user.email}
                        </option>
                      ))}
                    </Select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Business Unit *
                    </label>
                    <Select
                      value={formData.businessUnitId || ''}
                      onChange={(e) =>
                        setFormData({ ...formData, businessUnitId: parseInt(e.target.value) })
                      }
                    >
                      <option value="">Select a business unit</option>
                      {businessUnits.map((unit: any) => (
                        <option key={unit.id} value={unit.id}>
                          {unit.name} ({unit.code})
                        </option>
                      ))}
                    </Select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Sales Person *
                    </label>
                    <Select
                      value={formData.salesPersonId || ''}
                      onChange={(e) =>
                        setFormData({ ...formData, salesPersonId: parseInt(e.target.value) })
                      }
                    >
                      <option value="">Select a sales person</option>
                      {users.slice(0, 10).map((user: any) => (
                        <option key={user.id} value={user.id}>
                          {user.firstName} {user.lastName}
                        </option>
                      ))}
                    </Select>
                  </div>
                </div>
              )}

              {/* Vehicle Selection */}
              {currentStep === 'vehicle' && (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Select Vehicle *
                    </label>
                    <div className="space-y-2">
                      {mockVehicles.map((vehicle) => (
                        <div
                          key={vehicle.id}
                          onClick={() => setFormData({ ...formData, vehicleId: vehicle.id })}
                          className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                            formData.vehicleId === vehicle.id
                              ? 'border-blue-600 dark:border-primary bg-blue-50 dark:bg-primary/10'
                              : 'border-gray-200 dark:border-strokedark hover:border-gray-300 dark:hover:border-gray-600'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <div>
                              <p className="font-medium text-gray-900 dark:text-white">{vehicle.name}</p>
                              <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                                Base Price: {formatCurrency(vehicle.basePrice)}
                              </p>
                            </div>
                            {formData.vehicleId === vehicle.id && (
                              <div className="w-6 h-6 bg-blue-600 dark:bg-primary rounded-full flex items-center justify-center">
                                <svg
                                  className="w-4 h-4 text-white"
                                  fill="currentColor"
                                  viewBox="0 0 20 20"
                                >
                                  <path
                                    fillRule="evenodd"
                                    d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                                    clipRule="evenodd"
                                  />
                                </svg>
                              </div>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* Options & Accessories */}
              {currentStep === 'options' && (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Select Options & Accessories
                    </label>
                    <div className="space-y-2">
                      {mockOptions.map((option) => (
                        <div
                          key={option.id}
                          onClick={() => toggleOption(option.id)}
                          className={`p-4 border-2 rounded-lg cursor-pointer transition-colors ${
                            selectedOptions.includes(option.id)
                              ? 'border-blue-600 dark:border-primary bg-blue-50 dark:bg-primary/10'
                              : 'border-gray-200 dark:border-strokedark hover:border-gray-300 dark:hover:border-gray-600'
                          }`}
                        >
                          <div className="flex items-center justify-between">
                            <div>
                              <p className="font-medium text-gray-900 dark:text-white">{option.name}</p>
                              <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                                {option.category} - {formatCurrency(option.price)}
                              </p>
                            </div>
                            <input
                              type="checkbox"
                              checked={selectedOptions.includes(option.id)}
                              onChange={() => {}}
                              className="w-5 h-5 text-blue-600 dark:text-primary rounded"
                            />
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* Pricing & Payment */}
              {currentStep === 'pricing' && (
                <div className="space-y-4">
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Deposit Amount
                      </label>
                      <Input
                        type="number"
                        min="0"
                        step="100"
                        value={formData.depositAmount || ''}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            depositAmount: parseFloat(e.target.value) || 0,
                          })
                        }
                        placeholder="0.00"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Discount
                      </label>
                      <Input
                        type="number"
                        min="0"
                        step="100"
                        value={formData.discount || ''}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            discount: parseFloat(e.target.value) || 0,
                          })
                        }
                        placeholder="0.00"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Trade-In Value
                      </label>
                      <Input
                        type="number"
                        min="0"
                        step="100"
                        value={formData.tradeInValue || ''}
                        onChange={(e) =>
                          setFormData({
                            ...formData,
                            tradeInValue: parseFloat(e.target.value) || 0,
                          })
                        }
                        placeholder="0.00"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                        Estimated Delivery Date
                      </label>
                      <Input
                        type="date"
                        value={formData.estimatedDeliveryDate || ''}
                        onChange={(e) =>
                          setFormData({ ...formData, estimatedDeliveryDate: e.target.value })
                        }
                      />
                    </div>
                  </div>
                </div>
              )}

              {/* Documents */}
              {currentStep === 'documents' && (
                <div className="space-y-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Upload Order Documents
                    </label>
                    <p className="text-sm text-gray-500 dark:text-gray-400 mb-4">
                      Upload any relevant documents such as contracts, agreements, proof of identity, or vehicle documents.
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

                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Notes
                    </label>
                    <textarea
                      rows={6}
                      value={formData.notes || ''}
                      onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-strokedark rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:focus:ring-primary bg-white dark:bg-boxdark text-gray-900 dark:text-white"
                      placeholder="Add any additional notes or instructions..."
                    />
                  </div>

                  <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
                    <p className="text-sm text-blue-800 dark:text-blue-300">
                      <strong>Tip:</strong> You can upload multiple files at once by dragging and dropping them into the upload area, or by selecting multiple files from your computer. Supported formats include images (JPG, PNG), PDFs, and documents (DOC, DOCX, TXT).
                    </p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Navigation Buttons */}
          <div className="flex items-center justify-between mt-6">
            <Button
              variant="outline"
              onClick={handlePrevious}
              disabled={currentStepIndex === 0}
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              Previous
            </Button>

            {isLastStep ? (
              <Button
                variant="primary"
                onClick={handleSubmit}
                disabled={createOrderMutation.isPending}
                isLoading={createOrderMutation.isPending}
              >
                <Save className="w-4 h-4 mr-2" />
                Create Order
              </Button>
            ) : (
              <Button variant="primary" onClick={handleNext}>
                Next
                <ArrowRight className="w-4 h-4 ml-2" />
              </Button>
            )}
          </div>
        </div>

        {/* Order Summary Sidebar */}
        <div>
          <Card>
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div>
                  <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">Vehicle</h4>
                  {selectedVehicle ? (
                    <p className="text-sm text-gray-900 dark:text-white">{selectedVehicle.name}</p>
                  ) : (
                    <p className="text-sm text-gray-500 dark:text-gray-400 italic">No vehicle selected</p>
                  )}
                </div>

                <div className="border-t border-gray-200 dark:border-strokedark pt-4">
                  <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Options ({selectedOptionsList.length})
                  </h4>
                  {selectedOptionsList.length > 0 ? (
                    <ul className="space-y-1">
                      {selectedOptionsList.map((opt) => (
                        <li key={opt.id} className="text-sm text-gray-600 dark:text-gray-400 flex justify-between">
                          <span>{opt.name}</span>
                          <span>{formatCurrency(opt.price)}</span>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="text-sm text-gray-500 dark:text-gray-400 italic">No options selected</p>
                  )}
                </div>

                <div className="border-t border-gray-200 dark:border-strokedark pt-4 space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600 dark:text-gray-400">Base Price</span>
                    <span className="font-medium text-gray-900 dark:text-white">{formatCurrency(basePrice)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600 dark:text-gray-400">Options Total</span>
                    <span className="font-medium text-gray-900 dark:text-white">{formatCurrency(optionsTotal)}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600 dark:text-gray-400">Subtotal</span>
                    <span className="font-medium text-gray-900 dark:text-white">{formatCurrency(subtotal)}</span>
                  </div>
                  {discount > 0 && (
                    <div className="flex justify-between text-sm text-green-600">
                      <span>Discount</span>
                      <span>-{formatCurrency(discount)}</span>
                    </div>
                  )}
                  {tradeInValue > 0 && (
                    <div className="flex justify-between text-sm text-green-600">
                      <span>Trade-In Value</span>
                      <span>-{formatCurrency(tradeInValue)}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600 dark:text-gray-400">Tax (8%)</span>
                    <span className="font-medium text-gray-900 dark:text-white">{formatCurrency(taxAmount)}</span>
                  </div>
                  <div className="flex justify-between text-base font-bold pt-2 border-t border-gray-300 dark:border-strokedark">
                    <span className="text-gray-900 dark:text-white">Total</span>
                    <span className="text-blue-600 dark:text-primary">{formatCurrency(totalPrice)}</span>
                  </div>
                </div>

                {uploadedFiles.length > 0 && (
                  <div className="border-t border-gray-200 dark:border-strokedark pt-4">
                    <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      Documents ({uploadedFiles.length})
                    </h4>
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                      {uploadedFiles.length} file{uploadedFiles.length > 1 ? 's' : ''} ready to upload
                    </p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
