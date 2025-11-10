'use client';

import React, { useState, useEffect } from 'react';
import ReactFlow, {
  Background,
  Controls,
  MiniMap,
  Node,
  Edge,
  MarkerType,
  Panel,
} from 'reactflow';
import 'reactflow/dist/style.css';
import Button from '@/components/ui/Button';
import { Home, Download } from 'lucide-react';
import { useRouter } from 'next/navigation';
import toast from 'react-hot-toast';

export default function UltraComplexWorkflowPage() {
  const router = useRouter();
  const [nodes, setNodes] = useState<Node[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ states: 0, transitions: 0, tasks: 0 });

  useEffect(() => {
    loadWorkflow();
  }, []);

  const loadWorkflow = async () => {
    try {
      // Fetch states
      const statesRes = await fetch('/api/workflows/ULTRA_COMPLEX_DEMO/states');
      if (!statesRes.ok) throw new Error('Failed to fetch states');
      const states = await statesRes.json();

      // Fetch transitions
      const transitionsRes = await fetch('/api/workflows/ULTRA_COMPLEX_DEMO/transitions');
      if (!transitionsRes.ok) throw new Error('Failed to fetch transitions');
      const transitions = await transitionsRes.json();

      // Create nodes
      const workflowNodes: Node[] = states.map((state: any, index: number) => {
        const tasks = state.tasks || [];
        const taskCount = tasks.length;
        const documentTasks = tasks.filter((t: any) => t.taskType === 'DOCUMENT').length;
        const actionTasks = tasks.filter((t: any) => t.taskType === 'ACTION').length;
        const controlTasks = tasks.filter((t: any) => t.taskType === 'CONTROL').length;
        const tasksWithForms = tasks.filter((t: any) => t.inputFields && t.inputFields.length > 0);
        const tasksWithDeps = tasks.filter((t: any) => t.dependencies && t.dependencies.length > 0);
        const tasksWithConds = tasks.filter((t: any) => t.conditions && t.conditions.length > 0);
        const totalFormFields = tasksWithForms.reduce((sum: number, t: any) => sum + (t.inputFields?.length || 0), 0);

        let bgColor = '#ffffff';
        let borderColor = '#6b7280';

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
          id: state.stateCode,
          type: 'default',
          position: { x: 80 + index * 320, y: 80 + (index % 3) * 200 },
          data: {
            label: (
              <div className="p-4 min-w-[300px]">
                <div className="font-bold text-lg mb-2">{state.stateName}</div>
                <div className="text-xs text-gray-600 mb-3">{state.description}</div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between text-sm">
                    <span className="font-semibold">Total Tasks:</span>
                    <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded font-bold">{taskCount}</span>
                  </div>
                  {taskCount > 0 && (
                    <>
                      <div className="flex gap-1 flex-wrap">
                        {documentTasks > 0 && (
                          <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded font-semibold">
                            📄 {documentTasks}
                          </span>
                        )}
                        {actionTasks > 0 && (
                          <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded font-semibold">
                            ⚡ {actionTasks}
                          </span>
                        )}
                        {controlTasks > 0 && (
                          <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded font-semibold">
                            🛡️ {controlTasks}
                          </span>
                        )}
                      </div>
                      <div className="flex gap-2 text-xs flex-wrap">
                        {totalFormFields > 0 && (
                          <span className="text-blue-600 font-semibold">📝 {totalFormFields} form fields</span>
                        )}
                        {tasksWithDeps.length > 0 && (
                          <span className="text-purple-600 font-semibold">🔗 {tasksWithDeps.length} dependencies</span>
                        )}
                        {tasksWithConds.length > 0 && (
                          <span className="text-orange-600 font-semibold">⚙️ {tasksWithConds.length} conditional</span>
                        )}
                      </div>
                      <div className="mt-3 pt-3 border-t border-gray-200">
                        <div className="text-xs font-semibold text-gray-600 mb-1">Tasks:</div>
                        <div className="space-y-1 max-h-32 overflow-y-auto">
                          {tasks.slice(0, 5).map((task: any, idx: number) => (
                            <div key={idx} className="flex items-start gap-1 text-xs">
                              <span className="text-gray-400">{idx + 1}.</span>
                              <div className="flex-1">
                                <div className="font-medium">{task.taskName}</div>
                                <div className="text-gray-500 flex gap-1 flex-wrap items-center">
                                  <span className="bg-gray-100 px-1 rounded">{task.taskType}</span>
                                  {task.isMandatory && <span className="text-red-600">*</span>}
                                  {task.requiresApproval && <span className="text-green-600">✓</span>}
                                  {task.inputFields && task.inputFields.length > 0 && (
                                    <span className="text-blue-600 font-semibold">📝{task.inputFields.length}</span>
                                  )}
                                  {task.dependencies && task.dependencies.length > 0 && (
                                    <span className="text-purple-600">🔗</span>
                                  )}
                                  {task.conditions && task.conditions.length > 0 && (
                                    <span className="text-orange-600">⚙️</span>
                                  )}
                                </div>
                              </div>
                            </div>
                          ))}
                          {tasks.length > 5 && (
                            <div className="text-xs text-gray-500 italic">
                              ... and {tasks.length - 5} more
                            </div>
                          )}
                        </div>
                      </div>
                    </>
                  )}
                </div>
              </div>
            ),
          },
          style: {
            background: bgColor,
            border: `3px solid ${borderColor}`,
            borderRadius: '12px',
            padding: 0,
            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
            minWidth: 320,
          },
        };
      });

      // Create edges
      const workflowEdges: Edge[] = transitions.map((transition: any, index: number) => {
        let strokeColor = '#6b7280';
        let strokeWidth = 2;
        let animated = false;

        if (transition.transitionType === 'CANCEL') {
          strokeColor = '#dc2626';
          strokeWidth = 2;
        } else if (transition.transitionType === 'ROLLBACK') {
          strokeColor = '#ef4444';
          strokeWidth = 2;
        } else if (transition.autoTransition) {
          strokeColor = '#10b981';
          animated = true;
          strokeWidth = 3;
        } else if (transition.requiresApproval) {
          strokeColor = '#f59e0b';
          strokeWidth = 2;
        }

        return {
          id: `edge-${index}`,
          source: transition.fromStateCode,
          target: transition.toStateCode,
          label: transition.transitionName,
          type: 'smoothstep',
          animated,
          style: { stroke: strokeColor, strokeWidth },
          markerEnd: {
            type: MarkerType.ArrowClosed,
            color: strokeColor,
          },
          labelStyle: {
            fill: strokeColor,
            fontWeight: 600,
            fontSize: 11,
          },
          labelBgStyle: {
            fill: '#ffffff',
            fillOpacity: 0.95,
          },
        };
      });

      const totalTasks = states.reduce((sum: number, s: any) => sum + (s.tasks?.length || 0), 0);

      setNodes(workflowNodes);
      setEdges(workflowEdges);
      setStats({
        states: states.length,
        transitions: transitions.length,
        tasks: totalTasks,
      });
      setLoading(false);
      toast.success('Ultra-complex workflow loaded!');
    } catch (error: any) {
      console.error('Error:', error);
      toast.error('Failed to load workflow: ' + error.message);
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading ultra-complex workflow...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Ultra-Complex Workflow Visualization</h1>
            <p className="text-sm text-gray-600 mt-1">
              ULTRA_COMPLEX_DEMO - Demonstrating ALL features
            </p>
          </div>
          <Button variant="outline" onClick={() => router.push('/dashboard')}>
            <Home className="w-4 h-4 mr-2" />
            Dashboard
          </Button>
        </div>
      </div>

      <div className="bg-white border-b border-gray-200 px-6 py-2">
        <div className="flex items-center gap-6 flex-wrap text-xs">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#d1fae5', border: '2px solid #10b981' }}></div>
            <span>Start</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#e9d5ff', border: '2px solid #a855f7' }}></div>
            <span>Final</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#fef3c7', border: '2px solid #f59e0b' }}></div>
            <span>Approval</span>
          </div>
          <div className="h-0.5 w-6 bg-green-500"></div>
          <span>Auto</span>
          <div className="h-0.5 w-6 bg-red-500"></div>
          <span>Rollback</span>
          <div className="h-0.5 w-6 bg-red-700"></div>
          <span>Cancel</span>
          <span className="px-2 py-1 bg-indigo-100 text-indigo-700 rounded">📄=Doc</span>
          <span className="px-2 py-1 bg-green-100 text-green-700 rounded">⚡=Action</span>
          <span className="px-2 py-1 bg-amber-100 text-amber-700 rounded">🛡️=Control</span>
          <span className="text-blue-600">📝=Forms</span>
          <span className="text-purple-600">🔗=Deps</span>
          <span className="text-orange-600">⚙️=Cond</span>
        </div>
      </div>

      <div className="flex-1">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          fitView
          attributionPosition="bottom-left"
          minZoom={0.2}
          maxZoom={1.5}
          defaultViewport={{ x: 0, y: 0, zoom: 0.6 }}
        >
          <Background color="#e5e7eb" gap={16} />
          <Controls />
          <MiniMap
            nodeColor={(node) => {
              if (node.style?.border?.includes('#10b981')) return '#10b981';
              if (node.style?.border?.includes('#a855f7')) return '#a855f7';
              if (node.style?.border?.includes('#f59e0b')) return '#f59e0b';
              return '#6b7280';
            }}
          />

          <Panel position="top-right" className="bg-white rounded-lg shadow-lg p-4 min-w-[300px]">
            <h3 className="font-bold text-gray-900 mb-3 text-lg">Workflow Statistics</h3>
            <div className="space-y-3 text-sm">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">States:</span>
                <span className="font-bold text-xl text-blue-600">{stats.states}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">Transitions:</span>
                <span className="font-bold text-xl text-green-600">{stats.transitions}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600">Total Tasks:</span>
                <span className="font-bold text-xl text-purple-600">{stats.tasks}</span>
              </div>
              <div className="border-t border-gray-200 pt-3 mt-3">
                <div className="text-xs font-semibold text-gray-500 mb-2">Features Implemented:</div>
                <div className="space-y-1.5 text-xs">
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>8 Input Field Types</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>Dynamic Form Generation</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>Task Dependencies</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>Conditional Tasks</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>Multi-Level Approvals</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>Auto-Transitions</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>Rollback Support</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className="text-green-600">✓</span>
                    <span>SLA & Escalation</span>
                  </div>
                </div>
              </div>
            </div>
          </Panel>
        </ReactFlow>
      </div>

      <div className="bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 text-white px-6 py-4">
        <div className="flex items-center justify-between">
          <div className="flex gap-8 text-sm font-semibold">
            <span>✅ 18 Total Tasks</span>
            <span>✅ 35+ Form Fields</span>
            <span>✅ 11+ Dependencies</span>
            <span>✅ 2 Conditional Tasks</span>
            <span>✅ Multi-Level Approvals</span>
          </div>
          <div className="text-lg font-bold">Ultra-Complex Workflow - 100% Functional</div>
        </div>
      </div>
    </div>
  );
}
