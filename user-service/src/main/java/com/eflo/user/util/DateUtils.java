package com.eflo.user.util;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations.
 */
@UtilityClass
public class DateUtils {

    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter CUSTOM_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CUSTOM_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get current LocalDateTime in UTC.
     *
     * @return current UTC datetime
     */
    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Get current LocalDate in UTC.
     *
     * @return current UTC date
     */
    public static LocalDate todayUtc() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    /**
     * Format LocalDateTime to ISO string.
     *
     * @param dateTime the datetime to format
     * @return ISO formatted string
     */
    public static String formatIsoDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(ISO_DATE_TIME_FORMATTER) : null;
    }

    /**
     * Format LocalDate to ISO string.
     *
     * @param date the date to format
     * @return ISO formatted string
     */
    public static String formatIsoDate(LocalDate date) {
        return date != null ? date.format(ISO_DATE_FORMATTER) : null;
    }

    /**
     * Format LocalDateTime to custom format.
     *
     * @param dateTime the datetime to format
     * @return custom formatted string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(CUSTOM_DATE_TIME_FORMATTER) : null;
    }

    /**
     * Format LocalDate to custom format.
     *
     * @param date the date to format
     * @return custom formatted string
     */
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(CUSTOM_DATE_FORMATTER) : null;
    }

    /**
     * Parse ISO date string to LocalDate.
     *
     * @param dateStr the date string
     * @return parsed LocalDate
     */
    public static LocalDate parseDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, ISO_DATE_FORMATTER) : null;
    }

    /**
     * Parse ISO datetime string to LocalDateTime.
     *
     * @param dateTimeStr the datetime string
     * @return parsed LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null ? LocalDateTime.parse(dateTimeStr, ISO_DATE_TIME_FORMATTER) : null;
    }

    /**
     * Calculate days between two dates.
     *
     * @param startDate the start date
     * @param endDate   the end date
     * @return number of days between dates
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Calculate hours between two datetimes.
     *
     * @param startDateTime the start datetime
     * @param endDateTime   the end datetime
     * @return number of hours between datetimes
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * Check if a date is in the past.
     *
     * @param date the date to check
     * @return true if date is in the past
     */
    public static boolean isPast(LocalDate date) {
        return date != null && date.isBefore(todayUtc());
    }

    /**
     * Check if a datetime is in the past.
     *
     * @param dateTime the datetime to check
     * @return true if datetime is in the past
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isBefore(nowUtc());
    }

    /**
     * Check if a date is in the future.
     *
     * @param date the date to check
     * @return true if date is in the future
     */
    public static boolean isFuture(LocalDate date) {
        return date != null && date.isAfter(todayUtc());
    }

    /**
     * Check if a datetime is in the future.
     *
     * @param dateTime the datetime to check
     * @return true if datetime is in the future
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime != null && dateTime.isAfter(nowUtc());
    }

    /**
     * Add days to a date.
     *
     * @param date the date
     * @param days number of days to add
     * @return new date with days added
     */
    public static LocalDate addDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }

    /**
     * Add hours to a datetime.
     *
     * @param dateTime the datetime
     * @param hours    number of hours to add
     * @return new datetime with hours added
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    /**
     * Get start of day for a date.
     *
     * @param date the date
     * @return datetime at start of day (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }

    /**
     * Get end of day for a date.
     *
     * @param date the date
     * @return datetime at end of day (23:59:59)
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date != null ? date.atTime(23, 59, 59) : null;
    }

    /**
     * Convert LocalDateTime to epoch milliseconds.
     *
     * @param dateTime the datetime
     * @return epoch milliseconds
     */
    public static long toEpochMilli(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli() : 0L;
    }

    /**
     * Convert epoch milliseconds to LocalDateTime.
     *
     * @param epochMilli the epoch milliseconds
     * @return LocalDateTime
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
    }
}
