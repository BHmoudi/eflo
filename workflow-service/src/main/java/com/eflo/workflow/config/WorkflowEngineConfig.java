package com.eflo.workflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Workflow Engine Configuration Properties
 *
 * Maps configuration from application.yml to Java objects.
 *
 * @author Workflow Service
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "eflo.workflow")
public class WorkflowEngineConfig {

    private Engine engine = new Engine();
    private Scheduling scheduling = new Scheduling();
    private Escalation escalation = new Escalation();
    private Notifications notifications = new Notifications();
    private Performance performance = new Performance();
    private Integration integration = new Integration();

    @Data
    public static class Engine {
        private Boolean enabled = true;
        private Boolean autoProgress = true;
        private Boolean parallelExecution = true;
        private Integer maxRetries = 3;
    }

    @Data
    public static class Scheduling {
        private Long deadlineCheckInterval = 1800000L; // 30 minutes
        private Long escalationCheckInterval = 3600000L; // 1 hour
        private Long cleanupInterval = 86400000L; // 24 hours
    }

    @Data
    public static class Escalation {
        private Boolean enabled = true;
        private List<EscalationLevel> levels;

        @Data
        public static class EscalationLevel {
            private Integer durationHours;
            private String action;
        }
    }

    @Data
    public static class Notifications {
        private Boolean enabled = true;
        private List<String> channels;
        private Templates templates = new Templates();

        @Data
        public static class Templates {
            private String taskAssigned;
            private String taskOverdue;
            private String instanceOverdue;
            private String escalation;
        }
    }

    @Data
    public static class Performance {
        private Boolean cacheEnabled = true;
        private Integer cacheTtlSeconds = 300;
        private Integer batchSize = 100;
        private Boolean asyncEnabled = true;
    }

    @Data
    public static class Integration {
        private ServiceConfig orderService = new ServiceConfig();
        private ServiceConfig documentService = new ServiceConfig();
        private ServiceConfig userService = new ServiceConfig();

        @Data
        public static class ServiceConfig {
            private String url;
            private Integer timeout = 5000;
        }
    }
}
