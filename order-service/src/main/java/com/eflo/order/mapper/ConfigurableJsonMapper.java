package com.eflo.order.mapper;

import com.eflo.order.mapper.config.MappingConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Configurable JSON Mapper
 * Maps any JSON structure to Order entities based on configuration files
 */
@Slf4j
@Component
public class ConfigurableJsonMapper {

    private final ObjectMapper objectMapper;
    private final Map<String, MappingConfig> mappingConfigs = new HashMap<>();

    public ConfigurableJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadMappingConfigurations();
    }

    /**
     * Load all mapping configurations from resources/mappings/
     */
    private void loadMappingConfigurations() {
        try {
            // Load MOVE mapping
            loadMappingConfig("mappings/move-order-mapping.json", "MOVE");

            // Load Generic mapping
            loadMappingConfig("mappings/generic-order-mapping.json", "GENERIC");

            log.info("Loaded {} mapping configurations", mappingConfigs.size());
        } catch (Exception e) {
            log.error("Failed to load mapping configurations", e);
        }
    }

    private void loadMappingConfig(String resourcePath, String source) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                MappingConfig config = objectMapper.readValue(is, MappingConfig.class);
                mappingConfigs.put(source, config);
                log.info("Loaded mapping config: {} ({})", config.getMappingName(), source);
            }
        } catch (IOException e) {
            log.warn("Failed to load mapping config from {}", resourcePath, e);
        }
    }

    /**
     * Map JSON to structured order data using specified mapping source
     *
     * @param jsonNode The input JSON
     * @param source The mapping source (e.g., "MOVE", "GENERIC")
     * @return Mapped order data
     */
    public Map<String, Object> mapToOrder(JsonNode jsonNode, String source) {
        MappingConfig config = mappingConfigs.get(source);
        if (config == null) {
            throw new IllegalArgumentException("No mapping configuration found for source: " + source);
        }

        Map<String, Object> result = new HashMap<>();

        // Map customer
        if (config.getCustomer() != null) {
            result.put("customer", mapObject(jsonNode, config.getCustomer(), config));
        }

        // Map order
        if (config.getOrder() != null) {
            result.put("order", mapObject(jsonNode, config.getOrder(), config));
        }

        // Map trade-in (if enabled)
        if (config.getTradeIn() != null) {
            Map<String, Object> tradeInEnabled = (Map<String, Object>) config.getTradeIn().get("enabled");
            if (tradeInEnabled != null && isConditionMet(jsonNode, tradeInEnabled)) {
                result.put("tradeIn", mapObject(jsonNode, config.getTradeIn(), config));
            }
        }

        // Map commercial actions
        if (config.getCommercialActions() != null) {
            result.put("commercialActions", mapArray(jsonNode, config.getCommercialActions(), config));
        }

        // Map options
        if (config.getOptions() != null) {
            result.put("options", mapArray(jsonNode, config.getOptions(), config));
        }

        // Map accessories
        if (config.getAccessories() != null) {
            result.put("accessories", mapArray(jsonNode, config.getAccessories(), config));
        }

        // Map supplements
        if (config.getSupplements() != null) {
            result.put("supplements", mapArray(jsonNode, config.getSupplements(), config));
        }

        // Map services
        if (config.getServices() != null) {
            result.put("services", mapArray(jsonNode, config.getServices(), config));
        }

        // Map documents
        if (config.getDocuments() != null) {
            result.put("documents", mapArray(jsonNode, config.getDocuments(), config));
        }

        // Map actors
        if (config.getActors() != null) {
            result.put("actors", mapActors(jsonNode, config.getActors(), config));
        }

        return result;
    }

    /**
     * Map a single object based on field mappings
     */
    private Map<String, Object> mapObject(JsonNode jsonNode, Map<String, Object> fieldMappings, MappingConfig config) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : fieldMappings.entrySet()) {
            String targetField = entry.getKey();
            Object mappingValue = entry.getValue();

            if (mappingValue instanceof String) {
                // Simple path mapping
                String sourcePath = (String) mappingValue;
                Object value = extractValue(jsonNode, sourcePath);
                if (value != null) {
                    result.put(targetField, value);
                }
            } else if (mappingValue instanceof Map) {
                // Complex mapping with transformations
                Map<String, Object> fieldConfig = (Map<String, Object>) mappingValue;
                Object value = mapComplexField(jsonNode, fieldConfig, config);
                if (value != null) {
                    result.put(targetField, value);
                }
            }
        }

        return result;
    }

    /**
     * Map array of items
     */
    private List<Map<String, Object>> mapArray(JsonNode jsonNode, MappingConfig.ArrayMappingConfig arrayConfig, MappingConfig config) {
        List<Map<String, Object>> result = new ArrayList<>();

        JsonNode arrayNode = getNodeByPath(jsonNode, arrayConfig.getArrayPath());
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                Map<String, Object> mappedItem = mapObject(item, arrayConfig.getFields(), config);
                result.add(mappedItem);
            }
        }

        return result;
    }

    /**
     * Map actors (special handling)
     */
    private List<Map<String, Object>> mapActors(JsonNode jsonNode, Map<String, Map<String, Object>> actorMappings, MappingConfig config) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, Map<String, Object>> entry : actorMappings.entrySet()) {
            Map<String, Object> actorData = mapObject(jsonNode, entry.getValue(), config);
            if (!actorData.isEmpty()) {
                result.add(actorData);
            }
        }

        return result;
    }

    /**
     * Map complex field with transformations, conditions, mappings
     */
    private Object mapComplexField(JsonNode jsonNode, Map<String, Object> fieldConfig, MappingConfig config) {
        String sourcePath = (String) fieldConfig.get("source");
        if (sourcePath == null) {
            // Handle template
            String template = (String) fieldConfig.get("template");
            if (template != null) {
                return processTemplate(jsonNode, template);
            }
            return fieldConfig.get("default");
        }

        Object value = extractValue(jsonNode, sourcePath);

        // Apply transform
        String transform = (String) fieldConfig.get("transform");
        if (transform != null && config.getTransforms() != null) {
            MappingConfig.TransformConfig transformConfig = config.getTransforms().get(transform);
            if (transformConfig != null) {
                value = applyTransform(value, transformConfig, config);
            }
        }

        // Apply mapping (value substitution)
        Map<String, String> mapping = (Map<String, String>) fieldConfig.get("mapping");
        if (mapping != null && value != null) {
            String mappedValue = mapping.get(String.valueOf(value));
            if (mappedValue != null) {
                return mappedValue;
            }
            return fieldConfig.get("default");
        }

        // Apply condition
        String condition = (String) fieldConfig.get("condition");
        if (condition != null) {
            return evaluateCondition(value, condition, fieldConfig);
        }

        return value != null ? value : fieldConfig.get("default");
    }

    /**
     * Extract value from JSON using dot notation path
     */
    private Object extractValue(JsonNode jsonNode, String path) {
        JsonNode node = getNodeByPath(jsonNode, path);
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isTextual()) return node.asText();
        if (node.isInt()) return node.asInt();
        if (node.isLong()) return node.asLong();
        if (node.isDouble()) return node.asDouble();
        if (node.isBoolean()) return node.asBoolean();

        return node.toString();
    }

    /**
     * Navigate JSON tree using dot notation
     */
    private JsonNode getNodeByPath(JsonNode root, String path) {
        if (path == null) return null;

        String[] parts = path.split("\\.");
        JsonNode current = root;

        for (String part : parts) {
            if (current == null) return null;
            current = current.get(part);
        }

        return current;
    }

    /**
     * Process template string (e.g., "{prix.szPrenomVendeur} {prix.szNomVendeur}")
     */
    private String processTemplate(JsonNode jsonNode, String template) {
        String result = template;

        // Find all {path} placeholders
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(template);

        while (matcher.find()) {
            String path = matcher.group(1);
            Object value = extractValue(jsonNode, path);
            result = result.replace("{" + path + "}", value != null ? String.valueOf(value) : "");
        }

        return result.trim();
    }

    /**
     * Apply transformation to value
     */
    private Object applyTransform(Object value, MappingConfig.TransformConfig transformConfig, MappingConfig config) {
        if (value == null) return null;

        String operation = transformConfig.getOperation();

        switch (operation.toUpperCase()) {
            case "MULTIPLY":
                if (value instanceof Number && transformConfig.getValue() instanceof Number) {
                    double result = ((Number) value).doubleValue() * ((Number) transformConfig.getValue()).doubleValue();
                    return BigDecimal.valueOf(result);
                }
                break;

            case "MULTIPLY_100":
                if (value instanceof Number) {
                    double result = ((Number) value).doubleValue() * 100;
                    return BigDecimal.valueOf(result);
                }
                break;

            case "TO_BOOLEAN":
                if (value instanceof Number) {
                    return ((Number) value).intValue() != 0;
                }
                if (value instanceof String) {
                    String str = ((String) value).toLowerCase();
                    return "true".equals(str) || "1".equals(str) || "yes".equals(str);
                }
                return Boolean.parseBoolean(String.valueOf(value));

            case "PARSE_DATE":
                if (value instanceof String) {
                    String format = transformConfig.getFormat() != null ?
                        transformConfig.getFormat() : config.getDateFormat();
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                        // Try LocalDateTime first
                        try {
                            return LocalDateTime.parse((String) value, formatter);
                        } catch (Exception e) {
                            // Try LocalDate
                            return LocalDate.parse((String) value, formatter);
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse date: {}", value, e);
                    }
                }
                break;

            case "ISNOTEMPTY":
            case "NOT_EMPTY":
                if (value instanceof String) {
                    return !((String) value).trim().isEmpty();
                }
                return value != null;

            default:
                log.warn("Unknown transform operation: {}", operation);
        }

        return value;
    }

    /**
     * Evaluate condition
     */
    private Object evaluateCondition(Object value, String condition, Map<String, Object> fieldConfig) {
        switch (condition.toUpperCase()) {
            case "NOT_EMPTY":
            case "NOTEMPTY":
                if (value instanceof String) {
                    return !((String) value).trim().isEmpty();
                }
                return value != null;

            case "NOT_NULL":
            case "NOTNULL":
                return value != null;

            case "EQUALS":
                Object compareValue = fieldConfig.get("value");
                if (compareValue != null && value != null) {
                    return String.valueOf(value).equals(String.valueOf(compareValue));
                }
                return fieldConfig.get("default");

            default:
                return value;
        }
    }

    /**
     * Check if condition is met
     */
    private boolean isConditionMet(JsonNode jsonNode, Map<String, Object> conditionConfig) {
        String sourcePath = (String) conditionConfig.get("source");
        String condition = (String) conditionConfig.get("condition");

        Object value = extractValue(jsonNode, sourcePath);
        Object result = evaluateCondition(value, condition, conditionConfig);

        return result instanceof Boolean ? (Boolean) result : false;
    }

    /**
     * Get list of available mapping sources
     */
    public List<String> getAvailableMappings() {
        return new ArrayList<>(mappingConfigs.keySet());
    }

    /**
     * Get mapping configuration by source
     */
    public MappingConfig getMappingConfig(String source) {
        return mappingConfigs.get(source);
    }

    /**
     * Auto-detect mapping source from JSON structure
     */
    public String detectMappingSource(JsonNode jsonNode) {
        // Check for MOVE format (has "prix" object)
        if (jsonNode.has("prix")) {
            return "MOVE";
        }

        // Check for Generic format (has "customer" and "vehicle" objects)
        if (jsonNode.has("customer") && jsonNode.has("vehicle")) {
            return "GENERIC";
        }

        // Default
        return null;
    }
}
