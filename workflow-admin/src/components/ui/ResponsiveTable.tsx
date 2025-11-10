'use client';

import React, { useState, useEffect, ReactNode } from 'react';
import { cn } from '@/lib/utils';

export interface ColumnConfig {
  key: string;
  label: string;
  priority?: number; // 1 = highest priority (always show), 5 = lowest (hide first)
  sortable?: boolean;
  render?: (value: any, row: any) => ReactNode;
  mobileLabel?: string; // Custom label for mobile card view
}

export interface ResponsiveTableProps {
  columns: ColumnConfig[];
  data: any[];
  keyExtractor: (row: any, index: number) => string | number;
  onSort?: (key: string, direction: 'asc' | 'desc') => void;
  showFilter?: boolean;
  onFilter?: (filters: Record<string, string>) => void;
  stickyHeader?: boolean;
  className?: string;
  emptyMessage?: string;
}

type SortState = {
  key: string;
  direction: 'asc' | 'desc';
} | null;

const ResponsiveTable: React.FC<ResponsiveTableProps> = ({
  columns,
  data,
  keyExtractor,
  onSort,
  showFilter = false,
  onFilter,
  stickyHeader = false,
  className,
  emptyMessage = 'No data available',
}) => {
  const [isMobile, setIsMobile] = useState(false);
  const [isTablet, setIsTablet] = useState(false);
  const [sortState, setSortState] = useState<SortState>(null);
  const [filters, setFilters] = useState<Record<string, string>>({});
  const [visibleColumns, setVisibleColumns] = useState<ColumnConfig[]>(columns);

  // Breakpoint detection
  useEffect(() => {
    const handleResize = () => {
      const width = window.innerWidth;
      setIsMobile(width < 640); // Mobile: < 640px
      setIsTablet(width >= 640 && width < 1024); // Tablet: 640-1024px

      // Hide columns based on priority and screen size
      if (width < 640) {
        // Mobile: Show only priority 1 columns in card view
        setVisibleColumns(columns);
      } else if (width < 768) {
        // Small tablet: Show priority 1-2
        setVisibleColumns(columns.filter(col => (col.priority || 3) <= 2));
      } else if (width < 1024) {
        // Tablet: Show priority 1-3
        setVisibleColumns(columns.filter(col => (col.priority || 3) <= 3));
      } else {
        // Desktop: Show all
        setVisibleColumns(columns);
      }
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [columns]);

  const handleSort = (key: string) => {
    if (!columns.find(col => col.key === key)?.sortable) return;

    const newDirection =
      sortState?.key === key && sortState.direction === 'asc' ? 'desc' : 'asc';

    setSortState({ key, direction: newDirection });
    onSort?.(key, newDirection);
  };

  const handleFilterChange = (key: string, value: string) => {
    const newFilters = { ...filters, [key]: value };
    setFilters(newFilters);
    onFilter?.(newFilters);
  };

  const renderSortIcon = (columnKey: string) => {
    if (sortState?.key !== columnKey) {
      return (
        <svg className="w-4 h-4 opacity-0 group-hover:opacity-40" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
        </svg>
      );
    }
    return sortState.direction === 'asc' ? (
      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 15l7-7 7 7" />
      </svg>
    ) : (
      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
      </svg>
    );
  };

  // Mobile Card View
  if (isMobile) {
    return (
      <div className={cn('space-y-4', className)}>
        {data.length === 0 ? (
          <div className="text-center py-12 text-gray-500 dark:text-gray-400 text-sm">
            {emptyMessage}
          </div>
        ) : (
          data.map((row, index) => (
            <div
              key={keyExtractor(row, index)}
              className="rounded-xl border border-gray-200 bg-white shadow-theme-sm dark:border-gray-800 dark:bg-gray-900 p-4 space-y-3"
            >
              {columns.map((column) => {
                const value = row[column.key];
                const displayValue = column.render ? column.render(value, row) : value;

                return (
                  <div key={column.key} className="flex justify-between items-start gap-4">
                    <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider min-w-[100px]">
                      {column.mobileLabel || column.label}
                    </span>
                    <span className="text-sm text-gray-900 dark:text-white/90 text-right flex-1">
                      {displayValue}
                    </span>
                  </div>
                );
              })}
            </div>
          ))
        )}
      </div>
    );
  }

  // Tablet/Desktop Table View
  return (
    <div className={cn('w-full', isTablet && 'overflow-x-auto', className)}>
      <table className="w-full caption-bottom text-sm">
        <thead
          className={cn(
            'bg-gray-50 dark:bg-gray-900',
            stickyHeader && 'sticky top-0 z-10'
          )}
        >
          <tr>
            {visibleColumns.map((column) => (
              <th
                key={column.key}
                className={cn(
                  'px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider',
                  column.sortable && 'cursor-pointer select-none group hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors'
                )}
                onClick={() => column.sortable && handleSort(column.key)}
              >
                <div className="flex items-center gap-2">
                  <span>{column.label}</span>
                  {column.sortable && renderSortIcon(column.key)}
                </div>
              </th>
            ))}
          </tr>
          {showFilter && (
            <tr className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
              {visibleColumns.map((column) => (
                <th key={`filter-${column.key}`} className="px-6 py-2">
                  <input
                    type="text"
                    placeholder={`Filter ${column.label.toLowerCase()}...`}
                    value={filters[column.key] || ''}
                    onChange={(e) => handleFilterChange(column.key, e.target.value)}
                    className="w-full px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-800 text-gray-900 dark:text-white focus:ring-2 focus:ring-brand-500/12 focus:border-brand-500 transition-colors"
                  />
                </th>
              ))}
            </tr>
          )}
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-800 bg-white dark:bg-gray-900">
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={visibleColumns.length}
                className="px-6 py-12 text-center text-gray-500 dark:text-gray-400 text-sm"
              >
                {emptyMessage}
              </td>
            </tr>
          ) : (
            data.map((row, index) => (
              <tr
                key={keyExtractor(row, index)}
                className="transition-colors hover:bg-gray-50 dark:hover:bg-white/5"
              >
                {visibleColumns.map((column) => {
                  const value = row[column.key];
                  const displayValue = column.render ? column.render(value, row) : value;

                  return (
                    <td
                      key={column.key}
                      className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white/90"
                    >
                      {displayValue}
                    </td>
                  );
                })}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
};

ResponsiveTable.displayName = 'ResponsiveTable';

export default ResponsiveTable;
