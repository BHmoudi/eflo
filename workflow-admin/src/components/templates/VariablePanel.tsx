'use client';

import React from 'react';
import { useDraggable } from '@dnd-kit/core';
import { Database, User, Car, DollarSign, Calendar, FileText, Building } from 'lucide-react';

interface Variable {
  name: string;
  label: string;
  type: string;
  category: string;
  example: string;
  description: string;
}

const AVAILABLE_VARIABLES: Variable[] = [
  // Order Variables
  { name: 'orderNumber', label: 'Order Number', type: 'string', category: 'Order', example: 'VN-2025-001', description: 'Unique order identifier' },
  { name: 'orderType', label: 'Order Type', type: 'string', category: 'Order', example: 'VN', description: 'VN, VO, or EVO' },
  { name: 'orderDate', label: 'Order Date', type: 'date', category: 'Order', example: '20/10/2025', description: 'Order creation date' },
  { name: 'orderStatus', label: 'Order Status', type: 'string', category: 'Order', example: 'CONFIRMED', description: 'Current order status' },

  // Customer Variables
  { name: 'customer.name', label: 'Customer Name', type: 'string', category: 'Customer', example: 'Jean Dupont', description: 'Full customer name' },
  { name: 'customer.email', label: 'Customer Email', type: 'string', category: 'Customer', example: 'jean.dupont@example.com', description: 'Customer email address' },
  { name: 'customer.phone', label: 'Customer Phone', type: 'string', category: 'Customer', example: '+33 6 12 34 56 78', description: 'Customer phone number' },
  { name: 'customer.address', label: 'Customer Address', type: 'string', category: 'Customer', example: '123 Rue de Paris, 75001 Paris', description: 'Full address' },
  { name: 'customer.postalCode', label: 'Postal Code', type: 'string', category: 'Customer', example: '75001', description: 'Postal code' },
  { name: 'customer.city', label: 'City', type: 'string', category: 'Customer', example: 'Paris', description: 'City name' },

  // Vehicle Variables
  { name: 'vehicle.brand', label: 'Vehicle Brand', type: 'string', category: 'Vehicle', example: 'Peugeot', description: 'Vehicle manufacturer' },
  { name: 'vehicle.model', label: 'Vehicle Model', type: 'string', category: 'Vehicle', example: '308', description: 'Vehicle model' },
  { name: 'vehicle.year', label: 'Vehicle Year', type: 'number', category: 'Vehicle', example: '2025', description: 'Manufacturing year' },
  { name: 'vehicle.vin', label: 'VIN', type: 'string', category: 'Vehicle', example: 'VF3XXXXXXXX123456', description: 'Vehicle Identification Number' },
  { name: 'vehicle.color', label: 'Color', type: 'string', category: 'Vehicle', example: 'Noir Perla Nera', description: 'Vehicle color' },
  { name: 'vehicle.mileage', label: 'Mileage', type: 'number', category: 'Vehicle', example: '15000', description: 'Kilometers' },

  // Pricing Variables
  { name: 'basePrice', label: 'Base Price', type: 'currency', category: 'Pricing', example: '25,000.00 €', description: 'Base vehicle price' },
  { name: 'discount', label: 'Discount', type: 'currency', category: 'Pricing', example: '2,000.00 €', description: 'Total discount' },
  { name: 'totalPrice', label: 'Total HT', type: 'currency', category: 'Pricing', example: '23,000.00 €', description: 'Total without tax' },
  { name: 'vat', label: 'VAT', type: 'currency', category: 'Pricing', example: '4,600.00 €', description: 'Value Added Tax' },
  { name: 'totalTTC', label: 'Total TTC', type: 'currency', category: 'Pricing', example: '27,600.00 €', description: 'Total with tax' },

  // Seller/Employee Variables
  { name: 'seller.name', label: 'Seller Name', type: 'string', category: 'Seller', example: 'Marie Martin', description: 'Sales person name' },
  { name: 'seller.email', label: 'Seller Email', type: 'string', category: 'Seller', example: 'marie.martin@eflo.com', description: 'Seller email' },
  { name: 'seller.phone', label: 'Seller Phone', type: 'string', category: 'Seller', example: '+33 1 23 45 67 89', description: 'Seller phone' },

  // Company Variables
  { name: 'company.name', label: 'Company Name', type: 'string', category: 'Company', example: 'Eflo Automobiles', description: 'Company legal name' },
  { name: 'company.address', label: 'Company Address', type: 'string', category: 'Company', example: '456 Avenue des Champs-Élysées', description: 'Company address' },
  { name: 'company.siret', label: 'SIRET', type: 'string', category: 'Company', example: '123 456 789 00010', description: 'Company SIRET number' },
  { name: 'company.phone', label: 'Company Phone', type: 'string', category: 'Company', example: '+33 1 99 88 77 66', description: 'Company phone' },
];

