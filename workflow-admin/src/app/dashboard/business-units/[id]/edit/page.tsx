'use client';

import React, { useState, useEffect } from 'react';
import { useRouter, useParams, useSearchParams } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import Button from '@/components/ui/Button';
import Breadcrumb from '@/components/layout/Breadcrumb';
import UserSelector from '@/components/ui/UserSelector';
import { businessUnitApi, UpdateBusinessUnitDto, BusinessUnit } from '@/lib/api/business-units';
import { COUNTRIES } from '@/lib/constants/countries';
import { ArrowLeft, Save, Building2, MapPin, Phone, Mail, Globe, DollarSign, UserPlus, UserX } from 'lucide-react';
import toast from 'react-hot-toast';

const BUSINESS_UNIT_TYPES = [
  { value: 'HEADQUARTERS', label: 'Headquarters' },
  { value: 'REGIONAL_OFFICE', label: 'Regional Office' },
  { value: 'BRANCH', label: 'Branch' },
  { value: 'WAREHOUSE', label: 'Warehouse' },
  { value: 'STORE', label: 'Store' },
  { value: 'FACTORY', label: 'Factory' },
  { value: 'SERVICE_CENTER', label: 'Service Center' },
];

export default function EditBusinessUnitPage() {
  const router = useRouter();
  const params = useParams();
  const searchParams = useSearchParams();
  const businessUnitId = Number(params.id);
  const queryClient = useQueryClient();

  const [activeTab, setActiveTab] = useState<'details' | 'manager'>('details');
  const [formData, setFormData] = useState<UpdateBusinessUnitDto>({
    code: '',
    name: '',
    legalName: '',
    type: 'BRANCH',
    regionCode: '',
    regionName: '',
    rrfCode: '',
    phone: '',
    fax: '',
    email: '',
    website: '',
    addressLine1: '',
    addressLine2: '',
    postalCode: '',
    city: '',
    country: '',
    latitude: undefined,
    longitude: undefined,
    siret: '',
    vatNumber: '',
    brands: [],
    openingHours: '',
  });

  const [brandsInput, setBrandsInput] = useState('');
  const [newManagerId, setNewManagerId] = useState<number | undefined>(undefined);

  // Load business unit data
  const { data: businessUnit, isLoading } = useQuery({
    queryKey: ['business-unit', businessUnitId],
    queryFn: () => businessUnitApi.getBusinessUnitById(businessUnitId),
    enabled: !!businessUnitId,
  });

  // Check if tab parameter is set in URL
  useEffect(() => {
    const tab = searchParams.get('tab');
    if (tab === 'manager') {
      setActiveTab('manager');
    }
  }, [searchParams]);

  // Populate form when data is loaded
  useEffect(() => {
    if (businessUnit) {
      setFormData({
        code: businessUnit.code || '',
        name: businessUnit.name || '',
        legalName: businessUnit.legalName || '',
        type: businessUnit.type,
        regionCode: businessUnit.regionCode || '',
        regionName: businessUnit.regionName || '',
        rrfCode: businessUnit.rrfCode || '',
        phone: businessUnit.phone || '',
        fax: businessUnit.fax || '',
        email: businessUnit.email || '',
        website: businessUnit.website || '',
        addressLine1: businessUnit.addressLine1 || '',
        addressLine2: businessUnit.addressLine2 || '',
        postalCode: businessUnit.postalCode || '',
        city: businessUnit.city || '',
        country: businessUnit.country || '',
        latitude: businessUnit.latitude,
        longitude: businessUnit.longitude,
        siret: businessUnit.siret || '',
        vatNumber: businessUnit.vatNumber || '',
        brands: businessUnit.brands || [],
        openingHours: businessUnit.openingHours || '',
      });

      if (businessUnit.brands && businessUnit.brands.length > 0) {
        setBrandsInput(businessUnit.brands.join(', '));
      }

      setNewManagerId(businessUnit.managerId);
    }
  }, [businessUnit]);

  const updateMutation = useMutation({
    mutationFn: (data: UpdateBusinessUnitDto) => businessUnitApi.updateBusinessUnit(businessUnitId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-unit', businessUnitId] });
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      toast.success('Business unit updated successfully');
      router.push('/dashboard/business-units');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to update business unit');
    },
  });

  const assignManagerMutation = useMutation({
    mutationFn: (managerId: number) => businessUnitApi.assignManager(businessUnitId, managerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-unit', businessUnitId] });
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      toast.success('Manager assigned successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to assign manager');
    },
  });

  const removeManagerMutation = useMutation({
    mutationFn: () => businessUnitApi.removeManager(businessUnitId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['business-unit', businessUnitId] });
      queryClient.invalidateQueries({ queryKey: ['business-units'] });
      setNewManagerId(undefined);
      toast.success('Manager removed successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to remove manager');
    },
  });

  const handleInputChange = (field: keyof UpdateBusinessUnitDto, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleBrandsChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setBrandsInput(value);
    const brandsArray = value.split(',').map((brand) => brand.trim()).filter(Boolean);
    setFormData((prev) => ({ ...prev, brands: brandsArray }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    // Validate required fields
    if (!formData.code || !formData.name || !formData.type) {
      toast.error('Please fill in all required fields');
      return;
    }

    // Convert string coordinates to numbers if provided
    const submitData = {
      ...formData,
      latitude: formData.latitude ? Number(formData.latitude) : undefined,
      longitude: formData.longitude ? Number(formData.longitude) : undefined,
    };

    updateMutation.mutate(submitData);
  };

  const handleAssignManager = () => {
    if (newManagerId) {
      assignManagerMutation.mutate(newManagerId);
    }
  };

  const handleRemoveManager = () => {
    if (confirm('Are you sure you want to remove the current manager?')) {
      removeManagerMutation.mutate();
    }
  };

  const handleCancel = () => {
    router.push('/dashboard/business-units');
  };

  if (isLoading) {
    return (
      <div className="p-6">
        <div className="text-center py-12 text-gray-500">Loading business unit...</div>
      </div>
    );
  }

  if (!businessUnit) {
    return (
      <div className="p-6">
        <div className="text-center py-12 text-red-500">Business unit not found</div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Business Units', href: '/dashboard/business-units' },
          { label: businessUnit.name },
          { label: 'Edit' },
        ]}
      />

      <div className="flex items-center gap-3">
        <button
          onClick={handleCancel}
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          title="Back to Business Units"
        >
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Edit Business Unit</h1>
          <p className="text-gray-600 mt-1">{businessUnit.name} ({businessUnit.code})</p>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200">
        <nav className="flex gap-6">
          <button
            onClick={() => setActiveTab('details')}
            className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'details'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Business Unit Details
          </button>
          <button
            onClick={() => setActiveTab('manager')}
            className={`py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'manager'
                ? 'border-blue-600 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            Manager Assignment
          </button>
        </nav>
      </div>

      {/* Business Unit Details Tab */}
      {activeTab === 'details' && (
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Basic Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Building2 className="w-5 h-5 text-blue-600" />
                Basic Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Business Unit Code"
                  value={formData.code}
                  onChange={(e) => handleInputChange('code', e.target.value)}
                  placeholder="e.g., BU001"
                  required
                  helperText="Unique identifier for this business unit"
                />
                <Select
                  label="Type"
                  value={formData.type}
                  onChange={(e) => handleInputChange('type', e.target.value)}
                  required
                >
                  {BUSINESS_UNIT_TYPES.map((type) => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </Select>
              </div>

              <Input
                label="Business Unit Name"
                value={formData.name}
                onChange={(e) => handleInputChange('name', e.target.value)}
                placeholder="e.g., Paris Branch"
                required
              />

              <Input
                label="Legal Name"
                value={formData.legalName}
                onChange={(e) => handleInputChange('legalName', e.target.value)}
                placeholder="Official legal name"
                helperText="Optional: Legal entity name if different from business name"
              />
            </CardContent>
          </Card>

          {/* Regional Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="w-5 h-5 text-green-600" />
                Regional Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Input
                  label="Region Code"
                  value={formData.regionCode}
                  onChange={(e) => handleInputChange('regionCode', e.target.value)}
                  placeholder="e.g., EU-FR"
                  helperText="Geographic region identifier"
                />
                <Input
                  label="Region Name"
                  value={formData.regionName}
                  onChange={(e) => handleInputChange('regionName', e.target.value)}
                  placeholder="e.g., Europe - France"
                />
                <Input
                  label="RRF Code"
                  value={formData.rrfCode}
                  onChange={(e) => handleInputChange('rrfCode', e.target.value)}
                  placeholder="e.g., RRF123"
                  helperText="Regional reference code"
                />
              </div>
            </CardContent>
          </Card>

          {/* Contact Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Phone className="w-5 h-5 text-purple-600" />
                Contact Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Phone"
                  type="tel"
                  value={formData.phone}
                  onChange={(e) => handleInputChange('phone', e.target.value)}
                  placeholder="+33 1 23 45 67 89"
                />
                <Input
                  label="Fax"
                  type="tel"
                  value={formData.fax}
                  onChange={(e) => handleInputChange('fax', e.target.value)}
                  placeholder="+33 1 23 45 67 90"
                />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => handleInputChange('email', e.target.value)}
                  placeholder="contact@example.com"
                />
                <Input
                  label="Website"
                  type="url"
                  value={formData.website}
                  onChange={(e) => handleInputChange('website', e.target.value)}
                  placeholder="https://www.example.com"
                />
              </div>
            </CardContent>
          </Card>

          {/* Address */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <MapPin className="w-5 h-5 text-red-600" />
                Physical Address
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <Input
                label="Address Line 1"
                value={formData.addressLine1}
                onChange={(e) => handleInputChange('addressLine1', e.target.value)}
                placeholder="Street address"
              />

              <Input
                label="Address Line 2"
                value={formData.addressLine2}
                onChange={(e) => handleInputChange('addressLine2', e.target.value)}
                placeholder="Apartment, suite, unit, etc. (optional)"
              />

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <Input
                  label="Postal Code"
                  value={formData.postalCode}
                  onChange={(e) => handleInputChange('postalCode', e.target.value)}
                  placeholder="75001"
                />
                <Input
                  label="City"
                  value={formData.city}
                  onChange={(e) => handleInputChange('city', e.target.value)}
                  placeholder="Paris"
                />
                <Select
                  label="Country"
                  value={formData.country}
                  onChange={(e) => handleInputChange('country', e.target.value)}
                >
                  <option value="">Select country</option>
                  {COUNTRIES.map((country) => (
                    <option key={country.code} value={country.code}>
                      {country.name}
                    </option>
                  ))}
                </Select>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="Latitude"
                  type="number"
                  step="any"
                  value={formData.latitude || ''}
                  onChange={(e) => handleInputChange('latitude', e.target.value ? parseFloat(e.target.value) : undefined)}
                  placeholder="48.8566"
                  helperText="GPS coordinate"
                />
                <Input
                  label="Longitude"
                  type="number"
                  step="any"
                  value={formData.longitude || ''}
                  onChange={(e) => handleInputChange('longitude', e.target.value ? parseFloat(e.target.value) : undefined)}
                  placeholder="2.3522"
                  helperText="GPS coordinate"
                />
              </div>
            </CardContent>
          </Card>

          {/* Financial Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <DollarSign className="w-5 h-5 text-yellow-600" />
                Financial Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Input
                  label="SIRET"
                  value={formData.siret}
                  onChange={(e) => handleInputChange('siret', e.target.value)}
                  placeholder="123 456 789 00010"
                  helperText="French business registration number"
                />
                <Input
                  label="VAT Number"
                  value={formData.vatNumber}
                  onChange={(e) => handleInputChange('vatNumber', e.target.value)}
                  placeholder="FR12345678901"
                  helperText="European VAT identification"
                />
              </div>
            </CardContent>
          </Card>

          {/* Additional Information */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Globe className="w-5 h-5 text-indigo-600" />
                Additional Information
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <Input
                label="Brands"
                value={brandsInput}
                onChange={handleBrandsChange}
                placeholder="Brand1, Brand2, Brand3"
                helperText="Comma-separated list of brands associated with this business unit"
              />

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Opening Hours
                </label>
                <textarea
                  value={formData.openingHours}
                  onChange={(e) => handleInputChange('openingHours', e.target.value)}
                  placeholder="Monday-Friday: 9:00 AM - 6:00 PM&#10;Saturday: 10:00 AM - 4:00 PM&#10;Sunday: Closed"
                  rows={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                <p className="mt-1 text-sm text-gray-500">Business hours and schedule</p>
              </div>
            </CardContent>
          </Card>

          {/* Action Buttons */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-200">
            <Button
              type="button"
              variant="outline"
              onClick={handleCancel}
              disabled={updateMutation.isPending}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={updateMutation.isPending}
              className="flex items-center gap-2"
            >
              <Save className="w-4 h-4" />
              {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
            </Button>
          </div>
        </form>
      )}

      {/* Manager Assignment Tab */}
      {activeTab === 'manager' && (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Current Manager</CardTitle>
            </CardHeader>
            <CardContent>
              {businessUnit.managerId && businessUnit.managerName ? (
                <div className="flex items-center justify-between p-4 bg-blue-50 border border-blue-200 rounded-lg">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-blue-600 rounded-full flex items-center justify-center">
                      <span className="text-white font-semibold">
                        {businessUnit.managerName.charAt(0)}
                      </span>
                    </div>
                    <div>
                      <p className="font-medium text-gray-900">{businessUnit.managerName}</p>
                      <p className="text-sm text-gray-600">Manager ID: {businessUnit.managerId}</p>
                    </div>
                  </div>
                  <Button
                    variant="outline"
                    onClick={handleRemoveManager}
                    disabled={removeManagerMutation.isPending}
                    className="flex items-center gap-2"
                  >
                    <UserX className="w-4 h-4" />
                    Remove Manager
                  </Button>
                </div>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <p>No manager assigned to this business unit</p>
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Assign New Manager</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <UserSelector
                label="Select Manager"
                value={newManagerId}
                onChange={(userId) => setNewManagerId(typeof userId === 'number' ? userId : undefined)}
                placeholder="Select a user to assign as manager"
                activeOnly={true}
                helperText="Choose a user to manage this business unit"
              />

              <div className="flex justify-end">
                <Button
                  variant="primary"
                  onClick={handleAssignManager}
                  disabled={!newManagerId || assignManagerMutation.isPending}
                  className="flex items-center gap-2"
                >
                  <UserPlus className="w-4 h-4" />
                  {assignManagerMutation.isPending ? 'Assigning...' : 'Assign Manager'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}
