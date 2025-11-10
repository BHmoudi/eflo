package com.eflo.user.service;

import com.eflo.user.domain.dto.CreateUserRequest;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.repository.UserRepository;
import com.eflo.user.exception.DuplicateUserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for importing users from CSV files.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportService {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Import users from CSV file.
     *
     * @param file       the CSV file
     * @param skipHeader whether to skip the first row
     * @return import result with success and error counts
     */
    @Transactional
    public ImportResult importUsersFromCSV(MultipartFile file, boolean skipHeader) {
        log.info("Starting user import from CSV file: {}", file.getOriginalFilename());

        ImportResult result = new ImportResult();
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip header if requested
                if (lineNumber == 1 && skipHeader) {
                    continue;
                }

                try {
                    importUserFromCSVLine(line);
                    result.incrementSuccess();
                } catch (Exception e) {
                    result.incrementError();
                    errors.add(String.format("Line %d: %s", lineNumber, e.getMessage()));
                    log.error("Error importing user at line {}: {}", lineNumber, e.getMessage());
                }
            }

            result.setErrors(errors);
            log.info("User import completed. Success: {}, Errors: {}", result.getSuccessCount(), result.getErrorCount());

        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            throw new RuntimeException("Failed to read CSV file", e);
        }

        return result;
    }

    /**
     * Import a single user from CSV line.
     *
     * @param line the CSV line
     */
    private void importUserFromCSVLine(String line) {
        String[] fields = parseCSVLine(line);

        // Expected format: username,email,firstName,lastName,phone
        if (fields.length < 5) {
            throw new IllegalArgumentException("Invalid CSV format. Expected at least 5 fields.");
        }

        String username = fields[0].trim();
        String email = fields[1].trim();
        String firstName = fields[2].trim();
        String lastName = fields[3].trim();
        String phone = fields[4].trim();

        // Check if user already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new DuplicateUserException("User with email " + email + " already exists");
        }

        if (userRepository.findByKeycloakUsername(username).isPresent()) {
            throw new DuplicateUserException("User with username " + username + " already exists");
        }

        // Generate a temporary employee number if not provided
        String employeeNumber = fields.length > 5 ? fields[5].trim() : "EMP-" + System.currentTimeMillis();

        // Create user request
        CreateUserRequest request = CreateUserRequest.builder()
                .employeeNumber(employeeNumber)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phone)
                .temporaryPassword("ChangeMe123!")
                .build();

        // Create User entity from request
        User user = User.builder()
                .employeeNumber(request.getEmployeeNumber())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .keycloakUsername(username)
                .build();

        userService.createUser(user, request.getTemporaryPassword(), "system");
    }

    /**
     * Parse CSV line handling quoted fields.
     *
     * @param line the CSV line
     * @return array of fields
     */
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Result of user import operation.
     */
    @lombok.Data
    public static class ImportResult {
        private int successCount = 0;
        private int errorCount = 0;
        private List<String> errors = new ArrayList<>();

        public void incrementSuccess() {
            successCount++;
        }

        public void incrementError() {
            errorCount++;
        }
    }
}
