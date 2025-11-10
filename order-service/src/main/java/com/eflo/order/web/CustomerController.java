package com.eflo.order.web;

import com.eflo.order.domain.model.dto.CustomerDTO;
import com.eflo.order.domain.model.request.CreateCustomerRequest;
import com.eflo.order.domain.model.request.UpdateCustomerRequest;
import com.eflo.order.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.eflo.order.security.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers (PA and PRO)")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers (paginated)")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<Page<CustomerDTO>> getAllCustomers(Pageable pageable) {
        log.debug("REST request to get all customers - page: {}, size: {}",
                  pageable.getPageNumber(), pageable.getPageSize());
        Page<CustomerDTO> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long id) {
        log.debug("REST request to get customer by ID: {}", id);
        CustomerDTO customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<CustomerDTO> getCustomerByEmail(@PathVariable String email) {
        log.debug("REST request to get customer by email: {}", email);
        CustomerDTO customer = customerService.getCustomerByEmail(email);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/search")
    @Operation(summary = "Search customers by name, email, or company")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(@RequestParam String query) {
        log.debug("REST request to search customers with query: {}", query);
        List<CustomerDTO> customers = customerService.searchCustomers(query);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active customers")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON', 'VIEWER')")
    public ResponseEntity<List<CustomerDTO>> getActiveCustomers() {
        log.debug("REST request to get all active customers");
        List<CustomerDTO> customers = customerService.getActiveCustomers();
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    @Operation(summary = "Create a new customer")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<CustomerDTO> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("REST request to create customer: {} {}", request.getFirstName(), request.getLastName());
        Long userId = SecurityUtils.getUserId(jwt);
        CustomerDTO customer = customerService.createCustomer(request);
        log.info("Customer created successfully by user {}: {} (ID: {})",
                 userId, customer.getLastName(), customer.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing customer")
    @PreAuthorize("hasAnyRole('SALES_MANAGER', 'SALESPERSON')")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("REST request to update customer: {}", id);
        Long userId = SecurityUtils.getUserId(jwt);
        CustomerDTO customer = customerService.updateCustomer(id, request);
        log.info("Customer updated successfully by user {}: {} (ID: {})",
                 userId, customer.getLastName(), customer.getId());
        return ResponseEntity.ok(customer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer (soft delete)")
    @PreAuthorize("hasRole('SALES_MANAGER')")
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("REST request to delete customer: {}", id);
        Long userId = SecurityUtils.getUserId(jwt);
        customerService.deleteCustomer(id);
        log.info("Customer deleted successfully by user {}: {}", userId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extract user ID from JWT token
     * Follows the same pattern as OrderController
     */
    // User extraction centralized in SecurityUtils
}
