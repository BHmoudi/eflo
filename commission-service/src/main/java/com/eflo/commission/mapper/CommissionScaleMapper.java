package com.eflo.commission.mapper;

import com.eflo.commission.domain.entity.CommissionScale;
import com.eflo.commission.domain.entity.CommissionScaleTier;
import com.eflo.commission.domain.model.dto.CommissionScaleDTO;
import com.eflo.commission.domain.model.dto.CommissionScaleTierDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommissionScaleMapper {

    CommissionScaleDTO toDto(CommissionScale scale);

    CommissionScale toEntity(CommissionScaleDTO dto);

    CommissionScaleTierDTO toDto(CommissionScaleTier tier);

    CommissionScaleTier toEntity(CommissionScaleTierDTO dto);
}
