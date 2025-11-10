import React from 'react';
import { cn } from '@/lib/utils';
import Badge from '@/components/ui/Badge';

export interface StatusBadgeProps {
  status: string;
  className?: string;
  size?: 'sm' | 'md';
  variant?: 'light' | 'solid';
}

const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  className,
  size = 'md',
  variant = 'light'
}) => {
  // Map status to badge color
  const getStatusBadgeColor = (status: string): 'primary' | 'success' | 'error' | 'warning' | 'info' | 'light' | 'dark' => {
    const statusLower = status.toUpperCase();

    // Success states
    if (['COMPLETED', 'APPROVED', 'ACTIVE', 'DELIVERED', 'INVOICED'].includes(statusLower)) {
      return 'success';
    }

    // Error/Rejected states
    if (['REJECTED', 'CANCELLED', 'FAILED'].includes(statusLower)) {
      return 'error';
    }

    // Warning/Pending states
    if (['PENDING', 'ON_HOLD'].includes(statusLower)) {
      return 'warning';
    }

    // In Progress states
    if (['IN_PROGRESS', 'IN_PRODUCTION', 'CONFIRMED'].includes(statusLower)) {
      return 'info';
    }

    // Ready states
    if (['READY_FOR_DELIVERY'].includes(statusLower)) {
      return 'primary';
    }

    // Draft/Inactive states
    if (['DRAFT', 'INACTIVE'].includes(statusLower)) {
      return 'light';
    }

    // Default
    return 'light';
  };

  // Format status text for display
  const formatStatusText = (status: string): string => {
    return status
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  };

  const color = getStatusBadgeColor(status);
  const displayText = formatStatusText(status);

  return (
    <Badge
      variant={variant}
      color={color}
      size={size}
      className={cn(className)}
    >
      {displayText}
    </Badge>
  );
};

StatusBadge.displayName = 'StatusBadge';

export default StatusBadge;
