'use client';

import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter, useParams } from 'next/navigation';
import Card, { CardContent, CardHeader, CardTitle } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import CollapsibleSection from '@/components/ui/CollapsibleSection';
import Tabs, { Tab } from '@/components/ui/Tabs';
import OrderCriteriaSidebar from '@/components/orders/OrderCriteriaSidebar';
import { orderApi, Order } from '@/lib/api/orders';
import { workflowApi } from '@/lib/api/workflows';
import { instanceApi, WorkflowInstance } from '@/lib/api/instances';
import { taskApi, CompleteTaskRequest } from '@/lib/api/tasks';
import { businessUnitApi } from '@/lib/api/business-units';
import { userApi } from '@/lib/api/users';
import { WorkflowProcess, WorkflowState, WorkflowTask, TaskInstance, TaskType } from '@/types/workflow';
import toast from 'react-hot-toast';
import {
  Save,
  Printer,
  FileText,
  AlertTriangle,
  Calendar,
  Upload,
  Check,
  CheckCircle2,
  XCircle,
  Clock,
  ChevronRight
} from 'lucide-react';
import { cn } from '@/lib/utils';

// Workflow Stepper Component
interface WorkflowStepperProps {
  states: WorkflowState[];
  currentStateCode: string;
  onStateClick?: (stateCode: string) => void;
}

