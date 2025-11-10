package com.eflo.user.mapper;

import com.eflo.user.domain.dto.HierarchyDTO;
import com.eflo.user.domain.entity.Hierarchy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HierarchyMapper {

    @Mapping(target = "employeeId", expression = "java(hierarchy.getEmployee() != null ? hierarchy.getEmployee().getId() : null)")
    @Mapping(target = "employeeEmail", expression = "java(hierarchy.getEmployee() != null ? hierarchy.getEmployee().getEmail() : null)")
    @Mapping(target = "employeeFullName", expression = "java(hierarchy.getEmployee() != null ? hierarchy.getEmployee().getFullName() : null)")
    @Mapping(target = "managerId", expression = "java(hierarchy.getManager() != null ? hierarchy.getManager().getId() : null)")
    @Mapping(target = "managerEmail", expression = "java(hierarchy.getManager() != null ? hierarchy.getManager().getEmail() : null)")
    @Mapping(target = "managerFullName", expression = "java(hierarchy.getManager() != null ? hierarchy.getManager().getFullName() : null)")
    @Mapping(target = "businessUnitId", expression = "java(hierarchy.getBusinessUnit() != null ? hierarchy.getBusinessUnit().getId() : null)")
    @Mapping(target = "businessUnitName", expression = "java(hierarchy.getBusinessUnit() != null ? hierarchy.getBusinessUnit().getName() : null)")
    HierarchyDTO toDTO(Hierarchy hierarchy);

    List<HierarchyDTO> toDTOList(List<Hierarchy> hierarchies);
}
