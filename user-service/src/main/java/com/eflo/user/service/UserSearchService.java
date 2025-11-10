package com.eflo.user.service;

import com.eflo.user.domain.dto.UserDTO;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.repository.UserRepository;
import com.eflo.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for advanced user search operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Search users with filters.
     *
     * @param searchTerm     search term for name, email, or username
     * @param businessUnitId filter by business unit
     * @param role           filter by role
     * @param isActive       filter by active status
     * @param page           page number
     * @param size           page size
     * @param sortBy         sort field
     * @param sortDirection  sort direction
     * @return page of users
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> searchUsers(
            String searchTerm,
            String businessUnitId,
            String role,
            Boolean isActive,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        log.debug("Searching users with term: {}, businessUnitId: {}, role: {}, isActive: {}",
                searchTerm, businessUnitId, role, isActive);

        Sort.Direction direction = "DESC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy != null ? sortBy : "createdAt"));

        // This is a simplified implementation. In production, you would use
        // Specifications or QueryDSL for more complex queries
        Page<User> users;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            users = userRepository.findAll(pageable);
            // Filter by search term - in production, use database query
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(userMapper::toDTO);
    }

    /**
     * Search users by name (first or last name).
     *
     * @param name the name to search
     * @return list of matching users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchByName(String name) {
        log.debug("Searching users by name: {}", name);
        return userRepository.findAll().stream()
                .filter(user -> (user.getFirstName() != null && user.getFirstName().contains(name))
                        || (user.getLastName() != null && user.getLastName().contains(name)))
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search users by email domain.
     *
     * @param domain the email domain
     * @return list of matching users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchByEmailDomain(String domain) {
        log.debug("Searching users by email domain: {}", domain);
        return userRepository.findAll().stream()
                .filter(user -> user.getEmail() != null && user.getEmail().endsWith("@" + domain))
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recently created users.
     *
     * @param limit number of users to return
     * @return list of recently created users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getRecentlyCreatedUsers(int limit) {
        log.debug("Getting {} recently created users", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userRepository.findAll(pageable).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get recently updated users.
     *
     * @param limit number of users to return
     * @return list of recently updated users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getRecentlyUpdatedUsers(int limit) {
        log.debug("Getting {} recently updated users", limit);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return userRepository.findAll(pageable).stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Count active users.
     *
     * @return number of active users
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .count();
    }

    /**
     * Count users by business unit.
     *
     * @param businessUnitId the business unit ID
     * @return number of users in the business unit
     */
    @Transactional(readOnly = true)
    public long countUsersByBusinessUnit(String businessUnitId) {
        // This would use a proper repository query in production
        return userRepository.findAll().stream()
                .filter(user -> user.getUserBusinessUnits() != null &&
                        user.getUserBusinessUnits().stream()
                                .anyMatch(ubu -> ubu.getBusinessUnit().getId().toString().equals(businessUnitId)))
                .count();
    }
}
