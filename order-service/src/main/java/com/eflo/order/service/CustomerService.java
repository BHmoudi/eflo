package com.eflo.order.service;

import com.eflo.order.domain.entity.Customer;
import com.eflo.order.domain.model.dto.CustomerDTO;
import com.eflo.order.domain.model.request.CreateCustomerRequest;
import com.eflo.order.domain.model.request.UpdateCustomerRequest;
import com.eflo.order.domain.repository.CustomerRepository;
import com.eflo.order.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    /**
     * Create a new customer
     */
    @Transactional
    public CustomerDTO createCustomer(CreateCustomerRequest request) {
        log.info("Creating new customer: {} {}", request.getFirstName(), request.getLastName());

        // Check if email already exists
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            customerRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                throw new RuntimeException("Customer with email " + request.getEmail() + " already exists");
            });
        }

        // Check if external customer ID already exists
        if (request.getExternalCustomerId() != null && !request.getExternalCustomerId().isBlank()) {
            customerRepository.findByExternalCustomerId(request.getExternalCustomerId()).ifPresent(existing -> {
                throw new RuntimeException("Customer with external ID " + request.getExternalCustomerId() + " already exists");
            });
        }

        // Create customer entity
        Customer customer = customerMapper.toEntity(request);

        // Save customer
        customer = customerRepository.save(customer);

        log.info("Customer created successfully: {} (ID: {})",
                 customer.getLastName(), customer.getId());
        return customerMapper.toDTO(customer);
    }

    /**
     * Get all customers (paginated)
     */
    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(Pageable pageable) {
        log.debug("Fetching all customers - page: {}, size: {}",
                  pageable.getPageNumber(), pageable.getPageSize());
        Page<Customer> customers = customerRepository.findAll(pageable);
        return customers.map(customerMapper::toDTO);
    }

    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) {
        log.debug("Fetching customer by ID: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return customerMapper.toDTO(customer);
    }

    /**
     * Get customer by email
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByEmail(String email) {
        log.debug("Fetching customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found with email: " + email));
        return customerMapper.toDTO(customer);
    }

    /**
     * Search customers by query string
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomers(String query) {
        log.debug("Searching customers with query: {}", query);
        List<Customer> customers = customerRepository.searchCustomers(query);
        return customerMapper.toDTOs(customers);
    }

    /**
     * Get active customers
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getActiveCustomers() {
        log.debug("Fetching all active customers");
        List<Customer> customers = customerRepository.findByIsActiveTrue();
        return customerMapper.toDTOs(customers);
    }

    /**
     * Update an existing customer
     */
    @Transactional
    public CustomerDTO updateCustomer(Long customerId, UpdateCustomerRequest request) {
        log.info("Updating customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        // Check if email is being changed and if new email already exists
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            customerRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(customerId)) {
                    throw new RuntimeException("Customer with email " + request.getEmail() + " already exists");
                }
            });
        }

        // Check if external customer ID is being changed and if new ID already exists
        if (request.getExternalCustomerId() != null &&
            !request.getExternalCustomerId().equals(customer.getExternalCustomerId())) {
            customerRepository.findByExternalCustomerId(request.getExternalCustomerId()).ifPresent(existing -> {
                if (!existing.getId().equals(customerId)) {
                    throw new RuntimeException("Customer with external ID " + request.getExternalCustomerId() + " already exists");
                }
            });
        }

        // Update fields
        customerMapper.updateEntityFromRequest(customer, request);

        customer = customerRepository.save(customer);

        log.info("Customer updated successfully: {} (ID: {})",
                 customer.getLastName(), customer.getId());
        return customerMapper.toDTO(customer);
    }

    /**
     * Delete customer (soft delete by setting isActive to false)
     */
    @Transactional
    public void deleteCustomer(Long customerId) {
        log.info("Deleting customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));

        // Soft delete by setting isActive to false
        customer.setIsActive(false);
        customerRepository.save(customer);

        log.info("Customer {} deleted successfully (soft delete)", customer.getLastName());
    }
}
