package com.eflo.document.unit;

import com.eflo.document.domain.entity.Document;
import com.eflo.document.domain.enums.DocumentStatus;
import com.eflo.document.domain.model.DocumentSearchRequest;
import com.eflo.document.domain.repository.DocumentRepository;
import com.eflo.document.service.DocumentSearchService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentSearchService Unit Tests")
class DocumentSearchServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private CriteriaQuery<Document> criteriaQuery;

    @Mock
    private Root<Document> root;

    @Mock
    private TypedQuery<Document> typedQuery;

    @InjectMocks
    private DocumentSearchService searchService;

    @BeforeEach
    void setUp() {
        when(entityManager.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(criteriaBuilder.createQuery(Document.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(Document.class)).thenReturn(root);
    }

    @Test
    @DisplayName("searchDocuments - multiple criteria")
    void testSearchDocuments() {
        // Arrange
        DocumentSearchRequest request = DocumentSearchRequest.builder()
                .searchTerm("invoice")
                .status(DocumentStatus.VALIDATED)
                .build();

        when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());
        when(typedQuery.getSingleResult()).thenReturn(0L);

        // Act
        Page<Document> result = searchService.searchDocuments(request, PageRequest.of(0, 10));

        // Assert
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getOrderDocuments - filtering")
    void testGetOrderDocuments() {
        // Arrange
        when(documentRepository.findByOrderIdOrderByUploadedAtDesc(100L))
                .thenReturn(Collections.emptyList());

        // Act
        List<Document> result = searchService.getOrderDocuments(100L);

        // Assert
        assertThat(result).isNotNull();
        verify(documentRepository).findByOrderIdOrderByUploadedAtDesc(100L);
    }

    @Test
    @DisplayName("getPendingValidation - status filter")
    void testGetPendingValidation() {
        // Arrange
        when(entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        // Act
        List<Document> result = searchService.getPendingValidation();

        // Assert
        assertThat(result).isNotNull();
    }
}
