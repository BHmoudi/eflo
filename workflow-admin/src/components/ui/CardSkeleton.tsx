import React from 'react';
import { cn } from '@/lib/utils';
import { Skeleton, SkeletonText, SkeletonCircle } from './Skeleton';

export interface CardSkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /**
   * Whether to show card header with avatar
   * @default true
   */
  showHeader?: boolean;
  /**
   * Whether to show avatar in header
   * @default true
   */
  showAvatar?: boolean;
  /**
   * Number of content lines to display
   * @default 3
   */
  lines?: number;
  /**
   * Whether to show action buttons at the bottom
   * @default false
   */
  showActions?: boolean;
  /**
   * Whether to show an image placeholder at the top
   * @default false
   */
  showImage?: boolean;
  /**
   * Additional CSS classes
   */
  className?: string;
}

/**
 * Card Skeleton component for loading card states
 *
 * Features:
 * - Optional header with avatar
 * - Customizable number of content lines
 * - Optional action buttons
 * - Optional image placeholder
 * - Full dark mode support
 * - Matches Card component structure
 *
 * @example
 * ```tsx
 * // Basic card skeleton
 * <CardSkeleton />
 *
 * // Card with image and actions
 * <CardSkeleton showImage showActions />
 *
 * // Card without header
 * <CardSkeleton showHeader={false} lines={5} />
 *
 * // Multiple cards in a grid
 * <div className="grid grid-cols-3 gap-4">
 *   {[1, 2, 3].map(i => <CardSkeleton key={i} />)}
 * </div>
 * ```
 */
export const CardSkeleton: React.FC<CardSkeletonProps> = ({
  showHeader = true,
  showAvatar = true,
  lines = 3,
  showActions = false,
  showImage = false,
  className,
  ...props
}) => {
  return (
    <div
      role="status"
      aria-label="Loading card"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-xl shadow-theme-sm',
        'overflow-hidden',
        className
      )}
      {...props}
    >
      {/* Optional Image */}
      {showImage && (
        <Skeleton
          variant="rectangular"
          className="w-full h-48 rounded-none"
          height={192}
        />
      )}

      <div className="p-6 space-y-4">
        {/* Header with Avatar and Title */}
        {showHeader && (
          <div className="flex items-center gap-3">
            {showAvatar && (
              <SkeletonCircle
                width={40}
                height={40}
                className="flex-shrink-0"
              />
            )}
            <div className="flex-1 space-y-2">
              <SkeletonText width="60%" height={16} />
              <SkeletonText width="40%" height={12} />
            </div>
          </div>
        )}

        {/* Content Lines */}
        <div className="space-y-2">
          {Array.from({ length: lines }).map((_, index) => {
            // Make last line shorter for realistic effect
            const isLastLine = index === lines - 1;
            const width = isLastLine ? '75%' : '100%';

            return (
              <SkeletonText
                key={index}
                width={width}
                height={12}
              />
            );
          })}
        </div>

        {/* Optional Actions */}
        {showActions && (
          <div className="flex items-center gap-2 pt-2">
            <Skeleton
              variant="rectangular"
              width={100}
              height={36}
              className="rounded-lg"
            />
            <Skeleton
              variant="rectangular"
              width={100}
              height={36}
              className="rounded-lg"
            />
          </div>
        )}
      </div>
    </div>
  );
};

/**
 * Grid of card skeletons
 * Useful for list views
 */
export const CardSkeletonGrid: React.FC<{
  count?: number;
  cols?: 1 | 2 | 3 | 4;
  cardProps?: Omit<CardSkeletonProps, 'className'>;
  className?: string;
}> = ({ count = 6, cols = 3, cardProps, className }) => {
  const gridCols = {
    1: 'grid-cols-1',
    2: 'grid-cols-1 md:grid-cols-2',
    3: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
    4: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4',
  };

  return (
    <div className={cn('grid gap-4', gridCols[cols], className)}>
      {Array.from({ length: count }).map((_, index) => (
        <CardSkeleton key={index} {...cardProps} />
      ))}
    </div>
  );
};

/**
 * Compact card skeleton for list items
 */
export const CompactCardSkeleton: React.FC<
  Omit<CardSkeletonProps, 'showImage' | 'showActions'>
> = ({ showHeader = true, showAvatar = false, lines = 2, className, ...props }) => {
  return (
    <div
      role="status"
      aria-label="Loading item"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-lg p-4',
        'space-y-3',
        className
      )}
      {...props}
    >
      {showHeader && (
        <div className="flex items-center gap-3">
          {showAvatar && <SkeletonCircle width={32} height={32} />}
          <SkeletonText width="50%" height={14} />
        </div>
      )}

      <div className="space-y-2">
        {Array.from({ length: lines }).map((_, index) => (
          <SkeletonText
            key={index}
            width={index === lines - 1 ? '60%' : '100%'}
            height={12}
          />
        ))}
      </div>
    </div>
  );
};

export default CardSkeleton;
