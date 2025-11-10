'use client';

import React from 'react';
import { cn } from '@/lib/utils';
import { OrderStatus } from '@/types/order';
import {
  FileText,
  CheckCircle2,
  Cog,
  Package,
  Truck,
  Archive,
  Check,
} from 'lucide-react';

interface WorkflowStage {
  id: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  statuses: OrderStatus[];
}

const WORKFLOW_STAGES: WorkflowStage[] = [
  {
    id: 'commande',
    label: 'Commande',
    icon: FileText,
    statuses: ['DRAFT', 'PENDING'],
  },
  {
    id: 'validation',
    label: 'Validation',
    icon: CheckCircle2,
    statuses: ['CONFIRMED'],
  },
  {
    id: 'traitement',
    label: 'Traitement de la commande',
    icon: Cog,
    statuses: ['IN_PRODUCTION'],
  },
  {
    id: 'preparation',
    label: 'Préparation',
    icon: Package,
    statuses: ['READY_FOR_DELIVERY'],
  },
  {
    id: 'livraison',
    label: 'Livraison',
    icon: Truck,
    statuses: ['DELIVERED'],
  },
  {
    id: 'archivee',
    label: 'Archivée',
    icon: Archive,
    statuses: ['CANCELLED', 'ON_HOLD'],
  },
];

interface OrderWorkflowStepperProps {
  currentStage?: string;
  orderStatus: OrderStatus;
  className?: string;
}

const OrderWorkflowStepper: React.FC<OrderWorkflowStepperProps> = ({
  currentStage,
  orderStatus,
  className,
}) => {
  // Determine the current stage index based on order status
  const getCurrentStageIndex = (): number => {
    if (currentStage) {
      const index = WORKFLOW_STAGES.findIndex((stage) => stage.id === currentStage);
      if (index !== -1) return index;
    }

    // Map order status to stage
    const stageIndex = WORKFLOW_STAGES.findIndex((stage) =>
      stage.statuses.includes(orderStatus)
    );
    return stageIndex !== -1 ? stageIndex : 0;
  };

  const currentStageIndex = getCurrentStageIndex();

  const getStageStatus = (index: number): 'completed' | 'current' | 'upcoming' => {
    if (index < currentStageIndex) return 'completed';
    if (index === currentStageIndex) return 'current';
    return 'upcoming';
  };

  return (
    <div className={cn('w-full', className)}>
      {/* Desktop horizontal layout */}
      <div className="hidden md:block">
        <div className="flex items-center justify-between">
          {WORKFLOW_STAGES.map((stage, index) => {
            const status = getStageStatus(index);
            const Icon = stage.icon;
            const isLast = index === WORKFLOW_STAGES.length - 1;

            return (
              <React.Fragment key={stage.id}>
                <div className="flex flex-col items-center flex-1">
                  {/* Icon circle */}
                  <div
                    className={cn(
                      'flex items-center justify-center w-12 h-12 rounded-full border-2 transition-all duration-300',
                      {
                        'bg-yellow-400 border-yellow-500 text-white shadow-md':
                          status === 'current',
                        'bg-green-500 border-green-600 text-white':
                          status === 'completed',
                        'bg-gray-100 border-gray-300 text-gray-400':
                          status === 'upcoming',
                      }
                    )}
                  >
                    {status === 'completed' ? (
                      <Check className="w-6 h-6" />
                    ) : (
                      <Icon className="w-6 h-6" />
                    )}
                  </div>

                  {/* Label */}
                  <div
                    className={cn(
                      'mt-2 text-sm font-medium text-center max-w-[120px] transition-colors duration-300',
                      {
                        'text-yellow-600': status === 'current',
                        'text-gray-900': status === 'completed',
                        'text-gray-400': status === 'upcoming',
                      }
                    )}
                  >
                    {stage.label}
                  </div>
                </div>

                {/* Connector line */}
                {!isLast && (
                  <div className="flex-1 h-0.5 mx-2 mb-8">
                    <div
                      className={cn(
                        'h-full transition-all duration-300',
                        {
                          'bg-green-500': index < currentStageIndex,
                          'bg-gray-300': index >= currentStageIndex,
                        }
                      )}
                    />
                  </div>
                )}
              </React.Fragment>
            );
          })}
        </div>
      </div>

      {/* Mobile vertical layout */}
      <div className="md:hidden space-y-4">
        {WORKFLOW_STAGES.map((stage, index) => {
          const status = getStageStatus(index);
          const Icon = stage.icon;
          const isLast = index === WORKFLOW_STAGES.length - 1;

          return (
            <div key={stage.id} className="relative">
              <div className="flex items-start">
                {/* Icon circle */}
                <div
                  className={cn(
                    'flex items-center justify-center w-10 h-10 rounded-full border-2 transition-all duration-300 flex-shrink-0',
                    {
                      'bg-yellow-400 border-yellow-500 text-white shadow-md':
                        status === 'current',
                      'bg-green-500 border-green-600 text-white':
                        status === 'completed',
                      'bg-gray-100 border-gray-300 text-gray-400':
                        status === 'upcoming',
                    }
                  )}
                >
                  {status === 'completed' ? (
                    <Check className="w-5 h-5" />
                  ) : (
                    <Icon className="w-5 h-5" />
                  )}
                </div>

                {/* Label */}
                <div
                  className={cn(
                    'ml-4 text-base font-medium transition-colors duration-300',
                    {
                      'text-yellow-600': status === 'current',
                      'text-gray-900': status === 'completed',
                      'text-gray-400': status === 'upcoming',
                    }
                  )}
                >
                  {stage.label}
                </div>
              </div>

              {/* Connector line */}
              {!isLast && (
                <div className="absolute left-5 top-10 bottom-0 w-0.5 h-4 -mb-4">
                  <div
                    className={cn(
                      'w-full h-full transition-all duration-300',
                      {
                        'bg-green-500': index < currentStageIndex,
                        'bg-gray-300': index >= currentStageIndex,
                      }
                    )}
                  />
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default OrderWorkflowStepper;
