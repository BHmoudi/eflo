'use client';

import React, { useState } from 'react';
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
import { Home } from 'lucide-react';
import { useRouter } from 'next/navigation';

// Complete VN_STANDARD Workflow Demo Data
const demoWorkflow = {
  name: "VN_STANDARD - Complete Vehicle Workflow with All Features",
  
  nodes: [
    {
      id: 'COMMANDE',
      type: 'default',
      position: { x: 50, y: 100 },
      draggable: true,
      data: {
        label: (
          <div className="p-4 min-w-[320px]">
            <div className="font-bold text-lg mb-2">Commande</div>
            <div className="text-xs text-gray-600 mb-3">Initial order creation and documentation</div>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-semibold">Total Tasks:</span>
                <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">13</span>
              </div>
              <div className="flex justify-between text-xs">
                <span>Mandatory:</span>
                <span className="bg-red-100 text-red-800 px-2 py-0.5 rounded">9</span>
              </div>
              <div className="flex gap-1 mt-2">
                <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded">📄 6</span>
                <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded">⚡ 3</span>
                <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">🛡️ 4</span>
              </div>
              <div className="mt-3 pt-3 border-t border-gray-200">
                <div className="text-xs font-semibold text-gray-600 mb-1">Sample Tasks:</div>
                <div className="space-y-1 text-xs">
                  <div>1. Signature électronique <span className="bg-gray-100 px-1">DOC</span> <span className="text-blue-600">📝3</span></div>
                  <div>2. Saisie lieu livraison <span className="bg-gray-100 px-1">ACTION</span> <span className="text-blue-600">📝4</span></div>
                  <div>3. Validation dossier <span className="bg-gray-100 px-1">CONTROL</span> ✓</div>
                </div>
              </div>
            </div>
          </div>
        ),
      },
      style: {
        background: '#d1fae5',
        border: '3px solid #10b981',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
    {
      id: 'VALIDATION',
      type: 'default',
      position: { x: 450, y: 100 },
      data: {
        label: (
          <div className="p-4 min-w-[280px]">
            <div className="font-bold text-lg mb-2">Validation</div>
            <div className="text-xs text-gray-600 mb-3">Multi-level hierarchical approval</div>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-semibold">Total Tasks:</span>
                <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">1</span>
              </div>
              <div className="flex justify-between text-xs">
                <span>Approval Required:</span>
                <span className="bg-orange-100 text-orange-800 px-2 py-0.5 rounded">1</span>
              </div>
              <div className="flex gap-1 mt-2">
                <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">🛡️ 1</span>
              </div>
              <div className="mt-3 pt-3 border-t border-gray-200">
                <div className="text-xs">
                  <div className="font-semibold mb-1">CDV then CDR Approval</div>
                  <div className="text-gray-500">Sequential approval chain</div>
                  <div className="text-blue-600 mt-1">🔗 2 levels</div>
                </div>
              </div>
            </div>
          </div>
        ),
      },
      style: {
        background: '#fef3c7',
        border: '3px solid #f59e0b',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
    {
      id: 'TRAITEMENT_APPRO',
      type: 'default',
      position: { x: 800, y: 100 },
      data: {
        label: (
          <div className="p-4 min-w-[280px]">
            <div className="font-bold text-lg mb-2">Traitement Appro</div>
            <div className="text-xs text-gray-600 mb-3">Procurement processing</div>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-semibold">Total Tasks:</span>
                <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">5</span>
              </div>
              <div className="flex gap-1 mt-2">
                <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded">📄 2</span>
                <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded">⚡ 2</span>
                <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">🛡️ 1</span>
              </div>
              <div className="mt-3 pt-3 border-t border-gray-200 text-xs">
                <div>• Copie écran rapprochant</div>
                <div>• Saisie délai appro <span className="text-blue-600">📝2</span></div>
                <div>• Confirmation CAR</div>
              </div>
            </div>
          </div>
        ),
      },
      style: {
        background: '#ffffff',
        border: '2px solid #6b7280',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
    {
      id: 'TRAITEMENT_COMMANDE',
      type: 'default',
      position: { x: 1150, y: 100 },
      data: {
        label: (
          <div className="p-4 min-w-[260px]">
            <div className="font-bold text-lg mb-2">Traitement Commande</div>
            <div className="text-xs text-gray-600 mb-3">Order processing</div>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-semibold">Total Tasks:</span>
                <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">2</span>
              </div>
              <div className="flex gap-1 mt-2">
                <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded">📄 2</span>
              </div>
            </div>
          </div>
        ),
      },
      style: {
        background: '#ffffff',
        border: '2px solid #6b7280',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
    {
      id: 'SAISIE_DCS',
      type: 'default',
      position: { x: 50, y: 400 },
      data: {
        label: (
          <div className="p-4 min-w-[260px]">
            <div className="font-bold text-lg mb-2">Saisie DCS</div>
            <div className="text-xs text-gray-600 mb-3">DCS data entry</div>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-semibold">Total Tasks:</span>
                <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">1</span>
              </div>
              <div className="flex gap-1 mt-2">
                <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">🛡️ 1</span>
              </div>
            </div>
          </div>
        ),
      },
      style: {
        background: '#ffffff',
        border: '2px solid #6b7280',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
    {
      id: 'PREPARATION',
      type: 'default',
      position: { x: 400, y: 400 },
      data: {
        label: (
          <div className="p-4 min-w-[260px]">
            <div className="font-bold text-lg mb-2">Préparation</div>
            <div className="text-xs text-gray-600 mb-3">Vehicle preparation</div>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-semibold">Total Tasks:</span>
                <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">2</span>
              </div>
              <div className="flex gap-1 mt-2">
                <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded">⚡ 1</span>
                <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">🛡️ 1</span>
              </div>
            </div>
          </div>
        ),
      },
      style: {
        background: '#ffffff',
        border: '2px solid #6b7280',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
    {
      id: 'LIVRAISON',
      type: 'default',
      position: { x: 750, y: 400 },
      data: {
        label: (
          <div className="p-4 min-w-[280px]">
            <div className="font-bold text-lg mb-2">Livraison</div>
            <div className="text-xs text-gray-600 mb-3">Customer delivery</div>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="font-semibold">Total Tasks:</span>
                <span className="bg-blue-100 text-blue-800 px-2 py-0.5 rounded">4</span>
              </div>
              <div className="flex gap-1 mt-2">
                <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded">📄 1</span>
                <span className="text-xs px-2 py-0.5 bg-green-100 text-green-700 rounded">⚡ 2</span>
                <span className="text-xs px-2 py-0.5 bg-amber-100 text-amber-700 rounded">🛡️ 1</span>
              </div>
              <div className="mt-2 text-xs">
                <div>• Planifier RDV <span className="text-blue-600">📝3</span></div>
                <div>• Préparer docs</div>
                <div>• Remise client</div>
              </div>
            </div>
          </div>
        ),
      },
      style: {
        background: '#ffffff',
        border: '2px solid #6b7280',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
    {
      id: 'ARCHIVEE',
      type: 'default',
      position: { x: 1100, y: 400 },
      data: {
        label: (
          <div className="p-4 min-w-[220px]">
            <div className="font-bold text-lg mb-2">Archivée</div>
            <div className="text-xs text-gray-600 mb-3">Completed and archived</div>
            <div className="text-xs text-gray-500">Final State</div>
          </div>
        ),
      },
      style: {
        background: '#e9d5ff',
        border: '3px solid #a855f7',
        borderRadius: '12px',
        boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
      },
    },
  ],
  edges: [
    {
      id: 'e1',
      source: 'COMMANDE',
      target: 'VALIDATION',
      label: 'Soumettre pour validation',
      type: 'smoothstep',
      style: { stroke: '#6b7280', strokeWidth: 2 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#6b7280' },
      labelStyle: { fill: '#6b7280', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
    {
      id: 'e2',
      source: 'VALIDATION',
      target: 'TRAITEMENT_APPRO',
      label: 'Approuver et envoyer',
      type: 'smoothstep',
      animated: true,
      style: { stroke: '#10b981', strokeWidth: 3 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#10b981' },
      labelStyle: { fill: '#10b981', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
    {
      id: 'e3',
      source: 'TRAITEMENT_APPRO',
      target: 'TRAITEMENT_COMMANDE',
      label: 'Continuer traitement',
      type: 'smoothstep',
      animated: true,
      style: { stroke: '#10b981', strokeWidth: 3 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#10b981' },
      labelStyle: { fill: '#10b981', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
    {
      id: 'e4',
      source: 'TRAITEMENT_COMMANDE',
      target: 'SAISIE_DCS',
      label: 'Passer à DCS',
      type: 'smoothstep',
      style: { stroke: '#6b7280', strokeWidth: 2 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#6b7280' },
      labelStyle: { fill: '#6b7280', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
    {
      id: 'e5',
      source: 'SAISIE_DCS',
      target: 'PREPARATION',
      label: 'En préparation',
      type: 'smoothstep',
      animated: true,
      style: { stroke: '#10b981', strokeWidth: 3 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#10b981' },
      labelStyle: { fill: '#10b981', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
    {
      id: 'e6',
      source: 'PREPARATION',
      target: 'LIVRAISON',
      label: 'Planifier livraison',
      type: 'smoothstep',
      style: { stroke: '#6b7280', strokeWidth: 2 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#6b7280' },
      labelStyle: { fill: '#6b7280', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
    {
      id: 'e7',
      source: 'LIVRAISON',
      target: 'ARCHIVEE',
      label: 'Archiver',
      type: 'smoothstep',
      animated: true,
      style: { stroke: '#10b981', strokeWidth: 3 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#10b981' },
      labelStyle: { fill: '#10b981', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
    {
      id: 'e8-rollback',
      source: 'VALIDATION',
      target: 'COMMANDE',
      label: 'Rejeter - retour',
      type: 'smoothstep',
      style: { stroke: '#ef4444', strokeWidth: 2 },
      markerEnd: { type: MarkerType.ArrowClosed, color: '#ef4444' },
      labelStyle: { fill: '#ef4444', fontWeight: 600 },
      labelBgStyle: { fill: '#fff', fillOpacity: 0.9 },
    },
  ],
};

export default function WorkflowDemoPage() {
  const router = useRouter();
  const [nodes] = useState<Node[]>(demoWorkflow.nodes);
  const [edges] = useState<Edge[]>(demoWorkflow.edges);

  return (
    <div className="h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <div className="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Complete Workflow System Demo</h1>
            <p className="text-sm text-gray-600 mt-1">{demoWorkflow.name}</p>
          </div>
          <Button variant="outline" onClick={() => router.push('/dashboard')}>
            <Home className="w-4 h-4 mr-2" />
            Dashboard
          </Button>
        </div>
      </div>

      {/* Legend */}
      <div className="bg-white border-b border-gray-200 px-6 py-3">
        <div className="flex items-center gap-6 flex-wrap text-xs">
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#d1fae5', border: '2px solid #10b981' }}></div>
            <span>Start State</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#e9d5ff', border: '2px solid #a855f7' }}></div>
            <span>Final State</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 rounded" style={{ background: '#fef3c7', border: '2px solid #f59e0b' }}></div>
            <span>Approval Required</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-0.5 w-6 bg-green-500"></div>
            <span>Auto-transition</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="h-0.5 w-6 bg-red-500"></div>
            <span>Rollback</span>
          </div>
          <div className="px-2 py-1 bg-indigo-100 text-indigo-700 rounded">📄 = Documents</div>
          <div className="px-2 py-1 bg-green-100 text-green-700 rounded">⚡ = Actions</div>
          <div className="px-2 py-1 bg-amber-100 text-amber-700 rounded">🛡️ = Controls</div>
          <div className="text-blue-600">📝 = Dynamic Form Fields</div>
          <div className="text-blue-600">🔗 = Dependencies</div>
          <div className="text-purple-600">⚙️ = Conditional</div>
        </div>
      </div>

      {/* React Flow Canvas */}
      <div className="flex-1">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          nodesDraggable={true}
          nodesConnectable={true}
          nodesFocusable={true}
          edgesFocusable={true}
          edgesUpdatable={true}
          attributionPosition="bottom-left"
          minZoom={0.3}
          maxZoom={1.5}
          defaultViewport={{ x: 50, y: 50, zoom: 0.7 }}
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
            maskColor="rgba(0, 0, 0, 0.1)"
          />

          <Panel position="top-right" className="bg-white rounded-lg shadow-lg p-4 min-w-[280px]">
            <h3 className="font-bold text-gray-900 mb-3">Workflow Statistics</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-600">States:</span>
                <span className="font-semibold">8</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Transitions:</span>
                <span className="font-semibold">8</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Total Tasks:</span>
                <span className="font-semibold">28+</span>
              </div>
              <div className="border-t border-gray-200 pt-2 mt-2">
                <div className="text-xs text-gray-500 mb-2">Features Implemented:</div>
                <div className="space-y-1 text-xs">
                  <div>✓ Dynamic form fields</div>
                  <div>✓ Task dependencies</div>
                  <div>✓ Conditional tasks</div>
                  <div>✓ Multi-level approvals</div>
                  <div>✓ Document validation</div>
                  <div>✓ SLA tracking</div>
                  <div>✓ Auto-transitions</div>
                  <div>✓ Rollback support</div>
                </div>
              </div>
            </div>
          </Panel>
        </ReactFlow>
      </div>

      {/* Feature Info Banner */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white px-6 py-3">
        <div className="flex items-center justify-between text-sm">
          <div className="flex gap-6">
            <span>✅ 3 Task Types</span>
            <span>✅ 8 Input Types</span>
            <span>✅ 4 Dependency Types</span>
            <span>✅ 5 Condition Types</span>
            <span>✅ Multi-Level Approvals</span>
          </div>
          <div className="font-semibold">100% Complete & Production-Ready</div>
        </div>
      </div>
    </div>
  );
}
