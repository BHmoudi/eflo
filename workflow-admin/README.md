# Workflow Admin Dashboard

## Overview

The Workflow Admin is the Next.js 14-based administrative dashboard for the Eflo platform. It provides a comprehensive web interface for managing orders, workflows, users, documents, and commissions with an intuitive visual workflow designer.

## Functionality

### Core Features

- **Order Management**: Create, view, and manage vehicle orders (VN, VO, EVO)
- **Workflow Visualization**: Visual workflow designer using React Flow
- **Task Management**: View and complete workflow tasks
- **User Administration**: Manage users, roles, and business units
- **Document Management**: Upload, view, and download documents
- **Commission Tracking**: View sales commissions and reports
- **Dashboard Analytics**: Real-time metrics and KPIs
- **Authentication**: OAuth2/JWT integration with Keycloak
- **Bilingual Support**: French and English interface

### Technical Details

- **Port**: 3001
- **Technology**: Next.js 14 (App Router), React 18, TypeScript
- **State Management**: Zustand (client state), TanStack Query (server state)
- **Styling**: Tailwind CSS 4.0
- **Visualization**: React Flow for workflow diagrams
- **Forms**: React Hook Form with Zod validation
- **API Communication**: Axios with interceptors

## Tech Stack

- **Framework**: Next.js 14 with App Router
- **Language**: TypeScript
- **UI Library**: React 18
- **Styling**: Tailwind CSS 4.0
- **State Management**:
  - Zustand (client state)
  - TanStack Query (React Query) for server state
- **Forms**: React Hook Form + Zod
- **Charts**: Recharts or Chart.js
- **Workflow Visualization**: React Flow
- **Date Handling**: date-fns
- **HTTP Client**: Axios
- **Database (Direct)**: PostgreSQL client (pg) for certain operations

## Project Structure

```
workflow-admin/
├── app/                    # Next.js App Router pages
│   ├── (auth)/             # Authentication routes
│   │   ├── login/
│   │   └── callback/
│   ├── (dashboard)/        # Protected dashboard routes
│   │   ├── layout.tsx      # Dashboard layout
│   │   ├── page.tsx        # Dashboard home
│   │   ├── orders/         # Order management
│   │   ├── workflows/      # Workflow management
│   │   ├── tasks/          # Task management
│   │   ├── users/          # User management
│   │   ├── documents/      # Document management
│   │   └── commissions/    # Commission tracking
│   ├── api/                # API routes (if any)
│   ├── layout.tsx          # Root layout
│   └── page.tsx            # Landing page
├── components/             # React components
│   ├── ui/                 # UI primitives (buttons, inputs, etc.)
│   ├── forms/              # Form components
│   ├── tables/             # Data table components
│   ├── charts/             # Chart components
│   └── workflow/           # Workflow-specific components
├── lib/                    # Utilities and configurations
│   ├── api/                # API client and endpoints
│   ├── auth/               # Authentication utilities
│   ├── stores/             # Zustand stores
│   ├── utils/              # Helper functions
│   └── validations/        # Zod schemas
├── types/                  # TypeScript type definitions
├── hooks/                  # Custom React hooks
├── public/                 # Static assets
├── styles/                 # Global styles
├── middleware.ts           # Next.js middleware
├── next.config.js          # Next.js configuration
├── tailwind.config.ts      # Tailwind configuration
├── tsconfig.json           # TypeScript configuration
└── package.json            # Dependencies
```

## Installation

### Prerequisites

- Node.js 18+ (LTS recommended)
- npm or yarn
- Running Eflo backend services

### Install Dependencies

```bash
cd workflow-admin
npm install
```

### Environment Variables

Create `.env.local` file:

```env
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8081

# Keycloak Configuration
NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8180
NEXT_PUBLIC_KEYCLOAK_REALM=eflo
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=workflow-admin

# Database (if using direct connection)
DATABASE_URL=postgresql://workflowuser:workflowpass@localhost:5434/workflow_db

# Optional
NEXT_PUBLIC_APP_NAME=Eflo Workflow Admin
NEXT_PUBLIC_APP_VERSION=1.0.0
```

## Development

### Run Development Server

```bash
npm run dev
```

Access at: `http://localhost:3001`

### Build for Production

```bash
npm run build
```

### Start Production Server

```bash
npm start
```

### Type Checking

```bash
npm run type-check
```

### Linting

```bash
npm run lint
npm run lint:fix
```

## Authentication

### Keycloak Integration

The application uses OAuth2/JWT authentication via Keycloak:

1. User clicks "Login"
2. Redirected to Keycloak login page
3. After successful login, redirected back with authorization code
4. Token exchange occurs
5. JWT token stored in localStorage/cookies
6. API requests include Bearer token

### Auth Flow

```typescript
// lib/auth/keycloak.ts
export const keycloak = {
  url: process.env.NEXT_PUBLIC_KEYCLOAK_URL,
  realm: process.env.NEXT_PUBLIC_KEYCLOAK_REALM,
  clientId: process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID,

  login() {
    // Redirect to Keycloak login
  },

  logout() {
    // Clear tokens and redirect to Keycloak logout
  },

  getToken() {
    // Retrieve stored JWT token
  },

  refreshToken() {
    // Refresh expired token
  }
}
```

### Protected Routes

```typescript
// middleware.ts
export function middleware(request: NextRequest) {
  const token = request.cookies.get('auth_token');

  if (!token && request.nextUrl.pathname.startsWith('/dashboard')) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/dashboard/:path*']
};
```

