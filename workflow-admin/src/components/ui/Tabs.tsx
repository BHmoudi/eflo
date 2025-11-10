'use client';

import React, { useState } from 'react';
import { cn } from '@/lib/utils';

export interface Tab {
  id: string;
  label: string;
  content: React.ReactNode;
  disabled?: boolean;
  badge?: React.ReactNode;
}

interface TabsProps {
  tabs: Tab[];
  defaultTab?: string;
  activeTab?: string;
  onTabChange?: (tabId: string) => void;
  className?: string;
}

const Tabs = React.forwardRef<HTMLDivElement, TabsProps>(
  ({
    tabs,
    defaultTab,
    activeTab: controlledActiveTab,
    onTabChange,
    className
  }, ref) => {
    const [internalActiveTab, setInternalActiveTab] = useState(defaultTab || tabs[0]?.id);

    // Use controlled if activeTab is provided, otherwise use internal state
    const isControlled = controlledActiveTab !== undefined;
    const activeTab = isControlled ? controlledActiveTab : internalActiveTab;

    const handleTabClick = (tabId: string) => {
      if (!isControlled) {
        setInternalActiveTab(tabId);
      }
      onTabChange?.(tabId);
    };

    const activeTabContent = tabs.find((tab) => tab.id === activeTab)?.content;

    return (
      <div ref={ref} className={cn('space-y-4', className)}>
        {/* Tab Navigation */}
        <div className="border-b border-gray-200 dark:border-gray-800">
          <nav className="-mb-px flex space-x-4 overflow-x-auto" aria-label="Tabs">
            {tabs.map((tab) => (
              <button
                key={tab.id}
                type="button"
                onClick={() => !tab.disabled && handleTabClick(tab.id)}
                disabled={tab.disabled}
                className={cn(
                  'whitespace-nowrap py-3 px-4 border-b-2 font-medium text-sm transition-colors',
                  'focus:outline-none focus:ring-4 focus:ring-brand-500/12',
                  activeTab === tab.id
                    ? 'border-brand-500 text-brand-600 dark:text-brand-400'
                    : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600',
                  tab.disabled && 'opacity-50 cursor-not-allowed hover:text-gray-500 dark:hover:text-gray-400 hover:border-transparent'
                )}
                aria-selected={activeTab === tab.id}
                aria-disabled={tab.disabled}
                role="tab"
              >
                <span className="flex items-center gap-2">
                  {tab.label}
                  {tab.badge}
                </span>
              </button>
            ))}
          </nav>
        </div>

        {/* Tab Content */}
        <div className="py-4" role="tabpanel">{activeTabContent}</div>
      </div>
    );
  }
);

Tabs.displayName = 'Tabs';

export default Tabs;
