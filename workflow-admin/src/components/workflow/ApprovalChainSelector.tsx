'use client';

import React from 'react';
import Select from '@/components/ui/Select';
import { useQuery } from '@tanstack/react-query';

interface ApprovalChain {
  id: number;
  chainCode: string;
  chainName: string;
  description?: string;
  levels?: Array<{
    levelOrder: number;
    levelName: string;
    requiredRoleName?: string;
    approvalType: string;
  }>;
}

interface ApprovalChainSelectorProps {
  value?: number;
  onChange: (chainId: number | undefined) => void;
  label?: string;
  required?: boolean;
}

export default function ApprovalChainSelector({
  value,
  onChange,
  label = 'Approval Chain',
  required = false,
}: ApprovalChainSelectorProps) {
  // Fetch approval chains from API
  const { data: approvalChains = [], isLoading } = useQuery<ApprovalChain[]>({
    queryKey: ['approval-chains'],
    queryFn: async () => {
      const response = await fetch('/api/approval-chains');
      if (!response.ok) throw new Error('Failed to fetch approval chains');
      return response.json();
    },
  });

  const selectedChain = approvalChains.find((chain) => chain.id === value);

  return (
    <div className="space-y-2">
      <Select
        label={label}
        value={value?.toString() || ''}
        onChange={(e) => onChange(e.target.value ? parseInt(e.target.value) : undefined)}
        required={required}
        disabled={isLoading}
      >
        <option value="">No approval required</option>
        {approvalChains.map((chain) => (
          <option key={chain.id} value={chain.id}>
            {chain.chainName} ({chain.chainCode})
          </option>
        ))}
      </Select>

      {selectedChain && (
        <div className="mt-2 p-3 bg-blue-50 border border-blue-200 rounded-lg">
          <h5 className="text-sm font-semibold text-blue-900 mb-2">
            Approval Chain Details
          </h5>
          {selectedChain.description && (
            <p className="text-sm text-blue-700 mb-2">{selectedChain.description}</p>
          )}
          {selectedChain.levels && selectedChain.levels.length > 0 && (
            <div className="space-y-1">
              <p className="text-xs font-medium text-blue-800">Approval Levels:</p>
              {selectedChain.levels.map((level, index) => (
                <div key={index} className="text-xs text-blue-700 pl-2">
                  {level.levelOrder}. {level.levelName}
                  {level.requiredRoleName && (
                    <span className="ml-2 text-blue-600">
                      (Role: {level.requiredRoleName})
                    </span>
                  )}
                  <span className="ml-2 text-blue-500">
                    [{level.approvalType}]
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {!required && !value && (
        <p className="text-xs text-gray-500">
          No approval chain selected. Task will not require approval.
        </p>
      )}
    </div>
  );
}
