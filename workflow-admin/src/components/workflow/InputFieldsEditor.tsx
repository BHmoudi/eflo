'use client';

import React, { useState } from 'react';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Select from '@/components/ui/Select';
import { Plus, Trash2, Edit2 } from 'lucide-react';

export interface InputField {
  name: string;
  type: string;
  label: string;
  required?: boolean;
  placeholder?: string;
  defaultValue?: any;
  validation?: {
    min?: number;
    max?: number;
    minLength?: number;
    maxLength?: number;
    pattern?: string;
    message?: string;
  };
  options?: string[];
}

interface InputFieldsEditorProps {
  fields: InputField[];
  onChange: (fields: InputField[]) => void;
}

export default function InputFieldsEditor({ fields, onChange }: InputFieldsEditorProps) {
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [currentField, setCurrentField] = useState<InputField>({
    name: '',
    type: 'text',
    label: '',
    required: false,
  });

  const fieldTypes = [
    { value: 'text', label: 'Text Input' },
    { value: 'number', label: 'Number' },
    { value: 'date', label: 'Date' },
    { value: 'datetime', label: 'Date & Time' },
    { value: 'select', label: 'Dropdown (Select)' },
    { value: 'textarea', label: 'Text Area' },
    { value: 'checkbox', label: 'Checkbox' },
    { value: 'file', label: 'File Upload' },
  ];

  const handleAddField = () => {
    if (!currentField.name || !currentField.label) {
      alert('Field name and label are required');
      return;
    }

    if (editingIndex !== null) {
      // Update existing field
      const updatedFields = [...fields];
      updatedFields[editingIndex] = currentField;
      onChange(updatedFields);
      setEditingIndex(null);
    } else {
      // Add new field
      onChange([...fields, currentField]);
    }

    // Reset form
    setCurrentField({
      name: '',
      type: 'text',
      label: '',
      required: false,
    });
  };

  const handleEditField = (index: number) => {
    setCurrentField(fields[index]);
    setEditingIndex(index);
  };

  const handleDeleteField = (index: number) => {
    onChange(fields.filter((_, i) => i !== index));
  };

  const handleCancel = () => {
    setEditingIndex(null);
    setCurrentField({
      name: '',
      type: 'text',
      label: '',
      required: false,
    });
  };

  return (
    <div className="space-y-4">
      <div className="border border-gray-200 rounded-lg p-4 bg-gray-50">
        <h4 className="text-sm font-semibold text-gray-700 mb-3">
          {editingIndex !== null ? 'Edit Input Field' : 'Add Input Field'}
        </h4>

        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Field Name (Code)"
            value={currentField.name}
            onChange={(e) => setCurrentField({ ...currentField, name: e.target.value })}
            placeholder="e.g., deliveryAddress"
            required
          />

          <Input
            label="Field Label"
            value={currentField.label}
            onChange={(e) => setCurrentField({ ...currentField, label: e.target.value })}
            placeholder="e.g., Delivery Address"
            required
          />

          <Select
            label="Field Type"
            value={currentField.type}
            onChange={(e) => setCurrentField({ ...currentField, type: e.target.value })}
          >
            {fieldTypes.map((type) => (
              <option key={type.value} value={type.value}>
                {type.label}
              </option>
            ))}
          </Select>

          <Input
            label="Placeholder"
            value={currentField.placeholder || ''}
            onChange={(e) => setCurrentField({ ...currentField, placeholder: e.target.value })}
            placeholder="Optional placeholder text"
          />

          {currentField.type === 'number' && (
            <>
              <Input
                label="Min Value"
                type="number"
                value={currentField.validation?.min || ''}
                onChange={(e) =>
                  setCurrentField({
                    ...currentField,
                    validation: {
                      ...currentField.validation,
                      min: parseInt(e.target.value) || undefined,
                    },
                  })
                }
              />
              <Input
                label="Max Value"
                type="number"
                value={currentField.validation?.max || ''}
                onChange={(e) =>
                  setCurrentField({
                    ...currentField,
                    validation: {
                      ...currentField.validation,
                      max: parseInt(e.target.value) || undefined,
                    },
                  })
                }
              />
            </>
          )}

          {currentField.type === 'text' && (
            <>
              <Input
                label="Max Length"
                type="number"
                value={currentField.validation?.maxLength || ''}
                onChange={(e) =>
                  setCurrentField({
                    ...currentField,
                    validation: {
                      ...currentField.validation,
                      maxLength: parseInt(e.target.value) || undefined,
                    },
                  })
                }
              />
              <Input
                label="Pattern (Regex)"
                value={currentField.validation?.pattern || ''}
                onChange={(e) =>
                  setCurrentField({
                    ...currentField,
                    validation: {
                      ...currentField.validation,
                      pattern: e.target.value || undefined,
                    },
                  })
                }
                placeholder="e.g., ^[0-9]{5}$"
              />
            </>
          )}

          {currentField.type === 'select' && (
            <div className="col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Options (comma-separated)
              </label>
              <textarea
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                rows={2}
                value={currentField.options?.join(', ') || ''}
                onChange={(e) =>
                  setCurrentField({
                    ...currentField,
                    options: e.target.value.split(',').map((o) => o.trim()).filter(Boolean),
                  })
                }
                placeholder="Option 1, Option 2, Option 3"
              />
            </div>
          )}
        </div>

        <div className="mt-3 flex items-center gap-4">
          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              checked={currentField.required || false}
              onChange={(e) => setCurrentField({ ...currentField, required: e.target.checked })}
              className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span className="text-sm text-gray-700">Required Field</span>
          </label>

          <div className="ml-auto flex gap-2">
            {editingIndex !== null && (
              <Button variant="outline" size="sm" onClick={handleCancel}>
                Cancel
              </Button>
            )}
            <Button variant="primary" size="sm" onClick={handleAddField}>
              <Plus className="w-4 h-4 mr-1" />
              {editingIndex !== null ? 'Update Field' : 'Add Field'}
            </Button>
          </div>
        </div>
      </div>

      {/* Fields List */}
      {fields.length > 0 && (
        <div className="space-y-2">
          <h4 className="text-sm font-semibold text-gray-700">Configured Fields</h4>
          {fields.map((field, index) => (
            <div
              key={index}
              className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-lg"
            >
              <div className="flex-1">
                <div className="flex items-center gap-2">
                  <span className="font-medium text-gray-900">{field.label}</span>
                  <span className="text-xs text-gray-500">({field.name})</span>
                  {field.required && (
                    <span className="text-xs px-2 py-0.5 bg-red-100 text-red-700 rounded">
                      Required
                    </span>
                  )}
                </div>
                <div className="text-sm text-gray-600 mt-1">
                  Type: <span className="font-medium">{field.type}</span>
                  {field.type === 'select' && field.options && (
                    <span className="ml-2">
                      Options: {field.options.slice(0, 3).join(', ')}
                      {field.options.length > 3 && '...'}
                    </span>
                  )}
                </div>
              </div>
              <div className="flex items-center gap-2">
                <button
                  onClick={() => handleEditField(index)}
                  className="p-2 text-blue-600 hover:bg-blue-50 rounded transition-colors"
                  title="Edit"
                >
                  <Edit2 className="w-4 h-4" />
                </button>
                <button
                  onClick={() => handleDeleteField(index)}
                  className="p-2 text-red-600 hover:bg-red-50 rounded transition-colors"
                  title="Delete"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
