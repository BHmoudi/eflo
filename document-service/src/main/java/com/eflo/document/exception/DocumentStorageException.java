package com.eflo.document.exception;

/**
 * Exception thrown when document storage operations fail.
 * This includes failures in MinIO operations such as upload, download,
 * delete, and bucket management.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
public class DocumentStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String operation;
    private final String bucketName;
    private final String objectName;

    /**
     * Constructs a new DocumentStorageException with the specified detail message.
     *
     * @param message the detail message
     */
    public DocumentStorageException(String message) {
        super(message);
        this.operation = null;
        this.bucketName = null;
        this.objectName = null;
    }

    /**
     * Constructs a new DocumentStorageException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public DocumentStorageException(String message, Throwable cause) {
        super(message, cause);
        this.operation = null;
        this.bucketName = null;
        this.objectName = null;
    }

    /**
     * Constructs a new DocumentStorageException with detailed context.
     *
     * @param message the detail message
     * @param operation the storage operation that failed
     * @param bucketName the bucket name involved in the operation
     * @param objectName the object name involved in the operation
     */
    public DocumentStorageException(String message, String operation, String bucketName, String objectName) {
        super(message);
        this.operation = operation;
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    /**
     * Constructs a new DocumentStorageException with detailed context and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     * @param operation the storage operation that failed
     * @param bucketName the bucket name involved in the operation
     * @param objectName the object name involved in the operation
     */
    public DocumentStorageException(String message, Throwable cause, String operation, String bucketName, String objectName) {
        super(message, cause);
        this.operation = operation;
        this.bucketName = bucketName;
        this.objectName = objectName;
    }

    /**
     * Gets the storage operation that failed.
     *
     * @return the operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Gets the bucket name involved in the failed operation.
     *
     * @return the bucket name
     */
    public String getBucketName() {
        return bucketName;
    }

    /**
     * Gets the object name involved in the failed operation.
     *
     * @return the object name
     */
    public String getObjectName() {
        return objectName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (operation != null || bucketName != null || objectName != null) {
            sb.append(" [");
            if (operation != null) {
                sb.append("operation=").append(operation);
            }
            if (bucketName != null) {
                if (operation != null) sb.append(", ");
                sb.append("bucket=").append(bucketName);
            }
            if (objectName != null) {
                if (operation != null || bucketName != null) sb.append(", ");
                sb.append("object=").append(objectName);
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
