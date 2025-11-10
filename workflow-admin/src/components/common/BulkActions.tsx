import React, { useEffect, useState } from 'react';
import {
  CheckSquare,
  X,
  Trash2,
  Archive,
  Download,
  Copy,
  Edit,
  MoreHorizontal,
} from 'lucide-react';

export interface BulkAction {
  id: string;
  label: string;
  icon: React.ReactNode;
  onClick: (selectedIds: string[]) => void;
  variant?: 'default' | 'danger' | 'success';
  disabled?: boolean;
}

export interface BulkActionsProps {
  selectedIds: string[];
  totalCount: number;
  actions?: BulkAction[];
  darkMode?: boolean;
  onClearSelection: () => void;
  onSelectAll?: () => void;
  position?: 'top' | 'bottom';
  showSelectAll?: boolean;
  className?: string;
}

export const BulkActions: React.FC<BulkActionsProps> = ({
  selectedIds,
  totalCount,
  actions,
  darkMode = false,
  onClearSelection,
  onSelectAll,
  position = 'top',
  showSelectAll = true,
  className = '',
}) => {
  const [isVisible, setIsVisible] = useState(false);
  const [showMore, setShowMore] = useState(false);

  const selectedCount = selectedIds.length;
  const isAllSelected = selectedCount === totalCount && totalCount > 0;

  // Default actions if none provided
  const defaultActions: BulkAction[] = [
    {
      id: 'delete',
      label: 'Delete',
      icon: <Trash2 size={18} />,
      onClick: (ids) => console.log('Delete', ids),
      variant: 'danger',
    },
    {
      id: 'archive',
      label: 'Archive',
      icon: <Archive size={18} />,
      onClick: (ids) => console.log('Archive', ids),
    },
    {
      id: 'download',
      label: 'Download',
      icon: <Download size={18} />,
      onClick: (ids) => console.log('Download', ids),
    },
    {
      id: 'duplicate',
      label: 'Duplicate',
      icon: <Copy size={18} />,
      onClick: (ids) => console.log('Duplicate', ids),
    },
    {
      id: 'edit',
      label: 'Edit',
      icon: <Edit size={18} />,
      onClick: (ids) => console.log('Edit', ids),
    },
  ];

  const allActions = actions || defaultActions;

  // Show first 3 actions, rest in dropdown
  const visibleActions = allActions.slice(0, 3);
  const moreActions = allActions.slice(3);

  // Animate in/out based on selection
  useEffect(() => {
    if (selectedCount > 0) {
      setIsVisible(true);
    } else {
      // Delay hiding to allow animation
      const timeout = setTimeout(() => setIsVisible(false), 300);
      return () => clearTimeout(timeout);
    }
  }, [selectedCount]);

  // Close "more" menu when clicking outside
  useEffect(() => {
    const handleClickOutside = () => setShowMore(false);
    if (showMore) {
      window.addEventListener('click', handleClickOutside);
      return () => window.removeEventListener('click', handleClickOutside);
    }
  }, [showMore]);

  const getActionVariantClasses = (variant?: string) => {
    switch (variant) {
      case 'danger':
        return darkMode
          ? 'text-red-400 hover:bg-red-900 hover:bg-opacity-30'
          : 'text-red-600 hover:bg-red-50';
      case 'success':
        return darkMode
          ? 'text-green-400 hover:bg-green-900 hover:bg-opacity-30'
          : 'text-green-600 hover:bg-green-50';
      default:
        return darkMode
          ? 'text-gray-300 hover:bg-gray-700'
          : 'text-gray-700 hover:bg-gray-100';
    }
  };

  if (!isVisible) return null;

  return (
    <div
      className={`
        fixed ${position === 'top' ? 'top-20' : 'bottom-6'} left-1/2 transform -translate-x-1/2
        z-40 px-4 py-3 rounded-xl shadow-2xl
        flex items-center gap-4
        transition-all duration-300 ease-out
        ${selectedCount > 0
          ? 'opacity-100 translate-y-0'
          : 'opacity-0 -translate-y-4 pointer-events-none'
        }
        ${darkMode
          ? 'bg-gray-800 border border-gray-700'
          : 'bg-white border border-gray-200'
        }
        ${className}
      `}
      role="toolbar"
      aria-label="Bulk actions"
    >
      {/* Selection Info */}
      <div className="flex items-center gap-3">
        <div className={`
          p-2 rounded-lg
          ${darkMode ? 'bg-blue-900 bg-opacity-30' : 'bg-blue-50'}
        `}>
          <CheckSquare
            size={20}
            className={darkMode ? 'text-blue-400' : 'text-blue-600'}
          />
        </div>
        <div>
          <div className={`text-sm font-semibold ${darkMode ? 'text-white' : 'text-gray-900'}`}>
            {selectedCount} {selectedCount === 1 ? 'item' : 'items'} selected
          </div>
          {totalCount > 0 && (
            <div className={`text-xs ${darkMode ? 'text-gray-400' : 'text-gray-600'}`}>
              {Math.round((selectedCount / totalCount) * 100)}% of {totalCount} total
            </div>
          )}
        </div>
      </div>

      {/* Divider */}
      <div className={`h-8 w-px ${darkMode ? 'bg-gray-700' : 'bg-gray-200'}`} />

      {/* Actions */}
      <div className="flex items-center gap-2">
        {/* Select All Button */}
        {showSelectAll && onSelectAll && !isAllSelected && (
          <button
            onClick={onSelectAll}
            className={`
              px-3 py-2 rounded-lg text-sm font-medium
              transition-colors duration-150
              flex items-center gap-2
              ${darkMode
                ? 'bg-blue-600 hover:bg-blue-700 text-white'
                : 'bg-blue-500 hover:bg-blue-600 text-white'
              }
            `}
            aria-label="Select all items"
          >
            <CheckSquare size={16} />
            Select All ({totalCount})
          </button>
        )}

        {/* Action Buttons */}
        {visibleActions.map(action => (
          <button
            key={action.id}
            onClick={() => action.onClick(selectedIds)}
            disabled={action.disabled}
            className={`
              px-3 py-2 rounded-lg text-sm font-medium
              transition-colors duration-150
              flex items-center gap-2
              ${getActionVariantClasses(action.variant)}
              ${action.disabled
                ? 'opacity-50 cursor-not-allowed'
                : ''
              }
            `}
            aria-label={action.label}
          >
            {action.icon}
            {action.label}
          </button>
        ))}

        {/* More Actions Dropdown */}
        {moreActions.length > 0 && (
          <div className="relative">
            <button
              onClick={(e) => {
                e.stopPropagation();
                setShowMore(!showMore);
              }}
              className={`
                p-2 rounded-lg
                transition-colors duration-150
                ${darkMode
                  ? 'text-gray-300 hover:bg-gray-700'
                  : 'text-gray-700 hover:bg-gray-100'
                }
              `}
              aria-label="More actions"
              aria-expanded={showMore}
            >
              <MoreHorizontal size={18} />
            </button>

            {/* Dropdown Menu */}
            {showMore && (
              <div
                className={`
                  absolute ${position === 'top' ? 'top-full mt-2' : 'bottom-full mb-2'} right-0
                  min-w-[200px] rounded-lg shadow-xl py-1
                  ${darkMode
                    ? 'bg-gray-800 border border-gray-700'
                    : 'bg-white border border-gray-200'
                  }
                `}
              >
                {moreActions.map(action => (
                  <button
                    key={action.id}
                    onClick={() => {
                      action.onClick(selectedIds);
                      setShowMore(false);
                    }}
                    disabled={action.disabled}
                    className={`
                      w-full px-4 py-2 text-sm text-left
                      flex items-center gap-3
                      transition-colors duration-150
                      ${getActionVariantClasses(action.variant)}
                      ${action.disabled
                        ? 'opacity-50 cursor-not-allowed'
                        : ''
                      }
                    `}
                  >
                    {action.icon}
                    {action.label}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Divider */}
      <div className={`h-8 w-px ${darkMode ? 'bg-gray-700' : 'bg-gray-200'}`} />

      {/* Clear Selection Button */}
      <button
        onClick={onClearSelection}
        className={`
          p-2 rounded-lg
          transition-colors duration-150
          ${darkMode
            ? 'text-gray-400 hover:bg-gray-700 hover:text-white'
            : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
          }
        `}
        aria-label="Clear selection"
      >
        <X size={18} />
      </button>
    </div>
  );
};

export default BulkActions;
