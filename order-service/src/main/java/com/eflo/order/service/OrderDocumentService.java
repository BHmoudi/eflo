package com.eflo.order.service;

import com.eflo.order.domain.entity.DocumentType;
import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.entity.OrderDocument;
import com.eflo.order.domain.model.dto.OrderDocumentDTO;
import com.eflo.order.domain.repository.OrderDocumentRepository;
import com.eflo.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDocumentService {

    private final OrderDocumentRepository documentRepository;
    private final OrderRepository orderRepository;
    private final FileStorageService fileStorageService;

    /**
     * Upload a document for an order
     */
    @Transactional
    public OrderDocumentDTO uploadDocument(Long orderId,
                                          MultipartFile file,
                                          DocumentType documentType,
                                          String description,
                                          Long userId) {
        log.info("Uploading document for order: {}, type: {}", orderId, documentType);

        // Validate order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }

        // Check file size (max 10MB)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds maximum limit of 10MB");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new RuntimeException("File type not allowed: " + contentType);
        }

        // Store file
        String directory = "orders/" + orderId + "/documents";
        String filePath = fileStorageService.storeFile(file, directory);

        // Create document entity
        OrderDocument document = OrderDocument.builder()
                .order(order)
                .documentType(documentType)
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .contentType(contentType)
                .description(description)
                .uploadedByUserId(userId)
                .build();

        document = documentRepository.save(document);

        log.info("Document uploaded successfully: {}", document.getId());
        return toDTO(document);
    }

    /**
     * Get all documents for an order
     */
    @Transactional(readOnly = true)
    public List<OrderDocumentDTO> getOrderDocuments(Long orderId) {
        log.debug("Fetching documents for order: {}", orderId);

        // Validate order exists
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found: " + orderId);
        }

        List<OrderDocument> documents = documentRepository.findByOrderId(orderId);
        return documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific document
     */
    @Transactional(readOnly = true)
    public OrderDocumentDTO getDocument(Long orderId, Long documentId) {
        log.debug("Fetching document: {} for order: {}", documentId, orderId);

        OrderDocument document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        return toDTO(document);
    }

    /**
     * Download a document
     */
    @Transactional(readOnly = true)
    public Resource downloadDocument(Long orderId, Long documentId) {
        log.info("Downloading document: {} for order: {}", documentId, orderId);

        OrderDocument document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        Resource resource = fileStorageService.loadFileAsResource(document.getFilePath());
        log.info("Document downloaded successfully: {}", document.getFileName());
        return resource;
    }

    /**
     * Delete a document
     */
    @Transactional
    public void deleteDocument(Long orderId, Long documentId, Long userId) {
        log.info("Deleting document: {} for order: {}", documentId, orderId);

        OrderDocument document = documentRepository.findByIdAndOrderId(documentId, orderId)
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));

        // Soft delete
        document.setDeletedAt(LocalDateTime.now());
        document.setDeletedByUserId(userId);
        documentRepository.save(document);

        // Optionally delete physical file
        try {
            fileStorageService.deleteFile(document.getFilePath());
        } catch (Exception e) {
            log.warn("Failed to delete physical file: {}", document.getFilePath(), e);
        }

        log.info("Document deleted successfully: {}", documentId);
    }

    /**
     * Get documents by type
     */
    @Transactional(readOnly = true)
    public List<OrderDocumentDTO> getDocumentsByType(Long orderId, DocumentType documentType) {
        log.debug("Fetching documents for order: {} with type: {}", orderId, documentType);

        List<OrderDocument> documents = documentRepository.findByOrderIdAndDocumentType(orderId, documentType);
        return documents.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private OrderDocumentDTO toDTO(OrderDocument document) {
        return OrderDocumentDTO.builder()
                .id(document.getId())
                .orderId(document.getOrder().getId())
                .documentType(document.getDocumentType())
                .fileName(document.getFileName())
                .filePath(document.getFilePath())
                .fileSize(document.getFileSize())
                .contentType(document.getContentType())
                .description(document.getDescription())
                .uploadedAt(document.getUploadedAt())
                .uploadedByUserId(document.getUploadedByUserId())
                .build();
    }

    /**
     * Check if content type is allowed
     */
    private boolean isAllowedContentType(String contentType) {
        return contentType.equals("application/pdf") ||
               contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("application/msword") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
               contentType.equals("application/vnd.ms-excel") ||
               contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
