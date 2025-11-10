'use client';

import React from 'react';
import { cn } from '@/lib/utils';

export interface ToggleProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type' | 'size'> {
  label?: string;
  description?: string;
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
}

const Toggle = React.forwardRef<HTMLInputElement, ToggleProps>(
  ({
    className,
    label,
    description,
    id,
    size = 'md',
    checked,
    disabled,
    isLoading,
    onChange,
    ...props
  }, ref) => {
    const toggleId = id || `toggle-${Math.random().toString(36).substr(2, 9)}`;
    const isDisabled = disabled || isLoading;

    const sizeClasses = {
      sm: {
        track: 'h-5 w-9',
        thumb: 'h-4 w-4',
        translate: 'translate-x-4',
      },
      md: {
        track: 'h-6 w-11',
        thumb: 'h-5 w-5',
        translate: 'translate-x-5',
      },
      lg: {
        track: 'h-7 w-14',
        thumb: 'h-6 w-6',
        translate: 'translate-x-7',
      },
    };

    const currentSize = sizeClasses[size];

    return (
      <div className="flex items-start">
        <div className="flex items-center h-5">
          {/* Hidden checkbox input */}
          <input
            id={toggleId}
            ref={ref}
            type="checkbox"
            checked={checked}
            disabled={isDisabled}
            onChange={onChange}
            className="sr-only peer"
            {...props}
          />

          {/* Toggle track */}
          <label
            htmlFor={toggleId}
            className={cn(
              'relative inline-flex items-center cursor-pointer rounded-full',
              'transition-colors duration-200 ease-in-out',
              'focus-within:ring-4 focus-within:ring-brand-500/12',
              currentSize.track,
              checked
                ? 'bg-brand-600 peer-disabled:bg-brand-300'
                : 'bg-gray-200 dark:bg-gray-700 peer-disabled:bg-gray-100 dark:peer-disabled:bg-gray-800',
              isDisabled && 'cursor-not-allowed opacity-50',
              className
            )}
          >
            {/* Toggle thumb */}
            <span
              className={cn(
                'inline-block bg-white rounded-full shadow-sm',
                'transition-transform duration-200 ease-in-out',
                'transform ml-0.5',
                currentSize.thumb,
                checked && currentSize.translate
              )}
            >
              {/* Loading spinner */}
              {isLoading && (
                <svg
                  className={cn(
                    'animate-spin text-gray-400',
                    size === 'sm' ? 'h-3 w-3' : size === 'md' ? 'h-4 w-4' : 'h-5 w-5'
                  )}
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    className="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    strokeWidth="4"
                  ></circle>
                  <path
                    className="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
              )}
            </span>
          </label>
        </div>

        {/* Label and description */}
        {(label || description) && (
          <div className="ml-3">
            {label && (
              <label
                htmlFor={toggleId}
                className={cn(
                  'block text-sm font-medium text-gray-700 dark:text-gray-300 cursor-pointer',
                  isDisabled && 'cursor-not-allowed opacity-50'
                )}
              >
                {label}
              </label>
            )}
            {description && (
              <p
                className={cn(
                  'text-sm text-gray-500 dark:text-gray-400',
                  isDisabled && 'opacity-50'
                )}
              >
                {description}
              </p>
            )}
          </div>
        )}
      </div>
    );
  }
);

Toggle.displayName = 'Toggle';

export default Toggle;
