'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Button from '@/components/ui/Button';
import Input from '@/components/ui/Input';
import Card from '@/components/ui/Card';

export default function LoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      console.log('Attempting login for:', credentials.username);

      // Use API proxy to avoid CORS
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: credentials.username,
          password: credentials.password,
        }),
      });

      console.log('Login response status:', response.status);

      if (response.ok) {
        const data = await response.json();
        console.log('✅ Login successful! Token received');

        // Store tokens in BOTH localStorage and sessionStorage for reliability
        const token = data.access_token;
        const refreshToken = data.refresh_token;

        localStorage.setItem('authToken', token);
        sessionStorage.setItem('authToken', token);

        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
          sessionStorage.setItem('refreshToken', refreshToken);
        }

        // Verify storage
        console.log('✅ Token stored in localStorage:', !!localStorage.getItem('authToken'));
        console.log('✅ Token stored in sessionStorage:', !!sessionStorage.getItem('authToken'));

        // Use router.push for client-side navigation (no full page reload)
        console.log('🔄 Navigating to dashboard...');
        router.push('/dashboard');
      } else {
        const errorData = await response.json();
        console.error('Login failed:', errorData);
        setError(errorData.error_description || 'Invalid credentials. Please check your username and password.');
      }
    } catch (error) {
      console.error('Login error:', error);
      setError('Login failed: ' + (error instanceof Error ? error.message : 'Network error. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  // Quick login for demo/testing
  const quickLogin = async () => {
    setLoading(true);
    setError('');
    try {
      // Use API proxy to avoid CORS
      const response = await fetch('/api/auth/quick-login', {
        method: 'POST',
      });

      if (response.ok) {
        const data = await response.json();
        console.log('Quick login successful:', data);

        // Store tokens in localStorage
        localStorage.setItem('authToken', data.access_token);
        if (data.refresh_token) {
          localStorage.setItem('refreshToken', data.refresh_token);
        }

        // Verify token was stored
        const storedToken = localStorage.getItem('authToken');
        console.log('Token stored successfully:', storedToken ? 'YES' : 'NO');

        // Force a small delay
        await new Promise(resolve => setTimeout(resolve, 100));

        console.log('Redirecting to dashboard...');

        // Use window.location for full page reload
        window.location.href = '/dashboard';
      } else {
        setError('Quick login failed. Please try user login instead.');
      }
    } catch (error) {
      console.error('Quick login failed:', error);
      setError('Quick login failed: Network error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-500 to-purple-600 p-4">
      <Card className="w-full max-w-md">
        <div className="p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              E-FLO Workflow Admin
            </h1>
            <p className="text-gray-600">
              Sign in to manage workflows
            </p>
          </div>

          {error && (
            <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-md">
              <p className="text-sm text-red-800">{error}</p>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-6">
            <Input
              label="Username"
              type="text"
              value={credentials.username}
              onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
              placeholder="john.doe@eflo.com"
              required
            />

            <Input
              label="Password"
              type="password"
              value={credentials.password}
              onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
              placeholder="Enter your password"
              required
            />

            <Button
              type="submit"
              variant="primary"
              fullWidth
              disabled={loading}
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </Button>
          </form>

          <div className="mt-6 text-center">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">Or</span>
              </div>
            </div>

            <Button
              onClick={quickLogin}
              variant="secondary"
              fullWidth
              className="mt-4"
              disabled={loading}
            >
              Quick Demo Login
            </Button>
          </div>

          <div className="mt-6 text-center text-sm text-gray-600">
            <p>Demo users:</p>
            <p className="text-xs mt-2">
              john.doe@eflo.com / jane.smith@eflo.com
            </p>
          </div>
        </div>
      </Card>
    </div>
  );
}
