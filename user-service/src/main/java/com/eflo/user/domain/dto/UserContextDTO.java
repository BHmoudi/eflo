package com.eflo.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * User Context DTO - Contains user information with custom claims
 * Includes: user_ipn, rrf (from business unit), roles, etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContextDTO {

    // Basic user info
    private Long userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;

    // Employee info
    private String employeeNumber;
    private String userIpn;  // IPN field (e.g., d179090)
    private String department;
    private String jobTitle;

    // Business unit info
    private Long primaryBusinessUnitId;
    private String primaryBusinessUnitCode;
    private String primaryBusinessUnitName;
    private String rrf;  // RRF code from primary business unit

    // Roles
    private List<String> roles;

    // Additional business units
    private List<BusinessUnitInfo> businessUnits;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BusinessUnitInfo {
        private Long id;
        private String code;
        private String name;
        private String rrf;
        private Boolean isPrimary;
    }
}
