package com.eflo.document.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for business unit information from Order Service.
 *
 * @author Document Service
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessUnitResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Boolean isActive;
}
