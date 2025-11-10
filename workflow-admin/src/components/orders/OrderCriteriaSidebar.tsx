'use client';

import React, { useState } from 'react';
import { OrderAssignedCondition } from '@/lib/api/orders';
import { cn } from '@/lib/utils';
import { CheckCircle2, Circle, AlertCircle } from 'lucide-react';

interface OrderCriteriaSidebarProps {
  orderId: number;
  assignedConditions: OrderAssignedCondition[];
  editable?: boolean;
}

// Standard criteria that should always be displayed
const STANDARD_CRITERIA = [
  { code: 'FINANCEMENT', label: 'FINANCEMENT' },
  { code: 'PARTICULIER', label: 'PARTICULIER' },
  { code: 'REPRISE', label: 'REPRISE' },
  { code: 'CONNECTE', label: 'CONNECTE' },
  { code: 'PRIME_CONVERSION', label: 'PRIME CONVERSION' },
  { code: 'CARBURANT', label: 'CARBURANT' },
  { code: 'EXTENSION_DE_GTIE', label: 'EXTENSION_DE_GTIE' },
  { code: 'TRANSFORMATION', label: 'TRANSFORMATION' },
  { code: 'SERVICES', label: 'SERVICES' },
  { code: 'ENTRETIEN', label: 'ENTRETIEN' },
  { code: 'E/R', label: 'E/R' },
  { code: 'ACCESSOIRES', label: 'ACCESSOIRES' },
  { code: 'MOBILIZE', label: 'Mobilize' },
];

export default function OrderCriteriaSidebar({
  orderId,
  assignedConditions,
  editable = false,
}: OrderCriteriaSidebarProps) {
  const [showUnrecognized, setShowUnrecognized] = useState(false);

  // Create a map of assigned condition codes for quick lookup
  const assignedCodesMap = new Map(
    assignedConditions.map((condition) => [condition.conditionCode, condition])
  );

  // Separate standard and unrecognized conditions
  const standardCriteria = STANDARD_CRITERIA.map((criterion) => ({
    ...criterion,
    isAssigned: assignedCodesMap.has(criterion.code),
    condition: assignedCodesMap.get(criterion.code),
  }));

  const unrecognizedConditions = assignedConditions.filter(
    (condition) =>
      !STANDARD_CRITERIA.some((criterion) => criterion.code === condition.conditionCode)
  );

  const hasUnrecognized = unrecognizedConditions.length > 0;

  return (
    <aside className="w-80 bg-white border-l border-gray-200 flex flex-col sticky top-0 h-screen">
      {/* Header */}
      <div className="p-6 border-b border-gray-200">
        <h2 className="text-lg font-semibold text-gray-900">Critères du dossier</h2>
        <p className="text-xs text-gray-500 mt-1">
          {assignedConditions.length} condition{assignedConditions.length !== 1 ? 's' : ''} assignée
          {assignedConditions.length !== 1 ? 's' : ''}
        </p>
      </div>

      {/* Criteria List */}
      <div className="flex-1 overflow-y-auto p-6">
        <div className="space-y-2">
          {standardCriteria.map((criterion) => (
            <label
              key={criterion.code}
              className={cn(
                'flex items-center gap-3 p-3 rounded-lg transition-colors cursor-pointer',
                criterion.isAssigned
                  ? 'bg-blue-50 hover:bg-blue-100'
                  : 'hover:bg-gray-50',
                !editable && 'cursor-default'
              )}
            >
              <input
                type="checkbox"
                checked={criterion.isAssigned}
                disabled={!editable}
                readOnly={!editable}
                className={cn(
                  'w-5 h-5 rounded border-2 transition-colors',
                  criterion.isAssigned
                    ? 'border-blue-600 bg-blue-600 text-white'
                    : 'border-gray-300 bg-white',
                  editable
                    ? 'cursor-pointer focus:ring-2 focus:ring-blue-500 focus:ring-offset-2'
                    : 'cursor-default pointer-events-none'
                )}
                style={{
                  appearance: 'none',
                  WebkitAppearance: 'none',
                  MozAppearance: 'none',
                  backgroundImage: criterion.isAssigned
                    ? 'url("data:image/svg+xml,%3csvg viewBox=\'0 0 16 16\' fill=\'white\' xmlns=\'http://www.w3.org/2000/svg\'%3e%3cpath d=\'M12.207 4.793a1 1 0 010 1.414l-5 5a1 1 0 01-1.414 0l-2-2a1 1 0 011.414-1.414L6.5 9.086l4.293-4.293a1 1 0 011.414 0z\'/%3e%3c/svg%3e")'
                    : 'none',
                  backgroundSize: '100% 100%',
                  backgroundPosition: '50%',
                  backgroundRepeat: 'no-repeat',
                }}
              />
              <span
                className={cn(
                  'text-sm font-medium select-none',
                  criterion.isAssigned ? 'text-blue-900' : 'text-gray-700'
                )}
              >
                {criterion.label}
              </span>
              {criterion.condition?.isManual && (
                <span className="ml-auto text-xs text-blue-600 bg-blue-100 px-2 py-0.5 rounded">
                  Manuel
                </span>
              )}
            </label>
          ))}
        </div>

        {/* Unrecognized Conditions Section */}
        {hasUnrecognized && (
          <div className="mt-6 pt-6 border-t border-gray-200">
            <button
              onClick={() => setShowUnrecognized(!showUnrecognized)}
              className="flex items-center gap-2 text-sm font-medium text-amber-700 hover:text-amber-800 transition-colors"
            >
              <AlertCircle className="w-4 h-4" />
              <span>
                {unrecognizedConditions.length} condition{unrecognizedConditions.length !== 1 ? 's' : ''}{' '}
                non reconnue{unrecognizedConditions.length !== 1 ? 's' : ''}
              </span>
            </button>

            {showUnrecognized && (
              <div className="mt-3 space-y-2">
                {unrecognizedConditions.map((condition) => (
                  <div
                    key={condition.id}
                    className="p-3 rounded-lg bg-amber-50 border border-amber-200"
                  >
                    <div className="flex items-start gap-2">
                      <div className="flex-shrink-0 mt-0.5">
                        <CheckCircle2 className="w-4 h-4 text-amber-600" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-amber-900 break-words">
                          {condition.conditionLabel}
                        </p>
                        <p className="text-xs text-amber-700 mt-1">
                          Code: {condition.conditionCode}
                        </p>
                        {condition.isManual && (
                          <span className="inline-block text-xs text-amber-600 bg-amber-100 px-2 py-0.5 rounded mt-2">
                            Assignée manuellement
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* Footer Info */}
      <div className="p-4 border-t border-gray-200 bg-gray-50">
        <div className="flex items-center gap-2 text-xs text-gray-600">
          <Circle className="w-3 h-3" />
          <span>Non assigné</span>
        </div>
        <div className="flex items-center gap-2 text-xs text-blue-600 mt-2">
          <CheckCircle2 className="w-3 h-3" />
          <span>Assigné</span>
        </div>
        {!editable && (
          <p className="text-xs text-gray-500 mt-3">
            Vue lecture seule. Les critères sont attribués automatiquement selon les règles définies.
          </p>
        )}
      </div>
    </aside>
  );
}
