'use client';

import React, { useState, useEffect } from 'react';
import { cn } from '@/lib/utils';
import Input from '@/components/ui/Input';
import { Search, X } from 'lucide-react';

export interface SearchInputProps {
  value?: string;
  onChange: (value: string) => void;
  placeholder?: string;
  debounceMs?: number;
  className?: string;
  autoFocus?: boolean;
  disabled?: boolean;
}

const SearchInput: React.FC<SearchInputProps> = ({
  value: controlledValue = '',
  onChange,
  placeholder = 'Search...',
  debounceMs = 300,
  className,
  autoFocus = false,
  disabled = false
}) => {
  const [localValue, setLocalValue] = useState(controlledValue);

  // Sync local value with controlled value
  useEffect(() => {
    setLocalValue(controlledValue);
  }, [controlledValue]);

  // Debounce the onChange callback
  useEffect(() => {
    const timer = setTimeout(() => {
      if (localValue !== controlledValue) {
        onChange(localValue);
      }
    }, debounceMs);

    return () => clearTimeout(timer);
  }, [localValue, debounceMs, onChange, controlledValue]);

  const handleClear = () => {
    setLocalValue('');
    onChange('');
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setLocalValue(e.target.value);
  };

  return (
    <div className={cn('relative', className)}>
      <Input
        type="text"
        value={localValue}
        onChange={handleChange}
        placeholder={placeholder}
        autoFocus={autoFocus}
        disabled={disabled}
        startIcon={<Search className="h-4 w-4" />}
        endIcon={
          localValue && (
            <button
              onClick={handleClear}
              disabled={disabled}
              className={cn(
                'text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-colors',
                'disabled:cursor-not-allowed disabled:opacity-50'
              )}
              aria-label="Clear search"
              type="button"
            >
              <X className="h-4 w-4" />
            </button>
          )
        }
        className="pr-10"
      />
    </div>
  );
};

SearchInput.displayName = 'SearchInput';

export default SearchInput;
