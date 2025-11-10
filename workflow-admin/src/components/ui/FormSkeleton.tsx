import React from 'react';
import { cn } from '@/lib/utils';
import { Skeleton, SkeletonText } from './Skeleton';

export interface FormSkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /**
   * Number of form fields to display
   * @default 4
   */
  fields?: number;
  /**
   * Layout of form fields
   * @default 'vertical'
   */
  layout?: 'vertical' | 'horizontal' | 'grid';
  /**
   * Whether to show form title
   * @default true
   */
  showTitle?: boolean;
  /**
   * Whether to show form description
   * @default false
   */
  showDescription?: boolean;
  /**
   * Whether to show submit buttons
   * @default true
   */
  showButtons?: boolean;
  /**
   * Number of columns for grid layout
   * @default 2
   */
  columns?: 1 | 2 | 3;
  /**
   * Additional CSS classes
   */
  className?: string;
}

/**
 * Form Skeleton component for loading form states
 *
 * Features:
 * - Multiple layout options (vertical, horizontal, grid)
 * - Customizable number of fields
 * - Optional title and description
 * - Action buttons skeleton
 * - Full dark mode support
 * - Matches form component patterns
 *
 * @example
 * ```tsx
 * // Basic form skeleton
 * <FormSkeleton />
 *
 * // Form with description and grid layout
 * <FormSkeleton
 *   showDescription
 *   layout="grid"
 *   columns={2}
 *   fields={6}
 * />
 *
 * // Horizontal form layout
 * <FormSkeleton layout="horizontal" fields={3} />
 *
 * // Form without buttons
 * <FormSkeleton showButtons={false} />
 * ```
 */
export const FormSkeleton: React.FC<FormSkeletonProps> = ({
  fields = 4,
  layout = 'vertical',
  showTitle = true,
  showDescription = false,
  showButtons = true,
  columns = 2,
  className,
  ...props
}) => {
  const gridCols = {
    1: 'grid-cols-1',
    2: 'grid-cols-1 md:grid-cols-2',
    3: 'grid-cols-1 md:grid-cols-3',
  };

  const containerClass = layout === 'grid'
    ? cn('grid gap-6', gridCols[columns])
    : 'space-y-6';

  return (
    <div
      role="status"
      aria-label="Loading form"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-xl p-6',
        'space-y-6',
        className
      )}
      {...props}
    >
      {/* Form Header */}
      {(showTitle || showDescription) && (
        <div className="space-y-2 pb-4 border-b border-gray-200 dark:border-gray-700">
          {showTitle && (
            <SkeletonText width="40%" height={24} />
          )}
          {showDescription && (
            <SkeletonText width="60%" height={14} />
          )}
        </div>
      )}

      {/* Form Fields */}
      <div className={containerClass}>
        {Array.from({ length: fields }).map((_, index) => (
          <FormFieldSkeleton
            key={index}
            layout={layout}
          />
        ))}
      </div>

      {/* Form Actions */}
      {showButtons && (
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-gray-200 dark:border-gray-700">
          <Skeleton
            variant="rectangular"
            width={100}
            height={40}
            className="rounded-lg"
          />
          <Skeleton
            variant="rectangular"
            width={120}
            height={40}
            className="rounded-lg"
          />
        </div>
      )}
    </div>
  );
};

/**
 * Individual form field skeleton
 */
export const FormFieldSkeleton: React.FC<{
  layout?: 'vertical' | 'horizontal';
  fieldType?: 'input' | 'textarea' | 'select' | 'checkbox' | 'radio';
  className?: string;
}> = ({ layout = 'vertical', fieldType = 'input', className }) => {
  const isHorizontal = layout === 'horizontal';

  const fieldHeight = {
    input: 40,
    textarea: 100,
    select: 40,
    checkbox: 20,
    radio: 20,
  };

  return (
    <div
      className={cn(
        'space-y-2',
        isHorizontal && 'flex items-center gap-4',
        className
      )}
    >
      {/* Label */}
      <SkeletonText
        width={isHorizontal ? 120 : '30%'}
        height={14}
        className={isHorizontal ? 'flex-shrink-0' : ''}
      />

      {/* Input Field */}
      <Skeleton
        variant="rectangular"
        width={isHorizontal ? 300 : '100%'}
        height={fieldHeight[fieldType]}
        className="rounded-lg"
      />
    </div>
  );
};

/**
 * Search form skeleton
 * Optimized for search interfaces
 */
export const SearchFormSkeleton: React.FC<{
  showFilters?: boolean;
  filterCount?: number;
  className?: string;
}> = ({ showFilters = false, filterCount = 3, className }) => {
  return (
    <div
      role="status"
      aria-label="Loading search form"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-xl p-4',
        'space-y-4',
        className
      )}
    >
      {/* Search Input */}
      <div className="flex gap-2">
        <Skeleton
          variant="rectangular"
          className="flex-1 h-10 rounded-lg"
        />
        <Skeleton
          variant="rectangular"
          width={100}
          height={40}
          className="rounded-lg"
        />
      </div>

      {/* Filters */}
      {showFilters && (
        <div className="flex flex-wrap gap-2">
          {Array.from({ length: filterCount }).map((_, index) => (
            <Skeleton
              key={index}
              variant="rectangular"
              width={120}
              height={32}
              className="rounded-lg"
            />
          ))}
        </div>
      )}
    </div>
  );
};

/**
 * Login form skeleton
 * Specialized for authentication forms
 */
export const LoginFormSkeleton: React.FC<{
  showSocialButtons?: boolean;
  className?: string;
}> = ({ showSocialButtons = false, className }) => {
  return (
    <div
      role="status"
      aria-label="Loading login form"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-xl p-8',
        'space-y-6 max-w-md mx-auto',
        className
      )}
    >
      {/* Title */}
      <div className="text-center space-y-2">
        <SkeletonText width="60%" height={28} className="mx-auto" />
        <SkeletonText width="80%" height={14} className="mx-auto" />
      </div>

      {/* Form Fields */}
      <div className="space-y-4">
        <FormFieldSkeleton fieldType="input" />
        <FormFieldSkeleton fieldType="input" />
      </div>

      {/* Additional Options */}
      <div className="flex items-center justify-between">
        <SkeletonText width={100} height={14} />
        <SkeletonText width={120} height={14} />
      </div>

      {/* Submit Button */}
      <Skeleton
        variant="rectangular"
        width="100%"
        height={44}
        className="rounded-lg"
      />

      {/* Social Login Buttons */}
      {showSocialButtons && (
        <>
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-200 dark:border-gray-700" />
            </div>
            <div className="relative flex justify-center">
              <SkeletonText width={80} height={14} />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <Skeleton variant="rectangular" height={40} className="rounded-lg" />
            <Skeleton variant="rectangular" height={40} className="rounded-lg" />
          </div>
        </>
      )}
    </div>
  );
};

export default FormSkeleton;
