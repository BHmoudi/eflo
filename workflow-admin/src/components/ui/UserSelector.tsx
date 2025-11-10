'use client';

import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import Select from './Select';
import Input from './Input';
import { userApi, User } from '@/lib/api/users';
import { Search, User as UserIcon, X } from 'lucide-react';

export interface UserSelectorProps {
  value?: number | number[];
  onChange: (userId: number | number[] | undefined) => void;
  label?: string;
  multiple?: boolean;
  required?: boolean;
  placeholder?: string;
  filterByRole?: string;
  activeOnly?: boolean;
  helperText?: string;
  error?: string;
}

export default function UserSelector({
  value,
  onChange,
  label = 'Assign User',
  multiple = false,
  required = false,
  placeholder = 'Select user...',
  filterByRole,
  activeOnly = true,
  helperText,
  error,
}: UserSelectorProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedUsers, setSelectedUsers] = useState<User[]>([]);

  // Fetch users based on filters
  const { data: allUsersData } = useQuery({
    queryKey: ['users', 0, 100],
    queryFn: () => userApi.getUsers(0, 100),
  });

  const { data: activeUsers } = useQuery({
    queryKey: ['users', 'active'],
    queryFn: () => userApi.getActiveUsers(),
    enabled: activeOnly,
  });

  const { data: roleUsers } = useQuery({
    queryKey: ['users', 'role', filterByRole],
    queryFn: () => userApi.getUsersByRole(filterByRole!),
    enabled: !!filterByRole,
  });

  // Determine which user list to use
  const users = filterByRole ? roleUsers : activeOnly ? activeUsers : allUsersData?.content || [];

  // Update selected users when value changes
  useEffect(() => {
    if (!users) return;

    if (multiple && Array.isArray(value)) {
      const selected = users.filter((user) => value.includes(user.id));
      setSelectedUsers(selected);
    } else if (!multiple && typeof value === 'number') {
      const selected = users.find((user) => user.id === value);
      setSelectedUsers(selected ? [selected] : []);
    } else {
      setSelectedUsers([]);
    }
  }, [value, users, multiple]);

  const filteredUsers = users?.filter((user) => {
    const matchesSearch =
      user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.lastName?.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesSearch;
  });

  const handleSelectUser = (user: User) => {
    if (multiple) {
      const currentValues = Array.isArray(value) ? value : [];
      if (currentValues.includes(user.id)) {
        const newValues = currentValues.filter((id) => id !== user.id);
        onChange(newValues.length > 0 ? newValues : undefined);
      } else {
        onChange([...currentValues, user.id]);
      }
    } else {
      onChange(user.id);
      setIsDropdownOpen(false);
    }
  };

  const handleRemoveUser = (userId: number, e: React.MouseEvent) => {
    e.stopPropagation();
    if (multiple) {
      const currentValues = Array.isArray(value) ? value : [];
      const newValues = currentValues.filter((id) => id !== userId);
      onChange(newValues.length > 0 ? newValues : undefined);
    } else {
      onChange(undefined);
    }
  };

  const isSelected = (userId: number) => {
    if (multiple) {
      return Array.isArray(value) && value.includes(userId);
    }
    return value === userId;
  };

  return (
    <div className="w-full">
      {label && (
        <label className="block text-sm font-medium text-gray-700 mb-1">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}

      <div className="relative">
        <div
          className={`flex flex-wrap gap-2 min-h-[40px] w-full rounded-md border bg-white px-3 py-2 text-sm cursor-pointer
            focus-within:outline-none focus-within:ring-2 focus-within:ring-blue-500 focus-within:border-transparent
            ${error ? 'border-red-500' : 'border-gray-300'}
          `}
          onClick={() => setIsDropdownOpen(!isDropdownOpen)}
        >
          {selectedUsers.length > 0 ? (
            selectedUsers.map((user) => (
              <span
                key={user.id}
                className="inline-flex items-center gap-1 px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs"
              >
                <UserIcon className="w-3 h-3" />
                {user.firstName} {user.lastName}
                <button
                  type="button"
                  onClick={(e) => handleRemoveUser(user.id, e)}
                  className="hover:bg-blue-200 rounded-full p-0.5"
                >
                  <X className="w-3 h-3" />
                </button>
              </span>
            ))
          ) : (
            <span className="text-gray-400">{placeholder}</span>
          )}
        </div>

        {isDropdownOpen && (
          <>
            <div
              className="fixed inset-0 z-10"
              onClick={() => setIsDropdownOpen(false)}
            />
            <div className="absolute z-20 w-full mt-1 bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-hidden">
              <div className="p-2 border-b border-gray-200">
                <div className="relative">
                  <Search className="absolute left-2 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    type="text"
                    className="w-full pl-8 pr-3 py-1.5 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Search users..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onClick={(e) => e.stopPropagation()}
                  />
                </div>
              </div>

              <div className="overflow-y-auto max-h-48">
                {filteredUsers && filteredUsers.length > 0 ? (
                  filteredUsers.map((user) => (
                    <div
                      key={user.id}
                      className={`flex items-center gap-2 px-3 py-2 hover:bg-gray-100 cursor-pointer ${
                        isSelected(user.id) ? 'bg-blue-50' : ''
                      }`}
                      onClick={() => handleSelectUser(user)}
                    >
                      <input
                        type={multiple ? 'checkbox' : 'radio'}
                        checked={isSelected(user.id)}
                        onChange={() => {}}
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                      <div className="flex-1">
                        <div className="flex items-center gap-2">
                          <UserIcon className="w-4 h-4 text-gray-400" />
                          <span className="text-sm font-medium text-gray-900">
                            {user.firstName} {user.lastName}
                          </span>
                        </div>
                        <div className="text-xs text-gray-500">{user.email}</div>
                        {user.role && (
                          <span className="text-xs px-1.5 py-0.5 rounded bg-blue-100 text-blue-800">
                            {user.role}
                          </span>
                        )}
                      </div>
                      {isSelected(user.id) && (
                        <span className="text-blue-600 text-sm">✓</span>
                      )}
                    </div>
                  ))
                ) : (
                  <div className="px-3 py-6 text-center text-sm text-gray-500">
                    No users found
                  </div>
                )}
              </div>
            </div>
          </>
        )}
      </div>

      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
      {helperText && !error && <p className="mt-1 text-sm text-gray-500">{helperText}</p>}
    </div>
  );
}
