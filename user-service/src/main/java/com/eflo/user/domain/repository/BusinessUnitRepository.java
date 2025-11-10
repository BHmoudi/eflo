package com.eflo.user.domain.repository;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.enums.BusinessUnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessUnitRepository extends JpaRepository<BusinessUnit, Long> {

    Optional<BusinessUnit> findByCode(String code);

    List<BusinessUnit> findByRegionCode(String regionCode);

    List<BusinessUnit> findByRegionCodeAndIsActiveTrue(String regionCode);

    List<BusinessUnit> findByRegionName(String regionName);

    List<BusinessUnit> findByType(BusinessUnitType type);

    List<BusinessUnit> findByIsActiveTrue();

    @Query("SELECT bu FROM BusinessUnit bu WHERE bu.isActive = true AND bu.type = :type")
    List<BusinessUnit> findActiveByType(@Param("type") BusinessUnitType type);

    @Query("SELECT bu FROM BusinessUnit bu WHERE bu.isActive = true AND bu.regionCode = :regionCode")
    List<BusinessUnit> findActiveByRegionCode(@Param("regionCode") String regionCode);

    @Query("SELECT bu FROM BusinessUnit bu WHERE " +
           "LOWER(bu.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bu.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(bu.city) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BusinessUnit> searchBusinessUnits(@Param("searchTerm") String searchTerm);

    @Query("SELECT DISTINCT bu FROM BusinessUnit bu " +
           "LEFT JOIN FETCH bu.userAssignments ua " +
           "WHERE bu.id = :businessUnitId")
    Optional<BusinessUnit> findByIdWithUserAssignments(@Param("businessUnitId") Long businessUnitId);

    @Query("SELECT bu FROM BusinessUnit bu WHERE bu.manager.id = :managerId")
    List<BusinessUnit> findByManagerId(@Param("managerId") Long managerId);
}
