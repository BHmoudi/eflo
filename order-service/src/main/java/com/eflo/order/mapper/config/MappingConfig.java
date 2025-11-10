package com.eflo.order.mapper.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Configuration class for JSON to Order mapping
 * This class represents the structure of mapping configuration files
 */
@Data
public class MappingConfig {

    @JsonProperty("mappingName")
    private String mappingName;

    @JsonProperty("version")
    private String version;

    @JsonProperty("source")
    private String source;

    @JsonProperty("description")
    private String description;

    @JsonProperty("dateFormat")
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    @JsonProperty("customer")
    private Map<String, Object> customer;

    @JsonProperty("order")
    private Map<String, Object> order;

    @JsonProperty("tradeIn")
    private Map<String, Object> tradeIn;

    @JsonProperty("commercialActions")
    private ArrayMappingConfig commercialActions;

    @JsonProperty("options")
    private ArrayMappingConfig options;

    @JsonProperty("accessories")
    private ArrayMappingConfig accessories;

    @JsonProperty("supplements")
    private ArrayMappingConfig supplements;

    @JsonProperty("services")
    private ArrayMappingConfig services;

    @JsonProperty("documents")
    private DocumentMappingConfig documents;

    @JsonProperty("actors")
    private Map<String, Map<String, Object>> actors;

    @JsonProperty("transforms")
    private Map<String, TransformConfig> transforms;

    @Data
    public static class ArrayMappingConfig {
        @JsonProperty("arrayPath")
        private String arrayPath;

        @JsonProperty("fields")
        private Map<String, Object> fields;
    }

    @Data
    public static class DocumentMappingConfig extends ArrayMappingConfig {
        @JsonProperty("publishToKafka")
        private boolean publishToKafka = false;

        @JsonProperty("kafkaTopic")
        private String kafkaTopic;
    }

    @Data
    public static class TransformConfig {
        @JsonProperty("description")
        private String description;

        @JsonProperty("operation")
        private String operation;

        @JsonProperty("value")
        private Object value;

        @JsonProperty("format")
        private String format;
    }

    @Data
    public static class FieldMapping {
        @JsonProperty("source")
        private String source;

        @JsonProperty("transform")
        private String transform;

        @JsonProperty("mapping")
        private Map<String, String> mapping;

        @JsonProperty("default")
        private Object defaultValue;

        @JsonProperty("template")
        private String template;

        @JsonProperty("condition")
        private String condition;

        @JsonProperty("value")
        private Object value;
    }
}
