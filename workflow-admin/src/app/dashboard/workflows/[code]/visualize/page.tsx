'use client';

import React, { useEffect, useState, use } from 'react';
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  Node,
  Edge,
  MarkerType,
  ConnectionMode,
  Panel,
} from 'reactflow';
import 'reactflow/dist/style.css';
import { useRouter } from 'next/navigation';
import Button from '@/components/ui/Button';
import { ArrowLeft, Download, Maximize2 } from 'lucide-react';
import toast from 'react-hot-toast';
import {
  ApprovalEdge,
  RollbackEdge,
  AutoTransitionEdge,
  DefaultTransitionEdge,
} from '@/components/workflow/edges';

interface Task {
  taskCode: string;
  taskName: string;
  taskType: string;
  isMandatory: boolean;
  requiresApproval: boolean;
  slaHours?: number;
  inputFields?: any[];
  dependencies?: any[];
  conditions?: any[];
}

interface State {
  stateCode: string;
  stateName: string;
  description?: string;
  stateType: string;
  isFinalState: boolean;
  tasks: Task[];
}

interface Transition {
  fromStateCode: string;
  toStateCode: string;
  transitionName: string;
  transitionType: string;
  requiresApproval: boolean;
  autoTransition: boolean;
}

// Define edgeTypes outside component to prevent React Flow warning
const edgeTypes = {
  approval: ApprovalEdge,
  rollback: RollbackEdge,
  autoTransition: AutoTransitionEdge,
  default: DefaultTransitionEdge,
};

interface WorkflowVisualizationPageProps {
  params: { code: string };
}

