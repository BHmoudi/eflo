import React from 'react';
import { cn } from '@/lib/utils';
import Button from '@/components/ui/Button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  showPageNumbers?: boolean;
  maxPageNumbers?: number;
  className?: string;
  size?: 'sm' | 'md';
}

const Pagination: React.FC<PaginationProps> = ({
  currentPage,
  totalPages,
  onPageChange,
  showPageNumbers = true,
  maxPageNumbers = 7,
  className,
  size = 'md'
}) => {
  if (totalPages <= 1) return null;

  const getPageNumbers = () => {
    const pages: (number | string)[] = [];

    if (totalPages <= maxPageNumbers) {
      // Show all pages if total is less than max
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      // Show first page
      pages.push(1);

      // Calculate start and end of middle section
      let start = Math.max(2, currentPage - 1);
      let end = Math.min(totalPages - 1, currentPage + 1);

      // Adjust if near start
      if (currentPage <= 3) {
        start = 2;
        end = Math.min(maxPageNumbers - 2, totalPages - 1);
      }

      // Adjust if near end
      if (currentPage >= totalPages - 2) {
        start = Math.max(2, totalPages - maxPageNumbers + 3);
        end = totalPages - 1;
      }

      // Add ellipsis after first page if needed
      if (start > 2) {
        pages.push('...');
      }

      // Add middle pages
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }

      // Add ellipsis before last page if needed
      if (end < totalPages - 1) {
        pages.push('...');
      }

      // Show last page
      pages.push(totalPages);
    }

    return pages;
  };

  const pageNumbers = showPageNumbers ? getPageNumbers() : [];

  const buttonSize = size === 'sm' ? 'sm' : 'md';
  const pageButtonClasses = size === 'sm'
    ? 'h-8 w-8 min-w-[2rem]'
    : 'h-10 w-10 min-w-[2.5rem]';

  return (
    <div className={cn('flex items-center justify-center gap-2', className)}>
      {/* Previous Button */}
      <Button
        variant="outline"
        size={buttonSize}
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        aria-label="Previous page"
        className={pageButtonClasses}
      >
        <ChevronLeft className={size === 'sm' ? 'h-4 w-4' : 'h-5 w-5'} />
      </Button>

      {/* Page Numbers */}
      {showPageNumbers && (
        <div className="flex items-center gap-1">
          {pageNumbers.map((page, index) => {
            if (page === '...') {
              return (
                <span
                  key={`ellipsis-${index}`}
                  className={cn(
                    'flex items-center justify-center text-gray-500 dark:text-gray-400',
                    pageButtonClasses
                  )}
                >
                  ...
                </span>
              );
            }

            const pageNumber = page as number;
            const isActive = pageNumber === currentPage;

            return (
              <button
                key={pageNumber}
                onClick={() => onPageChange(pageNumber)}
                disabled={isActive}
                className={cn(
                  'flex items-center justify-center rounded-lg font-medium transition-colors',
                  'focus:outline-none focus:ring-4 focus:ring-brand-500/12',
                  pageButtonClasses,
                  size === 'sm' ? 'text-sm' : 'text-sm',
                  isActive
                    ? 'bg-brand-500 text-white cursor-default'
                    : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 border border-gray-200 dark:border-gray-700'
                )}
                aria-label={`Go to page ${pageNumber}`}
                aria-current={isActive ? 'page' : undefined}
              >
                {pageNumber}
              </button>
            );
          })}
        </div>
      )}

      {/* Next Button */}
      <Button
        variant="outline"
        size={buttonSize}
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        aria-label="Next page"
        className={pageButtonClasses}
      >
        <ChevronRight className={size === 'sm' ? 'h-4 w-4' : 'h-5 w-5'} />
      </Button>
    </div>
  );
};

Pagination.displayName = 'Pagination';

export default Pagination;
