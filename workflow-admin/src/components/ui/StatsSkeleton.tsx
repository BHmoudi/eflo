import React from 'react';
import { cn } from '@/lib/utils';
import { Skeleton, SkeletonText, SkeletonCircle } from './Skeleton';

export interface StatsSkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /**
   * Whether to show icon circle
   * @default true
   */
  showIcon?: boolean;
  /**
   * Whether to show trend indicator
   * @default true
   */
  showTrend?: boolean;
  /**
   * Whether to show description/subtitle
   * @default false
   */
  showDescription?: boolean;
  /**
   * Layout variant
   * @default 'horizontal'
   */
  layout?: 'horizontal' | 'vertical';
  /**
   * Additional CSS classes
   */
  className?: string;
}

/**
 * Stats Card Skeleton component for loading statistics states
 *
 * Features:
 * - Optional icon circle
 * - Title and value skeletons
 * - Trend indicator skeleton
 * - Multiple layout options
 * - Full dark mode support
 * - Matches stats card patterns
 *
 * @example
 * ```tsx
 * // Basic stats skeleton
 * <StatsSkeleton />
 *
 * // Stats with description
 * <StatsSkeleton showDescription />
 *
 * // Vertical layout
 * <StatsSkeleton layout="vertical" />
 *
 * // Without icon
 * <StatsSkeleton showIcon={false} />
 *
 * // Grid of stats
 * <div className="grid grid-cols-4 gap-4">
 *   {[1,2,3,4].map(i => <StatsSkeleton key={i} />)}
 * </div>
 * ```
 */
export const StatsSkeleton: React.FC<StatsSkeletonProps> = ({
  showIcon = true,
  showTrend = true,
  showDescription = false,
  layout = 'horizontal',
  className,
  ...props
}) => {
  const isVertical = layout === 'vertical';

  return (
    <div
      role="status"
      aria-label="Loading statistics"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-xl p-6',
        isVertical ? 'space-y-4' : 'flex items-center gap-4',
        className
      )}
      {...props}
    >
      {/* Icon Circle */}
      {showIcon && (
        <div
          className={cn(
            'flex-shrink-0',
            isVertical && 'mx-auto'
          )}
        >
          <SkeletonCircle width={48} height={48} />
        </div>
      )}

      {/* Content */}
      <div className={cn('flex-1', isVertical && 'text-center space-y-3')}>
        {/* Title/Label */}
        <SkeletonText
          width={isVertical ? '70%' : '60%'}
          height={14}
          className={isVertical ? 'mx-auto' : ''}
        />

        {/* Value */}
        <SkeletonText
          width={isVertical ? '50%' : '40%'}
          height={32}
          className={cn('mt-2', isVertical && 'mx-auto')}
        />

        {/* Description */}
        {showDescription && (
          <SkeletonText
            width={isVertical ? '80%' : '70%'}
            height={12}
            className={cn('mt-1', isVertical && 'mx-auto')}
          />
        )}

        {/* Trend Indicator */}
        {showTrend && (
          <div className={cn('flex items-center gap-1 mt-2', isVertical && 'justify-center')}>
            <Skeleton
              variant="rectangular"
              width={16}
              height={16}
              className="rounded"
            />
            <SkeletonText width={60} height={12} />
          </div>
        )}
      </div>
    </div>
  );
};

/**
 * Grid of stats skeletons
 * Useful for dashboard views
 */
export const StatsSkeletonGrid: React.FC<{
  count?: number;
  cols?: 1 | 2 | 3 | 4;
  statsProps?: Omit<StatsSkeletonProps, 'className'>;
  className?: string;
}> = ({ count = 4, cols = 4, statsProps, className }) => {
  const gridCols = {
    1: 'grid-cols-1',
    2: 'grid-cols-1 md:grid-cols-2',
    3: 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3',
    4: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4',
  };

  return (
    <div className={cn('grid gap-4', gridCols[cols], className)}>
      {Array.from({ length: count }).map((_, index) => (
        <StatsSkeleton key={index} {...statsProps} />
      ))}
    </div>
  );
};

/**
 * Compact stats skeleton
 * Smaller version for tight spaces
 */
export const CompactStatsSkeleton: React.FC<
  Omit<StatsSkeletonProps, 'layout' | 'showDescription'>
> = ({ showIcon = false, showTrend = false, className, ...props }) => {
  return (
    <div
      role="status"
      aria-label="Loading compact statistics"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-lg p-4',
        'flex items-center justify-between',
        className
      )}
      {...props}
    >
      <div className="space-y-2 flex-1">
        <SkeletonText width="60%" height={12} />
        <SkeletonText width="40%" height={24} />
        {showTrend && (
          <div className="flex items-center gap-1">
            <SkeletonText width={50} height={10} />
          </div>
        )}
      </div>

      {showIcon && (
        <SkeletonCircle width={36} height={36} className="ml-4" />
      )}
    </div>
  );
};

/**
 * Stats card with chart skeleton
 * For statistics with accompanying charts
 */
export const StatsWithChartSkeleton: React.FC<{
  chartHeight?: number;
  showIcon?: boolean;
  className?: string;
}> = ({ chartHeight = 200, showIcon = true, className }) => {
  return (
    <div
      role="status"
      aria-label="Loading statistics with chart"
      className={cn(
        'bg-white dark:bg-gray-800',
        'border border-gray-200 dark:border-gray-700',
        'rounded-xl overflow-hidden',
        className
      )}
    >
      {/* Header */}
      <div className="p-6 pb-4 flex items-center justify-between">
        <div className="space-y-2 flex-1">
          <SkeletonText width="50%" height={14} />
          <SkeletonText width="35%" height={28} />
        </div>
        {showIcon && <SkeletonCircle width={40} height={40} />}
      </div>

      {/* Chart Area */}
      <div className="px-6 pb-6">
        <Skeleton
          variant="rectangular"
          height={chartHeight}
          className="w-full rounded-lg"
        />
      </div>

      {/* Footer with trend */}
      <div className="px-6 py-4 border-t border-gray-200 dark:border-gray-700">
        <div className="flex items-center gap-2">
          <Skeleton variant="rectangular" width={20} height={20} className="rounded" />
          <SkeletonText width={150} height={12} />
        </div>
      </div>
    </div>
  );
};

/**
 * Dashboard stats overview skeleton
 * Complete dashboard statistics section
 */
export const DashboardStatsSkeleton: React.FC<{
  showMainStats?: boolean;
  showDetailedStats?: boolean;
  className?: string;
}> = ({ showMainStats = true, showDetailedStats = true, className }) => {
  return (
    <div className={cn('space-y-6', className)} role="status" aria-label="Loading dashboard">
      {/* Main Stats Grid */}
      {showMainStats && (
        <StatsSkeletonGrid count={4} cols={4} />
      )}

      {/* Detailed Stats */}
      {showDetailedStats && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <StatsWithChartSkeleton />
          <StatsWithChartSkeleton />
        </div>
      )}
    </div>
  );
};

export default StatsSkeleton;
