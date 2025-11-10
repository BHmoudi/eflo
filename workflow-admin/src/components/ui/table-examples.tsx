/**
 * Table Component Usage Examples
 *
 * This file demonstrates how to use the responsive table enhancement components:
 * - ResponsiveTable: Basic responsive table with auto-breakpoint detection
 * - DataTable: Advanced table with search, filtering, pagination, and selection
 * - TableSkeleton: Loading skeleton for tables
 */

import React from 'react';
import ResponsiveTable, { ColumnConfig } from './ResponsiveTable';
import DataTable, { DataTableColumn, BulkAction } from './DataTable';
import TableSkeleton from './TableSkeleton';
import Badge from './Badge';

// =============================================================================
// Example 1: Basic ResponsiveTable
// =============================================================================

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  status: 'active' | 'inactive';
}

const users: User[] = [
  { id: 1, name: 'John Doe', email: 'john@example.com', role: 'Admin', status: 'active' },
  { id: 2, name: 'Jane Smith', email: 'jane@example.com', role: 'User', status: 'active' },
  { id: 3, name: 'Bob Johnson', email: 'bob@example.com', role: 'User', status: 'inactive' },
];

export function BasicResponsiveTableExample() {
  const columns: ColumnConfig[] = [
    {
      key: 'name',
      label: 'Name',
      priority: 1, // Always visible
      sortable: true,
    },
    {
      key: 'email',
      label: 'Email',
      priority: 2, // Hide on small screens
      sortable: true,
      mobileLabel: 'Email Address',
    },
    {
      key: 'role',
      label: 'Role',
      priority: 1, // Always visible
    },
    {
      key: 'status',
      label: 'Status',
      priority: 3, // Hide on tablets
      render: (value) => (
        <Badge variant={value === 'active' ? 'success' : 'default'}>
          {value}
        </Badge>
      ),
    },
  ];

  return (
    <ResponsiveTable
      columns={columns}
      data={users}
      keyExtractor={(row) => row.id}
      stickyHeader
      emptyMessage="No users found"
    />
  );
}

// =============================================================================
// Example 2: ResponsiveTable with Sorting and Filtering
// =============================================================================

export function ResponsiveTableWithSortingExample() {
  const [sortedData, setSortedData] = React.useState(users);

  const handleSort = (key: string, direction: 'asc' | 'desc') => {
    const sorted = [...sortedData].sort((a, b) => {
      const aValue = a[key as keyof User];
      const bValue = b[key as keyof User];
      if (aValue < bValue) return direction === 'asc' ? -1 : 1;
      if (aValue > bValue) return direction === 'asc' ? 1 : -1;
      return 0;
    });
    setSortedData(sorted);
  };

  const handleFilter = (filters: Record<string, string>) => {
    let filtered = [...users];
    Object.entries(filters).forEach(([key, value]) => {
      if (value) {
        filtered = filtered.filter((user) =>
          String(user[key as keyof User]).toLowerCase().includes(value.toLowerCase())
        );
      }
    });
    setSortedData(filtered);
  };

  const columns: ColumnConfig[] = [
    { key: 'name', label: 'Name', priority: 1, sortable: true },
    { key: 'email', label: 'Email', priority: 2, sortable: true },
    { key: 'role', label: 'Role', priority: 1, sortable: true },
    { key: 'status', label: 'Status', priority: 3 },
  ];

  return (
    <ResponsiveTable
      columns={columns}
      data={sortedData}
      keyExtractor={(row) => row.id}
      onSort={handleSort}
      showFilter
      onFilter={handleFilter}
      stickyHeader
    />
  );
}

// =============================================================================
// Example 3: Advanced DataTable with All Features
// =============================================================================

interface Product {
  id: number;
  name: string;
  category: string;
  price: number;
  stock: number;
  status: 'in_stock' | 'low_stock' | 'out_of_stock';
}

