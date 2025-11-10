'use client';

import React, { ReactNode } from 'react';
import { cn } from '@/lib/utils';
import Button from '@/components/ui/Button';
import { X, AlertTriangle } from 'lucide-react';

export interface ConfirmDialogProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string | ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'danger' | 'primary' | 'warning';
  isLoading?: boolean;
  icon?: ReactNode;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  isOpen,
  onClose,
  onConfirm,
  title,
  message,
  confirmLabel = 'Confirm',
  cancelLabel = 'Cancel',
  variant = 'primary',
  isLoading = false,
  icon
}) => {
  if (!isOpen) return null;

  const variantColors = {
    danger: {
      icon: 'bg-error-100 text-error-600 dark:bg-error-500/15 dark:text-error-500',
      button: 'danger' as const,
    },
    primary: {
      icon: 'bg-brand-100 text-brand-600 dark:bg-brand-500/15 dark:text-brand-500',
      button: 'primary' as const,
    },
    warning: {
      icon: 'bg-warning-100 text-warning-600 dark:bg-warning-500/15 dark:text-orange-500',
      button: 'primary' as const,
    },
  };

  const colors = variantColors[variant];

  const handleBackdropClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if (e.target === e.currentTarget && !isLoading) {
      onClose();
    }
  };

  return (
    <div
      className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4"
      onClick={handleBackdropClick}
    >
      <div
        className={cn(
          'bg-white dark:bg-gray-900 rounded-lg shadow-xl max-w-md w-full',
          'border border-gray-200 dark:border-gray-800'
        )}
      >
        {/* Header */}
        <div className="flex items-start justify-between p-6 border-b border-gray-200 dark:border-gray-800">
          <div className="flex items-start gap-4 flex-1">
            {/* Icon */}
            <div
              className={cn(
                'flex items-center justify-center rounded-full h-10 w-10 flex-shrink-0',
                colors.icon
              )}
            >
              {icon || <AlertTriangle className="h-5 w-5" />}
            </div>

            {/* Title */}
            <div className="flex-1">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                {title}
              </h2>
            </div>
          </div>

          {/* Close Button */}
          <button
            onClick={onClose}
            disabled={isLoading}
            className={cn(
              'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors',
              'disabled:opacity-50 disabled:cursor-not-allowed'
            )}
            aria-label="Close dialog"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          <div className="text-sm text-gray-600 dark:text-gray-400">
            {typeof message === 'string' ? <p>{message}</p> : message}
          </div>

          {/* Action Buttons */}
          <div className="flex gap-3 mt-6">
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isLoading}
              fullWidth
            >
              {cancelLabel}
            </Button>
            <Button
              type="button"
              variant={colors.button}
              onClick={onConfirm}
              isLoading={isLoading}
              disabled={isLoading}
              fullWidth
            >
              {confirmLabel}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

ConfirmDialog.displayName = 'ConfirmDialog';

export default ConfirmDialog;
