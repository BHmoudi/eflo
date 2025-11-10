package com.eflo.order.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for file storage operations
 * Implementations can use local filesystem, MinIO, S3, etc.
 */
public interface FileStorageService {

    /**
     * Store a file and return its path
     * @param file the file to store
     * @param directory the directory to store the file in
     * @return the stored file path
     */
    String storeFile(MultipartFile file, String directory);

    /**
     * Load a file as a Resource
     * @param filePath the file path
     * @return the file as a Resource
     */
    Resource loadFileAsResource(String filePath);

    /**
     * Delete a file
     * @param filePath the file path to delete
     */
    void deleteFile(String filePath);

    /**
     * Check if a file exists
     * @param filePath the file path to check
     * @return true if the file exists
     */
    boolean fileExists(String filePath);
}
