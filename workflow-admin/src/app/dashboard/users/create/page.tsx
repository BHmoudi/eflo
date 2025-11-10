'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useMutation, useQuery } from '@tanstack/react-query';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import Button from '@/components/ui/Button';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { userApi, CreateUserDto } from '@/lib/api/users';
import { roleApi } from '@/lib/api/roles';
import { COUNTRIES } from '@/lib/constants/countries';
import { ArrowLeft, User as UserIcon, Briefcase, Phone, MapPin, Calendar, Lock } from 'lucide-react';
import toast from 'react-hot-toast';

type TabType = 'personal' | 'employee' | 'contact' | 'address' | 'employment' | 'account';

export default function CreateUserPage() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<TabType>('personal');
  const [formData, setFormData] = useState<CreateUserDto>({
    email: '',
    firstName: '',
    lastName: '',
    employeeNumber: '',
    userIpn: '',
    phoneNumber: '',
    mobileNumber: '',
    officeExtension: '',
    jobTitle: '',
    department: '',
    addressLine1: '',
    addressLine2: '',
    postalCode: '',
    city: '',
    country: 'FR',
    hireDate: '',
    profilePictureUrl: '',
    bio: '',
    password: '',
    role: '',
    phone: '',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleApi.getRoles(),
  });

  const createMutation = useMutation({
    mutationFn: (data: CreateUserDto) => userApi.createUser(data),
    onSuccess: () => {
      toast.success('User created successfully');
      router.push('/dashboard/users');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create user');
    },
  });

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.firstName?.trim()) {
      newErrors.firstName = 'First name is required';
    }
    if (!formData.lastName?.trim()) {
      newErrors.lastName = 'Last name is required';
    }
    if (!formData.email?.trim()) {
      newErrors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
    }
    if (formData.phoneNumber && !/^[+]?[(]?[0-9]{1,4}[)]?[-\s./0-9]*$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = 'Invalid phone number format';
    }
    if (formData.mobileNumber && !/^[+]?[(]?[0-9]{1,4}[)]?[-\s./0-9]*$/.test(formData.mobileNumber)) {
      newErrors.mobileNumber = 'Invalid mobile number format';
    }
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Invalid email format';
    }
    if (formData.postalCode && formData.postalCode.length > 10) {
      newErrors.postalCode = 'Postal code is too long';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const tabs = [
    { id: 'personal' as TabType, label: 'Personal Info', icon: UserIcon },
    { id: 'employee' as TabType, label: 'Employee Info', icon: Briefcase },
    { id: 'contact' as TabType, label: 'Contact Info', icon: Phone },
    { id: 'address' as TabType, label: 'Address', icon: MapPin },
    { id: 'employment' as TabType, label: 'Employment', icon: Calendar },
    { id: 'account' as TabType, label: 'Account', icon: Lock },
  ];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      createMutation.mutate(formData);
    }
  };

  const handleCancel = () => {
    router.push('/dashboard/users');
  };

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb
        items={[
          { label: 'Dashboard', href: '/dashboard' },
          { label: 'Users', href: '/dashboard/users' },
          { label: 'Create User' },
        ]}
      />

      <div className="flex items-center gap-3">
        <Button
          variant="ghost"
          onClick={handleCancel}
          className="p-2"
        >
          <ArrowLeft className="w-5 h-5" />
        </Button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Create New User</h1>
          <p className="text-gray-600 mt-1">Add a new user to the system</p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>User Information</CardTitle>
        </CardHeader>
        <CardContent>
          {/* Tabs Navigation */}
          <div className="border-b border-gray-200 mb-6">
            <nav className="-mb-px flex space-x-8 overflow-x-auto">
              {tabs.map((tab) => {
                const Icon = tab.icon;
                return (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => setActiveTab(tab.id)}
                    className={`
                      flex items-center gap-2 py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap
                      ${activeTab === tab.id
                        ? 'border-blue-500 text-blue-600'
                        : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                      }
                    `}
                  >
                    <Icon className="w-4 h-4" />
                    {tab.label}
                  </button>
                );
              })}
            </nav>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* Personal Info Tab */}
            {activeTab === 'personal' && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <Input
                    label="First Name"
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    placeholder="John"
                    error={errors.firstName}
                    required
                  />
                  <Input
                    label="Last Name"
                    value={formData.lastName}
                    onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                    placeholder="Doe"
                    error={errors.lastName}
                    required
                  />
                </div>

                <Input
                  label="Email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="john.doe@example.com"
                  error={errors.email}
                  required
                />

                <Input
                  label="Profile Picture URL"
                  type="url"
                  value={formData.profilePictureUrl}
                  onChange={(e) => setFormData({ ...formData, profilePictureUrl: e.target.value })}
                  placeholder="https://example.com/photo.jpg"
                  helperText="Optional"
                />

                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">
                    Bio
                  </label>
                  <textarea
                    value={formData.bio}
                    onChange={(e) => setFormData({ ...formData, bio: e.target.value })}
                    placeholder="Brief description about the user..."
                    rows={4}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
            )}

            {/* Employee Info Tab */}
            {activeTab === 'employee' && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <Input
                    label="Employee Number"
                    value={formData.employeeNumber}
                    onChange={(e) => setFormData({ ...formData, employeeNumber: e.target.value })}
                    placeholder="EMP12345"
                    helperText="Optional"
                  />
                  <Input
                    label="User IPN"
                    value={formData.userIpn}
                    onChange={(e) => setFormData({ ...formData, userIpn: e.target.value })}
                    placeholder="IPN12345"
                    helperText="Optional"
                  />
                </div>

                <Input
                  label="Job Title"
                  value={formData.jobTitle}
                  onChange={(e) => setFormData({ ...formData, jobTitle: e.target.value })}
                  placeholder="Senior Manager"
                  helperText="Optional"
                />

                <Input
                  label="Department"
                  value={formData.department}
                  onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                  placeholder="Sales"
                  helperText="Optional"
                />
              </div>
            )}

            {/* Contact Info Tab */}
            {activeTab === 'contact' && (
              <div className="space-y-6">
                <Input
                  label="Phone Number"
                  type="tel"
                  value={formData.phoneNumber}
                  onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                  placeholder="+33 1 23 45 67 89"
                  error={errors.phoneNumber}
                  helperText="Optional"
                />

                <Input
                  label="Mobile Number"
                  type="tel"
                  value={formData.mobileNumber}
                  onChange={(e) => setFormData({ ...formData, mobileNumber: e.target.value })}
                  placeholder="+33 6 12 34 56 78"
                  error={errors.mobileNumber}
                  helperText="Optional"
                />

                <Input
                  label="Office Extension"
                  type="tel"
                  value={formData.officeExtension}
                  onChange={(e) => setFormData({ ...formData, officeExtension: e.target.value })}
                  placeholder="1234"
                  helperText="Optional"
                />
              </div>
            )}

            {/* Address Tab */}
            {activeTab === 'address' && (
              <div className="space-y-6">
                <Input
                  label="Address Line 1"
                  value={formData.addressLine1}
                  onChange={(e) => setFormData({ ...formData, addressLine1: e.target.value })}
                  placeholder="123 Main Street"
                  helperText="Optional"
                />

                <Input
                  label="Address Line 2"
                  value={formData.addressLine2}
                  onChange={(e) => setFormData({ ...formData, addressLine2: e.target.value })}
                  placeholder="Apartment 4B"
                  helperText="Optional"
                />

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <Input
                    label="Postal Code"
                    value={formData.postalCode}
                    onChange={(e) => setFormData({ ...formData, postalCode: e.target.value })}
                    placeholder="75001"
                    error={errors.postalCode}
                    helperText="Optional"
                  />

                  <Input
                    label="City"
                    value={formData.city}
                    onChange={(e) => setFormData({ ...formData, city: e.target.value })}
                    placeholder="Paris"
                    helperText="Optional"
                  />

                  <div className="space-y-2">
                    <label className="block text-sm font-medium text-gray-700">
                      Country
                    </label>
                    <Select
                      value={formData.country}
                      onChange={(e) => setFormData({ ...formData, country: e.target.value })}
                      className="w-full"
                    >
                      {COUNTRIES.map((country) => (
                        <option key={country.code} value={country.code}>
                          {country.name}
                        </option>
                      ))}
                    </Select>
                  </div>
                </div>
              </div>
            )}

            {/* Employment Tab */}
            {activeTab === 'employment' && (
              <div className="space-y-6">
                <Input
                  label="Hire Date"
                  type="date"
                  value={formData.hireDate}
                  onChange={(e) => setFormData({ ...formData, hireDate: e.target.value })}
                  helperText="Optional"
                />

                <div className="bg-blue-50 border border-blue-200 rounded-md p-4">
                  <p className="text-sm text-blue-800">
                    <span className="font-medium">Note:</span> Business unit assignments and manager hierarchy can be configured after user creation.
                  </p>
                </div>
              </div>
            )}

            {/* Account Tab */}
            {activeTab === 'account' && (
              <div className="space-y-6">
                <div className="space-y-2">
                  <label className="block text-sm font-medium text-gray-700">
                    Role
                  </label>
                  <Select
                    value={formData.role}
                    onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                    className="w-full"
                  >
                    <option value="">Select a role</option>
                    {roles.map((role) => (
                      <option key={role.code} value={role.code}>
                        {role.name}
                      </option>
                    ))}
                  </Select>
                  {errors.role && <p className="text-sm text-red-600">{errors.role}</p>}
                </div>

                <Input
                  label="Password"
                  type="password"
                  value={formData.password}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  placeholder="Leave blank for auto-generated password"
                  helperText="Optional - A secure password will be generated if left blank"
                />

                <div className="bg-gray-50 border border-gray-200 rounded-md p-4">
                  <p className="text-sm text-gray-600">
                    <span className="font-medium">Security:</span> If no password is provided, a secure random password will be generated and sent to the user's email.
                  </p>
                </div>
              </div>
            )}

            {/* Form Actions */}
            <div className="flex items-center justify-between gap-3 pt-4 border-t border-gray-200">
              <div className="flex gap-3">
                <Button
                  type="submit"
                  variant="primary"
                  isLoading={createMutation.isPending}
                >
                  Create User
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleCancel}
                  disabled={createMutation.isPending}
                >
                  Cancel
                </Button>
              </div>

              {activeTab !== 'account' && (
                <Button
                  type="button"
                  variant="ghost"
                  onClick={() => {
                    const currentIndex = tabs.findIndex(t => t.id === activeTab);
                    if (currentIndex < tabs.length - 1) {
                      setActiveTab(tabs[currentIndex + 1].id);
                    }
                  }}
                >
                  Next
                </Button>
              )}
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
