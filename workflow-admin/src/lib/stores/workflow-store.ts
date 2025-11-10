import { create } from 'zustand';
import { Node, Edge, Connection, addEdge, applyNodeChanges, applyEdgeChanges } from 'reactflow';
import { WorkflowState, WorkflowTask } from '@/types/workflow';

export interface WorkflowNode extends Node {
  data: {
    label: string;
    state: WorkflowState;
  };
}

interface WorkflowStore {
  nodes: WorkflowNode[];
  edges: Edge[];
  selectedNode: WorkflowNode | null;
  workflowCode: string;
  workflowName: string;

  // Actions
  setNodes: (nodes: WorkflowNode[]) => void;
  setEdges: (edges: Edge[]) => void;
  setSelectedNode: (node: WorkflowNode | null) => void;
  setWorkflowInfo: (code: string, name: string) => void;

  addNode: (node: WorkflowNode) => void;
  updateNode: (id: string, data: Partial<WorkflowNode['data']>) => void;
  deleteNode: (id: string) => void;

  addEdge: (connection: Connection) => void;
  deleteEdge: (id: string) => void;

  onNodesChange: (changes: any) => void;
  onEdgesChange: (changes: any) => void;
  onConnect: (connection: Connection) => void;

  addTaskToNode: (nodeId: string, task: WorkflowTask) => void;
  updateTaskInNode: (nodeId: string, taskCode: string, task: Partial<WorkflowTask>) => void;
  deleteTaskFromNode: (nodeId: string, taskCode: string) => void;

  reset: () => void;
}

export const useWorkflowStore = create<WorkflowStore>((set, get) => ({
  nodes: [],
  edges: [],
  selectedNode: null,
  workflowCode: '',
  workflowName: '',

  setNodes: (nodes) => set({ nodes }),
  setEdges: (edges) => set({ edges }),
  setSelectedNode: (node) => set({ selectedNode: node }),
  setWorkflowInfo: (code, name) => set({ workflowCode: code, workflowName: name }),

  addNode: (node) =>
    set((state) => ({
      nodes: [...state.nodes, node],
    })),

  updateNode: (id, data) =>
    set((state) => ({
      nodes: state.nodes.map((node) =>
        node.id === id
          ? {
              ...node,
              data: { ...node.data, ...data },
            }
          : node
      ),
      selectedNode:
        state.selectedNode?.id === id
          ? {
              ...state.selectedNode,
              data: { ...state.selectedNode.data, ...data },
            }
          : state.selectedNode,
    })),

  deleteNode: (id) =>
    set((state) => ({
      nodes: state.nodes.filter((node) => node.id !== id),
      edges: state.edges.filter((edge) => edge.source !== id && edge.target !== id),
      selectedNode: state.selectedNode?.id === id ? null : state.selectedNode,
    })),

  addEdge: (connection) =>
    set((state) => ({
      edges: addEdge(connection, state.edges),
    })),

  deleteEdge: (id) =>
    set((state) => ({
      edges: state.edges.filter((edge) => edge.id !== id),
    })),

  onNodesChange: (changes) =>
    set((state) => ({
      nodes: applyNodeChanges(changes, state.nodes) as WorkflowNode[],
    })),

  onEdgesChange: (changes) =>
    set((state) => ({
      edges: applyEdgeChanges(changes, state.edges),
    })),

  onConnect: (connection) =>
    set((state) => ({
      edges: addEdge(connection, state.edges),
    })),

  addTaskToNode: (nodeId, task) =>
    set((state) => ({
      nodes: state.nodes.map((node) =>
        node.id === nodeId
          ? {
              ...node,
              data: {
                ...node.data,
                state: {
                  ...node.data.state,
                  tasks: [...node.data.state.tasks, task],
                },
              },
            }
          : node
      ),
      selectedNode:
        state.selectedNode?.id === nodeId
          ? {
              ...state.selectedNode,
              data: {
                ...state.selectedNode.data,
                state: {
                  ...state.selectedNode.data.state,
                  tasks: [...state.selectedNode.data.state.tasks, task],
                },
              },
            }
          : state.selectedNode,
    })),

  updateTaskInNode: (nodeId, taskCode, taskUpdate) =>
    set((state) => ({
      nodes: state.nodes.map((node) =>
        node.id === nodeId
          ? {
              ...node,
              data: {
                ...node.data,
                state: {
                  ...node.data.state,
                  tasks: node.data.state.tasks.map((t) =>
                    t.code === taskCode ? { ...t, ...taskUpdate } : t
                  ),
                },
              },
            }
          : node
      ),
      selectedNode:
        state.selectedNode?.id === nodeId
          ? {
              ...state.selectedNode,
              data: {
                ...state.selectedNode.data,
                state: {
                  ...state.selectedNode.data.state,
                  tasks: state.selectedNode.data.state.tasks.map((t) =>
                    t.code === taskCode ? { ...t, ...taskUpdate } : t
                  ),
                },
              },
            }
          : state.selectedNode,
    })),

  deleteTaskFromNode: (nodeId, taskCode) =>
    set((state) => ({
      nodes: state.nodes.map((node) =>
        node.id === nodeId
          ? {
              ...node,
              data: {
                ...node.data,
                state: {
                  ...node.data.state,
                  tasks: node.data.state.tasks.filter((t) => t.code !== taskCode),
                },
              },
            }
          : node
      ),
      selectedNode:
        state.selectedNode?.id === nodeId
          ? {
              ...state.selectedNode,
              data: {
                ...state.selectedNode.data,
                state: {
                  ...state.selectedNode.data.state,
                  tasks: state.selectedNode.data.state.tasks.filter((t) => t.code !== taskCode),
                },
              },
            }
          : state.selectedNode,
    })),

  reset: () =>
    set({
      nodes: [],
      edges: [],
      selectedNode: null,
      workflowCode: '',
      workflowName: '',
    }),
}));
