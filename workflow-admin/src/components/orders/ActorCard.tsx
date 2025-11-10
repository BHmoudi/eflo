'use client';

import React from 'react';
import { Mail, Phone, Edit2, Trash2 } from 'lucide-react';
import { cn } from '@/lib/utils';

export type ActorRole = 'Client' | 'Chef de Vente' | 'Financeur' | 'Secrétaire' | 'Vendeur';

interface Actor {
  id: string;
  role: ActorRole;
  name: string;
  phone?: string;
  email?: string;
}

interface ActorCardProps {
  actor: Actor;
  onEdit?: (actor: Actor) => void;
  onDelete?: (actorId: string) => void;
}

const roleColors: Record<ActorRole, { bg: string; text: string }> = {
  'Client': { bg: 'bg-blue-100', text: 'text-blue-700' },
  'Chef de Vente': { bg: 'bg-purple-100', text: 'text-purple-700' },
  'Financeur': { bg: 'bg-green-100', text: 'text-green-700' },
  'Secrétaire': { bg: 'bg-yellow-100', text: 'text-yellow-700' },
  'Vendeur': { bg: 'bg-pink-100', text: 'text-pink-700' },
};

export default function ActorCard({ actor, onEdit, onDelete }: ActorCardProps) {
  const colorScheme = roleColors[actor.role];

  return (
    <div className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow bg-white">
      <div className="flex items-start justify-between mb-3">
        <span
          className={cn(
            'inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold',
            colorScheme.bg,
            colorScheme.text
          )}
        >
          {actor.role}
        </span>
        <div className="flex items-center gap-2">
          {onEdit && (
            <button
              onClick={() => onEdit(actor)}
              className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
              title="Modifier"
            >
              <Edit2 className="w-4 h-4" />
            </button>
          )}
          {onDelete && (
            <button
              onClick={() => onDelete(actor.id)}
              className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded transition-colors"
              title="Supprimer"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      <div className="space-y-2">
        <h4 className="font-semibold text-gray-900 text-base">{actor.name}</h4>

        {actor.phone && (
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <Phone className="w-4 h-4 text-gray-400" />
            <span>{actor.phone}</span>
          </div>
        )}

        {actor.email && (
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <Mail className="w-4 h-4 text-gray-400" />
            <span className="truncate">{actor.email}</span>
          </div>
        )}
      </div>
    </div>
  );
}