const products: Product[] = [
  { id: 1, name: 'Laptop Pro', category: 'Electronics', price: 1299, stock: 15, status: 'in_stock' },
  { id: 2, name: 'Wireless Mouse', category: 'Electronics', price: 29, stock: 5, status: 'low_stock' },
  { id: 3, name: 'USB-C Cable', category: 'Accessories', price: 19, stock: 0, status: 'out_of_stock' },
  { id: 4, name: 'Monitor 4K', category: 'Electronics', price: 499, stock: 8, status: 'in_stock' },
  { id: 5, name: 'Keyboard Mechanical', category: 'Electronics', price: 149, stock: 12, status: 'in_stock' },
];

export function AdvancedDataTableExample() {
  const columns: DataTableColumn<Product>[] = [
    {
      key: 'name',
      label: 'Product Name',
      sortable: true,
      render: (value, row) => (
        <div className="font-medium">{value}</div>
      ),
    },
    {
      key: 'category',
      label: 'Category',
      sortable: true,
    },
    {
      key: 'price',
      label: 'Price',
      sortable: true,
      render: (value) => (
        <span className="font-semibold">${value.toFixed(2)}</span>
      ),
    },
    {
      key: 'stock',
      label: 'Stock',
      sortable: true,
      render: (value, row) => (
        <div className="flex items-center gap-2">
          <span>{value}</span>
          {value === 0 && (
            <span className="text-xs text-error-600 dark:text-error-400">(Out)</span>
          )}
        </div>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      render: (value) => {
        const variants = {
          in_stock: 'success',
          low_stock: 'warning',
          out_of_stock: 'danger',
        };
        const labels = {
          in_stock: 'In Stock',
          low_stock: 'Low Stock',
          out_of_stock: 'Out of Stock',
        };
        return (
          <Badge variant={variants[value] as any}>
            {labels[value]}
          </Badge>
        );
      },
    },
  ];

  const bulkActions: BulkAction[] = [
    {
      label: 'Delete',
      variant: 'danger',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
        </svg>
      ),
      onClick: (ids) => {
        console.log('Delete products:', ids);
        alert(`Delete ${ids.length} products`);
      },
    },
    {
      label: 'Export',
      variant: 'secondary',
      icon: (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      ),
      onClick: (ids) => {
        console.log('Export products:', ids);
      },
    },
  ];

  const handleExport = (data: Product[]) => {
    console.log('Export all data:', data);
    // Convert to CSV or Excel
    const csv = [
      ['Name', 'Category', 'Price', 'Stock', 'Status'].join(','),
      ...data.map(p => [p.name, p.category, p.price, p.stock, p.status].join(',')),
    ].join('\n');

    // Download CSV
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'products.csv';
    a.click();
  };

  return (
    <DataTable
      columns={columns}
      data={products}
      keyExtractor={(row) => row.id}
      searchable
      searchPlaceholder="Search products..."
      searchKeys={['name', 'category']}
      sortable
      selectable
      bulkActions={bulkActions}
      pagination
      pageSize={5}
      pageSizeOptions={[5, 10, 25, 50]}
      onExport={handleExport}
      emptyMessage="No products found"
      emptyIcon={
        <svg className="w-12 h-12 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
        </svg>
      }
      stickyHeader
    />
  );
}

// =============================================================================
// Example 4: DataTable with Loading State
// =============================================================================

export function DataTableWithLoadingExample() {
  const [isLoading, setIsLoading] = React.useState(true);
  const [data, setData] = React.useState<Product[]>([]);

  React.useEffect(() => {
    // Simulate API call
    setTimeout(() => {
      setData(products);
      setIsLoading(false);
    }, 2000);
  }, []);

  const columns: DataTableColumn<Product>[] = [
    { key: 'name', label: 'Product Name', sortable: true },
    { key: 'category', label: 'Category', sortable: true },
    { key: 'price', label: 'Price', sortable: true },
    { key: 'stock', label: 'Stock', sortable: true },
  ];

  return (
    <DataTable
      columns={columns}
      data={data}
      keyExtractor={(row) => row.id}
      isLoading={isLoading}
      searchable
      pagination
    />
  );
}

// =============================================================================
// Example 5: TableSkeleton Standalone
// =============================================================================

