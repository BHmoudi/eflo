'use client';

import React, { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import { useSidebar } from '@/context/SidebarContext';
import { useTranslation } from '@/hooks/useTranslation';
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
  ChevronDown,
  ChevronRight,
  Cog,
  RefreshCw,
  ShoppingCart,
  FileText,
  X,
} from 'lucide-react';

interface MenuItem {
  title: string;
  icon: any;
  href?: string;
  items?: MenuItem[];
}

const getMenuItems = (t: any): MenuItem[] => [
  {
    title: t('nav.dashboard'),
    icon: LayoutDashboard,
    href: '/dashboard',
  },
  {
    title: t('nav.workflows'),
    icon: Workflow,
    items: [
      { title: t('nav.workflows'), icon: Workflow, href: '/dashboard/workflows' },
      { title: t('nav.orders'), icon: ShoppingCart, href: '/dashboard/orders' },
      { title: 'Documents', icon: FileText, href: '/dashboard/documents' },
      { title: t('nav.tasks'), icon: ListTodo, href: '/dashboard/tasks' },
      { title: t('nav.approvals'), icon: CheckSquare, href: '/dashboard/approvals' },
    ],
  },
  {
    title: 'Configuration',
    icon: Cog,
    items: [
      { title: t('nav.users'), icon: UserCircle, href: '/dashboard/users' },
      { title: t('nav.businessUnits'), icon: Building2, href: '/dashboard/business-units' },
      { title: 'Document Types', icon: FileText, href: '/dashboard/document-types' },
      { title: 'Document Templates', icon: FileText, href: '/dashboard/templates' },
      { title: t('nav.roles'), icon: Shield, href: '/dashboard/roles' },
      { title: t('nav.keycloakSync'), icon: RefreshCw, href: '/dashboard/keycloak-sync' },
    ],
  },
  {
    title: t('nav.reports'),
    icon: BarChart3,
    href: '/dashboard/reports',
  },
];

