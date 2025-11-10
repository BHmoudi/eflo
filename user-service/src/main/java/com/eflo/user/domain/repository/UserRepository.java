package com.eflo.user.domain.repository;

import com.eflo.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKeycloakId(UUID keycloakId);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmployeeNumber(String employeeNumber);

    List<User> findByIsActiveTrue();

    List<User> findByDepartment(String department);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.employeeNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.department) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.isDeleted = false")
    List<User> findAllActiveAndNotDeleted();

    @Query("SELECT u FROM User u WHERE u.keycloakUsername = :username")
    Optional<User> findByKeycloakUsername(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.isActive = true AND u.department = :department")
    List<User> findActiveByDepartment(@Param("department") String department);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.businessUnits bu " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithBusinessUnits(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.businessUnits bu " +
           "LEFT JOIN FETCH u.roles r " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithBusinessUnitsAndRoles(@Param("userId") Long userId);
}