export function TableSkeletonExample() {
  return (
    <div className="space-y-8">
      <div>
        <h3 className="text-lg font-semibold mb-4">Default Skeleton (5 rows, 4 columns)</h3>
        <TableSkeleton />
      </div>

      <div>
        <h3 className="text-lg font-semibold mb-4">Custom Skeleton (10 rows, 6 columns)</h3>
        <TableSkeleton rows={10} columns={6} />
      </div>

      <div>
        <h3 className="text-lg font-semibold mb-4">Skeleton Without Header</h3>
        <TableSkeleton rows={3} columns={3} showHeader={false} />
      </div>
    </div>
  );
}

// =============================================================================
// Example 6: Simple DataTable (minimal configuration)
// =============================================================================

export function SimpleDataTableExample() {
  const columns: DataTableColumn[] = [
    { key: 'name', label: 'Name', sortable: true },
    { key: 'email', label: 'Email', sortable: true },
    { key: 'role', label: 'Role' },
  ];

  return (
    <DataTable
      columns={columns}
      data={users}
      keyExtractor={(row) => row.id}
    />
  );
}

// =============================================================================
// Example 7: DataTable with Custom Empty State
// =============================================================================

export function DataTableEmptyStateExample() {
  const columns: DataTableColumn[] = [
    { key: 'name', label: 'Name' },
    { key: 'email', label: 'Email' },
  ];

  return (
    <DataTable
      columns={columns}
      data={[]}
      keyExtractor={(row, index) => index}
      emptyMessage="No users available. Add your first user to get started!"
      emptyIcon={
        <svg className="w-16 h-16 text-gray-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
        </svg>
      }
    />
  );
}

// =============================================================================
// Example 8: Responsive Table with Complex Rendering
// =============================================================================

interface Order {
  id: string;
  customer: string;
  product: string;
  amount: number;
  status: 'pending' | 'processing' | 'completed' | 'cancelled';
  date: string;
}

const orders: Order[] = [
  { id: 'ORD-001', customer: 'John Doe', product: 'Laptop Pro', amount: 1299, status: 'completed', date: '2024-01-15' },
  { id: 'ORD-002', customer: 'Jane Smith', product: 'Wireless Mouse', amount: 29, status: 'processing', date: '2024-01-16' },
  { id: 'ORD-003', customer: 'Bob Johnson', product: 'Monitor 4K', amount: 499, status: 'pending', date: '2024-01-17' },
];

export function ComplexResponsiveTableExample() {
  const columns: ColumnConfig[] = [
    {
      key: 'id',
      label: 'Order ID',
      priority: 1,
      sortable: true,
      mobileLabel: 'ID',
      render: (value) => (
        <span className="font-mono text-xs bg-gray-100 dark:bg-gray-800 px-2 py-1 rounded">
          {value}
        </span>
      ),
    },
    {
      key: 'customer',
      label: 'Customer',
      priority: 1,
      sortable: true,
    },
    {
      key: 'product',
      label: 'Product',
      priority: 2,
      sortable: true,
    },
    {
      key: 'amount',
      label: 'Amount',
      priority: 2,
      sortable: true,
      render: (value) => (
        <span className="font-semibold text-green-600 dark:text-green-400">
          ${value.toFixed(2)}
        </span>
      ),
    },
    {
      key: 'status',
      label: 'Status',
      priority: 1,
      render: (value) => {
        const colors = {
          pending: 'default',
          processing: 'warning',
          completed: 'success',
          cancelled: 'danger',
        };
        return (
          <Badge variant={colors[value] as any}>
            {value.charAt(0).toUpperCase() + value.slice(1)}
          </Badge>
        );
      },
    },
    {
      key: 'date',
      label: 'Date',
      priority: 3,
      sortable: true,
      mobileLabel: 'Order Date',
      render: (value) => new Date(value).toLocaleDateString(),
    },
  ];

  return (
    <ResponsiveTable
      columns={columns}
      data={orders}
      keyExtractor={(row) => row.id}
      stickyHeader
    />
  );
}
