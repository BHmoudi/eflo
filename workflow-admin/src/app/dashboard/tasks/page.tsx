'use client';

import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from '@/hooks/useTranslation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/Table';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { taskApi } from '@/lib/api/tasks';
import { Play, CheckCircle, Search, Clock, ListTodo } from 'lucide-react';
import { formatDateTime, getStatusColor, getTaskTypeColor } from '@/lib/utils';
import toast from 'react-hot-toast';
import { TaskStatus } from '@/types/workflow';

export default function TasksPage() {
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');

  const { data: allTasks = [], isLoading: allTasksLoading } = useQuery({
    queryKey: ['tasks', statusFilter],
    queryFn: () => taskApi.getTasks(statusFilter ? { status: statusFilter } : undefined),
  });

  const { data: myTasks = [] } = useQuery({
    queryKey: ['my-tasks'],
    queryFn: () => taskApi.getMyTasks(),
  });

  const startTaskMutation = useMutation({
    mutationFn: (taskId: number) => taskApi.startTask(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      queryClient.invalidateQueries({ queryKey: ['my-tasks'] });
      toast.success('Task started successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to start task');
    },
  });

  const completeTaskMutation = useMutation({
    mutationFn: (taskId: number) => taskApi.completeTask(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      queryClient.invalidateQueries({ queryKey: ['my-tasks'] });
      toast.success('Task completed successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to complete task');
    },
  });

  const claimTaskMutation = useMutation({
    mutationFn: (taskId: number) => taskApi.claimTask(taskId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tasks'] });
      queryClient.invalidateQueries({ queryKey: ['my-tasks'] });
      toast.success('Task claimed successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to claim task');
    },
  });

  const filteredTasks = allTasks.filter(
    (task) =>
      (task.taskName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        task.taskCode?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        task.id.toString().includes(searchTerm))
  );

  const handleStartTask = (taskId: number) => {
    if (confirm('Start this task?')) {
      startTaskMutation.mutate(taskId);
    }
  };

  const handleCompleteTask = (taskId: number) => {
    if (confirm('Mark this task as completed?')) {
      completeTaskMutation.mutate(taskId);
    }
  };

  const handleClaimTask = (taskId: number) => {
    if (confirm('Claim this task?')) {
      claimTaskMutation.mutate(taskId);
    }
  };

  const pendingCount = myTasks.filter(t => t.status === TaskStatus.PENDING).length;
  const inProgressCount = myTasks.filter(t => t.status === TaskStatus.IN_PROGRESS).length;
  const completedCount = myTasks.filter(t => t.status === TaskStatus.COMPLETED).length;

  return (
    <div className="p-6 space-y-6">
      <Breadcrumb items={[{ label: t('nav.dashboard'), href: '/dashboard' }, { label: t('nav.tasks') }]} />

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{t('tasks.tasks')}</h1>
          <p className="text-gray-600 mt-1">{t('tasks.myTasks')}</p>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">My Tasks</p>
                <p className="text-3xl font-bold text-gray-900 mt-2">{myTasks.length}</p>
              </div>
              <ListTodo className="w-8 h-8 text-gray-600" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Pending</p>
                <p className="text-3xl font-bold text-yellow-600 mt-2">{pendingCount}</p>
              </div>
              <Clock className="w-8 h-8 text-yellow-600" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">In Progress</p>
                <p className="text-3xl font-bold text-blue-600 mt-2">{inProgressCount}</p>
              </div>
              <Play className="w-8 h-8 text-blue-600" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-600">Completed</p>
                <p className="text-3xl font-bold text-green-600 mt-2">{completedCount}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-600" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* My Tasks */}
      {myTasks.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>My Active Tasks ({myTasks.filter(t => t.status !== TaskStatus.COMPLETED).length})</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {myTasks
                .filter(t => t.status !== TaskStatus.COMPLETED)
                .map((task) => (
                  <div
                    key={task.id}
                    className="p-4 border border-blue-200 bg-blue-50 rounded-lg"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-medium text-gray-900">{task.taskName}</p>
                        <p className="text-sm text-gray-600 mt-1">
                          Instance #{task.workflowInstanceId} • {task.taskCode}
                        </p>
                        <div className="flex gap-2 mt-2">
                          <span className={`text-xs px-2 py-1 rounded ${getStatusColor(task.status)}`}>
                            {task.status}
                          </span>
                          <span className={`text-xs px-2 py-1 rounded ${getTaskTypeColor(task.taskType)}`}>
                            {task.taskType}
                          </span>
                        </div>
                        {task.dueDate && (
                          <p className="text-xs text-gray-500 mt-2">
                            Due: {formatDateTime(task.dueDate)}
                          </p>
                        )}
                      </div>
                      <div className="flex flex-col gap-2">
                        {task.status === TaskStatus.PENDING && (
                          <Button
                            size="sm"
                            variant="primary"
                            onClick={() => handleStartTask(task.id)}
                            className="flex items-center gap-1"
                          >
                            <Play className="w-3 h-3" />
                            Start
                          </Button>
                        )}
                        {task.status === TaskStatus.IN_PROGRESS && (
                          <Button
                            size="sm"
                            variant="secondary"
                            onClick={() => handleCompleteTask(task.id)}
                            className="flex items-center gap-1 bg-green-600 text-white hover:bg-green-700"
                          >
                            <CheckCircle className="w-3 h-3" />
                            Complete
                          </Button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* All Tasks */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>All Tasks ({filteredTasks.length})</CardTitle>
            <div className="flex gap-2">
              <Select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="w-40"
              >
                <option value="">All Status</option>
                <option value={TaskStatus.PENDING}>Pending</option>
                <option value={TaskStatus.IN_PROGRESS}>In Progress</option>
                <option value={TaskStatus.COMPLETED}>Completed</option>
                <option value={TaskStatus.REJECTED}>Rejected</option>
                <option value={TaskStatus.ON_HOLD}>On Hold</option>
              </Select>
              <div className="relative w-64">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <Input
                  placeholder="Search tasks..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {allTasksLoading ? (
            <div className="text-center py-12 text-gray-500">Loading tasks...</div>
          ) : filteredTasks.length === 0 ? (
            <div className="text-center py-12 text-gray-500">No tasks found</div>
          ) : (
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>ID</TableHead>
                    <TableHead>Task</TableHead>
                    <TableHead>Instance</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Assigned To</TableHead>
                    <TableHead>Due Date</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredTasks.map((task) => (
                    <TableRow key={task.id}>
                      <TableCell className="font-mono text-xs">{task.id}</TableCell>
                      <TableCell>
                        <div>
                          <p className="font-medium text-gray-900">{task.taskName}</p>
                          <p className="text-xs text-gray-500">{task.taskCode}</p>
                        </div>
                      </TableCell>
                      <TableCell>#{task.workflowInstanceId}</TableCell>
                      <TableCell>
                        <span className={`text-xs px-2 py-1 rounded ${getTaskTypeColor(task.taskType)}`}>
                          {task.taskType}
                        </span>
                      </TableCell>
                      <TableCell>
                        <span className={`text-xs px-2 py-1 rounded ${getStatusColor(task.status)}`}>
                          {task.status}
                        </span>
                      </TableCell>
                      <TableCell className="text-sm">
                        {task.assignedToUserName || task.assignedToRoleCode || 'Unassigned'}
                      </TableCell>
                      <TableCell className="text-sm text-gray-500">
                        {task.dueDate ? formatDateTime(task.dueDate) : 'No deadline'}
                      </TableCell>
                      <TableCell>
                        <div className="flex items-center gap-1">
                          {!task.assignedToUserId && task.status === TaskStatus.PENDING && (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleClaimTask(task.id)}
                            >
                              Claim
                            </Button>
                          )}
                          {task.status === TaskStatus.PENDING && task.assignedToUserId && (
                            <Button
                              size="sm"
                              variant="primary"
                              onClick={() => handleStartTask(task.id)}
                            >
                              Start
                            </Button>
                          )}
                          {task.status === TaskStatus.IN_PROGRESS && (
                            <Button
                              size="sm"
                              variant="secondary"
                              onClick={() => handleCompleteTask(task.id)}
                            >
                              Complete
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
