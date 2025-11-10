import React from 'react';

export interface FilePreviewProps {
  /** The file to preview */
  file: File;
  /** Preview URL for images */
  preview?: string;
  /** Upload status */
  status?: 'pending' | 'uploading' | 'success' | 'error';
  /** Callback when remove button is clicked */
  onRemove?: () => void;
  /** Callback when download button is clicked */
  onDownload?: () => void;
  /** Show actions (remove, download) */
  showActions?: boolean;
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

const getFileIcon = (fileType: string, fileName: string): React.ReactNode => {
  const extension = fileName.split('.').pop()?.toLowerCase();

  // Image files
  if (fileType.startsWith('image/')) {
    return (
      <svg className="w-full h-full text-blue-500" fill="currentColor" viewBox="0 0 20 20">
        <path
          fillRule="evenodd"
          d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
          clipRule="evenodd"
        />
      </svg>
    );
  }

  // PDF files
  if (fileType === 'application/pdf' || extension === 'pdf') {
    return (
      <svg className="w-full h-full text-red-500" fill="currentColor" viewBox="0 0 20 20">
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z"
          clipRule="evenodd"
        />
      </svg>
    );
  }

  // Word documents
  if (
    fileType === 'application/msword' ||
    fileType === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document' ||
    extension === 'doc' ||
    extension === 'docx'
  ) {
    return (
      <svg className="w-full h-full text-blue-600" fill="currentColor" viewBox="0 0 20 20">
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
          clipRule="evenodd"
        />
      </svg>
    );
  }

  // Excel files
  if (
    fileType === 'application/vnd.ms-excel' ||
    fileType === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
    extension === 'xls' ||
    extension === 'xlsx'
  ) {
    return (
      <svg className="w-full h-full text-green-600" fill="currentColor" viewBox="0 0 20 20">
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z"
          clipRule="evenodd"
        />
      </svg>
    );
  }

  // PowerPoint files
  if (
    fileType === 'application/vnd.ms-powerpoint' ||
    fileType === 'application/vnd.openxmlformats-officedocument.presentationml.presentation' ||
    extension === 'ppt' ||
    extension === 'pptx'
  ) {
    return (
      <svg className="w-full h-full text-orange-600" fill="currentColor" viewBox="0 0 20 20">
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z"
          clipRule="evenodd"
        />
      </svg>
    );
  }

  // Video files
  if (fileType.startsWith('video/')) {
    return (
      <svg className="w-full h-full text-purple-500" fill="currentColor" viewBox="0 0 20 20">
        <path d="M2 6a2 2 0 012-2h6a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6zM14.553 7.106A1 1 0 0014 8v4a1 1 0 00.553.894l2 1A1 1 0 0018 13V7a1 1 0 00-1.447-.894l-2 1z" />
      </svg>
    );
  }

  // Audio files
  if (fileType.startsWith('audio/')) {
    return (
      <svg className="w-full h-full text-pink-500" fill="currentColor" viewBox="0 0 20 20">
        <path d="M18 3a1 1 0 00-1.196-.98l-10 2A1 1 0 006 5v9.114A4.369 4.369 0 005 14c-1.657 0-3 .895-3 2s1.343 2 3 2 3-.895 3-2V7.82l8-1.6v5.894A4.37 4.37 0 0015 12c-1.657 0-3 .895-3 2s1.343 2 3 2 3-.895 3-2V3z" />
      </svg>
    );
  }

  // Archive files
  if (
    extension === 'zip' ||
    extension === 'rar' ||
    extension === '7z' ||
    extension === 'tar' ||
    extension === 'gz'
  ) {
    return (
      <svg className="w-full h-full text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
        <path
          fillRule="evenodd"
          d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V8a2 2 0 00-2-2h-5L9 4H4zm7 5a1 1 0 10-2 0v1H8a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V9z"
          clipRule="evenodd"
        />
      </svg>
    );
  }

  // Default document icon
  return (
    <svg className="w-full h-full text-gray-500" fill="currentColor" viewBox="0 0 20 20">
      <path
        fillRule="evenodd"
        d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4zm2 6a1 1 0 011-1h6a1 1 0 110 2H7a1 1 0 01-1-1zm1 3a1 1 0 100 2h6a1 1 0 100-2H7z"
        clipRule="evenodd"
      />
    </svg>
  );
};

const getStatusBadge = (status?: 'pending' | 'uploading' | 'success' | 'error'): React.ReactNode => {
  if (!status || status === 'pending') return null;

  const badges = {
    uploading: (
      <div className="absolute top-2 right-2 px-2 py-1 rounded-full bg-blue-500 text-white text-xs font-medium flex items-center space-x-1">
        <svg className="w-3 h-3 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path
            className="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        <span>Uploading</span>
      </div>
    ),
    success: (
      <div className="absolute top-2 right-2 px-2 py-1 rounded-full bg-green-500 text-white text-xs font-medium flex items-center space-x-1">
        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
            clipRule="evenodd"
          />
        </svg>
        <span>Success</span>
      </div>
    ),
    error: (
      <div className="absolute top-2 right-2 px-2 py-1 rounded-full bg-red-500 text-white text-xs font-medium flex items-center space-x-1">
        <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
          <path
            fillRule="evenodd"
            d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
            clipRule="evenodd"
          />
        </svg>
        <span>Error</span>
      </div>
    ),
  };

  return badges[status];
};

export const FilePreview: React.FC<FilePreviewProps> = ({
  file,
  preview,
  status,
  onRemove,
  onDownload,
  showActions = true,
  className = '',
}) => {
  const isImage = file.type.startsWith('image/');

  return (
    <div
      className={`
        relative group rounded-lg border border-stroke dark:border-strokedark
        bg-white dark:bg-boxdark overflow-hidden transition-all duration-200
        hover:shadow-lg hover:border-primary dark:hover:border-primary
        ${className}
      `}
    >
      {/* Status Badge */}
      {getStatusBadge(status)}

      {/* Preview Area */}
      <div className="aspect-square bg-gray-50 dark:bg-meta-4 flex items-center justify-center p-4">
        {preview && isImage ? (
          <img
            src={preview}
            alt={file.name}
            className="w-full h-full object-cover rounded"
            loading="lazy"
          />
        ) : (
          <div className="w-20 h-20">{getFileIcon(file.type, file.name)}</div>
        )}
      </div>

      {/* File Info */}
      <div className="p-3 border-t border-stroke dark:border-strokedark">
        <h4
          className="text-sm font-medium text-black dark:text-white truncate"
          title={file.name}
        >
          {file.name}
        </h4>
        <p className="text-xs text-gray-500 dark:text-gray-400 mt-1">
          {formatFileSize(file.size)}
        </p>
      </div>

      {/* Actions Overlay */}
      {showActions && (
        <div
          className="
            absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100
            transition-opacity duration-200 flex items-center justify-center space-x-2
          "
        >
          {onDownload && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onDownload();
              }}
              className="p-2 rounded-full bg-white hover:bg-gray-100 transition-colors duration-200"
              title="Download file"
              aria-label="Download file"
            >
              <svg
                className="w-5 h-5 text-gray-700"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"
                />
              </svg>
            </button>
          )}

          {onRemove && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onRemove();
              }}
              className="p-2 rounded-full bg-red-500 hover:bg-red-600 transition-colors duration-200"
              title="Remove file"
              aria-label="Remove file"
            >
              <svg
                className="w-5 h-5 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                />
              </svg>
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default FilePreview;
