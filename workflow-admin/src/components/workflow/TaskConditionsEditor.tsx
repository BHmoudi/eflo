'use client';

import React, { useState } from 'react';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import { Plus, Trash2 } from 'lucide-react';

export interface TaskCondition {
  conditionType: string;
  conditionField?: string;
  conditionOperator?: string;
  conditionValue?: string;
  conditionExpression?: string;
}

interface TaskConditionsEditorProps {
  conditions: TaskCondition[];
  onChange: (conditions: TaskCondition[]) => void;
}

export default function TaskConditionsEditor({ conditions, onChange }: TaskConditionsEditorProps) {
  const [currentCondition, setCurrentCondition] = useState<TaskCondition>({
    conditionType: 'FIELD_VALUE',
  });

  const conditionTypes = [
    { value: 'FIELD_VALUE', label: 'Field Value', description: 'Based on field value' },
    { value: 'ROLE_BASED', label: 'Role Based', description: 'Based on user role' },
    { value: 'DATE_BASED', label: 'Date Based', description: 'Based on date/time' },
    { value: 'ORDER_CRITERIA', label: 'Order Criteria', description: 'Based on order data' },
    { value: 'CUSTOM', label: 'Custom Expression', description: 'Custom logic expression' },
  ];

  const operators = [
    { value: 'EQUALS', label: 'Equals (=)' },
    { value: 'NOT_EQUALS', label: 'Not Equals (≠)' },
    { value: 'GREATER_THAN', label: 'Greater Than (>)' },
    { value: 'LESS_THAN', label: 'Less Than (<)' },
    { value: 'GREATER_OR_EQUAL', label: 'Greater or Equal (≥)' },
    { value: 'LESS_OR_EQUAL', label: 'Less or Equal (≤)' },
    { value: 'CONTAINS', label: 'Contains' },
    { value: 'NOT_CONTAINS', label: 'Does Not Contain' },
    { value: 'IN', label: 'In List' },
    { value: 'NOT_IN', label: 'Not In List' },
  ];

  const handleAddCondition = () => {
    if (!currentCondition.conditionType) {
      alert('Please select a condition type');
      return;
    }

    if (
      currentCondition.conditionType !== 'CUSTOM' &&
      (!currentCondition.conditionField || !currentCondition.conditionOperator)
    ) {
      alert('Please fill in all required fields');
      return;
    }

    onChange([...conditions, currentCondition]);
    setCurrentCondition({
      conditionType: 'FIELD_VALUE',
    });
  };

  const handleDeleteCondition = (index: number) => {
    onChange(conditions.filter((_, i) => i !== index));
  };

  return (
    <div className="space-y-4">
      <div className="border border-gray-200 rounded-lg p-4 bg-gray-50">
        <h4 className="text-sm font-semibold text-gray-700 mb-3">Add Task Condition</h4>

        <div className="grid grid-cols-1 gap-3">
          <Select
            label="Condition Type"
            value={currentCondition.conditionType}
            onChange={(e) =>
              setCurrentCondition({ ...currentCondition, conditionType: e.target.value })
            }
          >
            {conditionTypes.map((type) => (
              <option key={type.value} value={type.value}>
                {type.label} - {type.description}
              </option>
            ))}
          </Select>

          {currentCondition.conditionType !== 'CUSTOM' && (
            <>
              <Input
                label="Field Name"
                value={currentCondition.conditionField || ''}
                onChange={(e) =>
                  setCurrentCondition({ ...currentCondition, conditionField: e.target.value })
                }
                placeholder="e.g., orderAmount, customerType, deliveryDate"
                required
              />

              <Select
                label="Operator"
                value={currentCondition.conditionOperator || ''}
                onChange={(e) =>
                  setCurrentCondition({ ...currentCondition, conditionOperator: e.target.value })
                }
                required
              >
                <option value="">Select operator...</option>
                {operators.map((op) => (
                  <option key={op.value} value={op.value}>
                    {op.label}
                  </option>
                ))}
              </Select>

              <Input
                label="Value"
                value={currentCondition.conditionValue || ''}
                onChange={(e) =>
                  setCurrentCondition({ ...currentCondition, conditionValue: e.target.value })
                }
                placeholder="e.g., 10000, VIP, 2024-01-01"
              />
            </>
          )}

          {currentCondition.conditionType === 'CUSTOM' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Custom Expression (SpEL or JSON)
              </label>
              <textarea
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 font-mono text-sm"
                rows={3}
                value={currentCondition.conditionExpression || ''}
                onChange={(e) =>
                  setCurrentCondition({ ...currentCondition, conditionExpression: e.target.value })
                }
                placeholder='e.g., #order.amount > 10000 && #order.type == "VIP"'
              />
              <p className="text-xs text-gray-500 mt-1">
                Use SpEL (Spring Expression Language) or JSON logic format
              </p>
            </div>
          )}
        </div>

        <div className="mt-3 flex justify-end">
          <Button variant="primary" size="sm" onClick={handleAddCondition}>
            <Plus className="w-4 h-4 mr-1" />
            Add Condition
          </Button>
        </div>
      </div>

      {/* Conditions List */}
      {conditions.length > 0 && (
        <div className="space-y-2">
          <h4 className="text-sm font-semibold text-gray-700">Configured Conditions</h4>
          {conditions.map((condition, index) => {
            const condType = conditionTypes.find((t) => t.value === condition.conditionType);

            return (
              <div
                key={index}
                className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-lg"
              >
                <div className="flex-1">
                  <div className="font-medium text-gray-900">{condType?.label}</div>
                  <div className="text-sm text-gray-600 mt-1">
                    {condition.conditionType === 'CUSTOM' ? (
                      <code className="bg-gray-100 px-2 py-1 rounded text-xs">
                        {condition.conditionExpression}
                      </code>
                    ) : (
                      <>
                        <span className="font-medium">{condition.conditionField}</span>
                        {' '}{condition.conditionOperator}{' '}
                        <span className="font-medium">{condition.conditionValue}</span>
                      </>
                    )}
                  </div>
                </div>
                <button
                  onClick={() => handleDeleteCondition(index)}
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

      {conditions.length === 0 && (
        <div className="text-center py-8 text-gray-500 text-sm">
          No conditions configured. This task will always be available.
        </div>
      )}
    </div>
  );
}
