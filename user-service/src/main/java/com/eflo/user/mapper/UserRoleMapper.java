package com.eflo.user.mapper;

import com.eflo.user.domain.dto.UserRoleDTO;
import com.eflo.user.domain.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "businessUnit.id", target = "businessUnitId")
    @Mapping(source = "businessUnit.name", target = "businessUnitName")
    UserRoleDTO toDTO(UserRole userRole);

    List<UserRoleDTO> toDTOList(List<UserRole> userRoles);
}
