package com.eflo.commission.mapper;

import com.eflo.commission.domain.entity.CommissionPayment;
import com.eflo.commission.domain.model.dto.CommissionPaymentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommissionPaymentMapper {

    CommissionPaymentDTO toDto(CommissionPayment payment);

    CommissionPayment toEntity(CommissionPaymentDTO dto);
}
