package com.eflo.order.integration;

import com.eflo.order.domain.entity.Order;
import com.eflo.order.domain.model.event.OrderEvent;
import com.eflo.order.service.EventPublisherService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(
    partitions = 1,
    topics = {
        "orders.created",
        "orders.updated",
        "orders.status-changed",
        "orders.validated",
        "orders.delivered",
        "orders.cancelled",
        "orders.price-changed",
        "orders.data-changed"
    }
)
@ActiveProfiles("test")
@DisplayName("Kafka Integration Tests - Event Publishing and Consumption")
class KafkaIntegrationTest {

    @Autowired
    private EventPublisherService eventPublisherService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    private BlockingQueue<ConsumerRecord<String, OrderEvent>> consumerRecords;
    private KafkaMessageListenerContainer<String, OrderEvent> container;

    private Order testOrder;
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        consumerRecords = new LinkedBlockingQueue<>();

        // Setup test order
        testOrder = Order.builder()
                .id(1L)
                .orderNumber("VN-12345678-ABC123")
                .orderType(Order.OrderType.VN)
                .status(Order.OrderStatus.DRAFT)
                .customerId(100L)
                .businessUnitId(10L)
                .salespersonId(50L)
                .basePrice(BigDecimal.valueOf(25000.00))
                .totalAmount(BigDecimal.valueOf(28000.00))
                .marginPercentage(BigDecimal.valueOf(10.5))
                .build();
    }

    @Test
    @DisplayName("Publish Order Created Event - Successfully Published to Kafka")
    void publishOrderCreated_SuccessfullyPublished() throws Exception {
        // Arrange
        setupConsumer("orders.created");

        // Act
        eventPublisherService.publishOrderCreated(testOrder, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo("VN-12345678-ABC123");
        assertThat(record.value()).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(event.getOrderId()).isEqualTo(1L);
        assertThat(event.getOrderNumber()).isEqualTo("VN-12345678-ABC123");
        assertThat(event.getOrderType()).isEqualTo(Order.OrderType.VN);
        assertThat(event.getStatus()).isEqualTo(Order.OrderStatus.DRAFT);
        assertThat(event.getCustomerId()).isEqualTo(100L);
        assertThat(event.getSalespersonId()).isEqualTo(50L);
        assertThat(event.getBusinessUnitId()).isEqualTo(10L);
        assertThat(event.getTriggeredByUserId()).isEqualTo(TEST_USER_ID);
        assertThat(event.getSource()).isEqualTo("order-service");
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getTimestamp()).isNotNull();
        assertThat(event.getEventData()).containsKey("orderCreated");

        stopConsumer();
    }

    @Test
    @DisplayName("Publish Order Updated Event - Successfully Published to Kafka")
    void publishOrderUpdated_SuccessfullyPublished() throws Exception {
        // Arrange
        setupConsumer("orders.updated");

        // Act
        eventPublisherService.publishOrderUpdated(testOrder, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_UPDATED");
        assertThat(event.getOrderNumber()).isEqualTo("VN-12345678-ABC123");
        assertThat(event.getEventData()).containsKey("orderUpdated");

        stopConsumer();
    }

    @Test
    @DisplayName("Publish Order Status Changed Event - Contains Status Transition Data")
    void publishOrderStatusChanged_ContainsStatusTransitionData() throws Exception {
        // Arrange
        setupConsumer("orders.status-changed");
        Order.OrderStatus previousStatus = Order.OrderStatus.DRAFT;
        Order.OrderStatus newStatus = Order.OrderStatus.PENDING;

        // Act
        eventPublisherService.publishOrderStatusChanged(testOrder, previousStatus, newStatus, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_STATUS_CHANGED");
        assertThat(event.getEventData()).containsKey("previousStatus");
        assertThat(event.getEventData()).containsKey("newStatus");
        assertThat(event.getEventData().get("previousStatus")).isEqualTo("DRAFT");
        assertThat(event.getEventData().get("newStatus")).isEqualTo("PENDING");

        stopConsumer();
    }

    @Test
    @DisplayName("Publish Order Validated Event - Successfully Published")
    void publishOrderValidated_SuccessfullyPublished() throws Exception {
        // Arrange
        setupConsumer("orders.validated");

        // Act
        eventPublisherService.publishOrderValidated(testOrder, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_VALIDATED");
        assertThat(event.getEventData()).containsKey("validated");

        stopConsumer();
    }

    @Test
    @DisplayName("Publish Order Delivered Event - Contains Delivery Information")
    void publishOrderDelivered_ContainsDeliveryInformation() throws Exception {
        // Arrange
        setupConsumer("orders.delivered");

        // Act
        eventPublisherService.publishOrderDelivered(testOrder, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_DELIVERED");
        assertThat(event.getEventData()).containsKey("delivered");
        assertThat(event.getEventData()).containsKey("deliveryDate");

        stopConsumer();
    }

    @Test
    @DisplayName("Publish Order Cancelled Event - Contains Cancellation Reason")
    void publishOrderCancelled_ContainsCancellationReason() throws Exception {
        // Arrange
        setupConsumer("orders.cancelled");
        String reason = "Customer requested cancellation";

        // Act
        eventPublisherService.publishOrderCancelled(testOrder, TEST_USER_ID, reason);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_CANCELLED");
        assertThat(event.getEventData()).containsKey("cancelled");
        assertThat(event.getEventData()).containsKey("reason");
        assertThat(event.getEventData().get("reason")).isEqualTo(reason);

        stopConsumer();
    }

    @Test
    @DisplayName("Publish Order Price Changed Event - Contains Price Information")
    void publishOrderPriceChanged_ContainsPriceInformation() throws Exception {
        // Arrange
        setupConsumer("orders.price-changed");

        // Act
        eventPublisherService.publishOrderPriceChanged(testOrder, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_PRICE_CHANGED");
        assertThat(event.getEventData()).containsKey("totalAmount");
        assertThat(event.getEventData()).containsKey("marginPercentage");

        stopConsumer();
    }

    @Test
    @DisplayName("Publish Order Data Changed Event - Contains Change Type")
    void publishOrderDataChanged_ContainsChangeType() throws Exception {
        // Arrange
        setupConsumer("orders.data-changed");
        String changeType = "CUSTOMER_INFO_UPDATED";

        // Act
        eventPublisherService.publishOrderDataChanged(testOrder, TEST_USER_ID, changeType);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();

        OrderEvent event = record.value();
        assertThat(event.getEventType()).isEqualTo("ORDER_DATA_CHANGED");
        assertThat(event.getEventData()).containsKey("changeType");
        assertThat(event.getEventData().get("changeType")).isEqualTo(changeType);

        stopConsumer();
    }

    @Test
    @DisplayName("Multiple Events - All Successfully Published in Order")
    void multipleEvents_AllSuccessfullyPublished() throws Exception {
        // Arrange
        setupConsumer("orders.created", "orders.updated", "orders.status-changed");

        // Act
        eventPublisherService.publishOrderCreated(testOrder, TEST_USER_ID);
        eventPublisherService.publishOrderUpdated(testOrder, TEST_USER_ID);
        eventPublisherService.publishOrderStatusChanged(testOrder,
            Order.OrderStatus.DRAFT, Order.OrderStatus.PENDING, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record1 = consumerRecords.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, OrderEvent> record2 = consumerRecords.poll(10, TimeUnit.SECONDS);
        ConsumerRecord<String, OrderEvent> record3 = consumerRecords.poll(10, TimeUnit.SECONDS);

        assertThat(record1).isNotNull();
        assertThat(record2).isNotNull();
        assertThat(record3).isNotNull();

        assertThat(record1.value().getEventType()).isEqualTo("ORDER_CREATED");
        assertThat(record2.value().getEventType()).isEqualTo("ORDER_UPDATED");
        assertThat(record3.value().getEventType()).isEqualTo("ORDER_STATUS_CHANGED");

        stopConsumer();
    }

    @Test
    @DisplayName("Event Key - Uses Order Number as Partition Key")
    void eventKey_UsesOrderNumberAsPartitionKey() throws Exception {
        // Arrange
        setupConsumer("orders.created");

        // Act
        eventPublisherService.publishOrderCreated(testOrder, TEST_USER_ID);

        // Assert
        ConsumerRecord<String, OrderEvent> record = consumerRecords.poll(10, TimeUnit.SECONDS);
        assertThat(record).isNotNull();
        assertThat(record.key()).isEqualTo(testOrder.getOrderNumber());

        stopConsumer();
    }

    private void setupConsumer(String... topics) {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderEvent.class.getName());

        DefaultKafkaConsumerFactory<String, OrderEvent> consumerFactory =
            new DefaultKafkaConsumerFactory<>(consumerProps);

        ContainerProperties containerProperties = new ContainerProperties(topics);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        container.setupMessageListener((MessageListener<String, OrderEvent>) record -> {
            consumerRecords.add(record);
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    private void stopConsumer() {
        if (container != null) {
            container.stop();
        }
    }
}
