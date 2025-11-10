'use client';

import React, { useCallback, useState } from 'react';
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  ConnectionMode,
  Panel,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { useQueryClient } from '@tanstack/react-query';
import { useWorkflowStore } from '@/lib/stores/workflow-store';
import StateNode from './StateNode';
import PropertiesPanel from './PropertiesPanel';
import TaskConfigPanel from './TaskConfigPanel';
import {
  ApprovalEdge,
  RollbackEdge,
  AutoTransitionEdge,
  DefaultTransitionEdge,
} from './edges';
import Button from '@/components/ui/Button';
import { Plus, Save } from 'lucide-react';
import { WorkflowTask } from '@/types/workflow';
import toast from 'react-hot-toast';
import { workflowApi } from '@/lib/api/workflows';

// Define nodeTypes and edgeTypes outside component to prevent React Flow warning
const nodeTypes = {
  stateNode: StateNode,
};

const edgeTypes = {
  approval: ApprovalEdge,
  rollback: RollbackEdge,
  autoTransition: AutoTransitionEdge,
  default: DefaultTransitionEdge,
};

interface WorkflowCanvasProps {
  onSave?: () => void;
  existingStates?: any[];
  workflowCode?: string;
}

export default function WorkflowCanvas({ onSave, existingStates = [], workflowCode }: WorkflowCanvasProps) {
  const queryClient = useQueryClient();
  const {
    nodes,
    edges,
    selectedNode,
    onNodesChange,
    onEdgesChange,
    onConnect,
    addNode,
    setSelectedNode,
    addTaskToNode,
    updateTaskInNode,
    deleteTaskFromNode,
    setEdges,
  } = useWorkflowStore();

  const [showTaskConfig, setShowTaskConfig] = useState(false);
  const [editingTask, setEditingTask] = useState<WorkflowTask | null>(null);
  const [showStateConfig, setShowStateConfig] = useState(false);
  const [newStateName, setNewStateName] = useState('');
  const [newStateCode, setNewStateCode] = useState('');
  const [newStateDescription, setNewStateDescription] = useState('');
  const [newStateIsFinal, setNewStateIsFinal] = useState(false);

  const handleNodeClick = useCallback(
    (_event: any, node: any) => {
      setSelectedNode(node);
    },
    [setSelectedNode]
  );

  const handlePaneClick = useCallback(() => {
    setSelectedNode(null);
  }, [setSelectedNode]);

  const handleAddState = () => {
    // Show state configuration modal instead of auto-creating
    setShowStateConfig(true);

    // Find next available state number by checking ALL existing states (from backend AND current nodes)
    const existingNumbers = [
      ...existingStates.map((state: any) => {
        const match = state.stateCode.match(/^STATE_(\d+)$/);
        return match ? parseInt(match[1]) : null;
      }),
      ...nodes.map((node: any) => {
        const match = node.data.state.code.match(/^STATE_(\d+)$/);
        return match ? parseInt(match[1]) : null;
      })
    ].filter(n => n !== null) as number[];

    // Find the next available number
    let nextNumber = 1;
    while (existingNumbers.includes(nextNumber)) {
      nextNumber++;
    }

    setNewStateName(`State ${nextNumber}`);
    setNewStateCode(`STATE_${nextNumber}`);
    setNewStateDescription('');
    setNewStateIsFinal(false);
  };

  const handleSaveState = () => {
    if (!newStateName.trim()) {
      toast.error('Please enter a state name');
      return;
    }

    if (!newStateCode.trim()) {
      toast.error('Please enter a state code');
      return;
    }

    // Check if state code already exists in backend data OR current UI nodes
    const codeExistsInBackend = existingStates.some((state: any) => state.stateCode === newStateCode);
    const codeExistsInNodes = nodes.some((node: any) => node.data.state.code === newStateCode);

    if (codeExistsInBackend || codeExistsInNodes) {
      toast.error(`State with code ${newStateCode} already exists in this workflow`);
      return;
    }

    const stateNumber = nodes.length + 1;
    // Use consistent node ID format: state-${stateCode}
    const newNodeId = `state-${newStateCode}`;
    const newNode = {
      id: newNodeId,
      type: 'stateNode',
      position: { x: 250 + nodes.length * 50, y: 100 + nodes.length * 50 },
      data: {
        label: newStateName,
        state: {
          code: newStateCode,
          name: newStateName,
          description: newStateDescription,
          isFinal: newStateIsFinal,
          isInitial: nodes.length === 0,
          tasks: [],
          order: stateNumber,
        },
      },
    };
    addNode(newNode);

    // Auto-create transition from last node to new node
    if (nodes.length > 0) {
      const lastNode = nodes[nodes.length - 1];
      const newEdge = {
        id: `edge-${Date.now()}`,
        source: lastNode.id,
        target: newNodeId,
        type: 'smoothstep',
        label: 'Next',
      };
      setEdges([...edges, newEdge]);
      toast.success('State and transition added successfully');
    } else {
      toast.success('State added successfully');
    }

    setShowStateConfig(false);
    setNewStateName('');
    setNewStateCode('');
    setNewStateDescription('');
    setNewStateIsFinal(false);
  };

  const handleAddTask = () => {
    if (!selectedNode) {
      toast.error('Please select a state first');
      return;
    }
    setEditingTask(null);
    setShowTaskConfig(true);
  };

  const handleEditTask = (task: WorkflowTask) => {
    setEditingTask(task);
    setShowTaskConfig(true);
  };

  const handleSaveTask = (taskData: Omit<WorkflowTask, 'id' | 'createdAt' | 'updatedAt'>) => {
    if (!selectedNode) return;

    if (editingTask) {
      // Update existing task
      updateTaskInNode(selectedNode.id, editingTask.code, taskData);
      toast.success('Task updated successfully');
    } else {
      // Add new task
      const newTask: WorkflowTask = {
        ...taskData,
        id: Date.now(),
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };
      addTaskToNode(selectedNode.id, newTask);
      toast.success('Task added successfully');
    }

    setShowTaskConfig(false);
    setEditingTask(null);
  };

  const handleDeleteTask = (taskCode: string) => {
    if (!selectedNode) return;
    if (confirm('Are you sure you want to delete this task?')) {
      deleteTaskFromNode(selectedNode.id, taskCode);
      toast.success('Task deleted successfully');
    }
  };

  const handleSaveWorkflow = async () => {
    if (nodes.length === 0) {
      toast.error('Please add at least one state');
      return;
    }

    if (!workflowCode) {
      toast.error('Workflow code is required to save');
      return;
    }

    try {
      toast.loading('Saving workflow...', { id: 'save-workflow' });

      // Convert nodes to states format for API
      const states = await Promise.all(
        nodes.map(async (node, index) => {
          const stateCode = node.data.state.code;
          const stateData = {
            code: stateCode,
            name: node.data.state.name,
            description: node.data.state.description || '',
            isFinal: node.data.state.isFinal || false,
            isInitial: node.data.state.isInitial || false,
            order: node.data.state.order || index + 1,
          };

          // Check if state already exists in backend
          const existsInBackend = existingStates.some(
            (s: any) => s.stateCode === stateCode
          );

          if (existsInBackend) {
            // Update existing state
            await workflowApi.updateState(workflowCode, stateCode, stateData);
          } else {
            // Create new state
            await workflowApi.addState(workflowCode, stateData);
          }

          // Save tasks for this state
          const existingState = existingStates.find((s: any) => s.stateCode === stateCode);
          const existingTasks = existingState?.tasks || [];

          for (const task of node.data.state.tasks) {
            const taskData = {
              code: task.code,
              name: task.name,
              description: task.description || '',
              taskType: task.taskType,
              mandatory: task.mandatory || false,
              requiresApproval: task.requiresApproval || false,
              roleCode: task.roleCode || '',
              slaHours: task.slaHours || 0,
              order: task.order || 1,
            };

            const taskExists = existingTasks.some((t: any) => t.taskCode === task.code);

            if (taskExists) {
              // Update existing task
              await workflowApi.updateTask(workflowCode, stateCode, task.code, taskData);
            } else {
              // Create new task
              await workflowApi.addTask(workflowCode, stateCode, taskData);
            }
          }

          return stateData;
        })
      );

      // Convert edges to transitions format for API
      const transitions = edges.map((edge, index) => {
        // Extract state codes from node IDs (format: state-${stateCode})
        const fromStateCode = edge.source.replace('state-', '');
        const toStateCode = edge.target.replace('state-', '');

        return {
          name: edge.label?.toString() || `Transition ${index + 1}`,
          fromStateCode,
          toStateCode,
          condition: edge.data?.condition || '',
          roleCode: edge.data?.roleCode || '',
        };
      });

      // Note: Transition update logic would require fetching existing transitions
      // and comparing them, which is more complex. For now, we'll just log them.
      // You may want to implement a bulk update endpoint for transitions.
      console.log('Transitions to save:', transitions);

      toast.success('Workflow saved successfully!', { id: 'save-workflow' });

      // Invalidate workflows cache to refresh the list
      queryClient.invalidateQueries({ queryKey: ['workflows'] });

      // Call optional onSave callback
      onSave?.();
    } catch (error: any) {
      console.error('Error saving workflow:', error);
      toast.error(`Failed to save workflow: ${error.message || 'Unknown error'}`, {
        id: 'save-workflow',
      });
    }
  };

  return (
    <div className="flex h-full w-full">
      <div className="flex-1 relative">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          onNodeClick={handleNodeClick}
          onPaneClick={handlePaneClick}
          nodeTypes={nodeTypes}
          edgeTypes={edgeTypes}
          connectionMode={ConnectionMode.Loose}
          fitView
          attributionPosition="bottom-left"
        >
          <Background />
          <Controls />
          <MiniMap
            nodeColor={(node: any) => {
              if (node.data.state.isInitial) return '#10b981';
              if (node.data.state.isFinal) return '#a855f7';
              return '#6b7280';
            }}
          />
          <Panel position="top-right" className="bg-white rounded-lg shadow-lg p-2 space-y-2">
            <Button
              size="sm"
              variant="primary"
              onClick={handleAddState}
              className="w-full flex items-center justify-center gap-2"
            >
              <Plus className="w-4 h-4" />
              Add State
            </Button>
            <Button
              size="sm"
              variant="secondary"
              onClick={handleSaveWorkflow}
              className="w-full flex items-center justify-center gap-2"
            >
              <Save className="w-4 h-4" />
              Save Workflow
            </Button>
          </Panel>
        </ReactFlow>
      </div>

      <PropertiesPanel
        selectedNode={selectedNode}
        onClose={() => setSelectedNode(null)}
        onAddTask={handleAddTask}
        onEditTask={handleEditTask}
        onDeleteTask={handleDeleteTask}
      />

      {showTaskConfig && (
        <TaskConfigPanel
          task={editingTask}
          existingTasks={selectedNode?.data?.state?.tasks || []}
          onSave={handleSaveTask}
          onCancel={() => {
            setShowTaskConfig(false);
            setEditingTask(null);
          }}
        />
      )}

      {/* State Configuration Modal */}
      {showStateConfig && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Add New State
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  State Name *
                </label>
                <input
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  value={newStateName}
                  onChange={(e) => setNewStateName(e.target.value)}
                  placeholder="e.g., Document Review"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  State Code *
                </label>
                <input
                  type="text"
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                  value={newStateCode}
                  onChange={(e) => setNewStateCode(e.target.value.toUpperCase())}
                  placeholder="e.g., DOC_REVIEW"
                  required
                />
                <p className="text-xs text-gray-500 mt-1">Unique identifier for this state</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={3}
                  value={newStateDescription}
                  onChange={(e) => setNewStateDescription(e.target.value)}
                  placeholder="State description..."
                />
              </div>
              <div className="flex items-center gap-2">
                <input
                  type="checkbox"
                  id="isFinalState"
                  checked={newStateIsFinal}
                  onChange={(e) => setNewStateIsFinal(e.target.checked)}
                  className="w-4 h-4 text-purple-600 border-gray-300 rounded focus:ring-purple-500"
                />
                <label htmlFor="isFinalState" className="text-sm text-gray-700">
                  Mark as Final State
                </label>
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <Button
                variant="outline"
                onClick={() => {
                  setShowStateConfig(false);
                  setNewStateName('');
                  setNewStateCode('');
                  setNewStateDescription('');
                }}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                onClick={handleSaveState}
              >
                Add State
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
