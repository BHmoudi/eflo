import React from 'react';
import { cn } from '@/lib/utils';

export interface SkeletonProps extends React.HTMLAttributes<HTMLDivElement> {
  /**
   * The variant of the skeleton
   * @default 'rectangular'
   */
  variant?: 'text' | 'circular' | 'rectangular' | 'card';
  /**
   * The width of the skeleton
   * Can be a number (pixels) or string (e.g., '100%', '20rem')
   */
  width?: number | string;
  /**
   * The height of the skeleton
   * Can be a number (pixels) or string (e.g., '100px', '2rem')
   */
  height?: number | string;
  /**
   * Whether to show shimmer animation
   * @default true
   */
  shimmer?: boolean;
  /**
   * Additional CSS classes
   */
  className?: string;
}

/**
 * Base Skeleton component for loading states
 *
 * Features:
 * - Multiple variants: text, circular, rectangular, card
 * - Customizable width and height
 * - Shimmer animation effect
 * - Full dark mode support
 * - Accessible loading states
 *
 * @example
 * ```tsx
 * // Text skeleton
 * <Skeleton variant="text" width="80%" />
 *
 * // Circular skeleton (avatar)
 * <Skeleton variant="circular" width={40} height={40} />
 *
 * // Rectangular skeleton
 * <Skeleton variant="rectangular" width="100%" height={200} />
 *
 * // Card skeleton
 * <Skeleton variant="card" />
 * ```
 */
export const Skeleton: React.FC<SkeletonProps> = ({
  variant = 'rectangular',
  width,
  height,
  shimmer = true,
  className,
  style,
  ...props
}) => {
  // Convert numeric values to pixel strings
  const widthStyle = typeof width === 'number' ? `${width}px` : width;
  const heightStyle = typeof height === 'number' ? `${height}px` : height;

  // Base variant classes
  const variantClasses = {
    text: 'h-4 rounded',
    circular: 'rounded-full',
    rectangular: 'rounded-lg',
    card: 'rounded-xl h-48',
  };

  return (
    <div
      role="status"
      aria-label="Loading"
      aria-live="polite"
      className={cn(
        'relative overflow-hidden',
        'bg-gray-200 dark:bg-gray-700',
        variantClasses[variant],
        shimmer && 'animate-shimmer',
        className
      )}
      style={{
        width: widthStyle,
        height: heightStyle,
        ...style,
      }}
      {...props}
    >
      <span className="sr-only">Loading...</span>
    </div>
  );
};

/**
 * Skeleton component for text lines
 * Shorthand for text variant with common patterns
 */
export const SkeletonText: React.FC<Omit<SkeletonProps, 'variant'>> = (props) => (
  <Skeleton variant="text" {...props} />
);

/**
 * Skeleton component for circular elements (avatars, icons)
 * Shorthand for circular variant
 */
export const SkeletonCircle: React.FC<Omit<SkeletonProps, 'variant'>> = (props) => (
  <Skeleton variant="circular" {...props} />
);

/**
 * Skeleton component for rectangular blocks
 * Shorthand for rectangular variant
 */
export const SkeletonBox: React.FC<Omit<SkeletonProps, 'variant'>> = (props) => (
  <Skeleton variant="rectangular" {...props} />
);

export default Skeleton;
