package com.eflo.order.domain.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCustomerRequest {

    @NotBlank(message = "Customer type is required")
    @Pattern(regexp = "PA|PRO", message = "Customer type must be PA or PRO")
    private String customerType;

    @Size(max = 10)
    private String civility;

    @Size(max = 255)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 255)
    private String lastName;

    @Size(max = 255)
    private String commercialName;

    @Size(max = 255)
    private String companyName;

    @Size(max = 14, message = "SIRET must be 14 characters")
    private String siret;

    @Email(message = "Email should be valid")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phoneMobile;

    @Size(max = 20)
    private String phoneLandline;

    private String address;

    @Size(max = 10)
    private String postalCode;

    @Size(max = 100)
    private String city;

    @Size(max = 2, message = "Country code must be 2 characters")
    private String country;

    private Integer sa;

    @Size(max = 100)
    private String externalCustomerId;

    @Builder.Default
    private Boolean isActive = true;
}