const CATEGORY_ICONS: Record<string, any> = {
  'Order': Database,
  'Customer': User,
  'Vehicle': Car,
  'Pricing': DollarSign,
  'Seller': User,
  'Company': Building,
};

function DraggableVariable({ variable }: { variable: Variable }) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: variable.name,
    data: variable,
  });

  const style = transform ? {
    transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
    opacity: isDragging ? 0.5 : 1,
  } : undefined;

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      className="group cursor-move bg-white hover:bg-blue-50 border border-gray-200 hover:border-blue-400 rounded-lg p-3 transition-all"
      title={variable.description}
    >
      <div className="flex items-start gap-2">
        <div className="mt-0.5 text-gray-400 group-hover:text-blue-600">
          <FileText className="w-4 h-4" />
        </div>
        <div className="flex-1 min-w-0">
          <div className="font-mono text-xs text-blue-600 font-semibold">
            {`{{${variable.name}}}`}
          </div>
          <div className="text-xs text-gray-600 mt-0.5">
            {variable.label}
          </div>
          <div className="text-xs text-gray-400 mt-1 truncate">
            Ex: {variable.example}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function VariablePanel() {
  const categories = Array.from(new Set(AVAILABLE_VARIABLES.map(v => v.category)));

  return (
    <div className="w-80 bg-gray-50 border-r border-gray-200 overflow-y-auto">
      <div className="p-4 border-b border-gray-200 bg-white sticky top-0 z-10">
        <h3 className="font-semibold text-gray-900 flex items-center gap-2">
          <Database className="w-5 h-5 text-blue-600" />
          Available Variables
        </h3>
        <p className="text-xs text-gray-500 mt-1">
          Drag variables into your template
        </p>
      </div>

      <div className="p-4 space-y-6">
        {categories.map(category => {
          const Icon = CATEGORY_ICONS[category] || FileText;
          const categoryVars = AVAILABLE_VARIABLES.filter(v => v.category === category);

          return (
            <div key={category}>
              <h4 className="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <Icon className="w-4 h-4 text-gray-500" />
                {category}
              </h4>
              <div className="space-y-2">
                {categoryVars.map(variable => (
                  <DraggableVariable key={variable.name} variable={variable} />
                ))}
              </div>
            </div>
          );
        })}
      </div>

      <div className="p-4 border-t border-gray-200 bg-white sticky bottom-0">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <p className="text-xs font-semibold text-blue-900 mb-1">Pro Tip</p>
          <p className="text-xs text-blue-700">
            You can also type variables manually using the format: <code className="bg-blue-100 px-1 rounded">{'{{variableName}}'}</code>
          </p>
          <p className="text-xs text-blue-700 mt-2">
            Add formatting: <code className="bg-blue-100 px-1 rounded">{'{{price:currency}}'}</code>
          </p>
        </div>
      </div>
    </div>
  );
}

export { AVAILABLE_VARIABLES };