## API Integration

### API Client

```typescript
// lib/api/client.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 30000,
});

// Request interceptor - add auth token
apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle errors
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Redirect to login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### API Endpoints

```typescript
// lib/api/orders.ts
import apiClient from './client';

export const ordersApi = {
  getAll: (params?) => apiClient.get('/api/v1/orders', { params }),
  getById: (id: number) => apiClient.get(`/api/v1/orders/${id}`),
  create: (data) => apiClient.post('/api/v1/orders', data),
  update: (id: number, data) => apiClient.put(`/api/v1/orders/${id}`, data),
  delete: (id: number) => apiClient.delete(`/api/v1/orders/${id}`),
};

// lib/api/workflows.ts
export const workflowsApi = {
  getProcesses: () => apiClient.get('/api/v1/workflows/processes'),
  getInstance: (id) => apiClient.get(`/api/v1/workflows/instances/${id}`),
  getTasks: (userId) => apiClient.get(`/api/v1/workflows/tasks/user/${userId}`),
  completeTask: (taskId, data) => apiClient.post(`/api/v1/workflows/tasks/${taskId}/complete`, data),
};
```

## State Management

### TanStack Query (Server State)

```typescript
// hooks/useOrders.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ordersApi } from '@/lib/api/orders';

export function useOrders(filters?) {
  return useQuery({
    queryKey: ['orders', filters],
    queryFn: () => ordersApi.getAll(filters),
  });
}

export function useCreateOrder() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ordersApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
}
```

### Zustand (Client State)

```typescript
// lib/stores/authStore.ts
import { create } from 'zustand';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  setUser: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  setUser: (user) => set({ user, isAuthenticated: true }),
  logout: () => set({ user: null, isAuthenticated: false }),
}));
```

## Workflow Visualization

### React Flow Integration

```typescript
// components/workflow/WorkflowDesigner.tsx
import ReactFlow, {
  Node,
  Edge,
  Controls,
  Background
} from 'reactflow';
import 'reactflow/dist/style.css';

export function WorkflowDesigner({ processId }) {
  const [nodes, setNodes] = useState<Node[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);

  // Load workflow process
  const { data: process } = useQuery({
    queryKey: ['workflow-process', processId],
    queryFn: () => workflowsApi.getProcess(processId),
  });

  // Convert states to nodes
  useEffect(() => {
    if (process) {
      const nodes = process.states.map((state) => ({
        id: state.id.toString(),
        type: state.type === 'START' ? 'input' : state.type === 'END' ? 'output' : 'default',
        data: { label: state.name },
        position: state.position || { x: 0, y: 0 },
      }));

      const edges = process.transitions.map((transition) => ({
        id: transition.id.toString(),
        source: transition.fromStateId.toString(),
        target: transition.toStateId.toString(),
        label: transition.event,
      }));

      setNodes(nodes);
      setEdges(edges);
    }
  }, [process]);

  return (
    <div style={{ height: '600px' }}>
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        fitView
      >
        <Controls />
        <Background />
      </ReactFlow>
    </div>
  );
}
```

## Key Features

### Order Management

- Create orders with vehicle details
- View order list with filtering and pagination
- Edit order information
- Track order status
- View order history

### Workflow Management

- Visual workflow designer
- Create and edit workflow processes
- Define states and transitions
- Configure approval chains
- View workflow instances

### Task Management

- My Tasks dashboard
- Task list with filters
- Task details with context
- Complete tasks with outcomes
- Escalation handling

### User Management

- User list and details
- Create/edit users
- Role assignments
- Business unit management
- Hierarchy visualization

## Styling

### Tailwind CSS

The application uses Tailwind CSS 4.0:

```typescript
// tailwind.config.ts
import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f9ff',
          // ... color palette
          900: '#0c4a6e',
        },
      },
    },
  },
  plugins: [],
};

export default config;
```

## Testing

### Unit Tests (if configured)

```bash
npm run test
npm run test:watch
```

### E2E Tests (if configured)

```bash
npm run test:e2e
```

## Deployment

### Docker

Create `Dockerfile`:

```dockerfile
FROM node:18-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci

FROM node:18-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:18-alpine AS runner
WORKDIR /app
ENV NODE_ENV production
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3001
CMD ["node", "server.js"]
```

Build and run:

```bash
docker build -t eflo/workflow-admin:1.0.0 .
docker run -p 3001:3001 eflo/workflow-admin:1.0.0
```

### Production Considerations

- Enable output: 'standalone' in next.config.js
- Set up environment variables
- Configure proper CORS
- Enable HTTPS
- Set up CDN for static assets
- Implement proper error tracking (Sentry)
- Add performance monitoring

## Troubleshooting

### API connection issues
- Verify backend services are running
- Check API URL in .env.local
- Inspect network tab in browser DevTools
- Check CORS configuration

### Authentication issues
- Verify Keycloak is running
- Check Keycloak configuration
- Clear browser cookies/localStorage
- Check token expiration

### Build errors
- Clear .next directory: `rm -rf .next`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`
- Check TypeScript errors: `npm run type-check`

## Additional Resources

- Next.js Documentation: https://nextjs.org/docs
- React Flow: https://reactflow.dev/
- TanStack Query: https://tanstack.com/query
- Zustand: https://github.com/pmndrs/zustand
- Tailwind CSS: https://tailwindcss.com/
