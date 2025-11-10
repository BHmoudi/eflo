'use client';

import React, { createContext, useContext } from 'react';
import { cn } from '@/lib/utils';

interface RadioGroupContextType {
  name: string;
  value?: string;
  onChange?: (value: string) => void;
  disabled?: boolean;
  error?: string;
}

const RadioGroupContext = createContext<RadioGroupContextType | undefined>(undefined);

export interface RadioGroupProps {
  name: string;
  value?: string;
  defaultValue?: string;
  onChange?: (value: string) => void;
  children: React.ReactNode;
  className?: string;
  error?: string;
  disabled?: boolean;
}

export const RadioGroup = React.forwardRef<HTMLDivElement, RadioGroupProps>(
  ({ name, value, defaultValue, onChange, children, className, error, disabled }, ref) => {
    const [internalValue, setInternalValue] = React.useState(defaultValue);
    const isControlled = value !== undefined;
    const currentValue = isControlled ? value : internalValue;

    const handleChange = (newValue: string) => {
      if (!isControlled) {
        setInternalValue(newValue);
      }
      onChange?.(newValue);
    };

    return (
      <RadioGroupContext.Provider
        value={{
          name,
          value: currentValue,
          onChange: handleChange,
          disabled,
          error,
        }}
      >
        <div ref={ref} className={cn('space-y-3', className)} role="radiogroup">
          {children}
          {error && <p className="text-sm text-error-600 dark:text-error-500">{error}</p>}
        </div>
      </RadioGroupContext.Provider>
    );
  }
);

RadioGroup.displayName = 'RadioGroup';

export interface RadioProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  label?: string;
  value: string;
  description?: string;
}

export const Radio = React.forwardRef<HTMLInputElement, RadioProps>(
  ({ className, label, value, description, id, disabled: propDisabled, ...props }, ref) => {
    const context = useContext(RadioGroupContext);

    if (!context) {
      throw new Error('Radio must be used within RadioGroup');
    }

    const { name, value: groupValue, onChange, disabled: groupDisabled, error } = context;
    const isDisabled = propDisabled || groupDisabled;
    const isChecked = groupValue === value;
    const radioId = id || `radio-${name}-${value}`;

    const handleChange = () => {
      if (!isDisabled && onChange) {
        onChange(value);
      }
    };

    return (
      <div className="flex items-start">
        <div className="flex items-center h-5">
          <input
            id={radioId}
            ref={ref}
            type="radio"
            name={name}
            value={value}
            checked={isChecked}
            onChange={handleChange}
            disabled={isDisabled}
            className={cn(
              'h-4 w-4 border-gray-300 dark:border-gray-700',
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
        {(label || description) && (
          <div className="ml-3">
            {label && (
              <label
                htmlFor={radioId}
                className={cn(
                  'block text-sm font-medium cursor-pointer',
                  error
                    ? 'text-error-600 dark:text-error-500'
                    : 'text-gray-700 dark:text-gray-300',
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

Radio.displayName = 'Radio';

export default Radio;
