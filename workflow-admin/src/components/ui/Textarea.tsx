'use client';

import React, { useEffect, useRef, useState } from 'react';
import { cn } from '@/lib/utils';

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string;
  error?: string;
  helperText?: string;
  autoResize?: boolean;
  showCharacterCount?: boolean;
  maxLength?: number;
}

const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({
    className,
    label,
    error,
    helperText,
    id,
    autoResize = false,
    showCharacterCount = false,
    maxLength,
    onChange,
    value,
    ...props
  }, ref) => {
    const textareaId = id || `textarea-${Math.random().toString(36).substr(2, 9)}`;
    const internalRef = useRef<HTMLTextAreaElement>(null);
    const textareaRef = (ref as React.RefObject<HTMLTextAreaElement>) || internalRef;
    const [charCount, setCharCount] = useState(0);

    // Auto-resize functionality
    useEffect(() => {
      if (autoResize && textareaRef.current) {
        const textarea = textareaRef.current;
        textarea.style.height = 'auto';
        textarea.style.height = `${textarea.scrollHeight}px`;
      }
    }, [value, autoResize, textareaRef]);

    // Track character count
    useEffect(() => {
      if (showCharacterCount || maxLength) {
        const count = typeof value === 'string' ? value.length : 0;
        setCharCount(count);
      }
    }, [value, showCharacterCount, maxLength]);

    const handleChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
      if (showCharacterCount || maxLength) {
        setCharCount(e.target.value.length);
      }
      onChange?.(e);
    };

    const showCount = showCharacterCount || maxLength;
    const isNearLimit = maxLength && charCount > maxLength * 0.9;

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={textareaId}
            className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1"
          >
            {label}
          </label>
        )}
        <div className="relative">
          <textarea
            id={textareaId}
            ref={textareaRef}
            value={value}
            maxLength={maxLength}
            onChange={handleChange}
            className={cn(
              'flex min-h-[80px] w-full rounded-lg border border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 px-3 py-2 text-sm',
              'text-gray-800 dark:text-white/90',
              'placeholder:text-gray-400 dark:placeholder:text-gray-500',
              'focus:outline-none focus:ring-4 focus:ring-brand-500/12 focus:border-brand-500',
              'disabled:cursor-not-allowed disabled:opacity-50',
              'resize-none',
              error && 'border-error-600 dark:border-error-500 focus:ring-error-500/12 focus:border-error-600',
              autoResize && 'overflow-hidden',
              className
            )}
            {...props}
          />
        </div>

        {/* Character Count / Error / Helper Text */}
        <div className="flex items-center justify-between mt-1">
          <div className="flex-1">
            {error && <p className="text-sm text-error-600 dark:text-error-500">{error}</p>}
            {helperText && !error && (
              <p className="text-sm text-gray-500 dark:text-gray-400">{helperText}</p>
            )}
          </div>
          {showCount && (
            <p
              className={cn(
                'text-sm ml-2',
                error
                  ? 'text-error-600 dark:text-error-500'
                  : isNearLimit
                  ? 'text-warning-600 dark:text-warning-500'
                  : 'text-gray-500 dark:text-gray-400'
              )}
            >
              {charCount}
              {maxLength && ` / ${maxLength}`}
            </p>
          )}
        </div>
      </div>
    );
  }
);

Textarea.displayName = 'Textarea';

export default Textarea;
