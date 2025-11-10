package com.eflo.user.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UserDTO user;
    private List<UserBusinessUnitDTO> businessUnits;
    private List<UserRoleDTO> roles;
    private UserBusinessUnitDTO primaryBusinessUnit;
    private HierarchyDTO hierarchy;
}
