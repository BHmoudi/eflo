package com.eflo.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HierarchyDTO {
    private Long id;
    private Long employeeId;
    private String employeeEmail;
    private String employeeFullName;
    private Long managerId;
    private String managerEmail;
    private String managerFullName;
    private Long businessUnitId;
    private String businessUnitName;
    private Integer level;
    private Boolean isActive;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String assignedBy;
    private String removedBy;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
