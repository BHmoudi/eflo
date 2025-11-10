package com.eflo.user.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations.
 */
@UtilityClass
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]{3,20}$"
    );

    /**
     * Validate email address format.
     *
     * @param email the email to validate
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format (E.164 format).
     *
     * @param phone the phone number to validate
     * @return true if valid
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate username format.
     *
     * @param username the username to validate
     * @return true if valid
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * Check if a string is null or empty.
     *
     * @param str the string to check
     * @return true if null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty.
     *
     * @param str the string to check
     * @return true if not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Validate that a string is within a specified length range.
     *
     * @param str       the string to validate
     * @param minLength minimum length
     * @param maxLength maximum length
     * @return true if within range
     */
    public static boolean isLengthValid(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * Validate that a value is not null.
     *
     * @param value the value to check
     * @param name  the name of the value for error message
     * @throws IllegalArgumentException if value is null
     */
    public static void requireNonNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    /**
     * Validate that a string is not empty.
     *
     * @param value the string to check
     * @param name  the name of the value for error message
     * @throws IllegalArgumentException if value is empty
     */
    public static void requireNonEmpty(String value, String name) {
        if (isEmpty(value)) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
    }
}
