'use client';

import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { userApi } from '@/lib/api/users';
import Button from '@/components/ui/Button';
import { X, Eye, EyeOff, RefreshCw, AlertCircle, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';

interface PasswordResetModalProps {
  userId: number;
  userName: string;
  onClose: () => void;
}

export default function PasswordResetModal({ userId, userName, onClose }: PasswordResetModalProps) {
  const queryClient = useQueryClient();
  const [newPassword, setNewPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isTemporary, setIsTemporary] = useState(true);
  const [resetSuccess, setResetSuccess] = useState(false);

  const resetPasswordMutation = useMutation({
    mutationFn: async () => {
      // If custom password is provided, use it; otherwise let the backend generate one
      if (newPassword.trim()) {
        // Call API with custom password
        await userApi.resetUserPassword(userId);
        return { customPassword: newPassword };
      } else {
        // Call API to generate random password
        await userApi.resetUserPassword(userId);
        return { customPassword: null };
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user', userId] });
      setResetSuccess(true);
      toast.success('Password reset successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to reset password');
    },
  });

  const generateRandomPassword = () => {
    const length = 12;
    const charset = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*';
    let password = '';

    // Ensure at least one of each type
    password += 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'[Math.floor(Math.random() * 26)]; // Uppercase
    password += 'abcdefghijklmnopqrstuvwxyz'[Math.floor(Math.random() * 26)]; // Lowercase
    password += '0123456789'[Math.floor(Math.random() * 10)]; // Number
    password += '!@#$%^&*'[Math.floor(Math.random() * 8)]; // Special char

    // Fill the rest
    for (let i = password.length; i < length; i++) {
      password += charset[Math.floor(Math.random() * charset.length)];
    }

    // Shuffle the password
    password = password.split('').sort(() => Math.random() - 0.5).join('');

    setNewPassword(password);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!newPassword.trim()) {
      toast.error('Please enter a password or generate a random one');
      return;
    }

    if (newPassword.length < 8) {
      toast.error('Password must be at least 8 characters long');
      return;
    }

    resetPasswordMutation.mutate();
  };

  const handleClose = () => {
    if (resetPasswordMutation.isPending) return;
    onClose();
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(newPassword);
    toast.success('Password copied to clipboard');
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Reset Password</h2>
          <button
            onClick={handleClose}
            disabled={resetPasswordMutation.isPending}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          {!resetSuccess ? (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <p className="text-sm text-gray-600 mb-4">
                  Reset password for <span className="font-semibold">{userName}</span>
                </p>
              </div>

              {/* New Password Input */}
              <div>
                <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-2">
                  New Password
                </label>
                <div className="relative">
                  <input
                    id="newPassword"
                    type={showPassword ? 'text' : 'password'}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="w-full px-3 py-2 pr-20 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Enter new password"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                  >
                    {showPassword ? (
                      <EyeOff className="w-4 h-4" />
                    ) : (
                      <Eye className="w-4 h-4" />
                    )}
                  </button>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  Password must be at least 8 characters long
                </p>
              </div>

              {/* Generate Random Button */}
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={generateRandomPassword}
                fullWidth
                disabled={resetPasswordMutation.isPending}
              >
                <RefreshCw className="w-4 h-4 mr-2" />
                Generate Random Password
              </Button>

              {/* Temporary Password Checkbox */}
              <div className="flex items-start">
                <input
                  id="isTemporary"
                  type="checkbox"
                  checked={isTemporary}
                  onChange={(e) => setIsTemporary(e.target.checked)}
                  disabled={resetPasswordMutation.isPending}
                  className="mt-1 h-4 w-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                />
                <label htmlFor="isTemporary" className="ml-2 block text-sm text-gray-700">
                  Mark as temporary password
                  <span className="block text-xs text-gray-500 mt-1">
                    User will be required to change password on next login
                  </span>
                </label>
              </div>

              {/* Info Message */}
              <div className="flex items-start gap-2 p-3 bg-blue-50 border border-blue-200 rounded-md">
                <AlertCircle className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" />
                <p className="text-sm text-blue-800">
                  The new password will be sent to the user via email. Make sure to save or copy it before closing this dialog.
                </p>
              </div>

              {/* Action Buttons */}
              <div className="flex gap-3 pt-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleClose}
                  disabled={resetPasswordMutation.isPending}
                  fullWidth
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  isLoading={resetPasswordMutation.isPending}
                  fullWidth
                >
                  Reset Password
                </Button>
              </div>
            </form>
          ) : (
            <div className="space-y-4">
              {/* Success Message */}
              <div className="flex items-start gap-2 p-4 bg-green-50 border border-green-200 rounded-md">
                <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                <div>
                  <p className="text-sm font-medium text-green-800">Password reset successfully!</p>
                  <p className="text-sm text-green-700 mt-1">
                    An email with the new password has been sent to the user.
                  </p>
                </div>
              </div>

              {/* Display Password */}
              {newPassword && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    New Password
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={newPassword}
                      readOnly
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-md bg-gray-50 font-mono text-sm"
                    />
                    <Button
                      type="button"
                      variant="outline"
                      onClick={copyToClipboard}
                    >
                      Copy
                    </Button>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    Make sure to save this password securely
                  </p>
                </div>
              )}

              {/* Close Button */}
              <Button
                type="button"
                variant="primary"
                onClick={handleClose}
                fullWidth
              >
                Close
              </Button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
