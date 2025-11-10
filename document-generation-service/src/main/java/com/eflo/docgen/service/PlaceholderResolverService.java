package com.eflo.docgen.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Placeholder Resolver Service
 *
 * Replaces {{placeholder}} variables in templates with actual values
 * Supports nested properties, formatting, and default values
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceholderResolverService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.FRANCE);

    /**
     * Resolve all placeholders in text with provided data
     */
    public String resolvePlaceholders(String text, Map<String, Object> data) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            String replacement = resolveValue(placeholder, data);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Resolve a single placeholder value
     * Supports: {{variable}}, {{variable:format}}, {{variable|default}}
     */
    private String resolveValue(String placeholder, Map<String, Object> data) {
        // Check for default value: {{variable|default}}
        String[] defaultSplit = placeholder.split("\\|", 2);
        String variable = defaultSplit[0].trim();
        String defaultValue = defaultSplit.length > 1 ? defaultSplit[1].trim() : "";

        // Check for format: {{variable:format}}
        String[] formatSplit = variable.split(":", 2);
        String variableName = formatSplit[0].trim();
        String format = formatSplit.length > 1 ? formatSplit[1].trim() : null;

        // Resolve nested properties: {{customer.name}}
        Object value = resolveNestedProperty(variableName, data);

        if (value == null) {
            return defaultValue;
        }

        // Apply formatting
        return formatValue(value, format);
    }

    /**
     * Resolve nested property like "customer.name" or "vehicle.model"
     */
    private Object resolveNestedProperty(String propertyPath, Map<String, Object> data) {
        String[] parts = propertyPath.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }

            if (current == null) {
                return null;
            }
        }

        return current;
    }

    /**
     * Format value according to specified format
     * Supported formats: currency, date, datetime, uppercase, lowercase, number
     */
    private String formatValue(Object value, String format) {
        if (format == null) {
            return String.valueOf(value);
        }

        try {
            switch (format.toLowerCase()) {
                case "currency":
                case "price":
                    if (value instanceof Number) {
                        return CURRENCY_FORMATTER.format(((Number) value).doubleValue());
                    }
                    return CURRENCY_FORMATTER.format(Double.parseDouble(value.toString()));

                case "date":
                    if (value instanceof LocalDate) {
                        return ((LocalDate) value).format(DATE_FORMATTER);
                    }
                    if (value instanceof LocalDateTime) {
                        return ((LocalDateTime) value).format(DATE_FORMATTER);
                    }
                    if (value instanceof String) {
                        LocalDate date = LocalDate.parse((String) value);
                        return date.format(DATE_FORMATTER);
                    }
                    return value.toString();

                case "datetime":
                    if (value instanceof LocalDateTime) {
                        return ((LocalDateTime) value).format(DATETIME_FORMATTER);
                    }
                    if (value instanceof String) {
                        LocalDateTime dateTime = LocalDateTime.parse((String) value);
                        return dateTime.format(DATETIME_FORMATTER);
                    }
                    return value.toString();

                case "uppercase":
                case "upper":
                    return value.toString().toUpperCase();

                case "lowercase":
                case "lower":
                    return value.toString().toLowerCase();

                case "number":
                    if (value instanceof Number) {
                        return NumberFormat.getInstance(Locale.FRANCE).format(value);
                    }
                    return value.toString();

                default:
                    log.warn("Unknown format: {}. Using default string conversion", format);
                    return value.toString();
            }
        } catch (Exception e) {
            log.error("Error formatting value {} with format {}", value, format, e);
            return value.toString();
        }
    }

    /**
     * Extract all placeholders from text
     */
    public java.util.List<String> extractPlaceholders(String text) {
        java.util.List<String> placeholders = new java.util.ArrayList<>();

        if (text == null || text.isEmpty()) {
            return placeholders;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            // Remove format and default value parts
            String variableName = placeholder.split("[:|]")[0].trim();
            if (!placeholders.contains(variableName)) {
                placeholders.add(variableName);
            }
        }

        return placeholders;
    }

    /**
     * Create sample data map for testing templates
     */
    public Map<String, Object> createSampleData() {
        Map<String, Object> data = new HashMap<>();

        // Order info
        data.put("orderNumber", "VN-2025-001");
        data.put("orderType", "VN");
        data.put("orderDate", LocalDate.now());

        // Customer info
        Map<String, Object> customer = new HashMap<>();
        customer.put("name", "Jean Dupont");
        customer.put("email", "jean.dupont@example.com");
        customer.put("phone", "+33 6 12 34 56 78");
        customer.put("address", "123 Rue de la République, 75001 Paris");
        data.put("customer", customer);

        // Vehicle info
        Map<String, Object> vehicle = new HashMap<>();
        vehicle.put("brand", "Peugeot");
        vehicle.put("model", "308");
        vehicle.put("year", 2025);
        vehicle.put("vin", "VF3XXXXXXXX123456");
        data.put("vehicle", vehicle);

        // Pricing
        data.put("basePrice", 25000.00);
        data.put("discount", 2000.00);
        data.put("totalPrice", 23000.00);
        data.put("vat", 4600.00);
        data.put("totalTTC", 27600.00);

        // Seller info
        data.put("sellerName", "Marie Martin");
        data.put("sellerEmail", "marie.martin@eflo.com");

        // Company info
        data.put("companyName", "Eflo Automobiles");
        data.put("companyAddress", "456 Avenue des Champs-Élysées, 75008 Paris");

        return data;
    }
}
