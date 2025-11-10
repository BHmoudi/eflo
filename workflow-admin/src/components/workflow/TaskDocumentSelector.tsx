'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { documentTypeApi } from '@/lib/api/documents';
import { FileText, Check } from 'lucide-react';

interface TaskDocumentSelectorProps {
  selectedDocuments: string[];
  onChange: (documents: string[]) => void;
  disabled?: boolean;
}

export default function TaskDocumentSelector({
  selectedDocuments = [],
  onChange,
  disabled = false
}: TaskDocumentSelectorProps) {

  const { data: documentTypes = [], isLoading } = useQuery({
    queryKey: ['document-types'],
    queryFn: () => documentTypeApi.listAllTypes(true),
  });

  const handleToggle = (typeCode: string) => {
    if (disabled) return;

    if (selectedDocuments.includes(typeCode)) {
      onChange(selectedDocuments.filter(d => d !== typeCode));
    } else {
      onChange([...selectedDocuments, typeCode]);
    }
  };

  if (isLoading) {
    return (
      <div className="text-sm text-gray-500 py-2">
        Loading document types...
      </div>
    );
  }

  if (!documentTypes || documentTypes.length === 0) {
    return (
      <div className="text-sm text-gray-500 py-2">
        No document types available. Please create document types first.
      </div>
    );
  }

  return (
    <div className="space-y-2">
      <label className="block text-sm font-medium text-gray-700 mb-2">
        Required Documents
      </label>

      <div className="max-h-60 overflow-y-auto border border-gray-200 rounded-lg p-2 space-y-1">
        {documentTypes.map((docType) => {
          const isSelected = selectedDocuments.includes(docType.typeCode);

          return (
            <label
              key={docType.typeCode}
              className={`
                flex items-center gap-3 p-2 rounded-md cursor-pointer transition-colors
                ${disabled ? 'opacity-50 cursor-not-allowed' : 'hover:bg-gray-50'}
                ${isSelected ? 'bg-blue-50 border border-blue-200' : 'border border-transparent'}
              `}
            >
              <input
                type="checkbox"
                checked={isSelected}
                onChange={() => handleToggle(docType.typeCode)}
                disabled={disabled}
                className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
              />

              <FileText className={`w-4 h-4 ${isSelected ? 'text-blue-600' : 'text-gray-400'}`} />

              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <span className={`text-sm font-medium ${isSelected ? 'text-blue-900' : 'text-gray-900'}`}>
                    {docType.typeName}
                  </span>
                  <span className="text-xs font-mono text-gray-500">
                    {docType.typeCode}
                  </span>
                </div>
                {docType.description && (
                  <p className="text-xs text-gray-500 truncate">
                    {docType.description}
                  </p>
                )}
                <div className="flex gap-2 mt-1">
                  {docType.isMandatory && (
                    <span className="text-xs bg-red-100 text-red-700 px-1.5 py-0.5 rounded">
                      Mandatory
                    </span>
                  )}
                  {docType.requiresValidation && (
                    <span className="text-xs bg-orange-100 text-orange-700 px-1.5 py-0.5 rounded">
                      Validation Required
                    </span>
                  )}
                  <span className="text-xs bg-gray-100 text-gray-700 px-1.5 py-0.5 rounded">
                    {docType.category}
                  </span>
                </div>
              </div>

              {isSelected && (
                <Check className="w-5 h-5 text-blue-600 flex-shrink-0" />
              )}
            </label>
          );
        })}
      </div>

      {selectedDocuments.length > 0 && (
        <div className="text-xs text-gray-600 mt-2">
          {selectedDocuments.length} document{selectedDocuments.length !== 1 ? 's' : ''} selected
        </div>
      )}
    </div>
  );
}
