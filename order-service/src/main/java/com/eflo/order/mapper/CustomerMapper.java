package com.eflo.order.mapper;

import com.eflo.order.domain.entity.Customer;
import com.eflo.order.domain.model.dto.CustomerDTO;
import com.eflo.order.domain.model.request.CreateCustomerRequest;
import com.eflo.order.domain.model.request.UpdateCustomerRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CustomerMapper {

    public Customer toEntity(CreateCustomerRequest request) {
        return Customer.builder()
                .customerType(request.getCustomerType())
                .civility(request.getCivility())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .commercialName(request.getCommercialName())
                .companyName(request.getCompanyName())
                .siret(request.getSiret())
                .email(request.getEmail())
                .phoneMobile(request.getPhoneMobile())
                .phoneLandline(request.getPhoneLandline())
                .address(request.getAddress())
                .postalCode(request.getPostalCode())
                .city(request.getCity())
                .country(request.getCountry() != null ? request.getCountry() : "FR")
                .sa(request.getSa())
                .externalCustomerId(request.getExternalCustomerId())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    public void updateEntityFromRequest(Customer customer, UpdateCustomerRequest request) {
        if (request.getCustomerType() != null) customer.setCustomerType(request.getCustomerType());
        if (request.getCivility() != null) customer.setCivility(request.getCivility());
        if (request.getFirstName() != null) customer.setFirstName(request.getFirstName());
        if (request.getLastName() != null) customer.setLastName(request.getLastName());
        if (request.getCommercialName() != null) customer.setCommercialName(request.getCommercialName());
        if (request.getCompanyName() != null) customer.setCompanyName(request.getCompanyName());
        if (request.getSiret() != null) customer.setSiret(request.getSiret());
        if (request.getEmail() != null) customer.setEmail(request.getEmail());
        if (request.getPhoneMobile() != null) customer.setPhoneMobile(request.getPhoneMobile());
        if (request.getPhoneLandline() != null) customer.setPhoneLandline(request.getPhoneLandline());
        if (request.getAddress() != null) customer.setAddress(request.getAddress());
        if (request.getPostalCode() != null) customer.setPostalCode(request.getPostalCode());
        if (request.getCity() != null) customer.setCity(request.getCity());
        if (request.getCountry() != null) customer.setCountry(request.getCountry());
        if (request.getSa() != null) customer.setSa(request.getSa());
        if (request.getExternalCustomerId() != null) customer.setExternalCustomerId(request.getExternalCustomerId());
        if (request.getIsActive() != null) customer.setIsActive(request.getIsActive());
    }

    public CustomerDTO toDTO(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .customerType(customer.getCustomerType())
                .civility(customer.getCivility())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .commercialName(customer.getCommercialName())
                .companyName(customer.getCompanyName())
                .siret(customer.getSiret())
                .email(customer.getEmail())
                .phoneMobile(customer.getPhoneMobile())
                .phoneLandline(customer.getPhoneLandline())
                .address(customer.getAddress())
                .postalCode(customer.getPostalCode())
                .city(customer.getCity())
                .country(customer.getCountry())
                .sa(customer.getSa())
                .externalCustomerId(customer.getExternalCustomerId())
                .isActive(customer.getIsActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public List<CustomerDTO> toDTOs(List<Customer> customers) {
        return customers.stream().map(this::toDTO).collect(Collectors.toList());
    }
}
