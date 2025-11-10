package com.eflo.docgen.kafka;

import com.eflo.docgen.domain.entity.DocumentTemplate;
import com.eflo.docgen.domain.repository.DocumentTemplateRepository;
import com.eflo.docgen.service.DocumentGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Workflow Event Listener
 * Auto-generates documents when workflow reaches configured states
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowEventListener {

    private final DocumentGenerationService generationService;
    private final DocumentTemplateRepository templateRepository;

    @KafkaListener(topics = "workflow.events", groupId = "docgen-service-group")
    public void handleWorkflowEvent(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            log.debug("Received workflow event: {}", eventType);

            if ("STATE_CHANGED".equals(eventType)) {
                handleStateChanged(event);
            }
        } catch (Exception e) {
            log.error("Error processing workflow event", e);
        }
    }

    private void handleStateChanged(Map<String, Object> event) {
        Long orderId = extractLong(event.get("orderId"));
        Long instanceId = extractLong(event.get("instanceId"));
        String stateCode = (String) event.get("newStateCode");

        if (orderId == null || stateCode == null) {
            return;
        }

        // Find templates configured to auto-generate on this state
        List<DocumentTemplate> templates = templateRepository.findByAutoGenerateOnState(stateCode);

        for (DocumentTemplate template : templates) {
            try {
                log.info("Auto-generating document: template={}, order={}, state={}",
                        template.getTemplateCode(), orderId, stateCode);

                generationService.generateDocument(
                        template.getTemplateCode(),
                        orderId,
                        instanceId,
                        "AUTO_WORKFLOW");

            } catch (Exception e) {
                log.error("Failed to auto-generate document: template={}, order={}",
                        template.getTemplateCode(), orderId, e);
            }
        }
    }

    private Long extractLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
