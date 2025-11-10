'use client';

import React, { useState, useEffect, useMemo, ReactNode } from 'react';
import { cn } from '@/lib/utils';
import Checkbox from './Checkbox';
import Button from './Button';
import TableSkeleton from './TableSkeleton';

export interface DataTableColumn<T = any> {
  key: string;
  label: string;
  sortable?: boolean;
  render?: (value: any, row: T, index: number) => ReactNode;
  className?: string;
}

export interface BulkAction {
  label: string;
  icon?: ReactNode;
  onClick: (selectedIds: (string | number)[]) => void;
  variant?: 'primary' | 'secondary' | 'danger';
}

export interface DataTableProps<T = any> {
  columns: DataTableColumn<T>[];
  data: T[];
  keyExtractor: (row: T, index: number) => string | number;
  isLoading?: boolean;
  searchable?: boolean;
  searchPlaceholder?: string;
  searchKeys?: string[]; // Keys to search in
  sortable?: boolean;
  selectable?: boolean;
  bulkActions?: BulkAction[];
  pagination?: boolean;
  pageSize?: number;
  pageSizeOptions?: number[];
  emptyMessage?: string;
  emptyIcon?: ReactNode;
  onExport?: (data: T[]) => void;
  className?: string;
  stickyHeader?: boolean;
}

type SortConfig = {
  key: string;
  direction: 'asc' | 'desc';
} | null;

