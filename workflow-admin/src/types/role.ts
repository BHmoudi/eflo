export interface Role {
  id: number;
  code: string;
  name: string;
  description?: string;
  parentRoleCode?: string;
  level: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserRole {
  id: number;
  userId: number;
  userName?: string;
  userEmail?: string;
  roleCode: string;
  roleName?: string;
  assignedAt: string;
  assignedBy?: number;
  assignedByName?: string;
  expiresAt?: string;
  isActive: boolean;
}

export interface RoleHierarchy {
  role: Role;
  children: RoleHierarchy[];
}
