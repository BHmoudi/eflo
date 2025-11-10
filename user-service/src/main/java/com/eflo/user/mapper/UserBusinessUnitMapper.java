package com.eflo.user.mapper;

import com.eflo.user.domain.dto.UserBusinessUnitDTO;
import com.eflo.user.domain.entity.UserBusinessUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserBusinessUnitMapper {

    @Mapping(target = "userId", expression = "java(userBusinessUnit.getUser() != null ? userBusinessUnit.getUser().getId() : null)")
    @Mapping(target = "userEmail", expression = "java(userBusinessUnit.getUser() != null ? userBusinessUnit.getUser().getEmail() : null)")
    @Mapping(target = "userFullName", expression = "java(userBusinessUnit.getUser() != null ? userBusinessUnit.getUser().getFullName() : null)")
    @Mapping(target = "businessUnitId", expression = "java(userBusinessUnit.getBusinessUnit() != null ? userBusinessUnit.getBusinessUnit().getId() : null)")
    @Mapping(target = "businessUnitCode", expression = "java(userBusinessUnit.getBusinessUnit() != null ? userBusinessUnit.getBusinessUnit().getCode() : null)")
    @Mapping(target = "businessUnitName", expression = "java(userBusinessUnit.getBusinessUnit() != null ? userBusinessUnit.getBusinessUnit().getName() : null)")
    UserBusinessUnitDTO toDTO(UserBusinessUnit userBusinessUnit);

    List<UserBusinessUnitDTO> toDTOList(List<UserBusinessUnit> userBusinessUnits);
}
