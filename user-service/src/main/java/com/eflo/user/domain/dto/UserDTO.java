package com.eflo.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private UUID keycloakId;
    private String keycloakUsername;
    private String employeeNumber;
    private String userIpn;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
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
    private LocalDate terminationDate;
    private Boolean isActive;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;
    private String profilePictureUrl;
    private String bio;
    private String preferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
