package com.eflo.user.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for string operations.
 */
@UtilityClass
public class StringUtils {

    /**
     * Capitalize the first letter of a string.
     *
     * @param str the string to capitalize
     * @return capitalized string
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Convert string to camelCase.
     *
     * @param str the string to convert
     * @return camelCase string
     */
    public static String toCamelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        String[] parts = str.split("[_\\s-]+");
        StringBuilder result = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            result.append(capitalize(parts[i].toLowerCase()));
        }
        return result.toString();
    }

    /**
     * Convert string to snake_case.
     *
     * @param str the string to convert
     * @return snake_case string
     */
    public static String toSnakeCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("[\\s-]+", "_")
                .toLowerCase();
    }

    /**
     * Truncate string to specified length with ellipsis.
     *
     * @param str       the string to truncate
     * @param maxLength maximum length
     * @return truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Mask sensitive data (e.g., email, phone).
     *
     * @param str         the string to mask
     * @param visibleChars number of visible characters at start
     * @return masked string
     */
    public static String mask(String str, int visibleChars) {
        if (str == null || str.length() <= visibleChars) {
            return str;
        }
        StringBuilder masked = new StringBuilder(str.substring(0, visibleChars));
        for (int i = visibleChars; i < str.length(); i++) {
            masked.append('*');
        }
        return masked.toString();
    }

    /**
     * Mask email address.
     *
     * @param email the email to mask
     * @return masked email
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        int visibleChars = Math.min(3, localPart.length());
        String maskedLocal = mask(localPart, visibleChars);

        return maskedLocal + "@" + domain;
    }

    /**
     * Join strings with a delimiter.
     *
     * @param delimiter the delimiter
     * @param elements  the elements to join
     * @return joined string
     */
    public static String join(String delimiter, String... elements) {
        return String.join(delimiter, elements);
    }

    /**
     * Join list of strings with a delimiter.
     *
     * @param delimiter the delimiter
     * @param elements  the list of elements to join
     * @return joined string
     */
    public static String join(String delimiter, List<String> elements) {
        return String.join(delimiter, elements);
    }

    /**
     * Split string by delimiter and trim each element.
     *
     * @param str       the string to split
     * @param delimiter the delimiter
     * @return list of trimmed strings
     */
    public static List<String> splitAndTrim(String str, String delimiter) {
        if (str == null || str.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Remove all whitespace from a string.
     *
     * @param str the string
     * @return string without whitespace
     */
    public static String removeWhitespace(String str) {
        return str != null ? str.replaceAll("\\s+", "") : null;
    }

    /**
     * Check if string contains only digits.
     *
     * @param str the string to check
     * @return true if contains only digits
     */
    public static boolean isNumeric(String str) {
        return str != null && str.matches("\\d+");
    }

    /**
     * Check if string contains only letters.
     *
     * @param str the string to check
     * @return true if contains only letters
     */
    public static boolean isAlpha(String str) {
        return str != null && str.matches("[a-zA-Z]+");
    }

    /**
     * Check if string contains only letters and numbers.
     *
     * @param str the string to check
     * @return true if contains only letters and numbers
     */
    public static boolean isAlphanumeric(String str) {
        return str != null && str.matches("[a-zA-Z0-9]+");
    }

    /**
     * Pad string to the left with specified character.
     *
     * @param str       the string to pad
     * @param length    desired length
     * @param padChar   character to pad with
     * @return padded string
     */
    public static String padLeft(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - str.length(); i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    /**
     * Pad string to the right with specified character.
     *
     * @param str       the string to pad
     * @param length    desired length
     * @param padChar   character to pad with
     * @return padded string
     */
    public static String padRight(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < length - str.length(); i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }
}
