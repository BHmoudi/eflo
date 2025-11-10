package com.eflo.user.mapper;

import com.eflo.user.domain.dto.CreateUserRequest;
import com.eflo.user.domain.dto.UpdateUserRequest;
import com.eflo.user.domain.dto.UserDTO;
import com.eflo.user.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    List<UserDTO> toDTOList(List<User> users);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "keycloakUsername", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "isDeleted", constant = "false")
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "loginCount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "syncedFromKeycloakAt", ignore = true)
    @Mapping(target = "businessUnits", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "hierarchiesAsEmployee", ignore = true)
    @Mapping(target = "hierarchiesAsManager", ignore = true)
    @Mapping(target = "preferences", constant = "{}")
    @Mapping(target = "terminationDate", ignore = true)
    @Mapping(target = "country", defaultValue = "FR")
    User toEntity(CreateUserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "keycloakId", ignore = true)
    @Mapping(target = "keycloakUsername", ignore = true)
    @Mapping(target = "employeeNumber", ignore = true)
    @Mapping(target = "fullName", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "loginCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "syncedFromKeycloakAt", ignore = true)
    @Mapping(target = "businessUnits", ignore = true)
    @Mapping(target = "userBusinessUnits", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "hierarchiesAsEmployee", ignore = true)
    @Mapping(target = "hierarchiesAsManager", ignore = true)
    @Mapping(target = "terminationDate", ignore = true)
    void updateEntity(UpdateUserRequest request, @MappingTarget User user);
}
