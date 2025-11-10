package com.eflo.document.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * File Processor for processing uploaded files.
 * Provides file hashing, metadata extraction, thumbnail generation, and compression.
 *
 * @author Eflo Platform
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessor {

    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;
    private static final long COMPRESSION_THRESHOLD = 5 * 1024 * 1024; // 5MB
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "bmp"};

    private final Tika tika = new Tika();

    /**
     * Calculates the SHA-256 hash of a file.
     *
     * @param file the file to hash
     * @return hex string of the file hash
     * @throws IOException if reading the file fails
     */
    public String calculateFileHash(MultipartFile file) throws IOException {
        log.debug("Calculating SHA-256 hash for file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            return calculateHash(inputStream);
        }
    }

    /**
     * Calculates the SHA-256 hash from an InputStream.
     *
     * @param inputStream the input stream
     * @return hex string of the hash
     * @throws IOException if reading fails
     */
    public String calculateHash(InputStream inputStream) throws IOException {
        log.debug("Calculating SHA-256 hash from input stream");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String hash = hexString.toString();
            log.debug("Calculated hash: {}", hash);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available: {}", e.getMessage());
            throw new IOException("Failed to calculate file hash", e);
        }
    }

    /**
     * Extracts metadata from a file using Apache Tika.
     *
     * @param file the file to extract metadata from
     * @return map of metadata key-value pairs
     */
    public Map<String, String> extractFileMetadata(MultipartFile file) {
        log.debug("Extracting metadata for file: {}", file.getOriginalFilename());

        Map<String, String> metadataMap = new HashMap<>();

        try (InputStream inputStream = file.getInputStream()) {
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // No limit on content
            ParseContext context = new ParseContext();

            // Parse the file
            parser.parse(inputStream, handler, metadata, context);

            // Extract metadata
            String[] metadataNames = metadata.names();
            for (String name : metadataNames) {
                String value = metadata.get(name);
                if (value != null && !value.isEmpty()) {
                    metadataMap.put(name, value);
                    log.trace("Metadata: {} = {}", name, value);
                }
            }

            // Add basic file information
            metadataMap.put("originalFilename", file.getOriginalFilename());
            metadataMap.put("fileSize", String.valueOf(file.getSize()));
            metadataMap.put("contentType", file.getContentType());

            log.info("Extracted {} metadata fields for file: {}",
                    metadataMap.size(), file.getOriginalFilename());

        } catch (Exception e) {
            log.error("Failed to extract metadata for file {}: {}",
                    file.getOriginalFilename(), e.getMessage());

            // Return basic metadata even if full extraction fails
            metadataMap.put("originalFilename", file.getOriginalFilename());
            metadataMap.put("fileSize", String.valueOf(file.getSize()));
            metadataMap.put("contentType", file.getContentType());
            metadataMap.put("extractionError", e.getMessage());
        }

        return metadataMap;
    }

    /**
     * Generates a thumbnail for an image file.
     *
     * @param file the image file
     * @return byte array of the thumbnail image (JPEG format), or null if not an image
     * @throws IOException if thumbnail generation fails
     */
    public byte[] generateThumbnail(MultipartFile file) throws IOException {
        log.debug("Generating thumbnail for file: {}", file.getOriginalFilename());

        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();

        // Check if file is an image
        if (!isImageFile(extension)) {
            log.debug("File is not an image, skipping thumbnail generation: {}", extension);
            return null;
        }

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(inputStream);

            if (originalImage == null) {
                log.warn("Failed to read image, cannot generate thumbnail: {}", file.getOriginalFilename());
                return null;
            }

            return generateThumbnailFromImage(originalImage);

        } catch (Exception e) {
            log.error("Failed to generate thumbnail for file {}: {}",
                    file.getOriginalFilename(), e.getMessage());
            throw new IOException("Thumbnail generation failed", e);
        }
    }

    /**
     * Generates a thumbnail from a BufferedImage.
     *
     * @param originalImage the original image
     * @return byte array of the thumbnail
     * @throws IOException if writing the thumbnail fails
     */
    public byte[] generateThumbnailFromImage(BufferedImage originalImage) throws IOException {
        log.debug("Generating thumbnail from BufferedImage");

        // Calculate dimensions maintaining aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        int thumbnailWidth = THUMBNAIL_WIDTH;
        int thumbnailHeight = THUMBNAIL_HEIGHT;

        double aspectRatio = (double) originalWidth / originalHeight;

        if (originalWidth > originalHeight) {
            thumbnailHeight = (int) (thumbnailWidth / aspectRatio);
        } else {
            thumbnailWidth = (int) (thumbnailHeight * aspectRatio);
        }

        // Create thumbnail
        BufferedImage thumbnail = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = thumbnail.createGraphics();

        // Set rendering hints for better quality
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, thumbnailWidth, thumbnailHeight, null);
        graphics.dispose();

        // Convert to byte array
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(thumbnail, "jpg", outputStream);
            byte[] thumbnailBytes = outputStream.toByteArray();
            log.debug("Generated thumbnail: {} x {} pixels, size: {} bytes",
                    thumbnailWidth, thumbnailHeight, thumbnailBytes.length);
            return thumbnailBytes;
        }
    }

    /**
     * Checks if compression is needed for a file.
     *
     * @param fileSize the file size in bytes
     * @return true if file should be compressed
     */
    public boolean shouldCompress(long fileSize) {
        boolean shouldCompress = fileSize > COMPRESSION_THRESHOLD;
        log.debug("File size: {} bytes, should compress: {}", fileSize, shouldCompress);
        return shouldCompress;
    }

    /**
     * Compresses a file if it exceeds the compression threshold.
     * Note: This is a placeholder implementation. For production, you would implement
     * actual compression logic based on file type.
     *
     * @param file the file to compress
     * @return compressed file bytes, or original if compression is not needed
     * @throws IOException if compression fails
     */
    public byte[] compressFile(MultipartFile file) throws IOException {
        log.debug("Checking if file needs compression: {}", file.getOriginalFilename());

        if (!shouldCompress(file.getSize())) {
            log.debug("File size is below compression threshold, skipping compression");
            return file.getBytes();
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase();

        // For images, we can re-encode with lower quality
        if (isImageFile(extension)) {
            return compressImage(file);
        }

        // For other files, return original (in production, implement ZIP compression)
        log.debug("No compression implemented for file type: {}", extension);
        return file.getBytes();
    }

    /**
     * Compresses an image file by re-encoding with lower quality.
     *
     * @param file the image file
     * @return compressed image bytes
     * @throws IOException if compression fails
     */
    private byte[] compressImage(MultipartFile file) throws IOException {
        log.debug("Compressing image: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);

            if (image == null) {
                log.warn("Failed to read image for compression, returning original");
                return file.getBytes();
            }

            // Convert to JPEG with quality 0.8
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(image, "jpg", outputStream);
                byte[] compressedBytes = outputStream.toByteArray();

                long originalSize = file.getSize();
                long compressedSize = compressedBytes.length;
                double compressionRatio = (1.0 - (double) compressedSize / originalSize) * 100;

                log.info("Image compressed: original={} bytes, compressed={} bytes, ratio={:.2f}%",
                        originalSize, compressedSize, compressionRatio);

                return compressedBytes;
            }
        } catch (Exception e) {
            log.error("Image compression failed, returning original: {}", e.getMessage());
            return file.getBytes();
        }
    }

    /**
     * Checks if a file extension represents an image.
     *
     * @param extension the file extension
     * @return true if the extension is an image type
     */
    private boolean isImageFile(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }

        String lowerExtension = extension.toLowerCase();
        for (String imageExt : IMAGE_EXTENSIONS) {
            if (imageExt.equals(lowerExtension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates an InputStream from byte array.
     *
     * @param data the byte array
     * @return InputStream
     */
    public InputStream createInputStream(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    /**
     * Detects the content type of a file.
     *
     * @param file the file
     * @return detected content type
     */
    public String detectContentType(MultipartFile file) {
        log.debug("Detecting content type for file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            String contentType = tika.detect(inputStream, file.getOriginalFilename());
            log.debug("Detected content type: {}", contentType);
            return contentType;
        } catch (IOException e) {
            log.error("Failed to detect content type: {}", e.getMessage());
            return file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        }
    }

    /**
     * Gets basic file information.
     *
     * @param file the file
     * @return map of file information
     */
    public Map<String, Object> getFileInfo(MultipartFile file) {
        Map<String, Object> info = new HashMap<>();
        info.put("originalFilename", file.getOriginalFilename());
        info.put("size", file.getSize());
        info.put("contentType", file.getContentType());
        info.put("extension", FilenameUtils.getExtension(file.getOriginalFilename()));
        info.put("baseName", FilenameUtils.getBaseName(file.getOriginalFilename()));

        log.debug("File info: {}", info);
        return info;
    }
}
