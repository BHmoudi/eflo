package com.eflo.document.config;

import com.eflo.document.domain.model.DocumentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration for Document Service
 * Configures producers, consumers, topics, and error handling
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:document-service}")
    private String consumerGroupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${kafka.retry.max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${kafka.retry.backoff-interval:1000}")
    private long retryBackoffInterval;

    // ========================================
    // Topic Constants
    // ========================================

    // Document Event Topics
    public static final String TOPIC_DOCUMENT_UPLOADED = "document.uploaded";
    public static final String TOPIC_DOCUMENT_VALIDATED = "document.validated";
    public static final String TOPIC_DOCUMENT_REJECTED = "document.rejected";
    public static final String TOPIC_DOCUMENT_EXPIRED = "document.expired";
    public static final String TOPIC_DOCUMENT_DELETED = "document.deleted";
    public static final String TOPIC_DOCUMENT_ARCHIVED = "document.archived";
    public static final String TOPIC_DOCUMENT_VERSION_CREATED = "document.version.created";
    public static final String TOPIC_DOCUMENT_SCAN_COMPLETED = "document.scan.completed";
    public static final String TOPIC_ALL_DOCUMENTS_VALIDATED = "document.all.validated";

    // Order Event Topics (subscribed)
    public static final String TOPIC_ORDER_CREATED = "order.created";
    public static final String TOPIC_ORDER_DELIVERED = "order.delivered";
    public static final String TOPIC_ORDER_CANCELLED = "order.cancelled";

    // Workflow Event Topics (subscribed)
    public static final String TOPIC_WORKFLOW_STATE_CHANGED = "workflow.state.changed";
    public static final String TOPIC_WORKFLOW_TASK_COMPLETED = "workflow.task.completed";

    // Dead Letter Queue Topics
    public static final String TOPIC_DOCUMENT_DLQ = "document.dlq";
    public static final String TOPIC_ORDER_DLQ = "order.dlq";
    public static final String TOPIC_WORKFLOW_DLQ = "workflow.dlq";

    // ========================================
    // Producer Configuration
    // ========================================

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Reliability settings
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        // Performance settings
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 10);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        // Timeout settings
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);

        // JSON serialization settings
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());

        // Add producer listener for monitoring
        template.setProducerListener(new org.springframework.kafka.support.ProducerListener<String, Object>() {
            public void onSuccess(org.springframework.kafka.support.SendResult<String, Object> result) {
                log.debug("Message sent successfully to topic: {} partition: {} offset: {}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }

            public void onError(org.apache.kafka.clients.producer.ProducerRecord<String, Object> producerRecord,
                              org.apache.kafka.common.KafkaException exception,
                              org.springframework.kafka.support.SendResult<String, Object> result) {
                log.error("Error sending message to topic: {}", producerRecord.topic(), exception);
            }
        });

        return template;
    }

    // ========================================
    // Consumer Configuration
    // ========================================

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        // Deserialization settings with error handling
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // JSON deserialization settings
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.eflo.*");
        configProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");

        // Consumer reliability settings
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        configProps.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);

        // Isolation level for transactional messages
        configProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());

        // Concurrency settings
        factory.setConcurrency(3);

        // Acknowledgment mode
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        // Error handling with retry
        factory.setCommonErrorHandler(defaultErrorHandler());

        // Record filter strategy (optional - can be used to filter messages)
        factory.setRecordFilterStrategy(record -> {
            // Add custom filtering logic if needed
            return false; // false means don't filter out
        });

        return factory;
    }

    // ========================================
    // Error Handling Configuration
    // ========================================

    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        // Configure backoff with fixed interval
        FixedBackOff fixedBackOff = new FixedBackOff(retryBackoffInterval, retryMaxAttempts);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, exception) -> {
            // This is the recovery callback - called after all retries are exhausted
            log.error("Message processing failed after {} retries. Sending to DLQ. Topic: {}, Partition: {}, Offset: {}, Key: {}",
                    retryMaxAttempts,
                    consumerRecord.topic(),
                    consumerRecord.partition(),
                    consumerRecord.offset(),
                    consumerRecord.key(),
                    exception);

            // Send to appropriate DLQ based on topic
            sendToDeadLetterQueue(consumerRecord);
        }, fixedBackOff);

        // Add exceptions that should not be retried
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                com.fasterxml.jackson.core.JsonParseException.class,
                com.fasterxml.jackson.databind.JsonMappingException.class
        );

        // Log retry attempts
        errorHandler.setRetryListeners((record, ex, deliveryAttempt) -> {
            log.warn("Retry attempt {} for message from topic: {} partition: {} offset: {}. Error: {}",
                    deliveryAttempt,
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    ex.getMessage());
        });

        return errorHandler;
    }

    private void sendToDeadLetterQueue(org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record) {
        try {
            String dlqTopic = determineDlqTopic(record.topic());

            Map<String, Object> dlqMessage = new HashMap<>();
            dlqMessage.put("originalTopic", record.topic());
            dlqMessage.put("originalPartition", record.partition());
            dlqMessage.put("originalOffset", record.offset());
            dlqMessage.put("originalKey", record.key());
            dlqMessage.put("originalValue", record.value());
            dlqMessage.put("timestamp", System.currentTimeMillis());
            dlqMessage.put("headers", extractHeaders(record));

            kafkaTemplate().send(dlqTopic, record.key() != null ? record.key().toString() : null, dlqMessage);

            log.info("Message sent to DLQ topic: {}", dlqTopic);
        } catch (Exception e) {
            log.error("Failed to send message to DLQ", e);
        }
    }

    private String determineDlqTopic(String originalTopic) {
        if (originalTopic.startsWith("order.")) {
            return TOPIC_ORDER_DLQ;
        } else if (originalTopic.startsWith("workflow.")) {
            return TOPIC_WORKFLOW_DLQ;
        } else {
            return TOPIC_DOCUMENT_DLQ;
        }
    }

    private Map<String, String> extractHeaders(org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record) {
        Map<String, String> headers = new HashMap<>();
        record.headers().forEach(header -> {
            try {
                headers.put(header.key(), new String(header.value()));
            } catch (Exception e) {
                headers.put(header.key(), "Unable to decode");
            }
        });
        return headers;
    }

    // ========================================
    // Additional Consumer Factories for Specific Event Types
    // ========================================

    @Bean
    public ConsumerFactory<String, Map<String, Object>> orderEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-order");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.eflo.*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> orderKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderEventConsumerFactory());
        factory.setConcurrency(2);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(defaultErrorHandler());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Map<String, Object>> workflowEventConsumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId + "-workflow");
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.eflo.*");
        configProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "java.util.Map");
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return new DefaultKafkaConsumerFactory<>(configProps);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> workflowKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Map<String, Object>> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(workflowEventConsumerFactory());
        factory.setConcurrency(2);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(defaultErrorHandler());
        return factory;
    }
}