const WorkflowStepperHeader: React.FC<WorkflowStepperProps> = ({
  states,
  currentStateCode,
  onStateClick
}) => {
  const sortedStates = [...states].sort((a, b) => a.order - b.order);
  const currentIndex = sortedStates.findIndex(s => s.code === currentStateCode);

  return (
    <div className="bg-white rounded-lg shadow p-6 mb-6">
      <div className="flex items-center justify-between">
        {sortedStates.map((state, index) => {
          const isCompleted = index < currentIndex;
          const isCurrent = index === currentIndex;
          const isUpcoming = index > currentIndex;
          const isLast = index === sortedStates.length - 1;
          const isAccessible = index <= currentIndex;

          return (
            <React.Fragment key={state.code}>
              <div className="flex flex-col items-center flex-1">
                <button
                  type="button"
                  onClick={() => onStateClick && isAccessible && onStateClick(state.code)}
                  disabled={!isAccessible || !onStateClick}
                  className={cn(
                    'flex items-center justify-center w-12 h-12 rounded-full border-2 transition-all duration-300',
                    {
                      'bg-yellow-400 border-yellow-500 text-white shadow-md': isCurrent,
                      'bg-green-500 border-green-600 text-white': isCompleted,
                      'bg-gray-100 border-gray-300 text-gray-400': isUpcoming,
                      'cursor-pointer hover:scale-110': isAccessible && onStateClick,
                      'cursor-not-allowed': !isAccessible || !onStateClick,
                    }
                  )}
                >
                  {isCompleted ? (
                    <Check className="w-6 h-6" />
                  ) : (
                    <span className="text-sm font-bold">{index + 1}</span>
                  )}
                </button>
                <div
                  className={cn(
                    'mt-2 text-sm font-medium text-center max-w-[120px]',
                    {
                      'text-yellow-600': isCurrent,
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

// Task Section Component
interface TaskSectionProps {
  task: WorkflowTask;
  taskInstance?: TaskInstance;
  orderId: number;
  onTaskUpdate: () => void;
}

const TaskSection: React.FC<TaskSectionProps> = ({ task, taskInstance, orderId, onTaskUpdate }) => {
  const [selectedFiles, setSelectedFiles] = useState<File[]>([]);
  const [uploading, setUploading] = useState(false);

  const completeTaskMutation = useMutation({
    mutationFn: (data: CompleteTaskRequest) => {
      if (!taskInstance?.id) throw new Error('Task instance not found');
      return taskApi.completeTask(taskInstance.id, data);
    },
    onSuccess: () => {
      toast.success('Tâche terminée avec succès');
      onTaskUpdate();
    },
    onError: (error: any) => {
      toast.error(error.message || 'Échec de la finalisation de la tâche');
    },
  });

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      setSelectedFiles(Array.from(e.target.files));
    }
  };

  const handleUpload = async () => {
    if (!taskInstance?.id || selectedFiles.length === 0) return;

    setUploading(true);
    try {
      const uploadPromises = selectedFiles.map(file =>
        taskApi.uploadDocument(taskInstance.id, file)
      );
      await Promise.all(uploadPromises);
      toast.success('Documents téléchargés avec succès');
      setSelectedFiles([]);
      onTaskUpdate();
    } catch (error: any) {
      toast.error(error.message || 'Échec du téléchargement des documents');
    } finally {
      setUploading(false);
    }
  };

  const handleCompleteTask = () => {
    completeTaskMutation.mutate({});
  };

  const getStatusBadge = () => {
    if (!taskInstance) {
      return <span className="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600">Non démarré</span>;
    }

    switch (taskInstance.status) {
      case 'PENDING':
        return <span className="px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-700">En attente</span>;
      case 'IN_PROGRESS':
        return <span className="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-700">En cours</span>;
      case 'COMPLETED':
        return <span className="px-2 py-1 text-xs rounded-full bg-green-100 text-green-700">Terminé</span>;
      case 'REJECTED':
        return <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-700">Rejeté</span>;
      default:
        return null;
    }
  };

  return (
    <Card className="mb-4">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <CardTitle>{task.name}</CardTitle>
            {task.mandatory && (
              <span className="px-2 py-1 text-xs rounded-full bg-red-100 text-red-700">Obligatoire</span>
            )}
            {getStatusBadge()}
          </div>
          {taskInstance?.status !== 'COMPLETED' && (
            <Button
              variant="primary"
              size="sm"
              onClick={handleCompleteTask}
              disabled={completeTaskMutation.isPending}
              isLoading={completeTaskMutation.isPending}
            >
              <CheckCircle2 className="w-4 h-4 mr-2" />
              Marquer comme terminé
            </Button>
          )}
        </div>
        {task.description && (
          <p className="text-sm text-gray-600 mt-2">{task.description}</p>
        )}
      </CardHeader>
      <CardContent>
        {task.taskType === 'DOCUMENT' && (
          <DocumentUploadZone
            taskId={taskInstance?.id}
            selectedFiles={selectedFiles}
            onFileSelect={handleFileSelect}
            onUpload={handleUpload}
            uploading={uploading}
          />
        )}

        {task.taskType === 'ACTION' && (
          <ActionButtons
            task={task}
            taskInstance={taskInstance}
            onComplete={handleCompleteTask}
          />
        )}

        {task.taskType === 'CONTROL' && (
          <ControlChecklist
            task={task}
            taskInstance={taskInstance}
          />
        )}
      </CardContent>
    </Card>
  );
};

// Document Upload Zone Component
interface DocumentUploadZoneProps {
  taskId?: number;
  selectedFiles: File[];
  onFileSelect: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onUpload: () => void;
  uploading: boolean;
}

const DocumentUploadZone: React.FC<DocumentUploadZoneProps> = ({
  taskId,
  selectedFiles,
  onFileSelect,
  onUpload,
  uploading,
}) => {
  return (
    <div className="border-2 border-dashed border-gray-300 rounded-lg p-6">
      <div className="text-center">
        <Upload className="mx-auto h-12 w-12 text-gray-400" />
        <div className="mt-4">
          <label htmlFor="file-upload" className="cursor-pointer">
            <span className="mt-2 block text-sm font-medium text-gray-900">
              Glissez-déposez vos fichiers ici ou
            </span>
            <span className="text-blue-600 hover:text-blue-500"> parcourez</span>
            <input
              id="file-upload"
              name="file-upload"
              type="file"
              className="sr-only"
              multiple
              onChange={onFileSelect}
            />
          </label>
        </div>
        <p className="text-xs text-gray-500 mt-2">PDF, PNG, JPG jusqu'à 10MB</p>
      </div>

      {selectedFiles.length > 0 && (
        <div className="mt-4">
          <h4 className="text-sm font-medium text-gray-900 mb-2">Fichiers sélectionnés:</h4>
          <ul className="space-y-2">
            {selectedFiles.map((file, index) => (
              <li key={index} className="flex items-center justify-between text-sm text-gray-600">
                <span>{file.name}</span>
                <span className="text-gray-400">{(file.size / 1024).toFixed(2)} KB</span>
              </li>
            ))}
          </ul>
          <Button
            variant="primary"
            className="mt-4 w-full"
            onClick={onUpload}
            disabled={uploading}
            isLoading={uploading}
          >
            Télécharger {selectedFiles.length} fichier{selectedFiles.length > 1 ? 's' : ''}
          </Button>
        </div>
      )}
    </div>
  );
};

// Action Buttons Component
interface ActionButtonsProps {
  task: WorkflowTask;
  taskInstance?: TaskInstance;
  onComplete: () => void;
}

const ActionButtons: React.FC<ActionButtonsProps> = ({ task, taskInstance, onComplete }) => {
  return (
    <div className="space-y-4">
      <p className="text-sm text-gray-600">
        Cette tâche nécessite une action. Cliquez sur le bouton ci-dessous pour la marquer comme terminée.
      </p>
      {taskInstance?.status !== 'COMPLETED' && (
        <div className="flex gap-3">
          <Button variant="primary" onClick={onComplete}>
            <CheckCircle2 className="w-4 h-4 mr-2" />
            Effectuer l'action
          </Button>
        </div>
      )}
    </div>
  );
};

// Control Checklist Component
interface ControlChecklistProps {
  task: WorkflowTask;
  taskInstance?: TaskInstance;
}

const ControlChecklist: React.FC<ControlChecklistProps> = ({ task, taskInstance }) => {
  const [checks, setChecks] = useState<Record<string, boolean>>({
    check1: false,
    check2: false,
    check3: false,
  });

  const handleCheckChange = (key: string) => {
    setChecks(prev => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <div className="space-y-3">
      <p className="text-sm text-gray-600 mb-4">Vérifiez tous les points suivants:</p>
      <label className="flex items-center space-x-3 cursor-pointer">
        <input
          type="checkbox"
          checked={checks.check1}
          onChange={() => handleCheckChange('check1')}
          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
        <span className="text-sm text-gray-700">Vérifier les informations du client</span>
      </label>
      <label className="flex items-center space-x-3 cursor-pointer">
        <input
          type="checkbox"
          checked={checks.check2}
          onChange={() => handleCheckChange('check2')}
          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
        <span className="text-sm text-gray-700">Vérifier les détails du véhicule</span>
      </label>
      <label className="flex items-center space-x-3 cursor-pointer">
        <input
          type="checkbox"
          checked={checks.check3}
          onChange={() => handleCheckChange('check3')}
          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
        <span className="text-sm text-gray-700">Vérifier les conditions de financement</span>
      </label>
    </div>
  );
};

// Main Edit Page Component
export default function EditOrderPage() {
  const router = useRouter();
  const params = useParams();
  const queryClient = useQueryClient();
  const orderId = parseInt(params.id as string);

  const [activeTab, setActiveTab] = useState<string>('');
  const [formData, setFormData] = useState<any>({});

  // Fetch order data
  const { data: order, isLoading: orderLoading } = useQuery({
    queryKey: ['order', orderId],
    queryFn: () => orderApi.getOrder(orderId),
  });

  // Fetch workflow instance for this order
  const { data: workflowInstance, isLoading: instanceLoading } = useQuery<WorkflowInstance>({
    queryKey: ['workflow-instance', order?.workflowInstanceId],
    queryFn: () => {
      if (!order?.workflowInstanceId) throw new Error('No workflow instance');
      return instanceApi.getInstanceById(order.workflowInstanceId);
    },
    enabled: !!order?.workflowInstanceId,
  });

  // Fetch workflow process - use processCode from instance
  const { data: workflowProcess, isLoading: processLoading } = useQuery<WorkflowProcess>({
    queryKey: ['workflow-process', workflowInstance?.processCode],
    queryFn: () => {
      if (!workflowInstance?.processCode) {
        throw new Error('No process code available');
      }
      return workflowApi.getWorkflowByCode(workflowInstance.processCode);
    },
    enabled: !!workflowInstance?.processCode,
    retry: false,
  });

  // Fetch task instances for this workflow instance
  const { data: taskInstances = [], refetch: refetchTasks } = useQuery<TaskInstance[]>({
    queryKey: ['task-instances', workflowInstance?.id],
    queryFn: () => {
      if (!workflowInstance?.id) return [];
      return taskApi.getTasks({ workflowInstanceId: workflowInstance.id });
    },
    enabled: !!workflowInstance?.id,
  });

  const { data: businessUnits = [] } = useQuery({
    queryKey: ['business-units-active'],
    queryFn: () => businessUnitApi.getActiveBusinessUnits(),
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users-active'],
    queryFn: () => userApi.getActiveUsers(),
  });

  useEffect(() => {
    if (order) {
      setFormData({
        vin: order.vin,
        make: order.make,
        model: order.model,
        year: order.year,
        trim: order.trim,
        colorExterior: order.colorExterior,
        colorInterior: order.colorInterior,
        basePrice: order.basePrice,
        notes: order.notes,
      });
    }
  }, [order]);

  // Get current state code from workflow instance and process
  const getCurrentStateCode = (): string => {
    if (!workflowInstance || !workflowProcess) return '';
    if (!Array.isArray(workflowProcess.states)) return '';

    // Find the state by currentStateId from the instance
    const currentState = workflowProcess.states.find(
      s => s.id === workflowInstance.currentStateId
    );

    return currentState?.code || '';
  };

  const currentStateCode = getCurrentStateCode();

  // Set initial active tab to current state
  useEffect(() => {
    if (currentStateCode && !activeTab) {
      setActiveTab(currentStateCode);
    } else if (workflowProcess?.states && workflowProcess.states.length > 0 && !activeTab && !currentStateCode) {
      const sortedStates = [...workflowProcess.states].sort((a, b) => a.order - b.order);
      setActiveTab(sortedStates[0].code);
    }
  }, [workflowProcess, activeTab, currentStateCode]);

  const updateOrderMutation = useMutation({
    mutationFn: (data: any) => orderApi.updateOrder(orderId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      toast.success('Commande mise à jour avec succès');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Échec de la mise à jour de la commande');
    },
  });

  const transitionMutation = useMutation({
    mutationFn: (toStateCode: string) => {
      if (!workflowInstance?.id) throw new Error('No workflow instance');
      return instanceApi.executeTransition(workflowInstance.id, { toStateCode });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow-instance'] });
      queryClient.invalidateQueries({ queryKey: ['order', orderId] });
      toast.success('Transition effectuée avec succès');
      refetchTasks();
    },
    onError: (error: any) => {
      toast.error(error.message || 'Échec de la transition');
    },
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    updateOrderMutation.mutate(formData);
  };

  const handleNextState = () => {
    if (!workflowProcess?.transitions || !workflowInstance || !currentStateCode) return;

    const availableTransitions = workflowProcess.transitions.filter(
      t => t.fromStateCode === currentStateCode
    );

    if (availableTransitions.length > 0) {
      const nextTransition = availableTransitions[0];
      transitionMutation.mutate(nextTransition.toStateCode);
    }
  };

  const getTasksForState = (stateCode: string): WorkflowTask[] => {
    if (!workflowProcess || !Array.isArray(workflowProcess.states)) return [];
    const state = workflowProcess.states.find(s => s.code === stateCode);
    if (!state?.tasks || !Array.isArray(state.tasks)) return [];
    return [...state.tasks].sort((a, b) => a.order - b.order);
  };

  const getTaskInstanceForTask = (taskCode: string): TaskInstance | undefined => {
    return taskInstances.find(ti => ti.taskCode === taskCode);
  };

  const isStateAccessible = (state: WorkflowState): boolean => {
    if (!workflowInstance || !workflowProcess || !currentStateCode) return false;
    if (!Array.isArray(workflowProcess.states)) return false;
    const currentState = workflowProcess.states.find(s => s.code === currentStateCode);
    if (!currentState) return false;
    return state.order <= currentState.order;
  };

  const canTransitionToNextState = (): boolean => {
    if (!workflowInstance || !workflowProcess || !currentStateCode) return false;
    if (!Array.isArray(workflowProcess.states)) return false;

    const currentState = workflowProcess.states.find(s => s.code === currentStateCode);
    if (!currentState) return false;

    const mandatoryTasks = Array.isArray(currentState.tasks) ? currentState.tasks.filter(t => t.mandatory) : [];
    const mandatoryTaskInstances = mandatoryTasks.map(t => getTaskInstanceForTask(t.code));

    return mandatoryTaskInstances.every(ti => ti?.status === 'COMPLETED');
  };

  if (orderLoading || instanceLoading || processLoading) {
    return (
      <div className="p-6">
        <div className="text-center py-12 text-gray-500">Chargement...</div>
      </div>
    );
  }

  if (!order || !workflowProcess) {
    return (
      <div className="p-6">
        <div className="text-center py-12">
          <p className="text-gray-500">Commande ou workflow introuvable</p>
          <Button variant="outline" onClick={() => router.back()} className="mt-4">
            Retour
          </Button>
        </div>
      </div>
    );
  }

  // Ensure states is an array
  const states = Array.isArray(workflowProcess.states) ? workflowProcess.states : [];
  const sortedStates = [...states].sort((a, b) => a.order - b.order);

  return (
    <div className="min-h-screen bg-gray-50">
      <form onSubmit={handleSubmit}>
        <div className="p-6 space-y-6">
          {/* Workflow Stepper Header */}
          <WorkflowStepperHeader
            states={sortedStates}
            currentStateCode={currentStateCode}
            onStateClick={(stateCode) => setActiveTab(stateCode)}
          />

          {/* Main Layout: 3 columns + 1 sidebar */}
          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            {/* Main Content Area - Takes 3/4 of space */}
            <div className="lg:col-span-3 space-y-6">
              {/* Action Buttons */}
              <div className="flex flex-wrap gap-3 justify-end">
                <Button variant="outline" size="sm" type="button">
                  <FileText className="w-4 h-4 mr-2" />
                  Récapitulatif
                </Button>
                <Button variant="outline" size="sm" type="button">
                  <Printer className="w-4 h-4 mr-2" />
                  Imprimer
                </Button>
                <Button
                  variant="primary"
                  size="sm"
                  type="submit"
                  disabled={updateOrderMutation.isPending}
                  isLoading={updateOrderMutation.isPending}
                >
                  <Save className="w-4 h-4 mr-2" />
                  Enregistrer
                </Button>
                <Button variant="outline" size="sm" type="button">
                  <AlertTriangle className="w-4 h-4 mr-2" />
                  Gestion Alert
                </Button>
                <Button variant="outline" size="sm" type="button">
                  <Calendar className="w-4 h-4 mr-2" />
                  Planning
                </Button>
              </div>

              {/* Collapsible General Information */}
              <CollapsibleSection title="Informations Générales" defaultOpen={true}>
                <div className="grid grid-cols-3 gap-4 mt-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      N° Commande
                    </label>
                    <Input
                      value={order.orderNumber}
                      disabled
                      className="bg-gray-100"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      VIN
                    </label>
                    <Input
                      value={formData.vin || ''}
                      onChange={(e) => setFormData({ ...formData, vin: e.target.value })}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Statut
                    </label>
                    <Input
                      value={order.status}
                      disabled
                      className="bg-gray-100"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Marque
                    </label>
                    <Input
                      value={formData.make || ''}
                      onChange={(e) => setFormData({ ...formData, make: e.target.value })}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Modèle
                    </label>
                    <Input
                      value={formData.model || ''}
                      onChange={(e) => setFormData({ ...formData, model: e.target.value })}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Client
                    </label>
                    <Input
                      value={`${order.customerFirstName || ''} ${order.customerLastName || ''}`}
                      disabled
                      className="bg-gray-100"
                    />
                  </div>
                  <div className="col-span-3">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Commentaire
                    </label>
                    <textarea
                      rows={3}
                      value={formData.notes || ''}
                      onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
              </CollapsibleSection>

              {/* Workflow State Tabs */}
              <Card>
                <CardHeader>
                  <CardTitle>Workflow - Étapes et Tâches</CardTitle>
                </CardHeader>
                <CardContent>
                  <Tabs
                    tabs={sortedStates.map((state) => {
                      const isAccessible = isStateAccessible(state);
                      const isCurrent = state.code === currentStateCode;
                      const stateTasks = getTasksForState(state.code);

                      return {
                        id: state.code,
                        label: state.name,
                        disabled: !isAccessible,
                        badge: isCurrent ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800">
                            Actuel
                          </span>
                        ) : undefined,
                        content: (
                          <div className="space-y-4">
                            {stateTasks.map((task) => (
                              <TaskSection
                                key={task.code}
                                task={task}
                                taskInstance={getTaskInstanceForTask(task.code)}
                                orderId={orderId}
                                onTaskUpdate={refetchTasks}
                              />
                            ))}

                            {stateTasks.length === 0 && (
                              <div className="text-center py-8 text-gray-500">
                                Aucune tâche pour cette étape
                              </div>
                            )}

                            {/* Transition Button - only show on current state */}
                            {state.code === currentStateCode && (
                              <div className="mt-6 pt-6 border-t border-gray-200">
                                <Button
                                  variant="primary"
                                  size="lg"
                                  onClick={handleNextState}
                                  disabled={!canTransitionToNextState() || transitionMutation.isPending}
                                  isLoading={transitionMutation.isPending}
                                  className="w-full"
                                >
                                  <ChevronRight className="w-5 h-5 mr-2" />
                                  Passer à l'étape suivante
                                </Button>
                                {!canTransitionToNextState() && (
                                  <p className="text-sm text-amber-600 mt-2 text-center">
                                    Toutes les tâches obligatoires doivent être terminées pour passer à l'étape suivante
                                  </p>
                                )}
                              </div>
                            )}
                          </div>
                        ),
                      };
                    })}
                    activeTab={activeTab}
                    onTabChange={setActiveTab}
                  />
                </CardContent>
              </Card>
            </div>

            {/* Right Sidebar - Criteria */}
            <div className="lg:col-span-1">
              <OrderCriteriaSidebar
                orderId={orderId}
                assignedConditions={order.assignedConditions || []}
                editable={false}
              />
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}
