import React from 'react';
import Link from 'next/link';
import { cn } from '@/lib/utils';
import { CheckCircle, XCircle, AlertTriangle, Info } from 'lucide-react';

export interface AlertProps {
  variant: "success" | "error" | "warning" | "info";
  title: string;
  message: string;
  showLink?: boolean;
  linkHref?: string;
  linkText?: string;
  className?: string;
}

const Alert: React.FC<AlertProps> = ({
  variant,
  title,
  message,
  showLink = false,
  linkHref = "#",
  linkText = "Learn more",
  className,
}) => {
  const variantClasses = {
    success: {
      container: "border-success-500 bg-success-50 dark:border-success-500/30 dark:bg-success-500/15",
      icon: "text-success-500",
      IconComponent: CheckCircle,
    },
    error: {
      container: "border-error-500 bg-error-50 dark:border-error-500/30 dark:bg-error-500/15",
      icon: "text-error-500",
      IconComponent: XCircle,
    },
    warning: {
      container: "border-warning-500 bg-warning-50 dark:border-warning-500/30 dark:bg-warning-500/15",
      icon: "text-warning-500",
      IconComponent: AlertTriangle,
    },
    info: {
      container: "border-blue-light-500 bg-blue-light-50 dark:border-blue-light-500/30 dark:bg-blue-light-500/15",
      icon: "text-blue-light-500",
      IconComponent: Info,
    },
  };

  const { container, icon, IconComponent } = variantClasses[variant];

  return (
    <div className={cn("rounded-xl border p-4", container, className)}>
      <div className="flex items-start gap-3">
        <div className={cn("-mt-0.5", icon)}>
          <IconComponent className="w-5 h-5" />
        </div>
        <div className="flex-1">
          <h4 className="mb-1 text-sm font-semibold text-gray-800 dark:text-white/90">
            {title}
          </h4>
          <p className="text-sm text-gray-500 dark:text-gray-400">{message}</p>
          {showLink && (
            <Link
              href={linkHref}
              className="inline-block mt-3 text-sm font-medium text-gray-500 underline hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-300"
            >
              {linkText}
            </Link>
          )}
        </div>
      </div>
    </div>
  );
};

Alert.displayName = 'Alert';

export default Alert;
