package com.eflo.workflow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration
 *
 * Defines Kafka topics for workflow events.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:kafka:29092}")
    private String bootstrapServers;

    // Topic names
    public static final String WORKFLOW_INSTANCE_EVENTS = "workflow.instance.events";
    public static final String WORKFLOW_STATE_EVENTS = "workflow.state.events";
    public static final String WORKFLOW_TASK_EVENTS = "workflow.task.events";
    public static final String WORKFLOW_DEADLINE_EVENTS = "workflow.deadline.events";
    public static final String WORKFLOW_ESCALATION_EVENTS = "workflow.escalation.events";
    public static final String WORKFLOW_GENERAL_EVENTS = "workflow.general.events";

    // Input topics
    public static final String ORDER_EVENTS = "order.events";
    public static final String DOCUMENT_EVENTS = "document.events";

    @Bean
    public NewTopic workflowInstanceEventsTopic() {
        return TopicBuilder.name(WORKFLOW_INSTANCE_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic workflowStateEventsTopic() {
        return TopicBuilder.name(WORKFLOW_STATE_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic workflowTaskEventsTopic() {
        return TopicBuilder.name(WORKFLOW_TASK_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic workflowDeadlineEventsTopic() {
        return TopicBuilder.name(WORKFLOW_DEADLINE_EVENTS)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic workflowEscalationEventsTopic() {
        return TopicBuilder.name(WORKFLOW_ESCALATION_EVENTS)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic workflowGeneralEventsTopic() {
        return TopicBuilder.name(WORKFLOW_GENERAL_EVENTS)
                .partitions(2)
                .replicas(1)
                .build();
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "workflow-service-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(
                org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