export default function Sidebar() {
  const pathname = usePathname();
  const { t } = useTranslation();
  const { isExpanded, isMobileOpen, isHovered, isMobile, closeMobile, setHovered } = useSidebar();
  const [expandedSections, setExpandedSections] = useState<string[]>(['Workflows', 'Configuration']);
  const submenuRefs = useRef<{ [key: string]: HTMLUListElement | null }>({});

  // Calculate effective expanded state (expanded or hovered on desktop)
  const effectiveExpanded = isMobile ? isMobileOpen : (isExpanded || isHovered);

  // Memoize menuItems to prevent recreation on every render
  const menuItems = React.useMemo(() => getMenuItems(t), [t]);

  // Auto-expand sections when sidebar becomes expanded
  // DON'T include t in dependencies - it causes infinite loops
  useEffect(() => {
    if (effectiveExpanded) {
      setExpandedSections(['Workflows', 'Configuration']);
    }
  }, [effectiveExpanded]); // Removed t from dependencies

  const toggleSection = (title: string) => {
    if (effectiveExpanded) {
      setExpandedSections(prev =>
        prev.includes(title)
          ? prev.filter(s => s !== title)
          : [...prev, title]
      );
    }
  };

  return (
    <>
      {/* Mobile backdrop */}
      {isMobile && isMobileOpen && (
        <div
          className="fixed inset-0 z-99999 bg-black/50 lg:hidden"
          onClick={closeMobile}
          aria-hidden="true"
        />
      )}

      {/* Sidebar */}
      <aside
        className={cn(
          'fixed left-0 top-0 z-99999 h-screen flex flex-col',
          'bg-white dark:bg-gray-dark border-r border-gray-200 dark:border-gray-800',
          'transition-all duration-300 ease-in-out',
          // Desktop width
          'lg:sticky lg:z-999',
          effectiveExpanded ? 'lg:w-[290px]' : 'lg:w-[90px]',
          // Mobile: slide in/out from left
          isMobile
            ? isMobileOpen
              ? 'translate-x-0 w-[290px]'
              : '-translate-x-full w-[290px]'
            : 'translate-x-0'
        )}
        onMouseEnter={() => !isMobile && !isExpanded && setHovered(true)}
        onMouseLeave={() => !isMobile && setHovered(false)}
      >
        {/* Logo */}
        <div className="h-16 flex items-center justify-between px-6 border-b border-gray-200 dark:border-gray-800 shrink-0">
          <div className="flex items-center gap-2 overflow-hidden">
            <div className="w-8 h-8 bg-brand-600 dark:bg-brand-500 rounded-lg flex items-center justify-center shrink-0">
              <Workflow className="w-5 h-5 text-white" />
            </div>
            {effectiveExpanded && (
              <span className="text-xl font-bold text-gray-900 dark:text-white whitespace-nowrap">
                FLEET-AI
              </span>
            )}
          </div>

          {/* Mobile close button */}
          {isMobile && isMobileOpen && (
            <button
              onClick={closeMobile}
              className="lg:hidden p-1 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              aria-label="Close menu"
            >
              <X className="w-5 h-5 text-gray-500 dark:text-gray-400" />
            </button>
          )}
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-4 py-6 overflow-y-auto custom-scrollbar">
          <ul className="space-y-1">
            {menuItems.map((item, index) => {
              const Icon = item.icon;
              const hasItems = item.items && item.items.length > 0;
              const isExpanded = expandedSections.includes(item.title);
              const isActive = item.href && (pathname === item.href || pathname.startsWith(item.href + '/'));
              const hasActiveChild = hasItems && item.items.some(child => child.href && pathname.startsWith(child.href));

              return (
                <li key={item.title + index}>
                  {hasItems ? (
                    <>
                      <button
                        onClick={() => toggleSection(item.title)}
                        className={cn(
                          'group menu-item w-full',
                          hasActiveChild ? 'menu-item-active' : 'menu-item-inactive',
                          !effectiveExpanded && 'justify-center'
                        )}
                      >
                        <Icon
                          className={cn(
                            'w-5 h-5 shrink-0',
                            hasActiveChild ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
                          )}
                        />
                        {effectiveExpanded && (
                          <>
                            <span className="flex-1 text-left truncate">{item.title}</span>
                            <ChevronDown
                              className={cn(
                                'w-4 h-4 shrink-0 transition-transform duration-200',
                                isExpanded ? 'rotate-180' : 'rotate-0',
                                hasActiveChild ? 'menu-item-arrow-active' : 'menu-item-arrow-inactive'
                              )}
                            />
                          </>
                        )}
                      </button>

                      {/* Submenu with height animation */}
                      <div
                        className="overflow-hidden transition-all duration-300 ease-in-out"
                        style={{
                          maxHeight:
                            effectiveExpanded && isExpanded && submenuRefs.current[item.title]
                              ? `${submenuRefs.current[item.title]!.scrollHeight}px`
                              : '0px',
                        }}
                      >
                        <ul
                          ref={el => {
                            submenuRefs.current[item.title] = el;
                          }}
                          className="ml-4 mt-1 space-y-1"
                        >
                          {item.items.map((subItem, subIndex) => {
                            const SubIcon = subItem.icon;
                            const isSubActive = subItem.href && (pathname === subItem.href || pathname.startsWith(subItem.href + '/'));

                            return (
                              <li key={subItem.href || subItem.title + subIndex}>
                                <Link
                                  href={subItem.href || '#'}
                                  onClick={() => isMobile && closeMobile()}
                                  className={cn(
                                    'group menu-dropdown-item',
                                    isSubActive ? 'menu-dropdown-item-active' : 'menu-dropdown-item-inactive'
                                  )}
                                >
                                  <SubIcon
                                    className={cn(
                                      'w-4 h-4 shrink-0',
                                      isSubActive ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
                                    )}
                                  />
                                  <span className="truncate">{subItem.title}</span>
                                </Link>
                              </li>
                            );
                          })}
                        </ul>
                      </div>
                    </>
                  ) : (
                    <Link
                      href={item.href || '#'}
                      onClick={() => isMobile && closeMobile()}
                      className={cn(
                        'group menu-item',
                        isActive ? 'menu-item-active' : 'menu-item-inactive',
                        !effectiveExpanded && 'justify-center'
                      )}
                    >
                      <Icon
                        className={cn(
                          'w-5 h-5 shrink-0',
                          isActive ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
                        )}
                      />
                      {effectiveExpanded && <span className="truncate">{item.title}</span>}
                    </Link>
                  )}
                </li>
              );
            })}
          </ul>
        </nav>

        {/* Settings */}
        <div className="p-4 border-t border-gray-200 dark:border-gray-800 shrink-0">
          <Link
            href="/dashboard/settings"
            onClick={() => isMobile && closeMobile()}
            className={cn(
              'group menu-item',
              pathname === '/dashboard/settings' ? 'menu-item-active' : 'menu-item-inactive',
              !effectiveExpanded && 'justify-center'
            )}
          >
            <Settings
              className={cn(
                'w-5 h-5 shrink-0',
                pathname === '/dashboard/settings' ? 'menu-item-icon-active' : 'menu-item-icon-inactive'
              )}
            />
            {effectiveExpanded && <span className="truncate">{t('nav.settings')}</span>}
          </Link>
        </div>
      </aside>
    </>
  );
}
