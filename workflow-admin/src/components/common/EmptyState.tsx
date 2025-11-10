import React, { ReactNode } from 'react';
import { cn } from '@/lib/utils';
import Button from '@/components/ui/Button';

export interface EmptyStateProps {
  icon?: ReactNode;
  title: string;
  description?: string;
  actionLabel?: string;
  onAction?: () => void;
  className?: string;
  size?: 'sm' | 'md' | 'lg';
}

const EmptyState: React.FC<EmptyStateProps> = ({
  icon,
  title,
  description,
  actionLabel,
  onAction,
  className,
  size = 'md'
}) => {
  const sizeClasses = {
    sm: {
      container: 'py-8',
      icon: 'h-10 w-10',
      title: 'text-base',
      description: 'text-sm',
    },
    md: {
      container: 'py-12',
      icon: 'h-12 w-12',
      title: 'text-lg',
      description: 'text-sm',
    },
    lg: {
      container: 'py-16',
      icon: 'h-16 w-16',
      title: 'text-xl',
      description: 'text-base',
    },
  };

  const sizes = sizeClasses[size];

  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center text-center',
        sizes.container,
        className
      )}
    >
      {icon && (
        <div
          className={cn(
            'flex items-center justify-center rounded-full bg-gray-100 dark:bg-gray-800 text-gray-400 dark:text-gray-500 mb-4',
            sizes.icon
          )}
        >
          {icon}
        </div>
      )}

      <h3
        className={cn(
          'font-semibold text-gray-900 dark:text-white mb-2',
          sizes.title
        )}
      >
        {title}
      </h3>

      {description && (
        <p
          className={cn(
            'text-gray-600 dark:text-gray-400 max-w-md mb-6',
            sizes.description
          )}
        >
          {description}
        </p>
      )}

      {actionLabel && onAction && (
        <Button
          variant="primary"
          onClick={onAction}
        >
          {actionLabel}
        </Button>
      )}
    </div>
  );
};

EmptyState.displayName = 'EmptyState';

export default EmptyState;
