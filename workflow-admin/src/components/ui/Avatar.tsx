'use client';

import React, { useState } from 'react';
import { cn } from '@/lib/utils';

export interface AvatarProps {
  src?: string;
  alt?: string;
  name?: string;
  size?: 'xsmall' | 'small' | 'medium' | 'large' | 'xlarge' | 'xxlarge';
  status?: 'online' | 'offline' | 'busy' | 'none';
  className?: string;
}

const Avatar = React.forwardRef<HTMLDivElement, AvatarProps>(
  ({ src, alt, name, size = 'medium', status = 'none', className }, ref) => {
    const [imageError, setImageError] = useState(false);

    const sizeClasses = {
      xsmall: 'h-6 w-6 text-xs',
      small: 'h-8 w-8 text-sm',
      medium: 'h-10 w-10 text-base',
      large: 'h-12 w-12 text-lg',
      xlarge: 'h-16 w-16 text-xl',
      xxlarge: 'h-24 w-24 text-3xl',
    };

    const statusSizes = {
      xsmall: 'h-1.5 w-1.5',
      small: 'h-2 w-2',
      medium: 'h-2.5 w-2.5',
      large: 'h-3 w-3',
      xlarge: 'h-4 w-4',
      xxlarge: 'h-5 w-5',
    };

    const statusColors = {
      online: 'bg-success-500 ring-white dark:ring-gray-900',
      offline: 'bg-gray-400 ring-white dark:ring-gray-900',
      busy: 'bg-error-500 ring-white dark:ring-gray-900',
      none: '',
    };

    const getInitials = (name: string) => {
      if (!name) return '?';

      const parts = name.trim().split(' ');
      if (parts.length === 1) {
        return parts[0].charAt(0).toUpperCase();
      }
      return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    };

    const shouldShowImage = src && !imageError;
    const initials = name ? getInitials(name) : '?';

    return (
      <div ref={ref} className={cn('relative inline-block', className)}>
        <div
          className={cn(
            'flex items-center justify-center rounded-full overflow-hidden',
            'bg-gradient-to-br from-brand-400 to-brand-600',
            'text-white font-semibold',
            sizeClasses[size]
          )}
        >
          {shouldShowImage ? (
            <img
              src={src}
              alt={alt || name || 'Avatar'}
              className="h-full w-full object-cover"
              onError={() => setImageError(true)}
            />
          ) : (
            <span>{initials}</span>
          )}
        </div>

        {/* Status Indicator */}
        {status !== 'none' && (
          <span
            className={cn(
              'absolute bottom-0 right-0 block rounded-full ring-2',
              statusSizes[size],
              statusColors[status]
            )}
            aria-label={`Status: ${status}`}
          />
        )}
      </div>
    );
  }
);

Avatar.displayName = 'Avatar';

export default Avatar;
