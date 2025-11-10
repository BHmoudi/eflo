package com.eflo.user.domain.dto;

import com.eflo.user.domain.enums.BusinessUnitType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitDTO {
    private Long id;
    private String code;
    private String name;
    private String legalName;
    private BusinessUnitType type;
    private String regionCode;
    private String regionName;
    private String rrfCode;
    private String phoneNumber;
    private String faxNumber;
    private String email;
    private String website;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String city;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String openingHours;
    private Long managerId;
    private String managerName;
    private String siret;
    private String vatNumber;
    private String[] brands;
    private Boolean isActive;
    private LocalDate openingDate;
    private LocalDate closingDate;
    private String settings;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
