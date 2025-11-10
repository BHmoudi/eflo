'use client';

import React from 'react';
import { cn } from '@/lib/utils';

export interface CheckboxProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
  error?: string;
  indeterminate?: boolean;
}

const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  ({ className, label, error, id, indeterminate, checked, ...props }, ref) => {
    const checkboxId = id || `checkbox-${Math.random().toString(36).substr(2, 9)}`;
    const checkboxRef = React.useRef<HTMLInputElement>(null);

    // Handle indeterminate state
    React.useEffect(() => {
      const checkbox = (ref as React.RefObject<HTMLInputElement>)?.current || checkboxRef.current;
      if (checkbox) {
        checkbox.indeterminate = indeterminate || false;
      }
    }, [indeterminate, ref]);

    return (
      <div className="w-full">
        <div className="flex items-start">
          <div className="flex items-center h-5">
            <input
              id={checkboxId}
              ref={ref || checkboxRef}
              type="checkbox"
              checked={checked}
              className={cn(
                'h-4 w-4 rounded border-gray-300 dark:border-gray-700',
                'text-brand-600 focus:ring-brand-500',
                'focus:ring-4 focus:ring-brand-500/12',
                'bg-white dark:bg-gray-900',
                'transition-colors cursor-pointer',
                'disabled:cursor-not-allowed disabled:opacity-50',
                error && 'border-error-600 dark:border-error-500 focus:ring-error-500/12',
                className
              )}
              {...props}
            />
          </div>
          {label && (
            <div className="ml-3">
              <label
                htmlFor={checkboxId}
                className={cn(
                  'text-sm font-medium cursor-pointer',
                  error
                    ? 'text-error-600 dark:text-error-500'
                    : 'text-gray-700 dark:text-gray-300',
                  props.disabled && 'cursor-not-allowed opacity-50'
                )}
              >
                {label}
              </label>
            </div>
          )}
        </div>
        {error && <p className="mt-1 ml-7 text-sm text-error-600 dark:text-error-500">{error}</p>}
      </div>
    );
  }
);

Checkbox.displayName = 'Checkbox';

export default Checkbox;
