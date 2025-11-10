'use client';

import React, { useState, useEffect } from 'react';
import { WorkflowTask, TaskType } from '@/types/workflow';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import UserSelector from '@/components/ui/UserSelector';
import InputFieldsEditor, { InputField } from './InputFieldsEditor';
import TaskDependenciesEditor, { TaskDependency } from './TaskDependenciesEditor';
import TaskConditionsEditor, { TaskCondition } from './TaskConditionsEditor';
import ApprovalChainSelector from './ApprovalChainSelector';
import TaskDocumentSelector from './TaskDocumentSelector';
import { X, ChevronDown, ChevronUp } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { roleApi } from '@/lib/api/roles';

interface TaskConfigPanelProps {
  task?: WorkflowTask | null;
  availableTasks?: Array<{ code: string; name: string }>;
  existingTasks?: WorkflowTask[];
  onSave: (task: Omit<WorkflowTask, 'id' | 'createdAt' | 'updatedAt'>) => void;
  onCancel: () => void;
}

export default function TaskConfigPanel({ task, availableTasks = [], existingTasks = [], onSave, onCancel }: TaskConfigPanelProps) {
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    description: '',
    taskType: TaskType.ACTION,
    mandatory: false,
    requiresApproval: false,
    roleCode: '',
    slaHours: 24,
    order: 1,
  });

  const [inputFields, setInputFields] = useState<InputField[]>([]);
  const [dependencies, setDependencies] = useState<TaskDependency[]>([]);
  const [conditions, setConditions] = useState<TaskCondition[]>([]);
  const [approvalChainId, setApprovalChainId] = useState<number | undefined>();
  const [requiredDocuments, setRequiredDocuments] = useState<string[]>([]);
  const [showAdvanced, setShowAdvanced] = useState(false);
  const [assignedUserId, setAssignedUserId] = useState<number | undefined>();

  // n8n Configuration
  const [n8nEnabled, setN8nEnabled] = useState(false);
  const [n8nWebhookUrl, setN8nWebhookUrl] = useState('');
  const [n8nTriggerOn, setN8nTriggerOn] = useState('TASK_CREATED');

  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: () => roleApi.getRoles(),
  });

  useEffect(() => {
    if (task) {
      setFormData({
        code: task.code,
        name: task.name,
        description: task.description || '',
        taskType: task.taskType,
        mandatory: task.mandatory,
        requiresApproval: task.requiresApproval,
        roleCode: task.roleCode || '',
        slaHours: task.slaHours || 24,
        order: task.order,
      });

      // Load required documents
      if (task.requiredDocuments && Array.isArray(task.requiredDocuments)) {
        setRequiredDocuments(task.requiredDocuments);
      }

      // Load assigned user if exists
      const config = task.configuration as any;
      if (config?.assignedUserId) {
        setAssignedUserId(config.assignedUserId);
      }

      // Load n8n configuration if exists
      if (config?.n8nEnabled) {
        setN8nEnabled(true);
        setN8nWebhookUrl(config.n8nWebhookUrl || '');
        setN8nTriggerOn(config.n8nTriggerOn || 'TASK_CREATED');
      }
    } else {
      // When creating a new task, auto-calculate order as max(existing) + 1
      const maxOrder = existingTasks.length > 0
        ? Math.max(...existingTasks.map(t => t.order || 0))
        : 0;
      setFormData(prev => ({
        ...prev,
        order: maxOrder + 1,
      }));
    }
  }, [task, existingTasks]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const configuration: any = {};

    // Add user assignment to configuration
    if (assignedUserId) {
      configuration.assignedUserId = assignedUserId;
    }

    // Add n8n configuration
    if (n8nEnabled) {
      configuration.n8nEnabled = true;
      configuration.n8nWebhookUrl = n8nWebhookUrl;
      configuration.n8nTriggerOn = n8nTriggerOn;
      configuration.n8nAutoExecute = true;
    }

    const taskData: any = {
      ...formData,
      inputFields: inputFields.length > 0 ? inputFields : undefined,
      dependencies: dependencies.length > 0 ? dependencies : undefined,
      conditions: conditions.length > 0 ? conditions : undefined,
      approvalChainId: approvalChainId,
      requiredDocuments: requiredDocuments.length > 0 ? requiredDocuments : undefined,
      configuration: Object.keys(configuration).length > 0 ? configuration : undefined,
    };

    onSave(taskData);
  };

  const handleChange = (field: string, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden">
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">
            {task ? 'Edit Task' : 'Add Task'}
          </h2>
          <button
            onClick={onCancel}
            className="p-1 hover:bg-gray-100 rounded transition-colors"
          >
            <X className="w-5 h-5 text-gray-500" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 overflow-y-auto max-h-[calc(90vh-140px)]">
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Input
                label="Task Code"
                value={formData.code}
                onChange={(e) => handleChange('code', e.target.value)}
                placeholder="e.g., TASK_001"
                required
                disabled={!!task}
              />
              <Input
                label="Task Name"
                value={formData.name}
                onChange={(e) => handleChange('name', e.target.value)}
                placeholder="e.g., Review Document"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Description
              </label>
              <textarea
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={3}
                value={formData.description}
                onChange={(e) => handleChange('description', e.target.value)}
                placeholder="Task description..."
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Select
                label="Task Type"
                value={formData.taskType}
                onChange={(e) => handleChange('taskType', e.target.value as TaskType)}
                required
              >
                <option value={TaskType.DOCUMENT}>Document</option>
                <option value={TaskType.ACTION}>Action</option>
                <option value={TaskType.CONTROL}>Control</option>
              </Select>

              <Select
                label="Assigned Role"
                value={formData.roleCode}
                onChange={(e) => handleChange('roleCode', e.target.value)}
              >
                <option value="">None</option>
                {roles.map((role) => (
                  <option key={role.code} value={role.code}>
                    {role.name}
                  </option>
                ))}
              </Select>
            </div>

            <div>
              <UserSelector
                label="Assign to Specific User (Optional)"
                value={assignedUserId}
                onChange={(userId) => setAssignedUserId(userId as number | undefined)}
                placeholder="Select a user to assign this task to..."
                filterByRole={formData.roleCode || undefined}
                activeOnly={true}
                helperText="Optionally assign this task to a specific user. If not assigned, any user with the assigned role can complete it."
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <Input
                label="SLA Hours"
                type="number"
                value={formData.slaHours}
                onChange={(e) => handleChange('slaHours', parseInt(e.target.value))}
                min={1}
              />
              <Input
                label="Order"
                type="number"
                value={formData.order}
                onChange={(e) => handleChange('order', parseInt(e.target.value))}
                min={1}
              />
            </div>

            <div className="space-y-2">
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={formData.mandatory}
                  onChange={(e) => handleChange('mandatory', e.target.checked)}
                  className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">Mandatory Task</span>
              </label>

              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={formData.requiresApproval}
                  onChange={(e) => handleChange('requiresApproval', e.target.checked)}
                  className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <span className="text-sm text-gray-700">Requires Approval</span>
              </label>
            </div>

            {/* Advanced Configuration Toggle */}
            <div className="col-span-2 border-t border-gray-200 pt-4">
              <button
                type="button"
                onClick={() => setShowAdvanced(!showAdvanced)}
                className="flex items-center gap-2 text-sm font-medium text-gray-700 hover:text-gray-900"
              >
                {showAdvanced ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
                Advanced Configuration
              </button>
            </div>

            {/* Advanced Configuration Section */}
            {showAdvanced && (
              <div className="col-span-2 space-y-6 border border-gray-200 rounded-lg p-4 bg-gray-50">
                {/* Input Fields Configuration */}
                {(formData.taskType === TaskType.ACTION || formData.taskType === TaskType.DOCUMENT) && (
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-3">
                      Dynamic Form Fields
                    </h4>
                    <InputFieldsEditor fields={inputFields} onChange={setInputFields} />
                  </div>
                )}

                {/* Required Documents */}
                {formData.taskType === TaskType.DOCUMENT && (
                  <div>
                    <TaskDocumentSelector
                      selectedDocuments={requiredDocuments}
                      onChange={setRequiredDocuments}
                    />
                  </div>
                )}

                {/* Approval Chain */}
                {formData.requiresApproval && (
                  <div>
                    <ApprovalChainSelector
                      value={approvalChainId}
                      onChange={setApprovalChainId}
                      required={formData.requiresApproval}
                    />
                  </div>
                )}

                {/* Task Dependencies */}
                <div>
                  <h4 className="text-sm font-semibold text-gray-700 mb-3">
                    Task Dependencies
                  </h4>
                  <TaskDependenciesEditor
                    dependencies={dependencies}
                    availableTasks={availableTasks}
                    onChange={setDependencies}
                  />
                </div>

                {/* Task Conditions */}
                <div>
                  <h4 className="text-sm font-semibold text-gray-700 mb-3">
                    Conditional Activation
                  </h4>
                  <TaskConditionsEditor conditions={conditions} onChange={setConditions} />
                </div>

                {/* n8n Automation Configuration */}
                <div className="border-t border-gray-300 pt-4">
                  <h4 className="text-sm font-semibold text-gray-700 mb-3">
                    n8n Automation Integration
                  </h4>

                  <div className="space-y-4">
                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={n8nEnabled}
                        onChange={(e) => setN8nEnabled(e.target.checked)}
                        className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                      />
                      <span className="text-sm font-medium text-gray-700">Enable n8n Automation</span>
                    </label>

                    {n8nEnabled && (
                      <>
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            n8n Workflow Type
                          </label>
                          <select
                            value={n8nWebhookUrl}
                            onChange={(e) => setN8nWebhookUrl(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                            required={n8nEnabled}
                          >
                            <option value="">Select automation type...</option>
                            <option value="/webhook/document-analysis">AI Document Analysis</option>
                            <option value="/webhook/whatsapp-send">WhatsApp Notification</option>
                            <option value="/webhook/task-validation">Task Data Validation</option>
                            <option value="/webhook/file-reception">File Processing</option>
                            <option value="/webhook/multi-channel-notification">Multi-Channel Notification</option>
                            <option value="/webhook/custom">Custom Workflow</option>
                          </select>
                          <p className="text-xs text-gray-500 mt-1">
                            Select the n8n workflow to trigger for this task
                          </p>
                        </div>

                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">
                            Trigger Timing
                          </label>
                          <select
                            value={n8nTriggerOn}
                            onChange={(e) => setN8nTriggerOn(e.target.value)}
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                          >
                            <option value="TASK_CREATED">When Task is Created</option>
                            <option value="TASK_ASSIGNED">When Task is Assigned</option>
                            <option value="TASK_STARTED">When Task is Started</option>
                            <option value="TASK_COMPLETED">When Task is Completed</option>
                            <option value="STATE_ENTERED">When State is Entered</option>
                          </select>
                          <p className="text-xs text-gray-500 mt-1">
                            When should the n8n workflow be triggered?
                          </p>
                        </div>

                        <div className="bg-blue-50 border border-blue-200 rounded-md p-3">
                          <p className="text-xs text-blue-800 font-medium mb-1">n8n Integration Enabled</p>
                          <p className="text-xs text-blue-600">
                            This task will automatically trigger the selected n8n workflow {n8nTriggerOn.toLowerCase().replace('_', ' ')}.
                            The workflow can perform AI analysis, send notifications, validate data, or process files.
                          </p>
                        </div>
                      </>
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="flex justify-end gap-3 mt-6">
            <Button type="button" variant="outline" onClick={onCancel}>
              Cancel
            </Button>
            <Button type="submit" variant="primary">
              {task ? 'Update Task' : 'Add Task'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
