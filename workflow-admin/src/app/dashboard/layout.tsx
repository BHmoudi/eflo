'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import Sidebar from '@/components/layout/Sidebar';
import Header from '@/components/layout/Header';
import { SidebarProvider, useSidebar } from '@/context/SidebarContext';
import { CommandPalette, CommandAction } from '@/components/common/CommandPalette';
import { QuickActions, QuickAction } from '@/components/common/QuickActions';
import { KeyboardShortcuts, ShortcutDefinition } from '@/components/common/KeyboardShortcuts';
import {
  LayoutDashboard,
  Workflow,
  Users,
  UserCircle,
  Shield,
  Building2,
  CheckSquare,
  ListTodo,
  BarChart3,
  Settings,
  ShoppingCart,
  FileText,
  Download,
  Upload,
  Filter,
  Plus,
  Search,
  RefreshCw,
  Moon,
  Sun,
  LogOut,
  FileDown,
  FileUp,
  Trash2,
  Edit,
  Copy,
  Eye,
} from 'lucide-react';

function DashboardLayoutContent({ children }: { children: React.ReactNode }) {
  const { isExpanded, isHovered, isMobile } = useSidebar();
  const router = useRouter();
  const pathname = usePathname();
  const [isCommandPaletteOpen, setIsCommandPaletteOpen] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(false);

  // Calculate effective expanded state (expanded or hovered on desktop)
  const effectiveExpanded = isMobile ? false : (isExpanded || isHovered);

  // Initialize dark mode
  useEffect(() => {
    const isDark = document.documentElement.classList.contains('dark');
    setIsDarkMode(isDark);

    // Listen for theme changes
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.attributeName === 'class') {
          const isDark = document.documentElement.classList.contains('dark');
          setIsDarkMode(isDark);
        }
      });
    });

    observer.observe(document.documentElement, {
      attributes: true,
      attributeFilter: ['class'],
    });

    return () => observer.disconnect();
  }, []);

  // Comprehensive Command Actions Registry
  const commandActions = useMemo<CommandAction[]>(() => {
    const actions: CommandAction[] = [
      // === NAVIGATION ACTIONS ===
      {
        id: 'nav-dashboard',
        label: 'Go to Dashboard',
        description: 'View main dashboard and overview',
        icon: <LayoutDashboard size={18} />,
        category: 'pages',
        keywords: ['dashboard', 'home', 'overview', 'main'],
        shortcut: 'g h',
        onSelect: () => router.push('/dashboard'),
      },
      {
        id: 'nav-workflows',
        label: 'Go to Workflows',
        description: 'View all workflows',
        icon: <Workflow size={18} />,
        category: 'pages',
        keywords: ['workflows', 'processes', 'flow'],
        shortcut: 'g w',
        onSelect: () => router.push('/dashboard/workflows'),
      },
      {
        id: 'nav-orders',
        label: 'Go to Orders',
        description: 'View all orders',
        icon: <ShoppingCart size={18} />,
        category: 'pages',
        keywords: ['orders', 'purchases', 'requests'],
        shortcut: 'g o',
        onSelect: () => router.push('/dashboard/orders'),
      },
      {
        id: 'nav-tasks',
        label: 'Go to My Tasks',
        description: 'View your assigned tasks',
        icon: <ListTodo size={18} />,
        category: 'pages',
        keywords: ['tasks', 'todos', 'assignments'],
        shortcut: 'g t',
        onSelect: () => router.push('/dashboard/tasks'),
      },
      {
        id: 'nav-approvals',
        label: 'Go to Approvals',
        description: 'View pending approvals',
        icon: <CheckSquare size={18} />,
        category: 'pages',
        keywords: ['approvals', 'approve', 'pending'],
        shortcut: 'g a',
        onSelect: () => router.push('/dashboard/approvals'),
      },
      {
        id: 'nav-users',
        label: 'Go to Users',
        description: 'Manage system users',
        icon: <UserCircle size={18} />,
        category: 'pages',
        keywords: ['users', 'people', 'team', 'members'],
        shortcut: 'g u',
        onSelect: () => router.push('/dashboard/users'),
      },
      {
        id: 'nav-business-units',
        label: 'Go to Business Units',
        description: 'Manage business units',
        icon: <Building2 size={18} />,
        category: 'pages',
        keywords: ['business units', 'departments', 'organizations'],
        shortcut: 'g b',
        onSelect: () => router.push('/dashboard/business-units'),
      },
      {
        id: 'nav-roles',
        label: 'Go to Roles',
        description: 'Manage user roles and permissions',
        icon: <Shield size={18} />,
        category: 'pages',
        keywords: ['roles', 'permissions', 'access'],
        shortcut: 'g r',
        onSelect: () => router.push('/dashboard/roles'),
      },
      {
        id: 'nav-keycloak',
        label: 'Go to Keycloak Sync',
        description: 'Sync with Keycloak',
        icon: <RefreshCw size={18} />,
        category: 'pages',
        keywords: ['keycloak', 'sync', 'authentication'],
        shortcut: 'g k',
        onSelect: () => router.push('/dashboard/keycloak-sync'),
      },
      {
        id: 'nav-reports',
        label: 'Go to Reports',
        description: 'View analytics and reports',
        icon: <BarChart3 size={18} />,
        category: 'pages',
        keywords: ['reports', 'analytics', 'statistics', 'charts'],
        onSelect: () => router.push('/dashboard/reports'),
      },
      {
        id: 'nav-settings',
        label: 'Go to Settings',
        description: 'Configure system settings',
        icon: <Settings size={18} />,
        category: 'pages',
        keywords: ['settings', 'preferences', 'configuration'],
        shortcut: 'Ctrl+,',
        onSelect: () => router.push('/dashboard/settings'),
      },

      // === CREATE ACTIONS ===
      {
        id: 'action-new-workflow',
        label: 'Create New Workflow',
        description: 'Create a new workflow definition',
        icon: <Plus size={18} />,
        category: 'actions',
        keywords: ['new', 'create', 'workflow', 'add'],
        shortcut: 'Ctrl+N',
        onSelect: () => router.push('/dashboard/workflows/create'),
      },
      {
        id: 'action-new-order',
        label: 'Create New Order',
        description: 'Start a new order',
        icon: <ShoppingCart size={18} />,
        category: 'actions',
        keywords: ['new', 'create', 'order', 'purchase'],
        shortcut: 'Ctrl+Shift+O',
        onSelect: () => router.push('/dashboard/orders/create'),
      },
      {
        id: 'action-add-user',
        label: 'Add New User',
        description: 'Add a new user to the system',
        icon: <Users size={18} />,
        category: 'actions',
        keywords: ['add', 'new', 'user', 'create'],
        shortcut: 'Ctrl+Shift+U',
        onSelect: () => router.push('/dashboard/users/create'),
      },
      {
        id: 'action-add-business-unit',
        label: 'Add Business Unit',
        description: 'Create a new business unit',
        icon: <Building2 size={18} />,
        category: 'actions',
        keywords: ['add', 'new', 'business unit', 'department'],
        onSelect: () => router.push('/dashboard/business-units/create'),
      },

      // === QUICK FILTERS ===
      {
        id: 'filter-active',
        label: 'Filter: Active Items',
        description: 'Show only active items',
        icon: <Filter size={18} />,
        category: 'actions',
        keywords: ['filter', 'active', 'enabled'],
        onSelect: () => {
          // This would typically update a filter state
          console.log('Filter: Active items');
        },
      },
      {
        id: 'filter-pending',
        label: 'Filter: Pending Items',
        description: 'Show pending approvals',
        icon: <Filter size={18} />,
        category: 'actions',
        keywords: ['filter', 'pending', 'waiting'],
        onSelect: () => {
          console.log('Filter: Pending items');
        },
      },
      {
        id: 'filter-my-tasks',
        label: 'Filter: My Tasks Only',
        description: 'Show only tasks assigned to you',
        icon: <Filter size={18} />,
        category: 'actions',
        keywords: ['filter', 'my', 'assigned', 'tasks'],
        onSelect: () => {
          console.log('Filter: My tasks');
        },
      },

      // === EXPORT ACTIONS ===
      {
        id: 'export-csv',
        label: 'Export to CSV',
        description: 'Export current view to CSV',
        icon: <FileDown size={18} />,
        category: 'actions',
        keywords: ['export', 'download', 'csv', 'save'],
        shortcut: 'Ctrl+E',
        onSelect: () => {
          console.log('Export to CSV');
        },
      },
      {
        id: 'export-pdf',
        label: 'Export to PDF',
        description: 'Export current view to PDF',
        icon: <FileDown size={18} />,
        category: 'actions',
        keywords: ['export', 'download', 'pdf', 'print'],
        onSelect: () => {
          console.log('Export to PDF');
        },
      },
      {
        id: 'export-excel',
        label: 'Export to Excel',
        description: 'Export current view to Excel',
        icon: <FileDown size={18} />,
        category: 'actions',
        keywords: ['export', 'download', 'excel', 'xlsx'],
        onSelect: () => {
          console.log('Export to Excel');
        },
      },

      // === BULK ACTIONS ===
      {
        id: 'bulk-delete',
        label: 'Bulk Delete Selected',
        description: 'Delete all selected items',
        icon: <Trash2 size={18} />,
        category: 'actions',
        keywords: ['bulk', 'delete', 'remove', 'selected'],
        onSelect: () => {
          console.log('Bulk delete');
        },
      },
      {
        id: 'bulk-export',
        label: 'Bulk Export Selected',
        description: 'Export selected items',
        icon: <Download size={18} />,
        category: 'actions',
        keywords: ['bulk', 'export', 'download', 'selected'],
        onSelect: () => {
          console.log('Bulk export');
        },
      },
      {
        id: 'bulk-approve',
        label: 'Bulk Approve Selected',
        description: 'Approve all selected items',
        icon: <CheckSquare size={18} />,
        category: 'actions',
        keywords: ['bulk', 'approve', 'accept', 'selected'],
        onSelect: () => {
          console.log('Bulk approve');
        },
      },

      // === SYSTEM ACTIONS ===
      {
        id: 'action-refresh',
        label: 'Refresh Data',
        description: 'Reload current page data',
        icon: <RefreshCw size={18} />,
        category: 'actions',
        keywords: ['refresh', 'reload', 'update'],
        shortcut: 'Ctrl+R',
        onSelect: () => {
          router.refresh();
        },
      },
      {
        id: 'action-search',
        label: 'Global Search',
        description: 'Search across all content',
        icon: <Search size={18} />,
        category: 'actions',
        keywords: ['search', 'find', 'lookup'],
        shortcut: 'Ctrl+/',
        onSelect: () => {
          console.log('Global search');
        },
      },
      {
        id: 'action-toggle-theme',
        label: isDarkMode ? 'Switch to Light Mode' : 'Switch to Dark Mode',
        description: 'Toggle between light and dark theme',
        icon: isDarkMode ? <Sun size={18} /> : <Moon size={18} />,
        category: 'actions',
        keywords: ['theme', 'dark', 'light', 'mode'],
        shortcut: 'Ctrl+Shift+T',
        onSelect: () => {
          document.documentElement.classList.toggle('dark');
        },
      },

      // === HELP & DOCUMENTATION ===
      {
        id: 'help-shortcuts',
        label: 'View Keyboard Shortcuts',
        description: 'See all available keyboard shortcuts',
        icon: <FileText size={18} />,
        category: 'help',
        keywords: ['help', 'keyboard', 'shortcuts', 'keys'],
        shortcut: '?',
        onSelect: () => {
          console.log('Show keyboard shortcuts');
        },
      },
      {
        id: 'help-docs',
        label: 'View Documentation',
        description: 'Open help documentation',
        icon: <FileText size={18} />,
        category: 'help',
        keywords: ['help', 'docs', 'documentation', 'guide'],
        onSelect: () => {
          window.open('/docs', '_blank');
        },
      },
      {
        id: 'help-support',
        label: 'Contact Support',
        description: 'Get help from support team',
        icon: <UserCircle size={18} />,
        category: 'help',
        keywords: ['help', 'support', 'contact', 'assistance'],
        onSelect: () => {
          window.open('mailto:support@eflo.com', '_blank');
        },
      },
    ];

    return actions;
  }, [router, pathname, isDarkMode]);

  // Quick Actions for FAB
  const quickActions = useMemo<QuickAction[]>(() => [
    {
      id: 'quick-search',
      label: 'Command Palette',
      icon: <Search size={20} />,
      shortcut: 'Ctrl+K',
      onClick: () => setIsCommandPaletteOpen(true),
    },
    {
      id: 'new-workflow',
      label: 'New Workflow',
      icon: <FileText size={20} />,
      shortcut: 'Ctrl+N',
      onClick: () => router.push('/dashboard/workflows/create'),
    },
    {
      id: 'new-order',
      label: 'New Order',
      icon: <ShoppingCart size={20} />,
      shortcut: 'Ctrl+Shift+O',
      onClick: () => router.push('/dashboard/orders/create'),
    },
    {
      id: 'refresh',
      label: 'Refresh',
      icon: <RefreshCw size={20} />,
      shortcut: 'Ctrl+R',
      onClick: () => router.refresh(),
    },
  ], [router]);

  // Keyboard Shortcuts Definitions
  const keyboardShortcuts = useMemo<ShortcutDefinition[]>(() => [
    // Navigation shortcuts
    {
      id: 'nav-dashboard',
      keys: ['g', 'h'],
      description: 'Go to Dashboard',
      category: 'Navigation',
      action: () => router.push('/dashboard'),
    },
    {
      id: 'nav-workflows',
      keys: ['g', 'w'],
      description: 'Go to Workflows',
      category: 'Navigation',
      action: () => router.push('/dashboard/workflows'),
    },
    {
      id: 'nav-orders',
      keys: ['g', 'o'],
      description: 'Go to Orders',
      category: 'Navigation',
      action: () => router.push('/dashboard/orders'),
    },
    {
      id: 'nav-tasks',
      keys: ['g', 't'],
      description: 'Go to Tasks',
      category: 'Navigation',
      action: () => router.push('/dashboard/tasks'),
    },
    {
      id: 'nav-users',
      keys: ['g', 'u'],
      description: 'Go to Users',
      category: 'Navigation',
      action: () => router.push('/dashboard/users'),
    },
    {
      id: 'nav-settings',
      keys: ['Ctrl', ','],
      description: 'Go to Settings',
      category: 'Navigation',
      action: () => router.push('/dashboard/settings'),
    },

    // Action shortcuts
    {
      id: 'cmd-palette',
      keys: ['Ctrl', 'k'],
      description: 'Open Command Palette',
      category: 'Actions',
      action: () => setIsCommandPaletteOpen(true),
    },
    {
      id: 'new-workflow',
      keys: ['Ctrl', 'n'],
      description: 'Create New Workflow',
      category: 'Actions',
      action: () => router.push('/dashboard/workflows/create'),
    },
    {
      id: 'refresh',
      keys: ['Ctrl', 'r'],
      description: 'Refresh Page',
      category: 'Actions',
      action: () => router.refresh(),
    },
    {
      id: 'export',
      keys: ['Ctrl', 'e'],
      description: 'Export Data',
      category: 'Actions',
      action: () => console.log('Export'),
    },
    {
      id: 'search',
      keys: ['Ctrl', '/'],
      description: 'Global Search',
      category: 'Actions',
      action: () => console.log('Search'),
    },
    {
      id: 'toggle-theme',
      keys: ['Ctrl', 'Shift', 't'],
      description: 'Toggle Dark Mode',
      category: 'Actions',
      action: () => document.documentElement.classList.toggle('dark'),
    },

    // Help
    {
      id: 'help',
      keys: ['?'],
      description: 'Show Keyboard Shortcuts',
      category: 'Help',
      action: () => console.log('Show help'),
    },
  ], [router]);

  return (
    <div className="flex h-screen overflow-hidden bg-gray-50 dark:bg-gray-900">
      <Sidebar />
      <div
        className={cn(
          'flex-1 flex flex-col overflow-hidden transition-all duration-300 ease-in-out',
          // Add margin only on desktop, based on sidebar state
          !isMobile && (effectiveExpanded ? 'lg:ml-0' : 'lg:ml-0')
        )}
      >
        <Header />
        <main className="flex-1 overflow-auto">
          {children}
        </main>
      </div>

      {/* Command Palette - Global */}
      <CommandPalette
        isOpen={isCommandPaletteOpen}
        actions={commandActions}
        darkMode={isDarkMode}
        maxRecent={8}
        placeholder="Type a command or search..."
        onClose={() => setIsCommandPaletteOpen(false)}
      />

      {/* Quick Actions FAB */}
      <QuickActions
        actions={quickActions}
        position="bottom-right"
        darkMode={isDarkMode}
        showShortcuts={true}
        onSearch={() => setIsCommandPaletteOpen(true)}
      />

      {/* Keyboard Shortcuts Helper */}
      <KeyboardShortcuts
        shortcuts={keyboardShortcuts}
        darkMode={isDarkMode}
        helpKey="?"
        onShortcutTriggered={(id) => console.log('Shortcut triggered:', id)}
      />
    </div>
  );
}

// Simple cn utility function (fallback if not imported from lib/utils)
function cn(...classes: (string | boolean | undefined)[]) {
  return classes.filter(Boolean).join(' ');
}

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <SidebarProvider>
      <DashboardLayoutContent>{children}</DashboardLayoutContent>
    </SidebarProvider>
  );
}
