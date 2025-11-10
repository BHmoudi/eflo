import React, { useCallback, useState, useRef } from 'react';
import { FilePreview } from './FilePreview';
import { UploadProgress } from './UploadProgress';

export interface FileUploadFile {
  id: string;
  file: File;
  preview?: string;
  progress: number;
  status: 'pending' | 'uploading' | 'success' | 'error';
  error?: string;
}

export interface FileUploadProps {
  /** Accept specific file types (e.g., "image/*", ".pdf,.doc") */
  accept?: string;
  /** Maximum file size in bytes */
  maxSize?: number;
  /** Allow multiple files */
  multiple?: boolean;
  /** Maximum number of files */
  maxFiles?: number;
  /** Callback when files are selected */
  onFilesSelected?: (files: File[]) => void;
  /** Callback for file upload with progress tracking */
  onUpload?: (file: File, onProgress: (progress: number) => void) => Promise<void>;
  /** Callback when file is removed */
  onRemove?: (fileId: string) => void;
  /** Custom upload button text */
  uploadButtonText?: string;
  /** Show preview thumbnails */
  showPreview?: boolean;
  /** Disabled state */
  disabled?: boolean;
  /** Custom className */
  className?: string;
}

export const FileUpload: React.FC<FileUploadProps> = ({
  accept,
  maxSize = 10 * 1024 * 1024, // 10MB default
  multiple = true,
  maxFiles,
  onFilesSelected,
  onUpload,
  onRemove,
  uploadButtonText = 'Upload Files',
  showPreview = true,
  disabled = false,
  className = '',
}) => {
  const [files, setFiles] = useState<FileUploadFile[]>([]);
  const [isDragOver, setIsDragOver] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const dragCounterRef = useRef(0);

  const generateFileId = () => `file-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  };

  const validateFile = (file: File): string | null => {
    // Check file size
    if (file.size > maxSize) {
      return `File size exceeds maximum allowed size of ${formatFileSize(maxSize)}`;
    }

    // Check file type if accept is specified
    if (accept) {
      const acceptedTypes = accept.split(',').map(type => type.trim());
      const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();
      const fileMimeType = file.type;

      const isAccepted = acceptedTypes.some(type => {
        if (type.startsWith('.')) {
          return fileExtension === type.toLowerCase();
        }
        if (type.endsWith('/*')) {
          const baseType = type.split('/')[0];
          return fileMimeType.startsWith(baseType + '/');
        }
        return fileMimeType === type;
      });

      if (!isAccepted) {
        return `File type not accepted. Allowed types: ${accept}`;
      }
    }

    // Check max files limit
    if (maxFiles && files.length >= maxFiles) {
      return `Maximum ${maxFiles} file(s) allowed`;
    }

    return null;
  };

  const createFilePreview = (file: File): Promise<string | undefined> => {
    return new Promise((resolve) => {
      if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onloadend = () => resolve(reader.result as string);
        reader.onerror = () => resolve(undefined);
        reader.readAsDataURL(file);
      } else {
        resolve(undefined);
      }
    });
  };

  const processFiles = async (fileList: FileList | File[]) => {
    const newErrors: string[] = [];
    const validFiles: File[] = [];
    const filesArray = Array.from(fileList);

    // Validate files
    for (const file of filesArray) {
      const error = validateFile(file);
      if (error) {
        newErrors.push(`${file.name}: ${error}`);
      } else {
        validFiles.push(file);
      }
    }

    setErrors(newErrors);

    if (validFiles.length === 0) return;

    // Create file objects with previews
    const newFileObjects = await Promise.all(
      validFiles.map(async (file) => ({
        id: generateFileId(),
        file,
        preview: await createFilePreview(file),
        progress: 0,
        status: 'pending' as const,
      }))
    );

    setFiles(prev => multiple ? [...prev, ...newFileObjects] : newFileObjects);

    // Notify parent component
    if (onFilesSelected) {
      onFilesSelected(validFiles);
    }

    // Auto-upload if onUpload is provided
    if (onUpload) {
      newFileObjects.forEach(fileObj => handleUpload(fileObj.id));
    }
  };

  const handleUpload = async (fileId: string) => {
    if (!onUpload) return;

    setFiles(prev =>
      prev.map(f =>
        f.id === fileId ? { ...f, status: 'uploading' as const } : f
      )
    );

    const fileObj = files.find(f => f.id === fileId);
    if (!fileObj) return;

    try {
      await onUpload(fileObj.file, (progress) => {
        setFiles(prev =>
          prev.map(f =>
            f.id === fileId ? { ...f, progress } : f
          )
        );
      });

      setFiles(prev =>
        prev.map(f =>
          f.id === fileId ? { ...f, status: 'success' as const, progress: 100 } : f
        )
      );
    } catch (error) {
      setFiles(prev =>
        prev.map(f =>
          f.id === fileId
            ? {
                ...f,
                status: 'error' as const,
                error: error instanceof Error ? error.message : 'Upload failed',
              }
            : f
        )
      );
    }
  };

  const handleRemove = (fileId: string) => {
    setFiles(prev => prev.filter(f => f.id !== fileId));
    if (onRemove) {
      onRemove(fileId);
    }
  };

  const handleDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounterRef.current++;
    if (e.dataTransfer.items && e.dataTransfer.items.length > 0) {
      setIsDragOver(true);
    }
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    dragCounterRef.current--;
    if (dragCounterRef.current === 0) {
      setIsDragOver(false);
    }
  }, []);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      e.stopPropagation();
      setIsDragOver(false);
      dragCounterRef.current = 0;

      if (disabled) return;

      const droppedFiles = e.dataTransfer.files;
      if (droppedFiles.length > 0) {
        processFiles(droppedFiles);
      }
    },
    [disabled, processFiles]
  );

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      processFiles(e.target.files);
      // Reset input value to allow selecting the same file again
      e.target.value = '';
    }
  };

  const handleBrowseClick = () => {
    if (!disabled) {
      fileInputRef.current?.click();
    }
  };

  return (
    <div className={`file-upload-container ${className}`}>
      {/* Drop Zone */}
      <div
        className={`
          relative rounded-lg border-2 border-dashed transition-all duration-200
          ${isDragOver
            ? 'border-primary bg-primary/5 scale-[1.02]'
            : 'border-stroke dark:border-strokedark bg-white dark:bg-boxdark'
          }
          ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer hover:border-primary hover:bg-gray-50 dark:hover:bg-meta-4'}
        `}
        onDragEnter={handleDragEnter}
        onDragLeave={handleDragLeave}
        onDragOver={handleDragOver}
        onDrop={handleDrop}
        onClick={handleBrowseClick}
        role="button"
        tabIndex={disabled ? -1 : 0}
        aria-label="File upload drop zone"
        aria-disabled={disabled}
      >
        <div className="flex flex-col items-center justify-center p-12 space-y-4">
          {/* Upload Icon */}
          <div className={`
            rounded-full p-4 transition-colors duration-200
            ${isDragOver
              ? 'bg-primary/10'
              : 'bg-gray-100 dark:bg-meta-4'
            }
          `}>
            <svg
              className={`w-12 h-12 transition-colors duration-200 ${
                isDragOver ? 'text-primary' : 'text-gray-400 dark:text-gray-500'
              }`}
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
          </div>

          {/* Text Content */}
          <div className="text-center space-y-2">
            <p className="text-lg font-semibold text-black dark:text-white">
              {isDragOver ? 'Drop files here' : 'Drag and drop files here'}
            </p>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              or click to browse from your computer
            </p>
            {accept && (
              <p className="text-xs text-gray-400 dark:text-gray-500">
                Accepted formats: {accept}
              </p>
            )}
            <p className="text-xs text-gray-400 dark:text-gray-500">
              Maximum file size: {formatFileSize(maxSize)}
              {maxFiles && ` • Maximum ${maxFiles} file(s)`}
            </p>
          </div>
        </div>

        {/* Hidden File Input */}
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          accept={accept}
          multiple={multiple}
          disabled={disabled}
          onChange={handleFileInputChange}
          aria-label="File input"
        />
      </div>

      {/* Error Messages */}
      {errors.length > 0 && (
        <div className="mt-4 space-y-2" role="alert" aria-live="polite">
          {errors.map((error, index) => (
            <div
              key={index}
              className="flex items-start space-x-2 p-3 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800"
            >
              <svg
                className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5"
                fill="currentColor"
                viewBox="0 0 20 20"
                aria-hidden="true"
              >
                <path
                  fillRule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                  clipRule="evenodd"
                />
              </svg>
              <p className="text-sm text-red-700 dark:text-red-300">{error}</p>
            </div>
          ))}
        </div>
      )}

      {/* File List */}
      {files.length > 0 && (
        <div className="mt-6 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-lg font-semibold text-black dark:text-white">
              {files.length} {files.length === 1 ? 'File' : 'Files'}
            </h3>
            {files.some(f => f.status === 'pending') && onUpload && (
              <button
                onClick={() => files.filter(f => f.status === 'pending').forEach(f => handleUpload(f.id))}
                className="px-4 py-2 text-sm font-medium text-white bg-primary hover:bg-primary/90 rounded-lg transition-colors duration-200"
              >
                {uploadButtonText}
              </button>
            )}
          </div>

          {/* Preview Grid */}
          {showPreview && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {files.map(fileObj => (
                <FilePreview
                  key={fileObj.id}
                  file={fileObj.file}
                  preview={fileObj.preview}
                  status={fileObj.status}
                  onRemove={() => handleRemove(fileObj.id)}
                />
              ))}
            </div>
          )}

          {/* Progress List */}
          <div className="space-y-3">
            {files.map(fileObj => (
              <UploadProgress
                key={fileObj.id}
                fileName={fileObj.file.name}
                fileSize={fileObj.file.size}
                progress={fileObj.progress}
                status={fileObj.status}
                error={fileObj.error}
                onCancel={() => handleRemove(fileObj.id)}
                onRetry={() => handleUpload(fileObj.id)}
              />
            ))}
          </div>
        </div>
      )}

      {/* Empty State */}
      {files.length === 0 && (
        <div className="mt-4 text-center py-8">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            No files selected
          </p>
        </div>
      )}
    </div>
  );
};

export default FileUpload;