function DataTable<T = any>({
  columns,
  data,
  keyExtractor,
  isLoading = false,
  searchable = true,
  searchPlaceholder = 'Search...',
  searchKeys = [],
  sortable = true,
  selectable = false,
  bulkActions = [],
  pagination = true,
  pageSize: initialPageSize = 10,
  pageSizeOptions = [5, 10, 25, 50, 100],
  emptyMessage = 'No data available',
  emptyIcon,
  onExport,
  className,
  stickyHeader = false,
}: DataTableProps<T>) {
  const [searchQuery, setSearchQuery] = useState('');
  const [sortConfig, setSortConfig] = useState<SortConfig>(null);
  const [selectedIds, setSelectedIds] = useState<Set<string | number>>(new Set());
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(initialPageSize);
  const [isMobile, setIsMobile] = useState(false);

  // Detect mobile
  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 640);
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // Filter data based on search
  const filteredData = useMemo(() => {
    // Ensure data is an array
    if (!Array.isArray(data)) return [];
    if (!searchQuery.trim()) return data;

    const query = searchQuery.toLowerCase();
    return data.filter((row: any) => {
      if (searchKeys.length > 0) {
        return searchKeys.some(key => {
          const value = row[key];
          return value && String(value).toLowerCase().includes(query);
        });
      }

      // Search all string values if no keys specified
      return Object.values(row).some(value =>
        value && String(value).toLowerCase().includes(query)
      );
    });
  }, [data, searchQuery, searchKeys]);

  // Sort data
  const sortedData = useMemo(() => {
    // Ensure filteredData is an array
    if (!Array.isArray(filteredData)) return [];
    if (!sortConfig) return filteredData;

    return [...filteredData].sort((a: any, b: any) => {
      const aValue = a[sortConfig.key];
      const bValue = b[sortConfig.key];

      if (aValue === bValue) return 0;
      if (aValue == null) return 1;
      if (bValue == null) return -1;

      const comparison = aValue < bValue ? -1 : 1;
      return sortConfig.direction === 'asc' ? comparison : -comparison;
    });
  }, [filteredData, sortConfig]);

  // Paginate data
  const paginatedData = useMemo(() => {
    // Ensure sortedData is an array
    if (!Array.isArray(sortedData)) return [];
    if (!pagination) return sortedData;

    const startIndex = (currentPage - 1) * pageSize;
    return sortedData.slice(startIndex, startIndex + pageSize);
  }, [sortedData, currentPage, pageSize, pagination]);

  const totalPages = Array.isArray(sortedData) ? Math.ceil(sortedData.length / pageSize) : 0;

  // Reset to first page when data changes
  useEffect(() => {
    setCurrentPage(1);
  }, [searchQuery, sortConfig, pageSize]);

  const handleSort = (key: string) => {
    if (!sortable) return;
    const column = columns.find(col => col.key === key);
    if (!column?.sortable) return;

    setSortConfig(current => ({
      key,
      direction: current?.key === key && current.direction === 'asc' ? 'desc' : 'asc',
    }));
  };

  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      const allIds = paginatedData.map((row, index) => keyExtractor(row, index));
      setSelectedIds(new Set(allIds));
    } else {
      setSelectedIds(new Set());
    }
  };

  const handleSelectRow = (id: string | number, checked: boolean) => {
    const newSelected = new Set(selectedIds);
    if (checked) {
      newSelected.add(id);
    } else {
      newSelected.delete(id);
    }
    setSelectedIds(newSelected);
  };

  const isAllSelected = paginatedData.length > 0 && selectedIds.size === paginatedData.length;
  const isSomeSelected = selectedIds.size > 0 && !isAllSelected;

  const renderSortIcon = (columnKey: string) => {
    if (sortConfig?.key !== columnKey) {
      return (
        <svg className="w-4 h-4 opacity-0 group-hover:opacity-40 transition-opacity" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
        </svg>
      );
    }
    return sortConfig.direction === 'asc' ? (
      <svg className="w-4 h-4 text-brand-600 dark:text-brand-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 15l7-7 7 7" />
      </svg>
    ) : (
      <svg className="w-4 h-4 text-brand-600 dark:text-brand-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
      </svg>
    );
  };

  const renderEmptyState = () => (
    <div className="text-center py-12">
      {emptyIcon && <div className="flex justify-center mb-4">{emptyIcon}</div>}
      <p className="text-gray-500 dark:text-gray-400 text-sm">{emptyMessage}</p>
      {searchQuery && (
        <Button
          variant="ghost"
          size="sm"
          onClick={() => setSearchQuery('')}
          className="mt-4"
        >
          Clear search
        </Button>
      )}
    </div>
  );

  // Mobile Card View
  const renderMobileView = () => (
    <div className="space-y-4">
      {paginatedData.map((row, index) => {
        const rowKey = keyExtractor(row, index);
        const isSelected = selectedIds.has(rowKey);

        return (
          <div
            key={rowKey}
            className={cn(
              'rounded-xl border bg-white shadow-theme-sm dark:bg-gray-900 p-4',
              isSelected
                ? 'border-brand-500 dark:border-brand-400 ring-2 ring-brand-500/12'
                : 'border-gray-200 dark:border-gray-800'
            )}
          >
            {selectable && (
              <div className="mb-3 pb-3 border-b border-gray-200 dark:border-gray-800">
                <Checkbox
                  checked={isSelected}
                  onChange={(e) => handleSelectRow(rowKey, e.target.checked)}
                />
              </div>
            )}
            <div className="space-y-3">
              {columns.map((column) => {
                const value = (row as any)[column.key];
                const displayValue = column.render
                  ? column.render(value, row, index)
                  : value;

                return (
                  <div key={column.key} className="flex justify-between items-start gap-4">
                    <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider min-w-[100px]">
                      {column.label}
                    </span>
                    <span className="text-sm text-gray-900 dark:text-white/90 text-right flex-1">
                      {displayValue}
                    </span>
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );

  // Desktop Table View
  const renderTableView = () => (
    <div className="w-full overflow-x-auto">
      <table className="w-full caption-bottom text-sm">
        <thead
          className={cn(
            'bg-gray-50 dark:bg-gray-900',
            stickyHeader && 'sticky top-0 z-10'
          )}
        >
          <tr>
            {selectable && (
              <th className="px-6 py-3 w-12">
                <Checkbox
                  checked={isAllSelected}
                  indeterminate={isSomeSelected}
                  onChange={(e) => handleSelectAll(e.target.checked)}
                />
              </th>
            )}
            {columns.map((column) => (
              <th
                key={column.key}
                className={cn(
                  'px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-400 uppercase tracking-wider',
                  column.sortable && sortable && 'cursor-pointer select-none group hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors',
                  column.className
                )}
                onClick={() => column.sortable && handleSort(column.key)}
              >
                <div className="flex items-center gap-2">
                  <span>{column.label}</span>
                  {column.sortable && sortable && renderSortIcon(column.key)}
                </div>
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 dark:divide-gray-800 bg-white dark:bg-gray-900">
          {paginatedData.map((row, index) => {
            const rowKey = keyExtractor(row, index);
            const isSelected = selectedIds.has(rowKey);

            return (
              <tr
                key={rowKey}
                className={cn(
                  'transition-colors',
                  isSelected
                    ? 'bg-brand-50 dark:bg-brand-500/10'
                    : 'hover:bg-gray-50 dark:hover:bg-white/5'
                )}
              >
                {selectable && (
                  <td className="px-6 py-4">
                    <Checkbox
                      checked={isSelected}
                      onChange={(e) => handleSelectRow(rowKey, e.target.checked)}
                    />
                  </td>
                )}
                {columns.map((column) => {
                  const value = (row as any)[column.key];
                  const displayValue = column.render
                    ? column.render(value, row, index)
                    : value;

                  return (
                    <td
                      key={column.key}
                      className={cn(
                        'px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white/90',
                        column.className
                      )}
                    >
                      {displayValue}
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );

  return (
    <div className={cn('space-y-4', className)}>
      {/* Header with Search and Actions */}
      <div className="flex flex-col sm:flex-row gap-4 justify-between items-start sm:items-center">
        <div className="flex-1 w-full sm:w-auto">
          {searchable && (
            <div className="relative">
              <svg
                className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
              </svg>
              <input
                type="text"
                placeholder={searchPlaceholder}
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 text-sm border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white placeholder-gray-400 focus:ring-2 focus:ring-brand-500/12 focus:border-brand-500 transition-colors"
              />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery('')}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              )}
            </div>
          )}
        </div>

        {onExport && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => onExport(sortedData)}
            startIcon={
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            }
          >
            Export
          </Button>
        )}
      </div>

      {/* Bulk Actions Toolbar */}
      {selectable && selectedIds.size > 0 && bulkActions.length > 0 && (
        <div className="flex items-center gap-3 p-4 bg-brand-50 dark:bg-brand-500/10 border border-brand-200 dark:border-brand-500/20 rounded-lg">
          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
            {selectedIds.size} selected
          </span>
          <div className="flex gap-2 flex-wrap">
            {bulkActions.map((action, index) => (
              <Button
                key={index}
                variant={action.variant || 'secondary'}
                size="sm"
                onClick={() => action.onClick(Array.from(selectedIds))}
                startIcon={action.icon}
              >
                {action.label}
              </Button>
            ))}
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setSelectedIds(new Set())}
            className="ml-auto"
          >
            Clear selection
          </Button>
        </div>
      )}

      {/* Table Content */}
      {isLoading ? (
        <TableSkeleton rows={pageSize} columns={columns.length + (selectable ? 1 : 0)} />
      ) : sortedData.length === 0 ? (
        renderEmptyState()
      ) : isMobile ? (
        renderMobileView()
      ) : (
        renderTableView()
      )}

      {/* Pagination */}
      {pagination && sortedData.length > 0 && (
        <div className="flex flex-col sm:flex-row gap-4 justify-between items-center pt-4 border-t border-gray-200 dark:border-gray-800">
          <div className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400">
            <span>Rows per page:</span>
            <select
              value={pageSize}
              onChange={(e) => setPageSize(Number(e.target.value))}
              className="px-3 py-1.5 border border-gray-300 dark:border-gray-700 rounded-lg bg-white dark:bg-gray-900 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-brand-500/12 focus:border-brand-500 transition-colors"
            >
              {pageSizeOptions.map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-600 dark:text-gray-400">
              {((currentPage - 1) * pageSize) + 1}-{Math.min(currentPage * pageSize, sortedData.length)} of {sortedData.length}
            </span>
            <div className="flex gap-1">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(1)}
                disabled={currentPage === 1}
                className="px-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
                </svg>
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="px-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="px-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(totalPages)}
                disabled={currentPage === totalPages}
                className="px-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 5l7 7-7 7M5 5l7 7-7 7" />
                </svg>
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

DataTable.displayName = 'DataTable';

export default DataTable;
