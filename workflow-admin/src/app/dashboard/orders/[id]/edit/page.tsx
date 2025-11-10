'use client';

import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter, useParams } from 'next/navigation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import CollapsibleSection from '@/components/ui/CollapsibleSection';
import OrderCriteriaSidebar from '@/components/orders/OrderCriteriaSidebar';
import EnhancedDocumentsTab from '@/components/orders/EnhancedDocumentsTab';
import { orderApi, Order } from '@/lib/api/orders';
import { workflowApi } from '@/lib/api/workflows';
import { instanceApi, WorkflowInstance } from '@/lib/api/instances';
import { taskApi, CompleteTaskRequest } from '@/lib/api/tasks';
import { WorkflowProcess, WorkflowState, WorkflowTask, TaskInstance } from '@/types/workflow';
import toast from 'react-hot-toast';
import {
  Save,
  Printer,
  FileText,
  Calendar,
  Upload,
  Check,
  CheckCircle2,
  ChevronRight,
  ChevronLeft,
  Users,
  CreditCard,
  Clipboard,
  FileCheck,
  Truck,
  History,
  Plus,
  Download,
  X
} from 'lucide-react';
import { cn } from '@/lib/utils';

// Workflow Stepper Component (same as before)
interface WorkflowStepperProps {
  states: WorkflowState[];
  currentStateCode: string;
}

const WorkflowStepperHeader: React.FC<WorkflowStepperProps> = ({ states, currentStateCode }) => {
  const sortedStates = [...states].sort((a, b) => a.order - b.order);
  const currentIndex = sortedStates.findIndex(s => s.code === currentStateCode);

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 mb-4">
      <div className="flex items-center justify-between">
        {sortedStates.map((state, index) => {
          const isCompleted = index < currentIndex;
          const isCurrent = index === currentIndex;
          const isUpcoming = index > currentIndex;
          const isLast = index === sortedStates.length - 1;

          return (
            <React.Fragment key={state.code}>
              <div className="flex flex-col items-center flex-1">
                <div
                  className={cn(
                    'flex items-center justify-center w-16 h-16 rounded-lg border-2 transition-all duration-300',
                    {
                      'bg-cyan-400 border-cyan-500 text-white shadow-md': isCurrent && index === 0,
                      'bg-yellow-400 border-yellow-500 text-white shadow-md': isCurrent && index > 0,
                      'bg-green-500 border-green-600 text-white': isCompleted,
                      'bg-gray-100 border-gray-300 text-gray-500': isUpcoming,
                    }
                  )}
                >
                  {isCompleted ? (
                    <Check className="w-8 h-8" />
                  ) : (
                    <FileText className="w-8 h-8" />
                  )}
                </div>
                <div
                  className={cn(
                    'mt-2 text-xs font-medium text-center max-w-[100px]',
                    {
                      'text-cyan-600': isCurrent && index === 0,
                      'text-yellow-600': isCurrent && index > 0,
                      'text-gray-900': isCompleted,
                      'text-gray-400': isUpcoming,
                    }
                  )}
                >
                  {state.name}
                </div>
              </div>
              {!isLast && (
                <div className="flex-1 h-0.5 mx-2 mb-8">
                  <div
                    className={cn('h-full transition-all duration-300', {
                      'bg-green-500': isCompleted,
                      'bg-gray-300': !isCompleted,
                    })}
                  />
                </div>
              )}
            </React.Fragment>
          );
        })}
      </div>
    </div>
  );
};

// Main Tabs Type
type MainTab = 'ACTEURS' | 'COMMANDE_CLIENT' | 'FINANCEMENT' | 'COMMISSION' | 'ACTIONS' | 'DOCUMENTS' | 'LIVRAISON' | 'HISTORIQUE';

interface MainTabConfig {
  id: MainTab;
  label: string;
  icon: React.ReactNode;
}

