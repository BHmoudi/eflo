'use client';

import React, { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useWorkflowStore } from '@/lib/stores/workflow-store';
import { workflowApi } from '@/lib/api/workflows';
import WorkflowCanvas from '@/components/workflow/WorkflowCanvas';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Breadcrumb from '@/components/layout/Breadcrumb';
import { ArrowLeft, Save } from 'lucide-react';
import toast from 'react-hot-toast';
import { generateCode } from '@/lib/utils';

function WorkflowBuilderContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const workflowCode = searchParams.get('code');
  const queryClient = useQueryClient();

  const [workflowName, setWorkflowName] = useState('');
  const [workflowDescription, setWorkflowDescription] = useState('');
  const [workflowOrderType, setWorkflowOrderType] = useState('VN');
  const [showNameDialog, setShowNameDialog] = useState(!workflowCode);

  const {
    nodes,
    edges,
    setNodes,
    setEdges,
    setWorkflowInfo,
    reset,
  } = useWorkflowStore();

  // Load existing workflow if editing
  const { data: existingWorkflow } = useQuery({
    queryKey: ['workflow', workflowCode],
    queryFn: () => workflowApi.getWorkflowByCode(workflowCode!),
    enabled: !!workflowCode,
  });

  // Extract states and transitions from the main workflow object (which already has auth working)
  const workflowStates = existingWorkflow?.states || [];
  const workflowTransitions = existingWorkflow?.transitions || [];

  useEffect(() => {
    if (existingWorkflow && workflowCode) {
      setWorkflowName(existingWorkflow.name || existingWorkflow.processName);
      setWorkflowDescription(existingWorkflow.description || '');
      setWorkflowInfo(existingWorkflow.code || existingWorkflow.processCode, existingWorkflow.name || existingWorkflow.processName);
    }
  }, [existingWorkflow, workflowCode, setWorkflowInfo]);

  useEffect(() => {
    if (workflowStates && Array.isArray(workflowStates) && workflowStates.length > 0) {
      const workflowNodes = workflowStates.map((state, index) => ({
        id: `state-${state.stateCode}`,
        type: 'stateNode',
        position: { x: 100 + index * 300, y: 100 + (index % 2) * 150 },
        data: {
          label: state.stateName,
          state: {
            code: state.stateCode,
            name: state.stateName,
            description: state.description,
            isFinal: state.isFinalState,
            isInitial: state.stateType === 'START',
            // Transform task fields from backend format to frontend format
            tasks: (state.tasks || []).map((task: any) => ({
              code: task.taskCode,
              name: task.taskName,
              description: task.description,
              taskType: task.taskType,
              order: task.taskOrder,
              roleCode: task.assignedToRole,
              mandatory: task.isMandatory,
              requiresApproval: task.requiresApproval,
              slaHours: task.slaHours || task.expectedDurationHours,
              requiredDocuments: task.requiredDocuments,
              inputFields: task.inputFields,
              formDefinition: task.formDefinition,
              validationRules: task.validationRules,
              configuration: task.configuration,
              approvalChainId: task.approvalChainId,
              dependencies: task.dependencies,
              conditions: task.conditions,
            })),
            order: state.stateOrder,
          },
        },
      }));
      setNodes(workflowNodes);
      toast.success(`Loaded ${workflowStates.length} states`);
    }
  }, [workflowStates, setNodes]);

  useEffect(() => {
    if (workflowTransitions && Array.isArray(workflowTransitions) && workflowTransitions.length > 0) {
      const workflowEdges = workflowTransitions.map((transition, index) => ({
        id: `edge-${transition.id || index}`,
        source: `state-${transition.fromStateCode}`,
        target: `state-${transition.toStateCode}`,
        label: transition.transitionName,
        type: 'smoothstep',
      }));
      setEdges(workflowEdges);
      toast.success(`Loaded ${workflowTransitions.length} transitions`);
    }
  }, [workflowTransitions, setEdges]);

  const createWorkflowMutation = useMutation({
    mutationFn: async (data: { code: string; name: string; description?: string; orderType?: string }) => {
      return workflowApi.createWorkflow(data);
    },
    onSuccess: (workflow) => {
      setWorkflowInfo(workflow.code || workflow.processCode, workflow.name || workflow.processName);
      toast.success('Workflow created successfully');

      // Invalidate workflows cache to refresh the list
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to create workflow');
    },
  });

  const saveStatesMutation = useMutation({
    mutationFn: async ({ code, states, existingStates }: { code: string; states: any[]; existingStates?: any[] }) => {
      const existingStateCodes = new Set(existingStates?.map(s => s.stateCode) || []);

      // Save each state
      for (const state of states) {
        // Check if state already exists - if so, update it; otherwise create it
        const stateExists = existingStateCodes.has(state.code);
        const existingState = existingStates?.find(s => s.stateCode === state.code);

        if (stateExists) {
          // Update existing state
          await workflowApi.updateState(code, state.code, {
            code: state.code,
            name: state.name,
            description: state.description,
            isFinal: state.isFinal,
            isInitial: state.isInitial,
            order: state.order,
          });
        } else {
          // Create new state
          await workflowApi.addState(code, {
            code: state.code,
            name: state.name,
            description: state.description,
            isFinal: state.isFinal,
            isInitial: state.isInitial,
            order: state.order,
          });
        }

        // Get existing tasks for this state
        const existingTasks = existingState?.tasks || [];
        const existingTaskCodes = new Set(existingTasks.map((t: any) => t.taskCode));

        // Save tasks for this state - create only new ones, update existing
        for (const task of state.tasks || []) {
          const taskExists = existingTaskCodes.has(task.code);

          if (taskExists) {
            // Update existing task
            await workflowApi.updateTask(code, state.code, task.code, {
              code: task.code,
              name: task.name,
              description: task.description,
              taskType: task.taskType,
              mandatory: task.mandatory,
              requiresApproval: task.requiresApproval,
              roleCode: task.roleCode,
              slaHours: task.slaHours,
              order: task.order,
            });
          } else {
            // Create new task
            await workflowApi.addTask(code, state.code, {
              code: task.code,
              name: task.name,
              description: task.description,
              taskType: task.taskType,
              mandatory: task.mandatory,
              requiresApproval: task.requiresApproval,
              roleCode: task.roleCode,
              slaHours: task.slaHours,
              order: task.order,
            });
          }
        }
      }
    },
  });

  const saveTransitionsMutation = useMutation({
    mutationFn: async ({ code, transitions, nodes, existingTransitions }: { code: string; transitions: any[]; nodes: any[]; existingTransitions?: any[] }) => {
      // Create a map of node IDs to state codes
      const nodeIdToStateCode = new Map(
        nodes.map(node => [node.id, node.data.state.code])
      );

      // First, delete all existing transitions to avoid duplicates
      if (existingTransitions && existingTransitions.length > 0) {
        for (const existingTransition of existingTransitions) {
          try {
            await workflowApi.deleteTransition(code, existingTransition.id);
          } catch (error) {
            console.warn('Failed to delete transition:', existingTransition.id, error);
          }
        }
      }

      // Then, create new transitions from current edges
      for (const transition of transitions) {
        // Look up the actual state codes from the node IDs
        const fromStateCode = nodeIdToStateCode.get(transition.source);
        const toStateCode = nodeIdToStateCode.get(transition.target);

        if (!fromStateCode || !toStateCode) {
          console.warn('Skipping transition with invalid state codes:', transition);
          continue;
        }

        await workflowApi.addTransition(code, {
          name: transition.label || 'Next',
          fromStateCode,
          toStateCode,
        });
      }
    },
  });

  const handleCreateWorkflow = () => {
    if (!workflowName.trim()) {
      toast.error('Please enter a workflow name');
      return;
    }

    const code = generateCode('WF', workflowName);
    createWorkflowMutation.mutate({
      code,
      name: workflowName,
      description: workflowDescription,
      orderType: workflowOrderType,
    });
    setShowNameDialog(false);
  };

  const handleSaveWorkflow = async () => {
    // If workflow hasn't been created yet, create it first
    if (!workflowCode && !createWorkflowMutation.data) {
      if (!workflowName.trim()) {
        toast.error('Please enter a workflow name first');
        setShowNameDialog(true);
        return;
      }

      // Auto-create workflow with the name
      const code = generateCode('WF', workflowName);
      try {
        const workflow = await createWorkflowMutation.mutateAsync({
          code,
          name: workflowName,
          description: workflowDescription,
        });

        // Now continue with saving states using the newly created workflow code
        const workflowCodeToUse = workflow.code || workflow.processCode;
        if (!workflowCodeToUse) {
          toast.error('Failed to get workflow code');
          return;
        }

        // Continue with save after workflow is created
        await performSave(workflowCodeToUse);
      } catch (error: any) {
        toast.error(error.message || 'Failed to create workflow');
        return;
      }
      return;
    }

    // If workflow already exists, save directly
    const code = workflowCode || createWorkflowMutation.data?.code || createWorkflowMutation.data?.processCode;
    if (!code) {
      toast.error('Workflow code not found');
      return;
    }

    await performSave(code);
  };

  const performSave = async (code: string) => {
    // Comprehensive workflow validation
    if (nodes.length === 0) {
      toast.error('Workflow must have at least one state');
      return;
    }

    // Check for exactly one initial state
    const initialStates = nodes.filter(node => node.data.state.isInitial);
    if (initialStates.length === 0) {
      toast.error('Workflow must have exactly one initial state');
      return;
    }
    if (initialStates.length > 1) {
      toast.error('Workflow cannot have more than one initial state');
      return;
    }

    // Check for at least one final state
    const finalStates = nodes.filter(node => node.data.state.isFinal);
    if (finalStates.length === 0) {
      toast.error('Workflow must have at least one final state');
      return;
    }

    // Check for orphaned states (states with no incoming or outgoing transitions, except initial state)
    const stateConnections = new Map<string, { incoming: number; outgoing: number }>();
    nodes.forEach(node => {
      stateConnections.set(node.id, { incoming: 0, outgoing: 0 });
    });

    edges.forEach(edge => {
      const source = stateConnections.get(edge.source);
      const target = stateConnections.get(edge.target);
      if (source) source.outgoing++;
      if (target) target.incoming++;
    });

    const orphanedStates = nodes.filter(node => {
      const connections = stateConnections.get(node.id);
      // Initial state can have no incoming, final state can have no outgoing
      const isInitial = node.data.state.isInitial;
      const isFinal = node.data.state.isFinal;

      if (!connections) return true;

      // A state is orphaned if:
      // - It's not initial and has no incoming transitions
      // - It's not final and has no outgoing transitions
      const hasNoIncoming = connections.incoming === 0 && !isInitial;
      const hasNoOutgoing = connections.outgoing === 0 && !isFinal;

      return hasNoIncoming || hasNoOutgoing;
    });

    if (orphanedStates.length > 0) {
      const orphanedNames = orphanedStates.map(n => n.data.state.name).join(', ');
      toast.error(`The following states are not properly connected: ${orphanedNames}. All states must have incoming and outgoing transitions (except initial and final states).`);
      return;
    }

    try {
      // Save states and tasks
      const states = nodes.map((node) => node.data.state);
      await saveStatesMutation.mutateAsync({ code, states, existingStates: workflowStates });

      // Save transitions - pass nodes to map node IDs to state codes and existingTransitions to delete old ones
      await saveTransitionsMutation.mutateAsync({ code, transitions: edges, nodes, existingTransitions: workflowTransitions });

      // Invalidate all workflow-related queries to ensure fresh data
      await queryClient.invalidateQueries({ queryKey: ['workflow', code] });
      await queryClient.invalidateQueries({ queryKey: ['workflow-states', code] });
      await queryClient.invalidateQueries({ queryKey: ['workflow-transitions', code] });
      await queryClient.invalidateQueries({ queryKey: ['workflows'] });

      toast.success('Workflow saved successfully! Redirecting to workflow list...');

      // Small delay to ensure cache invalidation completes before redirect
      setTimeout(() => {
        router.push('/dashboard/workflows');
      }, 500);
    } catch (error: any) {
      toast.error(error.message || 'Failed to save workflow');
    }
  };

  return (
    <div className="h-screen flex flex-col">
      <div className="bg-white border-b border-gray-200 p-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button
              variant="ghost"
              size="sm"
              onClick={() => router.push('/dashboard/workflows')}
            >
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back
            </Button>
            <div>
              <h1 className="text-xl font-bold text-gray-900">
                {workflowCode ? 'Edit Workflow' : 'Create Workflow'}
              </h1>
              {workflowName && (
                <p className="text-sm text-gray-500">{workflowName}</p>
              )}
            </div>
          </div>
          <Button
            variant="primary"
            onClick={handleSaveWorkflow}
            className="flex items-center gap-2"
            isLoading={saveStatesMutation.isPending || saveTransitionsMutation.isPending}
          >
            <Save className="w-4 h-4" />
            Save Workflow
          </Button>
        </div>
      </div>

      <div className="flex-1 overflow-hidden">
        <WorkflowCanvas onSave={handleSaveWorkflow} existingStates={workflowStates || []} />
      </div>

      {/* Initial Workflow Name Dialog */}
      {showNameDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">
              Create New Workflow
            </h2>
            <div className="space-y-4">
              <Input
                label="Workflow Name"
                value={workflowName}
                onChange={(e) => setWorkflowName(e.target.value)}
                placeholder="e.g., Document Approval Workflow"
                required
              />
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={3}
                  value={workflowDescription}
                  onChange={(e) => setWorkflowDescription(e.target.value)}
                  placeholder="Workflow description..."
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Order Type *
                </label>
                <select
                  value={workflowOrderType}
                  onChange={(e) => setWorkflowOrderType(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                >
                  <option value="VN">VN - Véhicule Neuf (New Vehicle)</option>
                  <option value="VO">VO - Véhicule Occasion (Used Vehicle)</option>
                  <option value="EVO">EVO - Évolution (Evolution)</option>
                </select>
                <p className="text-xs text-gray-500 mt-1">
                  This workflow will be automatically triggered when orders of this type are created
                </p>
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <Button
                variant="outline"
                onClick={() => router.push('/dashboard/workflows')}
              >
                Cancel
              </Button>
              <Button
                variant="primary"
                onClick={handleCreateWorkflow}
                isLoading={createWorkflowMutation.isPending}
              >
                Create
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function WorkflowBuilderPage() {
  return (
    <Suspense fallback={<div className="flex items-center justify-center h-screen">Loading...</div>}>
      <WorkflowBuilderContent />
    </Suspense>
  );
}
