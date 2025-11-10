import React from 'react';
import { cn } from '@/lib/utils';
import Spinner from './Spinner';

export interface LoadingOverlayProps {
  isLoading: boolean;
  message?: string;
  blur?: boolean;
  className?: string;
}

const LoadingOverlay: React.FC<LoadingOverlayProps> = ({
  isLoading,
  message,
  blur = true,
  className
}) => {
  if (!isLoading) return null;

  return (
    <div
      className={cn(
        'fixed inset-0 z-50 flex flex-col items-center justify-center',
        'bg-white/80 dark:bg-gray-900/80',
        blur && 'backdrop-blur-sm',
        className
      )}
    >
      <Spinner size="xl" />
      {message && (
        <p className="mt-4 text-sm font-medium text-gray-700 dark:text-gray-300">
          {message}
        </p>
      )}
    </div>
  );
};

LoadingOverlay.displayName = 'LoadingOverlay';

export default LoadingOverlay;