const mainTabs: MainTabConfig[] = [
  { id: 'ACTEURS', label: 'ACTEURS', icon: <Users className="w-4 h-4" /> },
  { id: 'COMMANDE_CLIENT', label: 'COMMANDE CLIENT', icon: <FileText className="w-4 h-4" /> },
  { id: 'FINANCEMENT', label: 'FINANCEMENT', icon: <CreditCard className="w-4 h-4" /> },
  { id: 'COMMISSION', label: 'COMMISSION', icon: <Clipboard className="w-4 h-4" /> },
  { id: 'ACTIONS', label: 'ACTIONS', icon: <CheckCircle2 className="w-4 h-4" /> },
  { id: 'DOCUMENTS', label: 'DOCUMENTS', icon: <FileCheck className="w-4 h-4" /> },
  { id: 'LIVRAISON', label: 'DÉTAILS DE LIVRAISON', icon: <Truck className="w-4 h-4" /> },
  { id: 'HISTORIQUE', label: 'HISTORIQUE', icon: <History className="w-4 h-4" /> },
];

// Actor Card Component
interface ActorCardProps {
  role: string;
  name: string;
  phone?: string;
  email?: string;
  color?: string;
}

const ActorCard: React.FC<ActorCardProps> = ({ role, name, phone, email, color = 'blue' }) => {
  const [expanded, setExpanded] = useState(false);

  return (
    <div className="border border-gray-200 rounded-lg p-4 bg-white hover:shadow-md transition-shadow">
      <div className="flex items-start justify-between cursor-pointer" onClick={() => setExpanded(!expanded)}>
        <div className="flex-1">
          <div className="text-xs text-gray-500 mb-1">{role}</div>
          <div className={`font-semibold text-${color}-600`}>
            {name}
          </div>
        </div>
        <ChevronRight className={cn('w-4 h-4 text-gray-400 transition-transform', { 'rotate-90': expanded })} />
      </div>
      {expanded && (
        <div className="mt-3 pt-3 border-t border-gray-200 space-y-1">
          {phone && (
            <div className="text-sm">
              <span className="text-gray-600">Téléphone: </span>
              <span className={`text-${color}-600`}>{phone}</span>
            </div>
          )}
          {email && (
            <div className="text-sm">
              <span className="text-gray-600">Email: </span>
              <span className={`text-${color}-600`}>{email}</span>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

// Document Upload Component
interface DocumentUploadProps {
  stateName: string;
  tasks: WorkflowTask[];
  taskInstances: TaskInstance[];
  onTaskUpdate: () => void;
}

const DocumentUploadSection: React.FC<DocumentUploadProps> = ({ stateName, tasks, taskInstances, onTaskUpdate }) => {
  const documentTasks = tasks.filter(t => t.taskType === 'DOCUMENT');
  const [dragActive, setDragActive] = useState(false);
  const [selectedFiles, setSelectedFiles] = useState<Record<string, File[]>>({});

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent, taskCode: string) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const files = Array.from(e.dataTransfer.files);
      setSelectedFiles(prev => ({
        ...prev,
        [taskCode]: [...(prev[taskCode] || []), ...files]
      }));
    }
  };

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>, taskCode: string) => {
    if (e.target.files) {
      const files = Array.from(e.target.files);
      setSelectedFiles(prev => ({
        ...prev,
        [taskCode]: [...(prev[taskCode] || []), ...files]
      }));
    }
  };

  const removeFile = (taskCode: string, fileIndex: number) => {
    setSelectedFiles(prev => ({
      ...prev,
      [taskCode]: prev[taskCode].filter((_, i) => i !== fileIndex)
    }));
  };

  const handleUpload = async (taskCode: string) => {
    const files = selectedFiles[taskCode] || [];
    if (files.length === 0) return;

    const taskInstance = taskInstances.find(ti => ti.taskCode === taskCode);
    if (!taskInstance) {
      toast.error('Task instance not found');
      return;
    }

    try {
      await Promise.all(files.map(file => taskApi.uploadDocument(taskInstance.id, file)));
      toast.success(`${files.length} document(s) téléchargé(s)`);
      setSelectedFiles(prev => ({ ...prev, [taskCode]: [] }));
      onTaskUpdate();
    } catch (error) {
      toast.error('Erreur lors du téléchargement');
    }
  };

  if (documentTasks.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        Aucun document requis pour cette étape
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{stateName}</h3>
      {documentTasks.map((task) => {
        const taskInstance = taskInstances.find(ti => ti.taskCode === task.code);
        const files = selectedFiles[task.code] || [];
        const isCompleted = taskInstance?.status === 'COMPLETED';

        return (
          <div key={task.code} className="border border-gray-200 rounded-lg p-6 bg-white">
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                <h4 className="font-semibold text-gray-900">{task.name}</h4>
                {task.mandatory && (
                  <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-700">Obligatoire</span>
                )}
                {isCompleted && (
                  <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-700">Complété</span>
                )}
              </div>
            </div>

            <div
              className={cn(
                'border-2 border-dashed rounded-lg p-8 transition-colors',
                {
                  'border-blue-400 bg-blue-50': dragActive,
                  'border-gray-300 bg-gray-50': !dragActive && !isCompleted,
                  'border-green-300 bg-green-50': isCompleted,
                }
              )}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={(e) => handleDrop(e, task.code)}
            >
              <div className="text-center">
                <Upload className="mx-auto h-12 w-12 text-gray-400" />
                <div className="mt-4">
                  <label htmlFor={`file-${task.code}`} className="cursor-pointer">
                    <span className="text-sm font-medium text-gray-700">
                      Glissez-déposez vos fichiers ici ou{' '}
                    </span>
                    <span className="text-blue-600 hover:text-blue-500 font-medium">parcourez</span>
                    <input
                      id={`file-${task.code}`}
                      type="file"
                      className="sr-only"
                      multiple
                      accept=".pdf,.png,.jpg,.jpeg"
                      onChange={(e) => handleFileSelect(e, task.code)}
                      disabled={isCompleted}
                    />
                  </label>
                </div>
                <p className="text-xs text-gray-500 mt-2">PDF, PNG, JPG jusqu'à 10MB</p>
              </div>

              {files.length > 0 && (
                <div className="mt-6 space-y-2">
                  <h5 className="text-sm font-medium text-gray-900">Fichiers sélectionnés:</h5>
                  {files.map((file, index) => (
                    <div key={index} className="flex items-center justify-between p-3 bg-white rounded-md border border-gray-200">
                      <div className="flex items-center gap-3">
                        <FileText className="w-5 h-5 text-blue-500" />
                        <div>
                          <div className="text-sm font-medium text-gray-900">{file.name}</div>
                          <div className="text-xs text-gray-500">{(file.size / 1024).toFixed(2)} KB</div>
                        </div>
                      </div>
                      <button
                        type="button"
                        onClick={() => removeFile(task.code, index)}
                        className="p-1 hover:bg-gray-100 rounded"
                      >
                        <X className="w-4 h-4 text-gray-500" />
                      </button>
                    </div>
                  ))}
                  <Button
                    variant="primary"
                    className="w-full mt-4"
                    onClick={() => handleUpload(task.code)}
                  >
                    <Upload className="w-4 h-4 mr-2" />
                    Télécharger {files.length} fichier{files.length > 1 ? 's' : ''}
                  </Button>
                </div>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};

// Actions Tab Component
interface ActionsTabProps {
  workflowProcess: WorkflowProcess;
  taskInstances: TaskInstance[];
  onTaskUpdate: () => void;
}

const ActionsTab: React.FC<ActionsTabProps> = ({ workflowProcess, taskInstances, onTaskUpdate }) => {
  const states = Array.isArray(workflowProcess.states) ? workflowProcess.states : [];

  // Get all ACTION tasks from all states
  const actionTasksByState = states.map(state => ({
    stateName: state.name,
    tasks: (state.tasks || []).filter(t => t.taskType === 'ACTION')
  })).filter(s => s.tasks.length > 0);

  const completeTaskMutation = useMutation({
    mutationFn: (taskInstanceId: number) => {
      return taskApi.completeTask(taskInstanceId, {});
    },
    onSuccess: () => {
      toast.success('Action effectuée avec succès');
      onTaskUpdate();
    },
    onError: () => {
      toast.error('Erreur lors de l\'action');
    },
  });

  return (
    <div className="space-y-8">
      {actionTasksByState.map((stateGroup) => (
        <div key={stateGroup.stateName}>
          <h3 className="text-lg font-semibold text-gray-900 mb-4 pb-2 border-b border-gray-200">
            {stateGroup.stateName}
          </h3>
          <div className="space-y-4">
            {stateGroup.tasks.map((task) => {
              const taskInstance = taskInstances.find(ti => ti.taskCode === task.code);
              const isCompleted = taskInstance?.status === 'COMPLETED';
              const isPending = taskInstance?.status === 'PENDING';

              return (
                <div key={task.code} className="border border-gray-200 rounded-lg p-6 bg-white">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3 flex-1">
                      <div className="flex-1">
                        <div className="flex items-center gap-3">
                          <h4 className="font-semibold text-gray-900">{task.name}</h4>
                          {task.mandatory && (
                            <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-700">Obligatoire</span>
                          )}
                          {isCompleted && (
                            <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-700 flex items-center gap-1">
                              <Check className="w-3 h-3" />
                              Complété
                            </span>
                          )}
                          {isPending && (
                            <span className="px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-700">En attente</span>
                          )}
                        </div>
                        {task.description && (
                          <p className="text-sm text-gray-600 mt-2">{task.description}</p>
                        )}
                      </div>
                    </div>
                    {!isCompleted && taskInstance && (
                      <Button
                        variant="primary"
                        size="sm"
                        onClick={() => completeTaskMutation.mutate(taskInstance.id)}
                        disabled={completeTaskMutation.isPending}
                      >
                        <CheckCircle2 className="w-4 h-4 mr-2" />
                        Effectuer l'action
                      </Button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      ))}
      {actionTasksByState.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          Aucune action requise
        </div>
      )}
    </div>
  );
};

// Documents Tab Component
interface DocumentsTabProps {
  workflowProcess: WorkflowProcess;
  taskInstances: TaskInstance[];
  onTaskUpdate: () => void;
}

const DocumentsTab: React.FC<DocumentsTabProps> = ({ workflowProcess, taskInstances, onTaskUpdate }) => {
  const states = Array.isArray(workflowProcess.states) ? workflowProcess.states : [];

  return (
    <div className="space-y-8">
      {states.map((state) => {
        const documentTasks = (state.tasks || []).filter(t => t.taskType === 'DOCUMENT');
        if (documentTasks.length === 0) return null;

        return (
          <DocumentUploadSection
            key={state.code}
            stateName={state.name}
            tasks={documentTasks}
            taskInstances={taskInstances}
            onTaskUpdate={onTaskUpdate}
          />
        );
      })}
    </div>
  );
};

// Main Edit Page Component
export default function EditOrderPage() {
  const router = useRouter();
  const params = useParams();
  const queryClient = useQueryClient();
  const orderId = parseInt(params.id as string);

  const [activeMainTab, setActiveMainTab] = useState<MainTab>('COMMANDE_CLIENT');
  const [formData, setFormData] = useState<any>({});

  // Fetch order data
  const { data: order, isLoading: orderLoading } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => orderApi.getOrder(orderId),
  });

  // Fetch workflow instance
  const { data: workflowInstance } = useQuery<WorkflowInstance>({
    queryKey: ['workflow-instance', order?.workflowInstanceId],
    queryFn: () => {
      if (!order?.workflowInstanceId) throw new Error('No workflow instance');
      return instanceApi.getInstanceById(order.workflowInstanceId);
    },
    enabled: !!order?.workflowInstanceId,
  });

  // Fetch workflow process
  const { data: workflowProcess } = useQuery<WorkflowProcess>({
    queryKey: ['workflow-process', workflowInstance?.processCode],
    queryFn: () => {
      if (!workflowInstance?.processCode) throw new Error('No process code');
      return workflowApi.getWorkflowByCode(workflowInstance.processCode);
    },
    enabled: !!workflowInstance?.processCode,
    retry: false,
  });

  // Fetch task instances
  const { data: taskInstances = [], refetch: refetchTasks } = useQuery<TaskInstance[]>({
    queryKey: ['task-instances', workflowInstance?.id],
    queryFn: () => {
      if (!workflowInstance?.id) return [];
      return taskApi.getTasks({ workflowInstanceId: workflowInstance.id });
    },
    enabled: !!workflowInstance?.id,
  });

  useEffect(() => {
    if (order) {
      setFormData({
        vin: order.vin,
        make: order.make,
        model: order.model,
        year: order.year,
        orderNumber: order.orderNumber,
        status: order.status,
        notes: order.notes,
      });
    }
  }, [order]);

  const getCurrentStateCode = (): string => {
    if (!workflowInstance || !workflowProcess || !Array.isArray(workflowProcess.states)) return '';
    const currentState = workflowProcess.states.find(s => s.id === workflowInstance.currentStateId);
    return currentState?.code || '';
  };

  const currentStateCode = getCurrentStateCode();
  const states = Array.isArray(workflowProcess?.states) ? workflowProcess.states : [];
  const sortedStates = [...states].sort((a, b) => a.order - b.order);

  const updateOrderMutation = useMutation({
    mutationFn: (data: any) => orderApi.updateOrder(orderId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      toast.success('Commande mise à jour');
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateOrderMutation.mutate(formData);
  };

  if (orderLoading) {
    return <div className="p-6 text-center">Chargement...</div>;
  }

  if (!order) {
    return <div className="p-6 text-center">Commande introuvable</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <form onSubmit={handleSubmit}>
        {/* Workflow Stepper */}
        {sortedStates.length > 0 && (
          <div className="bg-white border-b border-gray-200 px-6 pt-6">
            <WorkflowStepperHeader states={sortedStates} currentStateCode={currentStateCode} />
          </div>
        )}

        <div className="p-6 space-y-6">
          {/* Top Section: General Info + Criteria Sidebar */}
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            <div className="lg:col-span-3">
              <CollapsibleSection title="order.main.tabs.GeneralInfo" defaultOpen={true}>
                <div className="grid grid-cols-4 gap-4 mt-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">N° Commande/Stock</label>
                    <Input value={order.orderNumber} disabled className="bg-gray-100" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Num Châssis</label>
                    <Input value={formData.vin || ''} onChange={(e) => setFormData({...formData, vin: e.target.value})} />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Num Immatriculation</label>
                    <Input value="" placeholder="Num Immatriculation" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Date commande distinct</label>
                    <Input type="date" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Statut</label>
                    <Input value={order.status} disabled className="bg-gray-100" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Affaire</label>
                    <select className="w-full px-3 py-2 border border-gray-300 rounded-md">
                      <option>CHERBOURG</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Commercial(s)</label>
                    <select className="w-full px-3 py-2 border border-gray-300 rounded-md">
                      <option>ANTHONE SUIVE</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Référence et nom de client</label>
                    <Input value="ABC Corporation" disabled className="bg-gray-100" />
                  </div>
                  <div className="col-span-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">Commentaire</label>
                    <textarea
                      rows={3}
                      value={formData.notes || ''}
                      onChange={(e) => setFormData({...formData, notes: e.target.value})}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md"
                    />
                  </div>
                </div>
              </CollapsibleSection>
            </div>

            <div className="lg:col-span-1">
              <OrderCriteriaSidebar
                orderId={orderId}
                assignedConditions={order.assignedConditions || []}
                editable={false}
              />
            </div>
          </div>

          {/* Main Tabs */}
          <div className="bg-white rounded-lg shadow-sm border border-gray-200">
            {/* Tab Navigation */}
            <div className="border-b border-gray-200">
              <div className="flex flex-wrap gap-2 p-2">
                {mainTabs.map((tab) => (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => setActiveMainTab(tab.id)}
                    className={cn(
                      'flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-md transition-colors',
                      {
                        'bg-blue-600 text-white shadow-sm': activeMainTab === tab.id,
                        'bg-gray-100 text-gray-700 hover:bg-gray-200': activeMainTab !== tab.id,
                      }
                    )}
                  >
                    {tab.icon}
                    {tab.label}
                  </button>
                ))}
              </div>
            </div>

            {/* Tab Content */}
            <div className="p-6">
              {/* ACTEURS Tab */}
              {activeMainTab === 'ACTEURS' && (
                <div className="space-y-4">
                  <Button variant="primary" size="sm">
                    <Plus className="w-4 h-4 mr-2" />
                    Ajouter un acteur
                  </Button>
                  <div className="grid grid-cols-3 gap-4">
                    <ActorCard role="Client" name="ABC Corporation" phone="(+33) 77 77 77 77 77" email="test@gmail.com" color="blue" />
                    <ActorCard role="Chef de Vente" name="Cynthia Orchide" phone="(+33) 52 78 25 27 85" email="Cynthia@gmail.com" color="blue" />
                    <ActorCard role="Financeur" name="Mobilize" email="test@Mobilize.com" color="blue" />
                    <ActorCard role="Secrétaire" name="Laura CADEL" phone="(+33) 56 55 55 77 77" email="laura.cadel@bodemerauto.com" color="blue" />
                    <ActorCard role="Secrétaire" name="Ghadaa Dridii" phone="(+33) 56 85 65 68 68" email="ghadaa@gmail.com" color="blue" />
                    <ActorCard role="Vendeur" name="Viviane Swift" phone="(+33) 25 42 45 20 52" email="Viviane@gmail.com" color="blue" />
                  </div>
                </div>
              )}

              {/* COMMANDE CLIENT Tab */}
              {activeMainTab === 'COMMANDE_CLIENT' && (
                <div className="space-y-6">
                  <div className="grid grid-cols-2 gap-6">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Marque</label>
                      <Input value={formData.make || ''} onChange={(e) => setFormData({...formData, make: e.target.value})} />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Modèle</label>
                      <Input value={formData.model || ''} onChange={(e) => setFormData({...formData, model: e.target.value})} />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Année</label>
                      <Input type="number" value={formData.year || ''} onChange={(e) => setFormData({...formData, year: e.target.value})} />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">VIN</label>
                      <Input value={formData.vin || ''} onChange={(e) => setFormData({...formData, vin: e.target.value})} />
                    </div>
                  </div>
                  <Button variant="primary" type="submit">
                    <Save className="w-4 h-4 mr-2" />
                    Enregistrer les modifications
                  </Button>
                </div>
              )}

              {/* FINANCEMENT Tab */}
              {activeMainTab === 'FINANCEMENT' && (
                <div className="text-center py-12 text-gray-500">
                  Section Financement - À implémenter
                </div>
              )}

              {/* COMMISSION Tab */}
              {activeMainTab === 'COMMISSION' && (
                <div className="text-center py-12 text-gray-500">
                  Section Commission - À implémenter
                </div>
              )}

              {/* ACTIONS Tab */}
              {activeMainTab === 'ACTIONS' && workflowProcess && (
                <ActionsTab
                  workflowProcess={workflowProcess}
                  taskInstances={taskInstances}
                  onTaskUpdate={refetchTasks}
                />
              )}

              {/* DOCUMENTS Tab */}
              {activeMainTab === 'DOCUMENTS' && workflowProcess && (
                <EnhancedDocumentsTab
                  workflowProcess={workflowProcess}
                  taskInstances={taskInstances}
                  onTaskUpdate={refetchTasks}
                />
              )}

              {/* LIVRAISON Tab */}
              {activeMainTab === 'LIVRAISON' && (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Date de livraison prévue</label>
                      <Input type="date" />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">Lieu de livraison</label>
                      <Input placeholder="Adresse de livraison" />
                    </div>
                  </div>
                </div>
              )}

              {/* HISTORIQUE Tab */}
              {activeMainTab === 'HISTORIQUE' && (
                <div className="space-y-4">
                  <div className="text-sm text-gray-600">
                    <div className="border-l-4 border-blue-500 pl-4 py-2 mb-4">
                      <div className="font-semibold">Workflow initialisé</div>
                      <div className="text-xs text-gray-500">
                        {workflowInstance?.createdAt && new Date(workflowInstance.createdAt).toLocaleString('fr-FR')}
                      </div>
                      <div className="mt-1">État: {workflowInstance?.currentStateName || 'Commande'}</div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
