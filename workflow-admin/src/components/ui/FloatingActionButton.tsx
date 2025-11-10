import React, { useState } from 'react';
import { Plus, X } from 'lucide-react';

export interface FABAction {
  id: string;
  label: string;
  icon: React.ReactNode;
  onClick: () => void;
  disabled?: boolean;
}

export interface FloatingActionButtonProps {
  actions: FABAction[];
  position?: 'bottom-right' | 'bottom-left' | 'top-right' | 'top-left';
  mainIcon?: React.ReactNode;
  darkMode?: boolean;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const FloatingActionButton: React.FC<FloatingActionButtonProps> = ({
  actions,
  position = 'bottom-right',
  mainIcon,
  darkMode = false,
  size = 'md',
  className = '',
}) => {
  const [isExpanded, setIsExpanded] = useState(false);

  const sizeClasses = {
    sm: 'w-12 h-12',
    md: 'w-14 h-14',
    lg: 'w-16 h-16',
  };

  const iconSizes = {
    sm: 20,
    md: 24,
    lg: 28,
  };

  const positionClasses = {
    'bottom-right': 'bottom-6 right-6',
    'bottom-left': 'bottom-6 left-6',
    'top-right': 'top-6 right-6',
    'top-left': 'top-6 left-6',
  };

  const getActionPosition = (index: number) => {
    const spacing = size === 'sm' ? 60 : size === 'md' ? 70 : 80;
    const offset = (index + 1) * spacing;

    if (position.includes('bottom')) {
      return { bottom: `${offset}px` };
    } else {
      return { top: `${offset}px` };
    }
  };

  const handleMainClick = () => {
    setIsExpanded(!isExpanded);
  };

  const handleActionClick = (action: FABAction) => {
    if (!action.disabled) {
      action.onClick();
      setIsExpanded(false);
    }
  };

  return (
    <>
      {/* Backdrop */}
      {isExpanded && (
        <div
          className="fixed inset-0 bg-black bg-opacity-20 transition-opacity duration-200 z-40"
          onClick={() => setIsExpanded(false)}
          aria-hidden="true"
        />
      )}

      {/* FAB Container */}
      <div className={`fixed ${positionClasses[position]} z-50 ${className}`}>
        {/* Action Buttons */}
        <div className="relative">
          {actions.map((action, index) => (
            <div
              key={action.id}
              className={`
                absolute ${position.includes('right') ? 'right-0' : 'left-0'}
                transition-all duration-300 ease-out
                ${isExpanded
                  ? 'opacity-100 scale-100'
                  : 'opacity-0 scale-50 pointer-events-none'
                }
              `}
              style={{
                ...getActionPosition(index),
                transitionDelay: isExpanded ? `${index * 50}ms` : '0ms',
              }}
            >
              <div className="flex items-center gap-3">
                {position.includes('right') && (
                  <span
                    className={`
                      px-3 py-2 rounded-lg shadow-lg text-sm font-medium whitespace-nowrap
                      ${darkMode
                        ? 'bg-gray-800 text-white border border-gray-700'
                        : 'bg-white text-gray-900 border border-gray-200'
                      }
                    `}
                  >
                    {action.label}
                  </span>
                )}

                <button
                  onClick={() => handleActionClick(action)}
                  disabled={action.disabled}
                  className={`
                    ${sizeClasses[size]} rounded-full shadow-lg
                    flex items-center justify-center
                    transition-all duration-200
                    ${darkMode
                      ? 'bg-gray-700 text-white hover:bg-gray-600'
                      : 'bg-white text-gray-700 hover:bg-gray-50'
                    }
                    ${action.disabled
                      ? 'opacity-50 cursor-not-allowed'
                      : 'hover:scale-110 active:scale-95'
                    }
                    focus:outline-none focus:ring-2 focus:ring-offset-2
                    ${darkMode ? 'focus:ring-blue-500' : 'focus:ring-blue-400'}
                  `}
                  aria-label={action.label}
                >
                  {action.icon}
                </button>

                {position.includes('left') && (
                  <span
                    className={`
                      px-3 py-2 rounded-lg shadow-lg text-sm font-medium whitespace-nowrap
                      ${darkMode
                        ? 'bg-gray-800 text-white border border-gray-700'
                        : 'bg-white text-gray-900 border border-gray-200'
                      }
                    `}
                  >
                    {action.label}
                  </span>
                )}
              </div>
            </div>
          ))}
        </div>

        {/* Main FAB Button */}
        <button
          onClick={handleMainClick}
          className={`
            ${sizeClasses[size]} rounded-full shadow-2xl
            flex items-center justify-center
            transition-all duration-300
            ${darkMode
              ? 'bg-blue-600 hover:bg-blue-700 text-white'
              : 'bg-blue-500 hover:bg-blue-600 text-white'
            }
            hover:scale-110 active:scale-95
            focus:outline-none focus:ring-4 focus:ring-blue-300
            ${isExpanded ? 'rotate-45' : 'rotate-0'}
          `}
          aria-label={isExpanded ? 'Close menu' : 'Open menu'}
          aria-expanded={isExpanded}
        >
          {mainIcon || (isExpanded ? (
            <X size={iconSizes[size]} />
          ) : (
            <Plus size={iconSizes[size]} />
          ))}
        </button>
      </div>
    </>
  );
};

export default FloatingActionButton;
