package com.eflo.user.mapper;

import com.eflo.user.domain.dto.BusinessUnitDTO;
import com.eflo.user.domain.dto.CreateBusinessUnitRequest;
import com.eflo.user.domain.entity.BusinessUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BusinessUnitMapper {

    @Mapping(source = "manager.id", target = "managerId")
    BusinessUnitDTO toDTO(BusinessUnit businessUnit);

    List<BusinessUnitDTO> toDTOList(List<BusinessUnit> businessUnits);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "managerName", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "closingDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "userAssignments", ignore = true)
    @Mapping(target = "hierarchies", ignore = true)
    @Mapping(target = "country", defaultValue = "FR")
    @Mapping(target = "settings", constant = "{}")
    BusinessUnit toEntity(CreateBusinessUnitRequest request);
}
