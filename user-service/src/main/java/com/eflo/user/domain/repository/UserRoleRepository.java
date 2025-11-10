package com.eflo.user.domain.repository;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserRole;
import com.eflo.user.domain.enums.RoleSource;
import com.eflo.user.domain.enums.UserRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByUserIdAndIsActiveTrue(Long userId);

    List<UserRole> findByRoleName(UserRoleEnum roleName);

    List<UserRole> findByRoleNameAndIsActiveTrue(UserRoleEnum roleName);

    List<UserRole> findByUser(User user);

    List<UserRole> findByUserAndIsActiveTrue(User user);

    List<UserRole> findByUserAndSource(User user, RoleSource source);

    Optional<UserRole> findByUserAndRoleNameAndBusinessUnit(User user, UserRoleEnum roleName, BusinessUnit businessUnit);

    List<UserRole> findByRoleNameAndBusinessUnitAndIsActiveTrue(UserRoleEnum roleName, BusinessUnit businessUnit);

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.roleName = :roleName")
    Optional<UserRole> findByUserIdAndRoleName(
            @Param("userId") Long userId,
            @Param("roleName") UserRoleEnum roleName
    );

    @Query("SELECT ur FROM UserRole ur WHERE " +
           "ur.user.id = :userId AND ur.roleName = :roleName AND ur.isActive = true")
    Optional<UserRole> findActiveByUserIdAndRoleName(
            @Param("userId") Long userId,
            @Param("roleName") UserRoleEnum roleName
    );

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.id = :userId AND ur.source = :source")
    List<UserRole> findByUserIdAndSource(
            @Param("userId") Long userId,
            @Param("source") RoleSource source
    );

    @Query("SELECT ur FROM UserRole ur WHERE " +
           "ur.user.id = :userId AND ur.source = :source AND ur.isActive = true")
    List<UserRole> findActiveByUserIdAndSource(
            @Param("userId") Long userId,
            @Param("source") RoleSource source
    );

    @Query("SELECT ur FROM UserRole ur WHERE ur.businessUnit.id = :businessUnitId")
    List<UserRole> findByBusinessUnitId(@Param("businessUnitId") Long businessUnitId);

    @Query("SELECT ur FROM UserRole ur WHERE ur.businessUnit.id = :businessUnitId AND ur.isActive = true")
    List<UserRole> findActiveByBusinessUnitId(@Param("businessUnitId") Long businessUnitId);

    @Query("SELECT ur FROM UserRole ur WHERE " +
           "ur.user.id = :userId AND ur.businessUnit.id = :businessUnitId AND ur.isActive = true")
    List<UserRole> findActiveByUserIdAndBusinessUnitId(
            @Param("userId") Long userId,
            @Param("businessUnitId") Long businessUnitId
    );

    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.user.id = :userId AND ur.isActive = true")
    long countActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT ur.roleName FROM UserRole ur WHERE ur.user.id = :userId AND ur.isActive = true")
    List<UserRoleEnum> findActiveRoleNamesByUserId(@Param("userId") Long userId);
}
