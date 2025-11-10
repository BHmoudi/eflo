package com.eflo.order.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDTO {

    private Long id;
    private String customerType;
    private String civility;
    private String firstName;
    private String lastName;
    private String commercialName;
    private String companyName;
    private String siret;
    private String email;
    private String phoneMobile;
    private String phoneLandline;
    private String address;
    private String postalCode;
    private String city;
    private String country;
    private Integer sa;
    private String externalCustomerId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
