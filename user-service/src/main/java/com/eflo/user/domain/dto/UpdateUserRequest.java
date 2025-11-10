package com.eflo.user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
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
    private String preferences;
}
