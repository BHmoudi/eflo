import React, { useState } from 'react';
import { WorkflowProcess, WorkflowState, WorkflowTask, TaskInstance } from '@/types/workflow';
import { taskApi } from '@/lib/api/tasks';
import toast from 'react-hot-toast';
import {
  ChevronDown,
  ChevronRight,
  Upload,
  Download,
  Trash2,
  Eye,
  FileText,
  Check,
  Clock
} from 'lucide-react';
import { cn } from '@/lib/utils';
import Button from '@/components/ui/Button';

interface EnhancedDocumentsTabProps {
  workflowProcess: WorkflowProcess;
  taskInstances: TaskInstance[];
  onTaskUpdate: () => void;
}

interface DocumentItemProps {
  task: WorkflowTask;
  taskInstance?: TaskInstance;
  onUpdate: () => void;
}

const DocumentItem: React.FC<DocumentItemProps> = ({ task, taskInstance, onUpdate }) => {
  const [selected, setSelected] = useState(false);
  const [uploadedFile, setUploadedFile] = useState<File | null>(null);
  const [comment, setComment] = useState('');
  const [uploading, setUploading] = useState(false);
  const [pasteText, setPasteText] = useState('');

  const isCompleted = taskInstance?.status === 'COMPLETED';
  const status = isCompleted ? 'Reçu' : 'En attente';

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setUploadedFile(e.target.files[0]);
    }
  };

  const handleUpload = async () => {
    if (!taskInstance?.id || !uploadedFile) return;

    setUploading(true);
    try {
      await taskApi.uploadDocument(taskInstance.id, uploadedFile);
      await taskApi.completeTask(taskInstance.id, { comments: comment });
      toast.success('Document téléchargé avec succès');
      setUploadedFile(null);
      setComment('');
      onUpdate();
    } catch (error) {
      toast.error('Erreur lors du téléchargement');
    } finally {
      setUploading(false);
    }
  };

  const handlePaste = async (e: React.ClipboardEvent) => {
    const items = e.clipboardData?.items;
    if (!items) return;

    for (let i = 0; i < items.length; i++) {
      if (items[i].type.indexOf('image') !== -1) {
        const blob = items[i].getAsFile();
        if (blob && taskInstance?.id) {
          try {
            await taskApi.uploadDocument(taskInstance.id, blob);
            toast.success('Capture d\'écran téléchargée');
            onUpdate();
          } catch (error) {
            toast.error('Erreur lors du téléchargement');
          }
        }
      }
    }
  };

  return (
    <div className="border-b border-gray-200 py-3 hover:bg-gray-50">
      <div className="flex items-start gap-4">
        {/* Checkbox */}
        <div className="pt-1">
          <input
            type="checkbox"
            checked={selected}
            onChange={(e) => setSelected(e.target.checked)}
            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
        </div>

        {/* Document Info */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-sm font-medium text-gray-900">Documents Attendus</span>
            {task.mandatory && <span className="text-red-500 text-sm">*</span>}
          </div>
          <div className="text-sm text-gray-700 font-semibold mb-2">{task.name}</div>

          {isCompleted && (
            <div className="grid grid-cols-4 gap-4 text-xs text-gray-600 mb-2">
              <div>
                <span className="font-medium">Statut: </span>
                <span className="text-green-600 font-semibold">{status}</span>
              </div>
              <div>
                <span className="font-medium">Date de réception: </span>
                <span>{taskInstance?.completedAt ? new Date(taskInstance.completedAt).toLocaleString('fr-FR') : '-'}</span>
              </div>
              <div>
                <span className="font-medium">Utilisateur de modification: </span>
                <span>{taskInstance?.assignedToUserName || 'SYSTÈME'}</span>
              </div>
              <div>
                <span className="font-medium">Commentaire</span>
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex items-center gap-2 mb-2">
            {isCompleted && (
              <>
                <button className="p-1.5 hover:bg-gray-100 rounded" title="Voir">
                  <Eye className="w-4 h-4 text-gray-600" />
                </button>
                <button className="p-1.5 hover:bg-gray-100 rounded" title="Télécharger">
                  <Download className="w-4 h-4 text-gray-600" />
                </button>
                <button className="p-1.5 hover:bg-gray-100 rounded" title="Supprimer">
                  <Trash2 className="w-4 h-4 text-gray-600" />
                </button>
              </>
            )}
          </div>

          {/* Upload Section */}
          {!isCompleted && (
            <div className="flex items-center gap-3">
              <div className="flex-1">
                <label className="cursor-pointer">
                  <div className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-md hover:border-blue-500 hover:bg-blue-50 transition-colors">
                    <Upload className="w-4 h-4 text-gray-500" />
                    <span className="text-sm text-gray-700">
                      {uploadedFile ? uploadedFile.name : 'Cliquez pour télécharger ou faites glisser le fichier'}
                    </span>
                    <input
                      type="file"
                      className="sr-only"
                      onChange={handleFileSelect}
                      accept=".pdf,.png,.jpg,.jpeg"
                    />
                  </div>
                </label>
              </div>
              {uploadedFile && (
                <Button
                  variant="primary"
                  size="sm"
                  onClick={handleUpload}
                  disabled={uploading}
                  isLoading={uploading}
                >
                  <Upload className="w-4 h-4 mr-1" />
                  Télécharger
                </Button>
              )}
            </div>
          )}

          {/* Paste Text Field */}
          {!isCompleted && (
            <div className="mt-2">
              <textarea
                placeholder="Collez une capture d'écran dans la zone de texte"
                className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500"
                rows={2}
                value={pasteText}
                onChange={(e) => setPasteText(e.target.value)}
                onPaste={handlePaste}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

interface StateSectionProps {
  stateName: string;
  stateOrder: number;
  tasks: WorkflowTask[];
  taskInstances: TaskInstance[];
  onUpdate: () => void;
}

const StateSection: React.FC<StateSectionProps> = ({ stateName, stateOrder, tasks, taskInstances, onUpdate }) => {
  const [expanded, setExpanded] = useState(stateOrder === 1); // First section expanded by default

  const documentTasks = tasks.filter(t => t.taskType === 'DOCUMENT');
  if (documentTasks.length === 0) return null;

  const completedCount = documentTasks.filter(t => {
    const instance = taskInstances.find(ti => ti.taskCode === t.code);
    return instance?.status === 'COMPLETED';
  }).length;

  const totalCount = documentTasks.length;
  const statusText = completedCount === totalCount ? 'Traité' : 'En traitement';
  const statusColor = completedCount === totalCount ? 'text-green-600' : 'text-blue-600';

  return (
    <div className="mb-6">
      {/* Section Header */}
      <div
        className="flex items-center justify-between p-4 bg-gray-50 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-100"
        onClick={() => setExpanded(!expanded)}
      >
        <div className="flex items-center gap-3">
          {expanded ? (
            <ChevronDown className="w-5 h-5 text-gray-600" />
          ) : (
            <ChevronRight className="w-5 h-5 text-gray-600" />
          )}
          <h3 className="text-base font-semibold text-gray-900">{stateName}</h3>
        </div>
        <div className="flex items-center gap-6 text-sm">
          <span className={cn('font-semibold', statusColor)}>{statusText}</span>
          <span className="text-gray-600">{completedCount}/{totalCount}</span>
          <span className="text-gray-400">0/{totalCount}</span>
        </div>
      </div>

      {/* Document List */}
      {expanded && (
        <div className="mt-2 bg-white border border-gray-200 rounded-lg">
          {documentTasks.map((task) => {
            const taskInstance = taskInstances.find(ti => ti.taskCode === task.code);
            return (
              <DocumentItem
                key={task.code}
                task={task}
                taskInstance={taskInstance}
                onUpdate={onUpdate}
              />
            );
          })}
        </div>
      )}
    </div>
  );
};

export const EnhancedDocumentsTab: React.FC<EnhancedDocumentsTabProps> = ({
  workflowProcess,
  taskInstances,
  onTaskUpdate,
}) => {
  const states = Array.isArray(workflowProcess.states) ? workflowProcess.states : [];
  const sortedStates = [...states].sort((a, b) => a.order - b.order);

  // Count total documents
  const totalDocuments = sortedStates.reduce((sum, state) => {
    const docs = (state.tasks || []).filter(t => t.taskType === 'DOCUMENT');
    return sum + docs.length;
  }, 0);

  const completedDocuments = taskInstances.filter(ti =>
    ti.status === 'COMPLETED' &&
    sortedStates.some(s => (s.tasks || []).some(t => t.code === ti.taskCode && t.taskType === 'DOCUMENT'))
  ).length;

  return (
    <div className="space-y-4">
      {/* Top Actions */}
      <div className="flex items-center gap-3 mb-6">
        <Button variant="outline" size="sm">
          Éclater en un seul clic
        </Button>
        <Button variant="outline" size="sm">
          Document Éclater
        </Button>
        <div className="flex-1">
          <input
            type="text"
            placeholder="Rechercher un document..."
            className="w-full px-4 py-2 border border-gray-300 rounded-md text-sm"
          />
        </div>
        <div className="flex items-center gap-2">
          <input type="checkbox" className="w-4 h-4" />
          <span className="text-sm text-gray-700">Téléchargement Par Lot</span>
        </div>
      </div>

      {/* Progress Overview */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <FileText className="w-6 h-6 text-blue-600" />
            <div>
              <div className="text-sm font-semibold text-gray-900">Liste des documents</div>
              <div className="text-xs text-gray-600">Progression globale</div>
            </div>
          </div>
          <div className="flex items-center gap-6">
            <div className="text-center">
              <div className="text-sm text-gray-600">Statut</div>
              <div className="text-lg font-bold text-blue-600">
                {completedDocuments === totalDocuments ? 'Complet' : 'En cours'}
              </div>
            </div>
            <div className="text-center">
              <div className="text-sm text-gray-600">Documents reçus</div>
              <div className="text-lg font-bold text-green-600">{completedDocuments}/{totalDocuments}</div>
            </div>
            <div className="text-center">
              <div className="text-sm text-gray-600">En attente</div>
              <div className="text-lg font-bold text-orange-600">{totalDocuments - completedDocuments}/{totalDocuments}</div>
            </div>
          </div>
        </div>
      </div>

      {/* Document Sections by State */}
      {sortedStates.map((state) => (
        <StateSection
          key={state.code}
          stateName={state.name}
          stateOrder={state.order}
          tasks={state.tasks || []}
          taskInstances={taskInstances}
          onUpdate={onTaskUpdate}
        />
      ))}

      {sortedStates.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          Aucun document défini pour ce workflow
        </div>
      )}
    </div>
  );
};

export default EnhancedDocumentsTab;
