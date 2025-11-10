'use client';

import React from 'react';
import { EdgeProps, getBezierPath, EdgeLabelRenderer, BaseEdge } from 'reactflow';
import { RotateCcw } from 'lucide-react';

export default function RollbackEdge({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
  label,
}: EdgeProps) {
  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
  });

  return (
    <>
      <BaseEdge
        id={id}
        path={edgePath}
        markerEnd={markerEnd}
        style={{
          ...style,
          stroke: '#ef4444',
          strokeWidth: 2,
          strokeDasharray: '10,5',
        }}
      />
      <EdgeLabelRenderer>
        <div
          style={{
            position: 'absolute',
            transform: `translate(-50%, -50%) translate(${labelX}px,${labelY}px)`,
            pointerEvents: 'all',
          }}
          className="nodrag nopan bg-red-100 px-2 py-1 rounded-md text-xs font-semibold text-red-800 flex items-center gap-1 border border-red-300"
        >
          <RotateCcw className="w-3 h-3" />
          {label || 'Rollback'}
        </div>
      </EdgeLabelRenderer>
    </>
  );
}
