'use client';

import React, { useState } from 'react';
import { WorkflowNode, useWorkflowStore } from '@/lib/stores/workflow-store';
import { WorkflowTask } from '@/types/workflow';
import Button from '@/components/ui/Button';
import { Plus, Edit2, Trash2, X, Check } from 'lucide-react';
import { getTaskTypeColor } from '@/lib/utils';
import toast from 'react-hot-toast';

interface PropertiesPanelProps {
  selectedNode: WorkflowNode | null;
  onClose: () => void;
  onAddTask: () => void;
  onEditTask: (task: WorkflowTask) => void;
  onDeleteTask: (taskCode: string) => void;
}

export default function PropertiesPanel({
  selectedNode,
  onClose,
  onAddTask,
  onEditTask,
  onDeleteTask,
}: PropertiesPanelProps) {
  const { updateNode, nodes } = useWorkflowStore();
  const [isEditingState, setIsEditingState] = useState(false);
  const [editedStateName, setEditedStateName] = useState('');
  const [editedStateCode, setEditedStateCode] = useState('');
  const [editedStateDescription, setEditedStateDescription] = useState('');

  if (!selectedNode) {
    return (
      <div className="w-80 bg-gray-50 border-l border-gray-200 flex items-center justify-center p-6">
        <p className="text-sm text-gray-500 text-center">
          Select a state node to view and configure its tasks
        </p>
      </div>
    );
  }

  const { state } = selectedNode.data;
  const tasks = state.tasks || [];

  const handleEditState = () => {
    setEditedStateName(state.name);
    setEditedStateCode(state.code);
    setEditedStateDescription(state.description || '');
    setIsEditingState(true);
  };

  const handleSaveState = () => {
    if (!editedStateName.trim()) {
      toast.error('State name is required');
      return;
    }
    if (!editedStateCode.trim()) {
      toast.error('State code is required');
      return;
    }

    // Validate duplicate state codes (exclude current node)
    const duplicateStateCode = nodes.find(
      (node) => node.id !== selectedNode.id && node.data.state.code === editedStateCode
    );
    if (duplicateStateCode) {
      toast.error(`State code '${editedStateCode}' is already in use. Please choose a unique code.`);
      return;
    }

    // Update node ID if state code changed
    const newNodeId = `state-${editedStateCode}`;
    const needsIdUpdate = selectedNode.id !== newNodeId;

    updateNode(selectedNode.id, {
      label: editedStateName,
      state: {
        ...state,
        name: editedStateName,
        code: editedStateCode,
        description: editedStateDescription,
      },
    });

    // Note: If state code changed, edges referencing this node will need updating
    // This is handled by the workflow store's updateNode function
    if (needsIdUpdate) {
      toast.success('State updated successfully. Note: You may need to reconnect edges if state code changed.');
    } else {
      toast.success('State updated successfully');
    }

    setIsEditingState(false);
  };

  return (
    <div className="w-80 bg-white border-l border-gray-200 flex flex-col">
      {/* Header */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between mb-2">
          <h3 className="font-semibold text-gray-900">State Properties</h3>
          <div className="flex gap-1">
            {!isEditingState && (
              <button
                onClick={handleEditState}
                className="p-1 hover:bg-gray-100 rounded transition-colors"
                title="Edit State"
                aria-label="Edit State"
              >
                <Edit2 className="w-4 h-4 text-gray-600" />
              </button>
            )}
            <button
              onClick={onClose}
              className="p-1 hover:bg-gray-100 rounded transition-colors"
              title="Close"
              aria-label="Close"
            >
              <X className="w-4 h-4 text-gray-500" />
            </button>
          </div>
        </div>

        {isEditingState ? (
          <div className="space-y-3">
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">State Name</label>
              <input
                type="text"
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={editedStateName}
                onChange={(e) => setEditedStateName(e.target.value)}
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">State Code</label>
              <input
                type="text"
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono"
                value={editedStateCode}
                onChange={(e) => setEditedStateCode(e.target.value.toUpperCase())}
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-700 mb-1">Description</label>
              <textarea
                className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={2}
                value={editedStateDescription}
                onChange={(e) => setEditedStateDescription(e.target.value)}
              />
            </div>
            <div className="flex gap-2">
              <Button size="sm" variant="primary" onClick={handleSaveState} className="flex-1">
                <Check className="w-3 h-3 mr-1" />
                Save
              </Button>
              <Button size="sm" variant="outline" onClick={() => setIsEditingState(false)} className="flex-1">
                Cancel
              </Button>
            </div>
          </div>
        ) : (
          <>
            <div>
              <p className="text-sm font-medium text-gray-900">{state.name}</p>
              <p className="text-xs text-gray-500 font-mono">{state.code}</p>
            </div>
            {state.description && (
              <p className="text-xs text-gray-600 mt-2">{state.description}</p>
            )}
            <div className="flex gap-2 mt-2">
              {state.isInitial && (
                <span className="text-xs px-2 py-1 bg-green-100 text-green-800 rounded">Initial</span>
              )}
              {state.isFinal && (
                <span className="text-xs px-2 py-1 bg-purple-100 text-purple-800 rounded">Final</span>
              )}
            </div>
          </>
        )}
      </div>

      {/* Tasks Section */}
      <div className="flex-1 overflow-y-auto">
        <div className="p-4">
          <div className="flex items-center justify-between mb-3">
            <h4 className="text-sm font-semibold text-gray-900">
              Tasks ({tasks.length})
            </h4>
            <Button
              size="sm"
              variant="primary"
              onClick={onAddTask}
              className="flex items-center gap-1"
            >
              <Plus className="w-4 h-4" />
              Add
            </Button>
          </div>

          {tasks.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-sm text-gray-500">No tasks configured</p>
              <p className="text-xs text-gray-400 mt-1">Click Add to create a task</p>
            </div>
          ) : (
            <div className="space-y-2">
              {tasks.map((task) => (
                <div
                  key={task.code}
                  className="p-3 border border-gray-200 rounded-lg hover:border-blue-300 transition-colors"
                >
                  <div className="flex items-start justify-between mb-2">
                    <div className="flex-1">
                      <p className="text-sm font-medium text-gray-900">{task.name}</p>
                      <p className="text-xs text-gray-500">{task.code}</p>
                    </div>
                    <div className="flex gap-1">
                      <button
                        onClick={() => onEditTask(task)}
                        className="p-1 hover:bg-gray-100 rounded transition-colors"
                        title="Edit Task"
                        aria-label="Edit Task"
                      >
                        <Edit2 className="w-3 h-3 text-gray-600" />
                      </button>
                      <button
                        onClick={() => onDeleteTask(task.code)}
                        className="p-1 hover:bg-red-50 rounded transition-colors"
                        title="Delete Task"
                        aria-label="Delete Task"
                      >
                        <Trash2 className="w-3 h-3 text-red-600" />
                      </button>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-1 mb-2">
                    <span className={`text-xs px-2 py-0.5 rounded ${getTaskTypeColor(task.taskType)}`}>
                      {task.taskType}
                    </span>
                    {task.mandatory && (
                      <span className="text-xs px-2 py-0.5 rounded bg-red-100 text-red-800">
                        Mandatory
                      </span>
                    )}
                    {task.requiresApproval && (
                      <span className="text-xs px-2 py-0.5 rounded bg-yellow-100 text-yellow-800">
                        Approval
                      </span>
                    )}
                  </div>

                  {task.description && (
                    <p className="text-xs text-gray-600 line-clamp-2">{task.description}</p>
                  )}

                  <div className="flex gap-3 mt-2 text-xs text-gray-500">
                    {task.roleCode && (
                      <span>Role: {task.roleCode}</span>
                    )}
                    {task.slaHours && (
                      <span>SLA: {task.slaHours}h</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
