'use client';

import React, { useState } from 'react';
import Button from '@/components/ui/Button';
import Select from '@/components/ui/Select';
import { Plus, Trash2 } from 'lucide-react';

export interface TaskDependency {
  requiredTaskCode: string;
  dependencyType: string;
  requiredStatus?: string;
}

interface TaskDependenciesEditorProps {
  dependencies: TaskDependency[];
  availableTasks: Array<{ code: string; name: string }>;
  onChange: (dependencies: TaskDependency[]) => void;
}

export default function TaskDependenciesEditor({
  dependencies,
  availableTasks,
  onChange,
}: TaskDependenciesEditorProps) {
  const [currentDependency, setCurrentDependency] = useState<TaskDependency>({
    requiredTaskCode: '',
    dependencyType: 'COMPLETION',
  });

  const dependencyTypes = [
    { value: 'COMPLETION', label: 'Task Completion', description: 'Task must be completed' },
    { value: 'APPROVAL', label: 'Task Approval', description: 'Task must be approved' },
    { value: 'STATUS', label: 'Specific Status', description: 'Task must have specific status' },
    { value: 'FIELD_VALUE', label: 'Field Value', description: 'Task field must have specific value' },
  ];

  const handleAddDependency = () => {
    if (!currentDependency.requiredTaskCode) {
      alert('Please select a required task');
      return;
    }

    onChange([...dependencies, currentDependency]);
    setCurrentDependency({
      requiredTaskCode: '',
      dependencyType: 'COMPLETION',
    });
  };

  const handleDeleteDependency = (index: number) => {
    onChange(dependencies.filter((_, i) => i !== index));
  };

  return (
    <div className="space-y-4">
      <div className="border border-gray-200 rounded-lg p-4 bg-gray-50">
        <h4 className="text-sm font-semibold text-gray-700 mb-3">Add Task Dependency</h4>

        <div className="grid grid-cols-1 gap-3">
          <Select
            label="Required Task"
            value={currentDependency.requiredTaskCode}
            onChange={(e) =>
              setCurrentDependency({ ...currentDependency, requiredTaskCode: e.target.value })
            }
          >
            <option value="">Select a task...</option>
            {availableTasks.map((task) => (
              <option key={task.code} value={task.code}>
                {task.name} ({task.code})
              </option>
            ))}
          </Select>

          <Select
            label="Dependency Type"
            value={currentDependency.dependencyType}
            onChange={(e) =>
              setCurrentDependency({ ...currentDependency, dependencyType: e.target.value })
            }
          >
            {dependencyTypes.map((type) => (
              <option key={type.value} value={type.value}>
                {type.label} - {type.description}
              </option>
            ))}
          </Select>

          {currentDependency.dependencyType === 'STATUS' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Required Status
              </label>
              <input
                type="text"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={currentDependency.requiredStatus || ''}
                onChange={(e) =>
                  setCurrentDependency({ ...currentDependency, requiredStatus: e.target.value })
                }
                placeholder="e.g., APPROVED, VALIDATED"
              />
            </div>
          )}
        </div>

        <div className="mt-3 flex justify-end">
          <Button variant="primary" size="sm" onClick={handleAddDependency}>
            <Plus className="w-4 h-4 mr-1" />
            Add Dependency
          </Button>
        </div>
      </div>

      {/* Dependencies List */}
      {dependencies.length > 0 && (
        <div className="space-y-2">
          <h4 className="text-sm font-semibold text-gray-700">Configured Dependencies</h4>
          {dependencies.map((dep, index) => {
            const task = availableTasks.find((t) => t.code === dep.requiredTaskCode);
            const depType = dependencyTypes.find((t) => t.value === dep.dependencyType);

            return (
              <div
                key={index}
                className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-lg"
              >
                <div className="flex-1">
                  <div className="font-medium text-gray-900">
                    {task?.name || dep.requiredTaskCode}
                  </div>
                  <div className="text-sm text-gray-600 mt-1">
                    Type: <span className="font-medium">{depType?.label}</span>
                    {dep.requiredStatus && (
                      <span className="ml-2">
                        Status: <span className="font-medium">{dep.requiredStatus}</span>
                      </span>
                    )}
                  </div>
                </div>
                <button
                  onClick={() => handleDeleteDependency(index)}
                  className="p-2 text-red-600 hover:bg-red-50 rounded transition-colors"
                  title="Delete"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            );
          })}
        </div>
      )}

      {dependencies.length === 0 && (
        <div className="text-center py-8 text-gray-500 text-sm">
          No dependencies configured. This task can be started independently.
        </div>
      )}
    </div>
  );
}
