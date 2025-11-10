package com.eflo.user.domain.repository;

import com.eflo.user.domain.entity.BusinessUnit;
import com.eflo.user.domain.entity.Hierarchy;
import com.eflo.user.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HierarchyRepository extends JpaRepository<Hierarchy, Long> {

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByEmployeeId(Long employeeId);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByEmployee(User employee);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByEmployeeAndIsActiveTrue(User employee);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByManagerId(Long managerId);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByManager(User manager);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByManagerAndIsActiveTrue(User manager);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByManagerIdAndIsActiveTrue(Long managerId);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByBusinessUnitId(Long businessUnitId);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByBusinessUnit(BusinessUnit businessUnit);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByBusinessUnitAndIsActiveTrue(BusinessUnit businessUnit);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByBusinessUnitIdAndIsActiveTrue(Long businessUnitId);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    List<Hierarchy> findByManagerAndBusinessUnitAndIsActiveTrue(User manager, BusinessUnit businessUnit);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    @Query("SELECT h FROM Hierarchy h WHERE h.employee.id = :employeeId AND h.isActive = true")
    List<Hierarchy> findActiveByEmployeeId(@Param("employeeId") Long employeeId);

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    @Query("SELECT h FROM Hierarchy h WHERE h.employee.id = :employeeId AND h.manager.id = :managerId")
    Optional<Hierarchy> findByEmployeeIdAndManagerId(
            @Param("employeeId") Long employeeId,
            @Param("managerId") Long managerId
    );

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    @Query("SELECT h FROM Hierarchy h WHERE " +
           "h.employee.id = :employeeId AND h.manager.id = :managerId AND h.isActive = true")
    Optional<Hierarchy> findActiveByEmployeeIdAndManagerId(
            @Param("employeeId") Long employeeId,
            @Param("managerId") Long managerId
    );

    @EntityGraph(attributePaths = {"employee", "manager", "businessUnit"})
    @Query("SELECT h FROM Hierarchy h WHERE " +
           "h.employee.id = :employeeId AND h.businessUnit.id = :businessUnitId AND h.isActive = true")
    Optional<Hierarchy> findActiveByEmployeeIdAndBusinessUnitId(
            @Param("employeeId") Long employeeId,
            @Param("businessUnitId") Long businessUnitId
    );

    @Query("SELECT COUNT(h) FROM Hierarchy h WHERE h.manager.id = :managerId AND h.isActive = true")
    long countActiveSubordinatesByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT h FROM Hierarchy h WHERE h.employee.id = :employeeId AND h.level = :level AND h.isActive = true")
    List<Hierarchy> findActiveByEmployeeIdAndLevel(
            @Param("employeeId") Long employeeId,
            @Param("level") Integer level
    );
}
