package com.eflo.user.domain.repository;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.User;
import com.eflo.user.domain.entity.UserBusinessUnit;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBusinessUnitRepository extends JpaRepository<UserBusinessUnit, Long> {

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    List<UserBusinessUnit> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    List<UserBusinessUnit> findByBusinessUnitId(Long businessUnitId);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    Optional<UserBusinessUnit> findByUserIdAndIsPrimaryTrue(Long userId);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    List<UserBusinessUnit> findByUserIdAndIsActiveTrue(Long userId);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    List<UserBusinessUnit> findByUserAndIsActiveTrue(User user);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    List<UserBusinessUnit> findByBusinessUnitIdAndIsActiveTrue(Long businessUnitId);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    List<UserBusinessUnit> findByBusinessUnitAndIsActiveTrue(BusinessUnit businessUnit);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    Optional<UserBusinessUnit> findByUserAndIsPrimaryTrueAndIsActiveTrue(User user);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    Optional<UserBusinessUnit> findByUserAndBusinessUnit(User user, BusinessUnit businessUnit);

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    @Query("SELECT ubu FROM UserBusinessUnit ubu WHERE ubu.user.id = :userId AND ubu.businessUnit.id = :businessUnitId")
    Optional<UserBusinessUnit> findByUserIdAndBusinessUnitId(
            @Param("userId") Long userId,
            @Param("businessUnitId") Long businessUnitId
    );

    @EntityGraph(attributePaths = {"user", "businessUnit"})
    @Query("SELECT ubu FROM UserBusinessUnit ubu WHERE " +
           "ubu.user.id = :userId AND ubu.businessUnit.id = :businessUnitId AND ubu.isActive = true")
    Optional<UserBusinessUnit> findActiveByUserIdAndBusinessUnitId(
            @Param("userId") Long userId,
            @Param("businessUnitId") Long businessUnitId
    );

    @Query("SELECT COUNT(ubu) FROM UserBusinessUnit ubu WHERE ubu.user.id = :userId AND ubu.isActive = true")
    long countActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(ubu) FROM UserBusinessUnit ubu WHERE ubu.businessUnit.id = :businessUnitId AND ubu.isActive = true")
    long countActiveByBusinessUnitId(@Param("businessUnitId") Long businessUnitId);
}
