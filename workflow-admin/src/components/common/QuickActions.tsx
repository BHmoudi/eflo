import React, { useState, useEffect } from 'react';
import {
  Plus,
  FileText,
  Users,
  Settings,
  Search,
  Zap,
  Command,
} from 'lucide-react';
import { FloatingActionButton, FABAction } from '../ui/FloatingActionButton';

export interface QuickAction {
  id: string;
  label: string;
  icon: React.ReactNode;
  shortcut?: string;
  onClick: () => void;
  disabled?: boolean;
}

export interface QuickActionsProps {
  actions?: QuickAction[];
  position?: 'bottom-right' | 'bottom-left' | 'top-right' | 'top-left';
  darkMode?: boolean;
  showShortcuts?: boolean;
  onSearch?: () => void;
  className?: string;
}

export const QuickActions: React.FC<QuickActionsProps> = ({
  actions,
  position = 'bottom-right',
  darkMode = false,
  showShortcuts = true,
  onSearch,
  className = '',
}) => {
  const [activeShortcuts, setActiveShortcuts] = useState<Set<string>>(new Set());

  // Default actions if none provided
  const defaultActions: QuickAction[] = [
    {
      id: 'new-workflow',
      label: 'New Workflow',
      icon: <FileText size={20} />,
      shortcut: 'Ctrl+N',
      onClick: () => console.log('New workflow'),
    },
    {
      id: 'add-user',
      label: 'Add User',
      icon: <Users size={20} />,
      shortcut: 'Ctrl+U',
      onClick: () => console.log('Add user'),
    },
    {
      id: 'quick-search',
      label: 'Quick Search',
      icon: <Search size={20} />,
      shortcut: 'Ctrl+K',
      onClick: () => onSearch?.(),
    },
    {
      id: 'settings',
      label: 'Settings',
      icon: <Settings size={20} />,
      shortcut: 'Ctrl+,',
      onClick: () => console.log('Settings'),
    },
  ];

  const finalActions = actions || defaultActions;

  // Convert to FAB actions
  const fabActions: FABAction[] = finalActions.map(action => ({
    id: action.id,
    label: showShortcuts && action.shortcut
      ? `${action.label} (${action.shortcut})`
      : action.label,
    icon: action.icon,
    onClick: action.onClick,
    disabled: action.disabled,
  }));

  // Keyboard shortcut handler
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      const key = e.key.toLowerCase();
      const ctrl = e.ctrlKey || e.metaKey;
      const shift = e.shiftKey;
      const alt = e.altKey;

      finalActions.forEach(action => {
        if (!action.shortcut || action.disabled) return;

        const shortcut = action.shortcut.toLowerCase();
        const parts = shortcut.split('+').map(p => p.trim());

        const needsCtrl = parts.includes('ctrl') || parts.includes('cmd');
        const needsShift = parts.includes('shift');
        const needsAlt = parts.includes('alt');
        const targetKey = parts[parts.length - 1];

        if (
          ctrl === needsCtrl &&
          shift === needsShift &&
          alt === needsAlt &&
          key === targetKey
        ) {
          e.preventDefault();
          action.onClick();

          // Visual feedback
          setActiveShortcuts(prev => new Set(prev).add(action.id));
          setTimeout(() => {
            setActiveShortcuts(prev => {
              const next = new Set(prev);
              next.delete(action.id);
              return next;
            });
          }, 300);
        }
      });
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [finalActions]);

  return (
    <div className={className}>
      <FloatingActionButton
        actions={fabActions}
        position={position}
        darkMode={darkMode}
        mainIcon={<Zap size={24} />}
      />

      {/* Shortcut hints - shown briefly when triggered */}
      {activeShortcuts.size > 0 && (
        <div
          className={`
            fixed bottom-20 left-1/2 transform -translate-x-1/2
            px-4 py-2 rounded-lg shadow-lg
            flex items-center gap-2
            transition-all duration-300
            ${darkMode
              ? 'bg-gray-800 text-white border border-gray-700'
              : 'bg-white text-gray-900 border border-gray-200'
            }
            z-50
          `}
        >
          <Command size={16} className="text-blue-500" />
          <span className="text-sm font-medium">
            Action triggered
          </span>
        </div>
      )}
    </div>
  );
};

export default QuickActions;