export default function WorkflowVisualizationPage({ params }: WorkflowVisualizationPageProps) {
  const router = useRouter();
  const [nodes, setNodes] = useState<Node[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);
  const [workflowName, setWorkflowName] = useState('');
  const [loading, setLoading] = useState(true);
  const [selectedNode, setSelectedNode] = useState<Node | null>(null);

  useEffect(() => {
    loadWorkflowData();
  }, [params.code]);

  const loadWorkflowData = async () => {
    try {
      setLoading(true);

      // Fetch states
      const statesRes = await fetch(`/api/workflows/${params.code}/states`);
      if (!statesRes.ok) throw new Error('Failed to fetch states');
      const states: State[] = await statesRes.json();

      // Fetch transitions
      const transitionsRes = await fetch(`/api/workflows/${params.code}/transitions`);
      if (!transitionsRes.ok) throw new Error('Failed to fetch transitions');
      const transitions: Transition[] = await transitionsRes.json();

      // Create nodes from states
      const workflowNodes: Node[] = states.map((state, index) => {
        const taskCount = state.tasks?.length || 0;
        const mandatoryTasks = state.tasks?.filter((t) => t.isMandatory).length || 0;
        const approvalTasks = state.tasks?.filter((t) => t.requiresApproval).length || 0;
        const documentTasks = state.tasks?.filter((t) => t.taskType === 'DOCUMENT').length || 0;
        const actionTasks = state.tasks?.filter((t) => t.taskType === 'ACTION').length || 0;
        const controlTasks = state.tasks?.filter((t) => t.taskType === 'CONTROL').length || 0;

        // Calculate node color based on state type
        let bgColor = '#ffffff';
        let borderColor = '#6b7280';
        let textColor = '#1f2937';

        if (state.stateType === 'START') {
          bgColor = '#d1fae5';
          borderColor = '#10b981';
        } else if (state.isFinalState) {
          bgColor = '#e9d5ff';
          borderColor = '#a855f7';
        } else if (state.requiresApproval) {
          bgColor = '#fef3c7';
          borderColor = '#f59e0b';
        }

        return {
          id: `state-${state.stateCode}`,
          type: 'default',
          position: { x: 100 + index * 350, y: 100 + (index % 2) * 150 },
          data: {
            label: (
              <div className="p-4 min-w-[280px]">
                <div className="font-bold text-lg mb-2">{state.stateName}</div>
                {state.description && (
                  <div className="text-xs text-gray-600 mb-3">{state.description}</div>
                )}
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-semibold">Total Tasks:</span>
                    <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">{taskCount}</span>
                  </div>
                  {mandatoryTasks > 0 && (
                    <div className="flex items-center justify-between text-xs">
                      <span>Mandatory:</span>
                      <span className="bg-red-100 text-red-800 px-2 py-0.5 rounded">{mandatoryTasks}</span>
                    </div>
                  )}
                  {approvalTasks > 0 && (
                    <div className="flex items-center justify-between text-xs">
                      <span>Requires Approval:</span>
                      <span className="bg-orange-100 text-orange-800 px-2 py-0.5 rounded">{approvalTasks}</span>
                    </div>
                  )}
                  <div className="flex gap-1 mt-2 flex-wrap">
                    {documentTasks > 0 && (
                      <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded">
                        📄 {documentTasks}
                      </span>
                    )}
                    {actionTasks > 0 && (
                      <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded">
                        ⚡ {actionTasks}
                      </span>
                    )}
                    {controlTasks > 0 && (
                      <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">
                        🛡️ {controlTasks}
                      </span>
                    )}
                  </div>
                  {state.tasks && state.tasks.length > 0 && (
                    <div className="mt-3 pt-3 border-t border-gray-200">
                      <div className="text-xs font-semibold text-gray-600 mb-1">Tasks:</div>
                      <div className="space-y-1 max-h-32 overflow-y-auto">
                        {state.tasks.map((task, idx) => (
                          <div key={idx} className="flex items-start gap-1 text-xs">
                            <span className="text-gray-400">{idx + 1}.</span>
                            <div className="flex-1">
                              <div className="font-medium">{task.taskName}</div>
                              <div className="text-gray-500 flex gap-1 flex-wrap">
                                <span className="bg-gray-100 px-1 rounded">{task.taskType}</span>
                                {task.isMandatory && <span className="text-red-600">*</span>}
                                {task.requiresApproval && <span>✓</span>}
                                {task.dependencies && task.dependencies.length > 0 && (
                                  <span className="text-blue-600">🔗{task.dependencies.length}</span>
                                )}
                                {task.conditions && task.conditions.length > 0 && (
                                  <span className="text-purple-600">⚙️{task.conditions.length}</span>
                                )}
                                {task.inputFields && task.inputFields.length > 0 && (
                                  <span className="text-green-600">📝{task.inputFields.length}</span>
                                )}
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ),
          },
          style: {
            background: bgColor,
            border: `2px solid ${borderColor}`,
            borderRadius: '12px',
            padding: 0,
            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
            minWidth: 300,
          },
        };
      });

      // Create edges from transitions with custom edge types
      const workflowEdges: Edge[] = transitions.map((transition, index) => {
        let edgeType = 'default';
        let strokeColor = '#6b7280';
        let animated = false;

        // Determine edge type based on transition properties
        if (transition.transitionType === 'ROLLBACK') {
          edgeType = 'rollback';
          strokeColor = '#ef4444';
        } else if (transition.autoTransition) {
          edgeType = 'autoTransition';
          strokeColor = '#10b981';
          animated = true;
        } else if (transition.requiresApproval) {
          edgeType = 'approval';
          strokeColor = '#f59e0b';
        }

        return {
          id: `edge-${index}`,
          source: `state-${transition.fromStateCode}`,
          target: `state-${transition.toStateCode}`,
          label: transition.transitionName,
          type: edgeType,
          animated,
          markerEnd: {
            type: MarkerType.ArrowClosed,
            color: strokeColor,
            width: 20,
            height: 20,
          },
        };
      });

      setNodes(workflowNodes);
      setEdges(workflowEdges);
      setWorkflowName(`${params.code} - ${states.length} States, ${transitions.length} Transitions`);
      setLoading(false);
      toast.success('Workflow loaded successfully!');
    } catch (error: any) {
      console.error('Error loading workflow:', error);
      toast.error('Failed to load workflow: ' + error.message);
      setLoading(false);
    }
  };

  const handleNodeClick = (_event: any, node: Node) => {
    setSelectedNode(node);
  };

  const handlePaneClick = () => {
    setSelectedNode(null);
  };

  const handleExport = () => {
    const workflowData = {
      code: params.code,
      nodes,
      edges,
      exportedAt: new Date().toISOString(),
    };
    const blob = new Blob([JSON.stringify(workflowData, null, 2)], {
      type: 'application/json',
    });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `workflow-${params.code}-${Date.now()}.json`;
    a.click();
    toast.success('Workflow exported!');
  };

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading workflow visualization...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
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
              <h1 className="text-2xl font-bold text-gray-900">Workflow Visualization</h1>
              <p className="text-sm text-gray-600 mt-1">{workflowName}</p>
            </div>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={handleExport}>
              <Download className="w-4 h-4 mr-2" />
              Export JSON
            </Button>
          </div>
        </div>
      </div>

      {/* Legend */}
      <div className="bg-white border-b border-gray-200 px-6 py-3">
        <div className="flex items-center gap-6 text-xs">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#d1fae5', border: '2px solid #10b981' }}></div>
            <span className="text-gray-700">Start State</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#e9d5ff', border: '2px solid #a855f7' }}></div>
            <span className="text-gray-700">Final State</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#fef3c7', border: '2px solid #f59e0b' }}></div>
            <span className="text-gray-700">Approval Required</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-0.5 w-6 bg-green-500"></div>
            <span className="text-gray-700">Auto-transition</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-0.5 w-6 bg-red-500"></div>
            <span className="text-gray-700">Rollback</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-blue-600">🔗</span>
            <span className="text-gray-700">Has Dependencies</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-purple-600">⚙️</span>
            <span className="text-gray-700">Conditional</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-green-600">📝</span>
            <span className="text-gray-700">Dynamic Form</span>
          </div>
        </div>
      </div>

      {/* React Flow Canvas */}
      <div className="flex-1 relative">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodeClick={handleNodeClick}
          onPaneClick={handlePaneClick}
          edgeTypes={edgeTypes}
          connectionMode={ConnectionMode.Loose}
          fitView
          attributionPosition="bottom-left"
          minZoom={0.1}
          maxZoom={2}
          defaultViewport={{ x: 0, y: 0, zoom: 0.8 }}
        >
          <Background color="#e5e7eb" gap={16} />
          <Controls />
          <MiniMap
            nodeColor={(node: Node) => {
              const data = node.data as any;
              if (data.label?.props?.children?.[0]?.props?.children === 'Start') return '#10b981';
              if (node.style?.border?.includes('#a855f7')) return '#a855f7';
              if (node.style?.border?.includes('#f59e0b')) return '#f59e0b';
              return '#6b7280';
            }}
            maskColor="rgba(0, 0, 0, 0.1)"
          />

          {/* Stats Panel */}
          <Panel position="top-right" className="bg-white rounded-lg shadow-lg p-4 min-w-[250px]">
            <h3 className="font-bold text-gray-900 mb-3">Workflow Statistics</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-600">States:</span>
                <span className="font-semibold">{nodes.length}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Transitions:</span>
                <span className="font-semibold">{edges.length}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Total Tasks:</span>
                <span className="font-semibold">
                  {nodes.reduce((sum, n) => {
                    const state = n.data as any;
                    return sum + (state.tasks?.length || 0);
                  }, 0)}
                </span>
              </div>
              <div className="border-t border-gray-200 pt-2 mt-2">
                <div className="text-xs text-gray-500 mb-1">Task Types:</div>
                <div className="flex gap-2">
                  <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded">
                    📄 Documents
                  </span>
                  <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded">
                    ⚡ Actions
                  </span>
                  <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">
                    🛡️ Controls
                  </span>
                </div>
              </div>
            </div>
          </Panel>
        </ReactFlow>
      </div>

      {/* Selected Node Details Panel */}
      {selectedNode && (
        <div className="absolute right-4 bottom-4 w-96 bg-white rounded-lg shadow-2xl border border-gray-200 max-h-[60vh] overflow-hidden z-10">
          <div className="bg-gray-50 px-4 py-3 border-b border-gray-200">
            <h3 className="font-bold text-gray-900">State Details</h3>
          </div>
          <div className="p-4 overflow-y-auto max-h-[calc(60vh-60px)]">
            <div className="space-y-3">
              <div>
                <div className="text-xs font-semibold text-gray-500 uppercase mb-1">State Code</div>
                <div className="font-mono text-sm bg-gray-100 px-2 py-1 rounded">{selectedNode.id}</div>
              </div>
              {/* Additional details can be added here */}
            </div>
          </div>
          <div className="bg-gray-50 px-4 py-3 border-t border-gray-200">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setSelectedNode(null)}
              className="w-full"
            >
              Close
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
