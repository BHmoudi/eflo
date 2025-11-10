'use client';

import React, { useEffect, useRef, useState } from 'react';
import { cn } from '@/lib/utils';

export interface DropdownItemProps {
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
  children: React.ReactNode;
  icon?: React.ReactNode;
  danger?: boolean;
}

export const DropdownItem = React.forwardRef<HTMLButtonElement, DropdownItemProps>(
  ({ onClick, disabled, className, children, icon, danger }, ref) => {
    return (
      <button
        ref={ref}
        type="button"
        onClick={onClick}
        disabled={disabled}
        className={cn(
          'w-full flex items-center gap-3 px-4 py-2.5 text-sm text-left',
          'transition-colors focus:outline-none',
          danger
            ? 'text-error-600 dark:text-error-500 hover:bg-error-50 dark:hover:bg-error-950/20 focus:bg-error-50 dark:focus:bg-error-950/20'
            : 'text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 focus:bg-gray-100 dark:focus:bg-gray-800',
          disabled && 'opacity-50 cursor-not-allowed hover:bg-transparent dark:hover:bg-transparent',
          className
        )}
      >
        {icon && <span className="flex items-center">{icon}</span>}
        {children}
      </button>
    );
  }
);

DropdownItem.displayName = 'DropdownItem';

export interface DropdownDividerProps {
  className?: string;
}

export const DropdownDivider = React.forwardRef<HTMLDivElement, DropdownDividerProps>(
  ({ className }, ref) => {
    return (
      <div
        ref={ref}
        className={cn('my-1 h-px bg-gray-200 dark:bg-gray-800', className)}
        role="separator"
      />
    );
  }
);

DropdownDivider.displayName = 'DropdownDivider';

export interface DropdownProps {
  trigger: React.ReactNode;
  children: React.ReactNode;
  align?: 'left' | 'right';
  className?: string;
  menuClassName?: string;
}

const Dropdown = React.forwardRef<HTMLDivElement, DropdownProps>(
  ({ trigger, children, align = 'left', className, menuClassName }, ref) => {
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef<HTMLDivElement>(null);
    const menuRef = useRef<HTMLDivElement>(null);

    // Handle click outside
    useEffect(() => {
      const handleClickOutside = (event: MouseEvent) => {
        if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
          setIsOpen(false);
        }
      };

      if (isOpen) {
        document.addEventListener('mousedown', handleClickOutside);
      }

      return () => {
        document.removeEventListener('mousedown', handleClickOutside);
      };
    }, [isOpen]);

    // Handle escape key
    useEffect(() => {
      const handleEscape = (event: KeyboardEvent) => {
        if (event.key === 'Escape' && isOpen) {
          setIsOpen(false);
        }
      };

      document.addEventListener('keydown', handleEscape);
      return () => document.removeEventListener('keydown', handleEscape);
    }, [isOpen]);

    // Keyboard navigation
    useEffect(() => {
      if (!isOpen || !menuRef.current) return;

      const items = menuRef.current.querySelectorAll<HTMLButtonElement>(
        'button:not([disabled])'
      );

      const handleKeyDown = (event: KeyboardEvent) => {
        if (!items.length) return;

        const currentIndex = Array.from(items).indexOf(
          document.activeElement as HTMLButtonElement
        );

        switch (event.key) {
          case 'ArrowDown':
            event.preventDefault();
            const nextIndex = currentIndex === items.length - 1 ? 0 : currentIndex + 1;
            items[nextIndex]?.focus();
            break;
          case 'ArrowUp':
            event.preventDefault();
            const prevIndex = currentIndex <= 0 ? items.length - 1 : currentIndex - 1;
            items[prevIndex]?.focus();
            break;
          case 'Home':
            event.preventDefault();
            items[0]?.focus();
            break;
          case 'End':
            event.preventDefault();
            items[items.length - 1]?.focus();
            break;
        }
      };

      document.addEventListener('keydown', handleKeyDown);
      return () => document.removeEventListener('keydown', handleKeyDown);
    }, [isOpen]);

    const handleToggle = () => {
      setIsOpen(!isOpen);
    };

    return (
      <div ref={ref || dropdownRef} className={cn('relative inline-block', className)}>
        {/* Trigger */}
        <div onClick={handleToggle} className="cursor-pointer">
          {trigger}
        </div>

        {/* Menu */}
        {isOpen && (
          <div
            ref={menuRef}
            className={cn(
              'absolute z-50 mt-2 min-w-[12rem] rounded-lg',
              'bg-white dark:bg-gray-900',
              'border border-gray-200 dark:border-gray-800',
              'shadow-lg',
              'py-1',
              'focus:outline-none',
              align === 'right' ? 'right-0' : 'left-0',
              menuClassName
            )}
            role="menu"
            aria-orientation="vertical"
          >
            {children}
          </div>
        )}
      </div>
    );
  }
);

Dropdown.displayName = 'Dropdown';

export default Dropdown;
