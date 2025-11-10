package com.eflo.user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    // Keycloak integration
    private UUID keycloakId;
    private String keycloakUsername;

    @NotBlank(message = "Employee number is required")
    private String employeeNumber;

    private String userIpn;  // IPN field (e.g., d179090)

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phoneNumber;
    private String mobileNumber;
    private String officeExtension;
    private String jobTitle;
    private String department;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String city;
    private String country;
    private LocalDate hireDate;
    private String profilePictureUrl;
    private String bio;

    @NotBlank(message = "Temporary password is required")
    private String temporaryPassword;
}
