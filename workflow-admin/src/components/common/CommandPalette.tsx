import React, { useState, useEffect, useRef, useMemo } from 'react';
import { createPortal } from 'react-dom';
import {
  Search,
  Clock,
  Hash,
  ChevronRight,
  FileText,
  Users,
  Settings,
  Home,
  BarChart,
  HelpCircle,
  Command,
  ArrowUp,
  CornerDownLeft,
} from 'lucide-react';

export interface CommandAction {
  id: string;
  label: string;
  description?: string;
  icon?: React.ReactNode;
  category: 'pages' | 'actions' | 'help' | 'recent';
  keywords?: string[];
  shortcut?: string;
  onSelect: () => void;
}

export interface CommandPaletteProps {
  actions?: CommandAction[];
  darkMode?: boolean;
  maxRecent?: number;
  placeholder?: string;
  isOpen?: boolean;
  onClose?: () => void;
}

export const CommandPalette: React.FC<CommandPaletteProps> = ({
  actions,
  darkMode = false,
  maxRecent = 5,
  placeholder = 'Type a command or search...',
  isOpen: isOpenProp,
  onClose,
}) => {
  const [isOpenInternal, setIsOpenInternal] = useState(false);
  const isOpen = isOpenProp !== undefined ? isOpenProp : isOpenInternal;
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [recentActions, setRecentActions] = useState<string[]>([]);
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  // Default actions
  const defaultActions: CommandAction[] = [
    // Pages
    {
      id: 'nav-home',
      label: 'Go to Home',
      description: 'Navigate to home page',
      icon: <Home size={18} />,
      category: 'pages',
      keywords: ['home', 'dashboard'],
      onSelect: () => console.log('Navigate to home'),
    },
    {
      id: 'nav-workflows',
      label: 'Go to Workflows',
      description: 'Navigate to workflows page',
      icon: <FileText size={18} />,
      category: 'pages',
      keywords: ['workflows', 'processes'],
      onSelect: () => console.log('Navigate to workflows'),
    },
    {
      id: 'nav-users',
      label: 'Go to Users',
      description: 'Navigate to users page',
      icon: <Users size={18} />,
      category: 'pages',
      keywords: ['users', 'team', 'people'],
      onSelect: () => console.log('Navigate to users'),
    },
    {
      id: 'nav-analytics',
      label: 'Go to Analytics',
      description: 'Navigate to analytics page',
      icon: <BarChart size={18} />,
      category: 'pages',
      keywords: ['analytics', 'reports', 'stats'],
      onSelect: () => console.log('Navigate to analytics'),
    },
    {
      id: 'nav-settings',
      label: 'Go to Settings',
      description: 'Navigate to settings page',
      icon: <Settings size={18} />,
      category: 'pages',
      keywords: ['settings', 'preferences', 'config'],
      shortcut: 'Ctrl+,',
      onSelect: () => console.log('Navigate to settings'),
    },
    // Actions
    {
      id: 'action-new-workflow',
      label: 'Create New Workflow',
      description: 'Create a new workflow',
      icon: <FileText size={18} />,
      category: 'actions',
      keywords: ['new', 'create', 'workflow'],
      shortcut: 'Ctrl+N',
      onSelect: () => console.log('Create workflow'),
    },
    {
      id: 'action-add-user',
      label: 'Add New User',
      description: 'Add a new user to the system',
      icon: <Users size={18} />,
      category: 'actions',
      keywords: ['add', 'new', 'user'],
      shortcut: 'Ctrl+U',
      onSelect: () => console.log('Add user'),
    },
    // Help
    {
      id: 'help-docs',
      label: 'View Documentation',
      description: 'Open documentation',
      icon: <HelpCircle size={18} />,
      category: 'help',
      keywords: ['help', 'docs', 'documentation'],
      onSelect: () => console.log('Open docs'),
    },
    {
      id: 'help-shortcuts',
      label: 'View Keyboard Shortcuts',
      description: 'See all keyboard shortcuts',
      icon: <Command size={18} />,
      category: 'help',
      keywords: ['help', 'keyboard', 'shortcuts'],
      shortcut: '?',
      onSelect: () => console.log('Show shortcuts'),
    },
  ];

  const allActions = actions || defaultActions;

  // Load recent actions from localStorage
  useEffect(() => {
    const stored = localStorage.getItem('commandPalette_recent');
    if (stored) {
      try {
        setRecentActions(JSON.parse(stored));
      } catch (e) {
        console.error('Failed to load recent actions', e);
      }
    }
  }, []);

  // Save recent actions to localStorage
  const addToRecent = (actionId: string) => {
    setRecentActions(prev => {
      const updated = [actionId, ...prev.filter(id => id !== actionId)].slice(0, maxRecent);
      localStorage.setItem('commandPalette_recent', JSON.stringify(updated));
      return updated;
    });
  };

  // Fuzzy search implementation
  const fuzzyMatch = (str: string, pattern: string): boolean => {
    const strLower = str.toLowerCase();
    const patternLower = pattern.toLowerCase();

    let patternIdx = 0;
    for (let strIdx = 0; strIdx < strLower.length; strIdx++) {
      if (strLower[strIdx] === patternLower[patternIdx]) {
        patternIdx++;
      }
      if (patternIdx === patternLower.length) return true;
    }
    return patternIdx === patternLower.length;
  };

  // Filter and sort actions
  const filteredActions = useMemo(() => {
    if (!searchQuery.trim()) {
      // Show recent actions when no search query
      const recent = recentActions
        .map(id => allActions.find(a => a.id === id))
        .filter(Boolean) as CommandAction[];

      return recent.length > 0 ? recent : allActions;
    }

    return allActions.filter(action => {
      const searchableText = [
        action.label,
        action.description || '',
        ...(action.keywords || []),
      ].join(' ');

      return fuzzyMatch(searchableText, searchQuery);
    });
  }, [searchQuery, allActions, recentActions]);

  // Group actions by category
  const groupedActions = useMemo(() => {
    const groups: Record<string, CommandAction[]> = {};

    filteredActions.forEach(action => {
      const category = searchQuery.trim() ? action.category : 'recent';
      if (!groups[category]) {
        groups[category] = [];
      }
      groups[category].push(action);
    });

    return groups;
  }, [filteredActions, searchQuery]);

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Cmd/Ctrl + K to open (only if not controlled externally)
      if ((e.metaKey || e.ctrlKey) && e.key === 'k' && isOpenProp === undefined) {
        e.preventDefault();
        setIsOpenInternal(prev => !prev);
        return;
      }

      // Escape to close
      if (e.key === 'Escape' && isOpen) {
        e.preventDefault();
        handleClose();
        return;
      }

      if (!isOpen) return;

      // Arrow navigation
      if (e.key === 'ArrowDown') {
        e.preventDefault();
        setSelectedIndex(prev =>
          prev < filteredActions.length - 1 ? prev + 1 : prev
        );
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        setSelectedIndex(prev => (prev > 0 ? prev - 1 : prev));
      } else if (e.key === 'Enter') {
        e.preventDefault();
        const action = filteredActions[selectedIndex];
        if (action) {
          handleSelect(action);
        }
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, selectedIndex, filteredActions]);

  // Auto-focus input when opened
  useEffect(() => {
    if (isOpen && inputRef.current) {
      inputRef.current.focus();
    }
  }, [isOpen]);

  // Scroll selected item into view
  useEffect(() => {
    if (listRef.current) {
      const selected = listRef.current.querySelector(`[data-index="${selectedIndex}"]`);
      if (selected) {
        selected.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
      }
    }
  }, [selectedIndex]);

  const handleClose = () => {
    if (isOpenProp === undefined) {
      setIsOpenInternal(false);
    }
    setSearchQuery('');
    setSelectedIndex(0);
    onClose?.();
  };

  const handleSelect = (action: CommandAction) => {
    action.onSelect();
    addToRecent(action.id);
    handleClose();
  };

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case 'pages': return <Home size={16} />;
      case 'actions': return <FileText size={16} />;
      case 'help': return <HelpCircle size={16} />;
      case 'recent': return <Clock size={16} />;
      default: return <Hash size={16} />;
    }
  };

  const getCategoryLabel = (category: string) => {
    switch (category) {
      case 'pages': return 'Pages';
      case 'actions': return 'Actions';
      case 'help': return 'Help';
      case 'recent': return 'Recent';
      default: return category;
    }
  };

  if (!isOpen) return null;

  const modal = (
    <div
      className="fixed inset-0 z-[9999] flex items-start justify-center pt-20 px-4"
      onClick={handleClose}
    >
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black bg-opacity-50 backdrop-blur-sm" />

      {/* Command Palette */}
      <div
        className={`
          relative w-full max-w-2xl rounded-xl shadow-2xl
          ${darkMode
            ? 'bg-gray-900 border border-gray-700'
            : 'bg-white border border-gray-200'
          }
          overflow-hidden
        `}
        onClick={e => e.stopPropagation()}
      >
        {/* Search Input */}
        <div className="flex items-center gap-3 px-4 py-3 border-b border-gray-200 dark:border-gray-700">
          <Search
            size={20}
            className={darkMode ? 'text-gray-400' : 'text-gray-500'}
          />
          <input
            ref={inputRef}
            type="text"
            value={searchQuery}
            onChange={e => {
              setSearchQuery(e.target.value);
              setSelectedIndex(0);
            }}
            placeholder={placeholder}
            className={`
              flex-1 bg-transparent outline-none text-base
              ${darkMode ? 'text-white placeholder-gray-500' : 'text-gray-900 placeholder-gray-400'}
            `}
            aria-label="Command palette search"
          />
          <div className="flex items-center gap-2 text-xs text-gray-500">
            <kbd className="px-2 py-1 rounded bg-gray-100 dark:bg-gray-800">
              <ArrowUp size={12} />
            </kbd>
            <kbd className="px-2 py-1 rounded bg-gray-100 dark:bg-gray-800">
              <CornerDownLeft size={12} />
            </kbd>
          </div>
        </div>

        {/* Results */}
        <div
          ref={listRef}
          className="max-h-96 overflow-y-auto"
        >
          {Object.keys(groupedActions).length === 0 ? (
            <div className="px-4 py-8 text-center text-gray-500">
              No results found
            </div>
          ) : (
            Object.entries(groupedActions).map(([category, categoryActions]) => (
              <div key={category}>
                {/* Category Header */}
                <div className={`
                  px-4 py-2 text-xs font-semibold uppercase tracking-wider
                  flex items-center gap-2
                  ${darkMode ? 'text-gray-400' : 'text-gray-600'}
                `}>
                  {getCategoryIcon(category)}
                  {getCategoryLabel(category)}
                </div>

                {/* Actions */}
                {categoryActions.map((action, index) => {
                  const globalIndex = filteredActions.indexOf(action);
                  const isSelected = globalIndex === selectedIndex;

                  return (
                    <button
                      key={action.id}
                      data-index={globalIndex}
                      onClick={() => handleSelect(action)}
                      className={`
                        w-full px-4 py-3 flex items-center gap-3
                        transition-colors duration-150
                        ${isSelected
                          ? darkMode
                            ? 'bg-blue-900 bg-opacity-30 border-l-2 border-blue-500'
                            : 'bg-blue-50 border-l-2 border-blue-500'
                          : darkMode
                            ? 'hover:bg-gray-800'
                            : 'hover:bg-gray-50'
                        }
                      `}
                    >
                      {/* Icon */}
                      <div className={`
                        flex-shrink-0
                        ${isSelected
                          ? 'text-blue-500'
                          : darkMode ? 'text-gray-400' : 'text-gray-600'
                        }
                      `}>
                        {action.icon || <Hash size={18} />}
                      </div>

                      {/* Label and Description */}
                      <div className="flex-1 text-left">
                        <div className={`
                          text-sm font-medium
                          ${darkMode ? 'text-white' : 'text-gray-900'}
                        `}>
                          {action.label}
                        </div>
                        {action.description && (
                          <div className={`
                            text-xs
                            ${darkMode ? 'text-gray-400' : 'text-gray-600'}
                          `}>
                            {action.description}
                          </div>
                        )}
                      </div>

                      {/* Shortcut or Arrow */}
                      <div className="flex-shrink-0">
                        {action.shortcut ? (
                          <kbd className={`
                            px-2 py-1 rounded text-xs font-mono
                            ${darkMode
                              ? 'bg-gray-800 text-gray-400'
                              : 'bg-gray-100 text-gray-600'
                            }
                          `}>
                            {action.shortcut}
                          </kbd>
                        ) : (
                          <ChevronRight
                            size={16}
                            className={darkMode ? 'text-gray-600' : 'text-gray-400'}
                          />
                        )}
                      </div>
                    </button>
                  );
                })}
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        <div className={`
          px-4 py-2 border-t text-xs flex items-center justify-between
          ${darkMode
            ? 'bg-gray-800 border-gray-700 text-gray-400'
            : 'bg-gray-50 border-gray-200 text-gray-600'
          }
        `}>
          <div className="flex items-center gap-4">
            <span className="flex items-center gap-1">
              <Command size={12} />
              <kbd>K</kbd> to open
            </span>
            <span>
              <kbd>ESC</kbd> to close
            </span>
          </div>
          <div>
            {filteredActions.length} result{filteredActions.length !== 1 ? 's' : ''}
          </div>
        </div>
      </div>
    </div>
  );

  return createPortal(modal, document.body);
};

export default CommandPalette;
