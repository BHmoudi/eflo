package com.eflo.user.domain.dto;

import com.eflo.user.domain.enums.BusinessUnitType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBusinessUnitRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Name is required")
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
    private String siret;
    private String vatNumber;
    private String[] brands;
    private LocalDate openingDate;
    private String settings;
}
