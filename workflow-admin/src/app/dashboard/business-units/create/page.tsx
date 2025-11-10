'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useMutation } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import Button from '@/components/ui/Button';
import Breadcrumb from '@/components/layout/Breadcrumb';
import UserSelector from '@/components/ui/UserSelector';
import { businessUnitApi, CreateBusinessUnitDto } from '@/lib/api/business-units';
import { COUNTRIES } from '@/lib/constants/countries';
import { ArrowLeft, Save, Building2, MapPin, Phone, Mail, Globe, DollarSign } from 'lucide-react';
import toast from 'react-hot-toast';
import { useTranslation } from '@/hooks/useTranslation';

const BUSINESS_UNIT_TYPES = [
  { value: 'HEADQUARTERS', label: 'Headquarters' },
  { value: 'REGIONAL_OFFICE', label: 'Regional Office' },
  { value: 'BRANCH', label: 'Branch' },
  { value: 'WAREHOUSE', label: 'Warehouse' },
  { value: 'STORE', label: 'Store' },
  { value: 'FACTORY', label: 'Factory' },
  { value: 'SERVICE_CENTER', label: 'Service Center' },
];

export default function CreateBusinessUnitPage() {
  const router = useRouter();
  const { t } = useTranslation();
  const [formData, setFormData] = useState<CreateBusinessUnitDto>({
    code: '',
    name: '',
    legalName: '',
    type: 'BRANCH',
    regionCode: '',
    regionName: '',
    rrfCode: '',
    managerId: undefined,
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

  const createMutation = useMutation({
    mutationFn: (data: CreateBusinessUnitDto) => businessUnitApi.createBusinessUnit(data),
    onSuccess: () => {
      toast.success(t('businessUnits.createSuccess'));
      router.push('/dashboard/business-units');
    },
    onError: (error: any) => {
      toast.error(error.message || t('businessUnits.createError'));
    },
  });

  const handleInputChange = (field: keyof CreateBusinessUnitDto, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  const handleManagerChange = (userId: number | number[] | undefined) => {
    setFormData((prev) => ({ ...prev, managerId: typeof userId === 'number' ? userId : undefined }));
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
      toast.error(t('common.fillRequiredFields'));
      return;
    }

    // Convert string coordinates to numbers if provided
    const submitData = {
      ...formData,
      latitude: formData.latitude ? Number(formData.latitude) : undefined,
      longitude: formData.longitude ? Number(formData.longitude) : undefined,
    };

    createMutation.mutate(submitData);
  };

  const handleCancel = () => {
    router.push('/dashboard/business-units');
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: t('common.dashboard'), href: '/dashboard' },
          { label: t('businessUnits.title'), href: '/dashboard/business-units' },
          { label: t('common.create') },
        ]}
      />

      <div className="flex items-center gap-3">
        <button
          onClick={handleCancel}
          className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          title={t('businessUnits.backToList')}
        >
          <ArrowLeft className="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{t('businessUnits.createBusinessUnit')}</h1>
          <p className="text-gray-600 mt-1">{t('businessUnits.createDescription')}</p>
        </div>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Building2 className="w-5 h-5 text-blue-600" />
              {t('businessUnits.basicInformation')}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label={t('businessUnits.code')}
                value={formData.code}
                onChange={(e) => handleInputChange('code', e.target.value)}
                placeholder="e.g., BU001"
                required
                helperText="Unique identifier for this business unit"
              />
              <Select
                label={t('businessUnits.type')}
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
              label={t('businessUnits.name')}
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

        {/* Manager Assignment */}
        <Card>
          <CardHeader>
            <CardTitle>Manager Assignment</CardTitle>
          </CardHeader>
          <CardContent>
            <UserSelector
              label="Assign Manager (Optional)"
              value={formData.managerId}
              onChange={handleManagerChange}
              placeholder="Select a manager for this business unit"
              activeOnly={true}
              helperText="You can assign a manager now or later"
            />
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
            disabled={createMutation.isPending}
          >
            {t('common.cancel')}
          </Button>
          <Button
            type="submit"
            variant="primary"
            disabled={createMutation.isPending}
            className="flex items-center gap-2"
          >
            <Save className="w-4 h-4" />
            {createMutation.isPending ? t('forms.creating') : t('businessUnits.createBusinessUnit')}
          </Button>
        </div>
      </form>
    </div>
  );
}
