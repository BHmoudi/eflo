import axios, { AxiosInstance, AxiosError, AxiosRequestConfig } from 'axios';

// Use empty base URL to make relative requests to Next.js API routes (NO CORS!)
const API_BASE_URL = '';

// Retry configuration
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000; // Base delay in ms
const RETRYABLE_STATUS_CODES = [408, 429, 500, 502, 503, 504];

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000, // 30 seconds
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor for adding auth token and user headers
apiClient.interceptors.request.use(
  (config) => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('authToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;

        // Extract user information from JWT token
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          config.headers['X-User-Id'] = payload.sub || payload.user_id || '1';
          config.headers['X-User-Email'] = payload.email || '';
          config.headers['X-Username'] = payload.preferred_username || payload.username || '';
        } catch (e) {
          // Fallback if token parsing fails
          console.warn('Failed to parse JWT token:', e);
          config.headers['X-User-Id'] = '1';
        }
      } else {
        // No token available - use default
        config.headers['X-User-Id'] = '1';
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Retry logic with exponential backoff
const shouldRetry = (error: AxiosError, retryCount: number): boolean => {
  // Don't retry if we've exceeded max retries
  if (retryCount >= MAX_RETRIES) {
    return false;
  }

  // Retry on network errors (no response)
  if (!error.response) {
    return true;
  }

  // Retry on specific status codes
  const status = error.response.status;
  return RETRYABLE_STATUS_CODES.includes(status);
};

const getRetryDelay = (retryCount: number): number => {
  // Exponential backoff: 1s, 2s, 4s
  return RETRY_DELAY * Math.pow(2, retryCount);
};

// Response interceptor for error handling and retry logic
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  async (error: AxiosError) => {
    const config = error.config as AxiosRequestConfig & { retryCount?: number };

    // Initialize retry count
    if (!config.retryCount) {
      config.retryCount = 0;
    }

    // Check if we should retry
    if (shouldRetry(error, config.retryCount)) {
      config.retryCount += 1;
      const delay = getRetryDelay(config.retryCount - 1);

      console.log(`Retrying request (attempt ${config.retryCount}/${MAX_RETRIES}) after ${delay}ms...`);

      // Wait before retrying
      await new Promise((resolve) => setTimeout(resolve, delay));

      // Retry the request
      return apiClient(config);
    }

    // Classify and format errors
    if (error.response) {
      // Server responded with error status
      const status = error.response.status;
      const data = error.response.data as any;

      // Handle 401 Unauthorized
      // DO NOT automatically redirect - let components handle 401
      // Automatic redirect causes login loops
      if (status === 401) {
        console.warn('⚠️ API returned 401 Unauthorized');
        // Components can check for 401 and handle appropriately
      }

      // Classify error type
      let errorType: ApiErrorType = 'server';
      if (status >= 400 && status < 500) {
        errorType = status === 422 ? 'validation' : 'client';
      } else if (status >= 500) {
        errorType = 'server';
      }

      // Return formatted error
      return Promise.reject({
        type: errorType,
        status,
        message: data?.message || data?.error || getDefaultErrorMessage(status),
        errors: data?.errors || [],
        originalError: error,
      });
    } else if (error.request) {
      // Request made but no response (network error)
      return Promise.reject({
        type: 'network' as ApiErrorType,
        status: 0,
        message: 'No response from server. Please check your connection.',
        errors: [],
        originalError: error,
      });
    } else {
      // Error in request setup
      return Promise.reject({
        type: 'unknown' as ApiErrorType,
        status: 0,
        message: error.message || 'An unexpected error occurred',
        errors: [],
        originalError: error,
      });
    }
  }
);

// Helper function to get default error messages
const getDefaultErrorMessage = (status: number): string => {
  switch (status) {
    case 400:
      return 'Bad request. Please check your input.';
    case 401:
      return 'Unauthorized. Please log in.';
    case 403:
      return 'Forbidden. You do not have permission to access this resource.';
    case 404:
      return 'Resource not found.';
    case 408:
      return 'Request timeout. Please try again.';
    case 422:
      return 'Validation error. Please check your input.';
    case 429:
      return 'Too many requests. Please try again later.';
    case 500:
      return 'Internal server error. Please try again later.';
    case 502:
      return 'Bad gateway. Please try again later.';
    case 503:
      return 'Service unavailable. Please try again later.';
    case 504:
      return 'Gateway timeout. Please try again later.';
    default:
      return 'An error occurred. Please try again.';
  }
};

// Error types
export type ApiErrorType = 'network' | 'server' | 'client' | 'validation' | 'unknown';

// Enhanced API Error interface
export interface ApiError {
  type: ApiErrorType;
  status: number;
  message: string;
  errors: any[];
  originalError?: AxiosError;
}

export default apiClient;
