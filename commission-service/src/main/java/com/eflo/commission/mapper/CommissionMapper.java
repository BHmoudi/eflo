package com.eflo.commission.mapper;

import com.eflo.commission.domain.entity.Commission;
import com.eflo.commission.domain.model.dto.CommissionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommissionMapper {

    @Mapping(target = "commissionScaleId", source = "commissionScale.id")
    CommissionDTO toDto(Commission commission);

    @Mapping(target = "commissionScale", ignore = true)
    Commission toEntity(CommissionDTO dto);
}
