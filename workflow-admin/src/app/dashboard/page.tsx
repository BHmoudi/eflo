'use client';

import React, { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import {
  Workflow,
  CheckSquare,
  ListTodo,
  TrendingUp,
  Plus,
  UserPlus,
  PlayCircle,
  BarChart3,
  Filter,
  X,
  Calendar,
  Clock,
  User,
  ArrowUp,
  ArrowDown,
  DollarSign,
  Activity,
  FileText,
  Zap,
} from 'lucide-react';
import { workflowApi } from '@/lib/api/workflows';
import { approvalApi } from '@/lib/api/approvals';
import { taskApi } from '@/lib/api/tasks';
import { formatRelativeTime } from '@/lib/utils';
import Link from 'next/link';

// Types for stats and activities
interface Stat {
  title: string;
  value: number;
  change?: number;
  trend?: 'up' | 'down';
  icon: any;
  color: string;
  href: string;
}

interface QuickAction {
  title: string;
  description: string;
  icon: any;
  href: string;
  shortcut: string;
  color: string;
}

interface Activity {
  id: string;
  type: 'workflow' | 'approval' | 'task' | 'order';
  user: string;
  action: string;
  timestamp: string;
  avatar?: string;
}

type StatusFilter = 'all' | 'pending' | 'active' | 'completed';
type DateRange = 'today' | 'week' | 'month' | 'all';

export default function DashboardPage() {
  const router = useRouter();
  const { t } = useTranslation();
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');
  const [dateRange, setDateRange] = useState<DateRange>('all');
  const [showShortcuts, setShowShortcuts] = useState(false);

  // Data queries
  const { data: workflows = [], isLoading: workflowsLoading } = useQuery({
    queryKey: ['workflows'],
    queryFn: () => workflowApi.getWorkflows(),
  });

  const { data: pendingApprovals = [], isLoading: approvalsLoading } = useQuery({
    queryKey: ['pending-approvals'],
    queryFn: () => approvalApi.getPendingApprovals(),
  });

  const { data: myTasks = [], isLoading: tasksLoading } = useQuery({
    queryKey: ['my-tasks'],
    queryFn: () => taskApi.getMyTasks({ status: 'PENDING,IN_PROGRESS' }),
  });

  const { data: instances = [] } = useQuery({
    queryKey: ['workflow-instances'],
    queryFn: () => workflowApi.getInstances(),
  });

  // Quick Actions Configuration
  const quickActions: QuickAction[] = [
    {
      title: 'Create New Order',
      description: 'Start a new order workflow',
      icon: Plus,
      href: '/dashboard/orders/new',
      shortcut: 'Ctrl+N',
      color: 'bg-blue-500 hover:bg-blue-600',
    },
    {
      title: 'Add Customer',
      description: 'Register a new customer',
      icon: UserPlus,
      href: '/dashboard/customers/new',
      shortcut: 'Ctrl+U',
      color: 'bg-green-500 hover:bg-green-600',
    },
    {
      title: 'Start Workflow',
      description: 'Launch a new workflow instance',
      icon: PlayCircle,
      href: '/dashboard/workflows/start',
      shortcut: 'Ctrl+W',
      color: 'bg-purple-500 hover:bg-purple-600',
    },
    {
      title: 'View Reports',
      description: 'Access analytics and reports',
      icon: BarChart3,
      href: '/dashboard/reports',
      shortcut: 'Ctrl+R',
      color: 'bg-orange-500 hover:bg-orange-600',
    },
  ];

  // Stats with trends
  const stats: Stat[] = [
    {
      title: 'Total Orders',
      value: instances.length,
      change: 12.5,
      trend: 'up',
      icon: FileText,
      color: 'bg-blue-500',
      href: '/dashboard/workflows',
    },
    {
      title: 'Pending Approvals',
      value: pendingApprovals.length,
      change: -5.2,
      trend: 'down',
      icon: CheckSquare,
      color: 'bg-yellow-500',
      href: '/dashboard/approvals',
    },
    {
      title: 'Active Workflows',
      value: instances.filter((i) => i.status === 'ACTIVE').length,
      change: 8.1,
      trend: 'up',
      icon: Activity,
      color: 'bg-green-500',
      href: '/dashboard/workflows',
    },
    {
      title: 'Revenue This Month',
      value: 45280,
      change: 15.3,
      trend: 'up',
      icon: DollarSign,
      color: 'bg-purple-500',
      href: '/dashboard/reports',
    },
  ];

  // Mock recent activities (in a real app, this would come from an API)
  const recentActivities: Activity[] = [
    {
      id: '1',
      type: 'workflow',
      user: 'John Doe',
      action: 'started a new Purchase Order workflow',
      timestamp: new Date(Date.now() - 5 * 60000).toISOString(),
      avatar: 'JD',
    },
    {
      id: '2',
      type: 'approval',
      user: 'Jane Smith',
      action: 'approved expense request #1234',
      timestamp: new Date(Date.now() - 15 * 60000).toISOString(),
      avatar: 'JS',
    },
    {
      id: '3',
      type: 'task',
      user: 'Mike Johnson',
      action: 'completed task "Review Contract"',
      timestamp: new Date(Date.now() - 30 * 60000).toISOString(),
      avatar: 'MJ',
    },
    {
      id: '4',
      type: 'order',
      user: 'Sarah Williams',
      action: 'created new order #5678',
      timestamp: new Date(Date.now() - 45 * 60000).toISOString(),
      avatar: 'SW',
    },
    {
      id: '5',
      type: 'workflow',
      user: 'Robert Brown',
      action: 'updated workflow configuration',
      timestamp: new Date(Date.now() - 60 * 60000).toISOString(),
      avatar: 'RB',
    },
  ];

  // Keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.metaKey) {
        switch (e.key.toLowerCase()) {
          case 'n':
            e.preventDefault();
            router.push('/dashboard/orders/new');
            break;
          case 'u':
            e.preventDefault();
            router.push('/dashboard/customers/new');
            break;
          case 'w':
            e.preventDefault();
            router.push('/dashboard/workflows/start');
            break;
          case 'r':
            e.preventDefault();
            router.push('/dashboard/reports');
            break;
          case 'k':
            e.preventDefault();
            setShowShortcuts(!showShortcuts);
            break;
          case '/':
            e.preventDefault();
            // Focus search (if implemented)
            break;
        }
      } else if (e.key === '?') {
        e.preventDefault();
        setShowShortcuts(!showShortcuts);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [router, showShortcuts]);

  // Filter status chips
  const statusFilters: { label: string; value: StatusFilter }[] = [
    { label: 'All', value: 'all' },
    { label: 'Pending', value: 'pending' },
    { label: 'Active', value: 'active' },
    { label: 'Completed', value: 'completed' },
  ];

  const dateRanges: { label: string; value: DateRange }[] = [
    { label: 'Today', value: 'today' },
    { label: 'This Week', value: 'week' },
    { label: 'This Month', value: 'month' },
    { label: 'All Time', value: 'all' },
  ];

  const getActivityIcon = (type: Activity['type']) => {
    switch (type) {
      case 'workflow':
        return Workflow;
      case 'approval':
        return CheckSquare;
      case 'task':
        return ListTodo;
      case 'order':
        return FileText;
      default:
        return Activity;
    }
  };

  const getActivityColor = (type: Activity['type']) => {
    switch (type) {
      case 'workflow':
        return 'bg-blue-100 text-blue-600';
      case 'approval':
        return 'bg-yellow-100 text-yellow-600';
      case 'task':
        return 'bg-green-100 text-green-600';
      case 'order':
        return 'bg-purple-100 text-purple-600';
      default:
        return 'bg-gray-100 text-gray-600';
    }
  };

  return (
    <div className="p-6 space-y-8 dark:bg-gray-900 min-h-screen transition-colors">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900 dark:text-white">{t('dashboard.dashboard')}</h1>
          <p className="text-gray-600 dark:text-gray-400 mt-1">
            {t('dashboard.welcomeMessage')}
          </p>
        </div>
        <button
          onClick={() => setShowShortcuts(!showShortcuts)}
          className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-700 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors flex items-center gap-2"
        >
          <Zap className="w-4 h-4" />
          Shortcuts (?)
        </button>
      </div>

      {/* Quick Actions */}
      <div>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          {t('dashboard.quickActions')}
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {quickActions.map((action) => {
            const Icon = action.icon;
            return (
              <Link key={action.title} href={action.href}>
                <Card
                  hover
                  className="cursor-pointer group transition-all duration-300 hover:shadow-xl dark:bg-gray-800 dark:border-gray-700"
                >
                  <CardContent className="p-6">
                    <div className="flex flex-col items-center text-center space-y-3">
                      <div
                        className={`${action.color} p-4 rounded-xl transition-transform group-hover:scale-110 shadow-lg`}
                      >
                        <Icon className="w-8 h-8 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold text-gray-900 dark:text-white">
                          {action.title}
                        </h3>
                        <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
                          {action.description}
                        </p>
                      </div>
                      <div className="flex items-center gap-1 text-xs text-gray-400 dark:text-gray-500 bg-gray-100 dark:bg-gray-700 px-2 py-1 rounded">
                        <kbd className="font-mono">{action.shortcut}</kbd>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      </div>

      {/* Stats Cards with Trends */}
      <div>
        <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          {t('dashboard.overview')}
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {stats.map((stat) => {
            const Icon = stat.icon;
            const TrendIcon = stat.trend === 'up' ? ArrowUp : ArrowDown;
            return (
              <Link key={stat.title} href={stat.href}>
                <Card
                  hover
                  className="cursor-pointer transition-all duration-300 hover:shadow-lg dark:bg-gray-800 dark:border-gray-700"
                >
                  <CardContent className="p-6">
                    <div className="flex items-center justify-between mb-4">
                      <div className={`${stat.color} p-3 rounded-lg shadow-md`}>
                        <Icon className="w-6 h-6 text-white" />
                      </div>
                      {stat.change && (
                        <div
                          className={`flex items-center gap-1 text-sm font-medium ${
                            stat.trend === 'up'
                              ? 'text-green-600 dark:text-green-400'
                              : 'text-red-600 dark:text-red-400'
                          }`}
                        >
                          <TrendIcon className="w-4 h-4" />
                          {Math.abs(stat.change)}%
                        </div>
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-600 dark:text-gray-400">
                        {stat.title}
                      </p>
                      <p className="text-3xl font-bold text-gray-900 dark:text-white mt-2">
                        {stat.title.includes('Revenue')
                          ? `$${stat.value.toLocaleString('en-US')}`
                          : stat.value}
                      </p>
                      <p className="text-xs text-gray-500 dark:text-gray-500 mt-2">
                        vs last month
                      </p>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      </div>

      {/* Quick Filters */}
      <div className="flex flex-wrap items-center gap-4">
        <div className="flex items-center gap-2">
          <Filter className="w-5 h-5 text-gray-500 dark:text-gray-400" />
          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
            Status:
          </span>
          <div className="flex gap-2">
            {statusFilters.map((filter) => (
              <button
                key={filter.value}
                onClick={() => setStatusFilter(filter.value)}
                className={`px-3 py-1 text-sm rounded-full transition-all ${
                  statusFilter === filter.value
                    ? 'bg-blue-500 text-white shadow-md'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
                }`}
              >
                {filter.label}
              </button>
            ))}
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Calendar className="w-5 h-5 text-gray-500 dark:text-gray-400" />
          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">
            Period:
          </span>
          <div className="flex gap-2">
            {dateRanges.map((range) => (
              <button
                key={range.value}
                onClick={() => setDateRange(range.value)}
                className={`px-3 py-1 text-sm rounded-full transition-all ${
                  dateRange === range.value
                    ? 'bg-purple-500 text-white shadow-md'
                    : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
                }`}
              >
                {range.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Activity Timeline */}
        <div className="lg:col-span-1">
          <Card className="dark:bg-gray-800 dark:border-gray-700">
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Clock className="w-5 h-5" />
                Recent Activity
              </CardTitle>
            </CardHeader>
            <CardContent>
              {recentActivities.length === 0 ? (
                <div className="text-center py-8 text-gray-500 dark:text-gray-400">
                  No recent activity
                </div>
              ) : (
                <div className="space-y-4">
                  {recentActivities.map((activity, index) => {
                    const ActivityIcon = getActivityIcon(activity.type);
                    return (
                      <div
                        key={activity.id}
                        className="flex items-start gap-3 group hover:bg-gray-50 dark:hover:bg-gray-700 p-2 rounded-lg transition-colors cursor-pointer"
                      >
                        <div
                          className={`${getActivityColor(
                            activity.type
                          )} p-2 rounded-lg`}
                        >
                          <ActivityIcon className="w-4 h-4" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2">
                            <div className="w-6 h-6 rounded-full bg-gradient-to-br from-blue-400 to-purple-500 flex items-center justify-center text-white text-xs font-medium">
                              {activity.avatar}
                            </div>
                            <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                              {activity.user}
                            </p>
                          </div>
                          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                            {activity.action}
                          </p>
                          <p className="text-xs text-gray-500 dark:text-gray-500 mt-1 flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {formatRelativeTime(activity.timestamp)}
                          </p>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Recent Workflows & Approvals */}
        <div className="lg:col-span-2 space-y-6">
          {/* Recent Workflows */}
          <Card className="dark:bg-gray-800 dark:border-gray-700">
            <CardHeader>
              <CardTitle>Recent Workflows</CardTitle>
            </CardHeader>
            <CardContent>
              {workflowsLoading ? (
                <div className="flex items-center justify-center py-12">
                  <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-blue-500"></div>
                </div>
              ) : workflows.length === 0 ? (
                <div className="text-center py-12">
                  <Workflow className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                  <p className="text-gray-500 dark:text-gray-400">No workflows yet</p>
                  <Link
                    href="/dashboard/workflows/new"
                    className="text-blue-500 hover:text-blue-600 text-sm mt-2 inline-block"
                  >
                    Create your first workflow
                  </Link>
                </div>
              ) : (
                <div className="space-y-3">
                  {workflows.slice(0, 5).map((workflow) => (
                    <Link
                      key={workflow.code}
                      href={`/dashboard/workflows/${workflow.code}`}
                      className="block p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:border-blue-300 dark:hover:border-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all hover:shadow-md"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="bg-blue-100 dark:bg-blue-900/30 p-2 rounded-lg">
                            <Workflow className="w-5 h-5 text-blue-600 dark:text-blue-400" />
                          </div>
                          <div>
                            <p className="font-medium text-gray-900 dark:text-white">
                              {workflow.name}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                              {workflow.code}
                            </p>
                          </div>
                        </div>
                        <span
                          className={`text-xs px-3 py-1 rounded-full font-medium ${
                            workflow.isActive
                              ? 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400'
                              : 'bg-gray-100 dark:bg-gray-700 text-gray-800 dark:text-gray-400'
                          }`}
                        >
                          {workflow.isActive ? 'Active' : 'Inactive'}
                        </span>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* Pending Approvals */}
          <Card className="dark:bg-gray-800 dark:border-gray-700">
            <CardHeader>
              <CardTitle>Pending Approvals</CardTitle>
            </CardHeader>
            <CardContent>
              {approvalsLoading ? (
                <div className="flex items-center justify-center py-12">
                  <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-yellow-500"></div>
                </div>
              ) : pendingApprovals.length === 0 ? (
                <div className="text-center py-12">
                  <CheckSquare className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                  <p className="text-gray-500 dark:text-gray-400">
                    No pending approvals
                  </p>
                  <p className="text-sm text-gray-400 dark:text-gray-500 mt-1">
                    You're all caught up!
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {pendingApprovals.slice(0, 5).map((approval) => (
                    <Link
                      key={approval.id}
                      href={`/dashboard/approvals/${approval.id}`}
                      className="block p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:border-yellow-300 dark:hover:border-yellow-500 hover:bg-yellow-50 dark:hover:bg-yellow-900/20 transition-all hover:shadow-md"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div className="bg-yellow-100 dark:bg-yellow-900/30 p-2 rounded-lg">
                            <CheckSquare className="w-5 h-5 text-yellow-600 dark:text-yellow-400" />
                          </div>
                          <div>
                            <p className="font-medium text-gray-900 dark:text-white">
                              {approval.chainName}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                              Level {approval.currentLevel}
                            </p>
                          </div>
                        </div>
                        <span className="text-xs text-gray-500 dark:text-gray-400">
                          {formatRelativeTime(approval.createdAt)}
                        </span>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          {/* My Tasks */}
          <Card className="dark:bg-gray-800 dark:border-gray-700">
            <CardHeader>
              <CardTitle>My Tasks</CardTitle>
            </CardHeader>
            <CardContent>
              {tasksLoading ? (
                <div className="flex items-center justify-center py-12">
                  <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-green-500"></div>
                </div>
              ) : myTasks.length === 0 ? (
                <div className="text-center py-12">
                  <ListTodo className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto mb-3" />
                  <p className="text-gray-500 dark:text-gray-400">No tasks assigned</p>
                  <p className="text-sm text-gray-400 dark:text-gray-500 mt-1">
                    Enjoy your free time!
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  {myTasks.slice(0, 5).map((task) => (
                    <Link
                      key={task.id}
                      href={`/dashboard/tasks/${task.id}`}
                      className="block p-4 border border-gray-200 dark:border-gray-700 rounded-lg hover:border-blue-300 dark:hover:border-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all hover:shadow-md"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3 flex-1">
                          <div className="bg-green-100 dark:bg-green-900/30 p-2 rounded-lg">
                            <ListTodo className="w-5 h-5 text-green-600 dark:text-green-400" />
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="font-medium text-gray-900 dark:text-white truncate">
                              {task.taskName}
                            </p>
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                              Instance #{task.workflowInstanceId}
                            </p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2 ml-4">
                          <span
                            className={`text-xs px-3 py-1 rounded-full font-medium whitespace-nowrap ${
                              task.status === 'PENDING'
                                ? 'bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400'
                                : task.status === 'IN_PROGRESS'
                                ? 'bg-blue-100 dark:bg-blue-900/30 text-blue-800 dark:text-blue-400'
                                : 'bg-green-100 dark:bg-green-900/30 text-green-800 dark:text-green-400'
                            }`}
                          >
                            {task.status}
                          </span>
                          {task.dueDate && (
                            <span className="text-xs text-gray-500 dark:text-gray-400 whitespace-nowrap">
                              Due {formatRelativeTime(task.dueDate)}
                            </span>
                          )}
                        </div>
                      </div>
                    </Link>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Keyboard Shortcuts Modal */}
      {showShortcuts && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-2xl w-full max-h-[80vh] overflow-auto">
            <div className="p-6">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-gray-900 dark:text-white">
                  Keyboard Shortcuts
                </h2>
                <button
                  onClick={() => setShowShortcuts(false)}
                  className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  <X className="w-6 h-6" />
                </button>
              </div>

              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">
                    Quick Actions
                  </h3>
                  <div className="space-y-2">
                    {quickActions.map((action) => (
                      <div
                        key={action.title}
                        className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
                      >
                        <span className="text-gray-700 dark:text-gray-300">
                          {action.title}
                        </span>
                        <kbd className="px-3 py-1 bg-white dark:bg-gray-600 border border-gray-300 dark:border-gray-500 rounded text-sm font-mono text-gray-800 dark:text-gray-200">
                          {action.shortcut}
                        </kbd>
                      </div>
                    ))}
                  </div>
                </div>

                <div>
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">
                    General
                  </h3>
                  <div className="space-y-2">
                    <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <span className="text-gray-700 dark:text-gray-300">
                        Toggle shortcuts panel
                      </span>
                      <kbd className="px-3 py-1 bg-white dark:bg-gray-600 border border-gray-300 dark:border-gray-500 rounded text-sm font-mono text-gray-800 dark:text-gray-200">
                        ?
                      </kbd>
                    </div>
                    <div className="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-700 rounded-lg">
                      <span className="text-gray-700 dark:text-gray-300">
                        Focus search
                      </span>
                      <kbd className="px-3 py-1 bg-white dark:bg-gray-600 border border-gray-300 dark:border-gray-500 rounded text-sm font-mono text-gray-800 dark:text-gray-200">
                        Ctrl+/
                      </kbd>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
