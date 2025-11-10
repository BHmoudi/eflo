'use client';

import React, { memo } from 'react';
import { Handle, Position, NodeProps } from 'reactflow';
import { cn } from '@/lib/utils';
import { Circle, CheckCircle, FileText } from 'lucide-react';

export interface StateNodeData {
  label: string;
  state: {
    code: string;
    name: string;
    description?: string;
    isFinal: boolean;
    isInitial: boolean;
    tasks: any[];
  };
}

function StateNode({ data, selected }: NodeProps<StateNodeData>) {
  const { state } = data;
  const taskCount = state.tasks?.length || 0;

  return (
    <div
      className={cn(
        'px-4 py-3 rounded-lg border-2 bg-white min-w-[220px] max-w-[300px] transition-all',
        selected ? 'border-blue-500 shadow-lg' : 'border-gray-300 shadow',
        state.isInitial && 'border-green-500',
        state.isFinal && 'border-purple-500'
      )}
    >
      <Handle type="target" position={Position.Top} className="w-3 h-3 !bg-blue-500" />

      <div className="flex items-center gap-2 mb-2">
        {state.isInitial && <Circle className="w-4 h-4 text-green-500" />}
        {state.isFinal && <CheckCircle className="w-4 h-4 text-purple-500" />}
        {!state.isInitial && !state.isFinal && <FileText className="w-4 h-4 text-gray-400" />}
        <div className="flex-1">
          <div className="font-semibold text-sm text-gray-900">{state.name}</div>
          <div className="text-xs text-gray-500 font-mono">{state.code}</div>
        </div>
      </div>

      {state.description && (
        <div className="text-xs text-gray-500 mb-2 line-clamp-2">{state.description}</div>
      )}

      {/* Task List */}
      {taskCount > 0 && (
        <div className="mt-2 border-t border-gray-200 pt-2">
          <div className="text-xs font-semibold text-gray-700 mb-1 flex items-center gap-1">
            <FileText className="w-3 h-3" />
            <span>Tasks ({taskCount})</span>
          </div>
          <div className="space-y-1">
            {state.tasks.slice(0, 5).map((task: any, idx: number) => (
              <div key={task.code || idx} className="text-xs bg-gray-50 px-2 py-1 rounded">
                <div className="flex items-center gap-1">
                  <span className="font-medium text-gray-700 truncate">{task.name}</span>
                </div>
                <div className="text-gray-500 text-[10px] font-mono">{task.taskType || task.type}</div>
              </div>
            ))}
            {taskCount > 5 && (
              <div className="text-xs text-gray-400">+{taskCount - 5} more...</div>
            )}
          </div>
        </div>
      )}

      <Handle type="source" position={Position.Bottom} className="w-3 h-3 !bg-blue-500" />
    </div>
  );
}

export default memo(StateNode);
