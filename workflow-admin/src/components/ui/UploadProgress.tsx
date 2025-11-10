import React from 'react';

export interface UploadProgressProps {
  /** Name of the file being uploaded */
  fileName: string;
  /** Size of the file in bytes */
  fileSize: number;
  /** Upload progress (0-100) */
  progress: number;
  /** Upload status */
  status: 'pending' | 'uploading' | 'success' | 'error';
  /** Error message if status is 'error' */
  error?: string;
  /** Callback when cancel button is clicked */
  onCancel?: () => void;
  /** Callback when retry button is clicked */
  onRetry?: () => void;
  /** Show detailed information */
  showDetails?: boolean;
  /** Custom className */
  className?: string;
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
};

const getFileIcon = (fileName: string): React.ReactNode => {
  const extension = fileName.split('.').pop()?.toLowerCase();

  const iconMap: Record<string, { icon: React.ReactNode; color: string }> = {
    pdf: {
      color: 'text-red-500',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
          clipRule="evenodd"
        />
      ),
    },
    doc: {
      color: 'text-blue-600',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
          clipRule="evenodd"
        />
      ),
    },
    docx: {
      color: 'text-blue-600',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
          clipRule="evenodd"
        />
      ),
    },
    xls: {
      color: 'text-green-600',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
          clipRule="evenodd"
        />
      ),
    },
    xlsx: {
      color: 'text-green-600',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
          clipRule="evenodd"
        />
      ),
    },
    jpg: {
      color: 'text-blue-500',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
          clipRule="evenodd"
        />
      ),
    },
    jpeg: {
      color: 'text-blue-500',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
          clipRule="evenodd"
        />
      ),
    },
    png: {
      color: 'text-blue-500',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
          clipRule="evenodd"
        />
      ),
    },
    zip: {
      color: 'text-yellow-600',
      icon: (
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V8a2 2 0 00-2-2h-5L9 4H4z"
          clipRule="evenodd"
        />
      ),
    },
  };

  const iconData = iconMap[extension || ''] || {
    color: 'text-gray-500',
    icon: (
      <path
        fillRule="evenodd"
        d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
        clipRule="evenodd"
      />
    ),
  };

  return (
    <svg className={`w-8 h-8 ${iconData.color}`} fill="currentColor" viewBox="0 0 20 20">
      {iconData.icon}
    </svg>
  );
};

const getStatusIcon = (status: 'pending' | 'uploading' | 'success' | 'error'): React.ReactNode => {
  switch (status) {
    case 'pending':
      return (
        <svg className="w-5 h-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
            clipRule="evenodd"
          />
        </svg>
      );
    case 'uploading':
      return (
        <svg className="w-5 h-5 text-blue-500 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
      );
    case 'success':
      return (
        <svg className="w-5 h-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
            clipRule="evenodd"
          />
        </svg>
      );
    case 'error':
      return (
        <svg className="w-5 h-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
            clipRule="evenodd"
          />
        </svg>
      );
  }
};

export const UploadProgress: React.FC<UploadProgressProps> = ({
  fileName,
  fileSize,
  progress,
  status,
  error,
  onCancel,
  onRetry,
  showDetails = true,
  className = '',
}) => {
  const getProgressBarColor = () => {
    switch (status) {
      case 'success':
        return 'bg-green-500';
      case 'error':
        return 'bg-red-500';
      case 'uploading':
        return 'bg-primary';
      default:
        return 'bg-gray-300 dark:bg-gray-600';
    }
  };

  const getStatusText = () => {
    switch (status) {
      case 'pending':
        return 'Pending';
      case 'uploading':
        return `Uploading ${progress}%`;
      case 'success':
        return 'Upload complete';
      case 'error':
        return error || 'Upload failed';
    }
  };

  return (
    <div
      className={`
        rounded-lg border border-stroke dark:border-strokedark
        bg-white dark:bg-boxdark p-4 transition-all duration-200
        ${status === 'error' ? 'border-red-300 dark:border-red-800' : ''}
        ${className}
      `}
    >
      <div className="flex items-start space-x-3">
        {/* File Icon */}
        <div className="flex-shrink-0">{getFileIcon(fileName)}</div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          {/* File Name and Status */}
          <div className="flex items-start justify-between mb-2">
            <div className="flex-1 min-w-0 pr-4">
              <h4
                className="text-sm font-medium text-black dark:text-white truncate"
                title={fileName}
              >
                {fileName}
              </h4>
              {showDetails && (
                <p className="text-xs text-gray-500 dark:text-gray-400 mt-0.5">
                  {formatFileSize(fileSize)}
                </p>
              )}
            </div>

            {/* Status Icon */}
            <div className="flex-shrink-0">{getStatusIcon(status)}</div>
          </div>

          {/* Progress Bar */}
          {(status === 'uploading' || status === 'pending') && (
            <div className="mb-2">
              <div className="h-2 bg-gray-100 dark:bg-meta-4 rounded-full overflow-hidden">
                <div
                  className={`h-full ${getProgressBarColor()} transition-all duration-300 ease-out rounded-full`}
                  style={{ width: `${progress}%` }}
                  role="progressbar"
                  aria-valuenow={progress}
                  aria-valuemin={0}
                  aria-valuemax={100}
                />
              </div>
            </div>
          )}

          {/* Status Text and Actions */}
          <div className="flex items-center justify-between">
            <span
              className={`text-xs font-medium ${
                status === 'error'
                  ? 'text-red-600 dark:text-red-400'
                  : status === 'success'
                  ? 'text-green-600 dark:text-green-400'
                  : 'text-gray-600 dark:text-gray-400'
              }`}
            >
              {getStatusText()}
            </span>

            {/* Action Buttons */}
            <div className="flex items-center space-x-2">
              {status === 'error' && onRetry && (
                <button
                  onClick={onRetry}
                  className="text-xs font-medium text-primary hover:text-primary/80 transition-colors duration-200"
                  aria-label="Retry upload"
                >
                  Retry
                </button>
              )}

              {(status === 'uploading' || status === 'pending' || status === 'error') && onCancel && (
                <button
                  onClick={onCancel}
                  className="text-xs font-medium text-gray-500 hover:text-red-600 dark:text-gray-400 dark:hover:text-red-400 transition-colors duration-200"
                  aria-label="Cancel upload"
                >
                  {status === 'error' ? 'Remove' : 'Cancel'}
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UploadProgress;
