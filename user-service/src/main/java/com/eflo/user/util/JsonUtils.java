package com.eflo.user.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * Utility class for JSON serialization and deserialization.
 */
@Slf4j
@UtilityClass
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Convert object to JSON string.
     *
     * @param obj the object to convert
     * @return JSON string
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON", e);
            return null;
        }
    }

    /**
     * Convert object to pretty JSON string.
     *
     * @param obj the object to convert
     * @return pretty JSON string
     */
    public static String toPrettyJson(Object obj) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to pretty JSON", e);
            return null;
        }
    }

    /**
     * Convert JSON string to object.
     *
     * @param json  the JSON string
     * @param clazz the target class
     * @param <T>   the type
     * @return converted object
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Error converting JSON to object", e);
            return null;
        }
    }

    /**
     * Convert JSON string to object with TypeReference.
     *
     * @param json          the JSON string
     * @param typeReference the type reference
     * @param <T>           the type
     * @return converted object
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            log.error("Error converting JSON to object", e);
            return null;
        }
    }

    /**
     * Convert object to Map.
     *
     * @param obj the object to convert
     * @return map representation
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object obj) {
        return OBJECT_MAPPER.convertValue(obj, Map.class);
    }

    /**
     * Convert Map to object.
     *
     * @param map   the map
     * @param clazz the target class
     * @param <T>   the type
     * @return converted object
     */
    public static <T> T fromMap(Map<String, Object> map, Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(map, clazz);
    }

    /**
     * Deep clone an object using JSON serialization.
     *
     * @param obj   the object to clone
     * @param clazz the object class
     * @param <T>   the type
     * @return cloned object
     */
    public static <T> T deepClone(T obj, Class<T> clazz) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Error deep cloning object", e);
            return null;
        }
    }

    /**
     * Check if a string is valid JSON.
     *
     * @param json the string to check
     * @return true if valid JSON
     */
    public static boolean isValidJson(String json) {
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get ObjectMapper instance.
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }
}
