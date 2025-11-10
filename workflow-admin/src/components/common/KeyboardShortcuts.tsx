import React, { useState, useEffect, useMemo } from 'react';
import { createPortal } from 'react-dom';
import { X, Command, Option, Shift, Delete, ArrowUp, ArrowDown, ArrowLeft, ArrowRight } from 'lucide-react';

export interface ShortcutDefinition {
  id: string;
  keys: string[];
  description: string;
  category: string;
  action: () => void;
  disabled?: boolean;
}

export interface KeyboardShortcutsProps {
  shortcuts?: ShortcutDefinition[];
  darkMode?: boolean;
  helpKey?: string;
  onShortcutTriggered?: (id: string) => void;
}

export const KeyboardShortcuts: React.FC<KeyboardShortcutsProps> = ({
  shortcuts,
  darkMode = false,
  helpKey = '?',
  onShortcutTriggered,
}) => {
  const [isHelpOpen, setIsHelpOpen] = useState(false);
  const [platform, setPlatform] = useState<'mac' | 'windows'>('mac');

  // Detect platform
  useEffect(() => {
    const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0;
    setPlatform(isMac ? 'mac' : 'windows');
  }, []);

  // Default shortcuts
  const defaultShortcuts: ShortcutDefinition[] = [
    // Navigation
    {
      id: 'nav-home',
      keys: ['g', 'h'],
      description: 'Go to home',
      category: 'Navigation',
      action: () => console.log('Navigate home'),
    },
    {
      id: 'nav-workflows',
      keys: ['g', 'w'],
      description: 'Go to workflows',
      category: 'Navigation',
      action: () => console.log('Navigate workflows'),
    },
    {
      id: 'nav-users',
      keys: ['g', 'u'],
      description: 'Go to users',
      category: 'Navigation',
      action: () => console.log('Navigate users'),
    },
    // Actions
    {
      id: 'action-search',
      keys: ['Ctrl', 'k'],
      description: 'Open command palette',
      category: 'Actions',
      action: () => console.log('Open search'),
    },
    {
      id: 'action-new',
      keys: ['Ctrl', 'n'],
      description: 'Create new workflow',
      category: 'Actions',
      action: () => console.log('Create new'),
    },
    {
      id: 'action-save',
      keys: ['Ctrl', 's'],
      description: 'Save changes',
      category: 'Actions',
      action: () => console.log('Save'),
    },
    {
      id: 'action-delete',
      keys: ['Ctrl', 'Delete'],
      description: 'Delete selected',
      category: 'Actions',
      action: () => console.log('Delete'),
    },
    // Selection
    {
      id: 'select-all',
      keys: ['Ctrl', 'a'],
      description: 'Select all',
      category: 'Selection',
      action: () => console.log('Select all'),
    },
    {
      id: 'select-none',
      keys: ['Escape'],
      description: 'Clear selection',
      category: 'Selection',
      action: () => console.log('Clear selection'),
    },
    // Editor
    {
      id: 'editor-undo',
      keys: ['Ctrl', 'z'],
      description: 'Undo',
      category: 'Editor',
      action: () => console.log('Undo'),
    },
    {
      id: 'editor-redo',
      keys: ['Ctrl', 'Shift', 'z'],
      description: 'Redo',
      category: 'Editor',
      action: () => console.log('Redo'),
    },
    {
      id: 'editor-copy',
      keys: ['Ctrl', 'c'],
      description: 'Copy',
      category: 'Editor',
      action: () => console.log('Copy'),
    },
    {
      id: 'editor-paste',
      keys: ['Ctrl', 'v'],
      description: 'Paste',
      category: 'Editor',
      action: () => console.log('Paste'),
    },
    // Help
    {
      id: 'help',
      keys: ['?'],
      description: 'Show keyboard shortcuts',
      category: 'Help',
      action: () => setIsHelpOpen(true),
    },
  ];

  const allShortcuts = shortcuts || defaultShortcuts;

  // Group shortcuts by category
  const groupedShortcuts = useMemo(() => {
    const groups: Record<string, ShortcutDefinition[]> = {};

    allShortcuts.forEach(shortcut => {
      if (!groups[shortcut.category]) {
        groups[shortcut.category] = [];
      }
      groups[shortcut.category].push(shortcut);
    });

    return groups;
  }, [allShortcuts]);

  // Format key for display
  const formatKey = (key: string): string => {
    const lowerKey = key.toLowerCase();

    if (platform === 'mac') {
      if (lowerKey === 'ctrl') return '⌘';
      if (lowerKey === 'alt') return '⌥';
      if (lowerKey === 'shift') return '⇧';
      if (lowerKey === 'delete') return '⌫';
      if (lowerKey === 'enter') return '↵';
      if (lowerKey === 'escape') return 'ESC';
    } else {
      if (lowerKey === 'ctrl') return 'Ctrl';
      if (lowerKey === 'alt') return 'Alt';
      if (lowerKey === 'shift') return 'Shift';
      if (lowerKey === 'delete') return 'Del';
      if (lowerKey === 'enter') return 'Enter';
      if (lowerKey === 'escape') return 'Esc';
    }

    return key.toUpperCase();
  };

  // Get icon for special keys
  const getKeyIcon = (key: string) => {
    const lowerKey = key.toLowerCase();

    switch (lowerKey) {
      case 'arrowup': return <ArrowUp size={14} />;
      case 'arrowdown': return <ArrowDown size={14} />;
      case 'arrowleft': return <ArrowLeft size={14} />;
      case 'arrowright': return <ArrowRight size={14} />;
      case 'delete': return <Delete size={14} />;
      default: return null;
    }
  };

  // Handle keyboard events
  useEffect(() => {
    const pressedKeys = new Set<string>();
    let sequenceKeys: string[] = [];
    let sequenceTimeout: NodeJS.Timeout;

    const handleKeyDown = (e: KeyboardEvent) => {
      const key = e.key.toLowerCase();

      // Open help with ? key
      if (key === helpKey && !isHelpOpen) {
        e.preventDefault();
        setIsHelpOpen(true);
        return;
      }

      // Close help with Escape
      if (key === 'escape' && isHelpOpen) {
        e.preventDefault();
        setIsHelpOpen(false);
        return;
      }

      // Track pressed keys
      if (e.ctrlKey || e.metaKey) pressedKeys.add('ctrl');
      if (e.altKey) pressedKeys.add('alt');
      if (e.shiftKey) pressedKeys.add('shift');
      pressedKeys.add(key);

      // Check for matching shortcuts
      allShortcuts.forEach(shortcut => {
        if (shortcut.disabled) return;

        const shortcutKeys = shortcut.keys.map(k => k.toLowerCase());

        // Check if it's a sequence shortcut (like 'g h')
        if (shortcutKeys.length > 1 && !shortcutKeys.includes('ctrl') && !shortcutKeys.includes('shift') && !shortcutKeys.includes('alt')) {
          sequenceKeys.push(key);

          // Clear sequence after timeout
          clearTimeout(sequenceTimeout);
          sequenceTimeout = setTimeout(() => {
            sequenceKeys = [];
          }, 1000);

          // Check if sequence matches
          if (sequenceKeys.length === shortcutKeys.length) {
            const matches = shortcutKeys.every((k, i) => k === sequenceKeys[i]);
            if (matches) {
              e.preventDefault();
              shortcut.action();
              onShortcutTriggered?.(shortcut.id);
              sequenceKeys = [];
            }
          }
        } else {
          // Check if all keys match
          const matches = shortcutKeys.every(k => pressedKeys.has(k));
          if (matches && pressedKeys.size === shortcutKeys.length) {
            e.preventDefault();
            shortcut.action();
            onShortcutTriggered?.(shortcut.id);
          }
        }
      });
    };

    const handleKeyUp = () => {
      pressedKeys.clear();
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
      clearTimeout(sequenceTimeout);
    };
  }, [allShortcuts, isHelpOpen, helpKey, onShortcutTriggered]);

  // Help modal
  const helpModal = isHelpOpen && (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center p-4"
      onClick={() => setIsHelpOpen(false)}
    >
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black bg-opacity-50 backdrop-blur-sm" />

      {/* Modal */}
      <div
        className={`
          relative w-full max-w-4xl max-h-[80vh] rounded-xl shadow-2xl overflow-hidden
          ${darkMode
            ? 'bg-gray-900 border border-gray-700'
            : 'bg-white border border-gray-200'
          }
        `}
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div className={`
          px-6 py-4 border-b flex items-center justify-between
          ${darkMode ? 'border-gray-700' : 'border-gray-200'}
        `}>
          <div>
            <h2 className={`text-xl font-bold ${darkMode ? 'text-white' : 'text-gray-900'}`}>
              Keyboard Shortcuts
            </h2>
            <p className={`text-sm mt-1 ${darkMode ? 'text-gray-400' : 'text-gray-600'}`}>
              {platform === 'mac' ? 'macOS' : 'Windows'} • Press {helpKey} to toggle
            </p>
          </div>
          <button
            onClick={() => setIsHelpOpen(false)}
            className={`
              p-2 rounded-lg transition-colors
              ${darkMode
                ? 'hover:bg-gray-800 text-gray-400 hover:text-white'
                : 'hover:bg-gray-100 text-gray-600 hover:text-gray-900'
              }
            `}
            aria-label="Close"
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div className="overflow-y-auto max-h-[calc(80vh-120px)] px-6 py-4">
          {Object.entries(groupedShortcuts).map(([category, shortcuts]) => (
            <div key={category} className="mb-6 last:mb-0">
              {/* Category Title */}
              <h3 className={`
                text-sm font-semibold uppercase tracking-wider mb-3
                ${darkMode ? 'text-gray-400' : 'text-gray-600'}
              `}>
                {category}
              </h3>

              {/* Shortcuts */}
              <div className="space-y-2">
                {shortcuts.map(shortcut => (
                  <div
                    key={shortcut.id}
                    className={`
                      flex items-center justify-between p-3 rounded-lg
                      ${darkMode ? 'bg-gray-800' : 'bg-gray-50'}
                      ${shortcut.disabled ? 'opacity-50' : ''}
                    `}
                  >
                    {/* Description */}
                    <span className={`
                      text-sm
                      ${darkMode ? 'text-gray-300' : 'text-gray-700'}
                    `}>
                      {shortcut.description}
                    </span>

                    {/* Keys */}
                    <div className="flex items-center gap-1">
                      {shortcut.keys.map((key, index) => (
                        <React.Fragment key={index}>
                          <kbd className={`
                            px-2.5 py-1.5 rounded font-mono text-sm min-w-[32px] text-center
                            inline-flex items-center justify-center
                            ${darkMode
                              ? 'bg-gray-700 text-gray-200 border border-gray-600'
                              : 'bg-white text-gray-800 border border-gray-300 shadow-sm'
                            }
                          `}>
                            {getKeyIcon(key) || formatKey(key)}
                          </kbd>
                          {index < shortcut.keys.length - 1 && (
                            <span className={`mx-1 ${darkMode ? 'text-gray-600' : 'text-gray-400'}`}>
                              {shortcut.keys.length === 2 && !['ctrl', 'shift', 'alt'].includes(shortcut.keys[0].toLowerCase()) ? 'then' : '+'}
                            </span>
                          )}
                        </React.Fragment>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>

        {/* Footer */}
        <div className={`
          px-6 py-3 border-t text-xs
          ${darkMode
            ? 'bg-gray-800 border-gray-700 text-gray-400'
            : 'bg-gray-50 border-gray-200 text-gray-600'
          }
        `}>
          <div className="flex items-center justify-between">
            <span>
              Platform: {platform === 'mac' ? 'macOS' : 'Windows'}
            </span>
            <span>
              Press <kbd className="px-1.5 py-0.5 rounded bg-gray-700 text-gray-300 font-mono">ESC</kbd> to close
            </span>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <>
      {typeof window !== 'undefined' && createPortal(helpModal, document.body)}
    </>
  );
};

export default KeyboardShortcuts;
