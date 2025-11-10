'use client';

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';

interface User {
  email: string;
  username: string;
  roles: string[];
  employeeNumber?: string;
  businessUnitIds?: number[];
}

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  refreshAuth: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  // Initialize auth state from storage
  const initializeAuth = useCallback(() => {
    try {
      const storedToken = localStorage.getItem('authToken') || sessionStorage.getItem('authToken');

      if (storedToken) {
        setToken(storedToken);

        // Decode JWT to get user info
        try {
          const payload = JSON.parse(atob(storedToken.split('.')[1]));
          setUser({
            email: payload.email || '',
            username: payload.preferred_username || payload.username || '',
            roles: payload.realm_access?.roles || [],
            employeeNumber: payload.employeeNumber,
            businessUnitIds: payload.businessUnitIds,
          });
        } catch (e) {
          console.error('Failed to decode token:', e);
          setToken(null);
          setUser(null);
        }
      }
    } catch (error) {
      console.error('Error initializing auth:', error);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  const login = async (username: string, password: string) => {
    try {
      setIsLoading(true);

      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password }),
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.error_description || 'Login failed');
      }

      const data = await response.json();

      // Store tokens
      localStorage.setItem('authToken', data.access_token);
      sessionStorage.setItem('authToken', data.access_token);

      if (data.refresh_token) {
        localStorage.setItem('refreshToken', data.refresh_token);
        sessionStorage.setItem('refreshToken', data.refresh_token);
      }

      // Decode and set user
      const payload = JSON.parse(atob(data.access_token.split('.')[1]));
      const userData: User = {
        email: payload.email || '',
        username: payload.preferred_username || payload.username || '',
        roles: payload.realm_access?.roles || [],
        employeeNumber: payload.employeeNumber,
        businessUnitIds: payload.businessUnitIds,
      };

      setToken(data.access_token);
      setUser(userData);

      console.log('✅ Login successful, user:', userData.username);

      // Navigate to dashboard
      router.push('/dashboard');
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = useCallback(async () => {
    try {
      // Call logout API
      await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});

      // Clear state
      setToken(null);
      setUser(null);

      // Clear storage
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      sessionStorage.removeItem('authToken');
      sessionStorage.removeItem('refreshToken');

      console.log('✅ Logged out');

      // Navigate to login
      router.push('/login');
    } catch (error) {
      console.error('Logout error:', error);
    }
  }, [router]);

  const refreshAuth = useCallback(() => {
    initializeAuth();
  }, [initializeAuth]);

  const value: AuthContextType = {
    user,
    token,
    isAuthenticated: !!token && !!user,
    isLoading,
    login,
    logout,
    refreshAuth,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
