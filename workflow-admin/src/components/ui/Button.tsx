import React, { ReactNode } from 'react';
import { cn } from '@/lib/utils';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  fullWidth?: boolean;
  startIcon?: ReactNode;
  endIcon?: ReactNode;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({
    className,
    variant = 'primary',
    size = 'md',
    isLoading,
    fullWidth,
    startIcon,
    endIcon,
    children,
    disabled,
    ...props
  }, ref) => {
    const baseStyles = `
      inline-flex items-center justify-center
      gap-2
      rounded-lg
      font-semibold
      transition-all
      duration-200
      active:scale-[0.98]
      focus:outline-none
      focus:ring-4
      disabled:opacity-50
      disabled:pointer-events-none
    `;

    const variants = {
      primary: `
        bg-brand-500 text-white shadow-theme-xs
        hover:bg-brand-600 hover:shadow-theme-md
        focus:ring-brand-500/12
        disabled:bg-brand-300
      `,
      secondary: `
        bg-gray-200 text-gray-900 shadow-theme-xs
        hover:bg-gray-300 hover:shadow-theme-md
        focus:ring-gray-500/12
        dark:bg-gray-800 dark:text-gray-300
        dark:hover:bg-gray-700
      `,
      danger: `
        bg-error-600 text-white shadow-theme-xs
        hover:bg-error-700 hover:shadow-theme-md
        focus:ring-error-500/12
        disabled:bg-error-300
      `,
      ghost: `
        bg-transparent text-gray-700
        hover:bg-gray-100
        focus:ring-gray-500/12
        dark:text-gray-300
        dark:hover:bg-white/5
        dark:hover:text-gray-200
      `,
      outline: `
        bg-white text-gray-700 shadow-theme-xs
        ring-1 ring-inset ring-gray-300
        hover:bg-gray-50 hover:shadow-theme-md
        focus:ring-brand-500/12
        dark:bg-gray-800 dark:text-gray-400
        dark:ring-gray-700
        dark:hover:bg-white/[0.03]
        dark:hover:text-gray-300
      `,
    };

    const sizes = {
      sm: 'h-10 px-4 text-sm',
      md: 'h-11 px-5 text-sm',
      lg: 'h-12 px-6 text-base',
    };

    return (
      <button
        ref={ref}
        className={cn(
          baseStyles,
          variants[variant],
          sizes[size],
          fullWidth && 'w-full',
          className
        )}
        disabled={disabled || isLoading}
        {...props}
      >
        {startIcon && <span className="flex items-center">{startIcon}</span>}
        {isLoading && (
          <svg
            className="animate-spin h-4 w-4"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            ></circle>
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
        )}
        {children}
        {endIcon && <span className="flex items-center">{endIcon}</span>}
      </button>
    );
  }
);

Button.displayName = 'Button';

export default Button;
