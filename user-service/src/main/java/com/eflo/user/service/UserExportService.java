package com.eflo.user.service;

import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Service for exporting user data to various formats.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserExportService {

    private final UserRepository userRepository;

    /**
     * Export all users to CSV format.
     *
     * @return CSV data as byte array
     */
    @Transactional(readOnly = true)
    public byte[] exportUsersToCSV() {
        log.info("Exporting all users to CSV");
        List<User> users = userRepository.findAll();
        return generateCSV(users);
    }

    /**
     * Export active users to CSV format.
     *
     * @return CSV data as byte array
     */
    @Transactional(readOnly = true)
    public byte[] exportActiveUsersToCSV() {
        log.info("Exporting active users to CSV");
        List<User> users = userRepository.findAll().stream()
                .filter(User::isActive)
                .toList();
        return generateCSV(users);
    }

    /**
     * Export users by business unit to CSV format.
     *
     * @param businessUnitId the business unit ID
     * @return CSV data as byte array
     */
    @Transactional(readOnly = true)
    public byte[] exportUsersByBusinessUnitToCSV(String businessUnitId) {
        log.info("Exporting users for business unit {} to CSV", businessUnitId);
        List<User> users = userRepository.findAll().stream()
                .filter(user -> user.getUserBusinessUnits() != null &&
                        user.getUserBusinessUnits().stream()
                                .anyMatch(ubu -> ubu.getBusinessUnit().getId().toString().equals(businessUnitId)))
                .toList();
        return generateCSV(users);
    }

    /**
     * Generate CSV data from user list.
     *
     * @param users list of users
     * @return CSV data as byte array
     */
    private byte[] generateCSV(List<User> users) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // Write CSV header
            writer.println("ID,Keycloak ID,Username,Email,First Name,Last Name,Phone,Active,Created At,Updated At");

            // Write user data
            for (User user : users) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escapeCSV(user.getId().toString()),
                        escapeCSV(user.getKeycloakId().toString()),
                        escapeCSV(user.getUsername()),
                        escapeCSV(user.getEmail()),
                        escapeCSV(user.getFirstName()),
                        escapeCSV(user.getLastName()),
                        escapeCSV(user.getPhone()),
                        user.isActive(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    /**
     * Escape CSV special characters.
     *
     * @param value the value to escape
     * @return escaped value
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Get CSV filename with timestamp.
     *
     * @param prefix filename prefix
     * @return filename
     */
    public String getCSVFilename(String prefix) {
        return String.format("%s_%s.csv", prefix, System.currentTimeMillis());
    }
}
