'use client';

import { useState } from 'react';
import Link from 'next/link';
import toast from 'react-hot-toast';
import { useTranslation } from '@/hooks/useTranslation';
import { Lock, Mail, Sparkles, Workflow, Car, Brain, Zap, ArrowRight, TrendingUp, Users, BarChart, Shield } from 'lucide-react';

export default function LoginPage() {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);
  const [credentials, setCredentials] = useState({
    username: '',
    password: ''
  });

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
      });

      if (response.ok) {
        const data = await response.json();
        localStorage.setItem('authToken', data.access_token);
        if (data.refresh_token) {
          localStorage.setItem('refreshToken', data.refresh_token);
        }

        toast.success(t('auth.loginSuccess'));
        setTimeout(() => {
          window.location.href = '/dashboard';
        }, 500);
      } else {
        const error = await response.json();
        toast.error(error.error_description || t('auth.loginError'));
      }
    } catch (error) {
      toast.error('Connection error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left Side - Animated Showcase */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-blue-600 via-purple-600 to-indigo-700 p-12 flex-col justify-between relative overflow-hidden">
        {/* Animated background elements */}
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-96 h-96 bg-white rounded-full mix-blend-multiply filter blur-3xl animate-blob"></div>
          <div className="absolute top-40 right-20 w-96 h-96 bg-purple-300 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-2000"></div>
          <div className="absolute bottom-20 left-40 w-96 h-96 bg-blue-300 rounded-full mix-blend-multiply filter blur-3xl animate-blob animation-delay-4000"></div>
        </div>

        {/* Animated SVG Icons - Floating */}
        <div className="absolute inset-0 overflow-hidden">
          <div className="absolute top-1/4 left-1/4 animate-float">
            <Car className="w-16 h-16 text-white/20" />
          </div>
          <div className="absolute top-1/3 right-1/4 animate-float-delayed">
            <Brain className="w-20 h-20 text-white/20" />
          </div>
          <div className="absolute bottom-1/3 left-1/3 animate-float-slow">
            <Workflow className="w-24 h-24 text-white/20" />
          </div>
          <div className="absolute bottom-1/4 right-1/3 animate-float">
            <TrendingUp className="w-14 h-14 text-white/20" />
          </div>
        </div>

        {/* Logo and Branding */}
        <div className="relative z-10">
          <div className="flex items-center space-x-3 mb-8">
            <div className="relative group">
              <div className="absolute inset-0 bg-gradient-to-r from-purple-500 to-pink-500 rounded-xl blur-xl group-hover:blur-2xl transition-all opacity-50"></div>
              <div className="relative w-16 h-16 bg-white rounded-xl flex items-center justify-center shadow-2xl">
                <Workflow className="w-10 h-10 text-blue-600 animate-pulse-slow" />
              </div>
              <div className="absolute -top-1 -right-1 w-6 h-6 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full flex items-center justify-center animate-ping-slow">
                <Sparkles className="w-3 h-3 text-white" />
              </div>
            </div>
            <div>
              <h1 className="text-4xl font-bold text-white">{t('common.appName')}</h1>
              <p className="text-blue-100 text-sm flex items-center">
                <Sparkles className="w-3 h-3 mr-1" />
                {t('common.appTagline')}
              </p>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="relative z-10 space-y-8">
          <div>
            <h2 className="text-5xl font-bold text-white mb-4 leading-tight">
              Intelligent<br />
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-200 to-purple-200">
                Vehicle Fleet
              </span><br />
              Management
            </h2>
            {/* Main Content 
            <p className="text-xl text-blue-100">Powered by Artificial Intelligence</p>
            */}
          </div>

          {/* Feature Grid */}
          <div className="grid grid-cols-2 gap-4">
            {/* AI Feature */}
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 border border-white/20 hover:bg-white/20 transition-all transform hover:scale-105">
              <Brain className="w-8 h-8 text-white mb-2 animate-pulse" />
              <h3 className="text-white font-semibold text-sm">{t('features.aiIntelligence')}</h3>
              <p className="text-blue-100 text-xs">{t('features.aiIntelligenceDesc')}</p>
            </div>

            {/* Vehicle Orders */}
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 border border-white/20 hover:bg-white/20 transition-all transform hover:scale-105">
              <Car className="w-8 h-8 text-white mb-2" />
              <h3 className="text-white font-semibold text-sm">{t('features.vehicleOrders')}</h3>
              <p className="text-blue-100 text-xs">{t('features.vehicleOrdersDesc')}</p>
            </div>

            {/* Real-time Analytics */}
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 border border-white/20 hover:bg-white/20 transition-all transform hover:scale-105">
              <BarChart className="w-8 h-8 text-white mb-2 animate-bounce-slow" />
              <h3 className="text-white font-semibold text-sm">{t('features.analytics')}</h3>
              <p className="text-blue-100 text-xs">{t('features.analyticsDesc')}</p>
            </div>

            {/* Team Collaboration */}
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 border border-white/20 hover:bg-white/20 transition-all transform hover:scale-105">
              <Users className="w-8 h-8 text-white mb-2" />
              <h3 className="text-white font-semibold text-sm">{t('features.collaboration')}</h3>
              <p className="text-blue-100 text-xs">{t('features.collaborationDesc')}</p>
            </div>

            {/* Automation */}
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 border border-white/20 hover:bg-white/20 transition-all transform hover:scale-105">
              <Zap className="w-8 h-8 text-white mb-2 animate-bounce" />
              <h3 className="text-white font-semibold text-sm">{t('features.automation')}</h3>
              <p className="text-blue-100 text-xs">{t('features.automationDesc')}</p>
            </div>

            {/* Security */}
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-4 border border-white/20 hover:bg-white/20 transition-all transform hover:scale-105">
              <Shield className="w-8 h-8 text-white mb-2" />
              <h3 className="text-white font-semibold text-sm">{t('features.enterpriseSecurity')}</h3>
              <p className="text-blue-100 text-xs">{t('features.enterpriseSecurityDesc')}</p>
            </div>
          </div>

          {/* Stats */}
          <div className="flex items-center justify-around pt-6 border-t border-white/20">
            <div className="text-center">
              <div className="text-3xl font-bold text-white mb-1">10K+</div>
              <div className="text-xs text-blue-100">{t('stats.ordersProcessed')}</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-white mb-1">99.9%</div>
              <div className="text-xs text-blue-100">{t('stats.uptime')}</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-white mb-1">24/7</div>
              <div className="text-xs text-blue-100">{t('stats.aiSupport')}</div>
            </div>
          </div>
        </div>

        {/* Footer 
        <div className="relative z-10 text-blue-100 text-sm">
          <p>© 2025 E-FLO Platform. AI-Enhanced Fleet Management System.</p>
          <p className="text-xs mt-1 text-blue-200">Intelligent Workflow Automation for Modern Enterprises</p>
        </div>
        */}
      </div>

      {/* Right Side - Login Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-gradient-to-br from-gray-50 to-white">
        <div className="w-full max-w-md">
          {/* Mobile Logo */}
          <div className="lg:hidden flex items-center justify-center mb-8">
            <div className="flex items-center space-x-3">
              <div className="relative">
                <div className="w-12 h-12 bg-gradient-to-br from-blue-600 to-purple-600 rounded-xl flex items-center justify-center shadow-xl">
                  <Workflow className="w-7 h-7 text-white" />
                </div>
                <div className="absolute -top-1 -right-1 w-4 h-4 bg-gradient-to-r from-purple-500 to-pink-500 rounded-full flex items-center justify-center">
                  <Sparkles className="w-2 h-2 text-white animate-pulse" />
                </div>
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">E-FLO</h1>
                <p className="text-gray-500 text-xs flex items-center">
                  <Brain className="w-3 h-3 mr-1" />
                  AI-Powered
                </p>
              </div>
            </div>
          </div>

          {/* Welcome Text */}
          <div className="mb-8">
            <h2 className="text-3xl font-bold text-gray-900 mb-2">{t('auth.welcomeBack')}</h2>
            <p className="text-gray-600">{t('auth.signInSubtitle')}</p>
          </div>

          {/* Login Form */}
          <form onSubmit={handleLogin} className="space-y-6">
            {/* Email Input */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('auth.email')}
              </label>
              <div className="relative group">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                </div>
                <input
                  type="text"
                  value={credentials.username}
                  onChange={(e) => setCredentials({ ...credentials, username: e.target.value })}
                  className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all shadow-sm hover:shadow-md"
                  placeholder={t('auth.emailPlaceholder')}
                  required
                  disabled={loading}
                />
              </div>
            </div>

            {/* Password Input */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                {t('auth.password')}
              </label>
              <div className="relative group">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-gray-400 group-focus-within:text-blue-500 transition-colors" />
                </div>
                <input
                  type="password"
                  value={credentials.password}
                  onChange={(e) => setCredentials({ ...credentials, password: e.target.value })}
                  className="block w-full pl-10 pr-3 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all shadow-sm hover:shadow-md"
                  placeholder={t('auth.passwordPlaceholder')}
                  required
                  disabled={loading}
                />
              </div>
            </div>

            {/* Remember Me & Forgot Password */}
            <div className="flex items-center justify-between">
              <label className="flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded transition-all"
                />
                <span className="ml-2 text-sm text-gray-600 hover:text-gray-900 transition-colors">{t('auth.rememberMe')}</span>
              </label>
              <Link
                href="/reset-password"
                className="text-sm font-medium text-blue-600 hover:text-blue-700 transition-colors flex items-center group"
              >
                {t('auth.forgotPassword')}
                <ArrowRight className="w-3 h-3 ml-1 group-hover:translate-x-1 transition-transform" />
              </Link>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center px-4 py-3.5 border border-transparent text-base font-semibold rounded-xl text-white bg-gradient-to-r from-blue-600 via-purple-600 to-indigo-600 hover:from-blue-700 hover:via-purple-700 hover:to-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed shadow-xl hover:shadow-2xl transform hover:scale-105 hover:-translate-y-0.5 relative overflow-hidden group"
            >
              <div className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/20 to-white/0 translate-x-[-100%] group-hover:translate-x-[100%] transition-transform duration-1000"></div>
              {loading ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  <span className="relative z-10">{t('auth.signingIn')}</span>
                </>
              ) : (
                <>
                  <span className="relative z-10">{t('auth.signIn')}</span>
                  <ArrowRight className="ml-2 h-5 w-5 relative z-10 group-hover:translate-x-1 transition-transform" />
                </>
              )}
            </button>
          </form>

          {/* Divider */}
          <div className="relative my-8">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-gray-300"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-4 bg-gradient-to-br from-gray-50 to-white text-gray-500">Demo Access</span>
            </div>
          </div>

          {/* Demo Credentials - Enhanced */}
          <div className="relative group">
            <div className="absolute inset-0 bg-gradient-to-r from-blue-400 to-purple-400 rounded-xl blur opacity-25 group-hover:opacity-40 transition-opacity"></div>
            <div className="relative bg-gradient-to-br from-blue-50 to-purple-50 border-2 border-blue-200 rounded-xl p-5 hover:border-blue-300 transition-all">
              <div className="flex items-center mb-3">
                <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-500 rounded-lg flex items-center justify-center mr-2">
                  <Lock className="w-4 h-4 text-white" />
                </div>
                <p className="text-sm text-blue-900 font-semibold">{t('auth.demoCredentials')}</p>
              </div>
              <div className="space-y-2 bg-white/60 rounded-lg p-3">
                <div className="flex items-center">
                  <Mail className="w-4 h-4 text-blue-600 mr-2" />
                  <p className="text-sm text-blue-800 font-mono">john.doe@eflo.com</p>
                </div>
                <div className="flex items-center">
                  <Lock className="w-4 h-4 text-blue-600 mr-2" />
                  <p className="text-sm text-blue-800 font-mono">test</p>
                </div>
              </div>
            </div>
          </div>

          {/* AI Badge */}
          <div className="mt-8 flex flex-col items-center space-y-3">
            <div className="flex items-center justify-center space-x-2 text-gray-500 text-sm">
              <Brain className="w-4 h-4 text-purple-500" />
              <span className="font-medium">{t('common.poweredBy')}</span>
              <Sparkles className="w-4 h-4 text-purple-500 animate-pulse" />
            </div>
            <div className="flex items-center space-x-6 text-xs text-gray-400">
              <div className="flex items-center">
                <div className="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse"></div>
                <span>{t('common.systemOnline')}</span>
              </div>
              <div className="flex items-center">
                <Shield className="w-3 h-3 mr-1" />
                <span>{t('common.secureConnection')}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Custom Animations */}
      <style jsx global>{`
        @keyframes blob {
          0%, 100% { transform: translate(0px, 0px) scale(1); }
          33% { transform: translate(30px, -50px) scale(1.1); }
          66% { transform: translate(-20px, 20px) scale(0.9); }
        }

        @keyframes float {
          0%, 100% { transform: translateY(0px); }
          50% { transform: translateY(-20px); }
        }

        @keyframes float-slow {
          0%, 100% { transform: translateY(0px) rotate(0deg); }
          50% { transform: translateY(-30px) rotate(5deg); }
        }

        @keyframes pulse-slow {
          0%, 100% { opacity: 1; transform: scale(1); }
          50% { opacity: 0.8; transform: scale(1.05); }
        }

        @keyframes ping-slow {
          0% { transform: scale(1); opacity: 1; }
          75%, 100% { transform: scale(2); opacity: 0; }
        }

        @keyframes bounce-slow {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-10px); }
        }

        .animate-blob {
          animation: blob 7s infinite;
        }

        .animate-float {
          animation: float 6s ease-in-out infinite;
        }

        .animate-float-delayed {
          animation: float 8s ease-in-out infinite;
          animation-delay: 2s;
        }

        .animate-float-slow {
          animation: float-slow 10s ease-in-out infinite;
        }

        .animate-pulse-slow {
          animation: pulse-slow 3s ease-in-out infinite;
        }

        .animate-ping-slow {
          animation: ping-slow 2s cubic-bezier(0, 0, 0.2, 1) infinite;
        }

        .animate-bounce-slow {
          animation: bounce-slow 2s infinite;
        }

        .animation-delay-2000 {
          animation-delay: 2s;
        }

        .animation-delay-4000 {
          animation-delay: 4s;
        }
      `}</style>
    </div>
  );
}
