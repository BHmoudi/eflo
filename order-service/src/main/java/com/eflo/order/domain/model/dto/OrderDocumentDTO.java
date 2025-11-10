package com.eflo.order.domain.model.dto;

import com.eflo.order.domain.entity.DocumentType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDocumentDTO {

    private Long id;
    private Long orderId;
    private DocumentType documentType;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String contentType;
    private String description;
    private LocalDateTime uploadedAt;
    private Long uploadedByUserId;
}
